<?php

return [
    // Database type: mysql, postgresql, sqlite
    'driver' => env('DB_DRIVER', 'mysql'),
    
    // MySQL and PostgreSQL settings
    'host' => env('DB_HOST', 'localhost'),
    'port' => env('DB_PORT', '3306'),
    'database' => env('DB_DATABASE', 'wordpresss_db'),
    'username' => env('DB_USERNAME', 'wordpress_user'),
    'password' => env('DB_PASSWORD', 'frodo@137'),
    'charset' => 'utf8mb4',
    'collation' => 'utf8mb4_unicode_ci',
    'prefix' => env('DB_TABLE_PREFIX', 'wpw3k7z_'), // Add this line for table prefix

    // SQLite settings
    'sqlite_path' => env('DB_SQLITE_PATH', __DIR__ . '/../sqlite/gpstracker.sqlite'),
    
    // PDO options
    'options' => [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES => false,
    ],
];
