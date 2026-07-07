package com.mesaitakibi.di

import android.content.Context
import androidx.room.Room
import com.mesaitakibi.data.local.MesaiDatabase
import com.mesaitakibi.data.local.dao.HolidayDao
import com.mesaitakibi.data.local.dao.PayrollPeriodDao
import com.mesaitakibi.data.local.dao.SettingsDao
import com.mesaitakibi.data.local.dao.ShiftDao
import com.mesaitakibi.data.local.dao.TaxParametersDao
import com.mesaitakibi.data.local.dao.TimeEntryDao
import com.mesaitakibi.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MesaiDatabase =
        Room.databaseBuilder(context, MesaiDatabase::class.java, MesaiDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun timeEntryDao(db: MesaiDatabase): TimeEntryDao = db.timeEntryDao()
    @Provides fun shiftDao(db: MesaiDatabase): ShiftDao = db.shiftDao()
    @Provides fun holidayDao(db: MesaiDatabase): HolidayDao = db.holidayDao()
    @Provides fun taxDao(db: MesaiDatabase): TaxParametersDao = db.taxParametersDao()
    @Provides fun payrollDao(db: MesaiDatabase): PayrollPeriodDao = db.payrollPeriodDao()
    @Provides fun transactionDao(db: MesaiDatabase): TransactionDao = db.transactionDao()
    @Provides fun settingsDao(db: MesaiDatabase): SettingsDao = db.settingsDao()
}
