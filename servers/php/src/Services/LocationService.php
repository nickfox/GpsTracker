<?php

namespace App\Services;

use PDOException;
use App\Models\GPSLocation;
use App\Utils\Logger;

/**
 * Service for location operations
 * 
 * This service class implements the business logic for GPS location operations.
 * It handles validation, conversion, and persistence of location data.
 * 
 * The class serves as an intermediary between controllers and the data model,
 * ensuring separation of concerns and proper validation before database operations.
 * 
 * @package App\Services
 */
class LocationService
{
    /**
     * Update a location
     *
     * Processes location data from tracking devices and saves it to the database.
     * The method:
     * 1. Validates the input coordinates
     * 2. Creates a GPSLocation object
     * 3. Validates the location data
     * 4. Saves the location to the database
     *
     * @param array $data Location data from the request
     * @return bool True on successful update, false on validation failure or database error
     */
    public function updateLocation(array $data): bool
    {
        try {
            // Validate input data
            $latitude = $this->validateCoordinate($data['latitude'] ?? 0);
            $longitude = $this->validateCoordinate($data['longitude'] ?? 0);

            // Basic validation - if both are 0, it's likely an invalid location
            if ($latitude === 0.0 && $longitude === 0.0) {
                Logger::warning('Invalid location coordinates', [
                    'latitude' => $latitude,
                    'longitude' => $longitude,
                ]);
                return false;
            }

            // Create location object
            $location = new GPSLocation(
                $latitude,
                $longitude,
                (int)($data['speed'] ?? 0),
                (int)($data['direction'] ?? 0),
                (float)($data['distance'] ?? 0),
                $data['date'] ?? '',
                $data['locationmethod'] ?? '',
                $data['username'] ?? '',
                $data['phonenumber'] ?? '',
                $data['sessionid'] ?? '',
                (int)($data['accuracy'] ?? 0),
                $data['extrainfo'] ?? '',
                $data['eventtype'] ?? ''
            );

            // Validate the location
            $errors = $location->validate();
            if (!empty($errors)) {
                Logger::warning('Location validation failed', [
                    'errors' => $errors,
                    'data' => $data,
                ]);
                return false;
            }

            // Save the location
            return $location->save();
        } catch (\Exception $e) {
            Logger::error('Failed to update location', [
                'error' => $e->getMessage(),
                'data' => $data,
            ]);

            return false;
        }
    }
    
    /**
     * Validate and normalize a coordinate value
     * 
     * Converts various input formats to a standardized float value.
     * Handles both string and numeric inputs, and supports European
     * locale formats with comma as decimal separator.
     * 
     * @param mixed $value The coordinate value to validate (string or numeric)
     * @return float Validated and normalized coordinate value
     */
    private function validateCoordinate($value): float
    {
        // Handle various input formats
        if (is_string($value)) {
            // Replace comma with dot for European locale support
            $value = str_replace(',', '.', $value);
        }
        
        return (float)$value;
    }
    
    /**
     * Get a location by ID
     * 
     * Retrieves a specific location record from the database by its primary key.
     * 
     * @param int $id Location ID to retrieve
     * @return GPSLocation|null The location object if found, or null if not found
     */
    public function getLocationById(int $id): ?GPSLocation
    {
        return GPSLocation::getById($id);
    }
    
    /**
     * Get the last location for a session
     * 
     * Retrieves the most recent location for a specific tracking session.
     * This is useful for showing the current position of a device.
     * 
     * @param string $sessionId Session ID to retrieve the last location for
     * @return GPSLocation|null The last location object if found, or null if not found
     */
    public function getLastLocationForSession(string $sessionId): ?GPSLocation
    {
        try {
            $sql = 'SELECT * FROM gpslocations WHERE sessionID = :sessionID ORDER BY gpsTime DESC LIMIT 1';
            $result = Database::queryOne($sql, [':sessionID' => $sessionId]);
            
            if ($result) {
                return GPSLocation::fromArray($result);
            }
            
            return null;
        } catch (PDOException $e) {
            Logger::error('Failed to get last location for session', [
                'sessionId' => $sessionId,
                'error' => $e->getMessage(),
            ]);
            
            return null;
        }
    }
}
