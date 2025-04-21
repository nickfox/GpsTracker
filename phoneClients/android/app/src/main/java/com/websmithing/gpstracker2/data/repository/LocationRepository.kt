// # android/app/src/main/java/com/websmithing/gpstracker2/data/repository/LocationRepository.kt
package com.websmithing.gpstracker2.data.repository

import android.location.Location
import kotlinx.coroutines.flow.Flow

/**
 * Represents the status of a location upload attempt.
 *
 * This sealed class provides different states for tracking the upload process:
 * - [Idle]: Initial state or state after processing a success/failure
 * - [Success]: Upload was successful
 * - [Failure]: Upload failed with an optional error message
 */
sealed class UploadStatus {
    /**
     * Initial state or state after processing a success/failure
     */
    object Idle : UploadStatus()
    
    /**
     * Indicates a successful location data upload
     */
    object Success : UploadStatus()
    
    /**
     * Indicates a failed location data upload with an optional error message
     *
     * @property errorMessage Optional error message describing the failure reason
     */
    data class Failure(val errorMessage: String?) : UploadStatus()
}

/**
 * Repository interface for handling location operations in the GPS Tracker app.
 *
 * This interface defines the contract for location-related operations:
 * - Retrieving and observing location data
 * - Tracking distance traveled
 * - Uploading location data to a remote server
 * - Managing location state for calculations
 */
interface LocationRepository {

    /**
     * A flow emitting the latest known device location.
     * 
     * Emits null if no location has been received yet.
     */
    val latestLocation: Flow<Location?>

    /**
     * A flow emitting the total distance traveled in meters since tracking started.
     */
    val totalDistance: Flow<Float>

    /**
     * A flow emitting the status of the last location upload attempt.
     */
    val lastUploadStatus: Flow<UploadStatus>

    /**
     * Fetches the current device location synchronously.
     * 
     * @return The current location or null if location could not be determined
     * @throws SecurityException if location permissions are not granted
     */
    suspend fun getCurrentLocation(): Location?

    /**
     * Uploads the provided location data to the remote server.
     * 
     * @param location The location data to upload
     * @param username The username identifying this tracker
     * @param appId Unique identifier for this device/installation
     * @param sessionId Unique identifier for this tracking session
     * @param eventType Type of tracking event (e.g., "start", "stop", "update")
     * @return true if upload was successful, false otherwise
     */
    suspend fun uploadLocationData(
        location: Location,
        username: String,
        appId: String,
        sessionId: String,
        eventType: String
    ): Boolean

    /**
     * Retrieves the previously saved location point.
     * 
     * @return The previously saved location or null if no previous location is stored
     */
    suspend fun getPreviousLocation(): Location?

    /**
     * Saves the current location as the "previous" location for the next calculation.
     * Also updates the total distance calculation based on this new location.
     * 
     * @param location The location to save as the current location
     */
    suspend fun saveAsPreviousLocation(location: Location)

    /**
     * Resets the location state for a new tracking session.
     * 
     * Clears the previous location, resets the total distance to zero,
     * and resets the upload status to Idle.
     */
    suspend fun resetLocationState()
}