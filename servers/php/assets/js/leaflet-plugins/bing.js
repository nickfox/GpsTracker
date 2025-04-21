L.BingLayer = L.TileLayer.extend({
	options: {
		culture: ''
	},

	initialize: function(key, options) {
		L.Util.setOptions(this, options);

		this._key = key;
		this._url = null;
		this.meta = {};
		this.loadMetadata();
	},

	tile2quad: function(x, y, z) {
		var quad = '';
		for (var i = z; i > 0; i--) {
			var digit = 0;
			var mask = 1 << (i - 1);
			if ((x & mask) !== 0) digit += 1;
			if ((y & mask) !== 0) digit += 2;
			quad = quad + digit;
		}
		return quad;
	},

	getMetaData: function() {
		if (this.meta && this.meta.resourceSets &&
				this.meta.resourceSets.length > 0 &&
				this.meta.resourceSets[0].resources &&
				this.meta.resourceSets[0].resources.length > 0) {
			return this.meta.resourceSets[0].resources[0];
		}
		return null;
	},

	loadMetadata: function() {
		var _this = this;
		var cbid = '_bing_metadata_' + L.Util.stamp(this);
		window[cbid] = function (meta) {
			_this.meta = meta;
			if (meta.statusCode === 200) {
				var resource = _this.getMetaData();
				if (!resource) {
					return;
				}
				var urlPattern = resource.imageUrl;
				var subdomains = resource.imageUrlSubdomains;
				_this._url = urlPattern.replace('{subdomain}', subdomains[0]);
				_this.fire('load');
			}
		};
		var url = 'https://dev.virtualearth.net/REST/v1/Imagery/Metadata/RoadOnDemand' +
			'?include=ImageryProviders&jsonp=' + cbid + '&key=' + this._key;
		var script = document.createElement('script');
		script.type = 'text/javascript';
		script.src = url;
		script.id = cbid;
		document.getElementsByTagName('head')[0].appendChild(script);
	},

	_update: function() {
		if (!this._url) return;
		L.TileLayer.prototype._update.call(this);
	},

	_createTile: function() {
		var tile = L.TileLayer.prototype._createTile.call(this);
		tile.onload = L.bind(this._tileOnLoad, this);
		tile.onerror = L.bind(this._tileOnError, this);
		return tile;
	},

	_getTileUrl: function(tilePoint) {
		if (!this._url) return '';
		var zoom = this._getZoomForUrl();
		var subdomains = this.options.subdomains,
			s = this.options.subdomains[(tilePoint.x + tilePoint.y) % subdomains.length];

		return this._url
			.replace('{subdomain}', s)
			.replace('{quadkey}', this.tile2quad(tilePoint.x, tilePoint.y, zoom))
			.replace('{culture}', this.options.culture);
	},

	onAdd: function(map) {
		if (!this._url) {
			this.on('load', function() {
				L.TileLayer.prototype.onAdd.call(this, map);
			});
		} else {
			L.TileLayer.prototype.onAdd.call(this, map);
		}
	}
});
