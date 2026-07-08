package com.mesaitakibi.data.repository

import com.mesaitakibi.data.local.dao.PayrollAdjustmentDao
import com.mesaitakibi.data.local.dao.PayrollPeriodDao
import com.mesaitakibi.data.local.entity.PayrollAdjustmentEntity
import com.mesaitakibi.data.local.entity.PayrollPeriodEntity
import kotlinx.coroutines.flow.Flow
import com.mesaitakibi.domain.overtime.WeeklyWorkResult
import com.mesaitakibi.domain.payroll.MonthlyWork
import com.mesaitakibi.domain.payroll.PayrollCalculator
import com.mesaitakibi.domain.payroll.PayrollResult
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PayrollRepository @Inject constructor(
    private val payrollDao: PayrollPeriodDao,
    private val adjustmentDao: PayrollAdjustmentDao,
    private val timeTracking: TimeTrackingRepository,
    private val settingsRepo: SettingsRepository,
    private val taxRepo: TaxRepository,
    private val calculator: PayrollCalculator
) {
    fun observeAll(): Flow<List<PayrollPeriodEntity>> = payrollDao.observeAll()

    fun observeAdjustments(year: Int, month: Int): Flow<List<PayrollAdjustmentEntity>> =
        adjustmentDao.observeForMonth(year, month)

    suspend fun addAdjustment(adjustment: PayrollAdjustmentEntity) { adjustmentDao.insert(adjustment) }

    suspend fun deleteAdjustment(adjustment: PayrollAdjustmentEntity) = adjustmentDao.delete(adjustment)

    suspend fun getSaved(year: Int, month: Int): PayrollPeriodEntity? =
        payrollDao.getById(year * 100 + month)

    /**
     * Ayın bordrosunu hesaplar. Mesai haftalık kurala göre bulunduğundan, ayın içindeki
     * her ISO haftası (Pazartesi'si ay içinde olan) analiz edilip zamlar toplanır.
     */
    suspend fun computeMonth(year: Int, month: Int): PayrollResult {
        val profile = settingsRepo.getProfile()
        val tax = taxRepo.getForYear(year)

        val monthStart = LocalDate.of(year, month, 1)
        val monthEnd = monthStart.plusMonths(1).minusDays(1)

        var normalMin = 0L
        var fazlaSureMin = 0L
        var fazlaCalismaMin = 0L
        var holidayMin = 0L
        var nightMin = 0L

        var weekStart = monthStart.with(DayOfWeek.MONDAY)
        while (!weekStart.isAfter(monthEnd)) {
            if (!weekStart.isBefore(monthStart)) {
                val r: WeeklyWorkResult = timeTracking.analyzeWeek(weekStart)
                normalMin += r.normalMinutes
                fazlaSureMin += r.fazlaSureMinutes
                fazlaCalismaMin += r.fazlaCalismaMinutes
                holidayMin += r.holidayMinutes
                nightMin += r.nightMinutes
            }
            weekStart = weekStart.plusDays(7)
        }

        val work = MonthlyWork(
            year = year,
            monthIndex = month,
            normalHours = minutesToHours(normalMin),
            fazlaSureHours = minutesToHours(fazlaSureMin),
            fazlaCalismaHours = minutesToHours(fazlaCalismaMin),
            holidayHours = minutesToHours(holidayMin),
            nightHours = minutesToHours(nightMin)
        )

        val adjustments = adjustmentDao.getForMonth(year, month)
        fun sumOf(kind: String) = adjustments.filter { it.kind == kind }
            .fold(BigDecimal.ZERO) { acc, a -> acc + BigDecimal(a.amount) }

        return calculator.calculate(
            work, profile, tax,
            additionalTaxableEarnings = sumOf(PayrollAdjustmentEntity.EARNING_TAXABLE),
            netAdditions = sumOf(PayrollAdjustmentEntity.EARNING_NET),
            netDeductions = sumOf(PayrollAdjustmentEntity.DEDUCTION_NET)
        )
    }

    suspend fun computeAndSave(year: Int, month: Int): PayrollResult {
        val result = computeMonth(year, month)
        payrollDao.upsert(result.toEntity(year, month))
        return result
    }

    private fun minutesToHours(minutes: Long): BigDecimal =
        BigDecimal(minutes).divide(BigDecimal(60), 4, RoundingMode.HALF_UP)

    private fun PayrollResult.toEntity(year: Int, month: Int) = PayrollPeriodEntity(
        id = year * 100 + month,
        year = year,
        month = month,
        gross = gross.toPlainString(),
        fazlaSurePay = fazlaSurePay.toPlainString(),
        fazlaCalismaPay = fazlaCalismaPay.toPlainString(),
        holidayPay = holidayPay.toPlainString(),
        sgkEmployee = sgkEmployee.toPlainString(),
        unemployment = unemployment.toPlainString(),
        incomeTax = incomeTax.toPlainString(),
        stampTax = stampTax.toPlainString(),
        totalDeductions = totalDeductions.toPlainString(),
        net = net.toPlainString(),
        generatedAt = LocalDateTime.now()
    )
}
