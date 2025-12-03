// # android/app/src/main/java/com/websmithing/gpstracker2/data/repository/LocationRepositoryImpl.kt
package com.websmithing.gpstracker2.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import timber.log.Timber
import com.google.android.gms.location.Priority
import com.websmithing.gpstracker2.network.ApiService
import com.websmithing.gpstracker2.util.PermissionChecker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.roundToInt

/**
 * Implementation of the [LocationRepository] interface.
 *
 * This class handles:
 * - Retrieving location from Google Play Services
 * - Calculating distance traveled
 * - Persisting location state between app sessions
 * - Formatting and uploading location data to a remote server
 * - Managing StateFlows for real-time UI updates
 *
 * It uses:
 * - [FusedLocationProviderClient] for location data
 * - Retrofit/OkHttp for network communication
 * - SharedPreferences for local state persistence
 * - Coroutines for asynchronous operations
 * - StateFlows for reactive data updates
 */
@Singleton
class LocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val okHttpClient: OkHttpClient,
    private val retrofitBuilder: Retrofit.Builder,
    private val settingsRepository: SettingsRepository,
    private val permissionChecker: PermissionChecker
) : LocationRepository {

    // Initialize SharedPreferences
    /**
     * SharedPreferences instance for persisting location data between app sessions
     */
    private val sharedPreferences: SharedPreferences =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // --- State Flows ---
    /**
     * Internal mutable state flow for the latest location
     */
    private val _latestLocation = MutableStateFlow<Location?>(null)
    
    /**
     * Publicly exposed immutable state flow of the latest location
     */
    override val latestLocation: StateFlow<Location?> = _latestLocation.asStateFlow()

    /**
     * Internal mutable state flow for the total distance in meters
     */
    private val _totalDistance = MutableStateFlow(0f)
    
    /**
     * Publicly exposed immutable state flow of the total distance
     */
    override val totalDistance: StateFlow<Float> = _totalDistance.asStateFlow()

    /**
     * Internal mutable state flow for the upload status
     */
    private val _lastUploadStatus = MutableStateFlow<UploadStatus>(UploadStatus.Idle)
    
    /**
     * Publicly exposed immutable state flow of the upload status
     */
    override val lastUploadStatus: StateFlow<UploadStatus> = _lastUploadStatus.asStateFlow()

    /**
     * Initializes the repository with fresh state.
     * 
     * In a production app, we might want to restore state from persistent storage
     * in case the app was restarted during an active tracking session.
     */
    init {
        Timber.d("LocationRepositoryImpl initialized.")
    }

    /**
     * Gets the current device location using the FusedLocationProviderClient.
     *
     * This method uses a suspendCancellableCoroutine to convert the callback-based
     * FusedLocationProviderClient API into a coroutine-compatible suspending function.
     *
     * @return The current location, or null if location could not be determined
     * @throws SecurityException If location permissions are not granted
     */
    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Location? = withContext(Dispatchers.IO) {
        if (!permissionChecker.hasLocationPermissions()) {
            Timber.e("Attempted to get location without permission")
            throw SecurityException("Location permission not granted.")
        }

        suspendCancellableCoroutine { continuation ->
            Timber.d("Requesting current location...")
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { location: Location? ->
                    if (continuation.isActive) continuation.resume(location)
                }
                .addOnFailureListener { e ->
                    Timber.e(e, "Location failure")
                    if (continuation.isActive) continuation.resumeWithException(e)
                }
                .addOnCanceledListener {
                    Timber.d("Location request cancelled")
                    if (continuation.isActive) continuation.cancel()
                }
            continuation.invokeOnCancellation { /* Optional: Cancel location request */ }
        }
    }

    /**
     * Uploads location data to a remote server.
     *
     * This method:
     * 1. Formats and encodes location data
     * 2. Determines the correct server URL (with fallbacks)
     * 3. Creates a dynamic Retrofit service with the target URL
     * 4. Makes the network request
     * 5. Processes the response and updates the upload status flow
     *
     * @param location The location data to upload
     * @param username The username identifying this tracker
     * @param appId Unique identifier for this device/installation
     * @param sessionId Unique identifier for this tracking session
     * @param eventType Type of tracking event (e.g., "start", "stop", "update")
     * @return true if upload was successful, false otherwise
     */
    override suspend fun uploadLocationData(
        location: Location,
        username: String,
        appId: String,
        sessionId: String,
        eventType: String
    ): Boolean = withContext(Dispatchers.IO) {
        var success = false
        var errorMessage: String? = null
        try {
            Timber.tag(TAG).i("REPO-CRITICAL: Starting location upload process")
            
            // Format and encode data
            val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
                timeZone = TimeZone.getDefault()
            }.format(Date(location.time))
            val encodedDate = try { URLEncoder.encode(formattedDate, "UTF-8") } catch (e: Exception) { formattedDate }
            val encodedMethod = try { URLEncoder.encode(location.provider ?: "unknown", "UTF-8") } catch (e: Exception) { location.provider ?: "unknown" }

            // Prepare numeric data
            val speedMph = (location.speed * 2.2369).roundToInt()
            val accuracyMeters = location.accuracy.roundToInt()
            val altitudeMeters = location.altitude.roundToInt()
            val direction = location.bearing.roundToInt()
            val currentTotalDistanceMeters = _totalDistance.value
            val totalDistanceMiles = currentTotalDistanceMeters / 1609.34f // Convert meters to miles for API
            
            // Get server URL
            var targetUrl = settingsRepository.getCurrentWebsiteUrl()
            Timber.tag(TAG).i("REPO-CRITICAL: Got URL from settings: $targetUrl")
            if (targetUrl.isBlank()) {
                 Timber.tag(TAG).e("Website URL is blank. Using default URL.")
                 targetUrl = "https://www.websmithing.com/gpstracker/api/locations/update"
            }
            
            // Ensure URL is properly formatted
            if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://")) {
                targetUrl = "https://" + targetUrl
                Timber.tag(TAG).d("Added https:// to URL: $targetUrl")
            }
            
            // Ensure URL has the correct endpoint
            if (!targetUrl.contains("/update") && !targetUrl.contains("/api/")) {
                if (targetUrl.endsWith("/")) {
                    targetUrl += "gpstracker/api/locations/update"
                } else {
                    targetUrl += "/gpstracker/api/locations/update"
                }
                Timber.tag(TAG).d("Appended default endpoint: $targetUrl")
            }

            // Parse URL for Retrofit
            val httpUrl = targetUrl.toHttpUrlOrNull()
            if (httpUrl == null) {
                Timber.tag(TAG).e("Invalid URL format after processing: $targetUrl")
                return@withContext false
            }
            
            // Build base URL
            val pathSegments = httpUrl.pathSegments.filter { it.isNotEmpty() }
            val baseUrl: String
            
            if (pathSegments.size <= 1) {
                val tempUrl = httpUrl.newBuilder().query(null).fragment(null).build().toString()
                baseUrl = if (tempUrl.endsWith("/")) tempUrl else "$tempUrl/"
            } else {
                val basePath = "/" + pathSegments.dropLast(1).joinToString("/") + "/"
                baseUrl = httpUrl.newBuilder()
                    .encodedPath(basePath)
                    .query(null)
                    .fragment(null)
                    .build()
                    .toString()
            }
            
            // Ensure baseUrl ends with a slash
            val finalBaseUrl = if (!baseUrl.endsWith("/")) "$baseUrl/" else baseUrl
            Timber.tag(TAG).d("Using base URL: $finalBaseUrl")

            // Create API service
            val dynamicApiService = retrofitBuilder
                .baseUrl(finalBaseUrl)
                .build()
                .create(ApiService::class.java)

            // Make API call with error handling
            val response = try {
                Timber.tag(TAG).i("REPO-CRITICAL: About to make API call with Retrofit")
                dynamicApiService.updateLocation(
                    latitude = location.latitude.toString(),
                    longitude = location.longitude.toString(),
                    speed = speedMph,
                    direction = direction,
                    date = encodedDate,
                    locationMethod = encodedMethod,
                    username = username,
                    phoneNumber = appId,
                    sessionId = sessionId,
                    accuracy = accuracyMeters,
                    extraInfo = altitudeMeters.toString(),
                    eventType = eventType
                )
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "REPO-CRITICAL: Exception during API call")
                return@withContext false
            }
            
            Timber.tag(TAG).i("REPO-CRITICAL: Got response code: ${response.code()}, message: ${response.message()}")

            // Process response
            val responseBody = response.body()
            if (response.isSuccessful && responseBody != null && responseBody != "-1") {
                Timber.tag(TAG).i("Upload successful. Server response: $responseBody")
                success = true
                return@withContext true
            } else {
                // Log more details about the failure
                val failureReason = when {
                    !response.isSuccessful -> {
                        val errorBodyString = try { response.errorBody()?.string() } catch (e: Exception) { "Error reading error body: ${e.message}" }
                        "HTTP error. Code: ${response.code()}, Message: ${response.message()}, Body: $errorBodyString"
                    }
                    responseBody == null -> "Response body was null."
                    responseBody == "-1" -> "Server returned error code: -1."
                    else -> "Unexpected successful response body: $responseBody"
                }
                Timber.tag(TAG).e("Upload failed: $failureReason")
                errorMessage = failureReason
                return@withContext false
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Unhandled exception during upload")
            errorMessage = e.localizedMessage ?: "Unknown upload error"
            success = false
        } finally {
             // Update the status flow regardless of outcome
             Timber.tag(TAG).d("Finally block: success=$success, errorMessage='$errorMessage'")
             _lastUploadStatus.value = if (success) UploadStatus.Success else UploadStatus.Failure(errorMessage)
        }
        return@withContext success
    }

    /**
     * Retrieves the previously saved location from SharedPreferences.
     *
     * @return The previously saved location, or null if no location was saved
     */
    override suspend fun getPreviousLocation(): Location? = withContext(Dispatchers.IO) {
        val lat = sharedPreferences.getFloat(KEY_PREVIOUS_LATITUDE, 0f)
        val lon = sharedPreferences.getFloat(KEY_PREVIOUS_LONGITUDE, 0f)

        if (lat != 0f && lon != 0f) {
            Location("").apply {
                latitude = lat.toDouble()
                longitude = lon.toDouble()
            }
        } else {
            null
        }
    }

    /**
     * Saves the current location and updates distance calculations.
     *
     * This method:
     * 1. Retrieves the previous location from the state flow
     * 2. Calculates the distance increment if there was a previous location
     * 3. Updates the total distance state flow
     * 4. Updates the latest location state flow
     * 5. Persists the current location to SharedPreferences
     *
     * @param location The new location to save
     */
    override suspend fun saveAsPreviousLocation(location: Location) = withContext(Dispatchers.IO) {
        val previousLocation = _latestLocation.value

        if (previousLocation != null) {
            val distanceIncrement = location.distanceTo(previousLocation) // Distance in meters
            _totalDistance.update { it + distanceIncrement }
            Timber.d("Distance updated: +${distanceIncrement}m, Total: ${_totalDistance.value}m")
        } else {
             Timber.d("First location received, distance starts at 0.")
        }

        // Update the latest location flow
        _latestLocation.value = location

        // Persist coordinates for potential app restart
        sharedPreferences.edit().apply {
            putFloat(KEY_PREVIOUS_LATITUDE, location.latitude.toFloat())
            putFloat(KEY_PREVIOUS_LONGITUDE, location.longitude.toFloat())
            apply()
        }
        Timber.tag(TAG).d("Updated location state: Lat=${location.latitude}, Lon=${location.longitude}, TotalDist=${_totalDistance.value}m")
    }

    /**
     * Resets all location state for a new tracking session.
     *
     * This method:
     * 1. Clears the latest location state flow
     * 2. Resets the total distance to zero
     * 3. Sets the upload status to Idle
     * 4. Removes persisted location data from SharedPreferences
     */
     override suspend fun resetLocationState() = withContext(Dispatchers.IO) {
        _latestLocation.value = null
        _totalDistance.value = 0f
        _lastUploadStatus.value = UploadStatus.Idle
        sharedPreferences.edit().apply {
            remove(KEY_PREVIOUS_LATITUDE)
            remove(KEY_PREVIOUS_LONGITUDE)
            apply()
        }
        Timber.i("Location state reset.")
    }

    /**
     * Constants used by this repository implementation
     */
    companion object {
        private const val TAG = "LocationRepository"
        private const val PREFS_NAME = "com.websmithing.gpstracker2.location_prefs"
        private const val KEY_PREVIOUS_LATITUDE = "previousLatitude"
        private const val KEY_PREVIOUS_LONGITUDE = "previousLongitude"
    }
}