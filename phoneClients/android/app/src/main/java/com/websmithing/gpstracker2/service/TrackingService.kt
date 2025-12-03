// # android/app/src/main/java/com/websmithing/gpstracker2/service/TrackingService.kt
package com.websmithing.gpstracker2.service

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.websmithing.gpstracker2.R
import com.websmithing.gpstracker2.data.repository.LocationRepository
import com.websmithing.gpstracker2.data.repository.SettingsRepository
import com.websmithing.gpstracker2.di.SettingsRepositoryEntryPoint
import com.websmithing.gpstracker2.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Foreground service responsible for location tracking.
 *
 * This service handles:
 * - Starting and stopping location updates via FusedLocationProviderClient
 * - Processing location data in a background thread
 * - Uploading location data to the remote server
 * - Managing wake locks to ensure tracking continues even when the device is in doze mode
 * - Displaying a persistent notification to inform the user of active tracking
 * - Maintaining the service across app termination and device reboots
 *
 * The service is integrated with Hilt for dependency injection and uses a combination
 * of coroutines (for repository operations) and a single-thread executor for background tasks.
 */
@AndroidEntryPoint
class TrackingService : Service() {

    /**
     * Location provider client for requesting location updates
     */
    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    /**
     * Repository for managing location data operations
     */
    @Inject
    lateinit var locationRepository: LocationRepository

    /**
     * Repository for managing app settings
     */
    @Inject
    lateinit var settingsRepository: SettingsRepository

    /**
     * HTTP client for network operations
     */
    @Inject
    lateinit var okHttpClient: OkHttpClient

    /**
     * Callback for receiving location updates
     */
    private var locationCallback: LocationCallback? = null

    /**
     * Executor for running location processing tasks in the background
     */
    private var backgroundExecutor: ExecutorService? = null

    /**
     * Wake lock to keep CPU running during tracking
     */
    private var wakeLock: PowerManager.WakeLock? = null

    /**
     * Constants used by the service
     */
    companion object {
        /**
         * Intent action to start the service
         */
        const val ACTION_START_SERVICE = "ACTION_START_SERVICE"

        /**
         * Intent action to stop the service
         */
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"

        /**
         * ID for the notification channel
         */
        private const val NOTIFICATION_CHANNEL_ID = "tracking_channel"

        /**
         * Name for the notification channel
         */
        private const val NOTIFICATION_CHANNEL_NAME = "GPS Tracking"

        /**
         * ID for the service notification
         */
        private const val NOTIFICATION_ID = 1
    }

    override fun attachBaseContext(base: Context) {
        val entryPoint = EntryPointAccessors.fromApplication(
            base.applicationContext,
            SettingsRepositoryEntryPoint::class.java
        )
        val repo = entryPoint.getSettingsRepository()
        val newCtx = LocaleHelper.onAttach(base, repo)
        super.attachBaseContext(newCtx)
    }

    /**
     * Called when the service is first created.
     *
     * Initializes the notification channel, wake lock, and background executor.
     */
    override fun onCreate() {
        super.onCreate()
        Timber.d("TrackingService onCreate")
        createNotificationChannel()
        createWakeLock()
        // Initialize the background executor
        backgroundExecutor = Executors.newSingleThreadExecutor()
        Timber.d("Background executor initialized.")

        // Add a direct test to check connectivity
        Thread {
            try {
                Timber.i("DIRECT-TEST: Starting direct test of connectivity in onCreate")
                val testRetrofit = Retrofit.Builder()
                    .baseUrl("https://www.google.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(
                        OkHttpClient.Builder()
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(10, TimeUnit.SECONDS)
                            .build()
                    )
                    .build()

                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()

                val request = okhttp3.Request.Builder()
                    .url("https://www.google.com")
                    .build()

                try {
                    val response = okHttpClient.newCall(request).execute()
                    Timber.i("DIRECT-TEST: Direct HTTP request to Google completed with code: ${response.code}")
                } catch (e: Exception) {
                    Timber.e(e, "DIRECT-TEST: Failed to make direct HTTP request to Google")
                }
            } catch (e: Exception) {
                Timber.e(e, "DIRECT-TEST: Exception in direct test")
            }
        }.start()
    }

    /**
     * Creates a partial wake lock to keep the CPU running during tracking
     */
    private fun createWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "GpsTracker::LocationTrackingWakeLock"
        ).apply {
            setReferenceCounted(false)
        }
        Timber.d("Wake lock created")
    }

    /**
     * Called every time an intent is sent to the service.
     *
     * Handles service start/stop requests and manages the foreground state.
     *
     * @param intent The intent sent to the service
     * @param flags Additional data about this start request
     * @param startId A unique integer representing this specific request to start
     * @return [START_STICKY] to indicate that the service should be restarted if killed
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("TrackingService onStartCommand: ${intent?.action}")
        when (intent?.action) {
            ACTION_START_SERVICE -> {
                Timber.d("ACTION_START_SERVICE received")
                startForeground(NOTIFICATION_ID, createNotification())
                startLocationUpdates()
            }

            ACTION_STOP_SERVICE -> {
                Timber.d("ACTION_STOP_SERVICE received")
                stopLocationUpdates()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }

            else -> {
                // If service is restarted after being killed, restart location updates
                Timber.d("Service restarted without specific action. Re-initializing location updates.")
                startForeground(NOTIFICATION_ID, createNotification())
                startLocationUpdates()
            }
        }
        return START_STICKY
    }

    /**
     * Called when the user swipes the app away from recent apps.
     *
     * Schedules the service to restart after a short delay to ensure continuous tracking.
     *
     * @param rootIntent The original intent that was used to launch the task that is being removed
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Timber.d("TrackingService onTaskRemoved - application swiped away from recent apps")

        // Create a restart intent
        val restartServiceIntent = Intent(applicationContext, TrackingService::class.java)
        restartServiceIntent.action = ACTION_START_SERVICE
        val pIntent = PendingIntent.getService(
            applicationContext, 1, restartServiceIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 5000, pIntent)

        Timber.d("TrackingService scheduled for restart in 5 seconds")
    }

    /**
     * Called when the service is being destroyed.
     *
     * Cleans up resources, stops location updates, and releases the wake lock.
     */
    override fun onDestroy() {
        super.onDestroy()
        Timber.d("TrackingService onDestroy")
        // Shut down the executor
        backgroundExecutor?.shutdown()
        Timber.d("Background executor shutdown requested.")
        backgroundExecutor = null
        stopLocationUpdates()

        // Make absolutely sure we release the wake lock
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
                Timber.d("Wake lock released in onDestroy")
            }
        }
        wakeLock = null
    }

    /**
     * Not used in this service implementation.
     *
     * @return Always returns null as this is not a bound service
     */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * Starts location update requests.
     *
     * Configures location request parameters based on settings,
     * sets up location callbacks, and acquires a wake lock.
     */
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        Timber.d("Starting location updates...")

        // Acquire wake lock to keep CPU running during updates
        wakeLock?.let {
            if (!it.isHeld) {
                it.acquire(TimeUnit.HOURS.toMillis(10)) // Maximum wake lock time of 10 hours
                Timber.d("Wake lock acquired")
            } else {
                Timber.d("Wake lock already held")
            }
        } ?: Timber.e("Wake lock is null, cannot acquire")

        try {
            // Wrap suspend call with runBlocking
            val intervalMinutes = runBlocking { settingsRepository.getCurrentTrackingInterval() }
            Timber.d("Using tracking interval: $intervalMinutes minutes")

            val intervalMillis = TimeUnit.MINUTES.toMillis(intervalMinutes.toLong())

            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMillis)
                .setMinUpdateIntervalMillis(intervalMillis / 2)
                .setMaxUpdateDelayMillis(intervalMillis)
                .setWaitForAccurateLocation(false)
                .build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { currentLocation ->
                        Timber.d("Location received: ${currentLocation.latitude}, ${currentLocation.longitude}")
                        handleNewLocation(currentLocation)
                    } ?: Timber.w("Received null location in onLocationResult")
                }
            }

            // Request an immediate location update first
            Timber.d("Requesting immediate location update...")
            fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    location?.let {
                        Timber.d("Got immediate location: ${it.latitude}, ${it.longitude}")
                        handleNewLocation(it)
                    } ?: Timber.w("Immediate location request returned null")
                }
                .addOnFailureListener { e ->
                    Timber.e(e, "Failed to get immediate location")
                }

            // Set up the regular location updates
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            ).addOnFailureListener { e ->
                Timber.e(e, "Failed to request location updates.")
                stopSelf()
            }.addOnSuccessListener {
                Timber.d("Location updates requested successfully.")
            }

        } catch (e: Exception) {
            Timber.e(e, "Exception in startLocationUpdates: ${e.message}")
            stopSelf()
        }
    }

    /**
     * Processes a new location update.
     *
     * Submits the location processing task to the background executor to avoid
     * blocking the main thread. The background task handles location state updates
     * and uploading to the server with retry logic.
     *
     * @param currentLocation The new location from FusedLocationProviderClient
     */
    private fun handleNewLocation(currentLocation: Location) {
        Timber.d("handleNewLocation: Received location ${currentLocation.latitude}, ${currentLocation.longitude}")

        Timber.d("handleNewLocation: Submitting location to background executor.")

        // Submit the processing and upload task to the background executor
        backgroundExecutor?.submit {
            Timber.i("Executor task started for location: ${currentLocation.latitude}, ${currentLocation.longitude}")
            try {
                // 1. Gather necessary data
                Timber.d("Executor: Fetching settings...")
                // Wrap suspend calls with runBlocking
                val username = runBlocking { settingsRepository.getCurrentUsername() }
                val sessionId = runBlocking { settingsRepository.getCurrentSessionId() }
                val appId = runBlocking { settingsRepository.getAppId() }
                Timber.i("Executor: Got username=$username, sessionId=$sessionId, appId=$appId")

                // 2. Save location state (updates latestLocation and totalDistance in repo)
                Timber.i("Executor: Saving location state via repository...")
                try {
                    runBlocking { locationRepository.saveAsPreviousLocation(currentLocation) }
                    Timber.d("Executor: Location state saved.")
                } catch (e: Exception) {
                    Timber.e(e, "Executor: Failed to save location state")
                    // Exit the executor task if state saving fails
                    return@submit
                }

                // 3. Perform location upload with retry logic
                Timber.i("Executor: Starting upload attempt loop...")
                var success = false
                var retryCount = 0
                val maxRetries = 3

                while (!success && retryCount < maxRetries) {
                    try {
                        Timber.d("Executor: Upload Attempt ${retryCount + 1}/$maxRetries")
                        // Wrap suspend call with runBlocking
                        Timber.d("Executor: Entering runBlocking for uploadLocationData")
                        try {
                            success = runBlocking {
                                Timber.i("Executor: Inside runBlocking for uploadLocationData")
                                locationRepository.uploadLocationData(
                                    location = currentLocation,
                                    username = username,
                                    sessionId = sessionId,
                                    appId = appId,
                                    eventType = "service-update-executor"
                                )
                            }
                            Timber.d("Executor: Exited runBlocking for uploadLocationData successfully")
                        } catch (rbError: Exception) {
                            Timber.e(rbError, "Executor: Exception occurred WITHIN or AROUND runBlocking for uploadLocationData")
                            success = false
                        }

                        if (success) {
                            Timber.i("Executor: Upload SUCCESS! (Attempt ${retryCount + 1}) Lat=${currentLocation.latitude}, Lon=${currentLocation.longitude}")
                        } else {
                            Timber.w("Executor: Upload FAILED (Attempt ${retryCount + 1}). Will retry if possible.")
                            if (retryCount < maxRetries - 1) {
                                val delayMs = 1000L * (retryCount + 1)
                                Timber.d("Executor: Waiting ${delayMs}ms before retry...")
                                try {
                                    Thread.sleep(delayMs)
                                } catch (ie: InterruptedException) {
                                    Timber.w("Executor: Sleep interrupted during retry delay.")
                                    Thread.currentThread().interrupt()
                                    break // Exit retry loop if interrupted
                                }
                            }
                        }
                    } catch (uploadException: Exception) {
                        Timber.e(uploadException, "Executor: Exception during upload attempt ${retryCount + 1}")
                        if (retryCount < maxRetries - 1) {
                            val delayMs = 1000L * (retryCount + 1)
                            Timber.d("Executor: Waiting ${delayMs}ms before retry after exception...")
                            try {
                                Thread.sleep(delayMs)
                            } catch (ie: InterruptedException) {
                                Timber.w("Executor: Sleep interrupted during retry delay after exception.")
                                Thread.currentThread().interrupt()
                                break
                            }
                        }
                    }
                    retryCount++
                }

                if (!success) {
                    Timber.e("Executor: All upload attempts failed after $maxRetries retries for location ${currentLocation.latitude}, ${currentLocation.longitude}")
                }

            } catch (t: Throwable) {
                Timber.e(t, "Executor: Uncaught Throwable inside background task")
            } finally {
                Timber.i("Executor task finished for location: ${currentLocation.latitude}, ${currentLocation.longitude}")
            }
        } ?: Timber.e("handleNewLocation: Background executor is null, cannot submit task.")
    }

    /**
     * Stops location updates and releases resources.
     *
     * Removes the location callback, releases the wake lock,
     * and cleans up associated resources.
     */
    private fun stopLocationUpdates() {
        Timber.d("stopLocationUpdates called.")

        // Release wake lock
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
                Timber.d("Wake lock released")
            }
        }

        locationCallback?.let {
            Timber.d("Stopping location updates...")
            try {
                val removeTask = fusedLocationProviderClient.removeLocationUpdates(it)
                removeTask.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Timber.d("Location updates stopped successfully.")
                    } else {
                        Timber.w(task.exception, "Failed to stop location updates.")
                    }
                }
            } catch (e: SecurityException) {
                Timber.e(e, "SecurityException while stopping location updates.")
            } finally {
                locationCallback = null
                Timber.d("Location callback cleared.")
            }
        } ?: Timber.d("stopLocationUpdates called but locationCallback was already null.")
    }

    /**
     * Creates the notification channel for Android O and above.
     *
     * This is required for displaying the foreground service notification.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            Timber.d("Notification channel created.")
        }
    }

    /**
     * Creates the notification for the foreground service.
     *
     * This notification is displayed while the service is running
     * to inform the user about the active tracking.
     *
     * @return The notification to display
     */
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_notification_tracking)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }
}