<?php

namespace App;

use App\Utils\Logger;

/**
 * Modern router for handling API requests with clean URLs
 * 
 * This router class implements a simple but powerful routing system for the GPS Tracker
 * application. It supports RESTful routes, path matching with wildcards, and different
 * HTTP methods.
 * 
 * Features:
 * - Fluent interface for registering routes
 * - Support for GET and POST HTTP methods
 * - Path normalization for consistent matching
 * - Wildcard route support (e.g., /assets/*)
 * - Automatic handling of subdirectory deployments
 * - Error handling with detailed logging
 * 
 * @package App
 */
class Router
{
    /**
     * Registered routes collection
     * 
     * Array of associative arrays, each containing:
     * - 'method': HTTP method (GET, POST, etc.)
     * - 'path': URL path to match
     * - 'handler': Callable to execute when route is matched
     * 
     * @var array Registered routes
     */
    private array $routes = [];
    
    /**
     * Base path for the application
     * 
     * Used when the app is deployed in a subdirectory
     * 
     * @var string Base path for the application
     */
    private string $basePath = '';
    
    /**
     * Constructor 
     * 
     * Creates a new Router instance with an optional base path
     * 
     * @param string $basePath Base path for the application (default: '')
     */
    public function __construct(string $basePath = '')
    {
        $this->basePath = $basePath;
    }
    
    /**
     * Register a route with any HTTP method
     * 
     * @param string $method HTTP method (GET, POST, etc.)
     * @param string $path URL path to match
     * @param callable $handler Route handler function that receives parameters and returns a response
     * @return self Returns the router instance for method chaining
     */
    public function addRoute(string $method, string $path, callable $handler): self
    {
        $this->routes[] = [
            'method' => strtoupper($method),
            'path' => $path,
            'handler' => $handler,
        ];
        
        return $this;
    }
    
    /**
     * Register a GET route
     * 
     * Shorthand method for registering routes that respond to GET requests
     * 
     * @param string $path URL path to match
     * @param callable $handler Route handler function
     * @return self Returns the router instance for method chaining
     */
    public function get(string $path, callable $handler): self
    {
        return $this->addRoute('GET', $path, $handler);
    }
    
    /**
     * Register a POST route
     * 
     * Shorthand method for registering routes that respond to POST requests
     * 
     * @param string $path URL path to match
     * @param callable $handler Route handler function
     * @return self Returns the router instance for method chaining
     */
    public function post(string $path, callable $handler): self
    {
        return $this->addRoute('POST', $path, $handler);
    }
    
    /**
     * Dispatch the request to the appropriate handler
     * 
     * This method:
     * 1. Determines the HTTP method and path
     * 2. Normalizes the path (removes query string, handles subdirectory)
     * 3. Matches against registered routes
     * 4. Executes the handler of the first matching route
     * 5. Returns the response from the handler
     * 
     * @param string|null $method HTTP method (defaults to $_SERVER['REQUEST_METHOD'])
     * @param string|null $path URL path (defaults to $_SERVER['REQUEST_URI'])
     * @return mixed Response from the handler
     */
    public function dispatch(?string $method = null, ?string $path = null)
    {
        $method = $method ?? ($_SERVER['REQUEST_METHOD'] ?? 'GET');
        $path = $path ?? parse_url($_SERVER['REQUEST_URI'] ?? '/', PHP_URL_PATH);
        
        // Remove query string from path if present
        if (strpos($path, '?') !== false) {
            $path = strstr($path, '?', true);
        }
        
        // Handle subdirectory deployment - strip the /gpstracker prefix
        if (strpos($path, '/gpstracker') === 0) {
            $path = substr($path, strlen('/gpstracker'));
        }
        
        // Remove index.php from the path if present
        if (strpos($path, '/index.php') === 0) {
            $path = '/';
        }
        
        // Normalize path
        $path = rtrim($path, '/');
        if (empty($path)) {
            $path = '/';
        }
        
        // Debug logging
        error_log("Original URI: " . $_SERVER['REQUEST_URI'] . ", Adjusted path: " . $path);
        
        Logger::debug('Routing request', [
            'method' => $method,
            'original_path' => $_SERVER['REQUEST_URI'] ?? '/',
            'adjusted_path' => $path
        ]);
        
        // Find matching route
        foreach ($this->routes as $route) {
            // Check method
            if ($route['method'] !== $method) {
                continue;
            }
            
            // For debugging
            error_log("Checking route: {$route['path']} against path: {$path}");
            
            // Check for exact path match
            if ($route['path'] === $path) {
                Logger::debug('Route matched', [
                    'method' => $method,
                    'path' => $path,
                    'route' => $route['path']
                ]);
                
                return $this->executeHandler($route['handler']);
            }
            
            // Handle routes that start with /api/
            if (strpos($path, '/api') === 0 && $route['path'] === substr($path, 1)) {
                Logger::debug('API route matched with leading slash', [
                    'method' => $method,
                    'path' => $path,
                    'route' => $route['path']
                ]);
                
                return $this->executeHandler($route['handler']);
            }
            
            // Check for wildcard match (e.g. /api/* matches /api/users)
            if (strpos($route['path'], '*') !== false) {
                $pattern = str_replace('*', '(.*)', $route['path']);
                $pattern = '#^' . $pattern . '$#';
                
                if (preg_match($pattern, $path)) {
                    Logger::debug('Wildcard route matched', [
                        'method' => $method,
                        'path' => $path,
                        'pattern' => $pattern,
                        'route' => $route['path']
                    ]);
                    
                    return $this->executeHandler($route['handler']);
                }
            }
        }
        
        // No route found
        Logger::warning('No route found', [
            'method' => $method,
            'path' => $path,
            'available_routes' => array_column($this->routes, 'path')
        ]);
        
        return $this->notFound();
    }
    
    /**
     * Execute the route handler with parameters
     * 
     * This method:
     * 1. Merges GET and POST parameters
     * 2. Executes the handler with the parameters
     * 3. Catches and logs any errors that occur
     * 
     * @param callable $handler Route handler function
     * @return mixed Response from the handler
     */
    private function executeHandler(callable $handler)
    {
        try {
            // Merge GET and POST parameters
            $params = array_merge($_GET ?? [], $_POST ?? []);
            
            // Execute handler with parameters
            return $handler($params);
        } catch (\Throwable $e) {
            Logger::error('Error executing route handler', [
                'error' => $e->getMessage(),
                'trace' => $e->getTraceAsString(),
            ]);
            
            return $this->serverError($e);
        }
    }
    
    /**
     * Handle 404 Not Found responses
     * 
     * Generates a standard 404 response when no matching route is found
     * 
     * @return string JSON-encoded error response
     */
    private function notFound(): string
    {
        http_response_code(404);
        header('Content-Type: application/json');
        
        return json_encode([
            'error' => 'Not Found',
            'message' => 'The requested resource was not found',
        ]);
    }
    
    /**
     * Handle 500 Server Error responses
     * 
     * Generates a standard 500 response with optional details in debug mode
     * 
     * @param \Throwable $e Exception or error that occurred
     * @return string JSON-encoded error response
     */
    private function serverError(\Throwable $e): string
    {
        http_response_code(500);
        header('Content-Type: application/json');
        
        if (config('app.debug', false)) {
            return json_encode([
                'error' => 'Server Error',
                'message' => $e->getMessage(),
                'file' => $e->getFile(),
                'line' => $e->getLine(),
                'trace' => explode("\n", $e->getTraceAsString()),
            ]);
        } else {
            return json_encode([
                'error' => 'Server Error',
                'message' => 'An unexpected error occurred',
            ]);
        }
    }
}
