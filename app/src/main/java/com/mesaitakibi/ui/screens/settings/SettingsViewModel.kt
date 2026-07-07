package com.mesaitakibi.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mesaitakibi.data.prefs.AppPreferences
import com.mesaitakibi.data.prefs.PreferencesRepository
import com.mesaitakibi.data.prefs.ThemeMode
import com.mesaitakibi.data.repository.SettingsRepository
import com.mesaitakibi.domain.model.EmployeeProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val prefs: PreferencesRepository
) : ViewModel() {

    val profile: StateFlow<EmployeeProfile?> =
        settings.observeProfile().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val preferences: StateFlow<AppPreferences> =
        prefs.preferences.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppPreferences())

    fun saveProfile(profile: EmployeeProfile) = viewModelScope.launch { settings.save(profile) }

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { prefs.setThemeMode(mode) }
    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch { prefs.setDynamicColor(enabled) }
    fun setHaptics(enabled: Boolean) = viewModelScope.launch { prefs.setHapticsEnabled(enabled) }
}
