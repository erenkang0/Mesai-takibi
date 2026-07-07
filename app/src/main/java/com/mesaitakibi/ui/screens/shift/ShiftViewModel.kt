package com.mesaitakibi.ui.screens.shift

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mesaitakibi.data.local.entity.ShiftEntity
import com.mesaitakibi.data.repository.ShiftRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class ShiftViewModel @Inject constructor(
    private val repo: ShiftRepository
) : ViewModel() {

    /** Haftanın 7 günü için vardiya; kayıtlı olmayan gün pasif varsayılan olarak döner. */
    val days: StateFlow<List<ShiftEntity>> =
        repo.observeAll().map { stored ->
            (1..7).map { dow ->
                stored.firstOrNull { it.dayOfWeek == dow }
                    ?: ShiftEntity(dow, LocalTime.of(9, 0), LocalTime.of(18, 0), 60, active = false)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(shift: ShiftEntity) = viewModelScope.launch { repo.upsert(shift) }
}
