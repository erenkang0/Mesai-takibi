package com.mesaitakibi.ui.screens.timetracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mesaitakibi.data.local.entity.TimeEntryEntity
import com.mesaitakibi.data.repository.TimeTrackingRepository
import com.mesaitakibi.domain.overtime.WeeklyWorkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TimeTrackingUiState(
    val entries: List<TimeEntryEntity> = emptyList(),
    val weekly: WeeklyWorkResult? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TimeTrackingViewModel @Inject constructor(
    private val repo: TimeTrackingRepository
) : ViewModel() {

    val uiState: StateFlow<TimeTrackingUiState> =
        repo.observeAll().mapLatest { entries ->
            val weekly = repo.analyzeWeek(TimeTrackingRepository.weekStartOf(LocalDate.now()))
            TimeTrackingUiState(entries.filter { it.clockOut != null }, weekly)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimeTrackingUiState())

    fun save(entry: TimeEntryEntity) = viewModelScope.launch { repo.addOrUpdate(entry) }

    fun delete(entry: TimeEntryEntity) = viewModelScope.launch { repo.delete(entry) }
}
