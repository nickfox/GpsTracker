package com.websmithing.gpstracker2.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import com.websmithing.gpstracker2.data.repository.SettingsRepository
import java.util.Locale

object LocaleHelper {

    /**
     * Attaches the saved locale to the base context of an Activity or Service.
     * This must be called in `attachBaseContext`.
     */
    fun onAttach(context: Context, settingsRepository: SettingsRepository): Context {
        val currentLanguage = settingsRepository.getCurrentLanguage()
        return setAppLocale(context, currentLanguage)
    }

    fun setAppLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources: Resources = context.resources
        val config: Configuration = resources.configuration
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}