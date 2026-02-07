package com.thelongevityapp.android

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class TheLongevityApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        FirebaseAuth.getInstance().setLanguageCode(Locale.getDefault().language.ifEmpty { "en" })
    }
}
