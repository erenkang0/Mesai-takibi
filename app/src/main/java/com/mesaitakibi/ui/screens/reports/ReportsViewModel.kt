package com.mesaitakibi.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mesaitakibi.data.repository.FinanceRepository
import com.mesaitakibi.data.repository.PayrollRepository
import com.mesaitakibi.data.repository.TimeTrackingRepository
import com.mesaitakibi.ui.components.BarDatum
import com.mesaitakibi.ui.util.Format
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

data class ReportsUiState(
    val weekly: List<BarDatum> = emptyList(),
    val netByMonth: List<BarDatum> = emptyList(),
    val expenseByCategory: List<Pair<String, BigDecimal>> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val timeTracking: TimeTrackingRepository,
    private val payroll: PayrollRepository,
    private val finance: FinanceRepository
) : ViewModel() {

    private val today = LocalDate.now()

    val uiState: StateFlow<ReportsUiState> = combine(
        timeTracking.observeAll(),
        payroll.observeAll(),
        finance.observeMonthlySummary(today.year, today.monthValue)
    ) { _, periods, budget -> periods to budget }
        .mapLatest { (periods, budget) ->
            val weekly = computeWeekly()
            val net = periods
                .sortedBy { it.year * 100 + it.month }
                .takeLast(6)
                .map { BarDatum(Format.monthName(it.month).take(3), BigDecimal(it.net).toFloat()) }
            val expenses = budget.expenseByCategory.entries
                .sortedByDescending { it.value }
                .map { it.key to it.value }
            ReportsUiState(weekly, net, expenses)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportsUiState())

    private suspend fun computeWeekly(): List<BarDatum> {
        val monday = TimeTrackingRepository.weekStartOf(today)
        return (7 downTo 0).map { i ->
            val ws = monday.minusWeeks(i.toLong())
            val r = timeTracking.analyzeWeek(ws)
            BarDatum(
                label = "${ws.dayOfMonth}.${ws.monthValue}",
                value = r.totalWorkedMinutes / 60f,
                highlight = (r.fazlaCalismaMinutes + r.fazlaSureMinutes) / 60f
            )
        }
    }
}
