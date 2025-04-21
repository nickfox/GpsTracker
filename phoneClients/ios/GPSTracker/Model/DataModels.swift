// /Users/nickfox137/Documents/gpstracker-clients/gpstracker-ios/GPSTracker/GPSTracker/Model/DataModels.swift

import Foundation
import CoreLocation

/// Represents a single location data point captured by the tracking service
///
/// This struct encapsulates all the relevant information about a device's
/// location at a specific point in time.
///
/// ## Overview
/// Location data includes geographical coordinates, altitude, accuracy information,
/// movement data, and the timestamp when the location was recorded.
///
/// ## Topics
/// ### Core Properties
/// - ``coordinate``
/// - ``timestamp``
///
/// ### Accuracy Information
/// - ``horizontalAccuracy``
/// - ``verticalAccuracy``
///
/// ### Movement Data
/// - ``speed``
/// - ``course``
struct LocationData {
    /// The geographical coordinates (latitude and longitude)
    let coordinate: CLLocationCoordinate2D
    
    /// The altitude above sea level in meters (if available)
    let altitude: CLLocationDistance?
    
    /// The accuracy of the horizontal coordinates in meters
    let horizontalAccuracy: CLLocationAccuracy
    
    /// The accuracy of the altitude value in meters (if available)
    let verticalAccuracy: CLLocationAccuracy?
    
    /// The instantaneous speed of the device in meters per second (if available)
    let speed: CLLocationSpeed?
    
    /// The direction of travel in degrees relative to true north (if available)
    let course: CLLocationDirection?
    
    /// The time when this location was determined
    let timestamp: Date
}

/// Represents the status of the location data upload process
///
/// This enum tracks the current state of communication with the server
/// and provides information about successful or failed uploads.
///
/// ## Overview
/// The upload status transitions between idle, uploading, success, and failure states.
/// Success and failure states include timestamps for tracking the timing of events.
///
/// ## Topics
/// ### States
/// - ``idle``
/// - ``uploading``
/// - ``success(_:)``
/// - ``failure(_:_:)``
enum UploadStatus: Equatable {
    /// No upload is currently in progress
    case idle
    
    /// An upload is currently in progress
    case uploading
    
    /// The last upload completed successfully
    /// - Parameter Date: When the successful upload completed
    case success(Date)
    
    /// The last upload failed
    /// - Parameters:
    ///   - String: The error message describing the failure
    ///   - Date: When the failed upload occurred
    case failure(String, Date)

    /// A human-readable description of the upload status
    var description: String {
        switch self {
        case .idle:
            return "Idle"
        case .uploading:
            return "Uploading..."
        case .success:
            return "Last upload successful"
        case .failure(let error, _):
            return "Upload failed: \(error)"
        }
    }
}

/// Response data structure from the tracking server API
///
/// This struct represents the format of responses received from the
/// GPS tracking server after uploading location data.
///
/// ## Overview
/// The API response includes a status field indicating success or failure,
/// and an optional message with additional details.
struct APIResponse: Codable {
    /// The status of the API operation ("success" or "error")
    let status: String
    
    /// Optional message with additional details about the operation
    let message: String?
}

/// Request parameters for the location API
///
/// This struct contains all the parameters needed when sending location
/// data to the tracking server.
///
/// ## Overview
/// These parameters include location coordinates, device information,
/// movement data, battery status, and session identifiers.
///
/// ## Topics
/// ### Identification
/// - ``username``
/// - ``sessionid``
/// - ``appid``
///
/// ### Location Data
/// - ``latitude``
/// - ``longitude``
/// - ``altitude``
/// - ``accuracy``
///
/// ### Movement Data
/// - ``speed``
/// - ``direction``
/// - ``distance``
///
/// ### Metadata
/// - ``gps_time``
/// - ``location_method``
/// - ``provider``
/// - ``battery``
struct LocationAPIRequestParameters: Codable {
    /// The username of the account tracking this device
    let username: String
    
    /// Unique identifier for the current tracking session
    let sessionid: String
    
    /// Unique identifier for this app installation
    let appid: String
    
    /// The latitude coordinate in decimal degrees
    let latitude: Double
    
    /// The longitude coordinate in decimal degrees
    let longitude: Double
    
    /// The device speed in meters per second
    let speed: Double
    
    /// The direction of travel in degrees (0-360)
    let direction: Double
    
    /// The cumulative distance traveled in this session (meters)
    let distance: Double
    
    /// Formatted timestamp of when this location was recorded
    let gps_time: String
    
    /// How the location was determined (e.g., "gps", "network")
    let location_method: String
    
    /// Horizontal accuracy of the location in meters
    let accuracy: Double
    
    /// Altitude above sea level in meters
    let altitude: Double
    
    /// Identifier for the platform ("ios")
    let provider: String
    
    /// Current battery level as a percentage (0-100)
    let battery: Int

    /// Coding keys matching the expected server parameter names
    enum CodingKeys: String, CodingKey {
        case username
        case sessionid
        case appid
        case latitude
        case longitude
        case speed
        case direction
        case distance
        case gps_time
        case location_method
        case accuracy
        case altitude
        case provider
        case battery
    }
}

// MARK: - Helper Extensions

/// Extension providing human-readable descriptions of authorization status
public extension CLAuthorizationStatus {
    /// Returns a string description of the authorization status
    var description: String {
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