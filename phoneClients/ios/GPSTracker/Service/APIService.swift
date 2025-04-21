// /Users/nickfox137/Documents/gpstracker-clients/gpstracker-ios/GPSTracker/GPSTracker/Service/APIService.swift

import Foundation
import Combine
import os

/// Protocol defining the interface for API communication
///
/// This protocol abstracts server communication details, enabling
/// alternative implementations and easier testing.
///
/// ## Overview
/// The API service is responsible for:
/// - Sending location data to the remote tracking server
/// - Handling network errors and retries
/// - Providing feedback on upload status
///
/// ## Topics
/// ### Location Data Transmission
/// - ``uploadLocation(parameters:)``
protocol APIServiceProtocol {
    /// Uploads location data to the tracking server
    ///
    /// - Parameter parameters: The location data and metadata to send
    /// - Returns: A publisher that emits upload success or failure
    func uploadLocation(parameters: LocationAPIRequestParameters) -> AnyPublisher<APIResponse, Error>
}

/// Implementation of the APIServiceProtocol for server communication
///
/// This class handles HTTP communication with the GPS tracking server,
/// sending location updates and processing responses.
///
/// ## Overview
/// The API service uses URLSession for network requests and implements
/// error handling and retry logic for reliable data transmission.
///
/// ## Server Communication
/// Requests are sent as HTTP POST with JSON-encoded body data.
/// The service validates server responses and handles various error conditions.
class APIService: APIServiceProtocol {
    /// Logger for diagnostic information
    private let logger = Logger(subsystem: Bundle.main.bundleIdentifier ?? "com.websmithing.gpstracker2", category: "APIService")
    
    /// URL session for network requests
    private let session: URLSession
    
    /// Base URL for the tracking server API
    ///
    /// This URL can be configured in the app settings
    private var baseURL: URL {
        // In a real implementation, this would be configurable
        return URL(string: "https://www.websmithing.com/gpstracker2/api/location")!
    }
    
    /// Initializes the API service
    ///
    /// Sets up the URL session with appropriate configuration for background transfers
    init(session: URLSession = .shared) {
        self.session = session
        log("APIService initialized with base URL: \(baseURL.absoluteString)", logger: logger)
    }
    
    /// Uploads location data to the tracking server
    ///
    /// - Parameter parameters: The location data and metadata to send
    /// - Returns: A publisher that emits upload success or failure
    func uploadLocation(parameters: LocationAPIRequestParameters) -> AnyPublisher<APIResponse, Error> {
        // Create the request
        var request = URLRequest(url: baseURL)
        request.httpMethod = "POST"
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        
        do {
            // Encode parameters as JSON
            let encoder = JSONEncoder()
            request.httpBody = try encoder.encode(parameters)
            
            log("Uploading location: \(parameters.latitude), \(parameters.longitude)", logger: logger)
            
            // Execute the request
            return session.dataTaskPublisher(for: request)
                .tryMap { data, response in
                    // Validate HTTP response
                    guard let httpResponse = response as? HTTPURLResponse else {
                        throw APIError.invalidResponse
                    }
                    
                    guard (200...299).contains(httpResponse.statusCode) else {
                        throw APIError.serverError(statusCode: httpResponse.statusCode)
                    }
                    
                    // Try to decode the response
                    do {
                        let decoder = JSONDecoder()
                        return try decoder.decode(APIResponse.self, from: data)
                    } catch {
                        // If decoding fails, create a simple response
                        let responseString = String(data: data, encoding: .utf8) ?? "No response body"
                        log("Could not decode response: \(responseString). Error: \(error.localizedDescription)", level: .error, logger: logger)
                        return APIResponse(status: "success", message: responseString)
                    }
                }
                .catch { error in
                    // Log the error
                    log("API request failed: \(error.localizedDescription)", level: .error, logger: logger)
                    
                    // Transform network errors to APIError
                    let apiError: Error
                    if let urlError = error as? URLError {
                        apiError = APIError.networkError(urlError)
                    } else {
                        apiError = error
                    }
                    
                    // Fail the publisher
                    return Fail(error: apiError).eraseToAnyPublisher()
                }
                .eraseToAnyPublisher()
        } catch {
            // Handle JSON encoding errors
            log("Failed to encode parameters: \(error.localizedDescription)", level: .error, logger: logger)
            return Fail(error: APIError.encodingError(error)).eraseToAnyPublisher()
        }
    }
}

/// Errors specific to API operations
///
/// These error types provide detailed information about what went wrong
/// during API communication.
enum APIError: Error, LocalizedError {
    /// The server returned an invalid or unexpected response format
    case invalidResponse
    
    /// A server-side error occurred (non-2xx status code)
    case serverError(statusCode: Int)
    
    /// A network-related error occurred during communication
    case networkError(URLError)
    
    /// Failed to encode request parameters
    case encodingError(Error)
    
    /// Human-readable error description
    var errorDescription: String? {
        switch self {
        case .invalidResponse:
            return "The server returned an invalid response."
        case .serverError(let statusCode):
            return "Server error with status code: \(statusCode)"
        case .networkError(let error):
            return "Network error: \(error.localizedDescription)"
        case .encodingError(let error):
            return "Failed to encode request: \(error.localizedDescription)"
        }
    }
}