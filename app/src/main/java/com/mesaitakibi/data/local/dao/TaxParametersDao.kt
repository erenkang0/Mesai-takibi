package com.mesaitakibi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mesaitakibi.data.local.entity.TaxParametersEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaxParametersDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(params: TaxParametersEntity)

    @Query("SELECT * FROM tax_parameters WHERE year = :year LIMIT 1")
    suspend fun getByYear(year: Int): TaxParametersEntity?

    @Query("SELECT * FROM tax_parameters ORDER BY year DESC LIMIT 1")
    fun observeLatest(): Flow<TaxParametersEntity?>

    @Query("SELECT COUNT(*) FROM tax_parameters")
    suspend fun count(): Int

    @Query("SELECT * FROM tax_parameters")
    suspend fun getAll(): List<TaxParametersEntity>

    @Query("DELETE FROM tax_parameters")
    suspend fun deleteAll()
}
