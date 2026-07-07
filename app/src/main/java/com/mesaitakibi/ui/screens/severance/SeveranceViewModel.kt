package com.mesaitakibi.ui.screens.severance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mesaitakibi.data.repository.SettingsRepository
import com.mesaitakibi.domain.payroll.SeveranceCalculator
import com.mesaitakibi.domain.payroll.SeveranceResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SeveranceViewModel @Inject constructor(
    private val calculator: SeveranceCalculator,
    private val settings: SettingsRepository
) : ViewModel() {

    private val _defaultGross = MutableStateFlow(BigDecimal.ZERO)
    val defaultGross: StateFlow<BigDecimal> = _defaultGross.asStateFlow()

    private val _result = MutableStateFlow<SeveranceResult?>(null)
    val result: StateFlow<SeveranceResult?> = _result.asStateFlow()

    init {
        viewModelScope.launch { _defaultGross.value = settings.getProfile().grossWage }
    }

    fun compute(start: LocalDate, end: LocalDate, monthlyGross: BigDecimal, ceiling: BigDecimal) {
        _result.value = calculator.calculate(start, end, monthlyGross, ceiling)
    }

    companion object {
        /** 2026 kıdem tazminatı tavanı için başlangıç değeri (düzenlenebilir; resmî kaynaktan doğrulayın). */
        val DEFAULT_CEILING_2026: BigDecimal = BigDecimal("53919.68")
    }
}
