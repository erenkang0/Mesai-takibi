package com.mesaitakibi.ui.util

import java.math.BigDecimal
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Türkçe biçimlendirme yardımcıları (₺, saat, tarih). */
object Format {
    private val turkish = Locale("tr", "TR")
    private val currency: NumberFormat = NumberFormat.getCurrencyInstance(turkish)
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", turkish)
    val dayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d MMM", turkish)
    val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", turkish)

    fun money(value: BigDecimal): String = currency.format(value)

    fun hoursShort(minutes: Long): String {
        val h = minutes / 60
        val m = minutes % 60
        return if (m == 0L) "$h sa" else "$h sa $m dk"
    }

    fun decimalHours(minutes: Long): String =
        String.format(turkish, "%.2f", minutes / 60.0)

    val monthNames = arrayOf(
        "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
        "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
    )

    fun monthName(month: Int): String = monthNames[(month - 1).coerceIn(0, 11)]
}
