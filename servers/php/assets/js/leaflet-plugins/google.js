// assets/js/leaflet-plugins/google.js
// Replacement Google Maps layer for Leaflet 1.x
// Based on concepts from plugins like Leaflet.GridLayer.GoogleMutant

/* global google: true */

L.GridLayer.GoogleMutant = L.GridLayer.extend({
    options: {
        minZoom: 0,
        maxZoom: 21,
        tileSize: 256,
        subdomains: 'abc', // unused, required by L.GridLayer
        errorTileUrl: '', // unused, required by L.GridLayer
        attribution: 'Map data &copy; Google',
        opacity: 1,
        continuousWorld: false,
        noWrap: false,
        // Types: 'roadmap', 'satellite', 'hybrid', 'terrain'
        type: 'roadmap',
        styles: [] // Ensure styles is an array by default
    },

    initialize: function (options) {
        L.GridLayer.prototype.initialize.call(this, options);

        // Ensure styles is an array
        if (this.options.styles && !Array.isArray(this.options.styles)) {
            console.warn('GoogleMutant: styles must be an array. Defaulting to empty array.');
            this.options.styles = [];
        }

        this._ready = typeof google === 'object' && typeof google.maps === 'object';

        if (!this._ready) L.Google.asyncWait.push(this);
    },

    onAdd: function (map) {
        this._map = map;
        this._initContainer();
        this._initMapObject();

        map.on('viewreset', this._reset, this);
        map.on('move', this._update, this);
        map.on('zoomend', this._handleZoomAnim, this);
        map.on('resize', this._resize, this);

        // Necessary hack for Leaflet 1.x compatibility with GridLayer
        // Prevents map dragging issues on tiles
        L.DomEvent.on(this._container, 'mousedown', function (e) {
            L.DomEvent.stopPropagation(e);
        });

        this._reset();
        this._update();
    },

    onRemove: function (map) {
        L.GridLayer.prototype.onRemove.call(this, map);
        map.off('viewreset', this._reset, this);
        map.off('move', this._update, this);
        map.off('zoomend', this._handleZoomAnim, this);
        map.off('resize', this._resize, this);

        if (this._mapObject) {
            google.maps.event.clearInstanceListeners(this._mapObject);
        }
        // Clean up the container
        if (this._container) {
             L.DomUtil.remove(this._container);
             this._container = null;
             this._mapObject = null;
        }
    },

    getAttribution: function () {
        return this.options.attribution;
    },

    setOpacity: function (opacity) {
        this.options.opacity = opacity;
        if (this._container) {
            L.DomUtil.setOpacity(this._container, opacity);
        }
    },

    setOptions: function (opts) {
        L.setOptions(this, opts);
        if (this._mapObject) {
            if (opts.styles) {
                 // Ensure styles is an array
                 if (opts.styles && !Array.isArray(opts.styles)) {
                     console.warn('GoogleMutant: styles must be an array. Defaulting to empty array.');
                     opts.styles = [];
                 }
                this._mapObject.setOptions({styles: opts.styles});
            }
            if (opts.type) {
                this._mapObject.setMapTypeId(opts.type);
            }
        }
    },

    _initContainer: function () {
        if (this._container) return;

        this._container = L.DomUtil.create('div', 'leaflet-google-layer leaflet-layer');
        this.getPane().appendChild(this._container);
        this._container.style.opacity = this.options.opacity;

        this.setElementSize(this._container, this._map.getSize());
    },

    _initMapObject: function () {
        if (!this._ready || this._mapObject) return;

        this._mapObject = new google.maps.Map(this._container, {
            center: new google.maps.LatLng(0, 0),
            zoom: 0,
            mapTypeId: this.options.type,
            disableDefaultUI: true,
            keyboardShortcuts: false,
            draggable: false,
            disableDoubleClickZoom: true,
            scrollwheel: false,
            streetViewControl: false,
            styles: this.options.styles,
            backgroundColor: 'transparent'
        });

        // Add listener for idle event to update tiles when Google Maps is ready
        google.maps.event.addListenerOnce(this._mapObject, 'idle', () => {
             this._checkZoomLevels();
             this._reset();
        });
        // Forward map events
        google.maps.event.addListener(this._mapObject, 'dragstart', () => { this._map.fire('movestart'); });
        google.maps.event.addListener(this._mapObject, 'dragend', () => { this._map.fire('moveend'); });
    },

    _checkZoomLevels: function () {
        // Check max zoom from Google Maps API
        var zoom = this._map.getZoom();
        var maxZoom = this.options.maxZoom;
        var mapTypeMaxZoom = this._mapObject.mapTypes.get(this.options.type).maxZoom;

        if (mapTypeMaxZoom !== undefined && maxZoom > mapTypeMaxZoom) {
            this.options.maxZoom = mapTypeMaxZoom;
            if (zoom > mapTypeMaxZoom) {
                this._map.setZoom(mapTypeMaxZoom);
            }
        }
    },

    _reset: function () {
        this._initContainer();
        this._initMapObject();
        if (this._mapObject) {
            var size = this._map.getSize();
            var center = this._map.getCenter();
            var _center = new google.maps.LatLng(center.lat, center.lng);
            this._mapObject.setCenter(_center);
            this._mapObject.setZoom(this._map.getZoom());
            this.setElementSize(this._container, size);
            google.maps.event.trigger(this._mapObject, 'resize'); // Trigger resize
        }
        L.GridLayer.prototype._reset.call(this);
    },

    _update: function () {
        if (!this._mapObject) return;
        var center = this._map.getCenter();
        var _center = new google.maps.LatLng(center.lat, center.lng);
        this._mapObject.setCenter(_center);
    },

    _handleZoomAnim: function () {
        if (!this._mapObject) return;
        var center = this._map.getCenter();
        var _center = new google.maps.LatLng(center.lat, center.lng);
        this._mapObject.setCenter(_center);
        this._mapObject.setZoom(Math.round(this._map.getZoom()));
    },

    _resize: function () {
        var size = this._map.getSize();
        if (this._container && this._container.style.width === size.x + 'px' &&
            this._container.style.height === size.y + 'px')
            return;
        if (this._container) {
            this.setElementSize(this._container, size);
        }
        if (this._mapObject) {
            google.maps.event.trigger(this._mapObject, 'resize');
        }
    },

    setElementSize: function (e, size) {
        e.style.width = size.x + 'px';
        e.style.height = size.y + 'px';
    }
});

// Factory function
L.gridLayer.googleMutant = function (options) {
    return new L.GridLayer.GoogleMutant(options);
};

// Async handling (similar to original plugin)
L.Google = L.GridLayer.GoogleMutant; // Alias for backward compatibility if needed
L.Google.asyncWait = [];
L.Google.asyncInitialize = function() {
	var i;
	for (i = 0; i < L.Google.asyncWait.length; i++) {
		var o = L.Google.asyncWait[i];
		o._ready = true;
		if (o._container) {
			o._initMapObject();
            o._update();
		}
	}
	L.Google.asyncWait = [];
};

// If google maps api is loaded async, then call L.Google.asyncInitialize()
// function when the load is complete. Example:
// function initializeGoogleApi() { ...; L.Google.asyncInitialize(); }
// <script src="https://maps.googleapis.com/maps/api/js?key=YOUR_API_KEY&callback=initializeGoogleApi" async defer></script>

