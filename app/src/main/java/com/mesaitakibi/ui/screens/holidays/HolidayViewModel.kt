package com.mesaitakibi.ui.screens.holidays

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mesaitakibi.data.local.entity.HolidayEntity
import com.mesaitakibi.data.repository.HolidayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HolidayViewModel @Inject constructor(
    private val repo: HolidayRepository
) : ViewModel() {

    val holidays: StateFlow<List<HolidayEntity>> =
        repo.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(date: LocalDate, name: String) = viewModelScope.launch {
        repo.upsert(HolidayEntity(date = date, name = name.ifBlank { "Tatil" }))
    }

    fun delete(holiday: HolidayEntity) = viewModelScope.launch { repo.delete(holiday) }
}
