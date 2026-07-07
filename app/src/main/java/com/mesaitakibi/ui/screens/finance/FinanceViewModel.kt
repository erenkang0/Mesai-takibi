package com.mesaitakibi.ui.screens.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mesaitakibi.data.local.entity.TransactionEntity
import com.mesaitakibi.data.repository.FinanceRepository
import com.mesaitakibi.domain.finance.MonthlyBudget
import com.mesaitakibi.domain.finance.TransactionType
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

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val repo: FinanceRepository
) : ViewModel() {

    private val today = LocalDate.now()
    private val _period = MutableStateFlow(YearMonth.of(today.year, today.monthValue))
    val period: StateFlow<YearMonth> = _period.asStateFlow()

    val summary: StateFlow<MonthlyBudget> =
        _period.flatMapLatest { ym -> repo.observeMonthlySummary(ym.year, ym.monthValue) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                MonthlyBudget(today.year, today.monthValue, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, emptyMap())
            )

    val transactions: StateFlow<List<TransactionEntity>> =
        repo.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun previousMonth() = _period.update { it.minusMonths(1) }
    fun nextMonth() = _period.update { it.plusMonths(1) }

    fun add(amount: BigDecimal, type: TransactionType, category: String, date: LocalDate, note: String?) =
        viewModelScope.launch {
            repo.add(
                TransactionEntity(
                    date = date,
                    amount = amount.toPlainString(),
                    type = type.name,
                    category = category.ifBlank { "Diğer" },
                    note = note?.ifBlank { null }
                )
            )
        }

    fun delete(txn: TransactionEntity) = viewModelScope.launch { repo.delete(txn) }
}
