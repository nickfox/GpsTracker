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


public class GpsTrackerActivity extends ActionBarActivity implements LocationListener, GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private LocationRequest locationRequest;
    private LocationClient locationClient;

    private static TextView longitudeTextView;
    private static TextView latitudeTextView;
    private static TextView accuracyTextView;
    private static TextView providerTextView;
    private static TextView timeStampTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpstracker);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

/*
        // create a new global location parameters object
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(60 * 1000); // interval ceiling
*/
        int response = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(response == ConnectionResult.SUCCESS){
            locationClient = new LocationClient(this,this,this);
            locationClient.connect();
        }
        else{
            Log.e("GpsTrackerActivity", "google play service error: " + response);
        }
    }

    // called when startTrackingButton is tapped
    public void startTracking(View v) {
        Log.e("GpsTrackerActivity", "startTracking");
        ((Button) v).setText("stop tracking");

        if (locationClient != null && locationClient.isConnected()) {
            Location location = locationClient.getLastLocation();
            longitudeTextView.setText("longitude: " + location.getLongitude());
            latitudeTextView.setText("latitude: " + location.getLatitude());

            accuracyTextView.setText("accuracy: " + location.getAccuracy());
            providerTextView.setText("provider: " + location.getProvider());
            timeStampTextView.setText("timeStamp: " + location.getTime());
        }
    }

    // called when startTrackingButton is tapped
    public void stopTracking(View v) {
        Log.e("GpsTrackerActivity", "stopTracking");
        ((Button) v).setText("start tracking");

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        //mLocationClient.connect();
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        //mLocationClient.disconnect();
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location location) {

        //  LocationUtils.getLatLng(this, location);
    }

    /**
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {

    }

    /**
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
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

    /**
     * A placeholder fragment containing a simple view.
     */
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
