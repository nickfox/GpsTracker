//
//  GpsTracker.java
//  GpsTracker
//
//  Created by Nick Fox on 11/7/13.
//  Copyright (c) 2013 Nick Fox. All rights reserved.
//

package com.websmithing.gpstracker;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.Calendar;

public class GpsTracker extends MIDlet implements CommandListener, ItemStateListener {
    private Display display;
    private Form form;
    private Form zoomScreen;
    private Form settingsScreen;
    private Command exitCmd;
    private Command saveCmd;
    private Command zoomCmd;
    private Command settingsCmd;
    private Command backCmd;
    private TextField phoneNumberTextField;
    private TextField uploadWebsiteTextField;
    private Gauge zoomGauge;
    private StringItem zoomStringItem;
    private ChoiceGroup updateIntervalCG;
    private String updateInterval;
    private int[] iTimes = {60, 300, 900};
    
    private RmsHelper rms;
    private GpsHelper gps;
    
    private String uploadWebsite;    
    private String defaultUploadWebsite = "http://www.websmithing.com/gpstracker2/getgooglemap3.php";

    protected String phoneNumber;
    protected String zoomLevel;
    protected int height, width;   
    protected Calendar currentTime;    
    protected long sessionID;
    protected Image im = null;
    
    public GpsTracker(){
        form = new Form("GpsTracker");
        display = Display.getDisplay(this);
        exitCmd = new Command("Exit", Command.EXIT, 1); 
        zoomCmd = new Command("Zoom", Command.SCREEN, 2);
        settingsCmd = new Command("Settings", Command.SCREEN, 3);
        
        form.addCommand(exitCmd);
        form.addCommand(zoomCmd);
        form.addCommand(settingsCmd);
        form.setCommandListener(this);        
        
        display.setCurrent(form);
        currentTime = Calendar.getInstance();
        sessionID = System.currentTimeMillis();
        height = form.getHeight();
        width = form.getWidth();
        
        // RMS is the phone's built in storage, kind of like a database, but
        // it only stores name-value pairs (like an associative array or hashtable). 
        // eveything is stored as a string.
        getSettingsFromRMS();
        
        // the phone number field is the only empty field when the application is 
        // first loaded. it does not have to be a phone number, it can be any string,
        // but for uniqueness, it's best to use a phone number. this only has to be 
        // done once.
        if (hasPhoneNumber()) {
            startGPS();
            displayInterval();
        }
    } 
   
    public void startApp() {
        if ( form != null ) { 
            display.setCurrent(form); 
        } 
    }
 
    // let the user know how often map will be updated
    private void displayInterval() {
        int tempTime = iTimes[Integer.parseInt(updateInterval)]/60;
    
        display.setCurrent(form);
        form.deleteAll();
         
        if (tempTime == 1) {
            log("Getting map once a minute...");
        }
        else {
           log("Getting map every " + String.valueOf(tempTime) + " minutes..."); 
        }       
    }    
    
    private void loadZoomScreen() {
        zoomScreen = new Form("Zoom");
        zoomGauge = new Gauge("Google Map Zoom", true, 17, Integer.parseInt(zoomLevel));
        zoomStringItem = new StringItem(null, "");
        zoomStringItem.setText("Zoom level: " + zoomGauge.getValue());
        backCmd = new Command("Back", Command.SCREEN, 1);

        zoomScreen.append(zoomGauge);
        zoomScreen.append(zoomStringItem);
        zoomScreen.addCommand(backCmd);
        zoomScreen.setItemStateListener(this);
        zoomScreen.setCommandListener(this);

        display.setCurrent(zoomScreen);
    }
 
    // this method is called every time the zoom guage changes value. the zoom level is 
    // reset and saved
    public void itemStateChanged(Item item) {
        if (item == zoomGauge) {
            zoomStringItem.setText("Zoom level: " + zoomGauge.getValue());
            zoomLevel = String.valueOf(zoomGauge.getValue());

            try { 
                rms.put("zoomLevel", zoomLevel);
                rms.save(); 
            }
            catch (Exception e) {
                log("GPSTracker.itemStateChanged: " + e);
            } 
        }
    }    

    private void loadSettingsScreen() {
        settingsScreen = new Form("Settings");

        phoneNumberTextField = new TextField("Phone number or user name", phoneNumber, 20, TextField.ANY);
        uploadWebsiteTextField = new TextField("Upload website", uploadWebsite, 100, TextField.ANY);
        settingsScreen.append(phoneNumberTextField);
        settingsScreen.append(uploadWebsiteTextField);

        String[] times = { "1 minute", "5 minutes", "15 minutes"};
        updateIntervalCG = new ChoiceGroup("Update map how often?", ChoiceGroup.EXCLUSIVE, times, null);
        updateIntervalCG.setSelectedIndex(Integer.parseInt(updateInterval), true);
        settingsScreen.append(updateIntervalCG);        

        saveCmd = new Command("Save", Command.SCREEN, 1);
        settingsScreen.addCommand(saveCmd);

        settingsScreen.setCommandListener(this);
        display.setCurrent(settingsScreen);
    }    
    
    // get the settings from the phone's storage and load 4 global variables
    public void getSettingsFromRMS() {
        try {
            rms = new RmsHelper(this, "GPSTracker");

            phoneNumber = rms.get("phoneNumber");
            uploadWebsite = rms.get("uploadWebsite");
            zoomLevel = rms.get("zoomLevel");
            updateInterval = rms.get("updateInterval");
        }
        catch (Exception e) {
            log("GPSTracker.getSettingsFromRMS: " + e);
        }
        
        if ((uploadWebsite == null) || (uploadWebsite.trim().length() == 0)) {
            uploadWebsite = defaultUploadWebsite;
        }        

        if ((zoomLevel == null) || (zoomLevel.trim().length() == 0)) {
            zoomLevel = "12";
        }
        if ((updateInterval == null) || (updateInterval.trim().length() == 0)) {
            updateInterval = "1";
        }        
    }    

    private boolean hasPhoneNumber() {
        if ((phoneNumber == null) || (phoneNumber.trim().length() == 0)) {
           log("Phone number required. Please go to settings.");
           return false;
        }
        else {
           return true;
      }
    }
  
    // gps is started with the update interval. the interval is the time in between
    // map updates
    private void startGPS() {
        if (gps == null) {
            gps = new GpsHelper(this, iTimes[Integer.parseInt(updateInterval)], uploadWebsite);
            gps.startGPS();
        }
    }
    
    // this is called when the user changes the interval in the settings screen
    private void changeInterval() {
        if (gps == null) {
            startGPS(); 
        }
        else {
            gps.changeInterval(iTimes[Integer.parseInt(updateInterval)]);
        }
    }
    
    // save settings back to phone memory
    private void saveSettingsToRMS() {
        try { 
            phoneNumber = phoneNumberTextField.getString();
            uploadWebsite = uploadWebsiteTextField.getString();
            updateInterval = String.valueOf(updateIntervalCG.getSelectedIndex());
            
            rms.put("phoneNumber", phoneNumber);
            rms.put("uploadWebsite", uploadWebsite);
            rms.put("updateInterval", updateInterval);

            rms.save(); 
        }
        catch (Exception e) {
            log("GPSTracker.saveSettings: " + e);
        } 
        display.setCurrent(form);
    }

    // this method displays the map image, it is called from the networker object
    public void showMap(boolean flag)
    {
        if (flag == false) {
            log("Map could not be downloaded.");
        }
        else {
            ImageItem imageitem = new ImageItem(null, im, ImageItem.LAYOUT_DEFAULT, null);

            if(form.size()!= 0) {
                form.set(0, imageitem);
            }
            else {
                form.append(imageitem);
            }
        }
    }   
        
    public void log(String text) {
        StringItem si = new StringItem(null, text);
        si.setLayout(Item.LAYOUT_NEWLINE_AFTER);
        form.append(si);
    }

    public void commandAction(Command cmd, Displayable screen) {		
        if (cmd == exitCmd) {
            shutDownApp();
        }
        else if (cmd == saveCmd) {
            saveSettingsToRMS();

            if (hasPhoneNumber()) {
                changeInterval();
                displayInterval();
            }
        }
        else if (cmd == settingsCmd) {
            loadSettingsScreen(); 
        }
        else if (cmd == zoomCmd) {
            loadZoomScreen(); 
        }        
        else if (cmd == backCmd) {
            displayInterval();
        }
    }
     
    public void pauseApp() {}

    public void destroyApp(boolean unconditional) {}

    protected void shutDownApp() {
        destroyApp(true);
        notifyDestroyed();   
    }
}



