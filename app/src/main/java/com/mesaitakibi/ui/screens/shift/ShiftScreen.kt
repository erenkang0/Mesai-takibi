package com.mesaitakibi.ui.screens.shift

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mesaitakibi.data.local.entity.ShiftEntity
import com.mesaitakibi.ui.components.BackTopBar
import com.mesaitakibi.ui.components.SectionCard
import com.mesaitakibi.ui.components.TimeField
import com.mesaitakibi.ui.haptics.HapticEvent
import com.mesaitakibi.ui.haptics.LocalAppHaptics
import java.time.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftScreen(
    onBack: () -> Unit,
    viewModel: ShiftViewModel = hiltViewModel()
) {
    val days by viewModel.days.collectAsStateWithLifecycle()

    Scaffold(topBar = { BackTopBar("Vardiya / Plan", onBack) }) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Normal çalışma saatlerini belirle. Bu saatlerin dışında yapılan çalışma " +
                        "plana göre mesai olarak işaretlenir.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(days, key = { it.dayOfWeek }) { shift ->
                DayCard(shift, onChange = viewModel::save)
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun DayCard(shift: ShiftEntity, onChange: (ShiftEntity) -> Unit) {
    val haptics = LocalAppHaptics.current
    SectionCard {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                dayName(shift.dayOfWeek),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Switch(
                checked = shift.active,
                onCheckedChange = {
                    haptics.perform(if (it) HapticEvent.TOGGLE_ON else HapticEvent.TOGGLE_OFF)
                    onChange(shift.copy(active = it))
                }
            )
        }
        AnimatedVisibility(visible = shift.active) {
            Column {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimeField("Başlangıç", shift.start, Modifier.weight(1f)) {
                        onChange(shift.copy(start = it))
                    }
                    TimeField("Bitiş", shift.end, Modifier.weight(1f)) {
                        onChange(shift.copy(end = it))
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = shift.breakMinutes.toString(),
                    onValueChange = { v ->
                        onChange(shift.copy(breakMinutes = v.filter { it.isDigit() }.toIntOrNull() ?: 0))
                    },
                    label = { Text("Mola (dakika)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun dayName(dow: Int): String = when (DayOfWeek.of(dow)) {
    DayOfWeek.MONDAY -> "Pazartesi"
    DayOfWeek.TUESDAY -> "Salı"
    DayOfWeek.WEDNESDAY -> "Çarşamba"
    DayOfWeek.THURSDAY -> "Perşembe"
    DayOfWeek.FRIDAY -> "Cuma"
    DayOfWeek.SATURDAY -> "Cumartesi"
    DayOfWeek.SUNDAY -> "Pazar"
}
