// /Users/nickfox137/Documents/gpstracker-clients/gpstracker-ios/GPSTracker/GPSTracker/Service/PersistenceService.swift

import Foundation
import os

/// Protocol defining the interface for data persistence operations
///
/// This protocol abstracts the storage mechanism, allowing for different
/// implementations (UserDefaults, CoreData, File-based, etc.)
///
/// ## Overview
/// The persistence service is responsible for:
/// - Saving and retrieving app settings
/// - Storing tracking session data
/// - Managing cached location data
///
/// ## Topics
/// ### Basic Operations
/// - ``getValue(forKey:defaultValue:)``
/// - ``setValue(_:forKey:)``
/// - ``removeValue(forKey:)``
protocol PersistenceServiceProtocol {
    /// Retrieves a value from persistent storage
    ///
    /// - Parameters:
    ///   - key: The key to retrieve
    ///   - defaultValue: Default value to return if key doesn't exist
    /// - Returns: The stored value or the default value
    func getValue<T>(forKey key: String, defaultValue: T) -> T
    
    /// Saves a value to persistent storage
    ///
    /// - Parameters:
    ///   - value: The value to store
    ///   - key: The key to associate with the value
    func setValue<T>(_ value: T, forKey key: String)
    
    /// Removes a value from persistent storage
    ///
    /// - Parameter key: The key to remove
    func removeValue(forKey key: String)
}

/// Implementation of PersistenceServiceProtocol using UserDefaults
///
/// This class provides persistent storage for app settings and small data items
/// using the UserDefaults system.
///
/// ## Overview
/// The persistence service uses UserDefaults for simple key-value storage
/// of application settings, user preferences, and small data items. For larger
/// datasets or more complex persistence needs, consider extending this service
/// or creating specialized persistence services.
class PersistenceService: PersistenceServiceProtocol {
    /// Logger for diagnostic information
    private let logger = Logger(subsystem: Bundle.main.bundleIdentifier ?? "com.websmithing.gpstracker2", category: "PersistenceService")
    
    /// User defaults instance for storing settings
    private let userDefaults: UserDefaults
    
    /// Key prefix to avoid namespace collisions
    private let keyPrefix = "com.websmithing.gpstracker2."
    
    /// Initializes the persistence service
    ///
    /// - Parameter userDefaults: The UserDefaults instance to use (defaults to standard)
    init(userDefaults: UserDefaults = .standard) {
        self.userDefaults = userDefaults
        log("PersistenceService initialized", logger: logger)
    }
    
    /// Retrieves a value from persistent storage
    ///
    /// - Parameters:
    ///   - key: The key to retrieve
    ///   - defaultValue: Default value to return if key doesn't exist
    /// - Returns: The stored value or the default value
    func getValue<T>(forKey key: String, defaultValue: T) -> T {
        let prefixedKey = keyPrefix + key
        let value = userDefaults.object(forKey: prefixedKey)
        
        guard let value = value as? T else {
            log("No value found for key: \(key), returning default", logger: logger)
            return defaultValue
        }
        
        log("Retrieved value for key: \(key)", logger: logger)
        return value
    }
    
    /// Saves a value to persistent storage
    ///
    /// - Parameters:
    ///   - value: The value to store
    ///   - key: The key to associate with the value
    func setValue<T>(_ value: T, forKey key: String) {
        let prefixedKey = keyPrefix + key
        userDefaults.set(value, forKey: prefixedKey)
        log("Saved value for key: \(key)", logger: logger)
    }
    
    /// Removes a value from persistent storage
    ///
    /// - Parameter key: The key to remove
    func removeValue(forKey key: String) {
        let prefixedKey = keyPrefix + key
        userDefaults.removeObject(forKey: prefixedKey)
        log("Removed value for key: \(key)", logger: logger)
    }
}