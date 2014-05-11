The php version of GpsTracker is now updated to v3 of google maps and jquery. The database file is in the mysql directory and this needs to be installed from the command line, please read the quick start guide to see how to do that:

https://www.websmithing.com/2014/01/20/quick-start-guide-for-gpstracker-3/#phpserver

If you want to turn off the advertisement in the map, then change line 8 of google.js to disabled:

    var adsense_status = 'disabled';

https://github.com/nickfox/GpsTracker/blob/master/servers/php/javascript/leaflet-plugins/google.js

If you want continue using adverstising, then change the channelNumber and publisherID on lines 186 and 187 to your own adsense information.
