
/**
 * GPS Tracker Application
 * 
 * @fileoverview Modern JavaScript implementation for the GPS Tracker web interface.
 * Handles map display, route management, and real-time tracking features.
 * 
 * This module provides functionality for:
 * - Displaying GPS locations on interactive maps using Leaflet
 * - Managing routes and sessions
 * - Real-time tracking with auto-refresh capabilities
 * - Creating and customizing markers and popups
 * - Handling different map providers
 * 
 * @requires Leaflet
 * @requires jQuery
 * @requires leaflet-plugins (optional, for Google Maps)
 * @version 2.0
 * @author Original: Nick Fox
 * @license MIT License
 */

// Use strict mode for better error checking and performance
'use strict';

/**
 * Main application object 
 * @namespace
 */
const GPSTracker = {
    /**
     * Application configuration options
     * @memberof GPSTracker
     * @type {Object}
     * @property {number} autoRefreshInterval - Interval for auto-refresh in milliseconds (default: 60 seconds)
     * @property {number} mapDefaultZoom - Default zoom level for map initialization
     * @property {number} errorTimeout - Timeout for temporary messages in milliseconds
     * @property {string} defaultMapProvider - Default map provider to use (openstreetmap or google)
     */
    config: {
        autoRefreshInterval: 60 * 1000, // 1 minute in milliseconds
        mapDefaultZoom: 13,
        errorTimeout: 7 * 1000, // 7 seconds
        defaultMapProvider: 'openstreetmap', // Default map provider
    },
    
    /**
     * Application state 
     * @memberof GPSTracker
     * @type {Object}
     * @property {Object|null} map - Leaflet map instance
     * @property {Object|null} baseLayer - Current base layer for the map
     * @property {HTMLElement|null} routeSelect - Reference to the route dropdown element
     * @property {boolean} autoRefresh - Whether auto-refresh is enabled
     * @property {number} intervalId - ID of the current auto-refresh interval
     * @property {Array<string>} sessionIdArray - Array of session IDs
     * @property {boolean} viewingAllRoutes - Whether currently viewing all routes
     * @property {Array<Object>} currentLayers - Array of current map layers
     * @property {boolean} isLoadingRoutes - Flag to prevent concurrent route loading
     */
    state: {
        map: null,
        baseLayer: null,
        routeSelect: null,
        autoRefresh: false,
        intervalId: 0,
        sessionIdArray: [],
        viewingAllRoutes: false,
        currentLayers: [],
        isLoadingRoutes: false // Flag to prevent concurrent route loading
    },
    
    /**
     * DOM elements cache
     * @memberof GPSTracker
     * @type {Object}
     */
    elements: {},
    
    /**
     * Initialize the application
     * 
     * Sets up the application by:
     * - Caching DOM elements
     * - Loading routes into dropdown menu
     * - Setting up event listeners
     * - Initializing the map
     * - Loading initial route data
     * 
     * @memberof GPSTracker
     * @function init
     * @returns {void}
     */
    init() {
        try {
            console.log('GPS Tracker initialization started');
            
            // Cache DOM elements
            this.cacheElements();
            
            // Load routes into dropdown
            this.loadRoutesIntoDropdownBox();
            
            // Set up event listeners
            this.setupEventListeners();
            
            // Initialize map structure (does not load data)
            this.safeInitMap();
            
            // Trigger initial data load after map structure is ready
            console.log('>>> init: Calling getAllRoutesForMap for initial load...');
            this.getAllRoutesForMap();

            console.log('GPS Tracker initialization completed');
        } catch (error) {
            console.error('Error initializing GPS Tracker:', error);
            this.showMessage('Initialization error: ' + error.message);
        }
    },
    
    /**
     * Safely initialize the map structure only once
     * 
     * Creates the initial Leaflet map instance without loading any route data.
     * Ensures the map is created only once to prevent memory leaks and errors.
     * 
     * @memberof GPSTracker
     * @function safeInitMap
     * @returns {void}
     */
    safeInitMap() {
        // Basic guard: If map object already exists in state, assume it's initialized.
        if (this.state.map) {
            console.warn('(safeInitMap) Map instance already exists in state. Skipping initialization.');
            return;
        }

        try {
            console.log('Proceeding with initial map structure initialization...');
            
            // Check if Leaflet is available
            if (typeof L === 'undefined') {
                console.error('Error: Leaflet library is not loaded');
                this.showMessage('Error: Map library is not available');
                return;
            }
            
            // Check if the map element exists
            if (!this.elements.mapCanvas) {
                 // Attempt to cache again if needed
                 this.cacheElements();
                 if (!this.elements.mapCanvas) {
                      console.error('Error: Map canvas element not found even after re-cache.');
                      this.showMessage('Error: Map container missing.');
                      return;
                 }
            }
            
            // Set default center/zoom for initial view
            const defaultCenter = [47.6062, -122.3321]; // Example: Seattle
            const defaultZoom = 13; 
            
            // Create the initial map instance
            let initialMapInstance = null;
            try {
                // Initialize map centered on default location
                initialMapInstance = L.map('map-canvas').setView(defaultCenter, defaultZoom); 
                console.log('Initial L.map() call successful.');
            } catch (mapCreationError) {
                console.error('*** CRITICAL ERROR CREATING INITIAL MAP INSTANCE ***', mapCreationError);
                this.showMessage('Critical error creating map: ' + mapCreationError.message);
                return; 
            }

            // Assign to state only AFTER successful creation
            this.state.map = initialMapInstance;
            console.log('Initial map instance assigned to this.state.map.');
            
            // Add base map layers (OSM primarily) to the initial instance
            this.addMapLayers(); 

            console.log('Initial map structure initialized successfully');
        } catch (e) {
            console.error('Error in safeInitMap:', e);
            this.showMessage('Error initializing map: ' + e.message);
        }
    },
    
    /**
     * Cache frequently used DOM elements
     * 
     * Stores references to commonly used DOM elements to avoid
     * repeated DOM queries and improve performance.
     * 
     * @memberof GPSTracker
     * @function cacheElements
     * @returns {void}
     */
    cacheElements() {
        this.elements = {
            routeSelect: document.getElementById('routeSelect'),
            mapCanvas: document.getElementById('map-canvas'),
            messages: document.getElementById('messages'),
            deleteBtn: document.getElementById('delete'),
            refreshBtn: document.getElementById('refresh'),
            autoRefreshBtn: document.getElementById('autorefresh'),
            viewAllBtn: document.getElementById('viewall'),
        };
        
        this.state.routeSelect = this.elements.routeSelect; // Keep separate reference if needed
        console.log("DOM elements cached.");
    },
    
    /**
     * Get the map provider from configuration or HTML data attribute
     * 
     * Determines which map provider to use (OpenStreetMap or Google Maps)
     * based on HTML data attributes or config defaults.
     * 
     * @memberof GPSTracker
     * @function getMapProvider
     * @returns {string} The map provider name to use
     */
    getMapProvider() {
        // Attempt to get from the current mapCanvas element, fallback to config
        const canvas = this.elements.mapCanvas || document.getElementById('map-canvas');
        const mapProviderAttr = canvas?.getAttribute('data-map-provider');
        return mapProviderAttr || this.config.defaultMapProvider;
    },
    
    /**
     * Add map layers to the current map instance
     * 
     * Adds base map layers (OpenStreetMap, Google Maps if available) to
     * the specified or current map instance.
     * 
     * @memberof GPSTracker
     * @function addMapLayers
     * @param {Object} [mapInstance] - Optional map instance to add layers to (defaults to state.map)
     * @returns {void}
     */
    addMapLayers(mapInstance) {
        // Use passed instance if provided (from loadGPSLocations), else use state (from safeInitMap)
        const currentMap = mapInstance || this.state.map; 
        
        // Guard: Ensure a map instance exists
        if (!currentMap) {
             console.error('(addMapLayers) No map instance available to add layers to.');
             return;
        }

        try {
            console.log('(addMapLayers) Adding map layers...');
            
            // OpenStreetMap layer
            const protocol = document.location.protocol === 'https:' ? 'https://' : 'http://';
            const openStreetMapsURL = `${protocol}{s}.tile.openstreetmap.org/{z}/{x}/{y}.png`;
            const openStreetMapsLayer = L.tileLayer(openStreetMapsURL, {
                attribution: '&copy;' + new Date().getFullYear() + ' <a href="http://openstreetmap.org">OpenStreetMap</a> contributors',
                maxZoom: 19
            });
            
            // Add event listeners (optional)
            openStreetMapsLayer.on('loading', () => console.log('OSM: Loading tiles...'));
            openStreetMapsLayer.on('load', () => console.log('OSM: Tiles loaded.'));
            openStreetMapsLayer.on('tileerror', (error) => {
                 console.error('OSM: Error loading tile', error);
                 this.showMessage('Error loading map tiles');
            });
            
            // Create Google Maps layer if available (Optional)
            let googleMapsLayer = null;
            try {
                // Use the new GoogleMutant plugin
                if (typeof L.gridLayer.googleMutant !== 'undefined') {
                    googleMapsLayer = L.gridLayer.googleMutant({
                        type: 'roadmap' // Can be 'roadmap', 'satellite', 'hybrid', 'terrain'
                    });
                    console.log('(addMapLayers) Google Maps Mutant layer created.');
                } else {
                    console.warn('(addMapLayers) L.gridLayer.googleMutant not defined, skipping Google Maps layer.');
                }
            } catch (e) {
                console.error('(addMapLayers) Error creating Google Maps layer:', e);
            }
            
            // Determine default provider
            const mapProvider = this.getMapProvider();
            console.log('(addMapLayers) Default map provider:', mapProvider);
            
            // Add layer control ONLY if Google layer was successfully created
            if (googleMapsLayer) {
                 const baseLayers = mapProvider === 'google' 
                     ? {'Google Maps': googleMapsLayer, 'OpenStreetMap': openStreetMapsLayer}
                     : {'OpenStreetMap': openStreetMapsLayer, 'Google Maps': googleMapsLayer};
                 // currentMap.addControl(new L.Control.Layers(baseLayers, {}));
                 console.log('(addMapLayers) Layer control added.');
            }

            // Set default layer
            if (mapProvider === 'google' && googleMapsLayer) {
                console.log('(addMapLayers) Adding Google Maps as default layer.');
                currentMap.addLayer(googleMapsLayer);
            } else {
                // Default to OpenStreetMap
                console.log('(addMapLayers) Adding OpenStreetMap as default layer.');
                currentMap.addLayer(openStreetMapsLayer);
            }
            
            // Fix potential zoom button freeze issue (from original code)
            L.polyline([[0, 0], ]).addTo(currentMap);

            console.log('(addMapLayers) Map layers setup complete.');
        } catch (e) {
            console.error('(addMapLayers) Error adding map layers:', e);
            this.showMessage('Error initializing map layers: ' + e.message);
        }
    },
    
    /**
     * Set up event listeners for UI elements
     * 
     * Attaches event handlers to UI controls for user interactions.
     * Includes handlers for:
     * - Route selection dropdown
     * - Refresh button
     * - Delete button
     * - Auto-refresh toggle
     * - View all routes button
     * 
     * @memberof GPSTracker
     * @function setupEventListeners
     * @returns {void}
     */
    setupEventListeners() {
        // Ensure elements are cached
        if (!this.elements.routeSelect) this.cacheElements(); 
        
        // Use event delegation or ensure elements exist before adding listeners
        if (this.elements.routeSelect) {
            this.elements.routeSelect.addEventListener('change', () => {
                if (this.hasRouteSelected()) {
                    this.state.viewingAllRoutes = false;
                    this.getRouteForMap();
                }
            });
        }
        if (this.elements.refreshBtn) {
            this.elements.refreshBtn.addEventListener('click', () => {
                if (this.state.viewingAllRoutes) {
                    this.getAllRoutesForMap(); 
                } else if (this.hasRouteSelected()) {
                    this.getRouteForMap();
                }             
            });
        }
        if (this.elements.deleteBtn) {
            this.elements.deleteBtn.addEventListener('click', () => {
                // this.deleteRoute();
				alert('Deleting has been turned off on this website.');
            });       
        }
        if (this.elements.autoRefreshBtn) {
            this.elements.autoRefreshBtn.addEventListener('click', () => { 
                if (this.state.autoRefresh) {
                    this.turnOffAutoRefresh();           
                } else {
                    this.turnOnAutoRefresh();                     
                }
            }); 
        }
        if (this.elements.viewAllBtn) {
            this.elements.viewAllBtn.addEventListener('click', () => {
                this.getAllRoutesForMap();
            });
        }
        console.log('Event listeners set up.');
    },
    
    /**
     * Check if a route is selected in the dropdown
     * 
     * @memberof GPSTracker
     * @function hasRouteSelected
     * @returns {boolean} True if a route is selected (not the default option)
     */
    hasRouteSelected() {
        return this.elements.routeSelect && this.elements.routeSelect.selectedIndex !== 0;
    },

   /**
    * Get all routes for map display
    * 
    * Fetches and displays all routes on the map, showing the
    * last location of each route with a marker.
    * 
    * @memberof GPSTracker
    * @function getAllRoutesForMap
    * @returns {void}
    * @fires API:GET:/api/routes/all
    */
   GPSTracker.getAllRoutesForMap = function() {
       // Prevent concurrent execution
       if (this.state.isLoadingRoutes) {
           console.warn('(getAllRoutesForMap) Request blocked: Already loading routes.');
           return;
       }
       this.state.isLoadingRoutes = true; // Set flag

       this.state.viewingAllRoutes = true;
       if (this.elements.routeSelect) this.elements.routeSelect.selectedIndex = 0;
       this.showPermanentMessage('Please select a route below');
    
       console.log('Fetching all routes data from API...');
       $.ajax({
           url: 'api/routes/all', // Using modern API path
           type: 'GET',
           dataType: 'json',
           context: this, // Ensure 'this' refers to GPSTracker in callbacks
           success: (data) => {
               console.log('Successfully retrieved all routes data', data);
               this.loadGPSLocations(data);
           },
           error: (xhr, status, error) => {
               console.error('Error fetching all routes:', error);
               console.error('Response:', xhr.responseText);
               this.showMessage('Error loading routes: ' + error);
           },
           complete: () => {
               this.state.isLoadingRoutes = false; // Reset flag when request finishes
               console.log('(getAllRoutesForMap) Request complete. isLoadingRoutes set to false.');
           }
       });
   };

   /**
    * Load routes into dropdown select box
    * 
    * Populates the route selection dropdown with available routes from the API.
    * 
    * @memberof GPSTracker
    * @function loadRoutesIntoDropdownBox
    * @returns {void}
    * @fires API:GET:/api/routes
    */
   GPSTracker.loadRoutesIntoDropdownBox = function() {      
       console.log('Loading routes into dropdown...');
       $.ajax({
           url: 'api/routes', // Using modern API path
           type: 'GET',
           dataType: 'json',
           context: this, // Ensure 'this' refers to GPSTracker
           success: function(data) { // Use standard function to ensure 'this' from context works
               console.log('Successfully retrieved routes for dropdown', data);
               this.loadRoutes(data); // 'this' should be GPSTracker here
           },
           error: function (xhr, status, errorThrown) {
               console.error('Error fetching routes:', errorThrown);
               console.error('Response:', xhr.responseText);
               // 'this' might not be GPSTracker here, access directly if needed
               GPSTracker.showMessage('Error loading route list: ' + errorThrown); 
           }
       });
   };    

   /**
    * Process routes data and populate the dropdown
    * 
    * Takes the API response and creates dropdown options for each route.
    * 
    * @memberof GPSTracker
    * @function loadRoutes
    * @param {Object} data - Response data from the routes API
    * @param {Array} data.routes - Array of route objects
    * @returns {void}
    */
   GPSTracker.loadRoutes = function(data) {        
       // Ensure routeSelect element exists
       if (!this.elements.routeSelect) {
            console.error("(loadRoutes) routeSelect element not found.");
            this.cacheElements(); // Try caching again
            if (!this.elements.routeSelect) return; // Give up if still not found
       }
       // Clear existing options
       this.elements.routeSelect.innerHTML = '';

       if (!data || !data.routes || data.routes.length === 0) {
           this.showPermanentMessage('There are no routes available to view');
       } else {
           // Create the first option
           const defaultOption = document.createElement('option');
           defaultOption.value = '0';
           defaultOption.textContent = 'Select Route...';
           this.elements.routeSelect.appendChild(defaultOption);

           // Reset session ID array
           this.state.sessionIdArray = [];
        
           // Add route options
           data.routes.forEach(route => {
               const option = document.createElement('option');
               // Use just sessionID as value, consistent with getRouteForMap
               option.value = route.sessionID; 
               this.state.sessionIdArray.push(route.sessionID);
               option.textContent = route.userName + " " + route.times;
               this.elements.routeSelect.appendChild(option);
           });

           console.log('Added routes to dropdown:', this.state.sessionIdArray);
        
           // Reset selection
           this.elements.routeSelect.selectedIndex = 0;
        
           this.showPermanentMessage('Please select a route below');
       }
   };

   /**
    * Get a specific route for map display
    * 
    * Fetches and displays all location points for a specific route.
    * 
    * @memberof GPSTracker
    * @function getRouteForMap
    * @returns {void}
    * @fires API:GET:/api/routes/detail
    */
   GPSTracker.getRouteForMap = function() { 
       if (this.hasRouteSelected()) {
           // Prevent concurrent execution
           if (this.state.isLoadingRoutes) {
               console.warn('(getRouteForMap) Request blocked: Already loading routes.');
               return;
           }
           this.state.isLoadingRoutes = true; // Set flag

           const sessionId = this.elements.routeSelect.value; // Value is now just sessionID
           console.log('Fetching specific route data for session:', sessionId);

           $.ajax({
                  url: 'api/routes/detail', // Using modern API path
                  type: 'GET',
                  data: { sessionid: sessionId }, // Send sessionid as data
                  dataType: 'json',
                  context: this, // Ensure 'this' is GPSTracker
                  success: function(data) {
                     console.log('Successfully retrieved route data for sessionId:', sessionId);
                     this.loadGPSLocations(data);
                  },
                  error: function (xhr, status, errorThrown) {
                      console.error('Error fetching route:', errorThrown);
                      console.error('Response:', xhr.responseText);
                      GPSTracker.showMessage('Error loading route: ' + errorThrown);
                  },
                  complete: () => {
                      // Use arrow function for 'this' context or access directly
                      GPSTracker.state.isLoadingRoutes = false; 
                      console.log('(getRouteForMap) Request complete. isLoadingRoutes set to false.');
                  }
              });
       } 
   };

   /**
    * Load GPS locations onto the map
    * 
    * Processes location data and displays it on the map.
    * Uses a destroy/recreate method for the map to ensure clean rendering.
    * 
    * @memberof GPSTracker
    * @function loadGPSLocations
    * @param {Object} data - Location data from API
    * @param {Array} data.locations - Array of location objects
    * @returns {void}
    */
   GPSTracker.loadGPSLocations = function(data) {
       console.log('(loadGPSLocations) Received data:', data);

       // Check if data or locations array is missing/empty
       if (!data || !data.locations || data.locations.length === 0) {
           console.warn('(loadGPSLocations) No location data found. Clearing map container.');
           this.showPermanentMessage('There is no tracking data to view');
           const mapCanvas = document.getElementById('map-canvas');
           if (mapCanvas) {
                mapCanvas.innerHTML = ''; // Clear content if no data
           }
           // Clear the map state if it exists from a previous load
           if (this.state.map) {
               try { this.state.map.remove(); } catch(e) { console.error("Error removing previous map state:", e); }
               this.state.map = null;
           }
           return;
       }

       // --- Implement Original Destroy/Recreate Logic ---
       console.log('(loadGPSLocations) Destroying and recreating map container...');
       const mapContainer = document.getElementById('map-canvas'); 
       if (!mapContainer) {
           console.error('(loadGPSLocations) Cannot find map-canvas element to replace!');
           this.showMessage('Error: Map container element missing.');
           return;
       }
       // Store data attribute before destroying
       const mapProviderAttr = mapContainer.getAttribute('data-map-provider');
       // Replace the container element itself
       mapContainer.outerHTML = `<div id="map-canvas" data-map-provider="${mapProviderAttr || 'openstreetmap'}"></div>`;
       // Re-cache the new element (important!)
       this.elements.mapCanvas = document.getElementById('map-canvas'); 
       if (!this.elements.mapCanvas) {
            console.error('(loadGPSLocations) Failed to find map-canvas element AFTER recreating!');
            this.showMessage('Error: Failed to recreate map container.');
            return;
       }
       console.log('(loadGPSLocations) Map container replaced.');

       // --- Initialize Leaflet on the NEW container ---
       let currentMapInstance; // Use local variable for this scope
       try {
           console.log('(loadGPSLocations) Initializing Leaflet on new container...');
           // Set default center/zoom for context if needed before fitBounds
           const defaultCenter = [47.6062, -122.3321];
           const defaultZoom = 5; // Lower zoom initially
           currentMapInstance = L.map('map-canvas').setView(defaultCenter, defaultZoom); 
        
           // Add base layers and controls using the dedicated function
           this.addMapLayers(currentMapInstance); 

       } catch (initError) {
           console.error('(loadGPSLocations) Error initializing Leaflet on new container:', initError);
           this.showMessage('Error creating map: ' + initError.message);
           // Clear state if initialization failed
           this.state.map = null; 
           return;
       }
    
       // --- Update Application State ---
       this.state.map = currentMapInstance; // Store the NEW map instance
       console.log('(loadGPSLocations) New map instance stored in state.');

       // --- Filter Locations based on view mode ---
       let locationsToProcess = [];
       if (this.state.viewingAllRoutes) {
           console.log('(loadGPSLocations) Viewing all routes. Filtering for last location per session...');
           const lastLocations = {};
           // Assuming locations are roughly time-ordered, last one encountered wins
           data.locations.forEach(loc => {
               if (loc && loc.sessionID) { // Basic validation
                    lastLocations[loc.sessionID] = loc;
               }
           });
           locationsToProcess = Object.values(lastLocations);
           console.log(`(loadGPSLocations) Filtered down to ${locationsToProcess.length} last locations.`);
       } else {
           // Viewing a single route, process all its locations
           locationsToProcess = data.locations;
       }

       // --- Process Locations and Add Markers (NO Path) ---
       const locationArrayForBounds = []; // LatLng objects for bounds calculation
       // Create a NEW layer group for markers for THIS map instance
       const routeLayer = L.layerGroup();
       this.state.currentLayers = [routeLayer]; // Reset/track layers for this instance

       console.log(`(loadGPSLocations) Processing ${locationsToProcess.length} locations for display...`);
       locationsToProcess.forEach((location, index) => {
           // Use parseFloat for safety
           const latitude = parseFloat(location.latitude);
           const longitude = parseFloat(location.longitude);

           // Check for valid numbers
           if (isNaN(latitude) || isNaN(longitude)) {
               console.warn(`(loadGPSLocations) Invalid coordinates:`, location);
               return; // Skip invalid point
           }

           const latLng = new L.LatLng(latitude, longitude);
           locationArrayForBounds.push(latLng); // Add valid point for bounds

           // Determine if this is the final location *for the current view*
           // If viewing all, every marker IS the final one for its route.
           // If viewing single, only the last in the array is final.
           const isFinalLocationForView = this.state.viewingAllRoutes || (index === locationsToProcess.length - 1);

           // Display city name only if viewing single route and it's the last point
           if (!this.state.viewingAllRoutes && isFinalLocationForView) {
               if (typeof this.displayCityName === 'function') {
                    try { this.displayCityName(latitude, longitude); } catch(e) { console.error("Error calling displayCityName:", e); }
               }
           }

           // Use createMarker, passing the layer group and correct finalLocation flag
            if (typeof this.createMarker === 'function') {
                try {
                    this.createMarker(
                        latitude, longitude,
                        location.speed, location.direction, location.distance,
                        location.locationMethod, location.gpsTime, location.userName,
                        location.sessionID, location.accuracy, location.extraInfo,
                        isFinalLocationForView, // Pass the calculated flag
                        routeLayer // Pass layer group
                    );
                } catch(e) { console.error("Error calling createMarker:", e); }
            }
       });
    
       // --- Remove Polyline Code ---
       // The block creating pathPoints and L.polyline is intentionally removed.

       // Add the layer group containing markers to the map
       routeLayer.addTo(currentMapInstance);
       console.log('(loadGPSLocations) Route layer group (markers only) added to map.');

       // Fit map to bounds if valid locations exist
       if (locationArrayForBounds.length > 0) {
           try {
               const bounds = new L.LatLngBounds(locationArrayForBounds);
               // Add padding to prevent markers being right on the edge
               // Use slightly more padding if only one point
               const padding = locationArrayForBounds.length > 1 ? [50, 50] : [100, 100];
               currentMapInstance.fitBounds(bounds, { padding: padding });
               console.log('(loadGPSLocations) Map bounds fitted.');
           } catch (boundsError) {
               console.error('(loadGPSLocations) Error fitting map bounds:', boundsError);
               // Fallback: Set view to the first point
               if (locationArrayForBounds.length > 0) {
                   currentMapInstance.setView(locationArrayForBounds[0], this.config.mapDefaultZoom);
               }
           }
       } else {
            console.warn("(loadGPSLocations) No valid locations processed to fit bounds.");
            // Optionally set a default view if map exists but no points?
            // currentMapInstance.setView(defaultCenter, defaultZoom); 
       }

       // Restart interval if auto-refresh is on
       if (this.state.autoRefresh) {
           this.restartInterval();
       }
       console.log('(loadGPSLocations) Finished processing locations.');
   };

   /**
    * Clear all markers and paths from the map
    * 
    * Removes all layers from the map and clears the layer references.
    * With the destroy/recreate approach, this mainly clears the state references.
    * 
    * @memberof GPSTracker
    * @function clearMapLayers
    * @returns {void}
    */
   GPSTracker.clearMapLayers = function() {
       // Only needs to clear the state array now
       console.log(`(clearMapLayers) Clearing ${this.state.currentLayers.length} layer references from state.`);
       this.state.currentLayers = []; 
   };

  /**
   * Create a map marker and add it to the provided layer group
   * 
   * Creates a customized marker with icon and popup based on location data.
   * 
   * @memberof GPSTracker
   * @function createMarker
   * @param {number} latitude - Latitude coordinate
   * @param {number} longitude - Longitude coordinate
   * @param {number} speed - Speed in mph
   * @param {number} direction - Direction in degrees
   * @param {number} distance - Distance traveled
   * @param {string} locationMethod - Method used to determine location
   * @param {string} gpsTime - Timestamp when the location was recorded
   * @param {string} userName - Username or device name
   * @param {string} sessionID - Session identifier
   * @param {number} accuracy - Location accuracy in meters
   * @param {string} extraInfo - Additional information
   * @param {boolean} finalLocation - Whether this is the final location in a route
   * @param {Object} layerGroup - Leaflet layer group to add the marker to
   * @returns {void}
   */
  GPSTracker.createMarker = function(latitude, longitude, speed, direction, distance, locationMethod, 
               gpsTime, userName, sessionID, accuracy, extraInfo, finalLocation, layerGroup) {
                 
      // Choose icon based on whether this is the final location
      const iconUrl = finalLocation ? 'assets/images/coolred_small.png' : 'assets/images/coolgreen2_small.png';
    
      // Create marker icon
      const markerIcon = new L.Icon({
          iconUrl: iconUrl,
          shadowUrl: 'assets/images/coolshadow_small.png',
          iconSize: [12, 20],
          shadowSize: [22, 20],
          iconAnchor: [6, 20],
          shadowAnchor: [6, 20],
          popupAnchor: [-3, -25]
      });
    
      // Convert accuracy from meters to feet for display
      // Use parseFloat for safety, default to 0 if invalid
      const accuracyNum = parseFloat(accuracy);
      const accuracyFeet = isNaN(accuracyNum) ? 'N/A' : parseInt(accuracyNum * 3.28);
    
      // Get the compass image filename first
      const compassImage = this.getCompassImage(direction); // Evaluate the function call

      // Create popup content (using template literal for readability)
      const popupContent = `
          <table class="popup-table">
              <tr>
                  <td class="popup-label">Speed:</td>
                  <td>${speed || 'N/A'} mph</td>
                  <td rowspan="2" style="text-align: right;">
                      <img src="assets/images/${compassImage}.jpg" alt="Compass" /> 
                  </td>
              </tr>
              <tr>
                  <td class="popup-label">Distance:</td>
                  <td>${distance || 'N/A'} mi</td>
              </tr>
              <tr>
                  <td class="popup-label">Time:</td>
                  <td colspan="2">${gpsTime || 'N/A'}</td>
              </tr>
              <tr>
                  <td class="popup-label">Name:</td>
                  <td colspan="2">${userName || 'N/A'}</td>
              </tr>
              <tr>
                  <td class="popup-label">Accuracy:</td>
                  <td colspan="2">${accuracyFeet} ft</td>
              </tr>
              ${finalLocation ? '<tr><td colspan="3" class="popup-final-location">Final location</td></tr>' : ''}
          </table>
      `;
    
      // Create marker options
      const markerOptions = {
           title: `${userName || 'User'} - ${gpsTime || 'Time'}`,
           icon: markerIcon,
           zIndexOffset: finalLocation ? 999 : 0 // Ensure final marker is on top
      };

      // Create marker
      const marker = L.marker(new L.LatLng(latitude, longitude), markerOptions);
    
      // Add to layer group (important change from original)
      layerGroup.addLayer(marker);
    
      // If viewing all routes, make markers clickable to show that route
      if (this.state.viewingAllRoutes) {
          marker.unbindPopup(); // Remove popup binding if viewing all
          marker.on('click', () => {
              // Ensure handler exists
              if (typeof this.handleAllRoutesMarkerClick === 'function') {
                   this.handleAllRoutesMarkerClick(sessionID);
              }
          });
      } else {
          // Otherwise, bind the popup
          marker.bindPopup(popupContent);
      }
  };

  /**
   * Handle click on a marker when viewing all routes
   * 
   * When in "all routes" view, clicking a marker loads the full route
   * for that marker's session.
   * 
   * @memberof GPSTracker
   * @function handleAllRoutesMarkerClick
   * @param {string} sessionID - Session ID of the clicked marker
   * @returns {void}
   * @fires API:GET:/api/routes/detail
   */
  GPSTracker.handleAllRoutesMarkerClick = function(sessionID) {
      // Prevent concurrent execution if user clicks rapidly
      if (this.state.isLoadingRoutes) {
          console.warn('(handleAllRoutesMarkerClick) Request blocked: Already loading routes.');
          return;
      }
      this.state.isLoadingRoutes = true; // Set flag

      this.state.viewingAllRoutes = false;
      console.log('Marker clicked, loading route for session:', sessionID);
    
      // Find the index of the route in the dropdown
      const indexOfRoute = this.state.sessionIdArray.indexOf(sessionID) + 1;
      if (this.elements.routeSelect && indexOfRoute > 0) {
           this.elements.routeSelect.selectedIndex = indexOfRoute;
      } else {
           console.warn("Could not find route in dropdown for session:", sessionID);
      }
    
      // Load the specific route - Make the AJAX call directly like the original
      $.ajax({
          url: 'api/routes/detail', // Modern API path
          type: 'GET',
          data: { sessionid: sessionID },
          dataType: 'json',
          context: this, // Ensure 'this' is GPSTracker
          success: (data) => {
              console.log('(handleAllRoutesMarkerClick) Successfully retrieved route data.');
              this.loadGPSLocations(data); // Reload map with this route's data
          },
          error: (xhr, status, error) => {
              console.error('Error fetching route on marker click:', error);
              console.error('Response:', xhr.responseText);
              this.showMessage('Error loading route: ' + error);
          },
          complete: () => {
               // Use arrow function or GPSTracker directly for context
               GPSTracker.state.isLoadingRoutes = false; 
               console.log('(handleAllRoutesMarkerClick) Request complete.');
          }
      });
  };

  /**
   * Get compass image based on direction
   * 
   * Determines the appropriate compass image filename based on
   * the direction in degrees.
   * 
   * @memberof GPSTracker
   * @function getCompassImage
   * @param {number} azimuth - Direction in degrees (0-360)
   * @returns {string} Compass image filename without extension
   */
  GPSTracker.getCompassImage = function(azimuth) {
      // Use parseFloat for safety, default to 0
      const direction = parseFloat(azimuth) || 0;
    
      if ((direction >= 337 && direction <= 360) || (direction >= 0 && direction < 23))
          return 'compassN';
      if (direction >= 23 && direction < 68)
          return 'compassNE';
      if (direction >= 68 && direction < 113)
          return 'compassE';
      if (direction >= 113 && direction < 158)
          return 'compassSE';
      if (direction >= 158 && direction < 203)
          return 'compassS';
      if (direction >= 203 && direction < 248)
          return 'compassSW';
      if (direction >= 248 && direction < 293)
          return 'compassW';
      if (direction >= 293 && direction < 337)
          return 'compassNW';
        
      return 'compassN'; // Default fallback
  };

  /**
   * Display city name using reverse geocoding
   * 
   * Uses Google Maps geocoding API to get a readable location name
   * for the coordinates and displays it in the message area.
   * 
   * @memberof GPSTracker
   * @function displayCityName
   * @param {number} latitude - Latitude coordinate
   * @param {number} longitude - Longitude coordinate
   * @returns {void}
   */
  GPSTracker.displayCityName = function(latitude, longitude) {
      try {
          // Check if Google Maps API is available
          if (typeof google === 'undefined' || typeof google.maps === 'undefined' || 
              typeof google.maps.Geocoder === 'undefined') {
              console.warn('Google Maps API not available for geocoding');
              return;
          }
        
          const latlng = new google.maps.LatLng(latitude, longitude);
          const geocoder = new google.maps.Geocoder();
        
          geocoder.geocode({ 'latLng': latlng }, (results, status) => {
              if (status === google.maps.GeocoderStatus.OK && results[1]) {
                  const address = results[1].formatted_address;
                  this.showPermanentMessage(address);
              } else {
                  console.warn('Geocoder failed:', status);
                  // Optionally clear message or show default?
                  // this.showPermanentMessage('Location lookup unavailable'); 
              }
          });
      } catch (e) {
          console.error('Error in reverse geocoding:', e);
          this.showMessage('Error looking up location name.');
      }
  };

  /**
   * Turn off auto-refresh
   * 
   * Disables automatic refreshing of routes.
   * 
   * @memberof GPSTracker
   * @function turnOffAutoRefresh
   * @returns {void}
   */
  GPSTracker.turnOffAutoRefresh = function() {
      this.showMessage('Auto Refresh Off');
      if (this.elements.autoRefreshBtn) {
           this.elements.autoRefreshBtn.textContent = 'Auto Refresh Off';
           this.elements.autoRefreshBtn.classList.remove('btn-success');
           this.elements.autoRefreshBtn.classList.add('btn-primary');
      }
      this.state.autoRefresh = false;
      clearInterval(this.state.intervalId);
      this.state.intervalId = 0; // Explicitly clear ID
      console.log("Auto-refresh turned OFF.");
  };

  /**
   * Turn on auto-refresh
   * 
   * Enables automatic refreshing of routes at regular intervals.
   * 
   * @memberof GPSTracker
   * @function turnOnAutoRefresh
   * @returns {void}
   */
  GPSTracker.turnOnAutoRefresh = function() {
      this.showMessage('Auto Refresh On (1 min)');
      if (this.elements.autoRefreshBtn) {
           this.elements.autoRefreshBtn.textContent = 'Auto Refresh On';
           this.elements.autoRefreshBtn.classList.remove('btn-primary');
           this.elements.autoRefreshBtn.classList.add('btn-success');
      }
      this.state.autoRefresh = true;
      this.restartInterval(); // Start the interval immediately
      console.log("Auto-refresh turned ON.");
  };

  /**
   * Restart the auto-refresh interval
   * 
   * Clears any existing interval and creates a new one based
   * on the current view state.
   * 
   * @memberof GPSTracker
   * @function restartInterval
   * @returns {void}
   */
  GPSTracker.restartInterval = function() {
      clearInterval(this.state.intervalId); // Clear any existing interval
      this.state.intervalId = 0; 
    
      if (!this.state.autoRefresh) {
           console.log("(restartInterval) Auto-refresh is off, not starting interval.");
           return;
      }

      const refreshFunction = this.state.viewingAllRoutes 
                             ? this.getAllRoutesForMap 
                             : this.getRouteForMap;
    
      // Bind 'this' context for the interval function
      const boundRefreshFunction = refreshFunction.bind(this); 

      console.log(`(restartInterval) Setting interval for ${this.state.viewingAllRoutes ? 'getAllRoutesForMap' : 'getRouteForMap'}`);
      this.state.intervalId = setInterval(boundRefreshFunction, this.config.autoRefreshInterval);
  };

  /**
   * Delete a route
   * 
   * Removes a route from the database after confirmation.
   * 
   * @memberof GPSTracker
   * @function deleteRoute
   * @returns {void}
   * @fires API:POST:/api/routes/delete
   */
  GPSTracker.deleteRoute = function() {
      if (!this.hasRouteSelected()) {
          alert('Please select a route before trying to delete.');
          return;
      }
    
      // Add confirmation
      const confirmed = confirm('This will permanently delete this route from the database. Do you want to delete?');
      if (!confirmed) {
          return;
      }
    
      const sessionId = this.elements.routeSelect.value;
      console.log("Attempting to delete route with session ID:", sessionId);
    
      $.ajax({
          url: 'api/routes/delete', // Modern API path
          type: 'POST', // Use POST for delete operations
          data: { sessionid: sessionId },
          context: this, // Ensure 'this' is GPSTracker
          success: () => {
              console.log("Delete successful for session ID:", sessionId);
              this.deleteRouteResponse(); // Handle UI updates
          },
          error: (xhr, status, error) => {
              console.error('Error deleting route:', error);
              console.error('Response:', xhr.responseText);
              this.showMessage('Error deleting route: ' + error);
          }
      });
  };

  /**
   * Handle successful route deletion response
   * 
   * Updates the UI after a route has been successfully deleted.
   * 
   * @memberof GPSTracker
   * @function deleteRouteResponse
   * @returns {void}
   */
  GPSTracker.deleteRouteResponse = function() {
      console.log("(deleteRouteResponse) Handling UI after delete.");
      // Clear route select dropdown
      if (this.elements.routeSelect) {
           this.elements.routeSelect.innerHTML = '';
      }
    
      // Clear map container content (map instance is destroyed by subsequent load)
      const mapCanvas = document.getElementById('map-canvas');
      if (mapCanvas) {
           mapCanvas.innerHTML = ''; 
      }
      if (this.state.map) { // Also clear state map if exists
           try { this.state.map.remove(); } catch(e) {}
           this.state.map = null;
      }
      this.state.currentLayers = []; // Clear layers state

      // Reload routes into dropdown and show default "all routes" view
      this.loadRoutesIntoDropdownBox(); 
      // Add a slight delay before loading all routes to allow dropdown to potentially populate
      setTimeout(() => {
           console.log("(deleteRouteResponse) Triggering getAllRoutesForMap after delete.");
           this.getAllRoutesForMap(); 
      }, 100); // Small delay
  };

  /**
   * Show a temporary message
   * 
   * Displays a message for a short time before reverting to the previous message.
   * 
   * @memberof GPSTracker
   * @function showMessage
   * @param {string} message - Message to display
   * @returns {void}
   */
  GPSTracker.showMessage = function(message) {
      if (!this.elements.messages) this.cacheElements(); // Try caching if missing
      if (!this.elements.messages) return; // Guard if still missing

      const tempMessage = this.elements.messages.innerHTML;
    
      this.elements.messages.innerHTML = message;
    
      // Use a property to store the timeout ID so we can clear it if needed
      if (this.messageTimeoutId) {
           clearTimeout(this.messageTimeoutId);
      }
      this.messageTimeoutId = setTimeout(() => {
          // Only restore if the message hasn't changed again
          if (this.elements.messages.innerHTML === message) {
               this.elements.messages.innerHTML = tempMessage;
          }
          this.messageTimeoutId = null;
      }, this.config.errorTimeout);
  };

  /**
   * Show a permanent message
   * 
   * Displays a message that remains until explicitly changed.
   * 
   * @memberof GPSTracker
   * @function showPermanentMessage
   * @param {string} message - Message to display
   * @returns {void}
   */
  GPSTracker.showPermanentMessage = function(message) {
      if (!this.elements.messages) this.cacheElements(); // Try caching if missing
      if (!this.elements.messages) return; // Guard if still missing

      // Clear any pending temporary message timeout
      if (this.messageTimeoutId) {
           clearTimeout(this.messageTimeoutId);
           this.messageTimeoutId = null;
      }
      this.elements.messages.innerHTML = message;
  };

  // NOTE: For initializing the application, use $(document).ready() in the main HTML template.
  // The original global initializeMap function was removed and replaced with object initialization.

 