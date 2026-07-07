package com.mesaitakibi.ui.screens.leave

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mesaitakibi.data.local.entity.LeaveEntity
import com.mesaitakibi.data.prefs.PreferencesRepository
import com.mesaitakibi.data.repository.LeaveRepository
import com.mesaitakibi.domain.leave.AnnualLeaveCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class LeaveUiState(
    val entitlement: Int = 0,
    val usedDays: Int = 0,
    val remaining: Int = 0,
    val serviceYears: Int = 1,
    val age: Int = 30,
    val leaves: List<LeaveEntity> = emptyList()
)

@HiltViewModel
class LeaveViewModel @Inject constructor(
    private val repo: LeaveRepository,
    private val prefs: PreferencesRepository,
    private val calc: AnnualLeaveCalculator
) : ViewModel() {

    private val year = LocalDate.now().year

    val uiState: StateFlow<LeaveUiState> = combine(
        prefs.preferences, repo.observeAll(), repo.observeUsedDays(year)
    ) { p, leaves, used ->
        val entitlement = calc.entitlementDays(p.leaveServiceYears, p.leaveAge)
        LeaveUiState(
            entitlement = entitlement,
            usedDays = used,
            remaining = entitlement - used,
            serviceYears = p.leaveServiceYears,
            age = p.leaveAge,
            leaves = leaves
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LeaveUiState())

    fun setInfo(serviceYears: Int, age: Int) =
        viewModelScope.launch { prefs.setLeaveInfo(serviceYears, age) }

    fun add(startDate: LocalDate, days: Int, note: String?) =
        viewModelScope.launch { repo.add(LeaveEntity(startDate = startDate, days = days, note = note?.ifBlank { null })) }

    fun delete(leave: LeaveEntity) = viewModelScope.launch { repo.delete(leave) }
}
