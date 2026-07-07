package com.mesaitakibi.data.repository

import com.mesaitakibi.data.local.dao.HolidayDao
import com.mesaitakibi.data.local.entity.HolidayEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HolidayRepository @Inject constructor(
    private val dao: HolidayDao
) {
    fun observeAll(): Flow<List<HolidayEntity>> = dao.observeAll()
    suspend fun upsert(holiday: HolidayEntity) = dao.upsert(holiday)
    suspend fun delete(holiday: HolidayEntity) = dao.delete(holiday)
}
