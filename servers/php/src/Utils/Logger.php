<?php

namespace App\Utils;

use Monolog\Level;
use Monolog\Logger as MonologLogger;
use Monolog\Handler\StreamHandler;
use Monolog\Handler\RotatingFileHandler;
use Monolog\Formatter\LineFormatter;
use Monolog\Processor\IntrospectionProcessor;
use Monolog\Processor\WebProcessor;

/**
 * Application Logging System
 * 
 * This class provides a wrapper around Monolog for application logging.
 * It configures log handlers, formatters, and processors based on application
 * configuration, and provides convenience methods for logging at different levels.
 * 
 * Features:
 * - Supports multiple logging channels (file, stderr)
 * - Configurable log rotation and retention
 * - Supports different log levels (debug, info, warning, error, etc.)
 * - Adds contextual information to log entries (file, line, web request data)
 * - Provides a global 'logger()' helper function
 * 
 * @package App\Utils
 */
class Logger
{
    /**
     * Monolog logger instance
     * 
     * @var MonologLogger|null The logger instance
     */
    private static ?MonologLogger $logger = null;
    
    /**
     * Initialize the logger
     * 
     * Sets up the Monolog logger with appropriate handlers,
     * formatters, and processors based on the application
     * configuration.
     * 
     * @return void
     */
    public static function init(): void
    {
        $channel = config('logging.default', 'stack');
        $channelsConfig = config('logging.channels', []);
        $levelMap = config('logging.level_map', []);
        
        // Create a new logger instance
        self::$logger = new MonologLogger('gpstracker');
        
        // Add processors
        self::$logger->pushProcessor(new IntrospectionProcessor());
        self::$logger->pushProcessor(new WebProcessor());
        
        // Handle 'stack' type channel by adding all its configured channels
        if ($channel === 'stack' && isset($channelsConfig['stack'])) {
            foreach ($channelsConfig['stack']['channels'] as $stackChannel) {
                self::addHandler($stackChannel, $channelsConfig, $levelMap);
            }
        } else {
            self::addHandler($channel, $channelsConfig, $levelMap);
        }
    }
    
    /**
     * Add a log handler based on configuration
     * 
     * Creates and configures different types of log handlers:
     * - file: Rotating file handler with automatic log rotation
     * - stderr: Stream handler that outputs to STDERR
     * 
     * @param string $channel Channel name ('file', 'stderr')
     * @param array $channelsConfig Channel configuration array
     * @param array $levelMap Log level name to Monolog Level mapping
     * @return void
     */
    private static function addHandler(string $channel, array $channelsConfig, array $levelMap): void
    {
        if (!isset($channelsConfig[$channel])) {
            return;
        }
        
        $config = $channelsConfig[$channel];
        $level = $levelMap[strtolower($config['level'] ?? 'warning')] ?? Level::Warning;
        
        if ($channel === 'file') {
            $path = $config['path'] ?? __DIR__ . '/../../logs/gpstracker.log';
            $days = $config['days'] ?? 7;
            
            $handler = new RotatingFileHandler($path, $days, $level);
        } elseif ($channel === 'stderr') {
            $handler = new StreamHandler('php://stderr', $level);
        } else {
            return;
        }
        
        // Use a consistent format for all handlers
        $formatter = new LineFormatter(
            "[%datetime%] %channel%.%level_name%: %message% %context% %extra%\n",
            "Y-m-d H:i:s",
            true,
            true
        );
        
        $handler->setFormatter($formatter);
        self::$logger->pushHandler($handler);
    }
    
    /**
     * Get the logger instance
     * 
     * Returns the Monolog logger instance, initializing it
     * if it hasn't been initialized yet.
     * 
     * @return MonologLogger The Monolog logger instance
     */
    public static function getLogger(): MonologLogger
    {
        if (self::$logger === null) {
            self::init();
        }
        
        return self::$logger;
    }
    
    /**
     * Log emergency level message
     * 
     * System is unusable.
     * 
     * @param string $message Log message
     * @param array $context Additional context data
     * @return void
     */
    public static function emergency(string $message, array $context = []): void
    {
        self::getLogger()->emergency($message, $context);
    }
    
    /**
     * Log alert level message
     * 
     * Action must be taken immediately.
     * 
     * @param string $message Log message
     * @param array $context Additional context data
     * @return void
     */
    public static function alert(string $message, array $context = []): void
    {
        self::getLogger()->alert($message, $context);
    }
    
    /**
     * Log critical level message
     * 
     * Critical conditions.
     * 
     * @param string $message Log message
     * @param array $context Additional context data
     * @return void
     */
    public static function critical(string $message, array $context = []): void
    {
        self::getLogger()->critical($message, $context);
    }
    
    /**
     * Log error level message
     * 
     * Runtime errors that do not require immediate action.
     * 
     * @param string $message Log message
     * @param array $context Additional context data
     * @return void
     */
    public static function error(string $message, array $context = []): void
    {
        self::getLogger()->error($message, $context);
    }
    
    /**
     * Log warning level message
     * 
     * Exceptional occurrences that are not errors.
     * 
     * @param string $message Log message
     * @param array $context Additional context data
     * @return void
     */
    public static function warning(string $message, array $context = []): void
    {
        self::getLogger()->warning($message, $context);
    }
    
    /**
     * Log notice level message
     * 
     * Normal but significant events.
     * 
     * @param string $message Log message
     * @param array $context Additional context data
     * @return void
     */
    public static function notice(string $message, array $context = []): void
    {
        self::getLogger()->notice($message, $context);
    }
    
    /**
     * Log info level message
     * 
     * Interesting events.
     * 
     * @param string $message Log message
     * @param array $context Additional context data
     * @return void
     */
    public static function info(string $message, array $context = []): void
    {
        self::getLogger()->info($message, $context);
    }
    
    /**
     * Log debug level message
     * 
     * Detailed debug information.
     * 
     * @param string $message Log message
     * @param array $context Additional context data
     * @return void
     */
    public static function debug(string $message, array $context = []): void
    {
        self::getLogger()->debug($message, $context);
    }
    
    /**
     * Log with arbitrary level
     * 
     * Log with a specified level name.
     * 
     * @param string $level Level name (debug, info, warning, error, etc.)
     * @param string $message Log message
     * @param array $context Additional context data
     * @return void
     */
    public static function log(string $level, string $message, array $context = []): void
    {
        $levelMap = config('logging.level_map', []);
        $monologLevel = $levelMap[strtolower($level)] ?? Level::Info;
        
        self::getLogger()->log($monologLevel, $message, $context);
    }
}

/**
 * Helper function to log messages
 * 
 * Global function for convenient access to the logger
 * 
 * @return Logger Logger instance
 */
if (!function_exists('logger')) {
    function logger(): Logger
    {
        return new Logger();
    }
}
