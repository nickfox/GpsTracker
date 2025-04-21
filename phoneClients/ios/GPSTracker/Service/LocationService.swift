// /Users/nickfox137/Documents/gpstracker-clients/gpstracker-ios/GPSTracker/GPSTracker/Service/LocationService.swift

import Foundation
import CoreLocation
import Combine
import os
import UIKit

/// A protocol defining the interface for location tracking services.
///
/// This protocol abstracts the details of location management, allowing for
/// easier testing and potential alternative implementations.
///
/// ## Overview
/// The location service is responsible for:
/// - Requesting and managing location permissions
/// - Providing continuous location updates
/// - Monitoring authorization status changes
///
/// ## Topics
/// ### Location Updates
/// - ``locationPublisher``
/// - ``startUpdatingLocation()``
/// - ``stopUpdatingLocation()``
///
/// ### Permission Management
/// - ``authorizationStatusPublisher``
/// - ``requestPermissions()``
/// - ``getCurrentAuthorizationStatus()``
protocol LocationServiceProtocol {
    /// Publisher for location updates
    ///
    /// Subscribe to this publisher to receive real-time location data
    var locationPublisher: AnyPublisher<CLLocation, Never> { get }
    
    /// Publisher for location authorization status changes
    ///
    /// Subscribe to this publisher to respond to permission changes
    var authorizationStatusPublisher: AnyPublisher<CLAuthorizationStatus, Never> { get }

    /// Requests location permissions from the user
    ///
    /// This method prompts the user to grant location access permissions.
    /// The app requests "Always" authorization for background tracking.
    func requestPermissions()

    /// Starts the location update process
    ///
    /// Begins continuous location monitoring. Requires appropriate permissions.
    func startUpdatingLocation()

    /// Stops the location update process
    ///
    /// Halts continuous location monitoring to conserve battery.
    func stopUpdatingLocation()

    /// Gets the current authorization status
    ///
    /// - Returns: The current location authorization status
    func getCurrentAuthorizationStatus() -> CLAuthorizationStatus
}

/// Implementation of the LocationServiceProtocol using Core Location
///
/// This class manages device location tracking using iOS Core Location services.
/// It provides continuous location updates and handles permission changes.
///
/// ## Overview
/// The location service is configured for high accuracy tracking with background
/// capabilities enabled. It handles permission changes and delivers location
/// updates via Combine publishers.
///
/// ## Battery Considerations
/// This service is optimized for accurate tracking rather than battery efficiency.
/// It monitors battery levels to provide this information to the server.
class LocationService: NSObject, LocationServiceProtocol, CLLocationManagerDelegate {
    /// The Core Location manager that provides location updates
    private let locationManager: CLLocationManager
    
    /// Logger for diagnostic information
    private let logger = Logger(subsystem: Bundle.main.bundleIdentifier ?? "com.websmithing.gpstracker2", category: "LocationService")

    // MARK: - Publishers
    
    /// Subject that emits location updates
    private let _locationSubject = PassthroughSubject<CLLocation, Never>()
    
    /// Publisher for location updates
    ///
    /// Subscribe to this publisher to receive location data in real-time.
    /// The publisher emits CLLocation objects containing coordinates, accuracy,
    /// speed, and other location-related information.
    var locationPublisher: AnyPublisher<CLLocation, Never> {
        _locationSubject.eraseToAnyPublisher()
    }

    /// Subject that emits authorization status changes
    private let _authorizationStatusSubject = PassthroughSubject<CLAuthorizationStatus, Never>()
    
    /// Publisher for location authorization status changes
    ///
    /// Subscribe to this publisher to be notified when the user changes
    /// location permission settings.
    var authorizationStatusPublisher: AnyPublisher<CLAuthorizationStatus, Never> {
        _authorizationStatusSubject.eraseToAnyPublisher()
    }

    /// Initializes the location service
    ///
    /// Sets up the Core Location manager with appropriate settings for
    /// continuous tracking in both foreground and background.
    override init() {
        self.locationManager = CLLocationManager()
        super.init()
        self.locationManager.delegate = self
        self.locationManager.desiredAccuracy = kCLLocationAccuracyBest // High accuracy needed for tracking
        self.locationManager.allowsBackgroundLocationUpdates = true // Enable background updates
        self.locationManager.pausesLocationUpdatesAutomatically = false // Prevent system from pausing updates
        self.locationManager.activityType = .other // General tracking

        // Enable battery monitoring for reporting to server
        UIDevice.current.isBatteryMonitoringEnabled = true
        log("Battery monitoring enabled.", logger: logger)

        log("LocationService initialized. Current status: \(self.getCurrentAuthorizationStatus().statusDescription)", level: .info, logger: logger)
        // Publish initial status
        _authorizationStatusSubject.send(self.getCurrentAuthorizationStatus())
    }

    /// Requests location permissions from the user
    ///
    /// Prompts the user to grant "Always" location access, which is required
    /// for proper background tracking functionality.
    func requestPermissions() {
        log("Requesting 'Always' location permissions.", level: .info, logger: logger)
        // Request Always authorization for background tracking
        locationManager.requestAlwaysAuthorization()
    }

    /// Starts the location update process
    ///
    /// Begins continuous location monitoring if appropriate permissions
    /// have been granted. Otherwise, logs an error and requests permissions
    /// if not yet determined.
    func startUpdatingLocation() {
        let status = getCurrentAuthorizationStatus()
        guard status == .authorizedAlways || status == .authorizedWhenInUse else {
            log("Cannot start location updates. Authorization status: \(status.statusDescription)", level: .error, logger: logger)
            // Optionally request permissions again or handle error
            if status == .notDetermined {
                requestPermissions()
            }
            return
        }

        log("Starting location updates.", level: .info, logger: logger)
        locationManager.startUpdatingLocation()
    }

    /// Stops the location update process
    ///
    /// Halts continuous location monitoring to conserve battery.
    func stopUpdatingLocation() {
        log("Stopping location updates.", level: .info, logger: logger)
        locationManager.stopUpdatingLocation()
    }

    /// Gets the current authorization status
    ///
    /// - Returns: The current location permissions status
    func getCurrentAuthorizationStatus() -> CLAuthorizationStatus {
        return locationManager.authorizationStatus
    }

    // MARK: - CLLocationManagerDelegate Methods

    /// Processes location updates from Core Location
    ///
    /// - Parameters:
    ///   - manager: The location manager providing the update
    ///   - locations: Array of location objects in chronological order
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        // Process the latest location
        guard let location = locations.last else {
            log("Received empty locations array in didUpdateLocations.", level: .error, logger: logger)
            return
        }
        log("Received location update: \(location.coordinate.latitude), \(location.coordinate.longitude) (Accuracy: \(location.horizontalAccuracy)m)", logger: logger)
        // Publish the location
        _locationSubject.send(location)
    }

    /// Handles location update failures
    ///
    /// - Parameters:
    ///   - manager: The location manager reporting the error
    ///   - error: The error that occurred
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        log("Location manager failed with error: \(error.localizedDescription)", level: .error, logger: logger)
        // Error handling strategy depends on error type
        // Some errors may be transient and can be ignored
    }

    /// Processes changes to location authorization status
    ///
    /// - Parameter manager: The location manager reporting the status change
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        let newStatus = manager.authorizationStatus
        log("Location authorization status changed to: \(newStatus.statusDescription)", level: .info, logger: logger)
        // Publish the new status
        _authorizationStatusSubject.send(newStatus)

        // Handle status changes
        switch newStatus {
        case .authorizedAlways, .authorizedWhenInUse:
            log("Authorization granted.", logger: logger)
        case .denied, .restricted:
            log("Location access denied or restricted. Stopping updates.", level: .error, logger: logger)
            stopUpdatingLocation() // Ensure updates are stopped if permissions revoked
        case .notDetermined:
            log("Authorization status is not determined.", level: .info, logger: logger)
        @unknown default:
            log("Unknown location authorization status encountered.", level: .fault, logger: logger)
        }
    }
}

// MARK: - Helper Extensions

/// Extension providing human-readable descriptions of authorization status
extension CLAuthorizationStatus {
    /// Returns a string description of the authorization status
    var statusDescription: String {
        switch self {
        case .notDetermined: return "Not Determined"
        case .restricted: return "Restricted"
        case .denied: return "Denied"
        case .authorizedAlways: return "Authorized Always"
        case .authorizedWhenInUse: return "Authorized When In Use"
        @unknown default: return "Unknown"
        }
    }
}