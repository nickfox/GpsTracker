![gpstracker](https://raw.githubusercontent.com/nickfox/GpsTracker/master/gpstracker_small.png)Gps Tracker v5.1.4
-------------

##### Google Map Gps Cell Phone Tracker

Now available as a Wordpress plugin and Android client!

This project allows you to track cell phones periodically. For instance every minute or every five minutes. You can watch the cell phone being tracked in real time using Google Maps (and other map providers such as OpenStreetMaps) and you can store and reload routes easily. The map display page is built using bootstrap which makes the page responsive and also uses bootswatch which gives you the choice of 17 different themes. There are 4 clients, iOS, Android, Windows Phone and Java ME. 

You have the following choices server side:

1.  ASP.NET with SQL Server
2.  PHP with your choice of:
  * MySQL
  * PostgreSQL
  * SQLite
3.  Wordpress plugin with Android client

All 3 stacks are in the same download but you only need to use one.

By default the Tracker server is set up to use the included SQLite database.  If you want to use one of the other supported database systems, edit the dbconnect.php file. 

If you need help, please go to:

https://www.websmithing.com/gps-tracker/

Here is a quick start guide to help you set up Gps Tracker:

https://www.websmithing.com/2014/01/20/quick-start-guide-for-gpstracker-3/

*************

#### Changelog

Jan 25, 2018 - v5.1.4

Updated android client to android studio 3.0.1 and SDK 26. Please note that if you use Google maps, you now need to get an API key from Google.

https://developers.google.com/maps/documentation/javascript/get-api-key

*************

Jun 3, 2017 - v5.1.3

Got rid of help forum, it cut my adsense revenue in half.

*************

Jul 11, 2016 - v5.1.2

Updating documentation to show new help forum.

*************

Apr 2, 2016 - v5.1.1

Updated gradle in android client

*************

Oct 21, 2015 - v5.1.0

Added TK-103 server

*************

Sep 30, 2015 - v5.0.0

Added Wordpress plugin and Android client

*************

Sep 8, 2015 - v4.0.4

Added support for Sqlite and PostgreSQL, thanks Brent Fraser

fixed prcGetAllRoutesForMap regression error 

*************

Nov 24, 2014 - v4.0.3

Added European decimal handling to updatelocation, thanks Wim

*************

Nov 15, 2014 - v4.0.2

Fixed prcGetAllRoutesForMap, thanks Hristo

*************

Sep 29, 2014 - v4.0.1

Added some validation to updatelocation page.

*************

Sep 25, 2014 - v4.0.0

Gps Tracker now has a responsive design using bootstrap. The phone clients are now using GET request again to make troubleshooting easier. Here are 3 out of the available 17 themes.

the light one:

![gpstrackerandroid](https://www.websmithing.com/images/gpstracker-light.jpg)

the dark one:

![gpstrackerandroid](https://www.websmithing.com/images/gpstracker-dark.jpg)

the cool blue one:

![gpstrackerandroid](https://www.websmithing.com/images/gpstracker-blue.jpg)

*************

Jun 12, 2014 - v3.2.4

Fixed android intervals not working above one minute and removed adsense ads publisher id.

*************

Jun 6, 2014 - v3.2.3

This version fixes the zoom buttons on the google map which was causing the app to freeze and updates leaflet to version 0.7.3. This version is now free from all known errors.

*************

May 5, 2014 - v3.2.2

This version fixes the total distance traveled in the android app.

*************

May 1, 2014 - v3.2.1

This version changes the UI of the android app from two buttons (Save and Track) to one button (Track).

![gpstrackerandroid](https://raw.githubusercontent.com/nickfox/GpsTracker/master/phoneClients/android/gpstracker3.2.1.png)


and the android version is now in Google Play:


https://play.google.com/store/apps/details?id=com.websmithing.gpstracker

*************

Apr 20, 2014 - v3.2.0

Some pretty major changes here. The android app now has google play location services running in a background service. Also updates will restart if phone is rebooted. The servers are both now using json and jquery. Xml is gone forever. Both servers now support multiple map types. It's currently set up for google maps, bing maps and OpenStreetMaps. 


*************

Feb 11, 2014 - v3.1.0

Version 3.1.0 updates the php server code from google maps 2 to 3 and now uses jquery. There was a bug in the delete route stored procedure (the sessionID parameter was to small and was causing the delete to fail silently). 

*************

Jan 14, 2014 - v3.0.0

Version 3.0.0 of Gps Tracker is complete. This was a very big change from the last version. It's been updated with ios, android, windowsPhone and java me/j2me phone clients and the servers have all been updated as well. Please let me know how it works, good or bad and create an issue if you find a problem. Thanks and enjoy the software, Nick.

ps. I will be writing tutorials and documentation shortly.

