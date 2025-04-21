<?php
/**
 * GPS Tracker Application - Main Entry Point
 *
 * This is the main entry point for the GPS Tracker web application. It initializes
 * the application, sets up routes, and handles requests.
 *
 * The application follows a simple structure:
 * - Routes are registered for both API and frontend
 * - Rate limiting is applied to prevent abuse
 * - Errors are handled globally
 *
 * @package    GpsTracker
 * @subpackage Server
 * @author     Original: Nick Fox
 * @license    MIT License
 * @version    2.0
 */

// Autoloader
require_once __DIR__ . '/vendor/autoload.php';

// Bootstrap application
require_once __DIR__ . '/src/bootstrap.php';

use App\Router;
use App\Controllers\RouteController;
use App\Controllers\LocationController;
use App\Middleware\RateLimiter;
use App\Utils\Logger;

// Initialize rate limiter
RateLimiter::init();

// Application entry point
try {
    // Check rate limiting
    if (!RateLimiter::check()) {
        header('HTTP/1.1 429 Too Many Requests');
        header('Content-Type: application/json');
        echo json_encode([
            'error' => 'Too Many Requests',
            'message' => 'API rate limit exceeded. Please try again later.',
        ]);
        exit;
    }
    
    // Log the request
    Logger::info('Request received', [
        'method' => $_SERVER['REQUEST_METHOD'] ?? 'unknown',
        'uri' => $_SERVER['REQUEST_URI'] ?? 'unknown',
        'ip' => $_SERVER['REMOTE_ADDR'] ?? 'unknown',
        'user_agent' => $_SERVER['HTTP_USER_AGENT'] ?? 'unknown',
    ]);
    
    // Create router
    $router = new Router();
    
    // Register controllers
    $routeController = new RouteController();
    $locationController = new LocationController();
    
    // API routes - register both with and without leading slash
    // With leading slash
    $router->get('/api/routes', function ($params) use ($routeController) {
        header('Content-Type: application/json');
        return $routeController->getRoutes();
    });
    
    $router->get('/api/routes/all', function ($params) use ($routeController) {
        header('Content-Type: application/json');
        return $routeController->getAllRoutesForMap();
    });
    
    $router->get('/api/routes/detail', function ($params) use ($routeController) {
        header('Content-Type: application/json');
        return $routeController->getRouteForMap($params);
    });
    
    $router->post('/api/routes/delete', function ($params) use ($routeController) {
        header('Content-Type: application/json');
        return $routeController->deleteRoute($params);
    });
    
    $router->post('/api/locations/update', function ($params) use ($locationController) {
        header('Content-Type: application/json');
        return $locationController->updateLocation($params);
    });
    
    // Without leading slash (for relative URLs in JavaScript)
    $router->get('api/routes', function ($params) use ($routeController) {
        header('Content-Type: application/json');
        return $routeController->getRoutes();
    });
    
    $router->get('api/routes/all', function ($params) use ($routeController) {
        header('Content-Type: application/json');
        return $routeController->getAllRoutesForMap();
    });
    
    $router->get('api/routes/detail', function ($params) use ($routeController) {
        header('Content-Type: application/json');
        return $routeController->getRouteForMap($params);
    });
    
    $router->post('api/routes/delete', function ($params) use ($routeController) {
        header('Content-Type: application/json');
        return $routeController->deleteRoute($params);
    });
    
    $router->post('api/locations/update', function ($params) use ($locationController) {
        header('Content-Type: application/json');
        return $locationController->updateLocation($params);
    });
    
    // Frontend routes
    $router->get('/map', function () {
        include __DIR__ . '/templates/map.php';
        return '';
    });
    
    $router->get('/map/dark', function () {
        $theme = 'dark';
        include __DIR__ . '/templates/map.php';
        return '';
    });
    
    $router->get('/map/blue', function () {
        $theme = 'blue';
        include __DIR__ . '/templates/map.php';
        return '';
    });
    
    // Asset serving
    $router->get('/assets/*', function ($params) {
        $path = parse_url($_SERVER['REQUEST_URI'] ?? '/', PHP_URL_PATH);
        $filePath = __DIR__ . $path;
        
        if (file_exists($filePath)) {
            // Determine content type
            $extension = pathinfo($filePath, PATHINFO_EXTENSION);
            $contentType = match ($extension) {
                'css' => 'text/css',
                'js' => 'application/javascript',
                'jpg', 'jpeg' => 'image/jpeg',
                'png' => 'image/png',
                'gif' => 'image/gif',
                'svg' => 'image/svg+xml',
                default => 'application/octet-stream',
            };
            
            // Set headers
            header('Content-Type: ' . $contentType);
            header('Cache-Control: max-age=86400'); // 1 day cache
            
            // Output file
            readfile($filePath);
            return '';
        }
        
        header('HTTP/1.1 404 Not Found');
        return '404 File Not Found';
    });
    
    // Special handler for direct access to index.php
    $reqPath = parse_url($_SERVER['REQUEST_URI'] ?? '/', PHP_URL_PATH);
    if (strpos($reqPath, '/index.php') !== false) {
        // Redirect index.php to root
        $router->get('/index.php', function () {
            return header('Location: .');
        });
    }
    
    // Default route
    $router->get('/', function () {
        return header('Location: map');
    });
    
    // No legacy routes - clean modern API only
    
    // 404 fallback (customize error message)
    $router->get('*', function () {
        header('HTTP/1.1 404 Not Found');
        header('Content-Type: application/json');
        
        // Get current path for better error message
        $path = parse_url($_SERVER['REQUEST_URI'] ?? '/', PHP_URL_PATH);
        
        return json_encode([
            'error' => 'Not Found',
            'message' => "The requested resource '{$path}' could not be found",
            'note' => 'Please check that you are using the correct API endpoint path'
        ]);
    });
    
    // Dispatch request
    echo $router->dispatch();
} catch (\Throwable $e) {
    // Log the error
    Logger::error('Unhandled exception', [
        'message' => $e->getMessage(),
        'file' => $e->getFile(),
        'line' => $e->getLine(),
        'trace' => $e->getTraceAsString(),
    ]);
    
    // Return error response
    header('HTTP/1.1 500 Internal Server Error');
    header('Content-Type: application/json');
    
    if (config('app.debug', false)) {
        echo json_encode([
            'error' => 'Internal Server Error',
            'message' => $e->getMessage(),
            'file' => $e->getFile(),
            'line' => $e->getLine(),
            'trace' => explode("\n", $e->getTraceAsString()),
        ]);
    } else {
        echo json_encode([
            'error' => 'Internal Server Error',
            'message' => 'An unexpected error occurred. Please try again later.',
        ]);
    }
}
