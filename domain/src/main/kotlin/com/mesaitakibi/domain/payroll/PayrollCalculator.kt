package com.mesaitakibi.domain.payroll

import com.mesaitakibi.domain.model.EmployeeProfile
import com.mesaitakibi.domain.model.WageType
import java.math.BigDecimal
import java.math.RoundingMode

/** Bir ay için çalışma özeti (saat cinsinden). Mesai motorundan üretilir. */
data class MonthlyWork(
    val year: Int,
    val monthIndex: Int, // 1..12, kümülatif gelir vergisi için
    val normalHours: BigDecimal = BigDecimal.ZERO,
    val fazlaSureHours: BigDecimal = BigDecimal.ZERO,
    val fazlaCalismaHours: BigDecimal = BigDecimal.ZERO,
    val holidayHours: BigDecimal = BigDecimal.ZERO,
    val nightHours: BigDecimal = BigDecimal.ZERO
)

/** Aylık bordro sonucu (brüt → net döküm). */
data class PayrollResult(
    val gross: BigDecimal,
    val basePay: BigDecimal,
    val fazlaSurePay: BigDecimal,
    val fazlaCalismaPay: BigDecimal,
    val holidayPay: BigDecimal,
    val sgkEmployee: BigDecimal,
    val unemployment: BigDecimal,
    val incomeTax: BigDecimal,
    val stampTax: BigDecimal,
    val totalDeductions: BigDecimal,
    val netAdditions: BigDecimal = BigDecimal.ZERO,
    val netDeductions: BigDecimal = BigDecimal.ZERO,
    val net: BigDecimal
)

/**
 * Bordro motoru: brütü hesaplar (temel ücret + mesai zamları) ve yasal kesintileri
 * uygulayarak neti bulur.
 *
 * Kesintiler: SGK işçi payı, işsizlik işçi payı, gelir vergisi (kümülatif matrah,
 * artan oranlı), damga vergisi. Asgari ücret gelir/damga vergisi istisnası uygulanır.
 */
class PayrollCalculator {
    private val scale = 2
    private val rm = RoundingMode.HALF_UP

    fun calculate(
        work: MonthlyWork,
        profile: EmployeeProfile,
        tax: TaxParameters,
        cumulativeIncomeTaxBaseBefore: BigDecimal? = null,
        additionalTaxableEarnings: BigDecimal = BigDecimal.ZERO,
        netAdditions: BigDecimal = BigDecimal.ZERO,
        netDeductions: BigDecimal = BigDecimal.ZERO
    ): PayrollResult {
        val hourly = profile.hourlyGross()

        val base = when (profile.wageType) {
            WageType.MONTHLY -> profile.grossWage
            WageType.HOURLY -> work.normalHours * hourly
        }
        val fazlaSurePay = work.fazlaSureHours * hourly * BigDecimal("1.25")
        val fazlaCalismaPay = work.fazlaCalismaHours * hourly * BigDecimal("1.50")
        val holidayPay = work.holidayHours * hourly * BigDecimal("1.00") // resmî tatil ek %100

        val gross = (base + fazlaSurePay + fazlaCalismaPay + holidayPay + additionalTaxableEarnings)
            .setScale(scale, rm)

        // --- Yasal kesintiler ---
        val sgkBase = gross.min(tax.sgkCeiling)
        val sgkEmployee = (sgkBase * tax.sgkEmployeeRate).setScale(scale, rm)
        val unemployment = (sgkBase * tax.unemploymentEmployeeRate).setScale(scale, rm)

        val incomeTaxBase = gross - sgkEmployee - unemployment
        val cumBefore = cumulativeIncomeTaxBaseBefore
            ?: incomeTaxBase * BigDecimal(work.monthIndex - 1) // sabit gelir varsayımı
        val grossIncomeTax = progressiveTax(cumBefore + incomeTaxBase, tax.incomeTaxBrackets) -
            progressiveTax(cumBefore, tax.incomeTaxBrackets)

        // Asgari ücret gelir vergisi istisnası (kümülatif yöntemle)
        val incomeTax = (if (tax.applyMinimumWageExemption) {
            val minBase = tax.minimumWageGross *
                (BigDecimal.ONE - tax.sgkEmployeeRate - tax.unemploymentEmployeeRate)
            val cumMinBefore = minBase * BigDecimal(work.monthIndex - 1)
            val exemption = progressiveTax(cumMinBefore + minBase, tax.incomeTaxBrackets) -
                progressiveTax(cumMinBefore, tax.incomeTaxBrackets)
            (grossIncomeTax - exemption).coerceAtLeastZero()
        } else {
            grossIncomeTax
        }).setScale(scale, rm)

        val grossStamp = gross * tax.stampTaxRate
        val stampTax = (if (tax.applyMinimumWageExemption) {
            (grossStamp - tax.minimumWageGross * tax.stampTaxRate).coerceAtLeastZero()
        } else {
            grossStamp
        }).setScale(scale, rm)

        val totalDeductions = (sgkEmployee + unemployment + incomeTax + stampTax).setScale(scale, rm)
        val net = (gross - totalDeductions + netAdditions - netDeductions).setScale(scale, rm)

        return PayrollResult(
            gross = gross,
            basePay = base.setScale(scale, rm),
            fazlaSurePay = fazlaSurePay.setScale(scale, rm),
            fazlaCalismaPay = fazlaCalismaPay.setScale(scale, rm),
            holidayPay = holidayPay.setScale(scale, rm),
            sgkEmployee = sgkEmployee,
            unemployment = unemployment,
            incomeTax = incomeTax,
            stampTax = stampTax,
            totalDeductions = totalDeductions,
            netAdditions = netAdditions.setScale(scale, rm),
            netDeductions = netDeductions.setScale(scale, rm),
            net = net
        )
    }

    /** Kümülatif matraha artan oranlı gelir vergisi uygular. */
    private fun progressiveTax(base: BigDecimal, brackets: List<TaxBracket>): BigDecimal {
        if (base <= BigDecimal.ZERO) return BigDecimal.ZERO
        var tax = BigDecimal.ZERO
        var lower = BigDecimal.ZERO
        for (b in brackets) {
            val upper = b.upTo ?: base
            val taxableInBracket = base.min(upper) - lower
            if (taxableInBracket > BigDecimal.ZERO) tax += taxableInBracket * b.rate
            if (b.upTo == null || base <= upper) break
            lower = upper
        }
        return tax
    }

    private fun BigDecimal.coerceAtLeastZero(): BigDecimal =
        if (this < BigDecimal.ZERO) BigDecimal.ZERO else this
}
