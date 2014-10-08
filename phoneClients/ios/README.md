This is the ios client for gpstracker. Remember that you need to open this project with GpsTracker.xcworkspace since you are using AFNetworking cocoapods. Don't forget to install your pod before using this.


In order to use this, you need to change the username on line 197 of WSViewController.m. Then test the app on the websmithing website:

https://www.websmithing.com/gpstracker/displaymap.php

Once you have tested the app on websmithing, you need to update your own website by changing line 186, defaultUploadWebsite to your own website. Don't forget to change from http to https if you are not using ssl.
