// /Users/nickfox137/Documents/gpstracker-clients/gpstracker-ios/GPSTracker/GPSTracker/View/MockDependencies.swift

import Foundation
import Combine
import CoreLocation

/// Provides mock dependencies for previews and testing
///
/// This file contains mock implementations of the app's protocols
/// to facilitate UI previews in Xcode and unit testing.
///
/// ## Overview
/// MockDependencies creates versions of services and repositories that
/// provide consistent, predetermined responses without requiring actual
/// system resources or networking.
///
/// ## Topics
/// ### Mock Factories
/// - ``previewViewModel``
/// - ``mockLocationService()``
/// - ``mockSettingsRepository()``

/// A mock implementation of LocationRepositoryProtocol for testing and previews
class MockLocationRepository: LocationRepositoryProtocol {
    /// Simulates uploading location data
    ///
    /// Instead of making real network requests, this method returns
    /// a simulated successful response after a short delay.
    ///
    /// - Parameter parameters: The location parameters that would be sent to the server
    /// - Returns: A publisher that simulates a successful API response
    func uploadLocation(parameters: LocationAPIRequestParameters) -> AnyPublisher<APIResponse, Error> {
        // Simulate a successful response with a slight delay
        return Just(APIResponse(status: "success", message: "Mock upload successful"))
            .delay(for: .seconds(0.5), scheduler: RunLoop.main)
            .setFailureType(to: Error.self)
            .eraseToAnyPublisher()
    }
}

/// A mock implementation of SettingsRepositoryProtocol for testing and previews
class MockSettingsRepository: SettingsRepositoryProtocol {
    /// Mock settings storage
    private var settings: [String: Any] = [
        "username": "demo_user",
        "server_url": "https://www.websmithing.com/gpstracker2/api/location",
        "tracking_interval": 10,
        "distance_filter": 5,
        "track_in_background": true,
        "app_id": "mock_app_id_12345"
    ]
    
    /// Returns the mock username
    /// - Returns: A predefined username for testing
    func getUsername() -> String {
        return settings["username"] as? String ?? "demo_user"
    }
    
    /// Returns the mock server URL
    /// - Returns: A predefined server URL for testing
    func getServerUrl() -> String {
        return settings["server_url"] as? String ?? "https://www.websmithing.com/gpstracker2/api/location"
    }
    
    /// Returns the mock tracking interval
    /// - Returns: A predefined tracking interval in seconds
    func getTrackingInterval() -> Int {
        return settings["tracking_interval"] as? Int ?? 10
    }
    
    /// Returns the mock distance filter
    /// - Returns: A predefined distance filter in meters
    func getDistanceFilter() -> Int {
        return settings["distance_filter"] as? Int ?? 5
    }
    
    /// Returns the mock background tracking setting
    /// - Returns: A predefined background tracking setting
    func getTrackInBackground() -> Bool {
        return settings["track_in_background"] as? Bool ?? true
    }
    
    /// Returns the mock app ID
    /// - Returns: A predefined app ID for testing
    func getAppId() -> String {
        return settings["app_id"] as? String ?? "mock_app_id_12345"
    }
    
    /// Stores a username value (mock implementation)
    /// - Parameter username: The username to store
    func saveUsername(_ username: String) {
        settings["username"] = username
    }
    
    /// Stores a server URL value (mock implementation)
    /// - Parameter url: The server URL to store
    func saveServerUrl(_ url: String) {
        settings["server_url"] = url
    }
    
    /// Stores a tracking interval value (mock implementation)
    /// - Parameter interval: The tracking interval to store
    func saveTrackingInterval(_ interval: Int) {
        settings["tracking_interval"] = interval
    }
    
    /// Stores a distance filter value (mock implementation)
    /// - Parameter distance: The distance filter to store
    func saveDistanceFilter(_ distance: Int) {
        settings["distance_filter"] = distance
    }
    
    /// Stores a background tracking setting (mock implementation)
    /// - Parameter enabled: The background tracking setting to store
    func saveTrackInBackground(_ enabled: Bool) {
        settings["track_in_background"] = enabled
    }
    
    /// Stores an app ID value (mock implementation)
    /// - Parameter appId: The app ID to store
    func saveAppId(_ appId: String) {
        settings["app_id"] = appId
    }
}

/// A mock implementation of LocationServiceProtocol for testing and previews
class MockLocationService: LocationServiceProtocol {
    /// Subject for publishing location updates
    private let locationSubject = PassthroughSubject<CLLocation, Never>()
    
    /// Publisher for location updates
    var locationPublisher: AnyPublisher<CLLocation, Never> {
        return locationSubject.eraseToAnyPublisher()
    }
    
    /// Subject for publishing authorization status changes
    private let authorizationSubject = PassthroughSubject<CLAuthorizationStatus, Never>()
    
    /// Publisher for authorization status changes
    var authorizationStatusPublisher: AnyPublisher<CLAuthorizationStatus, Never> {
        return authorizationSubject.eraseToAnyPublisher()
    }
    
    /// Mock authorization status
    private var authStatus: CLAuthorizationStatus = .authorizedAlways
    
    /// Timer for generating simulated location updates
    private var timer: Timer?
    
    /// Initializes the mock location service
    init() {
        // Emit initial authorization status
        authorizationSubject.send(authStatus)
    }
    
    /// Simulates requesting location permissions
    ///
    /// In a real app, this would trigger system permission dialogs.
    /// In this mock, it simply updates the status after a short delay.
    func requestPermissions() {
        // Simulate permission request with delayed response
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) { [weak self] in
            guard let self = self else { return }
            self.authStatus = .authorizedAlways
            self.authorizationSubject.send(self.authStatus)
        }
    }
    
    /// Simulates starting location updates
    ///
    /// Instead of using real device location, this method
    /// generates synthetic location data on a timer.
    func startUpdatingLocation() {
        // Stop any existing timer
        timer?.invalidate()
        
        // Start a new timer to simulate location updates
        timer = Timer.scheduledTimer(withTimeInterval: 5.0, repeats: true) { [weak self] _ in
            guard let self = self else { return }
            
            // Generate a random location near San Francisco
            let latitude = 37.7749 + Double.random(in: -0.01...0.01)
            let longitude = -122.4194 + Double.random(in: -0.01...0.01)
            let altitude = 10.0 + Double.random(in: 0...50)
            let speed = Double.random(in: 0...5)
            let course = Double.random(in: 0...360)
            
            // Create and publish the location
            let location = CLLocation(
                coordinate: CLLocationCoordinate2D(latitude: latitude, longitude: longitude),
                altitude: altitude,
                horizontalAccuracy: 10.0,
                verticalAccuracy: 10.0,
                course: course,
                speed: speed,
                timestamp: Date()
            )
            
            self.locationSubject.send(location)
        }
        
        // Fire immediately to get first location
        timer?.fire()
    }
    
    /// Simulates stopping location updates
    ///
    /// Stops the timer that generates synthetic location data.
    func stopUpdatingLocation() {
        timer?.invalidate()
        timer = nil
    }
    
    /// Returns the mock authorization status
    /// - Returns: The current mock authorization status
    func getCurrentAuthorizationStatus() -> CLAuthorizationStatus {
        return authStatus
    }
}

/// Factory for mock dependencies to use in SwiftUI previews
struct MockDependencies {
    /// Creates a fully configured view model with sample data for previews
    ///
    /// - Returns: A TrackingViewModel populated with realistic mock data
    static var previewViewModel: TrackingViewModel {
        let locationService = MockLocationService()
        let settingsRepository = MockSettingsRepository()
        let locationRepository = MockLocationRepository()
        
        let viewModel = TrackingViewModel(
            locationRepository: locationRepository,
            settingsRepository: settingsRepository,
            locationService: locationService
        )
        
        // Add sample data for preview
        viewModel.isTracking = true
        viewModel.totalDistance = 1582.5
        viewModel.sessionDuration = 1245.0 // About 20 minutes
        viewModel.currentSpeed = 3.2
        viewModel.averageSpeed = 2.6
        viewModel.maxSpeed = 5.8
        viewModel.locationCount = 78
        viewModel.uploadedCount = 75
        viewModel.uploadStatus = .success(Date().addingTimeInterval(-30))
        
        // Sample current location
        viewModel.currentLocation = CLLocation(
            coordinate: CLLocationCoordinate2D(latitude: 37.7749, longitude: -122.4194),
            altitude: 25.0,
            horizontalAccuracy: 8.0,
            verticalAccuracy: 12.0,
            course: 75.0,
            speed: 3.2,
            timestamp: Date()
        )
        
        // Generate simulated path
        var pathPoints: [LocationData] = []
        var speedData: [SpeedDataPoint] = []
        
        let startTime = Date().addingTimeInterval(-1245.0)
        for i in 0..<40 {
            let timeOffset = Double(i) * 30.0
            let timestamp = startTime.addingTimeInterval(timeOffset)
            
            // Create a sinusoidal path for visual interest
            let latitude = 37.77 + Double(i) * 0.0005 + sin(Double(i) * 0.2) * 0.001
            let longitude = -122.42 + Double(i) * 0.0005 + cos(Double(i) * 0.2) * 0.001
            let speed = 2.0 + sin(Double(i) * 0.4) * 2.0
            
            let locationData = LocationData(
                coordinate: CLLocationCoordinate2D(latitude: latitude, longitude: longitude),
                altitude: 10.0 + Double(i % 10),
                horizontalAccuracy: 8.0,
                verticalAccuracy: 12.0,
                speed: speed,
                course: Double((i * 10) % 360),
                timestamp: timestamp
            )
            
            pathPoints.append(locationData)
            speedData.append(SpeedDataPoint(speed: speed, timestamp: timestamp))
        }
        
        viewModel.pathPoints = pathPoints
        viewModel.speedData = speedData
        
        return viewModel
    }
}