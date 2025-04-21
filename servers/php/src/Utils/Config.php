<?php

namespace App\Utils;

/**
 * Configuration Handler
 * 
 * This class provides a centralized configuration management system for the application.
 * It loads configuration files from the config directory and provides methods to
 * access configuration values using dot notation (e.g., 'app.debug').
 * 
 * Features:
 * - Loads all PHP configuration files from a directory
 * - Supports hierarchical configuration using dot notation
 * - Provides default values for missing configuration keys
 * - Allows runtime modification of configuration values
 * - Offers a global 'config()' helper function for convenient access
 * 
 * @package App\Utils
 */
class Config
{
    /**
     * Configuration values storage
     * 
     * Stores all configuration values in a multi-dimensional array
     * where the first level keys are the filenames without extension
     * 
     * @var array Configuration values
     */
    private static array $config = [];
    
    /**
     * Load configuration from files
     * 
     * Recursively loads all .php files from the specified directory
     * into the configuration store. Each file should return an array
     * of configuration values.
     * 
     * @param string $path Path to the configuration directory
     * @return void
     */
    public static function load(string $path): void
    {
        foreach (glob("{$path}/*.php") as $file) {
            $name = pathinfo($file, PATHINFO_FILENAME);
            $values = require $file;
            
            if (is_array($values)) {
                self::$config[$name] = $values;
            }
        }
    }
    
    /**
     * Get configuration value using dot notation
     * 
     * Retrieves a configuration value using dot notation to navigate
     * through nested arrays. For example, 'app.debug' will get the
     * 'debug' value from the 'app' configuration array.
     * 
     * @param string $key Dot notation key (e.g. 'app.debug')
     * @param mixed $default Default value if key doesn't exist
     * @return mixed Configuration value or default if not found
     */
    public static function get(string $key, $default = null)
    {
        $keys = explode('.', $key);
        $config = self::$config;
        
        foreach ($keys as $segment) {
            if (!is_array($config) || !array_key_exists($segment, $config)) {
                return $default;
            }
            
            $config = $config[$segment];
        }
        
        return $config;
    }
    
    /**
     * Set configuration value using dot notation
     * 
     * Sets a configuration value using dot notation, creating
     * the necessary nested arrays as needed.
     * 
     * @param string $key Dot notation key (e.g. 'app.debug')
     * @param mixed $value Value to set
     * @return void
     */
    public static function set(string $key, $value): void
    {
        $keys = explode('.', $key);
        $configKey = array_shift($keys);
        
        if (!isset(self::$config[$configKey])) {
            self::$config[$configKey] = [];
        }
        
        $current = &self::$config[$configKey];
        
        foreach ($keys as $segment) {
            if (!is_array($current)) {
                $current = [];
            }
            
            if (!isset($current[$segment])) {
                $current[$segment] = [];
            }
            
            $current = &$current[$segment];
        }
        
        $current = $value;
    }
    
    /**
     * Check if configuration key exists
     * 
     * Verifies that a configuration key exists using dot notation.
     * 
     * @param string $key Dot notation key to check
     * @return bool True if the key exists, false otherwise
     */
    public static function has(string $key): bool
    {
        $keys = explode('.', $key);
        $config = self::$config;
        
        foreach ($keys as $segment) {
            if (!is_array($config) || !array_key_exists($segment, $config)) {
                return false;
            }
            
            $config = $config[$segment];
        }
        
        return true;
    }
    
    /**
     * Get all configuration values
     * 
     * Returns the entire configuration array.
     * 
     * @return array All configuration values
     */
    public static function all(): array
    {
        return self::$config;
    }
}

/**
 * Helper function to access configuration
 * 
 * Global function for convenient access to configuration values
 * 
 * @param string $key Dot notation configuration key
 * @param mixed $default Default value if the key doesn't exist
 * @return mixed Configuration value or default if not found
 */
if (!function_exists('config')) {
    function config(string $key, $default = null)
    {
        return Config::get($key, $default);
    }
}
