package com.mesaitakibi.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mesaitakibi.data.local.dao.HolidayDao
import com.mesaitakibi.data.local.dao.PayrollPeriodDao
import com.mesaitakibi.data.local.dao.SettingsDao
import com.mesaitakibi.data.local.dao.ShiftDao
import com.mesaitakibi.data.local.dao.TaxParametersDao
import com.mesaitakibi.data.local.dao.TimeEntryDao
import com.mesaitakibi.data.local.dao.TransactionDao
import com.mesaitakibi.data.local.entity.HolidayEntity
import com.mesaitakibi.data.local.entity.PayrollPeriodEntity
import com.mesaitakibi.data.local.entity.SettingsEntity
import com.mesaitakibi.data.local.entity.ShiftEntity
import com.mesaitakibi.data.local.entity.TaxParametersEntity
import com.mesaitakibi.data.local.entity.TimeEntryEntity
import com.mesaitakibi.data.local.entity.TransactionEntity

@Database(
    entities = [
        SettingsEntity::class,
        TimeEntryEntity::class,
        ShiftEntity::class,
        HolidayEntity::class,
        TaxParametersEntity::class,
        PayrollPeriodEntity::class,
        TransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MesaiDatabase : RoomDatabase() {
    abstract fun timeEntryDao(): TimeEntryDao
    abstract fun shiftDao(): ShiftDao
    abstract fun holidayDao(): HolidayDao
    abstract fun taxParametersDao(): TaxParametersDao
    abstract fun payrollPeriodDao(): PayrollPeriodDao
    abstract fun transactionDao(): TransactionDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        const val NAME = "mesai_takibi.db"
    }
}
