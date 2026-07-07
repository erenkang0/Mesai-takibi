package com.mesaitakibi.data.repository

import com.mesaitakibi.data.local.dao.ShiftDao
import com.mesaitakibi.data.local.entity.ShiftEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShiftRepository @Inject constructor(
    private val dao: ShiftDao
) {
    fun observeAll(): Flow<List<ShiftEntity>> = dao.observeAll()
    suspend fun upsert(shift: ShiftEntity) = dao.upsert(shift)
    suspend fun delete(shift: ShiftEntity) = dao.delete(shift)
}
