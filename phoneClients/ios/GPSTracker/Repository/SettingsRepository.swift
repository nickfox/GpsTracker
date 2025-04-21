// /Users/nickfox137/Documents/gpstracker-clients/gpstracker-ios/GPSTracker/GPSTracker/Repository/SettingsRepository.swift

import Foundation
import os

/// Implementation of SettingsRepositoryProtocol
///
/// This class manages the storage and retrieval of application settings
/// using the persistence service.
///
/// ## Overview
/// The settings repository provides a type-safe interface for accessing
/// user preferences and application configuration. It uses the persistence
/// service for actual storage and applies default values when settings
/// don't exist.
///
/// ## Topics
/// ### User Identification
/// - ``getUsername()``
/// - ``saveUsername(_:)``
///
/// ### Server Configuration
/// - ``getServerUrl()``
/// - ``saveServerUrl(_:)``
///
/// ### Tracking Configuration
/// - ``getTrackingInterval()``
/// - ``getDistanceFilter()``
class SettingsRepository: SettingsRepositoryProtocol {
    /// Logger for diagnostic information
    private let logger = Logger(subsystem: Bundle.main.bundleIdentifier ?? "com.websmithing.gpstracker2", category: "SettingsRepository")
    
    /// Service for data persistence
    private let persistenceService: PersistenceServiceProtocol
    
    /// Setting keys to avoid string literals throughout the code
    private enum SettingKeys {
        static let username = "username"
        static let serverUrl = "server_url"
        static let trackingInterval = "tracking_interval"
        static let distanceFilter = "distance_filter"
        static let trackInBackground = "track_in_background"
        static let appId = "app_id"
    }
    
    /// Default values for settings
    private enum Defaults {
        static let username = "default_user"
        static let serverUrl = "https://www.websmithing.com/gpstracker2/api/location"
        static let trackingInterval = 10 // seconds
        static let distanceFilter = 5 // meters
        static let trackInBackground = true
    }
    
    /// Initializes the settings repository with a persistence service
    ///
    /// - Parameter persistenceService: Service for storing and retrieving settings
    init(persistenceService: PersistenceServiceProtocol) {
        self.persistenceService = persistenceService
        log("SettingsRepository initialized", logger: logger)
    }
    
    /// Retrieves the username for identifying this device
    ///
    /// - Returns: The stored username or a default value
    func getUsername() -> String {
        let username = persistenceService.getValue(forKey: SettingKeys.username, defaultValue: Defaults.username)
        log("Retrieved username: \(username)", logger: logger)
        return username
    }
    
    /// Stores a new username
    ///
    /// - Parameter username: The username to save
    func saveUsername(_ username: String) {
        persistenceService.setValue(username, forKey: SettingKeys.username)
        log("Saved username: \(username)", logger: logger)
    }
    
    /// Retrieves the server URL for uploading tracking data
    ///
    /// - Returns: The stored server URL or a default value
    func getServerUrl() -> String {
        return persistenceService.getValue(forKey: SettingKeys.serverUrl, defaultValue: Defaults.serverUrl)
    }
    
    /// Stores a new server URL
    ///
    /// - Parameter url: The server URL to save
    func saveServerUrl(_ url: String) {
        persistenceService.setValue(url, forKey: SettingKeys.serverUrl)
        log("Saved server URL: \(url)", logger: logger)
    }
    
    /// Retrieves the tracking interval in seconds
    ///
    /// - Returns: The stored tracking interval or a default value
    func getTrackingInterval() -> Int {
        return persistenceService.getValue(forKey: SettingKeys.trackingInterval, defaultValue: Defaults.trackingInterval)
    }
    
    /// Stores a new tracking interval
    ///
    /// - Parameter interval: The tracking interval in seconds
    func saveTrackingInterval(_ interval: Int) {
        persistenceService.setValue(interval, forKey: SettingKeys.trackingInterval)
        log("Saved tracking interval: \(interval) seconds", logger: logger)
    }
    
    /// Retrieves the minimum distance between updates in meters
    ///
    /// - Returns: The stored distance filter or a default value
    func getDistanceFilter() -> Int {
        return persistenceService.getValue(forKey: SettingKeys.distanceFilter, defaultValue: Defaults.distanceFilter)
    }
    
    /// Stores a new distance filter
    ///
    /// - Parameter distance: The minimum distance between updates in meters
    func saveDistanceFilter(_ distance: Int) {
        persistenceService.setValue(distance, forKey: SettingKeys.distanceFilter)
        log("Saved distance filter: \(distance) meters", logger: logger)
    }
    
    /// Retrieves the background tracking preference
    ///
    /// - Returns: Whether background tracking is enabled
    func getTrackInBackground() -> Bool {
        return persistenceService.getValue(forKey: SettingKeys.trackInBackground, defaultValue: Defaults.trackInBackground)
    }
    
    /// Stores a new background tracking preference
    ///
    /// - Parameter enabled: Whether background tracking should be enabled
    func saveTrackInBackground(_ enabled: Bool) {
        persistenceService.setValue(enabled, forKey: SettingKeys.trackInBackground)
        log("Saved track in background: \(enabled)", logger: logger)
    }
    
    /// Retrieves the app installation identifier
    ///
    /// - Returns: The stored app ID or generates a new one
    func getAppId() -> String {
        let appId = persistenceService.getValue(forKey: SettingKeys.appId, defaultValue: "")
        if appId.isEmpty {
            // Generate a new app ID if none exists
            let newAppId = UUID().uuidString
            saveAppId(newAppId)
            return newAppId
        }
        return appId
    }
    
    /// Stores a new app installation identifier
    ///
    /// - Parameter appId: The app ID to save
    func saveAppId(_ appId: String) {
        persistenceService.setValue(appId, forKey: SettingKeys.appId)
        log("Saved app ID: \(appId)", logger: logger)
    }
}