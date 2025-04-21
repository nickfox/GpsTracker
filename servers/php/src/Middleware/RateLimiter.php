<?php

namespace App\Middleware;

use App\Utils\Logger;

/**
 * API Rate Limiter Middleware
 * 
 * This class provides protection against API abuse by limiting the number of
 * requests that can be made from a single IP address within a time window.
 * 
 * Features:
 * - IP-based rate limiting
 * - Configurable request limits and time windows
 * - Persistent rate limit data across requests
 * - Automatic cleanup of expired rate limit entries
 * - Detailed logging of rate limit violations
 * 
 * @package App\Middleware
 */
class RateLimiter
{
    /**
     * Rate limit data storage
     * 
     * Stores rate limit information for each IP address:
     * - count: Number of requests made
     * - timestamp: Time of the first request in the current window
     * 
     * @var array Rate limit data (IP => [count, timestamp])
     */
    private static array $rateLimits = [];
    
    /**
     * Cache file path for persisting rate limit data
     * 
     * @var string Cache file path
     */
    private static string $cacheFile;
    
    /**
     * Initialize the rate limiter
     * 
     * Sets up the rate limiter:
     * - Sets the cache file path
     * - Loads existing rate limits from cache
     * - Registers a shutdown function to save rate limits
     * 
     * @param string|null $cacheDir Directory to store rate limit cache (defaults to system temp directory)
     * @return void
     */
    public static function init(string $cacheDir = null): void
    {
        // Set cache file path
        self::$cacheFile = ($cacheDir ?? sys_get_temp_dir()) . '/gpstracker_ratelimit.json';
        
        // Load existing rate limits
        self::loadRateLimits();
        
        // Register shutdown function to save rate limits
        register_shutdown_function([self::class, 'saveRateLimits']);
    }
    
    /**
     * Check if a request is rate limited
     * 
     * Determines if the current request should be allowed based on rate limiting rules:
     * - If rate limiting is disabled, always allows the request
     * - If the IP is new, creates a new rate limit entry and allows the request
     * - If the rate limit window has expired, resets the count and allows the request
     * - If the count is below the limit, increments the count and allows the request
     * - If the count exceeds the limit, logs a warning and denies the request
     * 
     * @param string|null $ip Client IP address (defaults to $_SERVER['REMOTE_ADDR'])
     * @return bool True if request is allowed, false if rate limited
     */
    public static function check(?string $ip = null): bool
    {
        // Skip rate limiting if disabled
        if (!config('api.throttle.enabled', true)) {
            return true;
        }
        
        $ip = $ip ?? $_SERVER['REMOTE_ADDR'] ?? 'unknown';
        $maxRequests = config('api.throttle.max_requests', 60);
        $decayMinutes = config('api.throttle.decay_minutes', 1);
        
        // Clean old rate limits
        self::cleanRateLimits();
        
        // Get current timestamp
        $now = time();
        
        // Check if IP has rate limit entry
        if (!isset(self::$rateLimits[$ip])) {
            self::$rateLimits[$ip] = [
                'count' => 1,
                'timestamp' => $now,
            ];
            
            return true;
        }
        
        // Check if rate limit window has expired
        $timestamp = self::$rateLimits[$ip]['timestamp'];
        if ($now - $timestamp > $decayMinutes * 60) {
            self::$rateLimits[$ip] = [
                'count' => 1,
                'timestamp' => $now,
            ];
            
            return true;
        }
        
        // Check if rate limit exceeded
        $count = self::$rateLimits[$ip]['count'];
        if ($count >= $maxRequests) {
            Logger::warning('Rate limit exceeded', [
                'ip' => $ip,
                'count' => $count,
                'max' => $maxRequests,
                'window' => $decayMinutes . ' minutes',
            ]);
            
            return false;
        }
        
        // Increment request count
        self::$rateLimits[$ip]['count']++;
        
        return true;
    }
    
    /**
     * Clean old rate limits
     * 
     * Removes expired rate limit entries to prevent memory
     * growth and improve performance.
     * 
     * @return void
     */
    private static function cleanRateLimits(): void
    {
        $now = time();
        $decayMinutes = config('api.throttle.decay_minutes', 1);
        $expiry = $now - ($decayMinutes * 60);
        
        foreach (self::$rateLimits as $ip => $data) {
            if ($data['timestamp'] < $expiry) {
                unset(self::$rateLimits[$ip]);
            }
        }
    }
    
    /**
     * Load rate limits from cache file
     * 
     * Reads previously saved rate limit data from the cache file.
     * This preserves rate limiting across application restarts.
     * 
     * @return void
     */
    private static function loadRateLimits(): void
    {
        if (file_exists(self::$cacheFile)) {
            $data = file_get_contents(self::$cacheFile);
            if ($data) {
                $rateLimits = json_decode($data, true);
                if (is_array($rateLimits)) {
                    self::$rateLimits = $rateLimits;
                }
            }
        }
    }
    
    /**
     * Save rate limits to cache file
     * 
     * Persists rate limit data to the cache file. This is called
     * automatically during application shutdown.
     * 
     * @return void
     */
    public static function saveRateLimits(): void
    {
        if (!empty(self::$rateLimits)) {
            $data = json_encode(self::$rateLimits);
            @file_put_contents(self::$cacheFile, $data);
        }
    }
    
    /**
     * Get current rate limit data for debugging
     * 
     * Returns the current rate limit data. This is useful for
     * debugging and monitoring rate limit usage.
     * 
     * @return array Rate limit data (IP => [count, timestamp])
     */
    public static function getRateLimits(): array
    {
        return self::$rateLimits;
    }
}
