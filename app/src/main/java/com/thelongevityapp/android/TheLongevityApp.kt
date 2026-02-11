package com.thelongevityapp.android

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class TheLongevityApp : Application() {

    override fun attachBaseContext(base: Context) {
        val lang = base.getSharedPreferences("session", Context.MODE_PRIVATE)
            .getString("language", null) ?: Locale.getDefault().language
        val locale = Locale.forLanguageTag(if (lang.length >= 2) lang else "en")
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(ContextWrapper(base.createConfigurationContext(config)))
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        val lang = getSharedPreferences("session", Context.MODE_PRIVATE)
            .getString("language", null) ?: Locale.getDefault().language
        FirebaseAuth.getInstance().setLanguageCode(if (lang.length >= 2) lang else "en")
    }
}
