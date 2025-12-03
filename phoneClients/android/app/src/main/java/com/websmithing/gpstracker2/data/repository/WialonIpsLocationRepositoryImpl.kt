package com.websmithing.gpstracker2.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.websmithing.gpstracker2.util.PermissionChecker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.abs
import kotlin.math.roundToInt


/**
 * Implementation of the [LocationRepository] interface with Wialon IPS 2.0 protocol.
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
 * - TCP/IP socket for network communication
 * - SharedPreferences for local state persistence
 * - Coroutines for asynchronous operations
 * - StateFlows for reactive data updates
 *
 * @link https://extapi.wialon.com/hw/cfg/Wialon%20IPS_en_v_2_0.pdf
 *
 * @author binakot
 */
@Singleton
class WialonIpsLocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val settingsRepository: SettingsRepository,
    private val permissionChecker: PermissionChecker
) : LocationRepository {

    // Initialize SharedPreferences
    /**
     * SharedPreferences instance for persisting location data between app sessions
     */
    private val sharedPreferences: SharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

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

    private val dateFormatter = SimpleDateFormat("ddMMyy", Locale.US)
    private val timeFormatter = SimpleDateFormat("HHmmss", Locale.US)

    /**
     * Initializes the repository with fresh state.
     *
     * In a production app, we might want to restore state from persistent storage
     * in case the app was restarted during an active tracking session.
     */
    init {
        dateFormatter.timeZone = TimeZone.getTimeZone("UTC")
        timeFormatter.timeZone = TimeZone.getTimeZone("UTC")

        Timber.d("WialonIpsLocationRepositoryImpl initialized.")
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

            // Get server address
            var serverAddress = settingsRepository.getCurrentWebsiteUrl()
            Timber.tag(TAG).i("REPO-CRITICAL: Got server from settings: $serverAddress")
            if (serverAddress.isBlank()) {
                Timber.tag(TAG).e("Server address is blank. Using Waliot by default...")
                serverAddress = "device.waliot.com:30032"
            }

            val parts = serverAddress.split(":")
            val host = parts.getOrNull(0) ?: DEFAULT_HOST
            val port = parts.getOrNull(1)?.toIntOrNull() ?: DEFAULT_PORT

            try {
                Socket(host, port).use { socket ->
                    // Login Packet
                    val loginMessage = "#L#$PROTOCOL_VERSION;$username;$DEFAULT_PASSWORD"
                    val loginBytes = loginMessage.toByteArray(Charsets.UTF_8)
                    val loginCrc = toHex(calculate(loginBytes))
                    val fullLoginMessage = "$loginMessage;$loginCrc\r\n"

                    socket.getOutputStream().write(fullLoginMessage.toByteArray(Charsets.UTF_8))

                    val loginResponse = socket.getInputStream().bufferedReader().readLine()
                    if (!loginResponse.startsWith("#AL#1")) {
                        Timber.tag(TAG).e("Login failed: $loginResponse")
                        return@withContext false
                    }

                    // Extended Data Packet
                    val date = dateFormatter.format(location.time)
                    val time = timeFormatter.format(location.time)
                    val lat1 = decimalToDms(location.latitude, false)
                    val lat2 = latitudeToHemisphere(location.latitude)
                    val lon1 = decimalToDms(location.longitude, true)
                    val lon2 = longitudeToHemisphere(location.longitude)
                    val speed = (location.speed * 3.6).roundToInt()
                    val course = location.bearing.roundToInt()
                    val alt = location.altitude.roundToInt()
                    val sats = location.extras?.getInt("satellites")?.takeIf { it != 0 }?.toString() ?: NO_VALUE
                    val hdop = location.extras?.getDouble("hdop")?.takeIf { it != 0.0 }?.toString() ?: NO_VALUE
                    val inputs = NO_VALUE
                    val outputs = NO_VALUE
                    val adc = NO_VALUE
                    val iButton = NO_VALUE
                    val params = "accuracy:2:${location.accuracy},provider:3:${location.provider}"

                    val dataPayload = listOf(
                        date, time, lat1, lat2, lon1, lon2, speed, course, alt, sats, hdop,
                        inputs, outputs, adc, iButton, params
                    ).joinToString(";")

                    val dataMessage = "#D#$dataPayload"
                    val dataBytes = dataMessage.toByteArray(Charsets.UTF_8)
                    val dataCrc = toHex(calculate(dataBytes))
                    val fullDataMessage = "$dataMessage;$dataCrc\r\n"

                    socket.getOutputStream().write(fullDataMessage.toByteArray(Charsets.UTF_8))

                    val dataResponse = socket.getInputStream().bufferedReader().readLine()
                    if (!dataResponse.startsWith("#AD#1")) {
                        Timber.tag(TAG).e("Upload failed: $dataResponse")
                        return@withContext false
                    }

                    success = true
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "REPO-CRITICAL: Socket connection failed")
                return@withContext false
            }

            Timber.tag(TAG).i("REPO-CRITICAL: Location upload process is done successfully")

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
     * DMS: degrees minutes seconds
     */
    fun decimalToDms(coordinate: Double, isLon: Boolean): String {
        val absCoordinate = abs(coordinate)
        val degrees = absCoordinate.toInt()
        val minutes = (absCoordinate - degrees) * 60.0
        val dmm = degrees * 100 + minutes

        return if (isLon) {
            String.format(Locale.US, "%09.5f", dmm)
        } else {
            String.format(Locale.US, "%08.5f", dmm)
        }
    }

    fun latitudeToHemisphere(latitude: Double): String {
        return if (latitude >= 0) "N" else "S"
    }

    fun longitudeToHemisphere(longitude: Double): String {
        return if (longitude >= 0) "E" else "W"
    }

    /**
     * Constants used by this repository implementation
     */
    companion object {
        private const val TAG = "LocationRepository"
        private const val PREFS_NAME = "com.websmithing.gpstracker2.location_prefs"
        private const val KEY_PREVIOUS_LATITUDE = "previousLatitude"
        private const val KEY_PREVIOUS_LONGITUDE = "previousLongitude"

        private const val DEFAULT_HOST = "device.waliot.com"
        private const val DEFAULT_PORT = 30032
        private const val PROTOCOL_VERSION = "2.0"
        private const val NO_VALUE = "NA"
        private const val DEFAULT_PASSWORD = NO_VALUE

        private val table = intArrayOf(
            0x0000, 0xC0C1, 0xC181, 0x0140, 0xC301, 0x03C0, 0x0280, 0xC241,
            0xC601, 0x06C0, 0x0780, 0xC741, 0x0500, 0xC5C1, 0xC481, 0x0440,
            0xCC01, 0x0CC0, 0x0D80, 0xCD41, 0x0F00, 0xCFC1, 0xCE81, 0x0E40,
            0x0A00, 0xCAC1, 0xCB81, 0x0B40, 0xC901, 0x09C0, 0x0880, 0xC841,
            0xD801, 0x18C0, 0x1980, 0xD941, 0x1B00, 0xDBC1, 0xDA81, 0x1A40,
            0x1E00, 0xDEC1, 0xDF81, 0x1F40, 0xDD01, 0x1DC0, 0x1C80, 0xDC41,
            0x1400, 0xD4C1, 0xD581, 0x1540, 0xD701, 0x17C0, 0x1680, 0xD641,
            0xD201, 0x12C0, 0x1380, 0xD341, 0x1100, 0xD1C1, 0xD081, 0x1040,
            0xF001, 0x30C0, 0x3180, 0xF141, 0x3300, 0xF3C1, 0xF281, 0x3240,
            0x3600, 0xF6C1, 0xF781, 0x3740, 0xF501, 0x35C0, 0x3480, 0xF441,
            0x3C00, 0xFCC1, 0xFD81, 0x3D40, 0xFF01, 0x3FC0, 0x3E80, 0xFE41,
            0xFA01, 0x3AC0, 0x3B80, 0xFB41, 0x3900, 0xF9C1, 0xF881, 0x3840,
            0x2800, 0xE8C1, 0xE981, 0x2940, 0xEB01, 0x2BC0, 0x2A80, 0xEA41,
            0xEE01, 0x2EC0, 0x2F80, 0xEF41, 0x2D00, 0xEDC1, 0xEC81, 0x2C40,
            0xE401, 0x24C0, 0x2580, 0xE541, 0x2700, 0xE7C1, 0xE681, 0x2640,
            0x2200, 0xE2C1, 0xE381, 0x2340, 0xE101, 0x21C0, 0x2080, 0xE041,
            0xA001, 0x60C0, 0x6180, 0xA141, 0x6300, 0xA3C1, 0xA281, 0x6240,
            0x6600, 0xA6C1, 0xA781, 0x6740, 0xA501, 0x65C0, 0x6480, 0xA441,
            0x6C00, 0xACC1, 0xAD81, 0x6D40, 0xAF01, 0x6FC0, 0x6E80, 0xAE41,
            0xAA01, 0x6AC0, 0x6B80, 0xAB41, 0x6900, 0xA9C1, 0xA881, 0x6840,
            0x7800, 0xB8C1, 0xB981, 0x7940, 0xBB01, 0x7BC0, 0x7A80, 0xBA41,
            0xBE01, 0x7EC0, 0x7F80, 0xBF41, 0x7D00, 0xBDC1, 0xBC81, 0x7C40,
            0xB401, 0x74C0, 0x7580, 0xB541, 0x7700, 0xB7C1, 0xB681, 0x7640,
            0x7200, 0xB2C1, 0xB381, 0x7340, 0xB101, 0x71C0, 0x7080, 0xB041,
            0x5000, 0x90C1, 0x9181, 0x5140, 0x9301, 0x53C0, 0x5280, 0x9241,
            0x9601, 0x56C0, 0x5780, 0x9741, 0x5500, 0x95C1, 0x9481, 0x5440,
            0x9C01, 0x5CC0, 0x5D80, 0x9D41, 0x5F00, 0x9FC1, 0x9E81, 0x5E40,
            0x5A00, 0x9AC1, 0x9B81, 0x5B40, 0x9901, 0x59C0, 0x5880, 0x9841,
            0x8801, 0x48C0, 0x4980, 0x8941, 0x4B00, 0x8BC1, 0x8A81, 0x4A40,
            0x4E00, 0x8EC1, 0x8F81, 0x4F40, 0x8D01, 0x4DC0, 0x4C80, 0x8C41,
            0x4400, 0x84C1, 0x8581, 0x4540, 0x8701, 0x47C0, 0x4680, 0x8641,
            0x8201, 0x42C0, 0x4380, 0x8341, 0x4100, 0x81C1, 0x8081, 0x4040
        )

        fun calculate(data: ByteArray): Int {
            var crc = 0
            for (b in data) {
                val index = (crc xor (b.toInt() and 0xFF)) and 0xFF
                crc = (crc shr 8) xor table[index]
            }
            return crc and 0xFFFF
        }

        fun toHex(crc: Int): String {
            val high = (crc shr 8) and 0xFF
            val low = crc and 0xFF
            return "%02X%02X".format(high, low)
        }
    }
}