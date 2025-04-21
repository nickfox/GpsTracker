// # android/app/src/main/java/com/websmithing/gpstracker2/GpsTrackerActivity.kt
package com.websmithing.gpstracker2

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.EditorInfo
import timber.log.Timber
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch 
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import com.websmithing.gpstracker2.data.repository.UploadStatus 
import com.websmithing.gpstracker2.ui.TrackingViewModel 
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat 
import java.text.SimpleDateFormat 
import java.util.*
import android.location.Location 
import android.widget.TextView 

/**
 * Main activity for the GPS Tracker application.
 *
 * This activity serves as the primary user interface for the GPS tracking functionality.
 * It handles:
 * - User configuration (username, server URL, tracking interval)
 * - Permission management for location tracking
 * - Starting and stopping the tracking service
 * - Displaying real-time location data and tracking statistics
 * - Communicating with the backend ViewModel that manages data and services
 *
 * The activity is integrated with Hilt for dependency injection.
 */
@AndroidEntryPoint
class GpsTrackerActivity : AppCompatActivity() {

    // --- UI Elements ---
    /**
     * Text field for entering the username to identify this device's tracking data
     */
    private lateinit var txtUserName: EditText
    
    /**
     * Text field for entering the website URL where tracking data will be sent
     */
    private lateinit var txtWebsite: EditText
    
    /**
     * Radio group for selecting the tracking interval (1, 5, or 15 minutes)
     */
    private lateinit var intervalRadioGroup: RadioGroup
    
    /**
     * Button that toggles tracking on/off
     */
    private lateinit var trackingButton: Button
    
    /**
     * TextView for displaying the current speed
     */
    private lateinit var tvSpeed: TextView
    
    /**
     * TextView for displaying the current latitude and longitude
     */
    private lateinit var tvLatLon: TextView
    
    /**
     * TextView for displaying the current altitude
     */
    private lateinit var tvAltitude: TextView
    
    /**
     * TextView for displaying the accuracy of the location reading
     */
    private lateinit var tvAccuracy: TextView
    
    /**
     * TextView for displaying the current bearing (direction)
     */
    private lateinit var tvBearing: TextView
    
    /**
     * TextView for displaying the total distance traveled
     */
    private lateinit var tvDistance: TextView
    
    /**
     * TextView for displaying the timestamp of the last update and its status
     */
    private lateinit var tvLastUpdate: TextView
    
    /**
     * TextView for displaying the current GPS signal strength
     */
    private lateinit var tvSignalStrength: TextView

    // --- ViewModel ---
    /**
     * ViewModel that manages application state, tracking logic, and data operations
     */
    private val viewModel: TrackingViewModel by viewModels()

    // --- Formatting ---
    /**
     * Formatter for displaying latitude and longitude with 5 decimal places
     */
    private val coordinateFormatter = DecimalFormat("0.00000")
    
    /**
     * Formatter for displaying distance with 1 decimal place
     */
    private val distanceFormatter = DecimalFormat("0.0")
    
    /**
     * Formatter for displaying time in HH:mm:ss format
     */
    private val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    // --- Permission Handling ---
    /**
     * Activity result launcher for requesting multiple location permissions
     *
     * Handles the result of requesting foreground location permissions and
     * proceeds to request background permissions if needed.
     */
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocationGranted || coarseLocationGranted) {
                Timber.i("Foreground location permission granted.")
                checkAndRequestBackgroundLocationPermission()
            } else {
                Timber.w("Foreground location permission denied.")
                showPermissionDeniedSnackbar(getString(R.string.permission_denied_foreground_location))
                viewModel.forceStopTracking()
            }
        }

    /**
     * Activity result launcher for requesting background location permission
     *
     * Handles the result of requesting the background location permission
     * and starts tracking if granted.
     */
     private val requestBackgroundPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Timber.i("Background location permission granted.")
                viewModel.startTracking()
            } else {
                Timber.w("Background location permission denied.")
                showPermissionDeniedSnackbar(getString(R.string.permission_denied_background_location))
                viewModel.forceStopTracking()
            }
        }

    // --- Activity Lifecycle ---
    /**
     * Called when the activity is first created.
     *
     * Initializes the UI, sets up listeners and observers, and checks for first-time app setup.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down, this contains the data it most recently supplied in
     *                           onSaveInstanceState(Bundle).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps_tracker)

        setupToolbar()
        bindViews()
        setupListeners()
        observeViewModel()

        checkFirstTimeLoading()
        checkIfGooglePlayEnabled()
    }

    // --- Setup Methods ---
    /**
     * Configures the app's toolbar with the app logo
     */
    private fun setupToolbar() {
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(true)
            setLogo(R.mipmap.ic_launcher)
            setDisplayUseLogoEnabled(true)
        }
    }

    /**
     * Binds all view references and sets initial display values
     */
    private fun bindViews() {
        // Settings & Control
        txtWebsite = findViewById(R.id.txtWebsite)
        txtUserName = findViewById(R.id.txtUserName)
        intervalRadioGroup = findViewById(R.id.intervalRadioGroup)
        trackingButton = findViewById(R.id.trackingButton)
        txtUserName.imeOptions = EditorInfo.IME_ACTION_DONE

        // Display Fields
        tvSpeed = findViewById(R.id.tvSpeed)
        tvLatLon = findViewById(R.id.tvLatLon)
        tvAltitude = findViewById(R.id.tvAltitude)
        tvAccuracy = findViewById(R.id.tvAccuracy)
        tvBearing = findViewById(R.id.tvBearing)
        tvDistance = findViewById(R.id.tvDistance)
        tvLastUpdate = findViewById(R.id.tvLastUpdate)
        tvSignalStrength = findViewById(R.id.tvSignalStrength)

        // Set initial default text (could also be done in XML)
        updateLocationDisplay(null)
        updateDistanceDisplay(0f)
        updateUploadStatusDisplay(UploadStatus.Idle, null)
    }

    /**
     * Sets up all UI element listeners for user interaction
     */
    private fun setupListeners() {
        trackingButton.setOnClickListener { handleTrackingButtonClick() }

        intervalRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val newInterval = when (checkedId) {
                R.id.i1 -> 1
                R.id.i5 -> 5
                R.id.i15 -> 15
                else -> 1
            }
            viewModel.onIntervalChanged(newInterval)
        }

        txtUserName.addTextChangedListener { editable ->
            val name = editable.toString()
            if (name.isNotBlank() && !hasSpaces(name)) {
                 viewModel.onUserNameChanged(name)
                 txtUserName.error = null
            } else {
                 if (name.isNotBlank() && hasSpaces(name)) {
                      txtUserName.error = getString(R.string.username_error_spaces)
                 } else if (name.isBlank()) {
                      txtUserName.error = getString(R.string.username_error_empty)
                 }
            }
        }

        txtWebsite.addTextChangedListener { editable ->
            val url = editable.toString()
            if (url.isNotBlank() && !hasSpaces(url)) {
                viewModel.onWebsiteUrlChanged(url)
                txtWebsite.error = null
            } else {
                 if (url.isBlank()) {
                      txtWebsite.error = getString(R.string.website_error_empty)
                 } else if (hasSpaces(url)) {
                      txtWebsite.error = getString(R.string.website_error_spaces)
                 }
            }
        }
    }

    /**
     * Sets up observers for ViewModel data changes to update the UI accordingly
     */
    private fun observeViewModel() {
        // Observe Tracking State
        viewModel.isTracking.observe(this) { isTracking ->
            Timber.d("Observed isTracking state: $isTracking")
            setTrackingButtonState(isTracking)
            // Reset display fields when tracking stops
            if (!isTracking) {
                 updateLocationDisplay(null)
                 updateDistanceDisplay(0f)
                 updateUploadStatusDisplay(UploadStatus.Idle, null)
            }
        }

        viewModel.userName.observe(this) { name ->
            if (txtUserName.text.toString() != name) {
                txtUserName.setText(name)
            }
        }

        viewModel.websiteUrl.observe(this) { url ->
            if (txtWebsite.text.toString() != url) {
                txtWebsite.setText(url)
            }
        }

        // Observe Location Data using Flows (using repeatOnLifecycle)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe Latest Location
                viewModel.latestLocation.collect { location ->
                    updateLocationDisplay(location)
                    // Update upload status display as well, as it includes timestamp
                    updateUploadStatusDisplay(viewModel.lastUploadStatus.value, location?.time)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe Total Distance
                viewModel.totalDistance.collect { distanceMeters ->
                    updateDistanceDisplay(distanceMeters)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe Upload Status
                viewModel.lastUploadStatus.collect { status ->
                    updateUploadStatusDisplay(status, viewModel.latestLocation.value?.time)
                }
            }
        }

        viewModel.trackingInterval.observe(this) { interval ->
            val checkId = when (interval) {
                1 -> R.id.i1
                5 -> R.id.i5
                15 -> R.id.i15
                else -> R.id.i1
            }
            if (intervalRadioGroup.checkedRadioButtonId != checkId) {
                 intervalRadioGroup.check(checkId)
            }
        }

        // Observe Snackbar Messages
        viewModel.snackbarMessage.observe(this) { message ->
            message?.let {
                Snackbar.make(findViewById(android.R.id.content), it, Snackbar.LENGTH_SHORT).show()
                viewModel.onSnackbarMessageShown()
            }
        }
    }

    // --- Action Handling ---

    /**
     * Handles clicks on the tracking button
     *
     * Validates inputs before starting tracking, and stops tracking if already active
     */
    private fun handleTrackingButtonClick() {
        if (!validateInputs()) return

        if (viewModel.isTracking.value == false) {
            checkAndRequestForegroundLocationPermissions()
        } else {
            viewModel.stopTracking()
        }
    }

    /**
     * Validates username and website URL inputs
     *
     * Shows appropriate error messages if validation fails.
     *
     * @return True if all inputs are valid, false otherwise
     */
     private fun validateInputs(): Boolean {
         val name = txtUserName.text.toString().trim()
         val website = txtWebsite.text.toString().trim()

         val isNameValid = name.isNotBlank() && !hasSpaces(name)
         val isWebsiteValid = website.isNotBlank() && !hasSpaces(website)

         if (!isNameValid) {
             if (name.isBlank()) {
                 txtUserName.error = getString(R.string.username_error_empty)
             } else if (hasSpaces(name)) {
                 txtUserName.error = getString(R.string.username_error_spaces)
             }
         } else {
              txtUserName.error = null
         }

         if (!isWebsiteValid) {
             if (website.isBlank()) {
                  txtWebsite.error = getString(R.string.website_error_empty)
             } else if (hasSpaces(website)) {
                  txtWebsite.error = getString(R.string.website_error_spaces)
             }
         } else {
              txtWebsite.error = null
         }

         if (!isNameValid || !isWebsiteValid) {
              Toast.makeText(this, R.string.textfields_empty_or_spaces, Toast.LENGTH_LONG).show()
         }

         return isNameValid && isWebsiteValid
     }

    // --- Permission Logic ---

    /**
     * Checks for and requests foreground location permissions if needed
     *
     * Shows appropriate rationale dialogs if the user has previously denied permissions
     */
    private fun checkAndRequestForegroundLocationPermissions() {
        when {
            hasLocationPermissions() -> {
                Timber.d("Foreground location permissions already granted.")
                checkAndRequestBackgroundLocationPermission()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                Timber.d("Showing rationale for foreground location permission.")
                showPermissionRationaleDialog(
                    getString(R.string.permission_rationale_foreground_location_title),
                    getString(R.string.permission_rationale_foreground_location_message)
                ) { requestForegroundLocationPermissions() }
            }
        
            else -> {
                Timber.d("Requesting foreground location permissions.")
                requestForegroundLocationPermissions()
            }
        }
    }

    /**
     * Checks for and requests background location permission if needed
     *
     * Shows appropriate rationale dialogs if the user has previously denied the permission
     */
     private fun checkAndRequestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || hasBackgroundLocationPermission()) {
            Timber.d("Background permission granted or not required.")
            viewModel.startTracking()
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            Timber.d("Showing rationale for background location permission.")
            showPermissionRationaleDialog(
                getString(R.string.permission_rationale_background_location_title),
                getString(R.string.permission_rationale_background_location_message)
            ) { requestBackgroundLocationPermission() }
        } else {
            Timber.d("Requesting background location permission.")
            showBackgroundPermissionPreRequestDialog()
        }
    }

    /**
     * Checks if the app has necessary foreground location permissions
     *
     * @return True if either fine or coarse location permission is granted
     */
    private fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if the app has background location permission
     *
     * @return True if background location permission is granted or if running on Android < Q
     */
     private fun hasBackgroundLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else { true }
    }

    /**
     * Requests foreground location permissions (and notification permission on Android 13+)
     */
    private fun requestForegroundLocationPermissions() {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        // Add Notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    /**
     * Requests background location permission on Android Q (10) and above
     */
     private fun requestBackgroundLocationPermission() {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestBackgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
         }
     }
 
     // --- UI Update Helpers ---
 
    /**
     * Updates the location-related display fields with data from the provided location
     *
     * @param location The location to display, or null to reset to default values
     */
     private fun updateLocationDisplay(location: Location?) {
         if (location != null) {
             tvLatLon.text = getString(R.string.lat_lon_format,
                 coordinateFormatter.format(location.latitude),
                 coordinateFormatter.format(location.longitude))
             tvSpeed.text = getString(R.string.speed_format_kmh, location.speed * 3.6) // m/s to km/h
             tvAltitude.text = getString(R.string.altitude_format, location.altitude)
             tvAccuracy.text = getString(R.string.accuracy_format, location.accuracy)
             tvBearing.text = getString(R.string.bearing_format, location.bearing)
             tvSignalStrength.text = getString(R.string.signal_strength_format, getSignalStrengthDescription(location.accuracy))
         } else {
             // Set default text when location is null (e.g., tracking stopped)
             tvLatLon.text = getString(R.string.lat_lon_default)
             tvSpeed.text = getString(R.string.speed_default)
             tvAltitude.text = getString(R.string.altitude_default)
             tvAccuracy.text = getString(R.string.accuracy_default)
             tvBearing.text = getString(R.string.bearing_default)
             tvSignalStrength.text = getString(R.string.signal_strength_default)
         }
     }
 
    /**
     * Updates the distance display with the provided distance in meters
     *
     * @param distanceMeters The distance to display in meters
     */
     private fun updateDistanceDisplay(distanceMeters: Float) {
         val distanceKm = distanceMeters / 1000f
         tvDistance.text = getString(R.string.distance_format_km, distanceFormatter.format(distanceKm))
     }
 
    /**
     * Updates the upload status display with the provided status and timestamp
     *
     * @param status The upload status to display
     * @param lastLocationTime The timestamp of the last location, or null if not available
     */
     private fun updateUploadStatusDisplay(status: UploadStatus, lastLocationTime: Long?) {
         val timeString = if (lastLocationTime != null) timeFormatter.format(Date(lastLocationTime)) else "--:--:--"
         val statusText = when (status) {
             is UploadStatus.Idle -> getString(R.string.upload_status_idle)
             is UploadStatus.Success -> getString(R.string.upload_status_success, timeString)
             is UploadStatus.Failure -> getString(R.string.upload_status_failure, timeString, status.errorMessage ?: "Unknown error")
         }
         tvLastUpdate.text = statusText
     }
 
    /**
     * Gets a human-readable description of signal strength based on accuracy
     *
     * @param accuracy The accuracy of the location in meters
     * @return A string describing the signal strength
     */
     private fun getSignalStrengthDescription(accuracy: Float): String {
         return when {
             accuracy <= 0 -> getString(R.string.signal_unknown) // Accuracy shouldn't be <= 0
             accuracy <= 10 -> getString(R.string.signal_excellent) // meters
             accuracy <= 25 -> getString(R.string.signal_good)
             accuracy <= 50 -> getString(R.string.signal_fair)
             else -> getString(R.string.signal_poor)
         }
     }

    // --- UI Feedback for Permissions ---

    /**
     * Shows a dialog explaining why a permission is needed
     *
     * @param title The dialog title
     * @param message The explanation message
     * @param onPositive Callback for when the user clicks the positive button
     */
    private fun showPermissionRationaleDialog(title: String, message: String, onPositive: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.permission_button_grant) { _, _ -> onPositive() }
            .setNegativeButton(R.string.permission_button_deny) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    /**
     * Shows a pre-request dialog for background location permission
     *
     * This is needed because Android requires a separate permission request for background location,
     * and the user needs to be informed about this.
     */
     private fun showBackgroundPermissionPreRequestDialog() {
         AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_rationale_background_location_title))
            .setMessage(getString(R.string.permission_rationale_background_location_pre_request))
            .setPositiveButton(R.string.permission_button_continue) { _, _ -> requestBackgroundLocationPermission() }
            .setNegativeButton(R.string.permission_button_cancel) { dialog, _ -> dialog.dismiss() }
            .show()
     }

    /**
     * Shows a snackbar when permission is denied with a link to app settings
     *
     * @param message The message to display
     */
    private fun showPermissionDeniedSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .setAction(R.string.permission_button_settings) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .show()
    }

    // --- UI Update Method ---

    /**
     * Updates the tracking button appearance based on tracking state
     *
     * @param isTracking Whether tracking is currently active
     */
    private fun setTrackingButtonState(isTracking: Boolean) {
        if (isTracking) {
            trackingButton.setBackgroundResource(R.drawable.red_tracking_button)
            trackingButton.setTextColor(Color.WHITE)
            trackingButton.setText(R.string.tracking_is_on)
        } else {
            trackingButton.setBackgroundResource(R.drawable.green_tracking_button)
            trackingButton.setTextColor(Color.BLACK)
            trackingButton.setText(R.string.tracking_is_off)
        }
    }

    // --- Utility / Other Methods ---

    /**
     * Checks if a string contains spaces
     *
     * @param str The string to check
     * @return True if the string contains spaces, false otherwise
     */
    private fun hasSpaces(str: String): Boolean {
        return str.split(" ").size > 1
    }

    /**
     * Performs first-time app setup
     *
     * Generates a unique app ID and stores it in SharedPreferences along with
     * a flag indicating the app has been run at least once.
     */
    private fun checkFirstTimeLoading() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val firstTimeLoadingApp = prefs.getBoolean(KEY_FIRST_TIME_LOADING, true)
        if (firstTimeLoadingApp) {
            prefs.edit().apply {
                putBoolean(KEY_FIRST_TIME_LOADING, false)
                putString(KEY_APP_ID, UUID.randomUUID().toString())
                apply()
            }
            Timber.d("First time loading setup complete.")
        }
    }

    /**
     * Checks if Google Play Services is available and enabled
     *
     * Shows an appropriate error dialog if Google Play Services is unavailable
     * or needs to be updated.
     *
     * @return True if Google Play Services is available and up-to-date
     */
    private fun checkIfGooglePlayEnabled(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode == ConnectionResult.SUCCESS) {
            return true
        } else {
            Timber.e("Google Play Services check failed with code: $resultCode")
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                 googleApiAvailability.getErrorDialog(this, resultCode, 9000)?.show()
            } else {
                 Toast.makeText(applicationContext, R.string.google_play_services_unavailable, Toast.LENGTH_LONG).show()
            }
            return false
        }
    }

    /**
     * Constants used by the application
     */
    companion object {
        private const val TAG = "GpsTrackerActivity"
        // Constants needed for SharedPreferences checkFirstTimeLoading
        private const val PREFS_NAME = "com.websmithing.gpstracker.prefs"
        private const val KEY_FIRST_TIME_LOADING = "firstTimeLoadingApp"
        private const val KEY_APP_ID = "appID"
    }
}