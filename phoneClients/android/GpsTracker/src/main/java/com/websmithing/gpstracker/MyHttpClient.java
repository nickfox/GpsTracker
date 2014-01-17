package com.websmithing.gpstracker;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class MyHttpClient {
	
	// use the websmithing defaultUploadWebsite for testing, change the *phoneNumber* form variable to something you
	// know and then check your location with your browser here: http://www.websmithing.com/gpstracker/displaymap.php
	
    private static final String defaultUploadWebsite = "http://www.websmithing.com/gpstracker/updatelocation.php";
    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void post(String url, RequestParams requestParams, AsyncHttpResponseHandler responseHandler) {
        client.post(defaultUploadWebsite, requestParams, responseHandler);
    }
}
