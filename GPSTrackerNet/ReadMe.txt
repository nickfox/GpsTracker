
Google Map GPS Cell Phone Tracker, Version 2.0

Installation

There are 3 parts to this project, the website, the database and the phone app. I will describe the installation for all 3 parts. First unzip the files. There are some basic requirements for using this app. The first is that you have a GPS cellphone that has a data plan (so that you can access your website). Your carrier must support GPS, I used Sprint/Nextel with the Motorola i355 phone. 

The second is .NET 2.0 and the Visual Studio 2005 IDE. You can use the express edition, it's free.

http://www.microsoft.com/express/download/#webInstall

The third is MSSQL Server 2005, once again you can use the express edition, it's free also:

http://msdn2.microsoft.com/en-us/express/bb410791.aspx

*******************************************************

Website:

Put the .NET website solution files into a folder called GPSTracker. In administrative tools open the IIS console and right click on "default web site" and create a new virtual directory called GPSTracker and then browse to your GPSTracker directory that has the .NET solution files, click next. Make sure that Read and Run Scripts is selected and finish creating the virtual directory. 
You can then open the solution by clicking on the GPSTracker.sln file. Open the Web.config file and change the uid (username) and pwd (password) to your user in SQL server.

Go to http://www.google.com/apis/maps/signup.html and get your API key for your website. You must have your own domain name. You cannot use localhost on your home computer unless it has it's own domain name. Open the Web.config page and replace the key that is there with the one you got for your domain.

Right click on "default web site" again and go to properties. Click on the Http Headers tab and then at the bottom where it says Mime Map, click on File Types. Click on new type and and add the following:

associated extension: .jad

content type (MIME): text/vnd.sun.j2me.app-descriptor

Click ok and save it. This allows the user to download the jad/jar files for the phone application.

*******************************************************

Database:

There are 2 ways to create the database. Create a database called GPSTracker and then restore the database with the file GPSTracker.bak. When you restore, go to the Options page and select the checkbox that says "Overwrite the existing database". There is some sample GPS data in this database.

The second way is to open up a query window in Management Studio and run the GPSTracker.sql script. You then need to import the sample data in data.txt. You do not need to do both methods. I would do the .bak method personally because it's easier. You can use the data.txt file to reload data if you delete it. 

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




 