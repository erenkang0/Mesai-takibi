package com.mesaitakibi.data.local

import com.mesaitakibi.data.local.dao.HolidayDao
import com.mesaitakibi.data.local.dao.SettingsDao
import com.mesaitakibi.data.local.dao.TaxParametersDao
import javax.inject.Inject
import javax.inject.Singleton

/** Uygulama ilk açılışında varsayılan ayar, vergi parametresi ve tatilleri yükler. */
@Singleton
class DatabaseSeeder @Inject constructor(
    private val settingsDao: SettingsDao,
    private val taxDao: TaxParametersDao,
    private val holidayDao: HolidayDao
) {
    suspend fun seedIfEmpty() {
        if (settingsDao.get() == null) {
            settingsDao.upsert(DefaultData.defaultSettings())
        }
        if (taxDao.count() == 0) {
            taxDao.upsert(DefaultData.defaultTaxParameters())
        }
        if (holidayDao.count() == 0) {
            holidayDao.insertAll(DefaultData.turkishHolidays2026())
        }
    }
}
