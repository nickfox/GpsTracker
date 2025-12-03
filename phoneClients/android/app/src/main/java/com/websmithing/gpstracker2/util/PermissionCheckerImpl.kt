// # android/app/src/main/java/com/websmithing/gpstracker2/util/PermissionCheckerImpl.kt
package com.websmithing.gpstracker2.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default implementation of [PermissionChecker] using Android's [ContextCompat].
 * 
 * This class uses Android's permission checking mechanisms to determine if the app
 * has been granted the required location permissions.
 * 
 * It is provided as a singleton through Hilt dependency injection to ensure
 * consistent permission checking throughout the application.
 */
@Singleton
class PermissionCheckerImpl @Inject constructor(
    @ApplicationContext private val appContext: Context
) : PermissionChecker {

    /**
     * Checks if either ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION permission is granted.
     * 
     * Uses [ContextCompat.checkSelfPermission] to safely check permission status on
     * all Android versions.
     *
     * @return true if either location permission is granted, false otherwise.
     */
    override fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            appContext, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            appContext, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun hasBackgroundLocationPermissions(): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContextCompat.checkSelfPermission(
                    appContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
        }
    }
}