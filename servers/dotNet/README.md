The .NET server stack has been fully updated and tested. The gpstracker.bak file is in the sqlserver directory and was created with sql server 2012 express. Make sure to check the connection string in the Web.config, it is currently set to this:

connectionString="Data Source=localhost\SQLEXPRESS;Initial Catalog=GPSTracker;Persist Security Info=True;User ID=sa;Password=gpstracker"/>

This is set to use sql express with a sa password of gpstracker. Please read the connection string guide from Microsoft if you are using a different data source:

http://msdn.microsoft.com/en-us/library/ms156450.aspx

Please note that this is an asp.net website, not a web application. Its means you need to put source files directly onto an asp.net web server like IIS and it will be compiled on the fly. To open this project in visual studio for web (2012), you need to go to the file menu and open website, then select you iis web server and open the gpstracker website that you created there. Visual Studio express needs to be opened as an Administrator so that you can open the website.

If you want to turn off the advertisement in the map, then change line 8 of google.js to disabled:

    var adsense_status = 'disabled';

https://github.com/nickfox/GpsTracker/blob/master/servers/dotNet/javascript/leaflet-plugins/google.js

If you want continue using adverstising, then change the channelNumber and publisherID on lines 186 and 187 to your own adsense information..
