package com.mesaitakibi.ui.screens.severance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.mesaitakibi.ui.components.BackTopBar
import com.mesaitakibi.ui.components.DateField
import com.mesaitakibi.ui.components.LabeledRow
import com.mesaitakibi.ui.components.SectionCard
import com.mesaitakibi.ui.haptics.HapticEvent
import com.mesaitakibi.ui.haptics.LocalAppHaptics
import com.mesaitakibi.ui.util.Format
import java.math.BigDecimal
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeveranceScreen(
    onBack: () -> Unit,
    viewModel: SeveranceViewModel = hiltViewModel()
) {
    val defaultGross by viewModel.defaultGross.collectAsStateWithLifecycle()
    val result by viewModel.result.collectAsStateWithLifecycle()
    val haptics = LocalAppHaptics.current

    var start by remember { mutableStateOf(LocalDate.now().minusYears(2)) }
    var end by remember { mutableStateOf(LocalDate.now()) }
    var gross by remember(defaultGross) { mutableStateOf(defaultGross.takeIf { it > BigDecimal.ZERO }?.toPlainString() ?: "") }
    var ceiling by remember { mutableStateOf(SeveranceViewModel.DEFAULT_CEILING_2026.toPlainString()) }

    Scaffold(topBar = { BackTopBar("Kıdem & İhbar Tazminatı", onBack) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionCard(title = "Bilgiler") {
                DateField("İşe giriş", start, Modifier.fillMaxWidth()) { start = it }
                Spacer(Modifier.height(8.dp))
                DateField("İşten ayrılış", end, Modifier.fillMaxWidth()) { end = it }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = gross,
                    onValueChange = { gross = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Aylık brüt ücret (giydirilmiş) ₺") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = ceiling,
                    onValueChange = { ceiling = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Kıdem tazminatı tavanı ₺") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = {
                    haptics.perform(HapticEvent.SUCCESS)
                    viewModel.compute(
                        start, end,
                        gross.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                        ceiling.toBigDecimalOrNull() ?: SeveranceViewModel.DEFAULT_CEILING_2026
                    )
                },
                enabled = gross.toBigDecimalOrNull() != null,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Hesapla") }

            result?.let { r ->
                SectionCard(title = "Sonuç") {
                    LabeledRow("Hizmet süresi", "${r.serviceYears} yıl (${r.serviceDays} gün)")
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    if (r.eligibleForSeverance) {
                        LabeledRow("Kıdem tazminatı (brüt)", Format.money(r.severanceGross))
                        LabeledRow("  Damga vergisi", "- " + Format.money(r.severanceStampTax),
                            valueColor = MaterialTheme.colorScheme.error)
                        LabeledRow("Kıdem tazminatı (net)", Format.money(r.severanceNet), emphasize = true)
                    } else {
                        Text(
                            "Kıdem tazminatı için en az 1 yıl çalışma gerekir.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    LabeledRow("İhbar süresi", "${r.noticeWeeks} hafta")
                    LabeledRow("İhbar tazminatı (brüt)", Format.money(r.noticePayGross), emphasize = true)
                    Text(
                        "Not: Kıdem tazminatı gelir vergisinden istisnadır (yalnızca damga). İhbar " +
                            "tazminatı gelir ve damga vergisine tabidir. Tavanı resmî kaynaktan doğrulayın.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

private fun String.toBigDecimalOrNull(): BigDecimal? = runCatching { BigDecimal(this) }.getOrNull()
