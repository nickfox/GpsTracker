//
//  GpsHelper.java
//  GpsTracker
//
//  Created by Nick Fox on 11/7/13.
//  Copyright (c) 2013 Nick Fox. All rights reserved.
//

package com.websmithing.gpstracker;

import javax.microedition.location.*;
import java.util.Calendar;
import java.util.Date;

public class GpsHelper implements LocationListener  {
   
    private LocationProvider locationProvider = null;
    private Coordinates oldCoordinates = null, currentCoordinates = null;
    private float distance = 0;
    private int azimuth = 0;
    private String uploadWebsite;
    private GpsTracker midlet;
    private int interval;           
    protected long sessionID;
     
    public GpsHelper(GpsTracker Midlet, int Interval, String UploadWebsite){
        sessionID = System.currentTimeMillis();
        this.midlet = Midlet;
        this.interval = Interval;
        this.uploadWebsite = UploadWebsite;
    } 
   
    // getting the gps location is based on an interval in seconds. for instance, 
    // the location is gotten once a minute, sent to the website to be stored in 
    // the DB (and then viewed on Google map) and used to retrieve a map tile (image) 
    // to be diplayed on the phone
    
    public void startGPS() {
         if (locationProvider == null) {
                createLocationProvider();

                Thread locationThread = new Thread() {
                    public void run(){
                        createLocationListener();
                    }
                };
                locationThread.start();
       }       
    }
    
    // this allows us to change how often the gps location is gotten
    public void changeInterval(int Interval) {
        if (locationProvider != null) {
            locationProvider.setLocationListener(this, Interval, -1, -1);
        }
    }
    
    private void createLocationProvider() {
        Criteria cr = new Criteria(); 
        
        try {
            locationProvider = LocationProvider.getInstance(cr);
        } catch (Exception e) {
           midlet.log("GPS.createLocationProvider: " + e);
        }
    }
    
    private void createLocationListener(){
          // 2cd value is interval in seconds
          try {
            locationProvider.setLocationListener(this, interval, -1, -1); 
          } catch (Exception e) {
           midlet.log("GPS.createLocationListener: " + e);
        } 
   }
    
    public void locationUpdated(LocationProvider provider, final Location location) {
        // get new location from locationProvider
         
        try {
            Thread getLocationThread = new Thread(){
                public void run(){
                   sendLocationToWebsite(location);
                }
            };

            getLocationThread.start();
        } catch (Exception e) {
           midlet.log("GPS.locationUpdated: " + e);
        }
    }    
 
    public void providerStateChanged(LocationProvider provider, int newState) {}
    
    private void sendLocationToWebsite(Location location){
        float speed = 0;
            
        try {
            QualifiedCoordinates qualifiedCoordinates = location.getQualifiedCoordinates();

           qualifiedCoordinates.getLatitude();

            if (oldCoordinates == null){
                oldCoordinates = new Coordinates(qualifiedCoordinates.getLatitude(),
                                                 qualifiedCoordinates.getLongitude(),
                                                 qualifiedCoordinates.getAltitude());
            } else {
                if (!Float.isNaN( qualifiedCoordinates.distance(oldCoordinates))) {
                    distance += qualifiedCoordinates.distance(oldCoordinates);
                }

                currentCoordinates = new Coordinates(qualifiedCoordinates.getLatitude(),
                                                     qualifiedCoordinates.getLongitude(),
                                                     qualifiedCoordinates.getAltitude());
                azimuth = (int)oldCoordinates.azimuthTo(currentCoordinates);
                oldCoordinates.setAltitude(qualifiedCoordinates.getAltitude());
                oldCoordinates.setLatitude(qualifiedCoordinates.getLatitude());
                oldCoordinates.setLongitude(qualifiedCoordinates.getLongitude());

            }

            if (qualifiedCoordinates != null){
                // we are trying to get mySql datetime in the following format with a space (%20)
                // 2008-04-17%2012:07:02
                
                Calendar currentTime = Calendar.getInstance();
                StringBuffer mySqlDateTimeString = new StringBuffer();
                mySqlDateTimeString.append(currentTime.get(Calendar.YEAR)).append("-");
                mySqlDateTimeString.append(currentTime.get(Calendar.DATE)).append("-");
                mySqlDateTimeString.append(currentTime.get(Calendar.MONTH)+1).append("%20");
                mySqlDateTimeString.append(currentTime.get(Calendar.HOUR_OF_DAY)).append(':');
                mySqlDateTimeString.append(currentTime.get(Calendar.MINUTE)).append(':');
                mySqlDateTimeString.append(currentTime.get(Calendar.SECOND));
                
                if (!Float.isNaN(location.getSpeed())) {
                    speed = location.getSpeed();
                }

                String gpsData = "latitude=" + String.valueOf(qualifiedCoordinates.getLatitude()) 
                        + "&longitude=" + String.valueOf(qualifiedCoordinates.getLongitude())
                        + "&speed=" + String.valueOf((int)(speed/1609*3600)) // in miles per hour
                        + "&direction=" + String.valueOf(azimuth) 
                        + "&date=" + mySqlDateTimeString
                        + "&locationmethod=" + location.getLocationMethod()
                        + "&distance=" + String.valueOf((int)(distance/1609)) // in miles
                        + "&phonenumber=" + midlet.phoneNumber
                        + "&sessionid=" + String.valueOf(sessionID) // System.currentTimeMillis();
                        + "&accuracy=" + String.valueOf((int)(qualifiedCoordinates.getHorizontalAccuracy())) // in meters
                        + "&locationisvalid=yes"
                        + "&extrainfo=" + location.getExtraInfo("text/plain")
                        + "&eventtype=javaMe";
               
                // with our query string built, we create a networker object to send the 
                // gps data to our website and update the DB
                NetWorker netWorker = new NetWorker(midlet, uploadWebsite);
                netWorker.postGpsData(gpsData);
            }

        } catch (Exception e) {
            midlet.log("GPS.getLocation: " + e);
        }
    }
}



