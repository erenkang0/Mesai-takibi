package com.mesaitakibi.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mesaitakibi.data.local.entity.LeaveEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaveDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(leave: LeaveEntity): Long

    @Delete
    suspend fun delete(leave: LeaveEntity)

    @Query("SELECT * FROM leaves ORDER BY startDate DESC")
    fun observeAll(): Flow<List<LeaveEntity>>

    @Query("SELECT COALESCE(SUM(days), 0) FROM leaves WHERE strftime('%Y', startDate) = :year")
    fun observeUsedDays(year: String): Flow<Int>
}
