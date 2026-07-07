package com.mesaitakibi.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mesaitakibi.domain.model.WageType
import com.mesaitakibi.ui.components.SectionCard
import com.mesaitakibi.ui.components.TimeField
import com.mesaitakibi.ui.haptics.HapticEvent
import com.mesaitakibi.ui.haptics.LocalAppHaptics
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalTime

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onDone: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val haptics = LocalAppHaptics.current
    val requestNotificationPermission =
        com.mesaitakibi.ui.notification.rememberNotificationPermissionRequester()

    var name by remember { mutableStateOf("") }
    var wageType by remember { mutableStateOf(WageType.MONTHLY) }
    var grossWage by remember { mutableStateOf("") }
    var weeklyHours by remember { mutableStateOf("45") }
    var workStart by remember { mutableStateOf(LocalTime.of(9, 0)) }
    var workEnd by remember { mutableStateOf(LocalTime.of(18, 0)) }
    var breakMinutes by remember { mutableStateOf("60") }
    val workingDays = remember {
        mutableStateListOf(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        )
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(Modifier.fillMaxSize()) {
        AuroraBackground(Modifier.fillMaxSize())

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            AnimatedVisibility(visible = visible, enter = fadeIn(tween(700)) + scaleIn(tween(700), initialScale = 0.85f)) {
                OnboardingHero(
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            EntranceItem(visible, 0) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Hoş geldin! 👋",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Uygulamayı sana göre ayarlayalım. Bu bilgileri sonra Ayarlar'dan değiştirebilirsin.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )
                }
            }

            EntranceItem(visible, 1) {
                SectionCard(title = "Kişisel") {
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Adın") }, modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            EntranceItem(visible, 2) {
                SectionCard(title = "Ücret") {
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
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = grossWage,
                        onValueChange = { grossWage = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text(if (wageType == WageType.MONTHLY) "Aylık brüt ücret (₺)" else "Saatlik brüt ücret (₺)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = weeklyHours,
                        onValueChange = { weeklyHours = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Haftalık çalışma saati (yasal üst sınır 45)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            EntranceItem(visible, 3) {
                SectionCard(title = "Normal çalışma saatleri") {
                    Text(
                        "Bu saatlerden sonrası mesai olarak algılanır.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TimeField("Başlangıç", workStart, Modifier.weight(1f)) { workStart = it }
                        TimeField("Bitiş", workEnd, Modifier.weight(1f)) { workEnd = it }
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = breakMinutes,
                        onValueChange = { breakMinutes = it.filter { c -> c.isDigit() } },
                        label = { Text("Günlük mola (dakika)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            EntranceItem(visible, 4) {
                SectionCard(title = "Çalışma günleri") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DayOfWeek.entries.forEach { day ->
                            val selected = day in workingDays
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    haptics.perform(HapticEvent.TICK)
                                    if (selected) workingDays.remove(day) else workingDays.add(day)
                                },
                                label = { Text(dayShort(day)) }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            EntranceItem(visible, 5) {
                Button(
                    onClick = {
                        haptics.perform(HapticEvent.SUCCESS)
                        requestNotificationPermission()
                        viewModel.complete(
                            name = name,
                            wageType = wageType,
                            grossWage = grossWage.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                            weeklyContractHours = weeklyHours.toDoubleOrNull() ?: 45.0,
                            workStart = workStart,
                            workEnd = workEnd,
                            breakMinutes = breakMinutes.toIntOrNull() ?: 0,
                            workingDays = workingDays.toSet(),
                            onDone = onDone
                        )
                    },
                    enabled = name.isNotBlank() && grossWage.toBigDecimalOrNull() != null && workingDays.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Başla")
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Filled.ArrowForward, contentDescription = null)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EntranceItem(visible: Boolean, index: Int, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500, delayMillis = index * 90)) +
            slideInVertically(tween(500, delayMillis = index * 90)) { it / 5 }
    ) {
        content()
    }
}

private fun String.toBigDecimalOrNull(): BigDecimal? = runCatching { BigDecimal(this) }.getOrNull()

private fun dayShort(day: DayOfWeek): String = when (day) {
    DayOfWeek.MONDAY -> "Pzt"
    DayOfWeek.TUESDAY -> "Sal"
    DayOfWeek.WEDNESDAY -> "Çar"
    DayOfWeek.THURSDAY -> "Per"
    DayOfWeek.FRIDAY -> "Cum"
    DayOfWeek.SATURDAY -> "Cmt"
    DayOfWeek.SUNDAY -> "Paz"
}
