<?php

namespace Tests;

use PHPUnit\Framework\TestCase;
use App\Utils\Config;

class MapProviderTest extends TestCase
{
    /**
     * Set up the test environment
     */
    protected function setUp(): void
    {
        parent::setUp();
        
        // Load configuration
        Config::load(__DIR__ . '/../config');
        
        // Set environment variables for testing
        putenv('MAP_PROVIDER=openstreetmap');
    }
    
    /**
     * Test that the default map provider is set to OpenStreetMap
     */
    public function testDefaultMapProviderIsOpenStreetMap(): void
    {
        $this->assertEquals('openstreetmap', config('maps.default_provider'));
    }
    
    /**
     * Test map provider configuration value
     */
    public function testMapProviderConfig(): void
    {
        $config = Config::get('app.maps');
        
        $this->assertIsArray($config);
        $this->assertArrayHasKey('default_provider', $config);
        $this->assertEquals('openstreetmap', $config['default_provider']);
    }
    
    /**
     * Test map provider with environment override
     */
    public function testMapProviderEnvironmentOverride(): void
    {
        // Change environment variable
        putenv('MAP_PROVIDER=google');
        
        // Test that the configuration reflects the change
        $this->assertEquals('google', config('maps.default_provider'));
        
        // Reset environment variable
        putenv('MAP_PROVIDER=openstreetmap');
        
        // Verify it's reset
        $this->assertEquals('openstreetmap', config('maps.default_provider'));
    }
    
    /**
     * Test that the map template contains the correct data attribute
     */
    public function testMapTemplateDataAttribute(): void
    {
        // Capture output buffer
        ob_start();
        
        // Include the template
        include __DIR__ . '/../templates/map.php';
        
        // Get output and clean buffer
        $output = ob_get_clean();
        
        // Verify the data attribute is present and has the correct value
        $this->assertStringContainsString('data-map-provider="openstreetmap"', $output);
    }
}