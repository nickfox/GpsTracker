
Google Map GPS Cell Phone Tracker, Version 2.0

Installation

There are 3 parts to this project, the website, the database and the phone app. I will describe the installation for all 3 parts. First unzip the files. There are some basic requirements for using this app. The first is that you have a GPS cellphone that has a data plan (so that you can access your website). Your carrier must support GPS, I used Sprint/Nextel with the Motorola i355 phone. 

The second is an webserver running PHP and the MySQLi.dll extension.

http://www.php.net/

The MySQLi extension is required so that stored procedures in the database can be called from php. Here are the instructions on how to intall it:

http://forge.mysql.com/wiki/PHP_FAQ#Loading_the_MySQLi_extension_into_PHP

and here is where you can find the MySQLi.dll:

http://dev.mysql.com/get/Downloads/Connector-PHP-mysqlnd/php_5.2.1-mysqlnd-5.0.1-beta.zip/from/pick

when the extension is properly installed, the extension section will show up on the php_info.php webpage which is included in this distribution.

The third is MySQL 5.0 or higher: 

http://dev.mysql.com/downloads/mysql/5.0.html#downloads

Version 5.0 or greater is required for stored procedures.

*******************************************************

Website:

Go to http://www.google.com/apis/maps/signup.html and get your API key for your website. You must have your own domain name. You cannot use localhost on your home computer unless it has it's own domain name. The key needs to be added to the 2 following files:

displaymap2.php
getgooglemap2.php

You may need to add a new mime type to Apache so that jad files are recognized. This can be done in httpd.conf file (someone please correct me if I am wrong). If you try to browse with your phone to the jad file and it is not recognized, then you probably need to add this mime type.

For example:

<Directory /www/htdocs/GPSTRacker/phone>
AddType text/vnd.sun.j2me.app-descriptor .jad
</Directory>

in the file dbconnect2.php, you need to change the host, username and password for your mysql installation.  

*******************************************************

Database:

There are 2 ways to create the database. Create a database called GPSTracker2 and then restore the database with the file GPSTracker2.sql using MySQL Administrator. There is some sample GPS data in this database.

If you are using phpMyAdmin, you may need to change the import file. Look at the stored procedures in the GPSTracker.sql file, this is the first line of one them:

CREATE DEFINER=`root`@`localhost` PROCEDURE `prcDeleteRoute`(

If phpMyAdmin complains about trying to intall the 4 procedures, then remove the DEFINER of all 4 procedures like this:

CREATE PROCEDURE `prcDeleteRoute`(

They should install then. The file data.sql contains sample data.  

*******************************************************

Phone:

The best thing about version 2 of the gps tracker is that the phone application does not need to be compiled. Version 1 required everyone to download netbeans and compile the app. You can still compile the application if you want, the instructions for that are below.

The first thing you need to do is get the application onto your phone. If you have installed the website and set the mime type for jad files (see above), you can then browse to the application with your phone. For example:

http://www.mywebsite.com/gpstracker/phone/

Download the app and go to settings and set the phone number, download website and interval. The phone number can be any string, for instance, someone's name. If you want to uniquely identify routes, use an actual phone number.

If you try using the phone with the default download website (websmithing), you can test if your phone is working by going to:

http://www.websmithing.com/gpstracker/DisplayMap2.aspx

and viewing the route you created with your phone.

The second way to get the app onto your phone is to use a cable and your cell phone's application loader software that comes with your cell phone. 

For those of you who need to alter or recompile the phone application, here's what you need.

Download netbeans from here:

http://download.netbeans.org/netbeans/6.0/final/

choose the one that says Mobility.

Go to http://java.sun.com/products/sjwtoolkit/download-2_5_1.html and download:

Sun Java Wireless Toolkit 2.5.1 for CLDC

The link is about 3/4 of the way down the page. The directory must be in the root (C:\) with *no spaces*. It defaults to C:\WTK25. I would use that... Start Net Beans IDE and go to File/Open Project menu and open up the GPSTracker2.0 project. In netbeans, right click on the word GPSTracker2.0, you will find it up in the upper left hand corner. Click on the Platform menu item and then the button that says manage emulators. cLick "add platform" and select "Java Micro Edition Platform Emulator" and click next. A list will be generated, then click the C:\WTK25 2.5.1 Wireless Toolket that you installed earlier. 

Now go ahead and build the application. The 2 files that you need are in the Phone/dist folder. They are GPST2.0.jar and GPST2.0.jad.

*******************************************************

Please leave the link below with the source code, thank you.
http://www.websmithing.com/portal/Programming/tabid/55/articleType/ArticleView/articleId/6/Google-Map-GPS-Cell-Phone-Tracker-Version-2.aspx 




 