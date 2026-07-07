package com.mesaitakibi.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mesaitakibi.data.local.entity.ShiftEntity
import com.mesaitakibi.data.prefs.PreferencesRepository
import com.mesaitakibi.data.repository.SettingsRepository
import com.mesaitakibi.data.repository.ShiftRepository
import com.mesaitakibi.domain.model.EmployeeProfile
import com.mesaitakibi.domain.model.WageType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val shifts: ShiftRepository,
    private val prefs: PreferencesRepository
) : ViewModel() {

    /**
     * Onboarding'i tamamlar: profili kaydeder, normal çalışma saatlerinden haftalık
     * plan şablonu üretir (bu sayede mesai, normal bitiş saatinden sonra algılanır),
     * ve onboarding bayrağını işaretler.
     */
    fun complete(
        name: String,
        wageType: WageType,
        grossWage: BigDecimal,
        weeklyContractHours: Double,
        workStart: LocalTime,
        workEnd: LocalTime,
        breakMinutes: Int,
        workingDays: Set<DayOfWeek>,
        onDone: () -> Unit
    ) = viewModelScope.launch {
        val restDay = DayOfWeek.entries.firstOrNull { it !in workingDays } ?: DayOfWeek.SUNDAY

        settings.save(
            EmployeeProfile(
                name = name.trim(),
                wageType = wageType,
                grossWage = grossWage,
                weeklyContractHours = weeklyContractHours,
                weeklyRestDay = restDay
            )
        )

        workingDays.forEach { day ->
            shifts.upsert(
                ShiftEntity(
                    dayOfWeek = day.value,
                    start = workStart,
                    end = workEnd,
                    breakMinutes = breakMinutes,
                    active = true
                )
            )
        }

        prefs.setOnboarded(true)
        onDone()
    }
}
