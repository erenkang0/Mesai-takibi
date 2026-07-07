package com.mesaitakibi.ui.screens.tax

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mesaitakibi.data.repository.TaxRepository
import com.mesaitakibi.domain.payroll.TaxParameters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TaxViewModel @Inject constructor(
    private val repo: TaxRepository
) : ViewModel() {

    private val currentYear = LocalDate.now().year

    val params: StateFlow<TaxParameters?> =
        repo.observeLatest().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun save(params: TaxParameters) = viewModelScope.launch { repo.save(params) }

    fun resetToDefaults() = viewModelScope.launch {
        repo.save(TaxParameters.default2026().copy(year = currentYear))
    }
}
