The php version of GpsTracker is now updated to v3 of google maps and jquery. If you want to turn off the advertisement in the map, then change line 8 of google.js to disabled:

    var adsense_status = 'disabled';

https://github.com/nickfox/GpsTracker/blob/master/servers/php/javascript/leaflet-plugins/google.js

If you want continue using adverstising, then change the channelNumber and publisherID on lines 186 and 187 to your own adsense information.
