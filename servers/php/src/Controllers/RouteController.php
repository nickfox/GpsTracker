<?php

namespace App\Controllers;

use App\Services\RouteRepository;
use App\Utils\Logger;

/**
 * Route Controller
 * 
 * This controller handles all operations related to GPS routes,
 * including retrieving, displaying, and deleting routes.
 * 
 * It serves as the interface between the API endpoints and the
 * route data repository, processing requests and formatting responses.
 * 
 * Features:
 * - List all available routes
 * - Retrieve location data for specific routes
 * - Retrieve all routes for map display
 * - Delete routes with validation
 * - Consistent JSON response formatting
 * - Detailed error logging
 * 
 * @package App\Controllers
 */
class RouteController
{
    /**
     * Route repository for data operations
     * 
     * @var RouteRepository Handles database operations for routes
     */
    private RouteRepository $routeRepository;
    
    /**
     * Create a new RouteController
     * 
     * Initializes the controller with an optional repository dependency.
     * If no repository is provided, a new one will be created.
     * 
     * @param RouteRepository|null $routeRepository Optional repository dependency
     */
    public function __construct(RouteRepository $routeRepository = null)
    {
        $this->routeRepository = $routeRepository ?? new RouteRepository();
    }
    
    /**
     * Get all routes
     * 
     * Retrieves a list of all available routes from the repository
     * for display in the route selection dropdown.
     * 
     * @return string JSON response with an array of route information
     */
    public function getRoutes(): string
    {
        try {
            $routes = $this->routeRepository->getRoutes();
            
            // Format response
            $json = ['routes' => $routes];
            
            Logger::info('Routes retrieved successfully', [
                'count' => count($routes),
            ]);
            
            return $this->jsonResponse($json);
        } catch (\Exception $e) {
            Logger::error('Error in getRoutes', [
                'error' => $e->getMessage(),
                'trace' => $e->getTraceAsString(),
            ]);
            
            return $this->jsonResponse(['routes' => []]);
        }
    }
    
    /**
     * Get route for map display
     * 
     * Retrieves all location points for a specific route identified
     * by its session ID to display on the map.
     * 
     * @param array $params Request parameters including 'sessionid'
     * @return string JSON response with location data for the route
     */
    public function getRouteForMap(array $params): string
    {
        try {
            $sessionId = $params['sessionid'] ?? '0';
            
            if (empty($sessionId) || $sessionId === '0') {
                Logger::warning('Invalid session ID for getRouteForMap', [
                    'sessionId' => $sessionId,
                ]);
                
                return $this->jsonResponse(['locations' => []]);
            }
            
            $locations = $this->routeRepository->getRouteForMap($sessionId);
            
            // Debug log the response
            Logger::info('Route retrieved for map', [
                'sessionId' => $sessionId,
                'locationCount' => count($locations),
                'sampleLocation' => !empty($locations) ? json_encode($locations[0]) : 'No locations'
            ]);
            
            // Return response
            return $this->jsonResponse(['locations' => $locations]);
        } catch (\Exception $e) {
            Logger::error('Error in getRouteForMap', [
                'error' => $e->getMessage(),
                'sessionId' => $params['sessionid'] ?? 'unknown',
                'trace' => $e->getTraceAsString(),
            ]);
            
            return $this->jsonResponse(['locations' => []]);
        }
    }
    
    /**
     * Get all routes for map display
     * 
     * Retrieves the most recent location for each route
     * to display as markers on the map overview.
     * 
     * @return string JSON response with location data for all routes
     */
    public function getAllRoutesForMap(): string
    {
        try {
            $locations = $this->routeRepository->getAllRoutesForMap();
            
            // Debug log the response
            Logger::info('All routes retrieved for map', [
                'locationCount' => count($locations),
                'sampleLocation' => !empty($locations) ? json_encode($locations[0]) : 'No locations'
            ]);
            
            return $this->jsonResponse(['locations' => $locations]);
        } catch (\Exception $e) {
            Logger::error('Error in getAllRoutesForMap', [
                'error' => $e->getMessage(),
                'trace' => $e->getTraceAsString(),
            ]);
            
            return $this->jsonResponse(['locations' => []]);
        }
    }
    
    /**
     * Delete a route
     * 
     * Permanently removes a route and all its associated location
     * points from the database.
     * 
     * @param array $params Request parameters including 'sessionid'
     * @return string JSON response indicating success or failure
     */
    public function deleteRoute(array $params): string
    {
        try {
            error_log("!!! RouteController::deleteRoute entered. Params: " . json_encode($params)); // Moved inside try
            $sessionId = $params['sessionid'] ?? '0';
            
            // Log the received session ID before validation
            Logger::info('Attempting to delete route', ['receivedSessionId' => $sessionId]);

            if (empty($sessionId) || $sessionId === '0') {
                Logger::warning('Invalid session ID provided for deleteRoute', [
                    'sessionId' => $sessionId,
                ]);
                
                // Return JSON error response
                http_response_code(400); // Bad Request
                return $this->jsonResponse(['success' => false, 'message' => 'Invalid session ID']);
            }
            
            $success = $this->routeRepository->deleteRoute($sessionId);
            
            if ($success) {
                Logger::info('Route deleted successfully', [
                    'sessionId' => $sessionId,
                ]);
                
                // Return JSON success response
                return $this->jsonResponse(['success' => true, 'message' => 'Route deleted successfully']);
            } else {
                Logger::warning('Failed to delete route', [
                    'sessionId' => $sessionId,
                ]);
                
                // Return JSON error response
                http_response_code(500); // Internal Server Error
                return $this->jsonResponse(['success' => false, 'message' => 'Failed to delete route']);
            }
        } catch (\Exception $e) {
            Logger::error('Error in deleteRoute', [
                'error' => $e->getMessage(),
                'sessionId' => $params['sessionid'] ?? 'unknown',
                'trace' => $e->getTraceAsString(),
            ]);
            
            // Return JSON error response
            http_response_code(500); // Internal Server Error
            return $this->jsonResponse(['success' => false, 'message' => 'Error deleting route: ' . $e->getMessage()]);
        }
    }
    
    /**
     * Format JSON response
     * 
     * Encodes data as JSON with Unicode character support.
     * Used to ensure consistent response formatting across all methods.
     * 
     * @param array $data Response data to be encoded as JSON
     * @return string JSON-encoded string
     */
    private function jsonResponse(array $data): string
    {
        return json_encode($data, JSON_UNESCAPED_UNICODE);
    }
}
