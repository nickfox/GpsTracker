//
//  NetWorker.java
//  GpsTracker
//
//  Created by Nick Fox on 11/7/13.
//  Copyright (c) 2013 Nick Fox. All rights reserved.
//

package com.websmithing.gpstracker;

import javax.microedition.io.*;
import java.io.*;

public class NetWorker {
    private GpsTracker midlet;
    private String uploadWebsite;
    int i = 1;
    
    public NetWorker(GpsTracker lbsMidlet, String UploadWebsite){
	this.midlet = lbsMidlet;
        this.uploadWebsite = UploadWebsite;
    }   

  public void postGpsData(String queryString) { 
        queryString = urlEncodeString(queryString);
        HttpConnection httpConnection = null;
        DataOutputStream dataOutputStream = null;

        try{
            httpConnection = (HttpConnection)Connector.open(uploadWebsite);
            httpConnection.setRequestMethod(HttpConnection.POST);
            httpConnection.setRequestProperty("User-Agent", "Profile/MIDP-1.0 Configuration/CLDC-1.0");
            httpConnection.setRequestProperty("Content-Language", "en-US");
            httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpConnection.setRequestProperty("Content-Length", String.valueOf(queryString.length())); 
            
            dataOutputStream = new DataOutputStream(httpConnection.openOutputStream());
            dataOutputStream.write(queryString.getBytes());
            
            // some mobile devices have unexpected behavior with flush(), test before using
            //dataOutputStream.flush();

            if(httpConnection.getResponseCode() != HttpConnection.HTTP_OK){
                 midlet.log("NetWorker.postGpsData responseCode: " + httpConnection.getResponseCode());
            }
        } catch (Exception e) {
           midlet.log("NetWorker.postGpsData error: " + e);
        }
        finally{ // clean up
            try{
                if(httpConnection != null)
                    httpConnection.close();
                if(dataOutputStream != null)
                    dataOutputStream.close();
             }
            catch(Exception e){}
        }
    }    
  

    private String urlEncodeString(String s)
    {
        if (s != null) {
            StringBuffer tmp = new StringBuffer();
            int i = 0;
            try {
                while (true) {
                    int b = (int)s.charAt(i++);

                    if (b != 0x20) {
                        tmp.append((char)b); 
                    }
                    else {
                        tmp.append("%");
                        if (b <= 0xf) { 
                            tmp.append("0");
                        }
                        tmp.append(Integer.toHexString(b));  
                    }
                }
            }
            catch (Exception e) {}
            return tmp.toString();
        }
        return null;
    }   
}    
