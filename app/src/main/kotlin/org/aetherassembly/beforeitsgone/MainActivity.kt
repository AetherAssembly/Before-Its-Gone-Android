package org.aetherassembly.beforeitsgone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import org.aetherassembly.beforeitsgone.data.local.preferences.SettingsDataStore
import org.aetherassembly.beforeitsgone.ui.navigation.AppNavHost
import org.aetherassembly.beforeitsgone.ui.theme.BeforeItsGoneTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FossInit.showWarnings(this, BuildConfig.VERSION_CODE)
        enableEdgeToEdge()
        setContent {
            val androidSettings by settingsDataStore.androidSettings
                .collectAsStateWithLifecycle(
                    initialValue = org.aetherassembly.beforeitsgone.data.local.preferences.AndroidSettings()
                )
            val darkTheme = when (androidSettings.themeOverride) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }
            BeforeItsGoneTheme(
                darkTheme = darkTheme,
                dynamicColor = androidSettings.dynamicColor
            ) {
                AppNavHost()
            }
        }
    }
}
