Please note that this project works fine up to ios 7. Apple has changed the way location works for ios 8 (without telling anyone and letting it fail silently) and there are new requirements. You have to add a line to the app plist file and a line of code before you start getting the location. It's explained here in this StackOverflow question:

http://stackoverflow.com/questions/24062509/ios-8-location-services-not-working

I'm not going to spend $99 dollars just to update and test this app. If you would like me to update and test it for ios 8 and are willing to pay the $99 dollars for the Apple Developer Program, then I will do it. Create an issue and I will give you my paypal account. Otherwise just follow the directions in that StackOverflow question. After six years of dealing with Apple BS, I'm sick and tired of it. Android is so much more pleasant to deal with and the marketshare is so much bigger than iPhone. Good riddance.

***************************

This is the ios client for gpstracker. Remember that you need to open this project with GpsTracker.xcworkspace since you are using AFNetworking cocoapods. Don't forget to install your pod before using this.


In order to use this, you need to change the username on line 197 of WSViewController.m. Then test the app on the websmithing website:

https://www.websmithing.com/gpstracker/displaymap.php

Once you have tested the app on websmithing, you need to update your own website by changing line 186, defaultUploadWebsite to your own website. Don't forget to change from http to https if you are not using ssl.
