package com.websmithing.gpstracker;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.util.Locale;

public class LoopjHttpClient {
    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(url, params, responseHandler);
    }

    public static void post(String url, RequestParams requestParams, AsyncHttpResponseHandler responseHandler) {
        client.post(url, requestParams, responseHandler);
    }

    public static void debugLoopJ(String TAG, String methodName, byte[] response, Header[] headers, int statusCode, Throwable t) {
        if (headers != null) {
            Log.e(TAG, methodName)
            ;
            Log.e(TAG, "Return Headers:");
            for (Header h : headers) {
                String _h = String.format(Locale.US, "%s : %s", h.getName(), h.getValue());
                Log.e(TAG, _h);
            }

            if (t != null) {
                Log.e(TAG, "Throwable:" + t);
            }

            Log.e(TAG, "StatusCode: " + statusCode);

            if (response != null) {
                Log.e(TAG, "Resposne: " + new String(response));
            }

        }
    }
}
