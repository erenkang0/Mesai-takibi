package com.mesaitakibi.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mesaitakibi.data.repository.TimeTrackingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.Duration
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val timeTracking: TimeTrackingRepository
) : ViewModel() {

    private val _month = MutableStateFlow(YearMonth.now())
    val month: StateFlow<YearMonth> = _month.asStateFlow()

    /** Ay içindeki her gün için çalışılan dakika. */
    val dayMinutes: StateFlow<Map<LocalDate, Long>> =
        combine(_month, timeTracking.observeAll()) { ym, entries ->
            entries.filter { it.clockOut != null && YearMonth.from(it.date) == ym }
                .groupBy { it.date }
                .mapValues { (_, list) ->
                    list.sumOf { e ->
                        (Duration.between(e.clockIn, e.clockOut).toMinutes() - e.breakMinutes).coerceAtLeast(0)
                    }
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun previousMonth() = _month.update { it.minusMonths(1) }
    fun nextMonth() = _month.update { it.plusMonths(1) }
}
