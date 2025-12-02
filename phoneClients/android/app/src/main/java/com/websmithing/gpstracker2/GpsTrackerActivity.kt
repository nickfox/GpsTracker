package com.websmithing.gpstracker2

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import com.websmithing.gpstracker2.data.repository.SettingsRepository
import com.websmithing.gpstracker2.di.SettingsRepositoryEntryPoint
import com.websmithing.gpstracker2.ui.TrackingViewModel
import com.websmithing.gpstracker2.ui.components.*
import com.websmithing.gpstracker2.util.LocaleHelper
import com.websmithing.gpstracker2.util.PermissionChecker
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class GpsTrackerActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var permissionChecker: PermissionChecker

    private val viewModel: TrackingViewModel by viewModels()
    private lateinit var prefs: SharedPreferences
    private var showNotifyBanner by mutableStateOf(false)
    private var notifyStatus by mutableStateOf(NotifyStatus.Success)
    private var permissionsRequestedInThisSession = false
    private var firstLaunchWithoutPermissions = true

    // --- Permission Handling ---

    /**
     * Activity result launcher for requesting multiple location permissions
     *
     * Handles the result of requesting foreground location permissions and
     * proceeds to request background permissions if needed.
     */
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineGranted || coarseGranted) {
                Timber.i("Foreground permissions granted")

                if (permissionsRequestedInThisSession) {
                    Timber.i("Permissions granted after request - recreating activity")
                    window.decorView.post {
                        recreate()
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    !permissionChecker.hasBackgroundLocationPermissions()
                ) {
                    requestBackgroundLocationPermission()
                }
            } else {
                showPermissionDeniedSnackbar(getString(R.string.permission_denied_foreground_location))
            }
        }

    /**
     * Activity result launcher for requesting background location permission
     *
     * Handles the result of requesting the background location permission
     * and starts tracking if granted.
     */
    private val requestBackgroundPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                Timber.i("Background permission granted")
                if (permissionsRequestedInThisSession) {
                    Timber.i("Background permissions granted after request - recreating activity")
                    window.decorView.post {
                        recreate()
                    }
                }
            } else {
                showPermissionDeniedSnackbar(getString(R.string.permission_denied_background_location))
            }
        }

    // --- Activity Lifecycle ---

    override fun attachBaseContext(newBase: Context) {
        // 1. Get the EntryPoint accessor from the application context
        val entryPoint = EntryPointAccessors.fromApplication(
            newBase.applicationContext,
            SettingsRepositoryEntryPoint::class.java
        )

        // 2. Use the EntryPoint to get the repository instance
        val repo = entryPoint.getSettingsRepository()

        // 3. Use your LocaleHelper to create the new context
        val newCtx = LocaleHelper.onAttach(newBase, repo)

        super.attachBaseContext(newCtx)
    }

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

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        determineFirstLaunchWithoutPermissions()
        checkPermissionsAtStartup()

        setContent {
            var menuVisible by remember { mutableStateOf(false) }
            val username by viewModel.userName.observeAsState("")
            Surface(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val markerColor = when {
                        !menuVisible -> MarkerColor.GREY
                        menuVisible && username.isNotBlank() -> MarkerColor.BLUE
                        else -> MarkerColor.RED
                    }

                    OsmMapContainer(
                        modifier = Modifier.fillMaxSize(),
                        markerColor = markerColor,
                        onPointerClick = { menuVisible = !menuVisible }
                    )

                    SettingsButton(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(end = 16.dp, top = 8.dp),
                        onClick = {
                            val intent = Intent(this@GpsTrackerActivity, TrackerSettingsActivity::class.java)
                            startActivityForResult(intent, SETTINGS_REQUEST_CODE)
                        }
                    )

                    TrackingButton(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 16.dp),
                        status = getTrackingButtonState(viewModel),
                        onClick = { handleTrackingButtonClick(username) }
                    )

                    BottomSwipeMenu(
                        visible = menuVisible,
                        onClose = { menuVisible = false },
                        viewModel = viewModel,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )

                    NotifyBanner(
                        notifyStatus = notifyStatus,
                        visible = showNotifyBanner,
                        onDismiss = { showNotifyBanner = false }
                    )
                }
            }
        }

        checkFirstTimeLoading()
        checkIfGooglePlayEnabled()
    }


    /**
     * Handles activity results from launched intents.
     *
     * This callback is triggered when an activity started with startActivityForResult() exits.
     * Currently handles:
     * - Settings activity result: Refreshes settings from repository and recreates activity
     *   to apply updated settings (like language changes)
     *
     * @param requestCode The integer request code originally supplied
     * @param resultCode The integer result code returned by the child activity
     * @param data An Intent that carries the result data
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETTINGS_REQUEST_CODE) {
            viewModel.refreshSettingsFromRepository()
            recreate()
        }
    }

    // --- Handlers ---

    /**
     * Handles tracking button click events with validation and permission checks.
     *
     * This function:
     * 1. Stops tracking if already active
     * 2. Validates username before proceeding
     * 3. Checks required permissions before starting tracking
     * 4. Shows appropriate notification banners
     *
     * @param username Current username from the form for validation
     */
    private fun handleTrackingButtonClick(username: String) {
        if (viewModel.isTracking.value == true) {
            viewModel.stopTracking()
            return
        }

        if (username.isEmpty()) {
            showNotifyBanner = true
            notifyStatus = NotifyStatus.Error
            return
        }

        checkPermissionsForButton {
            viewModel.startTracking()
            showNotifyBanner = true
            notifyStatus = NotifyStatus.Success
        }
    }

    // --- Permissions logic ---

    /**
     * Determines if this is the first app launch without required permissions.
     *
     * This method checks current location permission status to determine if it's the
     * first time the app is launched without proper permissions. The result is used
     * to potentially show permission guidance or different UI states for new users.
     *
     * Checks include:
     * - Basic location permissions
     * - Background location permissions (Android 10/Q and above)
     */
    private fun determineFirstLaunchWithoutPermissions() {
        val hasPermissionsNow = permissionChecker.hasLocationPermissions() &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                        permissionChecker.hasBackgroundLocationPermissions())

        firstLaunchWithoutPermissions = !hasPermissionsNow
    }

    /**
     * Checks for required permissions during app startup.
     *
     * Verifies if location permissions are granted and handles the initial
     * permission request flow for new users. Only requests permissions on
     * first launch if they haven't been granted yet, avoiding repeated
     * requests for existing users who have previously denied permissions.
     *
     * Permission checks:
     * - Basic foreground location permissions (always required)
     * - Background location permissions (Android 10/Q+ only)
     *
     * Flow control:
     * - Only requests permissions on first app launch without permissions
     * - Tracks permission requests within current session to avoid duplicates
     */
    private fun checkPermissionsAtStartup() {
        val hasPermissionsNow = permissionChecker.hasLocationPermissions() &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                        permissionChecker.hasBackgroundLocationPermissions())

        if (!hasPermissionsNow) {
            if (firstLaunchWithoutPermissions) {
                permissionsRequestedInThisSession = true
                requestForegroundLocationPermissions()
            }
        }
    }

    /**
     * Checks and requests location permissions before performing a tracking action.
     *
     * This method handles the permission flow for starting location tracking:
     * 1. Checks if basic location permissions are granted
     * 2. If not, requests foreground location permissions
     * 3. If foreground permissions exist but background permissions are missing (Android 10+),
     *    requests background location permissions
     * 4. Only executes the provided callback when all required permissions are granted
     *
     * Tracks permission requests within the current session to avoid duplicate prompts.
     *
     * @param onPermissionsGranted Callback to execute when all required permissions are granted
     */
    private fun checkPermissionsForButton(onPermissionsGranted: () -> Unit) {
        if (permissionChecker.hasLocationPermissions()) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || permissionChecker.hasBackgroundLocationPermissions()) {
                onPermissionsGranted()
            } else {
                permissionsRequestedInThisSession = true
                requestBackgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        } else {
            permissionsRequestedInThisSession = true
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    /**
     * Requests foreground location permissions (and notification permission on Android 13+)
     */
    private fun requestForegroundLocationPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        requestPermissionLauncher.launch(permissions.toTypedArray())
    }

    /**
     * Requests background location permission on Android Q (10) and above
     */
    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionsRequestedInThisSession = true
            requestBackgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
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
            }.show()
    }

    // --- Utility ---

    /**
     * Determines the state of the tracking button based on form validation and tracking status.
     *
     * The button can be in three states:
     * - Disabled: Form is invalid (missing username, website, or interval)
     * - Tracking: Form is valid and tracking is active
     * - Stopped: Form is valid but tracking is not active
     *
     * @param viewModel ViewModel containing observed state for form fields and tracking status
     * @return Calculated TrackingButtonState
     */
    @Composable
    private fun getTrackingButtonState(viewModel: TrackingViewModel): TrackingButtonState {
        val username by viewModel.userName.observeAsState("")
        val website by viewModel.websiteUrl.observeAsState("")
        val interval by viewModel.trackingInterval.observeAsState(0)
        val isTracking by viewModel.isTracking.observeAsState(false)

        val isFormValid = username.isNotBlank() && website.isNotBlank() && interval > 0
        return when {
            !isFormValid -> TrackingButtonState.Disabled
            isTracking -> TrackingButtonState.Tracking
            else -> TrackingButtonState.Stopped
        }
    }

    /**
     * Performs first-time app setup
     *
     * Generates a unique app ID and stores it in SharedPreferences along with
     * a flag indicating the app has been run at least once.
     */
    private fun checkFirstTimeLoading() {
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
        if (resultCode == ConnectionResult.SUCCESS) return true
        else {
            Timber.e("Google Play Services unavailable: $resultCode")
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(this, resultCode, 9000)?.show()
            } else Toast.makeText(applicationContext, R.string.google_play_services_unavailable, Toast.LENGTH_LONG).show()
            return false
        }
    }

    companion object {
        private const val SETTINGS_REQUEST_CODE = 1001
        private const val PREFS_NAME = "com.waliot.tracker.prefs"
        private const val KEY_FIRST_TIME_LOADING = "firstTimeLoadingApp"
        private const val KEY_APP_ID = "appID"
    }
}