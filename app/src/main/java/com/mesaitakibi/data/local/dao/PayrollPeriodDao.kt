package com.mesaitakibi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mesaitakibi.data.local.entity.PayrollPeriodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PayrollPeriodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(period: PayrollPeriodEntity)

    @Query("SELECT * FROM payroll_periods WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): PayrollPeriodEntity?

    @Query("SELECT * FROM payroll_periods ORDER BY year DESC, month DESC")
    fun observeAll(): Flow<List<PayrollPeriodEntity>>

    @Query("SELECT * FROM payroll_periods")
    suspend fun getAll(): List<PayrollPeriodEntity>

    @Query("DELETE FROM payroll_periods")
    suspend fun deleteAll()
}
