// # android/app/src/main/java/com/websmithing/gpstracker2/network/ApiService.kt
package com.websmithing.gpstracker2.network

import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Retrofit API interface for the GPS Tracker backend.
 *
 * This interface defines the network endpoints for communicating with the GPS Tracker
 * server. It uses Retrofit annotations to specify the HTTP methods, endpoints, and
 * parameter formatting.
 *
 * All methods are suspending functions, designed to be called from coroutines.
 */
interface ApiService {

    /**
     * Sends location data to the server.
     *
     * This method uploads the device's location information to the GPS Tracker server
     * using HTTP POST with form URL encoding. The server expects a specific set of
     * parameters with the location details.
     *
     * Endpoint: https://www.websmithing.com/gpstracker/api/locations/update
     * Content-Type: application/x-www-form-urlencoded
     *
     * @param latitude The device's latitude coordinate as a string
     * @param longitude The device's longitude coordinate as a string
     * @param speed The device's speed in miles per hour
     * @param direction The device's bearing/direction in degrees (0-359)
     * @param date The date and time of the location reading in "YYYY-MM-DD HH:MM:SS" format (URL-encoded)
     * @param locationMethod The location provider method (e.g., "gps", "network") (URL-encoded)
     * @param username The username identifying this tracker
     * @param phoneNumber The unique identifier for this device (using app ID UUID)
     * @param sessionId The UUID for the current tracking session
     * @param accuracy The location accuracy in meters
     * @param extraInfo Additional information (typically used for altitude in meters)
     * @param eventType The type of event triggering this update (e.g., "periodic-android")
     * @return A Response containing a String. Success returns the database ID or timestamp, failure returns "-1"
     */
    @FormUrlEncoded
    @POST("update")
    suspend fun updateLocation(
        @Field("latitude") latitude: String,
        @Field("longitude") longitude: String,
        @Field("speed") speed: Int,
        @Field("direction") direction: Int,
        @Field("date") date: String,
        @Field("locationmethod") locationMethod: String,
        @Field("username") username: String,
        @Field("phonenumber") phoneNumber: String,
        @Field("sessionid") sessionId: String,
        @Field("accuracy") accuracy: Int,
        @Field("extrainfo") extraInfo: String,
        @Field("eventtype") eventType: String
    ): Response<String>
    
    /**
     * Simple test method with minimal required parameters for debugging.
     *
     * This method provides a simplified version of the updateLocation method
     * with default values for most parameters, making it easier to test the
     * server connection.
     *
     * @param latitude The device's latitude coordinate as a string
     * @param longitude The device's longitude coordinate as a string
     * @param speed The device's speed (default: 0)
     * @param direction The device's bearing/direction (default: 0)
     * @param distance The distance traveled in this session (default: "0.0")
     * @param date The date and time (default: "2023-04-05 00:00:00")
     * @param locationMethod The location provider method (default: "test")
     * @param username The username identifying this tracker
     * @param phoneNumber The device identifier (default: "test_phone")
     * @param sessionId The UUID for the current tracking session
     * @param accuracy The location accuracy (default: 0)
     * @param extraInfo Additional information (default: "test")
     * @param eventType The type of event (default: "test-direct")
     * @return A Response containing a String, similar to updateLocation
     */
    @FormUrlEncoded
    @POST("update")
    suspend fun testUpdate(
        @Field("latitude") latitude: String,
        @Field("longitude") longitude: String,
        @Field("speed") speed: Int = 0,
        @Field("direction") direction: Int = 0,
        @Field("distance") distance: String = "0.0",
        @Field("date") date: String = "2023-04-05 00:00:00", 
        @Field("locationmethod") locationMethod: String = "test",
        @Field("username") username: String,
        @Field("phonenumber") phoneNumber: String = "test_phone",
        @Field("sessionid") sessionId: String,
        @Field("accuracy") accuracy: Int = 0,
        @Field("extrainfo") extraInfo: String = "test",
        @Field("eventtype") eventType: String = "test-direct"
    ): Response<String>
}