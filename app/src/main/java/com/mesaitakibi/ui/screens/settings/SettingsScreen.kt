package com.mesaitakibi.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mesaitakibi.data.prefs.ThemeMode
import com.mesaitakibi.domain.model.EmployeeProfile
import com.mesaitakibi.domain.model.WageType
import com.mesaitakibi.ui.components.SectionCard
import com.mesaitakibi.ui.haptics.HapticEvent
import com.mesaitakibi.ui.haptics.LocalAppHaptics
import com.mesaitakibi.ui.motion.pressableClick
import com.mesaitakibi.ui.navigation.Routes
import java.math.BigDecimal

@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    onNavigate: (String) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val haptics = LocalAppHaptics.current

    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        profile?.let { ProfileSection(it, onSave = viewModel::saveProfile) }

        SectionCard(title = "Görünüm") {
            Text("Tema", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeChip("Sistem", prefs.themeMode == ThemeMode.SYSTEM) { viewModel.setThemeMode(ThemeMode.SYSTEM) }
                ThemeChip("Açık", prefs.themeMode == ThemeMode.LIGHT) { viewModel.setThemeMode(ThemeMode.LIGHT) }
                ThemeChip("Koyu", prefs.themeMode == ThemeMode.DARK) { viewModel.setThemeMode(ThemeMode.DARK) }
            }
            Spacer(Modifier.height(8.dp))
            SwitchRow("Dinamik renk (Material You)", prefs.dynamicColor) { viewModel.setDynamicColor(it) }
            SwitchRow("Titreşimli geri bildirim (haptic)", prefs.hapticsEnabled) {
                haptics.perform(if (it) HapticEvent.TOGGLE_ON else HapticEvent.TOGGLE_OFF)
                viewModel.setHaptics(it)
            }
        }

        SectionCard(title = "Yönetim") {
            NavRow("Vardiya / Plan", Icons.Filled.Schedule) { onNavigate(Routes.SHIFTS) }
            NavRow("Vergi ve SGK Parametreleri", Icons.Filled.Receipt) { onNavigate(Routes.TAX) }
            NavRow("Kıdem & İhbar Tazminatı", Icons.Filled.Calculate) { onNavigate(Routes.SEVERANCE) }
            NavRow("Yıllık İzin", Icons.Filled.BeachAccess) { onNavigate(Routes.LEAVE) }
            NavRow("Resmî Tatiller", Icons.Filled.CalendarMonth) { onNavigate(Routes.HOLIDAYS) }
            NavRow("Yedekleme ve Veri", Icons.Filled.Backup) { onNavigate(Routes.BACKUP) }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ProfileSection(profile: EmployeeProfile, onSave: (EmployeeProfile) -> Unit) {
    val haptics = LocalAppHaptics.current
    var name by remember(profile) { mutableStateOf(profile.name) }
    var wageType by remember(profile) { mutableStateOf(profile.wageType) }
    var grossWage by remember(profile) { mutableStateOf(profile.grossWage.toPlainString()) }
    var weeklyHours by remember(profile) { mutableStateOf(profile.weeklyContractHours.toString()) }

    SectionCard(title = "Profil") {
        OutlinedTextField(
            value = name, onValueChange = { name = it },
            label = { Text("Ad") }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = wageType == WageType.MONTHLY,
                onClick = { haptics.perform(HapticEvent.TICK); wageType = WageType.MONTHLY },
                label = { Text("Aylık") }
            )
            FilterChip(
                selected = wageType == WageType.HOURLY,
                onClick = { haptics.perform(HapticEvent.TICK); wageType = WageType.HOURLY },
                label = { Text("Saatlik") }
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = grossWage,
            onValueChange = { grossWage = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Brüt ücret (₺)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = weeklyHours,
            onValueChange = { weeklyHours = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Haftalık çalışma saati") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                haptics.perform(HapticEvent.SUCCESS)
                onSave(
                    profile.copy(
                        name = name.trim(),
                        wageType = wageType,
                        grossWage = runCatching { BigDecimal(grossWage) }.getOrDefault(profile.grossWage),
                        weeklyContractHours = weeklyHours.toDoubleOrNull() ?: profile.weeklyContractHours
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Kaydet") }
    }
}

@Composable
private fun ThemeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val haptics = LocalAppHaptics.current
    FilterChip(
        selected = selected,
        onClick = { haptics.perform(HapticEvent.TICK); onClick() },
        label = { Text(label) }
    )
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun NavRow(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val haptics = LocalAppHaptics.current
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = { Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null) },
        modifier = Modifier.pressableClick(haptics, HapticEvent.CLICK) { onClick() }
    )
}
