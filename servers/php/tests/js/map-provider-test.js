/**
 * Test suite for the OpenStreetMap provider functionality
 * 
 * These tests can be run in a browser or with a JavaScript testing framework like Jest
 */

// Mock DOM elements
const mockDOM = () => {
    // Create map canvas element
    const mapCanvas = document.createElement('div');
    mapCanvas.id = 'map-canvas';
    mapCanvas.setAttribute('data-map-provider', 'openstreetmap');
    
    // Add to document body
    document.body.appendChild(mapCanvas);
    
    // Create other required elements
    const elements = ['routeSelect', 'messages', 'delete', 'refresh', 'autorefresh', 'viewall'];
    elements.forEach(id => {
        const element = document.createElement('div');
        element.id = id;
        document.body.appendChild(element);
    });
};

// Clean up DOM
const cleanDOM = () => {
    const elements = ['map-canvas', 'routeSelect', 'messages', 'delete', 'refresh', 'autorefresh', 'viewall'];
    elements.forEach(id => {
        const element = document.getElementById(id);
        if (element) {
            element.remove();
        }
    });
};

// Run tests
const runTests = () => {
    console.log('Running Map Provider Tests...');
    
    // Set up
    mockDOM();
    
    // Test 1: GPSTracker exists
    console.assert(typeof GPSTracker === 'object', 'GPSTracker object should exist');
    
    // Test 2: getMapProvider method exists
    console.assert(typeof GPSTracker.getMapProvider === 'function', 'getMapProvider method should exist');
    
    // Test 3: Mock cacheElements and test getMapProvider
    const originalCacheElements = GPSTracker.cacheElements;
    GPSTracker.cacheElements();
    
    // Test default provider from data attribute
    const provider = GPSTracker.getMapProvider();
    console.assert(provider === 'openstreetmap', `Default provider should be openstreetmap, got ${provider}`);
    
    // Test 4: Test with google provider
    document.getElementById('map-canvas').setAttribute('data-map-provider', 'google');
    const updatedProvider = GPSTracker.getMapProvider();
    console.assert(updatedProvider === 'google', `Updated provider should be google, got ${updatedProvider}`);
    
    // Test 5: Test with no data attribute (should use config default)
    document.getElementById('map-canvas').removeAttribute('data-map-provider');
    const configProvider = GPSTracker.getMapProvider();
    console.assert(configProvider === GPSTracker.config.defaultMapProvider, 
        `Provider should fall back to config (${GPSTracker.config.defaultMapProvider}), got ${configProvider}`);
    
    // Clean up
    cleanDOM();
    
    console.log('Map Provider Tests completed!');
};

// Execute tests when loaded in a browser
if (typeof window !== 'undefined') {
    window.addEventListener('load', () => {
        // Only run if GPSTracker is available
        if (typeof GPSTracker !== 'undefined') {
            runTests();
        } else {
            console.error('GPSTracker not loaded. Tests cannot run.');
        }
    });
}

// Export for use with testing frameworks
if (typeof module !== 'undefined') {
    module.exports = { runTests };
}