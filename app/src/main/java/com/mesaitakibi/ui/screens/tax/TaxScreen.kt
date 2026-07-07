package com.mesaitakibi.ui.screens.tax

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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mesaitakibi.domain.payroll.TaxParameters
import com.mesaitakibi.ui.components.BackTopBar
import com.mesaitakibi.ui.components.SectionCard
import com.mesaitakibi.ui.haptics.HapticEvent
import com.mesaitakibi.ui.haptics.LocalAppHaptics
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxScreen(
    onBack: () -> Unit,
    viewModel: TaxViewModel = hiltViewModel()
) {
    val params by viewModel.params.collectAsStateWithLifecycle()
    val haptics = LocalAppHaptics.current

    Scaffold(topBar = { BackTopBar("Vergi ve SGK", onBack) }) { padding ->
        val p = params
        if (p == null) {
            Spacer(Modifier.height(0.dp))
            return@Scaffold
        }
        var minWage by remember(p) { mutableStateOf(p.minimumWageGross.toPlainString()) }
        var ceiling by remember(p) { mutableStateOf(p.sgkCeiling.toPlainString()) }
        var sgkRate by remember(p) { mutableStateOf(p.sgkEmployeeRate.toPlainString()) }
        var unempRate by remember(p) { mutableStateOf(p.unemploymentEmployeeRate.toPlainString()) }
        var stampRate by remember(p) { mutableStateOf(p.stampTaxRate.toPlainString()) }
        var exemption by remember(p) { mutableStateOf(p.applyMinimumWageExemption) }

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "${p.year} yılı parametreleri. Bu değerler her yıl değişir; resmî kaynaklardan " +
                    "(SGK, GİB) doğrulayın.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SectionCard(title = "Temel Parametreler") {
                NumField("Brüt asgari ücret (₺)", minWage) { minWage = it }
                Spacer(Modifier.height(8.dp))
                NumField("SGK tavanı (₺)", ceiling) { ceiling = it }
                Spacer(Modifier.height(8.dp))
                NumField("SGK işçi oranı (ör. 0.14)", sgkRate) { sgkRate = it }
                Spacer(Modifier.height(8.dp))
                NumField("İşsizlik işçi oranı (ör. 0.01)", unempRate) { unempRate = it }
                Spacer(Modifier.height(8.dp))
                NumField("Damga vergisi oranı (ör. 0.00759)", stampRate) { stampRate = it }
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Asgari ücret vergi istisnası", Modifier.weight(1f))
                    Switch(checked = exemption, onCheckedChange = {
                        haptics.perform(if (it) HapticEvent.TOGGLE_ON else HapticEvent.TOGGLE_OFF)
                        exemption = it
                    })
                }
            }

            SectionCard(title = "Gelir Vergisi Dilimleri") {
                p.incomeTaxBrackets.forEach { b ->
                    val upper = b.upTo?.let { "%,.0f ₺'ye kadar".format(it) } ?: "üzeri"
                    Text(
                        "• $upper: %${(b.rate * BigDecimal(100)).toPlainString()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    "Dilimleri düzenlemek için yedeği içe/dışa aktararak JSON üzerinde değişiklik yapabilirsiniz.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Button(
                onClick = {
                    haptics.perform(HapticEvent.SUCCESS)
                    viewModel.save(
                        p.copy(
                            minimumWageGross = minWage.toBd(p.minimumWageGross),
                            sgkCeiling = ceiling.toBd(p.sgkCeiling),
                            sgkEmployeeRate = sgkRate.toBd(p.sgkEmployeeRate),
                            unemploymentEmployeeRate = unempRate.toBd(p.unemploymentEmployeeRate),
                            stampTaxRate = stampRate.toBd(p.stampTaxRate),
                            applyMinimumWageExemption = exemption
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Kaydet") }

            OutlinedButton(
                onClick = { haptics.perform(HapticEvent.CLICK); viewModel.resetToDefaults() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("2026 varsayılanlarına dön") }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun NumField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.filter { c -> c.isDigit() || c == '.' }) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
}

private fun String.toBd(fallback: BigDecimal): BigDecimal =
    runCatching { BigDecimal(this) }.getOrDefault(fallback)
