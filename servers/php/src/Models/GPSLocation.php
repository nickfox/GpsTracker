<?php

namespace App\Models;

use App\Services\Database;
use App\Utils\Logger;

/**
 * GPS Location model
 * 
 * This model represents a GPS location point with all associated metadata.
 * It handles data validation, persistence, and conversion between formats.
 * 
 * Each location belongs to a tracking session and contains details like
 * coordinates, speed, direction, and timestamp. The model follows an
 * active record pattern for database operations.
 * 
 * @package App\Models
 */
class GPSLocation
{
    /**
     * Unique ID for this location
     * 
     * @var int|null Database primary key, null for new unsaved records
     */
    private ?int $id = null;
    
    /**
     * Timestamp of when the record was created/updated
     * 
     * @var string Timestamp in Y-m-d H:i:s format
     */
    private string $lastUpdate;
    
    /**
     * Latitude coordinate
     * 
     * @var float Latitude in decimal degrees (-90.0 to 90.0)
     */
    private float $latitude;
    
    /**
     * Longitude coordinate
     * 
     * @var float Longitude in decimal degrees (-180.0 to 180.0)
     */
    private float $longitude;
    
    /**
     * Phone number of the tracking device
     * 
     * @var string Phone number or device identifier
     */
    private string $phoneNumber;
    
    /**
     * Username of the tracking device
     * 
     * @var string Username or device name
     */
    private string $userName;
    
    /**
     * Session ID to group locations
     * 
     * @var string Unique identifier for the tracking session
     */
    private string $sessionID;
    
    /**
     * Speed in mph/kph
     * 
     * @var int Speed value (non-negative)
     */
    private int $speed;
    
    /**
     * Direction in degrees
     * 
     * @var int Direction in degrees (0-360)
     */
    private int $direction;
    
    /**
     * Distance traveled
     * 
     * @var float Distance value (non-negative)
     */
    private float $distance;
    
    /**
     * GPS timestamp
     * 
     * @var string Timestamp when the location was recorded in Y-m-d H:i:s format
     */
    private string $gpsTime;
    
    /**
     * Method used to determine location
     * 
     * @var string Location determination method (GPS, Network, etc.)
     */
    private string $locationMethod;
    
    /**
     * Accuracy of the location in meters
     * 
     * @var int Accuracy value (non-negative)
     */
    private int $accuracy;
    
    /**
     * Extra information
     * 
     * @var string Additional data as free-form text
     */
    private string $extraInfo;
    
    /**
     * Event type
     * 
     * @var string Type of event that triggered the location update
     */
    private string $eventType;
    
    /**
     * Create a new GPS location
     * 
     * Initializes a new location object with the provided values.
     * Default values are used for missing parameters.
     * 
     * @param float $latitude Latitude coordinate
     * @param float $longitude Longitude coordinate
     * @param int $speed Speed in mph/kph
     * @param int $direction Direction in degrees (0-360)
     * @param float $distance Distance traveled
     * @param string $gpsTime Timestamp in Y-m-d H:i:s format
     * @param string $locationMethod Method used to determine location
     * @param string $userName Username or device name
     * @param string $phoneNumber Phone number or device identifier
     * @param string $sessionID Unique identifier for the tracking session
     * @param int $accuracy Accuracy in meters
     * @param string $extraInfo Additional data as free-form text
     * @param string $eventType Type of event that triggered the location update
     */
    public function __construct(
        float $latitude = 0.0,
        float $longitude = 0.0,
        int $speed = 0,
        int $direction = 0,
        float $distance = 0.0,
        string $gpsTime = '',
        string $locationMethod = '',
        string $userName = '',
        string $phoneNumber = '',
        string $sessionID = '',
        int $accuracy = 0,
        string $extraInfo = '',
        string $eventType = ''
    ) {
        $this->latitude = $latitude;
        $this->longitude = $longitude;
        $this->speed = $speed;
        $this->direction = $direction;
        $this->distance = $distance;
        $this->gpsTime = !empty($gpsTime) ? $gpsTime : date('Y-m-d H:i:s');
        $this->locationMethod = $locationMethod;
        $this->userName = $userName;
        $this->phoneNumber = $phoneNumber;
        $this->sessionID = $sessionID;
        $this->accuracy = $accuracy;
        $this->extraInfo = $extraInfo;
        $this->eventType = $eventType;
        $this->lastUpdate = date('Y-m-d H:i:s');
    }
    
    /**
     * Create a GPSLocation from an array of data
     * 
     * Factory method that creates a new instance from an associative array.
     * Handles different field naming conventions for flexibility.
     * 
     * @param array $data Key-value pairs with location data
     * @return self New GPSLocation instance
     */
    public static function fromArray(array $data): self
    {
        $location = new self(
            (float)($data['latitude'] ?? 0.0),
            (float)($data['longitude'] ?? 0.0),
            (int)($data['speed'] ?? 0),
            (int)($data['direction'] ?? 0),
            (float)($data['distance'] ?? 0.0),
            $data['date'] ?? $data['gpsTime'] ?? '',
            $data['locationmethod'] ?? $data['locationMethod'] ?? '',
            $data['username'] ?? $data['userName'] ?? '',
            $data['phonenumber'] ?? $data['phoneNumber'] ?? '',
            $data['sessionid'] ?? $data['sessionID'] ?? '',
            (int)($data['accuracy'] ?? 0),
            $data['extrainfo'] ?? $data['extraInfo'] ?? '',
            $data['eventtype'] ?? $data['eventType'] ?? ''
        );
        
        if (isset($data['GPSLocationID']) || isset($data['id'])) {
            $location->id = (int)($data['GPSLocationID'] ?? $data['id']);
        }
        
        if (isset($data['lastUpdate'])) {
            $location->lastUpdate = $data['lastUpdate'];
        }
        
        return $location;
    }
    
    /**
     * Convert to array
     * 
     * Creates an associative array representation of this location.
     * Useful for API responses and internal data transfer.
     * 
     * @return array Key-value pairs with location data
     */
    public function toArray(): array
    {
        return [
            'id' => $this->id,
            'lastUpdate' => $this->lastUpdate,
            'latitude' => $this->latitude,
            'longitude' => $this->longitude,
            'phoneNumber' => $this->phoneNumber,
            'userName' => $this->userName,
            'sessionID' => $this->sessionID,
            'speed' => $this->speed,
            'direction' => $this->direction,
            'distance' => $this->distance,
            'gpsTime' => $this->gpsTime,
            'locationMethod' => $this->locationMethod,
            'accuracy' => $this->accuracy,
            'extraInfo' => $this->extraInfo,
            'eventType' => $this->eventType,
        ];
    }
    
    /**
     * Convert to JSON object for map display
     * 
     * Creates a JSON string representation of this location.
     * Formats specific fields as strings for consistent API output.
     * 
     * @return string JSON-encoded location data
     */
    public function toJson(): string
    {
        return json_encode([  
            'latitude' => (string)$this->latitude,
            'longitude' => (string)$this->longitude,
            'speed' => (string)$this->speed,
            'direction' => (string)$this->direction,
            'distance' => (string)$this->distance,
            'locationMethod' => $this->locationMethod,
            'gpsTime' => $this->gpsTime,
            'userName' => $this->userName,
            'phoneNumber' => $this->phoneNumber,
            'sessionID' => $this->sessionID,
            'accuracy' => (string)$this->accuracy,
            'extraInfo' => $this->extraInfo,
        ]);
    }
    
    /**
     * Save the location to the database
     * 
     * Persists the location data to the database, handling different database drivers.
     * Uses stored procedures for MySQL and direct SQL for SQLite/PostgreSQL.
     * 
     * @return bool True on success, false on failure
     */
    public function save(): bool
    {
        $driver = config('database.driver', 'sqlite');
        
        try {
            if ($driver === 'mysql') {
                $sql = Database::getSqlFunctionCallMethod() . 'prcSaveGPSLocation(
                    :latitude, 
                    :longitude, 
                    :speed, 
                    :direction, 
                    :distance, 
                    :gpsTime, 
                    :locationMethod,
                    :userName, 
                    :phoneNumber, 
                    :sessionID, 
                    :accuracy, 
                    :extraInfo, 
                    :eventType
                )';
                
                $params = [
                    ':latitude' => $this->latitude,
                    ':longitude' => $this->longitude,
                    ':speed' => $this->speed,
                    ':direction' => $this->direction,
                    ':distance' => $this->distance,
                    ':gpsTime' => $this->gpsTime,
                    ':locationMethod' => $this->locationMethod,
                    ':userName' => $this->userName,
                    ':phoneNumber' => $this->phoneNumber,
                    ':sessionID' => $this->sessionID,
                    ':accuracy' => $this->accuracy,
                    ':extraInfo' => $this->extraInfo,
                    ':eventType' => $this->eventType,
                ];
                
                $stmt = Database::getPdo()->prepare($sql);
                $stmt->execute($params);
                $this->id = $stmt->fetchColumn();
            } else {
                // PostgreSQL or SQLite
                $sql = 'INSERT INTO gpslocations (
                    latitude, longitude, speed, direction, distance, 
                    gpsTime, locationMethod, userName, phoneNumber, 
                    sessionID, accuracy, extraInfo, eventType
                ) VALUES (
                    :latitude, :longitude, :speed, :direction, :distance, 
                    :gpsTime, :locationMethod, :userName, :phoneNumber, 
                    :sessionID, :accuracy, :extraInfo, :eventType
                )';
                
                $params = [
                    ':latitude' => $this->latitude,
                    ':longitude' => $this->longitude,
                    ':speed' => $this->speed,
                    ':direction' => $this->direction,
                    ':distance' => $this->distance,
                    ':gpsTime' => $this->gpsTime,
                    ':locationMethod' => $this->locationMethod,
                    ':userName' => $this->userName,
                    ':phoneNumber' => $this->phoneNumber,
                    ':sessionID' => $this->sessionID,
                    ':accuracy' => $this->accuracy,
                    ':extraInfo' => $this->extraInfo,
                    ':eventType' => $this->eventType,
                ];
                
                $this->id = Database::insert($sql, $params);
            }
            
            Logger::info('GPS location saved', [
                'id' => $this->id,
                'latitude' => $this->latitude,
                'longitude' => $this->longitude,
                'userName' => $this->userName,
                'sessionID' => $this->sessionID,
            ]);
            
            return true;
        } catch (\PDOException $e) {
            Logger::error('Failed to save GPS location', [
                'error' => $e->getMessage(),
                'latitude' => $this->latitude,
                'longitude' => $this->longitude,
            ]);
            
            return false;
        }
    }
    
    /**
     * Validate the location data
     * 
     * Checks all location fields against business rules.
     * Returns an array of error messages for invalid fields.
     * 
     * @return array List of validation error messages, empty if valid
     */
    public function validate(): array
    {
        $errors = [];
        
        // Check required coordinates
        if ($this->latitude === 0.0 && $this->longitude === 0.0) {
            $errors[] = 'Invalid coordinates: both latitude and longitude cannot be 0';
        }
        
        // Validate latitude range
        if ($this->latitude < -90.0 || $this->latitude > 90.0) {
            $errors[] = 'Invalid latitude: must be between -90 and 90';
        }
        
        // Validate longitude range
        if ($this->longitude < -180.0 || $this->longitude > 180.0) {
            $errors[] = 'Invalid longitude: must be between -180 and 180';
        }
        
        // Validate speed
        if ($this->speed < 0) {
            $errors[] = 'Invalid speed: cannot be negative';
        }
        
        // Validate direction
        if ($this->direction < 0 || $this->direction > 360) {
            $errors[] = 'Invalid direction: must be between 0 and 360 degrees';
        }
        
        // Validate distance
        if ($this->distance < 0) {
            $errors[] = 'Invalid distance: cannot be negative';
        }
        
        // Validate GPS time format
        if (!empty($this->gpsTime) && !strtotime($this->gpsTime)) {
            $errors[] = 'Invalid GPS time format';
        }
        
        return $errors;
    }
    
    /**
     * Get a GPS location by ID
     * 
     * Static method to retrieve a location by its primary key.
     * 
     * @param int $id Location ID to retrieve
     * @return self|null Location object if found, null if not found
     */
    public static function getById(int $id): ?self
    {
        try {
            $sql = 'SELECT * FROM gpslocations WHERE GPSLocationID = :id LIMIT 1';
            $result = Database::queryOne($sql, [':id' => $id]);
            
            if ($result) {
                return self::fromArray($result);
            }
            
            return null;
        } catch (\PDOException $e) {
            Logger::error('Failed to get GPS location by ID', [
                'id' => $id,
                'error' => $e->getMessage(),
            ]);
            
            return null;
        }
    }
    
    // Getters and setters
    
    /**
     * Get the location ID
     * 
     * @return int|null The location ID or null if not yet saved
     */
    public function getId(): ?int
    {
        return $this->id;
    }
    
    /**
     * Get the last update timestamp
     * 
     * @return string Last update timestamp in Y-m-d H:i:s format
     */
    public function getLastUpdate(): string
    {
        return $this->lastUpdate;
    }
    
    /**
     * Get the latitude
     * 
     * @return float Latitude value
     */
    public function getLatitude(): float
    {
        return $this->latitude;
    }
    
    /**
     * Set the latitude
     * 
     * @param float $latitude New latitude value
     * @return self For method chaining
     */
    public function setLatitude(float $latitude): self
    {
        $this->latitude = $latitude;
        return $this;
    }
    
    /**
     * Get the longitude
     * 
     * @return float Longitude value
     */
    public function getLongitude(): float
    {
        return $this->longitude;
    }
    
    /**
     * Set the longitude
     * 
     * @param float $longitude New longitude value
     * @return self For method chaining
     */
    public function setLongitude(float $longitude): self
    {
        $this->longitude = $longitude;
        return $this;
    }
    
    /**
     * Get the phone number
     * 
     * @return string Phone number or device identifier
     */
    public function getPhoneNumber(): string
    {
        return $this->phoneNumber;
    }
    
    /**
     * Set the phone number
     * 
     * @param string $phoneNumber New phone number or device identifier
     * @return self For method chaining
     */
    public function setPhoneNumber(string $phoneNumber): self
    {
        $this->phoneNumber = $phoneNumber;
        return $this;
    }
    
    /**
     * Get the username
     * 
     * @return string Username or device name
     */
    public function getUserName(): string
    {
        return $this->userName;
    }
    
    /**
     * Set the username
     * 
     * @param string $userName New username or device name
     * @return self For method chaining
     */
    public function setUserName(string $userName): self
    {
        $this->userName = $userName;
        return $this;
    }
    
    /**
     * Get the session ID
     * 
     * @return string Session ID value
     */
    public function getSessionID(): string
    {
        return $this->sessionID;
    }
    
    /**
     * Set the session ID
     * 
     * @param string $sessionID New session ID value
     * @return self For method chaining
     */
    public function setSessionID(string $sessionID): self
    {
        $this->sessionID = $sessionID;
        return $this;
    }
    
    /**
     * Get the speed
     * 
     * @return int Speed value
     */
    public function getSpeed(): int
    {
        return $this->speed;
    }
    
    /**
     * Set the speed
     * 
     * @param int $speed New speed value
     * @return self For method chaining
     */
    public function setSpeed(int $speed): self
    {
        $this->speed = $speed;
        return $this;
    }
    
    /**
     * Get the direction
     * 
     * @return int Direction value in degrees
     */
    public function getDirection(): int
    {
        return $this->direction;
    }
    
    /**
     * Set the direction
     * 
     * @param int $direction New direction value in degrees
     * @return self For method chaining
     */
    public function setDirection(int $direction): self
    {
        $this->direction = $direction;
        return $this;
    }
    
    /**
     * Get the distance
     * 
     * @return float Distance value
     */
    public function getDistance(): float
    {
        return $this->distance;
    }
    
    /**
     * Set the distance
     * 
     * @param float $distance New distance value
     * @return self For method chaining
     */
    public function setDistance(float $distance): self
    {
        $this->distance = $distance;
        return $this;
    }
    
    /**
     * Get the GPS time
     * 
     * @return string GPS time in Y-m-d H:i:s format
     */
    public function getGpsTime(): string
    {
        return $this->gpsTime;
    }
    
    /**
     * Set the GPS time
     * 
     * @param string $gpsTime New GPS time in Y-m-d H:i:s format
     * @return self For method chaining
     */
    public function setGpsTime(string $gpsTime): self
    {
        $this->gpsTime = $gpsTime;
        return $this;
    }
    
    /**
     * Get the location method
     * 
     * @return string Location method value
     */
    public function getLocationMethod(): string
    {
        return $this->locationMethod;
    }
    
    /**
     * Set the location method
     * 
     * @param string $locationMethod New location method value
     * @return self For method chaining
     */
    public function setLocationMethod(string $locationMethod): self
    {
        $this->locationMethod = $locationMethod;
        return $this;
    }
    
    /**
     * Get the accuracy
     * 
     * @return int Accuracy value in meters
     */
    public function getAccuracy(): int
    {
        return $this->accuracy;
    }
    
    /**
     * Set the accuracy
     * 
     * @param int $accuracy New accuracy value in meters
     * @return self For method chaining
     */
    public function setAccuracy(int $accuracy): self
    {
        $this->accuracy = $accuracy;
        return $this;
    }
    
    /**
     * Get the extra information
     * 
     * @return string Extra information value
     */
    public function getExtraInfo(): string
    {
        return $this->extraInfo;
    }
    
    /**
     * Set the extra information
     * 
     * @param string $extraInfo New extra information value
     * @return self For method chaining
     */
    public function setExtraInfo(string $extraInfo): self
    {
        $this->extraInfo = $extraInfo;
        return $this;
    }
    
    /**
     * Get the event type
     * 
     * @return string Event type value
     */
    public function getEventType(): string
    {
        return $this->eventType;
    }
    
    /**
     * Set the event type
     * 
     * @param string $eventType New event type value
     * @return self For method chaining
     */
    public function setEventType(string $eventType): self
    {
        $this->eventType = $eventType;
        return $this;
    }
}
