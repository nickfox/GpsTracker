<?php
/**
 * GPS Tracker Application - Bootstrap
 * 
 * This file initializes the core components of the application:
 * - Loads environment variables from .env
 * - Sets up the configuration system
 * - Configures error handling and logging
 * - Sets PHP environment (timezone, error reporting)
 * 
 * The bootstrap process ensures the application is properly
 * initialized before any request processing occurs.
 * 
 * @package    GpsTracker
 * @subpackage Core
 * @author     Original: Nick Fox
 * @license    MIT License
 * @version    2.0
 */

use App\Utils\Env;
use App\Utils\Config;
use App\Utils\Logger;

// Make env() function available globally
require_once __DIR__ . '/Utils/Env.php';

// Load environment variables
Env::load(__DIR__ . '/../.env');

// Make config() function available globally
require_once __DIR__ . '/Utils/Config.php';

// Load configuration
Config::load(__DIR__ . '/../config');

// Define config function if not already defined (redundant safety check)
if (!function_exists('config')) {
    /**
     * Get configuration value
     * 
     * @param string $key Dot notation configuration key
     * @param mixed $default Default value if the key doesn't exist
     * @return mixed Configuration value
     */
    function config(string $key, $default = null) {
        return \App\Utils\Config::get($key, $default);
    }
}

// Configure error reporting
$debug = config('app.debug', false);
if ($debug) {
    error_reporting(E_ALL);
    ini_set('display_errors', 1);
} else {
    error_reporting(E_ERROR | E_PARSE);
    ini_set('display_errors', 0);
}

// Set timezone
date_default_timezone_set(config('app.timezone', 'UTC'));

// Initialize logger
Logger::init();

/**
 * Custom error handler
 * 
 * Converts PHP errors to log entries with appropriate severity levels
 * 
 * @param int $severity Error severity level
 * @param string $message Error message
 * @param string $file File where the error occurred
 * @param int $line Line number where the error occurred
 * @return bool True to prevent the PHP standard error handler from running
 */
set_error_handler(function ($severity, $message, $file, $line) {
    if (!(error_reporting() & $severity)) {
        // This error code is not included in error_reporting
        return;
    }
    
    $errorType = match ($severity) {
        E_ERROR, E_CORE_ERROR, E_COMPILE_ERROR, E_USER_ERROR => 'Error',
        E_WARNING, E_CORE_WARNING, E_COMPILE_WARNING, E_USER_WARNING => 'Warning',
        E_NOTICE, E_USER_NOTICE => 'Notice',
        E_DEPRECATED, E_USER_DEPRECATED => 'Deprecated',
        default => 'Unknown'
    };
    
    Logger::error("PHP {$errorType}: {$message}", [
        'file' => $file,
        'line' => $line,
    ]);
    
    // Don't execute PHP's internal error handler
    return true;
});

/**
 * Custom exception handler
 * 
 * Logs uncaught exceptions and returns appropriate responses
 * based on the application's debug mode
 * 
 * @param \Throwable $e The uncaught exception
 * @return void
 */
set_exception_handler(function (\Throwable $e) {
    Logger::error('Uncaught exception: ' . $e->getMessage(), [
        'exception' => get_class($e),
        'file' => $e->getFile(),
        'line' => $e->getLine(),
        'trace' => $e->getTraceAsString(),
    ]);
    
    if (config('app.debug', false)) {
        // In debug mode, show detailed error information
        header('Content-Type: application/json');
        echo json_encode([
            'error' => 'Internal Server Error',
            'message' => $e->getMessage(),
            'file' => $e->getFile(),
            'line' => $e->getLine(),
            'trace' => explode("\n", $e->getTraceAsString()),
        ]);
    } else {
        // In production, show a generic error message
        header('Content-Type: application/json');
        echo json_encode([
            'error' => 'Internal Server Error',
            'message' => 'An unexpected error occurred. Please try again later.',
        ]);
    }
    
    exit(1);
});

/**
 * Register shutdown function
 * 
 * Handles fatal errors that occur during script execution
 * 
 * @return void
 */
register_shutdown_function(function () {
    $error = error_get_last();
    if ($error !== null && in_array($error['type'], [E_ERROR, E_PARSE, E_CORE_ERROR, E_COMPILE_ERROR])) {
        Logger::critical('Fatal error', [
            'message' => $error['message'],
            'file' => $error['file'],
            'line' => $error['line'],
        ]);
        
        if (!headers_sent()) {
            header('HTTP/1.1 500 Internal Server Error');
            if (config('app.debug', false)) {
                header('Content-Type: application/json');
                echo json_encode([
                    'error' => 'Fatal Error',
                    'message' => $error['message'],
                    'file' => $error['file'],
                    'line' => $error['line'],
                ]);
            } else {
                header('Content-Type: application/json');
                echo json_encode([
                    'error' => 'Internal Server Error',
                    'message' => 'A fatal error occurred. Please try again later.',
                ]);
            }
        }
    }
});
