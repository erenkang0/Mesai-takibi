package com.mesaitakibi.data.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.mesaitakibi.data.local.dao.HolidayDao
import com.mesaitakibi.data.local.dao.PayrollPeriodDao
import com.mesaitakibi.data.local.dao.SettingsDao
import com.mesaitakibi.data.local.dao.ShiftDao
import com.mesaitakibi.data.local.dao.TaxParametersDao
import com.mesaitakibi.data.local.dao.TimeEntryDao
import com.mesaitakibi.data.local.dao.TransactionDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Kapsamlı veri yönetimi: tüm veritabanını JSON'a yedekler, geri yükler, CSV dışa
 * aktarır ve "Paylaş" (Intent.ACTION_SEND) ile Google Drive dahil herhangi bir
 * uygulamaya gönderilebilecek dosya üretir. (Google Drive API kullanılmaz.)
 */
@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsDao: SettingsDao,
    private val timeEntryDao: TimeEntryDao,
    private val shiftDao: ShiftDao,
    private val holidayDao: HolidayDao,
    private val taxDao: TaxParametersDao,
    private val payrollDao: PayrollPeriodDao,
    private val transactionDao: TransactionDao
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val stamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")

    // --- Dışa aktarma ---

    suspend fun exportJson(): String {
        val data = BackupData(
            version = 1,
            exportedAt = LocalDateTime.now().toString(),
            settings = settingsDao.get()?.toDto(),
            timeEntries = timeEntryDao.getAll().map { it.toDto() },
            shifts = shiftDao.getAll().map { it.toDto() },
            holidays = holidayDao.getAll().map { it.toDto() },
            taxParameters = taxDao.getAll().map { it.toDto() },
            payrollPeriods = payrollDao.getAll().map { it.toDto() },
            transactions = transactionDao.getAll().map { it.toDto() }
        )
        return json.encodeToString(data)
    }

    /** Yedeği cacheDir/backups altına yazar ve dosyayı döner (paylaşıma hazır). */
    suspend fun writeBackupFile(): File {
        val dir = File(context.cacheDir, "backups").apply { mkdirs() }
        val file = File(dir, "mesai-yedek-${LocalDateTime.now().format(stamp)}.json")
        file.writeText(exportJson())
        return file
    }

    /** Puantaj kayıtlarını CSV olarak dışa aktarır. */
    suspend fun writeTimeEntriesCsv(): File {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "puantaj-${LocalDateTime.now().format(stamp)}.csv")
        val sb = StringBuilder("Tarih;Giris;Cikis;Mola(dk);Not\n")
        timeEntryDao.getAll().sortedBy { it.clockIn }.forEach { e ->
            sb.append("${e.date};${e.clockIn};${e.clockOut ?: ""};${e.breakMinutes};${e.note ?: ""}\n")
        }
        file.writeText(sb.toString())
        return file
    }

    fun shareableUri(file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    fun shareIntent(file: File, mime: String): Intent {
        val uri = shareableUri(file)
        return Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    // --- İçe aktarma / geri yükleme ---

    suspend fun importJson(content: String) {
        val data = json.decodeFromString<BackupData>(content)
        clearAll()
        data.settings?.let { settingsDao.upsert(it.toEntity()) }
        data.timeEntries.forEach { timeEntryDao.insert(it.toEntity()) }
        data.shifts.forEach { shiftDao.upsert(it.toEntity()) }
        holidayDao.insertAll(data.holidays.map { it.toEntity() })
        data.taxParameters.forEach { taxDao.upsert(it.toEntity()) }
        data.payrollPeriods.forEach { payrollDao.upsert(it.toEntity()) }
        data.transactions.forEach { transactionDao.insert(it.toEntity()) }
    }

    suspend fun clearAll() {
        timeEntryDao.deleteAll()
        shiftDao.deleteAll()
        holidayDao.deleteAll()
        taxDao.deleteAll()
        payrollDao.deleteAll()
        transactionDao.deleteAll()
        settingsDao.deleteAll()
    }

    /** Yedek dosyasının içeriğini bir Uri'den okur (SAF ile seçilen dosya). */
    fun readUri(uri: Uri): String =
        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
}
