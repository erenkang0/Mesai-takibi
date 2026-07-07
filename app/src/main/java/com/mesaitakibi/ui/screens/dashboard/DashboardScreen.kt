package com.mesaitakibi.ui.screens.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mesaitakibi.domain.overtime.WeeklyWorkResult
import com.mesaitakibi.ui.components.StatTile
import com.mesaitakibi.ui.haptics.HapticEvent
import com.mesaitakibi.ui.haptics.LocalAppHaptics
import com.mesaitakibi.ui.motion.pressableClick
import com.mesaitakibi.ui.util.Format
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime

@Composable
fun DashboardScreen(
    contentPadding: PaddingValues,
    onOpenPayroll: () -> Unit,
    onOpenTimeTracking: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = greeting() + (state.userName.takeIf { it.isNotBlank() }?.let { ", $it" } ?: ""),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = Format.dayFormatter.format(LocalDateTime.now()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))

        ClockCard(
            clockedIn = state.clockedIn,
            since = state.clockInSince,
            onClockIn = viewModel::clockIn,
            onClockOut = viewModel::clockOut
        )

        Spacer(Modifier.height(20.dp))

        val weekly = state.weekly
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                label = "Bu hafta",
                value = Format.hoursShort(weekly?.totalWorkedMinutes ?: 0),
                icon = Icons.Filled.Timer,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                label = "Mesai",
                value = Format.hoursShort(
                    (weekly?.fazlaCalismaMinutes ?: 0) + (weekly?.fazlaSureMinutes ?: 0)
                ),
                icon = Icons.Filled.TrendingUp,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        StatTile(
            label = "Bu ay tahmini net maaş",
            value = state.estimatedNet?.let { Format.money(it) } ?: "—",
            icon = Icons.Filled.TrendingUp,
            modifier = Modifier
                .fillMaxWidth()
                .pressableClick(LocalAppHaptics.current, HapticEvent.CLICK) { onOpenPayroll() }
        )

        if (weekly != null && weekly.hasOvertime) {
            Spacer(Modifier.height(12.dp))
            OvertimeBanner(weekly)
        }
    }
}

@Composable
private fun ClockCard(
    clockedIn: Boolean,
    since: LocalDateTime?,
    onClockIn: () -> Unit,
    onClockOut: () -> Unit
) {
    val haptics = LocalAppHaptics.current
    val containerColor by animateColorAsState(
        if (clockedIn) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        label = "clockContainer"
    )

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = containerColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (clockedIn) "Çalışıyorsun" else "Şu an çalışmıyorsun",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))

            // Canlı geçen süre sayacı (saniyede bir güncellenir)
            val elapsed by produceState(initialValue = "00:00:00", clockedIn, since) {
                while (clockedIn && since != null) {
                    val d = Duration.between(since, LocalDateTime.now())
                    val h = d.toHours()
                    val m = d.toMinutes() % 60
                    val s = d.seconds % 60
                    value = String.format("%02d:%02d:%02d", h, m, s)
                    delay(1000)
                }
                if (!clockedIn) value = "00:00:00"
            }

            AnimatedContent(
                targetState = if (clockedIn) elapsed else "Hazır",
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "elapsed"
            ) { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(20.dp))

            val buttonColor by animateColorAsState(
                if (clockedIn) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                label = "buttonColor"
            )
            val scale by animateFloatAsState(if (clockedIn) 1f else 1f, label = "s")

            Surface(
                shape = CircleShape,
                color = buttonColor,
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .pressableClick(
                        haptics = haptics,
                        event = if (clockedIn) HapticEvent.CLOCK_OUT else HapticEvent.CLOCK_IN
                    ) {
                        if (clockedIn) onClockOut() else onClockIn()
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (clockedIn) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                        contentDescription = if (clockedIn) "İşi bitir" else "İşe başla",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                text = if (clockedIn) "İşi Bitir" else "İşe Başla",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun OvertimeBanner(weekly: WeeklyWorkResult) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(Modifier.size(12.dp))
            Column {
                Text(
                    "Bu hafta mesai yaptın",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Fazla çalışma: ${Format.hoursShort(weekly.fazlaCalismaMinutes)} · " +
                        "Fazla süre: ${Format.hoursShort(weekly.fazlaSureMinutes)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun greeting(): String {
    val hour = LocalDateTime.now().hour
    return when (hour) {
        in 5..11 -> "Günaydın"
        in 12..17 -> "İyi günler"
        in 18..21 -> "İyi akşamlar"
        else -> "İyi geceler"
    }
}
