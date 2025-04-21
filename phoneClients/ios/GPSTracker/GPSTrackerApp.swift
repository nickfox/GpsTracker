
import SwiftUI
import os // Import OSLog
import UIKit // For appearance customization

/// The main entry point for the GPSTracker iOS application.
///
/// This struct handles application initialization, dependency injection, and user interface setup.
/// It follows a clean architecture design pattern with separate view, viewmodel, repository and service layers.
///
/// - Important: This app requires location permissions to function correctly.
///
/// ## Overview
/// GPSTracker is an open source location tracking application that records and transmits
/// location data to a server. It operates in both foreground and background modes to
/// provide continuous tracking capabilities.
///
/// ## Topics
/// ### Application Structure
/// - ``ContentView``
/// - ``TrackingViewModel``
/// - ``LocationRepository``
/// - ``SettingsRepository``
@main
struct GPSTrackerApp: App {
    // Logger for the application lifecycle - Use static for access before init if needed elsewhere
    /// Application-wide logger for monitoring and debugging
    private static let logger = Logger(subsystem: Bundle.main.bundleIdentifier ?? "com.websmithing.gpstracker2", category: "GPSTrackerApp")
    
    /// Initializes the application and configures dark mode appearance
    ///
    /// This initializer sets up the application's user interface style to always use dark mode
    /// for improved visibility of the tracking interface and maps.
    init() {
        // Set the app to always use dark mode
        // For iOS 13+ compatibility
        if #available(iOS 15.0, *) {
            let scenes = UIApplication.shared.connectedScenes
            let windowScenes = scenes.compactMap { $0 as? UIWindowScene }
            windowScenes.forEach { windowScene in
                windowScene.windows.forEach { window in
                    window.overrideUserInterfaceStyle = .dark
                }
            }
        } else {
            UIApplication.shared.windows.forEach { window in
                window.overrideUserInterfaceStyle = .dark
            }
        }
    }

    // MARK: - Dependencies
    
    /// Persistence service for user settings and local data storage
    private static let persistenceService: PersistenceServiceProtocol = PersistenceService()
    
    /// Repository for managing user settings and preferences
    private static let settingsRepository: SettingsRepositoryProtocol = SettingsRepository(persistenceService: persistenceService)
    
    /// Service for managing location updates and permissions
    private static let locationService: LocationServiceProtocol = LocationService()
    
    /// Service for communication with the remote tracking server
    private static let apiService: APIServiceProtocol = APIService()
    
    /// Repository for location data processing and transmission
    private static let locationRepository: LocationRepositoryProtocol = LocationRepository(
        locationService: locationService,
        apiService: apiService,
        settingsRepository: settingsRepository
    )

    /// Primary view model that coordinates tracking functionality
    ///
    /// This StateObject is injected into the view hierarchy through the environment
    @StateObject private var viewModel: TrackingViewModel = TrackingViewModel(
        locationRepository: Self.locationRepository,
        settingsRepository: Self.settingsRepository,
        locationService: Self.locationService
    )

    /// The root scene of the application
    ///
    /// Creates the main window group and injects dependencies into the environment
    var body: some Scene {
        WindowGroup {
            makeContentView()
                .onAppear {
                    // Log appearance from the WindowGroup level
                    log("Root WindowGroup appeared.", level: .debug, logger: Self.logger)
                }
        }
    }

    /// Creates the main content view with properly injected dependencies
    ///
    /// - Returns: The configured ContentView with its required environment objects
    private func makeContentView() -> some View {
        ContentView()
            .environmentObject(viewModel)
    }
}
