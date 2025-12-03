// # android/app/src/main/java/com/websmithing/gpstracker2/util/PermissionChecker.kt
package com.websmithing.gpstracker2.util

/**
 * Interface for checking app permissions.
 * 
 * This interface abstracts permission checking functionality from its implementation,
 * making it easier to test components that depend on permission checks by allowing
 * the permission checker to be mocked.
 */
interface PermissionChecker {
    /**
     * Checks if either ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION permission is granted.
     * 
     * The app requires at least one of these permissions to access the device's location.
     * ACCESS_FINE_LOCATION provides precise location, while ACCESS_COARSE_LOCATION provides
     * approximate location.
     *
     * @return true if either location permission is granted, false otherwise.
     */
    fun hasLocationPermissions(): Boolean

    /**
     * Checks if the app has background location permission
     *
     * @return True if background location permission is granted or if running on Android < Q
     */
    fun hasBackgroundLocationPermissions(): Boolean
}