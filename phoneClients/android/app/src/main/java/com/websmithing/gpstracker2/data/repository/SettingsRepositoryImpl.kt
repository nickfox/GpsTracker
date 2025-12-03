// # android/app/src/main/java/com/websmithing/gpstracker2/data/repository/SettingsRepositoryImpl.kt
package com.websmithing.gpstracker2.data.repository

import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of the [SettingsRepository] interface using SharedPreferences.
 *
 * This class handles:
 * - Storing and retrieving user settings for the GPS tracker
 * - Managing tracking state
 * - Handling session and device identifiers
 * - Maintaining location-related state
 *
 * All methods use Kotlin coroutines with the IO dispatcher to ensure
 * that shared preferences operations don't block the main thread.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : SettingsRepository {

    // --- Tracking State ---
    /**
     * Sets whether tracking is currently active.
     *
     * @param isTracking True if tracking is active, false otherwise
     */
    override suspend fun setTrackingState(isTracking: Boolean) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().putBoolean(KEY_CURRENTLY_TRACKING, isTracking).apply()
        }
    }

    /**
     * Provides a Flow that emits the current tracking state.
     *
     * Note: This is a basic implementation. For real-time updates,
     * consider using a SharedPreferenceChangeListener or DataStore.
     *
     * @return A Flow emitting the current tracking state
     */
    override fun isTracking(): Flow<Boolean> = flow {
        emit(getCurrentTrackingState())
    }

    /**
     * Gets the current tracking state synchronously.
     *
     * @return True if tracking is active, false otherwise
     */
    override suspend fun getCurrentTrackingState(): Boolean {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getBoolean(KEY_CURRENTLY_TRACKING, false)
        }
    }

    // --- User Settings ---
    /**
     * Saves the username for identifying this tracker's data.
     *
     * @param username The username to save
     */
    override suspend fun saveUsername(username: String) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().putString(KEY_USER_NAME, username.trim()).apply()
        }
    }

    /**
     * Provides a Flow that emits the current username.
     *
     * @return A Flow emitting the current username
     */
    override fun getUsername(): Flow<String> = flow {
        emit(getCurrentUsername())
    }

    /**
     * Gets the current username synchronously.
     *
     * @return The current username, or empty string if not set
     */
    override suspend fun getCurrentUsername(): String {
        return withContext(Dispatchers.IO) {
            val storedUsername = sharedPreferences.getString(KEY_USER_NAME, "") ?: ""
            storedUsername
        }
    }

    /**
     * Saves the tracking interval in minutes.
     *
     * @param intervalMinutes The interval in minutes between location updates
     */
    override suspend fun saveTrackingInterval(intervalMinutes: Int) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().putInt(KEY_INTERVAL_MINUTES, intervalMinutes).apply()
        }
    }

    /**
     * Provides a Flow that emits the current tracking interval.
     *
     * @return A Flow emitting the current tracking interval in minutes
     */
    override fun getTrackingInterval(): Flow<Int> = flow {
        emit(getCurrentTrackingInterval())
    }

    /**
     * Gets the current tracking interval synchronously.
     *
     * @return The current tracking interval in minutes, defaulting to 1 if not set
     */
    override suspend fun getCurrentTrackingInterval(): Int {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getInt(KEY_INTERVAL_MINUTES, 1) // Default to 1 min
        }
    }

    /**
     * Saves the website URL where location data will be uploaded.
     *
     * @param url The website URL to save
     */
    override suspend fun saveWebsiteUrl(url: String) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().putString(KEY_WEBSITE_URL, url.trim()).apply()
        }
    }

    /**
     * Provides a Flow that emits the current website URL.
     *
     * @return A Flow emitting the current website URL
     */
    override fun getWebsiteUrl(): Flow<String> = flow {
        emit(getCurrentWebsiteUrl())
    }

    /**
     * Gets the current website URL synchronously.
     *
     * @return The current website URL, defaulting to the standard endpoint if not set
     */
    override suspend fun getCurrentWebsiteUrl(): String {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getString(KEY_WEBSITE_URL, "device.waliot.com:30032") ?: "device.waliot.com:30032"
        }
    }

    // --- Session/Device IDs ---
    /**
     * Saves a new session ID for the current tracking session.
     *
     * @param sessionId The session ID to save
     */
    override suspend fun saveSessionId(sessionId: String) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().putString(KEY_SESSION_ID, sessionId).apply()
        }
    }

    /**
     * Clears the current session ID, typically when tracking stops.
     */
    override suspend fun clearSessionId() {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().remove(KEY_SESSION_ID).apply()
        }
    }

    /**
     * Gets the current session ID synchronously.
     * If no session ID exists, generates and saves a new one.
     *
     * @return The current session ID
     */
    override suspend fun getCurrentSessionId(): String {
        return withContext(Dispatchers.IO) {
            val storedSessionId = sharedPreferences.getString(KEY_SESSION_ID, "") ?: ""
            if (storedSessionId.isBlank()) {
                // Force a default session ID if none is set
                val defaultSessionId = UUID.randomUUID().toString()
                sharedPreferences.edit().putString(KEY_SESSION_ID, defaultSessionId).apply()
                return@withContext defaultSessionId
            }
            storedSessionId
        }
    }

    /**
     * Gets the app ID (device identifier) synchronously.
     * The app ID is generated once when the app is first installed and remains constant.
     *
     * @return The app ID
     */
    override suspend fun getAppId(): String {
        return withContext(Dispatchers.IO) {
            var appId = sharedPreferences.getString(KEY_APP_ID, null)
            if (appId == null) {
                appId = generateAndSaveAppIdInternal()
            }
            appId
        }
    }

    // --- First Time Check ---
    /**
     * Checks if this is the first time the app is being loaded.
     * This is determined by the absence of an app ID.
     *
     * @return True if this is the first time loading, false otherwise
     */
    override suspend fun isFirstTimeLoading(): Boolean {
        return withContext(Dispatchers.IO) {
            !sharedPreferences.contains(KEY_APP_ID)
        }
    }

    /**
     * Sets the first-time loading flag.
     * If not first time, ensures that an app ID exists.
     *
     * @param isFirst True to mark as first time loading, false otherwise
     */
    override suspend fun setFirstTimeLoading(isFirst: Boolean) {
        withContext(Dispatchers.IO) {
            if (!isFirst) {
                // Ensure App ID exists if we are marking it as "not first time"
                getAppId()
            }
            // No direct "first time" flag is being set here anymore.
        }
    }

    /**
     * Internal implementation of generating and saving a new app ID.
     *
     * @return The newly generated app ID
     */
    private suspend fun generateAndSaveAppIdInternal(): String {
        return withContext(Dispatchers.IO) {
            val newId = UUID.randomUUID().toString()
            sharedPreferences.edit().putString(KEY_APP_ID, newId).apply()
            newId
        }
    }

    /**
     * Generates and saves a new app ID.
     * This should only be called during first-time setup.
     *
     * @return The newly generated app ID
     */
    override suspend fun generateAndSaveAppId(): String {
        return generateAndSaveAppIdInternal()
    }

    // --- Location State ---
    /**
     * Resets location state for a new tracking session.
     * This resets total distance and position flags.
     */
    override suspend fun resetLocationStateForNewSession() {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().apply {
                putFloat(KEY_TOTAL_DISTANCE, 0f)
                putBoolean(KEY_FIRST_TIME_GETTING_POSITION, true)
                remove(KEY_PREVIOUS_LATITUDE)
                remove(KEY_PREVIOUS_LONGITUDE)
                apply()
            }
        }
    }

    /**
     * Saves the total distance traveled and position flag.
     *
     * @param totalDistance The total distance traveled in meters
     * @param firstTime True if this is the first position in a tracking session
     */
    override suspend fun saveDistanceAndPositionFlags(totalDistance: Float, firstTime: Boolean) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().apply {
                putFloat(KEY_TOTAL_DISTANCE, totalDistance)
                putBoolean(KEY_FIRST_TIME_GETTING_POSITION, firstTime)
                apply()
            }
        }
    }

    /**
     * Gets the total distance traveled synchronously.
     *
     * @return The total distance traveled in meters
     */
    override suspend fun getTotalDistance(): Float {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getFloat(KEY_TOTAL_DISTANCE, 0f)
        }
    }

    /**
     * Checks if this is the first time getting a position in the current tracking session.
     *
     * @return True if this is the first position, false otherwise
     */
    override suspend fun isFirstTimeGettingPosition(): Boolean {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getBoolean(KEY_FIRST_TIME_GETTING_POSITION, true)
        }
    }

    override fun saveLanguage(language: String) {
        sharedPreferences.edit().putString(KEY_LANGUAGE, language).apply()

    }

    override fun getCurrentLanguage(): String {
        return sharedPreferences.getString(KEY_LANGUAGE, "ru") ?: "ru"
    }

    /**
     * Constants used by this repository implementation
     */
    companion object {
        private const val PREFS_NAME = "com.websmithing.gpstracker2.prefs"
        private const val KEY_CURRENTLY_TRACKING = "currentlyTracking"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_INTERVAL_MINUTES = "intervalInMinutes"
        private const val KEY_SESSION_ID = "sessionID"
        private const val KEY_APP_ID = "appID"
        private const val KEY_TOTAL_DISTANCE = "totalDistanceInMeters"
        private const val KEY_FIRST_TIME_GETTING_POSITION = "firstTimeGettingPosition"
        private const val KEY_PREVIOUS_LATITUDE = "previousLatitude"
        private const val KEY_PREVIOUS_LONGITUDE = "previousLongitude"
        private const val KEY_WEBSITE_URL = "defaultUploadWebsite"
        private const val KEY_LANGUAGE = "language"
    }
}