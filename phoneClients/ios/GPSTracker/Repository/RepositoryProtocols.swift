// /Users/nickfox137/Documents/gpstracker-clients/gpstracker-ios/GPSTracker/GPSTracker/Repository/RepositoryProtocols.swift

import Foundation
import Combine

/// Protocol defining the interface for location data operations
///
/// This protocol abstracts the storage, processing, and transmission
/// of location data in the application.
///
/// ## Overview
/// The location repository is responsible for:
/// - Coordinating between location service and API service
/// - Managing location data processing
/// - Handling upload of location data to the server
///
/// ## Topics
/// ### Location Uploads
/// - ``uploadLocation(parameters:)``
protocol LocationRepositoryProtocol {
    /// Uploads location data to the tracking server
    ///
    /// - Parameter parameters: The location data and metadata to send
    /// - Returns: A publisher that emits upload success or failure
    func uploadLocation(parameters: LocationAPIRequestParameters) -> AnyPublisher<APIResponse, Error>
}

/// Protocol defining the interface for settings operations
///
/// This protocol abstracts the storage and retrieval of application
/// settings and user preferences.
///
/// ## Overview
/// The settings repository is responsible for:
/// - Storing and retrieving user preferences
/// - Managing configuration data
/// - Providing default values for settings
///
/// ## Topics
/// ### User Identification
/// - ``getUsername()``
/// - ``saveUsername(_:)``
/// - ``getAppId()``
/// - ``saveAppId(_:)``
///
/// ### Server Configuration
/// - ``getServerUrl()``
/// - ``saveServerUrl(_:)``
///
/// ### Tracking Settings
/// - ``getTrackingInterval()``
/// - ``saveTrackingInterval(_:)``
/// - ``getDistanceFilter()``
/// - ``saveDistanceFilter(_:)``
/// - ``getTrackInBackground()``
/// - ``saveTrackInBackground(_:)``
protocol SettingsRepositoryProtocol {
    /// Retrieves the username for identifying this device
    /// - Returns: The stored username or a default value
    func getUsername() -> String
    
    /// Stores a new username
    /// - Parameter username: The username to save
    func saveUsername(_ username: String)
    
    /// Retrieves the server URL for uploading tracking data
    /// - Returns: The stored server URL or a default value
    func getServerUrl() -> String
    
    /// Stores a new server URL
    /// - Parameter url: The server URL to save
    func saveServerUrl(_ url: String)
    
    /// Retrieves the tracking interval in seconds
    /// - Returns: The stored tracking interval or a default value
    func getTrackingInterval() -> Int
    
    /// Stores a new tracking interval
    /// - Parameter interval: The tracking interval in seconds
    func saveTrackingInterval(_ interval: Int)
    
    /// Retrieves the minimum distance between updates in meters
    /// - Returns: The stored distance filter or a default value
    func getDistanceFilter() -> Int
    
    /// Stores a new distance filter
    /// - Parameter distance: The minimum distance between updates in meters
    func saveDistanceFilter(_ distance: Int)
    
    /// Retrieves the background tracking preference
    /// - Returns: Whether background tracking is enabled
    func getTrackInBackground() -> Bool
    
    /// Stores a new background tracking preference
    /// - Parameter enabled: Whether background tracking should be enabled
    func saveTrackInBackground(_ enabled: Bool)
    
    /// Retrieves the app installation identifier
    /// - Returns: The stored app ID or generates a new one
    func getAppId() -> String
    
    /// Stores a new app installation identifier
    /// - Parameter appId: The app ID to save
    func saveAppId(_ appId: String)
}