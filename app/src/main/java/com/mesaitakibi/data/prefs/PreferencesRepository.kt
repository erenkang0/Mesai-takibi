package com.mesaitakibi.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Uygulama tema modu. Varsayılan: KARANLIK. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppPreferences(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val dynamicColor: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val onboarded: Boolean = false,
    val leaveServiceYears: Int = 1,
    val leaveAge: Int = 30
)

private val Context.dataStore by preferencesDataStore(name = "mesai_prefs")

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val DYNAMIC = booleanPreferencesKey("dynamic_color")
        val HAPTICS = booleanPreferencesKey("haptics_enabled")
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val LEAVE_SERVICE_YEARS = intPreferencesKey("leave_service_years")
        val LEAVE_AGE = intPreferencesKey("leave_age")
    }

    val preferences: Flow<AppPreferences> = context.dataStore.data.map { prefs ->
        AppPreferences(
            themeMode = prefs[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.DARK,
            dynamicColor = prefs[Keys.DYNAMIC] ?: false,
            hapticsEnabled = prefs[Keys.HAPTICS] ?: true,
            onboarded = prefs[Keys.ONBOARDED] ?: false,
            leaveServiceYears = prefs[Keys.LEAVE_SERVICE_YEARS] ?: 1,
            leaveAge = prefs[Keys.LEAVE_AGE] ?: 30
        )
    }

    suspend fun setOnboarded(value: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDED] = value }
    }

    suspend fun setLeaveInfo(serviceYears: Int, age: Int) {
        context.dataStore.edit {
            it[Keys.LEAVE_SERVICE_YEARS] = serviceYears
            it[Keys.LEAVE_AGE] = age
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME] = mode.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DYNAMIC] = enabled }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HAPTICS] = enabled }
    }
}
