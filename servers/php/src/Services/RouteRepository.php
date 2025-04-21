<?php

namespace App\Services;

use PDOException;
use App\Models\GPSLocation;
use App\Utils\Logger;

/**
 * Route Repository
 * 
 * This service manages database operations related to routes and GPS locations.
 * It provides methods to retrieve, search, and delete route data for the
 * map display and route management features.
 * 
 * Features:
 * - Retrieve route lists for display in the UI
 * - Get detailed location data for map visualization
 * - Support for both single route and all-routes views
 * - Route deletion with transaction support
 * - Database driver abstraction (works with MySQL, SQLite)
 * - Detailed error logging
 * 
 * @package App\Services
 */
class RouteRepository
{
    /**
     * Get all routes for display
     * 
     * Retrieves a list of all routes for display in the route selection dropdown.
     * Each route includes basic information like username, session ID, and time range.
     * 
     * @return array Array of route data for display
     */
    public function getRoutes(): array
    {
        try {
            $driver = config('database.driver', 'sqlite');
            
            if ($driver === 'mysql') {
                $sql = Database::getSqlFunctionCallMethod() . 'prcGetRoutes()';
            } else {
                $sql = 'SELECT * FROM v_GetRoutes';
            }
            
            $results = Database::query($sql);
            
            $routes = [];
            foreach ($results as $row) {
                $jsonString = $row['json'] ?? null;
                if ($jsonString) {
                    $routeData = json_decode($jsonString, true);
                    if ($routeData) {
                        $routes[] = $routeData;
                    }
                }
            }
            
            return $routes;
        } catch (PDOException $e) {
            Logger::error('Failed to get routes', ['error' => $e->getMessage()]);
            return [];
        }
    }
    
    /**
     * Get route data for a specific session ID
     * 
     * Retrieves all GPS locations for a specific route identified by session ID.
     * The locations are ordered chronologically for proper display on the map.
     * 
     * @param string $sessionId Session ID to get route for
     * @return array Array of location data formatted for map display
     */
    public function getRouteForMap(string $sessionId): array
    {
        try {
            $driver = config('database.driver', 'sqlite');
            $tablePrefix = config('database.prefix', ''); // Get prefix from config
            
            // Debug logging
            Logger::debug('Getting route for map', [
                'sessionId' => $sessionId,
                'driver' => $driver,
                'tablePrefix' => $tablePrefix
            ]);
            
            if ($driver === 'mysql') {
                // Try direct query instead of stored procedure for debugging
                $sql = "SELECT 
                        GPSLocationID,
                        lastUpdate,
                        latitude,
                        longitude,
                        phoneNumber,
                        userName,
                        sessionID,
                        speed,
                        direction,
                        distance,
                        gpsTime,
                        locationMethod,
                        accuracy,
                        extraInfo,
                        eventType
                    FROM {$tablePrefix}gpslocations 
                    WHERE sessionID = :sessionID
                    ORDER BY gpsTime";
            } else {
                $sql = 'SELECT * FROM v_GetRouteForMap WHERE sessionID = :sessionID';
            }
            
            $results = Database::query($sql, [':sessionID' => $sessionId]);
            
            // Debug logging
            Logger::debug('Query results', [
                'count' => count($results),
                'sample' => !empty($results) ? json_encode($results[0]) : 'No results'
            ]);
            
            $locations = [];
            
            // Direct conversion without expecting JSON field
            foreach ($results as $row) {
                $locations[] = [
                    'latitude' => (string)($row['latitude'] ?? '0'),
                    'longitude' => (string)($row['longitude'] ?? '0'),
                    'speed' => (string)($row['speed'] ?? '0'),
                    'direction' => (string)($row['direction'] ?? '0'),
                    'distance' => (string)($row['distance'] ?? '0'),
                    'locationMethod' => $row['locationMethod'] ?? 'na',
                    'gpsTime' => $row['gpsTime'] ?? date('Y-m-d H:i:s'),
                    'userName' => $row['userName'] ?? '',
                    'phoneNumber' => $row['phoneNumber'] ?? '',
                    'sessionID' => $row['sessionID'] ?? $sessionId,
                    'accuracy' => (string)($row['accuracy'] ?? '0'),
                    'extraInfo' => $row['extraInfo'] ?? 'na'
                ];
            }
            
            Logger::debug('Processed locations', [
                'count' => count($locations),
                'sample' => !empty($locations) ? json_encode($locations[0]) : 'No locations'
            ]);
            
            return $locations;
        } catch (PDOException $e) {
            Logger::error('Failed to get route for map', [
                'sessionId' => $sessionId,
                'error' => $e->getMessage(),
                'trace' => $e->getTraceAsString()
            ]);
            return [];
        }
    }
    
    /**
     * Get all routes for map display
     * 
     * Retrieves the latest GPS location for each route for display on the map overview.
     * This provides a summary view of where each route's device was last located.
     * 
     * @return array Array of location data for the last position of each route
     */
    public function getAllRoutesForMap(): array
    {
        try {
            $driver = config('database.driver', 'sqlite');
            $tablePrefix = config('database.prefix', ''); // Get prefix from config
            
            // Debug logging
            Logger::debug('Getting all routes for map', [
                'driver' => $driver,
                'tablePrefix' => $tablePrefix
            ]);
            
            if ($driver === 'mysql') {
                // Use a direct query that gets the latest location for each sessionID
                $sql = "SELECT 
                        a.GPSLocationID,
                        a.lastUpdate,
                        a.latitude,
                        a.longitude,
                        a.phoneNumber,
                        a.userName,
                        a.sessionID,
                        a.speed,
                        a.direction,
                        a.distance,
                        a.gpsTime,
                        a.locationMethod,
                        a.accuracy,
                        a.extraInfo,
                        a.eventType
                    FROM {$tablePrefix}gpslocations a
                    INNER JOIN (
                        SELECT sessionID, MAX(gpsTime) as maxTime
                        FROM {$tablePrefix}gpslocations
                        GROUP BY sessionID
                    ) b ON a.sessionID = b.sessionID AND a.gpsTime = b.maxTime
                    ORDER BY a.gpsTime DESC";
            } else {
                $sql = 'SELECT * FROM v_GetAllRoutesForMap';
            }
            
            $results = Database::query($sql);
            
            // Debug logging
            Logger::debug('Query results', [
                'count' => count($results),
                'sample' => !empty($results) ? json_encode($results[0]) : 'No results'
            ]);
            
            $locations = [];
            
            // Direct conversion without expecting JSON field
            foreach ($results as $row) {
                $locations[] = [
                    'latitude' => (string)($row['latitude'] ?? '0'),
                    'longitude' => (string)($row['longitude'] ?? '0'),
                    'speed' => (string)($row['speed'] ?? '0'),
                    'direction' => (string)($row['direction'] ?? '0'),
                    'distance' => (string)($row['distance'] ?? '0'),
                    'locationMethod' => $row['locationMethod'] ?? 'na',
                    'gpsTime' => $row['gpsTime'] ?? date('Y-m-d H:i:s'),
                    'userName' => $row['userName'] ?? '',
                    'phoneNumber' => $row['phoneNumber'] ?? '',
                    'sessionID' => $row['sessionID'] ?? '',
                    'accuracy' => (string)($row['accuracy'] ?? '0'),
                    'extraInfo' => $row['extraInfo'] ?? 'na'
                ];
            }
            
            Logger::debug('Processed locations', [
                'count' => count($locations),
                'sample' => !empty($locations) ? json_encode($locations[0]) : 'No locations'
            ]);
            
            return $locations;
        } catch (PDOException $e) {
            Logger::error('Failed to get all routes for map', [
                'error' => $e->getMessage(),
                'trace' => $e->getTraceAsString()
            ]);
            return [];
        }
    }
    
    /**
     * Delete a route
     * 
     * Permanently removes a route and all its GPS locations from the database.
     * The operation is wrapped in a transaction to ensure data integrity.
     * 
     * @param string $sessionId Session ID of the route to delete
     * @return bool True if successful deletion occurred, false if no records found or error
     */
    public function deleteRoute(string $sessionId): bool
    {
        try {
            Database::beginTransaction();
            
            $tablePrefix = config('database.prefix', ''); // Get prefix from config
            $sql = "DELETE FROM {$tablePrefix}gpslocations WHERE sessionID = :sessionID";
            $count = Database::delete($sql, [':sessionID' => $sessionId]);
            
            Database::commit();
            
            if ($count > 0) {
                Logger::info('Route deleted successfully from database', [
                    'sessionId' => $sessionId,
                    'recordsDeleted' => $count,
                ]);
                return true;
            } else {
                // Log specifically when no records were found/deleted for the given sessionID
                Logger::warning('Delete command executed, but no route found/deleted in database for sessionID', [
                    'sessionId' => $sessionId,
                    'recordsDeleted' => $count, // Will be 0
                ]);
                return false; // Indicate failure as no rows were deleted
            }
        } catch (PDOException $e) {
            Database::rollback();
            
            Logger::error('Failed to delete route', [
                'sessionId' => $sessionId,
                'error' => $e->getMessage(),
            ]);
            
            return false;
        }
    }
    
    /**
     * Get locations for a route as model objects
     * 
     * Retrieves all locations for a specific route as GPSLocation model objects,
     * allowing for more advanced manipulation than the map display format.
     * 
     * @param string $sessionId Session ID to get locations for
     * @return array Array of GPSLocation objects
     */
    public function getLocations(string $sessionId): array
    {
        try {
            $tablePrefix = config('database.prefix', ''); // Get prefix from config
            $sql = "SELECT * FROM {$tablePrefix}gpslocations WHERE sessionID = :sessionID ORDER BY gpsTime ASC";
            $results = Database::query($sql, [':sessionID' => $sessionId]);
            
            $locations = [];
            foreach ($results as $row) {
                $locations[] = GPSLocation::fromArray($row);
            }
            
            return $locations;
        } catch (PDOException $e) {
            Logger::error('Failed to get locations for route', [
                'sessionId' => $sessionId,
                'error' => $e->getMessage(),
            ]);
            
            return [];
        }
    }
}
