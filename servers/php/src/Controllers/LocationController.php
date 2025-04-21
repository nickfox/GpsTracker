<?php

namespace App\Controllers;

use App\Services\LocationService;
use App\Utils\Logger;

/**
 * Controller for location operations
 * 
 * This controller handles all operations related to updating GPS locations
 * from tracking devices. It validates input data, sanitizes parameters,
 * and interacts with the LocationService to save location data.
 * 
 * @package App\Controllers
 */
class LocationController
{
    /**
     * Location service instance for data operations
     * 
     * @var LocationService Handles the business logic for location operations
     */
    private LocationService $locationService;
    
    /**
     * Create a new LocationController
     * 
     * Initializes a new controller with an optional LocationService instance.
     * If no service is provided, a new one will be created.
     * 
     * @param LocationService|null $locationService Optional service dependency
     */
    public function __construct(LocationService $locationService = null)
    {
        $this->locationService = $locationService ?? new LocationService();
    }
    
    /**
     * Handle location update request
     * 
     * Processes incoming location updates from tracking devices.
     * The method:
     * 1. Sanitizes and validates the input parameters
     * 2. Calls the LocationService to update the location
     * 3. Returns an appropriate JSON response
     * 
     * @param array $params Request parameters containing location data
     * @return string JSON response indicating success or failure
     */
    public function updateLocation(array $params): string
    {
        try {
            // Sanitize and validate input parameters
            $data = $this->sanitizeInput($params);
            
            // Update location
            $success = $this->locationService->updateLocation($data);
            
            if ($success) {
                // Success
                Logger::info('Location updated successfully', [
                    'username' => $data['username'] ?? '',
                    'sessionid' => $data['sessionid'] ?? '',
                ]);
                
                http_response_code(200);
                return json_encode(['status' => 'success']);
            } else {
                // Failed validation or database error
                Logger::warning('Location update failed', [
                    'data' => $data,
                ]);
                
                http_response_code(400);
                return json_encode(['status' => 'error', 'message' => 'Failed to update location']);
            }
        } catch (\Exception $e) {
            Logger::error('Error in updateLocation', [
                'error' => $e->getMessage(),
                'trace' => $e->getTraceAsString(),
            ]);
            
            http_response_code(500);
            return json_encode(['status' => 'error', 'message' => 'Server error: ' . $e->getMessage()]);
        }
    }

    /**
     * Sanitize and validate input parameters
     * 
     * Processes raw input data to ensure it is safe and properly formatted.
     * Handles:
     * - Coordinate data
     * - Numeric values
     * - Date/time formatting
     * - String parameters with URL decoding where needed
     * 
     * @param array $params Raw input parameters from the request
     * @return array Sanitized parameters ready for processing
     */
    private function sanitizeInput(array $params): array
    {
        $sanitized = [];
        
        // Sanitize and validate each parameter
        $sanitized['latitude'] = isset($params['latitude']) ? $params['latitude'] : '0';
        $sanitized['longitude'] = isset($params['longitude']) ? $params['longitude'] : '0';
        $sanitized['speed'] = isset($params['speed']) ? (int)$params['speed'] : 0;
        $sanitized['direction'] = isset($params['direction']) ? (int)$params['direction'] : 0;
        $sanitized['distance'] = isset($params['distance']) ? $params['distance'] : '0';
        
        // Date/time handling with basic validation
        $sanitized['date'] = isset($params['date']) ? $params['date'] : '0000-00-00 00:00:00';
        $sanitized['date'] = urldecode($sanitized['date']);
        
        // If date is invalid, use current date/time
        if (!$sanitized['date'] || $sanitized['date'] === '0000-00-00 00:00:00' || !strtotime($sanitized['date'])) {
            $sanitized['date'] = date('Y-m-d H:i:s');
        }
        
        // String parameters
        $sanitized['locationmethod'] = isset($params['locationmethod']) ? trim(urldecode($params['locationmethod'])) : '';
        $sanitized['username'] = isset($params['username']) ? trim($params['username']) : '';
        $sanitized['phonenumber'] = isset($params['phonenumber']) ? trim($params['phonenumber']) : '';
        $sanitized['sessionid'] = isset($params['sessionid']) ? trim($params['sessionid']) : '';
        $sanitized['accuracy'] = isset($params['accuracy']) ? (int)$params['accuracy'] : 0;
        $sanitized['extrainfo'] = isset($params['extrainfo']) ? trim($params['extrainfo']) : '';
        $sanitized['eventtype'] = isset($params['eventtype']) ? trim($params['eventtype']) : '';
        
        return $sanitized;
    }
}
