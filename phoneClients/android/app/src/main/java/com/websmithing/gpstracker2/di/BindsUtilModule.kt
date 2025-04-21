// # android/app/src/main/java/com/websmithing/gpstracker2/di/BindsUtilModule.kt
package com.websmithing.gpstracker2.di

import com.websmithing.gpstracker2.util.PermissionChecker
import com.websmithing.gpstracker2.util.PermissionCheckerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt dependency injection module for binding interface implementations using the @Binds annotation.
 *
 * This module demonstrates the use of Dagger's @Binds annotation, which is more efficient
 * than @Provides for binding an implementation to an interface. Unlike @Provides, which
 * requires an explicit instance creation, @Binds simply tells Dagger how to map an existing
 * binding to another type (typically an interface to its implementation).
 *
 * The module is abstract because @Binds methods are abstract and don't contain implementation code.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class BindsUtilModule {

    /**
     * Binds the PermissionCheckerImpl implementation to the PermissionChecker interface.
     *
     * This allows Dagger to provide an instance of PermissionChecker when it's requested
     * for injection, by automatically using the PermissionCheckerImpl implementation.
     * This binding is scoped as a singleton to ensure consistent permission checking
     * throughout the application.
     *
     * @param permissionCheckerImpl The implementation of PermissionChecker to bind
     * @return The bound PermissionChecker interface
     */
    @Binds
    @Singleton
    abstract fun bindPermissionChecker(
        permissionCheckerImpl: PermissionCheckerImpl
    ): PermissionChecker
}