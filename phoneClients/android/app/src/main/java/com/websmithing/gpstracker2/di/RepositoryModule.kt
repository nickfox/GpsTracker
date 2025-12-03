// # android/app/src/main/java/com/websmithing/gpstracker2/di/RepositoryModule.kt
package com.websmithing.gpstracker2.di

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.location.FusedLocationProviderClient
import com.websmithing.gpstracker2.data.repository.LocationRepository
import com.websmithing.gpstracker2.data.repository.SettingsRepository
import com.websmithing.gpstracker2.data.repository.SettingsRepositoryImpl
import com.websmithing.gpstracker2.data.repository.WialonIpsLocationRepositoryImpl
import com.websmithing.gpstracker2.util.PermissionChecker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Hilt dependency injection module for providing repository dependencies.
 *
 * This module provides implementations of the application's repository interfaces,
 * which serve as the data layer of the application. The repositories abstract
 * the data sources (local storage, network, etc.) from the rest of the application.
 *
 * All repositories are provided as singletons to ensure consistent data access
 * throughout the application.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * Provides a singleton implementation of the SettingsRepository interface.
     *
     * The SettingsRepository handles user preferences and app settings,
     * persisting them using SharedPreferences.
     *
     * @param sharedPreferences The SharedPreferences instance for storing settings
     * @return An implementation of SettingsRepository
     */
    @Provides
    @Singleton
    fun provideSettingsRepository(sharedPreferences: SharedPreferences): SettingsRepository {
        return SettingsRepositoryImpl(sharedPreferences)
    }

    /**
     * Provides a singleton implementation of the LocationRepository interface.
     *
     * The LocationRepository handles location data operations:
     * - Fetching the current location
     * - Calculating distance traveled
     * - Uploading location data to the server
     * - Managing location state
     *
     * @param context The application context
     * @param fusedLocationProviderClient Client for accessing location services
     * @param okHttpClient HTTP client for network requests
     * @param retrofitBuilder Builder for creating Retrofit instances with dynamic base URLs
     * @param settingsRepository Repository for accessing user settings
     * @param permissionChecker Utility for checking location permissions
     * @return An implementation of LocationRepository
     */
    @Provides
    @Singleton
    fun provideLocationRepository(
        @ApplicationContext context: Context,
        fusedLocationProviderClient: FusedLocationProviderClient,
        okHttpClient: OkHttpClient,
        retrofitBuilder: Retrofit.Builder,
        settingsRepository: SettingsRepository,
        permissionChecker: PermissionChecker
    ): LocationRepository {
        return WialonIpsLocationRepositoryImpl(
            context,
            fusedLocationProviderClient,
            settingsRepository,
            permissionChecker
        )
    }
}