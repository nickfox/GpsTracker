// /Users/nickfox137/Documents/gpstracker-clients/gpstracker-ios/GPSTracker/GPSTracker/ViewModel/TrackingViewModel.swift

import Foundation
import Combine
import CoreLocation
import SwiftUI
import os

/// A data point containing speed information with timestamp
struct SpeedDataPoint: Identifiable {
    /// Unique identifier for this data point
    let id = UUID()
    
    /// Speed in meters per second
    let speed: Double
    
    /// When this speed was recorded
    let timestamp: Date
}

/// The view model for tracking functionality
///
/// This class coordinates all aspects of location tracking, including permission management,
/// data collection, server communication, and statistics calculation.
///
/// ## Overview
/// TrackingViewModel serves as the primary coordinator between the UI layer and the 
/// data/service layers. It:
/// - Manages tracking state and settings
/// - Processes location updates
/// - Calculates tracking statistics
/// - Coordinates uploads to the server
///
/// ## Topics
/// ### Tracking Controls
/// - ``startTracking()``
/// - ``stopTracking()``
/// - ``applySettings()``
///
/// ### Location Management
/// - ``requestLocationPermissions()``
/// - ``locationAuthorizationStatus``
///
/// ### Tracking State
/// - ``isTracking``
/// - ``currentLocation``
/// - ``uploadStatus``
///
/// ### Statistics
/// - ``totalDistance``
/// - ``sessionDuration``
/// - ``currentSpeed``
/// - ``averageSpeed``
/// - ``maxSpeed``
class TrackingViewModel: ObservableObject {
    // MARK: - Published Properties
    
    /// Whether tracking is currently active
    @Published var isTracking = false
    
    /// The current device location
    @Published var currentLocation: CLLocation?
    
    /// Status of uploads to the server
    @Published var uploadStatus: UploadStatus = .idle
    
    /// Current authorization status for location services
    @Published var locationAuthorizationStatus: CLAuthorizationStatus = .notDetermined
    
    /// Total distance traveled in meters
    @Published var totalDistance: Double = 0
    
    /// Duration of the current tracking session in seconds
    @Published var sessionDuration: TimeInterval = 0
    
    /// Current speed in meters per second
    @Published var currentSpeed: Double = 0
    
    /// Average speed over the session in meters per second
    @Published var averageSpeed: Double = 0
    
    /// Maximum speed recorded in meters per second
    @Published var maxSpeed: Double = 0
    
    /// Count of location points collected
    @Published var locationCount: Int = 0
    
    /// Count of location points successfully uploaded
    @Published var uploadedCount: Int = 0
    
    /// Array of location points for displaying the path
    @Published var pathPoints: [LocationData] = []
    
    /// Array of speed data for charts
    @Published var speedData: [SpeedDataPoint] = []
    
    // MARK: - Settings Properties
    
    /// Username for identifying this device on the server
    @Published var username: String = "demo"
    
    /// Server URL for uploading location data
    @Published var serverUrl: String = "https://www.websmithing.com/gpstracker2/api/location"
    
    /// Time interval between location updates in seconds
    @Published var trackingInterval: Int = 10
    
    /// Minimum distance between location updates in meters
    @Published var distanceFilter: Int = 5
    
    /// Whether to continue tracking in the background
    @Published var trackInBackground: Bool = true
    
    /// Callback for handling location updates in the UI
    var onLocationUpdate: ((CLLocation) -> Void)?
    
    // MARK: - Private Properties
    
    /// Logger for diagnostic information
    private let logger = Logger(subsystem: Bundle.main.bundleIdentifier ?? "com.websmithing.gpstracker2", category: "TrackingViewModel")
    
    /// Repository for location data management
    private let locationRepository: LocationRepositoryProtocol
    
    /// Repository for settings management
    private let settingsRepository: SettingsRepositoryProtocol
    
    /// Service for location updates
    private let locationService: LocationServiceProtocol
    
    /// Set of subscription cancellables
    private var cancellables = Set<AnyCancellable>()
    
    /// Timer for updating session duration
    private var durationTimer: Timer?
    
    /// Start time of the current tracking session
    private var sessionStartTime: Date?
    
    /// Unique identifier for the current tracking session
    private var sessionId: String = ""
    
    /// Unique identifier for this app installation
    private var appId: String = ""
    
    // MARK: - Initialization
    
    /// Initializes the view model with required dependencies
    ///
    /// - Parameters:
    ///   - locationRepository: Repository for location data management
    ///   - settingsRepository: Repository for settings management
    ///   - locationService: Service for location updates
    init(locationRepository: LocationRepositoryProtocol,
         settingsRepository: SettingsRepositoryProtocol,
         locationService: LocationServiceProtocol) {
        self.locationRepository = locationRepository
        self.settingsRepository = settingsRepository
        self.locationService = locationService
        
        // Load settings
        loadSettings()
        
        // Generate app ID if not already set
        if appId.isEmpty {
            appId = UUID().uuidString
            settingsRepository.saveAppId(appId)
        }
        
        // Set up location authorization status subscription
        locationService.authorizationStatusPublisher
            .receive(on: DispatchQueue.main)
            .sink { [weak self] status in
                self?.locationAuthorizationStatus = status
            }
            .store(in: &cancellables)
        
        // Initialize with current authorization status
        locationAuthorizationStatus = locationService.getCurrentAuthorizationStatus()
        
        log("TrackingViewModel initialized", logger: logger)
    }
    
    // MARK: - Public Methods
    
    /// Starts location tracking
    ///
    /// This method:
    /// - Generates a new session ID
    /// - Resets tracking statistics
    /// - Starts location services
    /// - Begins timing the session
    func startTracking() {
        guard !isTracking else { return }
        
        // Generate a new session ID
        sessionId = UUID().uuidString
        
        // Reset tracking statistics
        resetStatistics()
        
        // Start location tracking
        locationService.startUpdatingLocation()
        
        // Set up location update subscription
        locationService.locationPublisher
            .receive(on: DispatchQueue.main)
            .sink { [weak self] location in
                self?.handleLocationUpdate(location)
            }
            .store(in: &cancellables)
        
        // Start session timer
        sessionStartTime = Date()
        durationTimer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { [weak self] _ in
            guard let self = self, let startTime = self.sessionStartTime else { return }
            self.sessionDuration = Date().timeIntervalSince(startTime)
        }
        
        // Update tracking state
        isTracking = true
        log("Location tracking started. Session ID: \(sessionId)", logger: logger)
    }
    
    /// Stops location tracking
    ///
    /// This method:
    /// - Stops location services
    /// - Stops the session timer
    /// - Clears subscriptions
    func stopTracking() {
        guard isTracking else { return }
        
        // Stop location tracking
        locationService.stopUpdatingLocation()
        
        // Stop the duration timer
        durationTimer?.invalidate()
        durationTimer = nil
        
        // Clear subscriptions
        cancellables.removeAll()
        
        // Update tracking state
        isTracking = false
        log("Location tracking stopped. Session duration: \(String(format: "%.1f", sessionDuration)) seconds", logger: logger)
        
        // Resubscribe to authorization status changes
        locationService.authorizationStatusPublisher
            .receive(on: DispatchQueue.main)
            .sink { [weak self] status in
                self?.locationAuthorizationStatus = status
            }
            .store(in: &cancellables)
    }
    
    /// Applies current settings to active tracking
    ///
    /// Call this method after changing settings to apply them
    /// to an active tracking session.
    func applySettings() {
        guard isTracking else { return }
        
        // Apply settings to location service
        // Note: This would typically involve stopping and restarting tracking
        // with new parameters, but implementation details depend on the specific
        // location service being used
        
        log("Applied settings to active tracking session", logger: logger)
    }
    
    /// Requests location permissions from the user
    ///
    /// This method prompts the user to grant location access permissions.
    func requestLocationPermissions() {
        locationService.requestPermissions()
    }
    
    // MARK: - Private Methods
    
    /// Loads user settings from the settings repository
    private func loadSettings() {
        username = settingsRepository.getUsername()
        serverUrl = settingsRepository.getServerUrl()
        trackingInterval = settingsRepository.getTrackingInterval()
        distanceFilter = settingsRepository.getDistanceFilter()
        trackInBackground = settingsRepository.getTrackInBackground()
        appId = settingsRepository.getAppId()
    }
    
    /// Resets all tracking statistics to zero
    private func resetStatistics() {
        totalDistance = 0
        sessionDuration = 0
        currentSpeed = 0
        averageSpeed = 0
        maxSpeed = 0
        locationCount = 0
        uploadedCount = 0
        pathPoints = []
        speedData = []
    }
    
    /// Processes a new location update
    /// 
    /// - Parameter location: The new location data from Core Location
    private func handleLocationUpdate(_ location: CLLocation) {
        // Update current location
        let previousLocation = currentLocation
        currentLocation = location
        
        // Create location data model
        let locationData = LocationData(
            coordinate: location.coordinate,
            altitude: location.altitude,
            horizontalAccuracy: location.horizontalAccuracy,
            verticalAccuracy: location.verticalAccuracy,
            speed: location.speed,
            course: location.course,
            timestamp: location.timestamp
        )
        
        // Update path for display
        pathPoints.append(locationData)
        
        // Update statistics
        locationCount += 1
        currentSpeed = max(0, location.speed) // CLLocation may return negative speed for invalid measurements
        
        // Add speed data point for charts
        let speedPoint = SpeedDataPoint(speed: currentSpeed, timestamp: location.timestamp)
        speedData.append(speedPoint)
        
        // Update maximum speed
        if currentSpeed > maxSpeed {
            maxSpeed = currentSpeed
        }
        
        // Calculate distance if we have a previous location
        if let previous = previousLocation {
            let increment = location.distance(from: previous)
            totalDistance += increment
        }
        
        // Calculate average speed
        if sessionDuration > 0 {
            averageSpeed = totalDistance / sessionDuration
        }
        
        // Invoke location update callback for UI updates
        onLocationUpdate?(location)
        
        // Upload location to server
        uploadLocationToServer(location)
        
        log("Processed location update: (\(location.coordinate.latitude), \(location.coordinate.longitude))", logger: logger)
    }
    
    /// Uploads location data to the tracking server
    /// 
    /// - Parameter location: The location to upload
    private func uploadLocationToServer(_ location: CLLocation) {
        // Skip upload if we're already uploading
        if case .uploading = uploadStatus {
            return
        }
        
        // Update upload status
        uploadStatus = .uploading
        
        // Get battery level
        let batteryLevel = Int(UIDevice.current.batteryLevel * 100)
        
        // Format timestamp
        let dateFormatter = ISO8601DateFormatter()
        let timeString = dateFormatter.string(from: location.timestamp)
        
        // Create request parameters
        let parameters = LocationAPIRequestParameters(
            username: username,
            sessionid: sessionId,
            appid: appId,
            latitude: location.coordinate.latitude,
            longitude: location.coordinate.longitude,
            speed: max(0, location.speed),
            direction: location.course,
            distance: totalDistance,
            gps_time: timeString,
            location_method: "gps",
            accuracy: location.horizontalAccuracy,
            altitude: location.altitude,
            provider: "ios",
            battery: batteryLevel
        )
        
        // Upload location data
        locationRepository.uploadLocation(parameters: parameters)
            .receive(on: DispatchQueue.main)
            .sink(
                receiveCompletion: { [weak self] completion in
                    guard let self = self else { return }
                    
                    switch completion {
                    case .finished:
                        // Nothing to do here, handled by receiveValue
                        break
                    case .failure(let error):
                        // Update upload status with error
                        self.uploadStatus = .failure(error.localizedDescription, Date())
                        self.log("Upload failed: \(error.localizedDescription)", level: .error, logger: self.logger)
                    }
                },
                receiveValue: { [weak self] response in
                    guard let self = self else { return }
                    
                    // Update upload status
                    self.uploadStatus = .success(Date())
                    self.uploadedCount += 1
                    
                    self.log("Upload successful: \(response.status)", logger: self.logger)
                }
            )
            .store(in: &cancellables)
    }
}

// MARK: - Mock Dependencies

/// Factory for creating mock/preview dependencies
struct MockDependencies {
    /// Creates a view model populated with sample data for previews
    static var previewViewModel: TrackingViewModel {
        let viewModel = TrackingViewModel(
            locationRepository: MockLocationRepository(),
            settingsRepository: MockSettingsRepository(),
            locationService: MockLocationService()
        )
        
        // Set up sample data
        viewModel.isTracking = true
        viewModel.currentLocation = CLLocation(
            coordinate: CLLocationCoordinate2D(latitude: 37.7749, longitude: -122.4194),
            altitude: 10,
            horizontalAccuracy: 5,
            verticalAccuracy: 10,
            course: 45,
            speed: 3.5,
            timestamp: Date()
        )
        viewModel.totalDistance = 1250
        viewModel.sessionDuration = 600 // 10 minutes
        viewModel.currentSpeed = 3.5
        viewModel.averageSpeed = 2.8
        viewModel.maxSpeed = 5.2
        viewModel.locationCount = 42
        viewModel.uploadedCount = 38
        viewModel.uploadStatus = .success(Date().addingTimeInterval(-30))
        
        // Generate sample path points
        var samplePoints: [LocationData] = []
        var sampleSpeedData: [SpeedDataPoint] = []
        
        let startDate = Date().addingTimeInterval(-600) // 10 minutes ago
        for i in 0..<20 {
            let lat = 37.7749 + Double(i) * 0.001
            let lon = -122.4194 + Double(i) * 0.001
            let time = startDate.addingTimeInterval(Double(i) * 30)
            let speed = 2.0 + sin(Double(i) * 0.5) * 3.0 // Varies between 2-5 m/s
            
            samplePoints.append(LocationData(
                coordinate: CLLocationCoordinate2D(latitude: lat, longitude: lon),
                altitude: 10 + Double(i),
                horizontalAccuracy: 5,
                verticalAccuracy: 10,
                speed: speed,
                course: 45,
                timestamp: time
            ))
            
            sampleSpeedData.append(SpeedDataPoint(
                speed: speed,
                timestamp: time
            ))
        }
        
        viewModel.pathPoints = samplePoints
        viewModel.speedData = sampleSpeedData
        
        return viewModel
    }
}

/// Mock location repository for previews
private class MockLocationRepository: LocationRepositoryProtocol {
    func uploadLocation(parameters: LocationAPIRequestParameters) -> AnyPublisher<APIResponse, Error> {
        // Simulate a successful response after a short delay
        return Just(APIResponse(status: "success", message: "Location uploaded successfully"))
            .setFailureType(to: Error.self)
            .delay(for: .seconds(0.5), scheduler: RunLoop.main)
            .eraseToAnyPublisher()
    }
}

/// Mock settings repository for previews
private class MockSettingsRepository: SettingsRepositoryProtocol {
    func getUsername() -> String { "preview_user" }
    func getServerUrl() -> String { "https://www.websmithing.com/gpstracker2/api/location" }
    func getTrackingInterval() -> Int { 10 }
    func getDistanceFilter() -> Int { 5 }
    func getTrackInBackground() -> Bool { true }
    func getAppId() -> String { "preview_app_id" }
    
    func saveUsername(_ username: String) {}
    func saveServerUrl(_ url: String) {}
    func saveTrackingInterval(_ interval: Int) {}
    func saveDistanceFilter(_ distance: Int) {}
    func saveTrackInBackground(_ enabled: Bool) {}
    func saveAppId(_ appId: String) {}
}

/// Mock location service for previews
private class MockLocationService: LocationServiceProtocol {
    let locationPublisher = PassthroughSubject<CLLocation, Never>().eraseToAnyPublisher()
    let authorizationStatusPublisher = PassthroughSubject<CLAuthorizationStatus, Never>().eraseToAnyPublisher()
    
    func requestPermissions() {}
    func startUpdatingLocation() {}
    func stopUpdatingLocation() {}
    func getCurrentAuthorizationStatus() -> CLAuthorizationStatus { .authorizedAlways }
}