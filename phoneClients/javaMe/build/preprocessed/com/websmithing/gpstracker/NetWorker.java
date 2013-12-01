//
//  NetWorker.java
//  GpsTracker
//
//  Created by Nick Fox on 12/1/13.
//  Copyright (c) 2013 Nick Fox. All rights reserved.
//

package com.websmithing.gpstracker;

import javax.microedition.io.*;
import java.io.*;
import javax.microedition.lcdui.Image;

public class NetWorker {
    private GpsTracker midlet;
    private String uploadWebsite;
    int i = 1;
    
    public NetWorker(GpsTracker lbsMidlet, String UploadWebsite){
	this.midlet = lbsMidlet;
        this.uploadWebsite = UploadWebsite;
    }   

  public void getUrl(String queryString) { 
        queryString = URLencodeSpaces(queryString);
        String url = uploadWebsite + queryString;
        HttpConnection httpConn = null;
        InputStream inputStream = null;
        DataInputStream iStrm = null;
        ByteArrayOutputStream bStrm = null;        
        Image im = null;        
        
        try{
            httpConn = (HttpConnection)Connector.open(url);
    
            if(httpConn.getResponseCode() == HttpConnection.HTTP_OK){
                inputStream = httpConn.openInputStream();
                iStrm = new DataInputStream(inputStream);

                byte imageData[];
                int length = (int)httpConn.getLength();

                if(length != -1) {
                    imageData = new byte[length];
                    iStrm.readFully(imageData);
                }
                else { //Length not available
                    bStrm = new ByteArrayOutputStream();
                    int ch;
    
                    while((ch = iStrm.read())!= -1) {
                        bStrm.write(ch);
                    }
                    imageData = bStrm.toByteArray();
                       
                }
                im = Image.createImage(imageData, 0, imageData.length);                
            }
            else {
                 midlet.log("NetWorker.getUrl responseCode: " + httpConn.getResponseCode());
            }

        } catch (Exception e) {
           midlet.log("NetWorker.getUrl: " + e);
        }
        finally{ // Clean up
            try{
                if(bStrm != null)
                     bStrm.close();
                if(iStrm != null)
                     iStrm.close();
                if(inputStream != null)
                    inputStream.close();
                if(httpConn != null)
                    httpConn.close();
             }
            catch(Exception e){}
        }

        // if we have successfully gotten a map image, then we want to display it 
        if( im == null) {
            midlet.showMap(false);
        }
        else {
            midlet.im = im;
            midlet.showMap(true);
        }       

    }    
  
    // http://forum.java.sun.com/thread.jspa?threadID=341790&messageID=1408555
    private String URLencodeSpaces(String s)
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
