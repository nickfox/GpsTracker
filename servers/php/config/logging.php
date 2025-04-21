<?php

use Monolog\Level;

return [
    'default' => env('LOG_CHANNEL', 'stack'),
    
    'channels' => [
        'stack' => [
            'channels' => ['file', 'stderr'],
        ],
        
        'file' => [
            'path' => __DIR__ . '/../logs/gpstracker.log',
            'level' => env('LOG_LEVEL', 'warning'),
            'days' => 7,
        ],
        
        'stderr' => [
            'level' => env('LOG_LEVEL', 'debug'),
        ],
    ],
    
    // Mapping between environment log levels and Monolog levels
    'level_map' => [
        'debug' => Level::Debug,
        'info' => Level::Info,
        'notice' => Level::Notice,
        'warning' => Level::Warning,
        'error' => Level::Error,
        'critical' => Level::Critical,
        'alert' => Level::Alert,
        'emergency' => Level::Emergency,
    ],
];
