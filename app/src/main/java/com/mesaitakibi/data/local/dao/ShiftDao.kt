package com.mesaitakibi.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mesaitakibi.data.local.entity.ShiftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(shift: ShiftEntity)

    @Delete
    suspend fun delete(shift: ShiftEntity)

    @Query("SELECT * FROM shifts ORDER BY dayOfWeek")
    fun observeAll(): Flow<List<ShiftEntity>>

    @Query("SELECT * FROM shifts WHERE active = 1")
    suspend fun getActive(): List<ShiftEntity>

    @Query("SELECT * FROM shifts")
    suspend fun getAll(): List<ShiftEntity>

    @Query("DELETE FROM shifts")
    suspend fun deleteAll()
}
