// /Users/nickfox137/Documents/gpstracker-clients/gpstracker-ios/GPSTracker/GPSTracker/Repository/LocationRepository.swift

import Foundation
import Combine
import CoreLocation
import os

/// Implementation of LocationRepositoryProtocol
///
/// This class coordinates between location services and API services to
/// process and transmit location data to the server.
///
/// ## Overview
/// The location repository manages the flow of location data from the device
/// sensors to the remote server. It handles formatting, processing, and transmission
/// of location information.
///
/// ## Topics
/// ### Location Processing
/// - ``processLocation(_:)``
///
/// ### Data Transmission
/// - ``uploadLocation(parameters:)``
class LocationRepository: LocationRepositoryProtocol {
    /// Logger for diagnostic information
    private let logger = Logger(subsystem: Bundle.main.bundleIdentifier ?? "com.websmithing.gpstracker2", category: "LocationRepository")
    
    /// Service for obtaining location updates
    private let locationService: LocationServiceProtocol
    
    /// Service for API communication with the server
    private let apiService: APIServiceProtocol
    
    /// Repository for accessing user settings
    private let settingsRepository: SettingsRepositoryProtocol
    
    /// Set of subscription cancellables
    private var cancellables = Set<AnyCancellable>()
    
    /// Cumulative distance traveled in the current session
    private var totalDistance: Double = 0
    
    /// Previous location for distance calculations
    private var previousLocation: CLLocation?
    
    /// Initializes the location repository with required dependencies
    ///
    /// - Parameters:
    ///   - locationService: Service for obtaining location updates
    ///   - apiService: Service for API communication
    ///   - settingsRepository: Repository for accessing settings
    init(locationService: LocationServiceProtocol, 
         apiService: APIServiceProtocol,
         settingsRepository: SettingsRepositoryProtocol) {
        self.locationService = locationService
        self.apiService = apiService
        self.settingsRepository = settingsRepository
        
        log("LocationRepository initialized", logger: logger)
    }
    
    /// Uploads location data to the tracking server
    ///
    /// - Parameter parameters: The location data to upload
    /// - Returns: A publisher that emits the server response or an error
    func uploadLocation(parameters: LocationAPIRequestParameters) -> AnyPublisher<APIResponse, Error> {
        log("Uploading location: \(parameters.latitude), \(parameters.longitude)", logger: logger)
        
        return apiService.uploadLocation(parameters: parameters)
            .handleEvents(
                receiveOutput: { [weak self] response in
                    self?.log("Upload successful: \(response.status)", logger: self?.logger)
                },
                receiveCompletion: { [weak self] completion in
                    if case let .failure(error) = completion {
                        self?.log("Upload failed: \(error.localizedDescription)", level: .error, logger: self?.logger)
                    }
                }
            )
            .eraseToAnyPublisher()
    }
    
    /// Processes a new location update
    ///
    /// This method handles distance calculation, data formatting, and 
    /// other preparation steps before transmission.
    ///
    /// - Parameter location: The new location from the location service
    /// - Returns: Processed location data
    private func processLocation(_ location: CLLocation) -> LocationData {
        // Calculate distance if we have a previous location
        if let previous = previousLocation {
            let increment = location.distance(from: previous)
            totalDistance += increment
        }
        
        // Update previous location for next calculation
        previousLocation = location
        
        // Create and return the location data
        return LocationData(
            coordinate: location.coordinate,
            altitude: location.altitude,
            horizontalAccuracy: location.horizontalAccuracy,
            verticalAccuracy: location.verticalAccuracy,
            speed: max(0, location.speed), // Ensure non-negative speed
            course: location.course,
            timestamp: location.timestamp
        )
    }
    
    /// Resets tracking session data
    ///
    /// Call this method when starting a new tracking session.
    func resetSession() {
        totalDistance = 0
        previousLocation = nil
        log("Location session reset", logger: logger)
    }
    
    /// Returns the total distance traveled in the current session
    ///
    /// - Returns: Distance in meters
    func getCurrentDistance() -> Double {
        return totalDistance
    }
}