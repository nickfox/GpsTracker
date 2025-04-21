// # android/app/src/main/java/com/websmithing/gpstracker2/di/ProvidesUtilModule.kt // Renamed file
package com.websmithing.gpstracker2.di

import android.content.Context
// Removed WorkManager import
// Removed WorkerScheduler import
// Removed WorkerSchedulerImpl import
// Removed PermissionChecker imports
// Removed Binds import
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
// Change back to object as we only have @Provides
object ProvidesUtilModule { // Renamed class

    // WorkerScheduler and WorkManager providers removed as they are no longer needed.

    // Removed @Binds method for PermissionChecker (it's in BindsUtilModule)
}