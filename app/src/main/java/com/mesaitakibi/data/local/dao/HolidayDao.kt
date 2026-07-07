package com.mesaitakibi.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mesaitakibi.data.local.entity.HolidayEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface HolidayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(holidays: List<HolidayEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(holiday: HolidayEntity)

    @Delete
    suspend fun delete(holiday: HolidayEntity)

    @Query("SELECT * FROM holidays ORDER BY date")
    fun observeAll(): Flow<List<HolidayEntity>>

    @Query("SELECT * FROM holidays WHERE date BETWEEN :start AND :end")
    suspend fun getBetween(start: LocalDate, end: LocalDate): List<HolidayEntity>

    @Query("SELECT COUNT(*) FROM holidays")
    suspend fun count(): Int

    @Query("SELECT * FROM holidays")
    suspend fun getAll(): List<HolidayEntity>

    @Query("DELETE FROM holidays")
    suspend fun deleteAll()
}
