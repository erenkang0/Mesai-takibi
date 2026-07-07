package com.mesaitakibi.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mesaitakibi.data.repository.PayrollRepository
import com.mesaitakibi.data.repository.SettingsRepository
import com.mesaitakibi.data.repository.TimeTrackingRepository
import com.mesaitakibi.domain.overtime.WeeklyWorkResult
import com.mesaitakibi.notification.WorkSessionController
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

data class DashboardUiState(
    val loading: Boolean = true,
    val clockedIn: Boolean = false,
    val clockInSince: LocalDateTime? = null,
    val weekly: WeeklyWorkResult? = null,
    val estimatedNet: BigDecimal? = null,
    val userName: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val timeTracking: TimeTrackingRepository,
    private val payroll: PayrollRepository,
    private val settings: SettingsRepository,
    private val workSessionController: WorkSessionController
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> =
        combine(timeTracking.observeOpenSession(), timeTracking.observeAll()) { open, _ -> open }
            .mapLatest { open ->
                val weekStart = TimeTrackingRepository.weekStartOf(LocalDate.now())
                val weekly = timeTracking.analyzeWeek(weekStart)
                val today = LocalDate.now()
                val net = runCatching {
                    payroll.computeMonth(today.year, today.monthValue).net
                }.getOrNull()
                val profile = settings.getProfile()
                DashboardUiState(
                    loading = false,
                    clockedIn = open != null,
                    clockInSince = open?.clockIn,
                    weekly = weekly,
                    estimatedNet = net,
                    userName = profile.name
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DashboardUiState(loading = true)
            )

    fun clockIn() = viewModelScope.launch {
        timeTracking.clockIn()
        val open = timeTracking.openSession() ?: return@launch
        val zone = ZoneId.systemDefault()
        val startMillis = open.clockIn.atZone(zone).toInstant().toEpochMilli()
        val overtimeAt = timeTracking.plannedEndForDate(open.date)?.let { end ->
            LocalDateTime.of(open.date, end).atZone(zone).toInstant().toEpochMilli()
        }
        workSessionController.start(startMillis, overtimeAt)
    }

    fun clockOut() = viewModelScope.launch {
        workSessionController.stop()
        timeTracking.clockOut()
    }
}
