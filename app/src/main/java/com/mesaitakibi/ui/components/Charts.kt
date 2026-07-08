package com.mesaitakibi.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mesaitakibi.ui.theme.Spacing

/** Bir sütun grafiği verisi. [highlight] değeri, sütunun üstünde vurgulanan (ör. mesai) kısım. */
data class BarDatum(val label: String, val value: Float, val highlight: Float = 0f)

/**
 * Tamamen Canvas ile çizilmiş özel sütun grafiği (harici kütüphane yok).
 * Değerler en yüksek sütuna göre ölçeklenir; [highlight] kısım [highlightColor] ile üstte gösterilir.
 */
@Composable
fun BarChart(
    data: List<BarDatum>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    highlightColor: Color = MaterialTheme.colorScheme.tertiary,
    height: androidx.compose.ui.unit.Dp = 150.dp
) {
    if (data.isEmpty()) return
    val max = (data.maxOf { it.value }).coerceAtLeast(1f)
    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

    Column(modifier.fillMaxWidth()) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            val n = data.size
            val gap = 10.dp.toPx()
            val barW = ((size.width - gap * (n + 1)) / n).coerceAtLeast(1f)
            val radius = CornerRadius(barW * 0.28f, barW * 0.28f)
            data.forEachIndexed { i, d ->
                val x = gap + i * (barW + gap)
                // arka plan izi
                drawRoundRect(
                    color = trackColor,
                    topLeft = Offset(x, 0f),
                    size = Size(barW, size.height),
                    cornerRadius = radius
                )
                val h = (d.value / max) * size.height
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, size.height - h),
                    size = Size(barW, h),
                    cornerRadius = radius
                )
                if (d.highlight > 0f) {
                    val hh = (d.highlight / max) * size.height
                    drawRoundRect(
                        color = highlightColor,
                        topLeft = Offset(x, size.height - hh),
                        size = Size(barW, hh),
                        cornerRadius = radius
                    )
                }
            }
        }
        Spacer(Modifier.height(Spacing.s))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            data.forEach { d ->
                Text(
                    d.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** Kategori dağılımı için yatay oranlı çubuklar. */
@Composable
fun CategoryBars(
    data: List<Pair<String, Float>>,
    valueFormatter: (Float) -> String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val max = (data.maxOfOrNull { it.second } ?: 1f).coerceAtLeast(1f)
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        data.forEach { (label, value) ->
            Column(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                    Text(valueFormatter(value), style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(Spacing.xs))
                Canvas(
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                ) {
                    drawRoundRect(
                        color = color.copy(alpha = 0.18f),
                        size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(size.height / 2, size.height / 2)
                    )
                    drawRoundRect(
                        color = color,
                        size = Size(size.width * (value / max), size.height),
                        cornerRadius = CornerRadius(size.height / 2, size.height / 2)
                    )
                }
            }
        }
    }
}
