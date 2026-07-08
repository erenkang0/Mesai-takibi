package com.mesaitakibi.ui.screens.payroll

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.mesaitakibi.data.local.entity.PayrollAdjustmentEntity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mesaitakibi.data.local.entity.PayrollPeriodEntity
import com.mesaitakibi.ui.components.LabeledRow
import com.mesaitakibi.ui.components.SectionCard
import com.mesaitakibi.ui.haptics.HapticEvent
import com.mesaitakibi.ui.haptics.LocalAppHaptics
import com.mesaitakibi.ui.util.Format
import java.math.BigDecimal

@Composable
fun PayrollScreen(
    contentPadding: PaddingValues,
    viewModel: PayrollViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val adjustments by viewModel.adjustments.collectAsStateWithLifecycle()
    val haptics = LocalAppHaptics.current
    var showAddAdjustment by remember { mutableStateOf(false) }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(contentPadding),
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
                AnimatedContent(
                    targetState = "${Format.monthName(state.month)} ${state.year}",
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "month"
                ) { label ->
                    Text(label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = { haptics.perform(HapticEvent.TICK); viewModel.nextMonth() }) {
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Sonraki ay")
                }
            }
        }

        item {
            val net = state.result?.net ?: BigDecimal.ZERO
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text("Net Maaş", style = MaterialTheme.typography.titleMedium)
                    AnimatedContent(
                        targetState = Format.money(net),
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "net"
                    ) { value ->
                        Text(
                            value,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            val r = state.result
            SectionCard(title = "Bordro Dökümü") {
                LabeledRow("Brüt", Format.money(r?.gross ?: BigDecimal.ZERO), emphasize = true)
                LabeledRow("  Temel ücret", Format.money(r?.basePay ?: BigDecimal.ZERO))
                LabeledRow("  Fazla süre (×1,25)", Format.money(r?.fazlaSurePay ?: BigDecimal.ZERO))
                LabeledRow("  Fazla çalışma (×1,50)", Format.money(r?.fazlaCalismaPay ?: BigDecimal.ZERO))
                LabeledRow("  Resmî tatil çalışması", Format.money(r?.holidayPay ?: BigDecimal.ZERO))
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                LabeledRow(
                    "SGK işçi payı (%14)", "- " + Format.money(r?.sgkEmployee ?: BigDecimal.ZERO),
                    valueColor = MaterialTheme.colorScheme.error
                )
                LabeledRow(
                    "İşsizlik (%1)", "- " + Format.money(r?.unemployment ?: BigDecimal.ZERO),
                    valueColor = MaterialTheme.colorScheme.error
                )
                LabeledRow(
                    "Gelir vergisi", "- " + Format.money(r?.incomeTax ?: BigDecimal.ZERO),
                    valueColor = MaterialTheme.colorScheme.error
                )
                LabeledRow(
                    "Damga vergisi", "- " + Format.money(r?.stampTax ?: BigDecimal.ZERO),
                    valueColor = MaterialTheme.colorScheme.error
                )
                if ((r?.netAdditions ?: BigDecimal.ZERO) > BigDecimal.ZERO) {
                    LabeledRow("Ek ödeme (net)", "+ " + Format.money(r!!.netAdditions),
                        valueColor = MaterialTheme.colorScheme.tertiary)
                }
                if ((r?.netDeductions ?: BigDecimal.ZERO) > BigDecimal.ZERO) {
                    LabeledRow("Kesinti (avans vb.)", "- " + Format.money(r!!.netDeductions),
                        valueColor = MaterialTheme.colorScheme.error)
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                LabeledRow("Net", Format.money(r?.net ?: BigDecimal.ZERO), emphasize = true)
            }
        }

        item {
            AdjustmentsSection(
                adjustments = adjustments,
                onAdd = { haptics.perform(HapticEvent.CLICK); showAddAdjustment = true },
                onDelete = { haptics.perform(HapticEvent.REJECT); viewModel.deleteAdjustment(it) }
            )
        }

        item {
            Text(
                "Not: Vergi/SGK parametreleri Ayarlar > Vergi ve SGK'dan düzenlenebilir. " +
                    "Değerler yıllık değişir; resmî kaynaktan (SGK, GİB) doğrulayın.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (history.isNotEmpty()) {
            item {
                Text(
                    "Geçmiş Dönemler",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(history, key = { it.id }) { period -> HistoryRow(period) }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }

    if (showAddAdjustment) {
        AddAdjustmentDialog(
            onDismiss = { showAddAdjustment = false },
            onAdd = { label, amount, kind ->
                viewModel.addAdjustment(label, amount, kind)
                haptics.perform(HapticEvent.SUCCESS)
                showAddAdjustment = false
            }
        )
    }
}

@Composable
private fun AdjustmentsSection(
    adjustments: List<PayrollAdjustmentEntity>,
    onAdd: () -> Unit,
    onDelete: (PayrollAdjustmentEntity) -> Unit
) {
    SectionCard(title = "Ek Ödeme / Kesinti") {
        if (adjustments.isEmpty()) {
            Text(
                "Prim, yol/yemek yardımı veya avans kesintisi ekleyerek bordroyu kişiselleştir.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            adjustments.forEach { adj ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(adj.label, style = MaterialTheme.typography.bodyLarge)
                        Text(kindLabel(adj.kind), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    val sign = if (adj.kind == PayrollAdjustmentEntity.DEDUCTION_NET) "- " else "+ "
                    val color = if (adj.kind == PayrollAdjustmentEntity.DEDUCTION_NET)
                        MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                    Text(sign + Format.money(BigDecimal(adj.amount)),
                        style = MaterialTheme.typography.titleSmall, color = color, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { onDelete(adj) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Sil")
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Kalem Ekle")
        }
    }
}

@Composable
private fun AddAdjustmentDialog(
    onDismiss: () -> Unit,
    onAdd: (String, BigDecimal, String) -> Unit
) {
    val haptics = LocalAppHaptics.current
    var label by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(PayrollAdjustmentEntity.EARNING_TAXABLE) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kalem Ekle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = label, onValueChange = { label = it },
                    label = { Text("Açıklama (ör. Prim, Avans)") }, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Tutar (₺)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    KindChip("Ek ödeme (vergiye tabi)", kind == PayrollAdjustmentEntity.EARNING_TAXABLE) {
                        haptics.perform(HapticEvent.TICK); kind = PayrollAdjustmentEntity.EARNING_TAXABLE
                    }
                    KindChip("Ek ödeme (istisna/net)", kind == PayrollAdjustmentEntity.EARNING_NET) {
                        haptics.perform(HapticEvent.TICK); kind = PayrollAdjustmentEntity.EARNING_NET
                    }
                    KindChip("Kesinti (avans vb.)", kind == PayrollAdjustmentEntity.DEDUCTION_NET) {
                        haptics.perform(HapticEvent.TICK); kind = PayrollAdjustmentEntity.DEDUCTION_NET
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val a = runCatching { BigDecimal(amount) }.getOrNull()
                    if (a != null) onAdd(label, a, kind)
                },
                enabled = amount.isNotBlank()
            ) { Text("Ekle") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("İptal") } }
    )
}

@Composable
private fun KindChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}

private fun kindLabel(kind: String): String = when (kind) {
    PayrollAdjustmentEntity.EARNING_TAXABLE -> "Ek ödeme · vergiye tabi"
    PayrollAdjustmentEntity.EARNING_NET -> "Ek ödeme · istisna"
    else -> "Kesinti"
}

@Composable
private fun HistoryRow(period: PayrollPeriodEntity) {
    SectionCard {
        LabeledRow(
            "${Format.monthName(period.month)} ${period.year}",
            Format.money(BigDecimal(period.net)),
            emphasize = true
        )
    }
}
