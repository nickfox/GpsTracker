package com.websmithing.gpstracker;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

public class GpsTrackerActivity extends ActionBarActivity implements LocationListener, GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private static final String TAG = "GpsTrackerActivity";
    private static TextView longitudeTextView;
    private static TextView latitudeTextView;
    private static TextView accuracyTextView;
    private static TextView providerTextView;
    private static TextView timeStampTextView;

    private LocationRequest locationRequest;
    private LocationClient locationClient;
    private Location previousLocation;
    private float totalDistanceInMeters = 0.0f;
    private boolean firstTimeGettingPosition = true;
    private String sessionID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpstracker);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        int response = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(response == ConnectionResult.SUCCESS){
            locationClient = new LocationClient(this,this,this);
            locationClient.connect();
        }
        else{
            Log.e(TAG, "google play service error: " + response);
        }
    }

    // called when startTrackingButton is tapped
    public void startTracking(View v) {
        ((Button) v).setText("stop tracking");

        sessionID = UUID.randomUUID().toString();
        totalDistanceInMeters = 0.0f;

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(60 * 1000);
        locationRequest.setFastestInterval(60 * 1000); // the fastest rate in milliseconds at which your app can handle location updates
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationClient.requestLocationUpdates(locationRequest, this);

        //oneTimeLocationUpdate();
    }

    protected void changeInterval(int intervalInMinutes) {
        locationRequest.setInterval(intervalInMinutes * 1000);
        locationRequest.setFastestInterval(intervalInMinutes * 1000);
    }

    protected void oneTimeLocationUpdate() {
        Log.e(TAG, "oneTimeLocationUpdate");

        if (locationClient != null && locationClient.isConnected()) {
            Location location = locationClient.getLastLocation();
            displayLocationData(location);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            displayLocationData(location);
            sendLocationDataToWebsite(location);
        }
    }

    protected void  sendLocationDataToWebsite(Location location) {
        // formatted for mysql datetime format
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        Date date = new Date(location.getTime());

        if (firstTimeGettingPosition) {
            firstTimeGettingPosition = false;
        } else {
            float distance = location.distanceTo(previousLocation);
            totalDistanceInMeters += distance;
        }

        previousLocation = location;

        RequestParams requestParams = new RequestParams();
            requestParams.put("latitude", Double.toString(location.getLatitude()));
            requestParams.put("longitude", Double.toString(location.getLongitude()));
            requestParams.put("speed", Double.toString(location.getSpeed())); // in miles per hour

        try {
            requestParams.put("date", URLEncoder.encode(dateFormat.format(date), "UTF-8"));
        } catch (UnsupportedEncodingException e) {}

            requestParams.put("locationmethod", location.getProvider());

            if ( totalDistanceInMeters > 0) {
                requestParams.put("distance", totalDistanceInMeters / 1609); // in miles
            } else {
                requestParams.put("distance", 0); // in miles
            }

            requestParams.put("phonenumber", "androidUser");
            requestParams.put("sessionid", sessionID); // uuid
            requestParams.put("accuracy", Float.toString(location.getAccuracy())); // in meters
            requestParams.put("extrainfo",  Double.toString(location.getAltitude()));
            requestParams.put("eventtype", "android");
            requestParams.put("direction", Float.toString(location.getBearing()));

        MyHttpClient.post(null, requestParams, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] responseBody) {
                try {
                     String response = new String(responseBody, "UTF-8");
                    Log.e(TAG, "onSuccess statusCode: " + statusCode + " responseBody: " + response);
                } catch (UnsupportedEncodingException e) {}
            }
            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, byte[] errorResponse, Throwable e) {
                Log.e(TAG, "onFailure statusCode: " + statusCode + " errorResponse: " + errorResponse);
             }
        });
    }

    protected void displayLocationData(Location location) {
        DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        Date date = new Date(location.getTime());

        longitudeTextView.setText("longitude: " + location.getLongitude());
        latitudeTextView.setText("latitude: " + location.getLatitude());
        accuracyTextView.setText("accuracy: " + location.getAccuracy());
        providerTextView.setText("provider: " + location.getProvider());
        timeStampTextView.setText("timeStamp: " + dateFormat.format(date));

        Log.e(TAG, dateFormat.format(date) +  " accuracy: " + location.getAccuracy());
    }

    public void stopTracking(View v) {
        Log.e(TAG, "stopTracking");
        ((Button) v).setText("start tracking");
    }

    @Override
    protected void onStart() {
        Log.e(TAG, "onStart");
        super.onStart();
        // Connect the client.
        //mLocationClient.connect();
    }

    @Override
    protected void onStop() {
        Log.e(TAG, "onStop");

        if (locationClient != null && locationClient.isConnected()) {
            locationClient.removeLocationUpdates(this);
            locationClient.disconnect();
        }

        super.onStop();
    }

    /**
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.e(TAG, "onConnected");

    }

    /**
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        Log.e(TAG, "onDisconnected");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gps_tracker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_gpstracker, container, false);
            longitudeTextView = (TextView)rootView.findViewById(R.id.longitudeTextView);
            latitudeTextView = (TextView)rootView.findViewById(R.id.latitudeTextView);
            accuracyTextView = (TextView)rootView.findViewById(R.id.accuracyTextView);
            providerTextView = (TextView)rootView.findViewById(R.id.providerTextView);
            timeStampTextView = (TextView)rootView.findViewById(R.id.timeStampTextView);
            return rootView;
        }
    }
}
