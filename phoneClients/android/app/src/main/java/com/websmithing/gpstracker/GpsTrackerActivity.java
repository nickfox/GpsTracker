package com.websmithing.gpstracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.UUID;


public class GpsTrackerActivity extends ActionBarActivity {
    private static final String TAG = "GpsTrackerActivity";

    // use the websmithing defaultUploadWebsite for testing and then check your
    // location with your browser here: https://www.websmithing.com/gpstracker/displaymap.php
    private String defaultUploadWebsite;

    private static EditText txtUserName;
    private static EditText txtWebsite;
    private static Button trackingButton;
    private static Button saveButton;

    private boolean currentlyTracking;
    private RadioGroup intervalRadioGroup;
    private int intervalInMinutes = 1;
    private AlarmManager alarmManager;
    private Intent gpsTrackerIntent;
    private PendingIntent pendingIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpstracker);

        defaultUploadWebsite = getString(R.string.default_upload_website);

        txtWebsite = (EditText)findViewById(R.id.txtWebsite);
        txtUserName = (EditText)findViewById(R.id.txtUserName);
        trackingButton = (Button)findViewById(R.id.trackingButton);
        saveButton = (Button)findViewById(R.id.saveButton);
        txtUserName.setImeOptions(EditorInfo.IME_ACTION_DONE);

        SharedPreferences sharedPreferences = this.getSharedPreferences("com.websmithing.gpstracker.prefs", Context.MODE_PRIVATE);
        currentlyTracking = sharedPreferences.getBoolean("currentlyTracking", false);

        trackingButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                trackLocation(v);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveUserSettings();
            }
        });
    }

    private void startAlarmManager(Context context) {
        Log.d(TAG, "startAlarmManager");
        alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        gpsTrackerIntent = new Intent(context, GpsTrackerAlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, 0, gpsTrackerIntent, 0);

        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                intervalInMinutes * 60000, // 60000 = 1 minute
                pendingIntent);
    }

    private void cancelAlarm() {
        Log.d(TAG, "cancelAlarm");
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            alarmManager = null;
        }
    }

    // called when trackingButton is tapped
    protected void trackLocation(View v) {
        SharedPreferences sharedPreferences = this.getSharedPreferences("com.websmithing.gpstracker.prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (textFieldsAreEmptyOrHaveSpaces()) {
            return;
        } else {
            // just in case user forgets to save username
            String tempUser = sharedPreferences.getString("userName", "");
            if (tempUser.trim().length() == 0) {
                editor.putString("userName", txtUserName.getText().toString().trim());
                editor.commit();
            }
        }

        if (!checkIfGooglePlayEnabled()) {
            return;
        }

        if (currentlyTracking) {
            ((Button) v).setText(getText(R.string.start_tracking));

            cancelAlarm();

            currentlyTracking = false;
            editor.putBoolean("currentlyTracking", false);
            editor.putString("sessionID", "");
        } else {
            ((Button) v).setText(getText(R.string.stop_tracking));

            startAlarmManager(getBaseContext());

            currentlyTracking = true;
            editor.putBoolean("currentlyTracking", true);
            editor.putString("sessionID",  UUID.randomUUID().toString());
        }

        editor.commit();
    }

    private void saveUserSettings() {
        if (textFieldsAreEmptyOrHaveSpaces()) {
            return;
        }

        checkIfWebsiteIsReachable();

        SharedPreferences sharedPreferences = this.getSharedPreferences("com.websmithing.gpstracker.prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        switch (intervalRadioGroup.getCheckedRadioButtonId()) {
            case R.id.i1:
                editor.putInt("intervalInMinutes", 1);
                break;
            case R.id.i5:
                editor.putInt("intervalInMinutes", 5);
                break;
            case R.id.i15:
                editor.putInt("intervalInMinutes", 15);
                break;
            case R.id.i30:
                editor.putInt("intervalInMinutes", 30);
                break;
            case R.id.i60:
                editor.putInt("intervalInMinutes", 60);
                break;
        }

        editor.putString("userName", txtUserName.getText().toString().trim());
        editor.putString("defaultUploadWebsite", txtWebsite.getText().toString().trim());

        editor.commit();

        Toast.makeText(this, R.string.setting_saved, Toast.LENGTH_SHORT).show();
    }

    private boolean textFieldsAreEmptyOrHaveSpaces() {
        String tempUserName = txtUserName.getText().toString().trim();
        String tempWebsite = txtWebsite.getText().toString().trim();

        if (tempWebsite.length() == 0 || hasSpaces(tempWebsite) || tempUserName.length() == 0 || hasSpaces(tempUserName)) {
            Toast.makeText(this, R.string.textfields_empty_or_spaces, Toast.LENGTH_LONG).show();
            return true;
        }

        return false;
    }

    private boolean hasSpaces(String str) {
        return ((str.split(" ").length > 1) ? true : false);
    }

    private void displayUserSettings() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("com.websmithing.gpstracker.prefs", Context.MODE_PRIVATE);
        int interval = sharedPreferences.getInt("intervalInMinutes", 1);

        intervalRadioGroup = (RadioGroup)findViewById(R.id.intervalRadioGroup);

        switch (intervalInMinutes) {
            case 1:
                intervalRadioGroup.check(R.id.i1);
                break;
            case 5:
                intervalRadioGroup.check(R.id.i5);
                break;
            case 15:
                intervalRadioGroup.check(R.id.i15);
                break;
            case 30:
                intervalRadioGroup.check(R.id.i30);
                break;
            case 60:
                intervalRadioGroup.check(R.id.i60);
                break;
        }

        txtWebsite.setText(sharedPreferences.getString("defaultUploadWebsite", defaultUploadWebsite));
        txtUserName.setText(sharedPreferences.getString("userName", ""));

        if (currentlyTracking) {
            trackingButton.setText(getText(R.string.stop_tracking));
        } else {
            trackingButton.setText(getText(R.string.start_tracking));
        }
    }

    private boolean checkIfGooglePlayEnabled() {
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            return true;
        } else {
            Log.e(TAG, "unable to connect to google play services.");
            Toast.makeText(getApplicationContext(), R.string.google_play_services_unavailable, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void checkIfWebsiteIsReachable() {
        LoopjHttpClient.get(defaultUploadWebsite, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] responseBody) {
                Log.e(TAG, "checkIfWebsiteIsReachable onSuccess statusCode: " + statusCode);
            }
            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, byte[] errorResponse, Throwable e) {
                Toast.makeText(getApplicationContext(), R.string.reachability_error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "checkIfWebsiteIsReachable onFailure statusCode: " + statusCode);
            }
        });
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();  // Always call the superclass method first

        displayUserSettings();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }
}
