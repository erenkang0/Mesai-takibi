package com.mesaitakibi.ui.screens.timetracking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mesaitakibi.data.local.entity.TimeEntryEntity
import com.mesaitakibi.ui.components.DateField
import com.mesaitakibi.ui.components.LabeledRow
import com.mesaitakibi.ui.components.SectionCard
import com.mesaitakibi.ui.components.TimeField
import com.mesaitakibi.ui.haptics.HapticEvent
import com.mesaitakibi.ui.haptics.LocalAppHaptics
import com.mesaitakibi.ui.util.Format
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Composable
fun TimeTrackingScreen(
    contentPadding: PaddingValues,
    viewModel: TimeTrackingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptics = LocalAppHaptics.current
    var editing by remember { mutableStateOf<TimeEntryEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                val w = state.weekly
                SectionCard(title = "Bu Hafta") {
                    LabeledRow("Toplam çalışma", Format.hoursShort(w?.totalWorkedMinutes ?: 0))
                    LabeledRow("Normal", Format.hoursShort(w?.normalMinutes ?: 0))
                    LabeledRow(
                        "Fazla süre (×1,25)",
                        Format.hoursShort(w?.fazlaSureMinutes ?: 0),
                        valueColor = MaterialTheme.colorScheme.tertiary
                    )
                    LabeledRow(
                        "Fazla çalışma / mesai (×1,50)",
                        Format.hoursShort(w?.fazlaCalismaMinutes ?: 0),
                        valueColor = MaterialTheme.colorScheme.tertiary
                    )
                    LabeledRow("Gece çalışması", Format.hoursShort(w?.nightMinutes ?: 0))
                }
            }

            if (state.entries.isEmpty()) {
                item {
                    Text(
                        "Henüz kayıt yok. Ana ekrandan işe başla/bitir veya sağ alttan manuel kayıt ekle.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            items(state.entries, key = { it.id }) { entry ->
                EntryRow(entry, onClick = {
                    haptics.perform(HapticEvent.TICK)
                    editing = entry
                    showEditor = true
                })
            }

            item { Spacer(Modifier.height(72.dp)) }
        }

        ExtendedFloatingActionButton(
            onClick = {
                haptics.perform(HapticEvent.CLICK)
                editing = null
                showEditor = true
            },
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Kayıt Ekle") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }

    if (showEditor) {
        TimeEntryEditor(
            initial = editing,
            onDismiss = { showEditor = false },
            onSave = {
                viewModel.save(it)
                haptics.perform(HapticEvent.SUCCESS)
                showEditor = false
            },
            onDelete = editing?.let { e ->
                {
                    viewModel.delete(e)
                    haptics.perform(HapticEvent.REJECT)
                    showEditor = false
                }
            }
        )
    }
}

@Composable
private fun EntryRow(entry: TimeEntryEntity, onClick: () -> Unit) {
    val worked = entry.clockOut?.let {
        (Duration.between(entry.clockIn, it).toMinutes() - entry.breakMinutes).coerceAtLeast(0)
    } ?: 0
    SectionCard {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    Format.dayFormatter.format(entry.date),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${Format.timeFormatter.format(entry.clockIn)} – " +
                        (entry.clockOut?.let { Format.timeFormatter.format(it) } ?: "…") +
                        if (entry.breakMinutes > 0) "  (mola ${entry.breakMinutes} dk)" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AssistChip(onClick = onClick, label = { Text(Format.hoursShort(worked)) })
        }
    }
}

@Composable
private fun TimeEntryEditor(
    initial: TimeEntryEntity?,
    onDismiss: () -> Unit,
    onSave: (TimeEntryEntity) -> Unit,
    onDelete: (() -> Unit)?
) {
    var date by remember { mutableStateOf(initial?.date ?: LocalDate.now()) }
    var start by remember { mutableStateOf(initial?.clockIn?.toLocalTime() ?: LocalTime.of(9, 0)) }
    var end by remember { mutableStateOf(initial?.clockOut?.toLocalTime() ?: LocalTime.of(18, 0)) }
    var breakText by remember { mutableStateOf((initial?.breakMinutes ?: 60).toString()) }
    var note by remember { mutableStateOf(initial?.note ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Yeni Kayıt" else "Kaydı Düzenle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DateField("Tarih", date, Modifier.fillMaxWidth()) { date = it }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimeField("Giriş", start, Modifier.weight(1f)) { start = it }
                    TimeField("Çıkış", end, Modifier.weight(1f)) { end = it }
                }
                OutlinedTextField(
                    value = breakText,
                    onValueChange = { breakText = it.filter { c -> c.isDigit() } },
                    label = { Text("Mola (dakika)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Not (isteğe bağlı)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val clockIn = LocalDateTime.of(date, start)
                val clockOutDate = if (end.isAfter(start)) date else date.plusDays(1)
                val clockOut = LocalDateTime.of(clockOutDate, end)
                onSave(
                    (initial ?: TimeEntryEntity(date = date, clockIn = clockIn)).copy(
                        date = date,
                        clockIn = clockIn,
                        clockOut = clockOut,
                        breakMinutes = breakText.toIntOrNull() ?: 0,
                        note = note.ifBlank { null }
                    )
                )
            }) { Text("Kaydet") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Sil")
                    }
                }
                TextButton(onClick = onDismiss) { Text("İptal") }
            }
        }
    )
}
