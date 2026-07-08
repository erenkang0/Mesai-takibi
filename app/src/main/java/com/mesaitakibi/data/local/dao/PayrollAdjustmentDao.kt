package com.mesaitakibi.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mesaitakibi.data.local.entity.PayrollAdjustmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PayrollAdjustmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(adjustment: PayrollAdjustmentEntity): Long

    @Delete
    suspend fun delete(adjustment: PayrollAdjustmentEntity)

    @Query("SELECT * FROM payroll_adjustments WHERE year = :year AND month = :month ORDER BY id DESC")
    fun observeForMonth(year: Int, month: Int): Flow<List<PayrollAdjustmentEntity>>

    @Query("SELECT * FROM payroll_adjustments WHERE year = :year AND month = :month")
    suspend fun getForMonth(year: Int, month: Int): List<PayrollAdjustmentEntity>

    @Query("DELETE FROM payroll_adjustments")
    suspend fun deleteAll()
}
