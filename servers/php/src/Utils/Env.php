<?php

namespace App\Utils;

/**
 * Environment Variable Handler
 * 
 * This class provides functionality for loading and accessing environment variables
 * from a .env file. It supports parsing of common value formats and type conversion
 * for boolean, null, and empty values.
 * 
 * Features:
 * - Loads variables from .env file
 * - Creates default .env file if none exists
 * - Supports quoted values
 * - Handles type conversions for common values (true, false, null, empty)
 * - Provides a convenient global 'env()' helper function
 * 
 * @package App\Utils
 */
class Env
{
    /**
     * Flag to track if .env has been loaded
     * 
     * Prevents multiple loads of the same file
     * 
     * @var bool
     */
    private static bool $loaded = false;
    
    /**
     * Load environment variables from .env file
     * 
     * Parses a .env file and loads all variables into the environment.
     * If the file doesn't exist, creates a default one with sensible values.
     * 
     * @param string $path Full path to the .env file
     * @return void
     */
    public static function load(string $path): void
    {
        if (self::$loaded) {
            return;
        }
        
        if (file_exists($path)) {
            $lines = file($path, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
            foreach ($lines as $line) {
                // Skip comments
                if (strpos(trim($line), '#') === 0) {
                    continue;
                }
                
                // Skip invalid lines
                if (strpos($line, '=') === false) {
                    continue;
                }
                
                list($name, $value) = explode('=', $line, 2);
                $name = trim($name);
                $value = trim($value);
                
                // Remove quotes if present
                if (strpos($value, '"') === 0 && strrpos($value, '"') === strlen($value) - 1) {
                    $value = substr($value, 1, -1);
                } elseif (strpos($value, "'") === 0 && strrpos($value, "'") === strlen($value) - 1) {
                    $value = substr($value, 1, -1);
                }
                // Debug log the parsed key/value
                if ($name === 'DB_TABLE_PREFIX') {
                    error_log("!!! Env::load found DB_TABLE_PREFIX: " . $value);
                }
                
                putenv("{$name}={$value}");
                $_ENV[$name] = $value;
                $_SERVER[$name] = $value;
                $_SERVER[$name] = $value;
            }
            
            self::$loaded = true;
        } else {
            // Create a default .env file if it doesn't exist
            $defaultEnv = "# Application Settings\n";
            $defaultEnv .= "APP_NAME=\"GPS Tracker\"\n";
            $defaultEnv .= "APP_ENV=production\n";
            $defaultEnv .= "APP_DEBUG=false\n";
            $defaultEnv .= "APP_TIMEZONE=UTC\n\n";
            
            $defaultEnv .= "# Database Settings\n";
            $defaultEnv .= "DB_DRIVER=mysql\n";
            $defaultEnv .= "DB_HOST=localhost\n";
            $defaultEnv .= "DB_PORT=3306\n";
            $defaultEnv .= "DB_DATABASE=your_wordpress_database\n";
            $defaultEnv .= "DB_USERNAME=your_wordpress_user\n";
            $defaultEnv .= "DB_PASSWORD=your_wordpress_password\n\n";
            
            $defaultEnv .= "# Maps Configuration\n";
            $defaultEnv .= "MAP_PROVIDER=openstreetmap\n";
            $defaultEnv .= "GOOGLE_MAPS_KEY=\n";
            $defaultEnv .= "BING_MAPS_KEY=\n\n";
            
            $defaultEnv .= "# Logging\n";
            $defaultEnv .= "LOG_CHANNEL=file\n";
            $defaultEnv .= "LOG_LEVEL=error\n\n";
            
            $defaultEnv .= "# Units\n";
            $defaultEnv .= "DISTANCE_UNIT=mi\n";
            $defaultEnv .= "SPEED_UNIT=mph\n";
            
            // Create directory if it doesn't exist
            $dir = dirname($path);
            if (!is_dir($dir)) {
                mkdir($dir, 0755, true);
            }
            
            file_put_contents($path, $defaultEnv);
            
            // Recursively call load() to populate the env vars
            self::load($path);
        }
    }
    
    /**
     * Get an environment variable value
     * 
     * Retrieves the value of an environment variable with special handling for:
     * - boolean values (true/false)
     * - null values
     * - empty strings
     * 
     * @param string $key Environment variable name
     * @param mixed $default Default value to return if variable is not found
     * @return mixed The environment variable value or the default
     */
    public static function get(string $key, $default = null)
    {
        $value = getenv($key);
        // Debug log the key being requested and the value found by getenv
        if ($key === 'DB_TABLE_PREFIX') {
            error_log("!!! Env::get checking for DB_TABLE_PREFIX. getenv() returned: " . ($value === false ? 'false' : $value));
        }
        
        if ($value === false) {
            return $default;
        }
        
        switch (strtolower($value)) {
            case 'true':
            case '(true)':
                return true;
            case 'false':
            case '(false)':
                return false;
            case 'null':
            case '(null)':
                return null;
            case 'empty':
            case '(empty)':
                return '';
        }
        
        return $value;
    }
}

/**
 * Helper function to access environment variables
 * 
 * Global function for convenient access to environment variables
 * 
 * @param string $key Environment variable name
 * @param mixed $default Default value to return if variable is not found
 * @return mixed The environment variable value or the default
 */
if (!function_exists('env')) {
    function env(string $key, $default = null)
    {
        return Env::get($key, $default);
    }
}
