package com.thelongevityapp.android

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import com.thelongevityapp.android.ui.RootNav
import com.thelongevityapp.android.ui.theme.PrimaryGreen
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val lang = newBase.getSharedPreferences("session", Context.MODE_PRIVATE)
            .getString("language", null) ?: Locale.getDefault().language
        val locale = Locale.forLanguageTag(if (lang.length >= 2) lang else "en")
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(ContextWrapper(newBase.createConfigurationContext(config)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
        }
        window.statusBarColor = Color.Black.toArgb()
        window.navigationBarColor = Color.Black.toArgb()
        setContent { AppContent() }
    }
}

@Composable
private fun AppContent() {
    TheLongevityAppTheme {
        RootNav()
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun TheLongevityAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = PrimaryGreen,
        surface = Color(0xFF041008),
        background = Color.Black
    )
    MaterialTheme(colorScheme = colorScheme, content = content)
}
