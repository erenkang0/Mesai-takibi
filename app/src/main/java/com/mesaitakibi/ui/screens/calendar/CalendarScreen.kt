package com.mesaitakibi.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mesaitakibi.ui.components.BackTopBar
import com.mesaitakibi.ui.components.SectionCard
import com.mesaitakibi.ui.haptics.HapticEvent
import com.mesaitakibi.ui.haptics.LocalAppHaptics
import com.mesaitakibi.ui.theme.Spacing
import com.mesaitakibi.ui.util.Format
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onBack: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val month by viewModel.month.collectAsStateWithLifecycle()
    val dayMinutes by viewModel.dayMinutes.collectAsStateWithLifecycle()
    val haptics = LocalAppHaptics.current
    var selected by remember { mutableStateOf<LocalDate?>(null) }

    val maxMinutes = (dayMinutes.values.maxOrNull() ?: 1L).coerceAtLeast(1L)
    val leading = month.atDay(1).dayOfWeek.value - 1
    val cells = buildList<LocalDate?> {
        repeat(leading) { add(null) }
        for (d in 1..month.lengthOfMonth()) add(month.atDay(d))
        while (size % 7 != 0) add(null)
    }
    val weeks = cells.chunked(7)
    val weekdays = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")

    Scaffold(topBar = { BackTopBar("Takvim", onBack) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Spacing.screenH, vertical = Spacing.screenTop),
            verticalArrangement = Arrangement.spacedBy(Spacing.section)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { haptics.perform(HapticEvent.TICK); viewModel.previousMonth() }) {
                    Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Önceki ay")
                }
                Text("${Format.monthName(month.monthValue)} ${month.year}",
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = { haptics.perform(HapticEvent.TICK); viewModel.nextMonth() }) {
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Sonraki ay")
                }
            }

            SectionCard {
                Row(Modifier.fillMaxWidth()) {
                    weekdays.forEach {
                        Text(it, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(Spacing.s))
                weeks.forEach { week ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        week.forEach { date ->
                            DayCell(
                                date = date,
                                minutes = date?.let { dayMinutes[it] } ?: 0L,
                                maxMinutes = maxMinutes,
                                selected = date != null && date == selected,
                                modifier = Modifier.weight(1f)
                            ) {
                                haptics.perform(HapticEvent.TICK)
                                selected = date
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }

            selected?.let { d ->
                val mins = dayMinutes[d] ?: 0L
                SectionCard {
                    Text(Format.dayFormatter.format(d), style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        if (mins > 0) "Çalışma: ${Format.hoursShort(mins)}" else "Bu gün kayıt yok.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate?,
    minutes: Long,
    maxMinutes: Long,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    if (date == null) {
        Box(modifier.aspectRatio(1f))
        return
    }
    val intensity = minutes.toFloat() / maxMinutes.toFloat()
    val bg = if (minutes > 0)
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f + 0.75f * intensity)
    else MaterialTheme.colorScheme.surface
    val border = if (selected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline
    Box(
        modifier
            .aspectRatio(1f)
            .background(bg, RoundedCornerShape(10.dp))
            .androidxBorder(selected, border)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (minutes > 0f && intensity > 0.5f)
                MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun Modifier.androidxBorder(selected: Boolean, color: androidx.compose.ui.graphics.Color): Modifier =
    if (selected) this.then(
        androidx.compose.foundation.border(1.5.dp, color, RoundedCornerShape(10.dp))
    ) else this
