package com.mesaitakibi.data.local

import com.mesaitakibi.data.local.entity.HolidayEntity
import com.mesaitakibi.data.local.entity.SettingsEntity
import com.mesaitakibi.data.local.entity.TaxParametersEntity
import com.mesaitakibi.data.mapper.toEntity
import com.mesaitakibi.domain.payroll.TaxParameters
import java.time.LocalDate

/** İlk kurulumda veritabanına yüklenecek varsayılan veriler. */
object DefaultData {

    fun defaultSettings(): SettingsEntity = SettingsEntity(id = 0)

    fun defaultTaxParameters(): TaxParametersEntity = TaxParameters.default2026().toEntity()

    /**
     * 2026 Türkiye resmî ve dinî bayram günleri.
     * NOT: Dinî bayram (Ramazan/Kurban) tarihleri hicri takvime göre değişir ve resmî
     * ilanla kesinleşir; kullanıcı Ayarlar > Tatiller ekranından güncelleyebilir.
     */
    fun turkishHolidays2026(): List<HolidayEntity> = listOf(
        HolidayEntity(LocalDate.of(2026, 1, 1), "Yılbaşı"),
        HolidayEntity(LocalDate.of(2026, 3, 20), "Ramazan Bayramı 1. Gün"),
        HolidayEntity(LocalDate.of(2026, 3, 21), "Ramazan Bayramı 2. Gün"),
        HolidayEntity(LocalDate.of(2026, 3, 22), "Ramazan Bayramı 3. Gün"),
        HolidayEntity(LocalDate.of(2026, 4, 23), "Ulusal Egemenlik ve Çocuk Bayramı"),
        HolidayEntity(LocalDate.of(2026, 5, 1), "Emek ve Dayanışma Günü"),
        HolidayEntity(LocalDate.of(2026, 5, 19), "Atatürk'ü Anma, Gençlik ve Spor Bayramı"),
        HolidayEntity(LocalDate.of(2026, 5, 27), "Kurban Bayramı 1. Gün"),
        HolidayEntity(LocalDate.of(2026, 5, 28), "Kurban Bayramı 2. Gün"),
        HolidayEntity(LocalDate.of(2026, 5, 29), "Kurban Bayramı 3. Gün"),
        HolidayEntity(LocalDate.of(2026, 5, 30), "Kurban Bayramı 4. Gün"),
        HolidayEntity(LocalDate.of(2026, 7, 15), "Demokrasi ve Millî Birlik Günü"),
        HolidayEntity(LocalDate.of(2026, 8, 30), "Zafer Bayramı"),
        HolidayEntity(LocalDate.of(2026, 10, 29), "Cumhuriyet Bayramı")
    )
}
