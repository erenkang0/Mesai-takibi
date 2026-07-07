package com.mesaitakibi.data.repository

import com.mesaitakibi.data.local.dao.LeaveDao
import com.mesaitakibi.data.local.entity.LeaveEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeaveRepository @Inject constructor(
    private val dao: LeaveDao
) {
    fun observeAll(): Flow<List<LeaveEntity>> = dao.observeAll()

    fun observeUsedDays(year: Int): Flow<Int> = dao.observeUsedDays(year.toString())

    suspend fun add(leave: LeaveEntity) { dao.insert(leave) }

    suspend fun delete(leave: LeaveEntity) = dao.delete(leave)
}
