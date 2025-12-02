// # android/app/src/main/java/com/websmithing/gpstracker2/di/AppModule.kt
package com.websmithing.gpstracker2.di

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt dependency injection module for providing application-level singleton dependencies.
 *
 * This module uses Dagger Hilt's [Provides] annotation to create and configure
 * instances of various dependencies used throughout the application, including:
 * - SharedPreferences for persistent storage
 * - FusedLocationProviderClient for location services
 * - OkHttpClient and Retrofit for networking
 *
 * All dependencies provided by this module are scoped as singletons, meaning they
 * will be created once and reused throughout the application's lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- SharedPreferences ---
    /**
     * Name for the application's SharedPreferences file
     */
    private const val PREFS_NAME = "com.websmithing.gpstracker2.prefs"

    /**
     * Provides a singleton instance of SharedPreferences.
     *
     * @param context The application context
     * @return A SharedPreferences instance for persistent storage
     */
    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // --- Location ---
    /**
     * Provides a singleton instance of FusedLocationProviderClient.
     *
     * FusedLocationProviderClient is the main entry point for interacting with the 
     * Google Play Services location APIs.
     *
     * @param context The application context
     * @return A FusedLocationProviderClient instance for location services
     */
    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    // --- Network ---
    /**
     * Provides a singleton instance of OkHttpClient.
     *
     * Configures the HTTP client with logging interceptors and timeouts.
     * Longer timeouts are used to accommodate potential network issues when
     * uploading location data from areas with poor connectivity.
     *
     * @return A configured OkHttpClient instance
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            // TODO: Set level based on BuildConfig.DEBUG later
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provides a singleton instance of Retrofit.Builder.
     *
     * Unlike a typical Retrofit configuration, this provides a builder without
     * setting a base URL. This allows the repository to dynamically set the base URL
     * at runtime based on user settings.
     *
     * @param okHttpClient The OkHttpClient to use for HTTP requests
     * @return A Retrofit.Builder instance configured with the OkHttpClient and converter factory
     */
    @Provides
    @Singleton
    fun provideRetrofitBuilder(okHttpClient: OkHttpClient): Retrofit.Builder {
        return Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            // No base URL here, it will be set dynamically in the repository
    }
}