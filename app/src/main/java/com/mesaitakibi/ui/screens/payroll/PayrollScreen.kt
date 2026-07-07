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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    val haptics = LocalAppHaptics.current

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
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                LabeledRow("Net", Format.money(r?.net ?: BigDecimal.ZERO), emphasize = true)
            }
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
