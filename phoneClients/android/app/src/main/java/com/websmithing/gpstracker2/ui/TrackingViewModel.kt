// # android/app/src/main/java/com/websmithing/gpstracker2/ui/TrackingViewModel.kt
package com.websmithing.gpstracker2.ui

import android.content.Context
import android.content.Intent
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.websmithing.gpstracker2.data.repository.LocationRepository
import com.websmithing.gpstracker2.data.repository.SettingsRepository
import com.websmithing.gpstracker2.data.repository.UploadStatus
import com.websmithing.gpstracker2.service.TrackingService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for the GPS tracking functionality.
 *
 * This ViewModel serves as the bridge between the UI and the data layer, handling user actions,
 * managing application state, and coordinating between repositories and services.
 * It's responsible for:
 * - Starting and stopping the tracking service
 * - Maintaining UI state (tracking status, settings)
 * - Providing location data and tracking statistics to the UI
 * - Managing user preferences
 *
 * The ViewModel uses Hilt for dependency injection and follows MVVM architecture.
 */
@HiltViewModel
class TrackingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    // --- LiveData for UI State ---
    /**
     * Tracks whether location tracking is currently active
     */
    private val _isTracking = MutableLiveData<Boolean>()
    val isTracking: LiveData<Boolean> = _isTracking

    /**
     * Stores the current username for tracking identification
     */
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    /**
     * Stores the current tracking interval in minutes (1, 5, or 15)
     */
    private val _trackingInterval = MutableLiveData<Int>()
    val trackingInterval: LiveData<Int> = _trackingInterval

    /**
     * Stores the current website URL where tracking data is sent
     */
    private val _websiteUrl = MutableLiveData<String>()
    val websiteUrl: LiveData<String> = _websiteUrl

    private val _language = MutableLiveData<String>()
    val language: LiveData<String> = _language

    /**
     * Temporary messages to display to the user via Snackbar
     */
    private val _snackbarMessage = MutableLiveData<String?>()
    val snackbarMessage: LiveData<String?> = _snackbarMessage

    // --- StateFlows for Location Data ---
    /**
     * The most recent location data received from the location services
     * Exposes the repository's location flow as a StateFlow for the UI to collect
     */
    val latestLocation: StateFlow<Location?> = locationRepository.latestLocation
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * The total distance traveled during the current tracking session in meters
     * Exposes the repository's distance flow as a StateFlow for the UI to collect
     */
    val totalDistance: StateFlow<Float> = locationRepository.totalDistance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    /**
     * The status of the most recent location data upload to the server
     * Exposes the repository's upload status flow as a StateFlow for the UI to collect
     */
    val lastUploadStatus: StateFlow<UploadStatus> = locationRepository.lastUploadStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UploadStatus.Idle)

    /**
     * Job reference for the initialization coroutine (useful for testing)
     */
    internal val initJob: Job

    /**
     * Initialize the ViewModel by loading current settings from repositories
     */
    init {
        // Initialize state from Repository using viewModelScope
        initJob = viewModelScope.launch {
            _isTracking.value = settingsRepository.getCurrentTrackingState()
            _userName.value = settingsRepository.getCurrentUsername()
            _trackingInterval.value = settingsRepository.getCurrentTrackingInterval()
            _websiteUrl.value = settingsRepository.getCurrentWebsiteUrl()
            _language.value = settingsRepository.getCurrentLanguage()
            Timber.d("ViewModel initialized. Tracking: ${isTracking.value}")

            // Check if first time loading and generate App ID if needed
            if (settingsRepository.isFirstTimeLoading()) {
                Timber.d("First time loading detected, generating App ID.")
                settingsRepository.generateAndSaveAppId()
                settingsRepository.setFirstTimeLoading(false) // Mark as no longer first time
            }
        }
    }

    // --- Actions from UI ---
    /**
     * Starts location tracking after permissions are granted
     *
     * This should only be called by the Activity after confirming all required
     * permissions have been granted by the user.
     */
    fun startTracking() {
        Timber.d("startTracking called in ViewModel")
        updateTrackingState(true)
    }

    /**
     * Stops location tracking
     *
     * Can be called by the Activity when the user requests to stop tracking
     * or directly by the ViewModel in response to errors.
     */
    fun stopTracking() {
        Timber.d("stopTracking called in ViewModel")
        updateTrackingState(false)
    }

    /**
     * Forces tracking to stop in case of permission denial or errors
     *
     * Called by the Activity if the user denies required permissions during
     * an attempt to start tracking.
     */
    fun forceStopTracking() {
        Timber.d("forceStopTracking called in ViewModel")
        if (_isTracking.value == true) {
            updateTrackingState(false)
        }
    }

    /**
     * Updates the tracking interval setting
     *
     * If tracking is currently active, this will restart the tracking service
     * to apply the new interval immediately.
     *
     * @param newInterval The new tracking interval in minutes (1, 5, or 15)
     */
    fun onIntervalChanged(newInterval: Int) {
        _trackingInterval.value = newInterval
    }

    /**
     * Updates the username setting
     *
     * @param newName The new username for tracking identification
     */
    fun onUserNameChanged(newName: String) {
        _userName.value = newName.trim()
    }

    /**
     * Updates the website URL setting
     *
     * @param newUrl The new URL where tracking data will be sent
     */
    fun onWebsiteUrlChanged(newUrl: String) {
        _websiteUrl.value = newUrl.trim()
    }

    /**
     * Updates the language setting
     *
     * @param language The new language where app be using
     */
    fun onLanguageChanged(language: String) {
        _language.value = language
    }


    fun refreshSettingsFromRepository() {
        viewModelScope.launch {
            _userName.value = settingsRepository.getCurrentUsername()
            _websiteUrl.value = settingsRepository.getCurrentWebsiteUrl()
            _trackingInterval.value = settingsRepository.getCurrentTrackingInterval()
            _language.value = settingsRepository.getCurrentLanguage()
            _isTracking.value = settingsRepository.getCurrentTrackingState()
        }
    }

    /**
     * A new function (rewrite)
     */
    fun saveSettings() {
        viewModelScope.launch {
            _userName.value?.let { settingsRepository.saveUsername(it) }
            _websiteUrl.value?.let { settingsRepository.saveWebsiteUrl(it) }
            _language.value?.let { settingsRepository.saveLanguage(it) }

            val currentInterval = settingsRepository.getCurrentTrackingInterval()
            val newInterval = _trackingInterval.value ?: currentInterval
            settingsRepository.saveTrackingInterval(newInterval)

            if (_isTracking.value == true) {
                val intervalChanged = currentInterval != newInterval
                if (intervalChanged) {
                    _snackbarMessage.value = "Interval updated. Restarting tracking service."
                    Intent(context, TrackingService::class.java).also { intent ->
                        intent.action = TrackingService.ACTION_STOP_SERVICE
                        context.stopService(intent)
                    }
                    Intent(context, TrackingService::class.java).also { intent ->
                        intent.action = TrackingService.ACTION_START_SERVICE
                        context.startForegroundService(intent)
                    }
                }
            }
        }
    }


    /**
     * Marks a snackbar message as shown to prevent reappearance
     */
    fun onSnackbarMessageShown() {
        _snackbarMessage.value = null
    }

    // --- Private Helper Methods ---

    /**
     * Updates the tracking state and handles service lifecycle
     *
     * This method persists the tracking state to settings, manages the tracking session ID,
     * and starts or stops the TrackingService as appropriate.
     *
     * @param shouldTrack Whether tracking should be active
     * @return The coroutine Job handling the update operations
     */
    private fun updateTrackingState(shouldTrack: Boolean): Job {
        if (_isTracking.value == shouldTrack) return Job().apply { complete() }

        _isTracking.value = shouldTrack
        return viewModelScope.launch {
            settingsRepository.setTrackingState(shouldTrack)
            if (shouldTrack) {
                val newSessionId = UUID.randomUUID().toString()
                settingsRepository.saveSessionId(newSessionId)
                locationRepository.resetLocationState() // Reset location repo state
                // Start the foreground service
                Intent(context, TrackingService::class.java).also { intent ->
                    intent.action = TrackingService.ACTION_START_SERVICE
                    context.startForegroundService(intent)
                    Timber.i("Tracking started via ViewModel. Session: $newSessionId. Service started.")
                }
            } else {
                settingsRepository.clearSessionId()
                // Stop the foreground service
                Intent(context, TrackingService::class.java).also { intent ->
                    intent.action = TrackingService.ACTION_STOP_SERVICE
                    context.stopService(intent) // Use stopService for foreground services
                    Timber.i("Tracking stopped via ViewModel. Service stopped.")
                }
            }
        }
    }

    companion object {
        private const val TAG = "TrackingViewModel"
    }
}