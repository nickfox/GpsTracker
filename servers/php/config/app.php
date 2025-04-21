<?php

// Ensure env() function is available
if (!function_exists('env')) {
    function env(string $key, $default = null) {
        // Get from $_ENV or $_SERVER first
        if (isset($_ENV[$key])) {
            return $_ENV[$key];
        }
        
        if (isset($_SERVER[$key])) {
            return $_SERVER[$key];
        }
        
        // Try getenv
        $value = getenv($key);
        if ($value !== false) {
            // Handle special values
            switch (strtolower($value)) {
                case 'true': case '(true)': return true;
                case 'false': case '(false)': return false;
                case 'null': case '(null)': return null;
                case 'empty': case '(empty)': return '';
            }
            return $value;
        }
        
        return $default;
    }
}

return [
    // Application settings
    'name' => env('APP_NAME', 'GPS Tracker'),
    'env' => env('APP_ENV', 'production'),
    'debug' => env('APP_DEBUG', false),
    'timezone' => env('APP_TIMEZONE', 'UTC'),
    
    // API settings
    'api' => [
        'throttle' => [   
            'enabled' => true,
            'max_requests' => 60,
            'decay_minutes' => 1,
        ],
    ],
    
    // Map settings
    'maps' => [
        'default_provider' => env('MAP_PROVIDER', 'openstreetmap'),
        'google_maps_key' => env('GOOGLE_MAPS_KEY', ''),
        'bing_maps_key' => env('BING_MAPS_KEY', ''),
    ],
    
    // Unit settings
    'units' => [
        'distance' => env('DISTANCE_UNIT', 'mi'), // mi or km
        'speed' => env('SPEED_UNIT', 'mph'),      // mph or kph
    ],
];
