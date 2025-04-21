// /Users/nickfox137/Documents/gpstracker-clients/gpstracker-ios/GPSTracker/GPSTracker/View/ContentView.swift

import SwiftUI
import MapKit
import CoreLocation

/// The main view of the GPS Tracker application
///
/// This view serves as the primary interface for the application, displaying
/// the tracking map, controls, and providing navigation to other views.
///
/// ## Overview
/// ContentView includes:
/// - A map display showing the current location and tracking path
/// - Tracking controls (start/stop)
/// - Status information display
/// - Navigation to settings and statistics views
///
/// ## Topics
/// ### Location Display
/// - ``mapRegion``
/// - ``userTrackingMode``
///
/// ### Tracking Controls
/// - ``startTracking()``
/// - ``stopTracking()``
struct ContentView: View {
    /// The view model that manages tracking logic and state
    @EnvironmentObject private var viewModel: TrackingViewModel
    
    /// Controls whether the settings sheet is displayed
    @State private var showingSettings = false
    
    /// Controls whether the statistics view is displayed
    @State private var showingStats = false
    
    /// The region displayed on the map
    @State private var mapRegion = MKCoordinateRegion(
        center: CLLocationCoordinate2D(latitude: 37.7749, longitude: -122.4194), // Default to San Francisco
        span: MKCoordinateSpan(latitudeDelta: 0.05, longitudeDelta: 0.05)
    )
    
    /// The mode for user tracking on the map
    @State private var userTrackingMode: MapUserTrackingMode = .follow
    
    /// The body of the view defining its content and layout
    var body: some View {
        ZStack {
            // Map view taking up the entire screen
            mapView
                .ignoresSafeArea()
            
            // Overlay content on top of the map
            VStack {
                // Top status bar
                statusBar
                    .padding()
                    .background(Color.black.opacity(0.7))
                    .cornerRadius(10)
                    .padding()
                
                Spacer()
                
                // Bottom control panel
                controlPanel
                    .padding()
                    .background(Color.black.opacity(0.7))
                    .cornerRadius(10)
                    .padding()
            }
        }
        .sheet(isPresented: $showingSettings) {
            SettingsView()
                .environmentObject(viewModel)
        }
        .sheet(isPresented: $showingStats) {
            StatsView()
                .environmentObject(viewModel)
        }
        .onAppear {
            // Request location permissions when the view appears
            viewModel.requestLocationPermissions()
            
            // Set up location updates to update the map
            viewModel.onLocationUpdate = { location in
                withAnimation {
                    self.mapRegion = MKCoordinateRegion(
                        center: location.coordinate,
                        span: MKCoordinateSpan(latitudeDelta: 0.005, longitudeDelta: 0.005)
                    )
                }
            }
        }
    }
    
    /// The map view displaying the user's current location and path
    private var mapView: some View {
        Map(coordinateRegion: $mapRegion, 
            showsUserLocation: true, 
            userTrackingMode: $userTrackingMode,
            annotationItems: viewModel.pathPoints) { point in
            MapAnnotation(coordinate: point.coordinate) {
                Circle()
                    .fill(Color.blue)
                    .frame(width: 6, height: 6)
            }
        }
    }
    
    /// The status bar showing tracking information
    private var statusBar: some View {
        HStack {
            VStack(alignment: .leading) {
                Text("GPS Tracker")
                    .font(.headline)
                
                if let location = viewModel.currentLocation {
                    Text("Location: \(String(format: "%.6f", location.coordinate.latitude)), \(String(format: "%.6f", location.coordinate.longitude))")
                        .font(.caption)
                        .foregroundColor(.gray)
                }
                
                Text("Status: \(viewModel.isTracking ? "Tracking" : "Idle")")
                    .font(.caption)
                    .foregroundColor(viewModel.isTracking ? .green : .gray)
                
                Text("Upload: \(viewModel.uploadStatus.description)")
                    .font(.caption)
                    .foregroundColor(uploadStatusColor)
            }
            
            Spacer()
            
            Button(action: {
                showingStats = true
            }) {
                Image(systemName: "chart.bar")
                    .font(.title)
                    .foregroundColor(.white)
            }
        }
    }
    
    /// The control panel with tracking buttons
    private var controlPanel: some View {
        HStack {
            // Settings button
            Button(action: {
                showingSettings = true
            }) {
                Image(systemName: "gear")
                    .font(.title)
                    .padding()
                    .background(Color.gray.opacity(0.5))
                    .clipShape(Circle())
            }
            
            Spacer()
            
            // Start/Stop tracking button
            Button(action: {
                viewModel.isTracking ? viewModel.stopTracking() : viewModel.startTracking()
            }) {
                Image(systemName: viewModel.isTracking ? "stop.fill" : "play.fill")
                    .font(.largeTitle)
                    .padding()
                    .background(viewModel.isTracking ? Color.red : Color.green)
                    .clipShape(Circle())
                    .shadow(radius: 5)
            }
            
            Spacer()
            
            // Center on user button
            Button(action: {
                userTrackingMode = .follow
                if let location = viewModel.currentLocation?.coordinate {
                    mapRegion.center = location
                }
            }) {
                Image(systemName: "location")
                    .font(.title)
                    .padding()
                    .background(Color.gray.opacity(0.5))
                    .clipShape(Circle())
            }
        }
    }
    
    /// The color to use for the upload status text
    private var uploadStatusColor: Color {
        switch viewModel.uploadStatus {
        case .idle:
            return .gray
        case .uploading:
            return .yellow
        case .success:
            return .green
        case .failure:
            return .red
        }
    }
}

/// Preview provider for displaying ContentView in Xcode previews
struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
            .environmentObject(MockDependencies.previewViewModel)
            .preferredColorScheme(.dark)
    }
}