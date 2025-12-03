// # android/app/src/main/java/com/websmithing/gpstracker2/data/repository/SettingsRepository.kt
package com.websmithing.gpstracker2.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing user settings and tracking state.
 *
 * This interface defines the contract for persisting and retrieving
 * settings, session data, and tracking state for the GPS tracker application.
 * It provides both synchronous and asynchronous (Flow) methods for accessing settings.
 */
interface SettingsRepository {

    // --- Tracking State ---
    /**
     * Sets the current tracking state.
     *
     * @param isTracking True if tracking is active, false otherwise
     */
    suspend fun setTrackingState(isTracking: Boolean)

    /**
     * Provides a Flow that emits the current tracking state whenever it changes.
     *
     * @return A Flow emitting the current tracking state
     */
    fun isTracking(): Flow<Boolean>

    /**
     * Gets the current tracking state synchronously.
     *
     * @return True if tracking is active, false otherwise
     */
    suspend fun getCurrentTrackingState(): Boolean

    // --- User Settings ---
    /**
     * Saves the username for identifying this tracker's data.
     *
     * @param username The username to save
     */
    suspend fun saveUsername(username: String)

    /**
     * Provides a Flow that emits the current username whenever it changes.
     *
     * @return A Flow emitting the current username
     */
    fun getUsername(): Flow<String>

    /**
     * Gets the current username synchronously.
     *
     * @return The current username
     */
    suspend fun getCurrentUsername(): String

    /**
     * Saves the tracking interval in minutes.
     *
     * @param intervalMinutes The interval in minutes between location updates
     */
    suspend fun saveTrackingInterval(intervalMinutes: Int)

    /**
     * Provides a Flow that emits the current tracking interval whenever it changes.
     *
     * @return A Flow emitting the current tracking interval in minutes
     */
    fun getTrackingInterval(): Flow<Int>

    /**
     * Gets the current tracking interval synchronously.
     *
     * @return The current tracking interval in minutes
     */
    suspend fun getCurrentTrackingInterval(): Int

    /**
     * Saves the website URL where location data will be uploaded.
     *
     * @param url The website URL to save
     */
    suspend fun saveWebsiteUrl(url: String)

    /**
     * Provides a Flow that emits the current website URL whenever it changes.
     *
     * @return A Flow emitting the current website URL
     */
    fun getWebsiteUrl(): Flow<String>

    /**
     * Gets the current website URL synchronously.
     *
     * @return The current website URL
     */
    suspend fun getCurrentWebsiteUrl(): String

    // --- Session/Device IDs ---
    /**
     * Saves a new session ID for the current tracking session.
     *
     * @param sessionId The session ID to save
     */
    suspend fun saveSessionId(sessionId: String)

    /**
     * Clears the current session ID, typically when tracking stops.
     */
    suspend fun clearSessionId()

    /**
     * Gets the current session ID synchronously.
     * If no session ID exists, generates and saves a new one.
     *
     * @return The current session ID
     */
    suspend fun getCurrentSessionId(): String

    /**
     * Gets the app ID (device identifier) synchronously.
     * The app ID is generated once when the app is first installed and remains constant.
     *
     * @return The app ID
     */
    suspend fun getAppId(): String

    // --- First Time Check ---
    /**
     * Checks if this is the first time the app is being loaded.
     *
     * @return True if this is the first time loading, false otherwise
     */
    suspend fun isFirstTimeLoading(): Boolean

    /**
     * Sets the first-time loading flag.
     *
     * @param isFirst True to mark as first time loading, false otherwise
     */
    suspend fun setFirstTimeLoading(isFirst: Boolean)

    /**
     * Generates and saves a new app ID.
     * This should only be called during first-time setup.
     *
     * @return The newly generated app ID
     */
    suspend fun generateAndSaveAppId(): String

    // --- Location State ---
    /**
     * Resets location state for a new tracking session.
     * This resets total distance and position flags.
     */
    suspend fun resetLocationStateForNewSession()

    /**
     * Saves the total distance traveled and position flag.
     *
     * @param totalDistance The total distance traveled in meters
     * @param firstTime True if this is the first position in a tracking session
     */
    suspend fun saveDistanceAndPositionFlags(totalDistance: Float, firstTime: Boolean)

    /**
     * Gets the total distance traveled synchronously.
     *
     * @return The total distance traveled in meters
     */
    suspend fun getTotalDistance(): Float

    /**
     * Checks if this is the first time getting a position in the current tracking session.
     *
     * @return True if this is the first position, false otherwise
     */
    suspend fun isFirstTimeGettingPosition(): Boolean

    fun saveLanguage(language: String)

    fun getCurrentLanguage(): String
}