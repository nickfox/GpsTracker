The database file is in the sqlserver or mysql directory and this needs to be restored, please read the quick start guide to see how to do that:

https://www.websmithing.com/2014/01/20/quick-start-guide-for-gpstracker-3/#aspnetserver

If you want to turn on the adsense advertisement in the map, then change line 8 of google.js to enabled:

var adsense_status = 'enabled';
and add your adsense channel number and publisher id to line 206 and 207:

user_adsense.channelNumber = "YOUR_CHANNEL_NUMBER";
user_adsense.publisherID = "YOUR_PUBLISHER_ID";    
https://github.com/nickfox/GpsTracker/tree/master/servers/dotNet/javascript/leaflet-plugins/google.js