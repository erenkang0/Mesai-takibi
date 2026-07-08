package com.mesaitakibi.ui.screens.payroll

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mesaitakibi.data.local.entity.PayrollAdjustmentEntity
import com.mesaitakibi.data.local.entity.PayrollPeriodEntity
import com.mesaitakibi.data.repository.PayrollRepository
import com.mesaitakibi.domain.payroll.PayrollResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class PayrollUiState(
    val year: Int,
    val month: Int,
    val result: PayrollResult? = null,
    val computing: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PayrollViewModel @Inject constructor(
    private val repo: PayrollRepository
) : ViewModel() {

    private val today = LocalDate.now()
    private val _state = MutableStateFlow(PayrollUiState(today.year, today.monthValue))
    val state: StateFlow<PayrollUiState> = _state.asStateFlow()

    val history: StateFlow<List<PayrollPeriodEntity>> =
        repo.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val adjustments: StateFlow<List<PayrollAdjustmentEntity>> =
        _state.flatMapLatest { repo.observeAdjustments(it.year, it.month) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init { compute() }

    fun addAdjustment(label: String, amount: BigDecimal, kind: String) = viewModelScope.launch {
        repo.addAdjustment(
            PayrollAdjustmentEntity(
                year = _state.value.year, month = _state.value.month,
                label = label.ifBlank { "Kalem" }, amount = amount.toPlainString(), kind = kind
            )
        )
        compute()
    }

    fun deleteAdjustment(adjustment: PayrollAdjustmentEntity) = viewModelScope.launch {
        repo.deleteAdjustment(adjustment)
        compute()
    }

    fun previousMonth() {
        val ym = YearMonth.of(_state.value.year, _state.value.month).minusMonths(1)
        _state.update { it.copy(year = ym.year, month = ym.monthValue) }
        compute()
    }

    fun nextMonth() {
        val ym = YearMonth.of(_state.value.year, _state.value.month).plusMonths(1)
        _state.update { it.copy(year = ym.year, month = ym.monthValue) }
        compute()
    }

    fun compute() = viewModelScope.launch {
        _state.update { it.copy(computing = true) }
        val r = repo.computeAndSave(_state.value.year, _state.value.month)
        _state.update { it.copy(result = r, computing = false) }
    }
}
