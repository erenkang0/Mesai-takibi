package com.mesaitakibi.ui.screens.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mesaitakibi.ui.components.BarChart
import com.mesaitakibi.ui.components.CategoryBars
import com.mesaitakibi.ui.components.BackTopBar
import com.mesaitakibi.ui.components.SectionCard
import com.mesaitakibi.ui.theme.Spacing
import com.mesaitakibi.ui.util.Format
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onBack: () -> Unit,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { BackTopBar("Analiz & Grafikler", onBack) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenH, vertical = Spacing.screenTop),
            verticalArrangement = Arrangement.spacedBy(Spacing.section)
        ) {
            SectionCard(title = "Son 8 Hafta Çalışma") {
                BarChart(data = state.weekly)
                Spacer(Modifier.height(Spacing.m))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.l)) {
                    Legend(MaterialTheme.colorScheme.primary, "Toplam çalışma")
                    Legend(MaterialTheme.colorScheme.tertiary, "Mesai")
                }
            }

            if (state.netByMonth.isNotEmpty()) {
                SectionCard(title = "Aylık Net Maaş") {
                    BarChart(
                        data = state.netByMonth,
                        barColor = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            SectionCard(title = "Bu Ay Giderler") {
                if (state.expenseByCategory.isEmpty()) {
                    Text(
                        "Bu ay için gider kaydı yok.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    CategoryBars(
                        data = state.expenseByCategory.map { it.first to it.second.toFloat() },
                        valueFormatter = { Format.money(it.toBigDecimal()) },
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(Spacing.l))
        }
    }
}

@Composable
private fun Legend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).background(color, RoundedCornerShape(3.dp)))
        Spacer(Modifier.size(6.dp))
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
