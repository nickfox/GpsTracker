If you are planning to use the default database, SQLite, the database is ready to go and no importing or configuring is necessary. If you want to use one of the other supported databases, the database import file is in the sqlserver, mysql, or PostgreSQL directory.  This needs to be imported into your empty database.  Please read the quick start guide to see how to do that:

https://www.websmithing.com/2014/01/20/quick-start-guide-for-gpstracker-3/#aspnetserver

If you want to turn on the adsense advertisement in the map, then change line 8 of google.js to enabled:

var adsense_status = 'enabled';
and add your adsense channel number and publisher id to line 206 and 207:

user_adsense.channelNumber = "YOUR_CHANNEL_NUMBER";
user_adsense.publisherID = "YOUR_PUBLISHER_ID";    
https://github.com/nickfox/GpsTracker/tree/master/servers/dotNet/javascript/leaflet-plugins/google.js