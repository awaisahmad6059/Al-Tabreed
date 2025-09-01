package com.aak.al_tabreed.Authentication

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import java.util.Locale

class MyApp : Application() {

    override fun attachBaseContext(base: Context) {
        val sharedPref: SharedPreferences = base.getSharedPreferences("Settings", Context.MODE_PRIVATE)

        // Set default English if first launch
        if (!sharedPref.contains("My_Lang")) {
            with(sharedPref.edit()) {
                putString("My_Lang", "en")
                apply()
            }
        }

        val language = sharedPref.getString("My_Lang", "en") ?: "en"
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = base.resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        super.attachBaseContext(base.createConfigurationContext(config))
    }
}
