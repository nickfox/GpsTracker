<?php

namespace Tests;

use PHPUnit\Framework\TestCase;
use App\Models\GPSLocation;

class GPSLocationTest extends TestCase
{
    /**
     * Test creating a new GPSLocation object
     */
    public function testCreateGPSLocation(): void
    {
        $location = new GPSLocation(
            40.7128, // latitude
            -74.0060, // longitude
            20, // speed
            90, // direction
            10.5, // distance
            '2023-01-01 12:00:00', // gpsTime
            'GPS', // locationMethod
            'testUser', // userName
            '555-1234', // phoneNumber
            'test-session-123', // sessionID
            10, // accuracy
            'Test location', // extraInfo
            'test' // eventType
        );
        
        $this->assertEquals(40.7128, $location->getLatitude());
        $this->assertEquals(-74.0060, $location->getLongitude());
        $this->assertEquals(20, $location->getSpeed());
        $this->assertEquals(90, $location->getDirection());
        $this->assertEquals(10.5, $location->getDistance());
        $this->assertEquals('2023-01-01 12:00:00', $location->getGpsTime());
        $this->assertEquals('GPS', $location->getLocationMethod());
        $this->assertEquals('testUser', $location->getUserName());
        $this->assertEquals('555-1234', $location->getPhoneNumber());
        $this->assertEquals('test-session-123', $location->getSessionID());
        $this->assertEquals(10, $location->getAccuracy());
        $this->assertEquals('Test location', $location->getExtraInfo());
        $this->assertEquals('test', $location->getEventType());
    }
    
    /**
     * Test location validation
     */
    public function testValidate(): void
    {
        // Valid location
        $validLocation = new GPSLocation(40.7128, -74.0060);
        $this->assertEmpty($validLocation->validate());
        
        // Invalid latitude
        $invalidLatLocation = new GPSLocation(100, -74.0060);
        $errors = $invalidLatLocation->validate();
        $this->assertNotEmpty($errors);
        $this->assertStringContainsString('latitude', $errors[0]);
        
        // Invalid longitude
        $invalidLonLocation = new GPSLocation(40.7128, -200);
        $errors = $invalidLonLocation->validate();
        $this->assertNotEmpty($errors);
        $this->assertStringContainsString('longitude', $errors[0]);
        
        // Invalid coordinates (both 0)
        $invalidCoordLocation = new GPSLocation(0, 0);
        $errors = $invalidCoordLocation->validate();
        $this->assertNotEmpty($errors);
        $this->assertStringContainsString('coordinates', $errors[0]);
    }
    
    /**
     * Test fromArray method
     */
    public function testFromArray(): void
    {
        $data = [
            'latitude' => 40.7128,
            'longitude' => -74.0060,
            'speed' => 20,
            'direction' => 90,
            'distance' => 10.5,
            'gpsTime' => '2023-01-01 12:00:00',
            'locationMethod' => 'GPS',
            'userName' => 'testUser',
            'phoneNumber' => '555-1234',
            'sessionID' => 'test-session-123',
            'accuracy' => 10,
            'extraInfo' => 'Test location',
            'eventType' => 'test'
        ];
        
        $location = GPSLocation::fromArray($data);
        
        $this->assertEquals(40.7128, $location->getLatitude());
        $this->assertEquals(-74.0060, $location->getLongitude());
        $this->assertEquals(20, $location->getSpeed());
        $this->assertEquals(90, $location->getDirection());
        $this->assertEquals(10.5, $location->getDistance());
        $this->assertEquals('2023-01-01 12:00:00', $location->getGpsTime());
        $this->assertEquals('GPS', $location->getLocationMethod());
        $this->assertEquals('testUser', $location->getUserName());
        $this->assertEquals('555-1234', $location->getPhoneNumber());
        $this->assertEquals('test-session-123', $location->getSessionID());
        $this->assertEquals(10, $location->getAccuracy());
        $this->assertEquals('Test location', $location->getExtraInfo());
        $this->assertEquals('test', $location->getEventType());
    }
    
    /**
     * Test toArray method
     */
    public function testToArray(): void
    {
        $location = new GPSLocation(
            40.7128, // latitude
            -74.0060, // longitude
            20, // speed
            90, // direction
            10.5, // distance
            '2023-01-01 12:00:00', // gpsTime
            'GPS', // locationMethod
            'testUser', // userName
            '555-1234', // phoneNumber
            'test-session-123', // sessionID
            10, // accuracy
            'Test location', // extraInfo
            'test' // eventType
        );
        
        $array = $location->toArray();
        
        $this->assertEquals(40.7128, $array['latitude']);
        $this->assertEquals(-74.0060, $array['longitude']);
        $this->assertEquals(20, $array['speed']);
        $this->assertEquals(90, $array['direction']);
        $this->assertEquals(10.5, $array['distance']);
        $this->assertEquals('2023-01-01 12:00:00', $array['gpsTime']);
        $this->assertEquals('GPS', $array['locationMethod']);
        $this->assertEquals('testUser', $array['userName']);
        $this->assertEquals('555-1234', $array['phoneNumber']);
        $this->assertEquals('test-session-123', $array['sessionID']);
        $this->assertEquals(10, $array['accuracy']);
        $this->assertEquals('Test location', $array['extraInfo']);
        $this->assertEquals('test', $array['eventType']);
    }
}