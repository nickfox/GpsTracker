// /Users/nickfox137/Documents/gpstracker-clients/gpstracker-ios/GPSTracker/GPSTracker/View/SettingsView.swift

import SwiftUI

/// Settings configuration view for the GPS Tracker app
///
/// This view allows users to configure tracking settings, server details,
/// and manage app permissions.
///
/// ## Overview
/// SettingsView provides interfaces for:
/// - Setting user identification details
/// - Configuring server URL and API endpoints
/// - Adjusting tracking frequency and accuracy
/// - Managing location permissions
///
/// ## Topics
/// ### User Settings
/// - ``username``
/// - ``serverUrl``
/// - ``trackingInterval``
///
/// ### Permissions
/// - ``requestLocationPermission()``
struct SettingsView: View {
    /// The view model that manages app state
    @EnvironmentObject private var viewModel: TrackingViewModel
    
    /// Environment object to dismiss this view
    @Environment(\.dismiss) private var dismiss
    
    /// Username for identifying the device on the server
    @State private var username: String = ""
    
    /// Server URL for uploading location data
    @State private var serverUrl: String = ""
    
    /// Tracking frequency in seconds
    @State private var trackingInterval: Double = 10
    
    /// Minimum distance between location updates in meters
    @State private var distanceFilter: Double = 5
    
    /// Flag indicating if the app should continue tracking in background
    @State private var trackInBackground: Bool = true
    
    /// The body of the view defining its content and layout
    var body: some View {
        NavigationView {
            Form {
                // User identification section
                Section(header: Text("User Identification")) {
                    TextField("Username", text: $username)
                        .autocapitalization(.none)
                        .disableAutocorrection(true)
                }
                
                // Server configuration section
                Section(header: Text("Server Configuration")) {
                    TextField("Server URL", text: $serverUrl)
                        .autocapitalization(.none)
                        .disableAutocorrection(true)
                        .keyboardType(.URL)
                }
                
                // Tracking settings section
                Section(header: Text("Tracking Settings")) {
                    HStack {
                        Text("Update Interval")
                        Spacer()
                        Text("\(Int(trackingInterval)) seconds")
                    }
                    Slider(value: $trackingInterval, in: 1...60, step: 1)
                    
                    HStack {
                        Text("Distance Filter")
                        Spacer()
                        Text("\(Int(distanceFilter)) meters")
                    }
                    Slider(value: $distanceFilter, in: 0...100, step: 1)
                    
                    Toggle("Track in Background", isOn: $trackInBackground)
                }
                
                // Permissions section
                Section(header: Text("Permissions")) {
                    VStack(alignment: .leading) {
                        Text("Location: \(viewModel.locationAuthorizationStatus.description)")
                            .foregroundColor(locationStatusColor)
                        
                        if viewModel.locationAuthorizationStatus == .denied ||
                           viewModel.locationAuthorizationStatus == .restricted {
                            Button("Open Settings") {
                                openAppSettings()
                            }
                            .foregroundColor(.blue)
                            .padding(.top, 4)
                        } else if viewModel.locationAuthorizationStatus == .notDetermined {
                            Button("Request Permission") {
                                viewModel.requestLocationPermissions()
                            }
                            .foregroundColor(.blue)
                            .padding(.top, 4)
                        }
                    }
                }
                
                // App info section
                Section(header: Text("About")) {
                    HStack {
                        Text("Version")
                        Spacer()
                        Text(Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "Unknown")
                    }
                    
                    HStack {
                        Text("Build")
                        Spacer()
                        Text(Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "Unknown")
                    }
                }
            }
            .navigationTitle("Settings")
            .navigationBarItems(trailing: Button("Done") {
                saveSettings()
                dismiss()
            })
            .onAppear {
                // Load current settings when view appears
                loadSettings()
            }
        }
    }
    
    /// The color to display based on location authorization status
    private var locationStatusColor: Color {
        switch viewModel.locationAuthorizationStatus {
        case .authorizedAlways:
            return .green
        case .authorizedWhenInUse:
            return .yellow
        case .denied, .restricted:
            return .red
        case .notDetermined:
            return .gray
        @unknown default:
            return .gray
        }
    }
    
    /// Opens the system settings app to allow changing permissions
    private func openAppSettings() {
        guard let settingsURL = URL(string: UIApplication.openSettingsURLString) else {
            return
        }
        
        if UIApplication.shared.canOpenURL(settingsURL) {
            UIApplication.shared.open(settingsURL)
        }
    }
    
    /// Loads current settings from the view model
    private func loadSettings() {
        username = viewModel.username
        serverUrl = viewModel.serverUrl
        trackingInterval = Double(viewModel.trackingInterval)
        distanceFilter = Double(viewModel.distanceFilter)
        trackInBackground = viewModel.trackInBackground
    }
    
    /// Saves the current settings to the view model
    private func saveSettings() {
        viewModel.username = username
        viewModel.serverUrl = serverUrl
        viewModel.trackingInterval = Int(trackingInterval)
        viewModel.distanceFilter = Int(distanceFilter)
        viewModel.trackInBackground = trackInBackground
        
        // Apply settings to active tracking if currently tracking
        if viewModel.isTracking {
            viewModel.applySettings()
        }
    }
}

/// Preview provider for displaying SettingsView in Xcode previews
struct SettingsView_Previews: PreviewProvider {
    static var previews: some View {
        SettingsView()
            .environmentObject(MockDependencies.previewViewModel)
            .preferredColorScheme(.dark)
    }
}