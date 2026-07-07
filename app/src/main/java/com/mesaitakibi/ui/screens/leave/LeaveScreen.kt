package com.mesaitakibi.ui.screens.leave

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.mesaitakibi.data.local.entity.LeaveEntity
import com.mesaitakibi.ui.components.BackTopBar
import com.mesaitakibi.ui.components.DateField
import com.mesaitakibi.ui.components.LabeledRow
import com.mesaitakibi.ui.components.SectionCard
import com.mesaitakibi.ui.haptics.HapticEvent
import com.mesaitakibi.ui.haptics.LocalAppHaptics
import com.mesaitakibi.ui.util.Format
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveScreen(
    onBack: () -> Unit,
    viewModel: LeaveViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptics = LocalAppHaptics.current
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(topBar = { BackTopBar("Yıllık İzin", onBack) }) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { InfoCard(state.serviceYears, state.age, onSave = viewModel::setInfo) }

                item {
                    SectionCard(title = "${LocalDate.now().year} Yılı") {
                        LabeledRow("Yıllık izin hakkı", "${state.entitlement} gün")
                        LabeledRow("Kullanılan", "${state.usedDays} gün",
                            valueColor = MaterialTheme.colorScheme.error)
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        LabeledRow("Kalan", "${state.remaining} gün", emphasize = true)
                    }
                }

                if (state.leaves.isNotEmpty()) {
                    item {
                        Text("İzin Kayıtları", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
                    }
                    items(state.leaves, key = { it.id }) { leave ->
                        LeaveRow(leave, onDelete = {
                            haptics.perform(HapticEvent.REJECT); viewModel.delete(leave)
                        })
                    }
                }

                item { Spacer(Modifier.height(72.dp)) }
            }

            ExtendedFloatingActionButton(
                onClick = { haptics.perform(HapticEvent.CLICK); showAdd = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("İzin Ekle") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }

    if (showAdd) {
        AddLeaveDialog(
            onDismiss = { showAdd = false },
            onAdd = { date, days, note ->
                viewModel.add(date, days, note)
                haptics.perform(HapticEvent.SUCCESS)
                showAdd = false
            }
        )
    }
}

@Composable
private fun InfoCard(serviceYears: Int, age: Int, onSave: (Int, Int) -> Unit) {
    val haptics = LocalAppHaptics.current
    var years by remember(serviceYears) { mutableStateOf(serviceYears.toString()) }
    var ageText by remember(age) { mutableStateOf(age.toString()) }
    SectionCard(title = "Bilgiler") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = years,
                onValueChange = { years = it.filter { c -> c.isDigit() } },
                label = { Text("Kıdem (yıl)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = ageText,
                onValueChange = { ageText = it.filter { c -> c.isDigit() } },
                label = { Text("Yaş") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                haptics.perform(HapticEvent.SUCCESS)
                onSave(years.toIntOrNull() ?: 1, ageText.toIntOrNull() ?: 30)
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Kaydet") }
    }
}

@Composable
private fun LeaveRow(leave: LeaveEntity, onDelete: () -> Unit) {
    SectionCard {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("${leave.days} gün", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    Format.dateFormatter.format(leave.startDate) + (leave.note?.let { " · $it" } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Sil") }
        }
    }
}

@Composable
private fun AddLeaveDialog(onDismiss: () -> Unit, onAdd: (LocalDate, Int, String?) -> Unit) {
    var date by remember { mutableStateOf(LocalDate.now()) }
    var days by remember { mutableStateOf("1") }
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("İzin Ekle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DateField("Başlangıç", date, Modifier.fillMaxWidth()) { date = it }
                OutlinedTextField(
                    value = days,
                    onValueChange = { days = it.filter { c -> c.isDigit() } },
                    label = { Text("Gün sayısı") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note, onValueChange = { note = it },
                    label = { Text("Not (isteğe bağlı)") }, modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(date, days.toIntOrNull() ?: 1, note) },
                enabled = (days.toIntOrNull() ?: 0) > 0
            ) { Text("Ekle") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("İptal") } }
    )
}
