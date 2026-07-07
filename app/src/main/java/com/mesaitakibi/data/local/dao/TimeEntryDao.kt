package com.mesaitakibi.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mesaitakibi.data.local.entity.TimeEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TimeEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: TimeEntryEntity): Long

    @Update
    suspend fun update(entry: TimeEntryEntity)

    @Delete
    suspend fun delete(entry: TimeEntryEntity)

    @Query("SELECT * FROM time_entries ORDER BY clockIn DESC")
    fun observeAll(): Flow<List<TimeEntryEntity>>

    @Query("SELECT * FROM time_entries WHERE date BETWEEN :start AND :end ORDER BY clockIn")
    fun observeBetween(start: LocalDate, end: LocalDate): Flow<List<TimeEntryEntity>>

    @Query("SELECT * FROM time_entries WHERE date BETWEEN :start AND :end ORDER BY clockIn")
    suspend fun getBetween(start: LocalDate, end: LocalDate): List<TimeEntryEntity>

    /** Devam eden (çıkış yapılmamış) oturum. */
    @Query("SELECT * FROM time_entries WHERE clockOut IS NULL ORDER BY clockIn DESC LIMIT 1")
    fun observeOpenSession(): Flow<TimeEntryEntity?>

    @Query("SELECT * FROM time_entries WHERE clockOut IS NULL ORDER BY clockIn DESC LIMIT 1")
    suspend fun getOpenSession(): TimeEntryEntity?

    @Query("SELECT * FROM time_entries")
    suspend fun getAll(): List<TimeEntryEntity>

    @Query("DELETE FROM time_entries")
    suspend fun deleteAll()
}
