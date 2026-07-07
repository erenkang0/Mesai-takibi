package com.mesaitakibi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mesaitakibi.data.prefs.AppPreferences
import com.mesaitakibi.data.prefs.PreferencesRepository
import com.mesaitakibi.ui.MesaiApp
import com.mesaitakibi.ui.theme.MesaiTakibiTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefs by preferencesRepository.preferences
                .collectAsStateWithLifecycle(initialValue = AppPreferences())
            MesaiTakibiTheme(
                themeMode = prefs.themeMode,
                dynamicColor = prefs.dynamicColor
            ) {
                MesaiApp(
                    hapticsEnabled = prefs.hapticsEnabled,
                    needsOnboarding = !prefs.onboarded
                )
            }
        }
    }
}
