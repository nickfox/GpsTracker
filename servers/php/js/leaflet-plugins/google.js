/*
Copyright (c) 2011-2012, Pavel Shramov
All rights reserved. 
https://github.com/robertharm/leaflet-plugins
License in same folder as this file
*/

var adsense_status = 'disabled';

L.Google = L.Class.extend( {
	includes: L.Mixin.Events,

	options: {
		minZoom: 0,
		maxZoom: 18,
		tileSize: 256,
		subdomains: 'abc',
		errorTileUrl: '',
		attribution: '',
		opacity: 1,
		continuousWorld: false,
		noWrap: false,
		mapOptions: {
			backgroundColor: '#dddddd'
		}
	},

	// Possible types: SATELLITE, ROADMAP, HYBRID, TERRAIN
	initialize: function(type, options) {              
		L.Util.setOptions(this, options);

		this._ready = google.maps.Map !== undefined;
		if (!this._ready) L.Google.asyncWait.push(this);

		this._type = type || 'ROADMAP';
	},

	onAdd: function(map, insertAtTheBottom) {
		this._map = map;
		this._insertAtTheBottom = insertAtTheBottom;

		// create a container div for tiles
		this._initContainer();
		this._initMapObject();

        if (adsense_status == 'enabled') { 
            this._initAdSense(); 
        }

		// set up events
		map.on('viewreset', this._resetCallback, this);

		this._limitedUpdate = L.Util.limitExecByInterval(this._update, 150, this);
		map.on('move', this._update, this);

		map.on('zoomanim', this._handleZoomAnim, this);

		//20px instead of 1em to avoid a slight overlap with google's attribution
		map._controlCorners.bottomright.style.marginBottom = '20px';

		this._reset();
		this._update();
	},

	onRemove: function(map) {
		this._map._container.removeChild(this._container);
		this._map.off('viewreset', this._resetCallback, this);
		this._map.off('move', this._update, this);
		this._map.off('zoomanim', this._handleZoomAnim, this);
		map._controlCorners.bottomright.style.marginBottom = '0em';
	},

	getAttribution: function() {
		return this.options.attribution;
	},

	setOpacity: function(opacity) {
		this.options.opacity = opacity;
		if (opacity < 1) {
			L.DomUtil.setOpacity(this._container, opacity);
		}
	},

	setElementSize: function(e, size) {
		e.style.width = size.x + 'px';
		e.style.height = size.y + 'px';
	},

	_initContainer: function() {
		var tilePane = this._map._container,
			first = tilePane.firstChild;

		if (!this._container) {
			this._container = L.DomUtil.create('div', 'leaflet-google-layer leaflet-top leaflet-left');
			this._container.id = '_GMapContainer_' + L.Util.stamp(this);
			this._container.style.zIndex = 'auto';
		}

		tilePane.insertBefore(this._container, first);

		this.setOpacity(this.options.opacity);
		this.setElementSize(this._container, this._map.getSize());
	},

	_initMapObject: function() {
		if (!this._ready) return;
		this._google_center = new google.maps.LatLng(0, 0);
		var map = new google.maps.Map(this._container, {
		    center: this._google_center,
		    zoom: 0,
		    tilt: 0,
		    mapTypeId: google.maps.MapTypeId[this._type],
		    disableDefaultUI: true,
		    keyboardShortcuts: false,
		    draggable: false,
		    disableDoubleClickZoom: true,
		    scrollwheel: false,
		    streetViewControl: false,
		    styles: this.options.mapOptions.styles,
		    backgroundColor: this.options.mapOptions.backgroundColor
		});

		var _this = this;
		this._reposition = google.maps.event.addListenerOnce(map, 'center_changed',
			function() { _this.onReposition(); });
		this._google = map;

		google.maps.event.addListenerOnce(map, 'idle',
			function() { _this._checkZoomLevels(); });
            
		//Reporting that map-object was initialized.
		this.fire('MapObjectInitialized', { mapObject: map });
	},

	_checkZoomLevels: function() {
		//setting the zoom level on the Google map may result in a different zoom level than the one requested
		//(it won't go beyond the level for which they have data).
		// verify and make sure the zoom levels on both Leaflet and Google maps are consistent
		if (this._google.getZoom() !== this._map.getZoom()) {
			//zoom levels are out of sync. Set the leaflet zoom level to match the google one
			this._map.setZoom( this._google.getZoom() );
		}
        
        // this._resetCallback(this._container);
	},

	_resetCallback: function(e) {
		this._reset(e.hard);
	},

	_reset: function(clearOldContainer) {
		this._initContainer();
	},

	_update: function(e) {
		if (!this._google) return;
		this._resize();

		var center = e && e.latlng ? e.latlng : this._map.getCenter();
		var _center = new google.maps.LatLng(center.lat, center.lng);

		this._google.setCenter(_center);
		this._google.setZoom(Math.round(this._map.getZoom()));

		this._checkZoomLevels();
	},

	_resize: function() {
		var size = this._map.getSize();
		if (this._container.style.width === size.x &&
		    this._container.style.height === size.y)
			return;
		this.setElementSize(this._container, size);
		this.onReposition();
	},


	_handleZoomAnim: function (e) {
		var center = e.center;
		var _center = new google.maps.LatLng(center.lat, center.lng);

		this._google.setCenter(_center);
		this._google.setZoom(Math.round(e.zoom));
	},


	onReposition: function() {
		if (!this._google) return;
		google.maps.event.trigger(this._google, 'resize');
	},
    
    _initAdSense: function() {
    	  var adUnitDiv = document.createElement('div');
    	  adUnitDiv.className = "leaflet-control";
    	  adUnitDiv.style.margin = "0";
    	  adUnitDiv.style.clear = "none";
    	  var user_adsense = new String;
    	  user_adsense.format = google.maps.adsense.AdFormat["HALF_BANNER"];
    	  user_adsense.position = google.maps.ControlPosition["TOP_CENTER"];
    	  user_adsense.cposition = "TOP_CENTER";
    	  user_adsense.backgroundColor = "#c4d4f3";
    	  user_adsense.borderColor = "#aaa";
    	  user_adsense.titleColor = "#0000cc";
    	  user_adsense.textColor = "#000000";
    	  user_adsense.urlColor = "#009900";
    	  user_adsense.channelNumber = "6961715451";
    	  user_adsense.publisherID = "pub-7095775186404141";

    	  var adUnitOptions = {
    	    format: user_adsense.format,
    	    position: user_adsense.position,
    	    backgroundColor: user_adsense.backgroundColor,
    	    borderColor: user_adsense.borderColor,
    	    titleColor: user_adsense.titleColor,
    	    textColor: user_adsense.textColor,
    	    urlColor: user_adsense.urlColor,
    	    map: this._google,
    	    visible: true,
    		channelNumber: user_adsense.channelNumber,
    	    publisherId: user_adsense.publisherID
    	  }
    	  //info: dont load ads on minimaps  
    	  var size = this._map.getSize();
    	  if (size.x > 150) {
    		this._adUnit = new google.maps.adsense.AdUnit(adUnitDiv, adUnitOptions);
    	  }
        }
});

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
