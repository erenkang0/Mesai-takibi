package com.mesaitakibi.ui.screens.finance

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
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import com.mesaitakibi.data.local.entity.TransactionEntity
import com.mesaitakibi.domain.finance.TransactionType
import com.mesaitakibi.ui.components.LabeledRow
import com.mesaitakibi.ui.components.SectionCard
import com.mesaitakibi.ui.haptics.HapticEvent
import com.mesaitakibi.ui.haptics.LocalAppHaptics
import com.mesaitakibi.ui.util.Format
import java.math.BigDecimal
import java.time.LocalDate

@Composable
fun FinanceScreen(
    contentPadding: PaddingValues,
    viewModel: FinanceViewModel = hiltViewModel()
) {
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val period by viewModel.period.collectAsStateWithLifecycle()
    val haptics = LocalAppHaptics.current
    var showAdd by remember { mutableStateOf(false) }

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
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { haptics.perform(HapticEvent.TICK); viewModel.previousMonth() }) {
                        Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Önceki ay")
                    }
                    Text(
                        "${Format.monthName(period.monthValue)} ${period.year}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { haptics.perform(HapticEvent.TICK); viewModel.nextMonth() }) {
                        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Sonraki ay")
                    }
                }
            }

            item {
                SectionCard(title = "Aylık Özet") {
                    LabeledRow(
                        "Gelir", Format.money(summary.totalIncome),
                        valueColor = MaterialTheme.colorScheme.tertiary
                    )
                    LabeledRow(
                        "Gider", Format.money(summary.totalExpense),
                        valueColor = MaterialTheme.colorScheme.error
                    )
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    LabeledRow("Denge", Format.money(summary.balance), emphasize = true)
                }
            }

            if (summary.expenseByCategory.isNotEmpty()) {
                item {
                    SectionCard(title = "Gider Kategorileri") {
                        summary.expenseByCategory.entries
                            .sortedByDescending { it.value }
                            .forEach { (cat, amount) -> LabeledRow(cat, Format.money(amount)) }
                    }
                }
            }

            item {
                Text(
                    "İşlemler",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(transactions, key = { it.id }) { txn ->
                TransactionRow(txn, onDelete = {
                    haptics.perform(HapticEvent.REJECT)
                    viewModel.delete(txn)
                })
            }

            item { Spacer(Modifier.height(72.dp)) }
        }

        ExtendedFloatingActionButton(
            onClick = { haptics.perform(HapticEvent.CLICK); showAdd = true },
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("İşlem Ekle") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }

    if (showAdd) {
        AddTransactionDialog(
            onDismiss = { showAdd = false },
            onAdd = { amount, type, category, date, note ->
                viewModel.add(amount, type, category, date, note)
                haptics.perform(HapticEvent.SUCCESS)
                showAdd = false
            }
        )
    }
}

@Composable
private fun TransactionRow(txn: TransactionEntity, onDelete: () -> Unit) {
    val isIncome = txn.type == TransactionType.INCOME.name
    SectionCard {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(txn.category, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    Format.dateFormatter.format(txn.date) + (txn.note?.let { " · $it" } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                (if (isIncome) "+" else "-") + Format.money(BigDecimal(txn.amount)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isIncome) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Sil")
            }
        }
    }
}

@Composable
private fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onAdd: (BigDecimal, TransactionType, String, LocalDate, String?) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var category by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var note by remember { mutableStateOf("") }
    val haptics = LocalAppHaptics.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni İşlem") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == TransactionType.EXPENSE,
                        onClick = { haptics.perform(HapticEvent.TICK); type = TransactionType.EXPENSE },
                        label = { Text("Gider") }
                    )
                    FilterChip(
                        selected = type == TransactionType.INCOME,
                        onClick = { haptics.perform(HapticEvent.TICK); type = TransactionType.INCOME },
                        label = { Text("Gelir") }
                    )
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Tutar (₺)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Kategori (ör. Kira, Market)") },
                    modifier = Modifier.fillMaxWidth()
                )
                com.mesaitakibi.ui.components.DateField("Tarih", date, Modifier.fillMaxWidth()) { date = it }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Not (isteğe bağlı)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val a = runCatching { BigDecimal(amount) }.getOrNull()
                    if (a != null) onAdd(a, type, category, date, note)
                },
                enabled = amount.isNotBlank()
            ) { Text("Ekle") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("İptal") } }
    )
}
