package com.mesaitakibi.domain.payroll

import java.math.BigDecimal

/** Gelir vergisi dilimi. [upTo] null ise üst sınırsızdır (son dilim). Kümülatif matraha uygulanır. */
data class TaxBracket(val upTo: BigDecimal?, val rate: BigDecimal)

/**
 * Yıllık değişen SGK ve vergi parametreleri.
 *
 * ÖNEMLİ: Asgari ücret, SGK tavanı ve gelir vergisi dilimleri her yıl değişir.
 * Bu değerler uygulama içinde (Ayarlar > Vergi/SGK parametreleri) düzenlenebilir olmalı
 * ve resmî kaynaklardan (SGK, GİB) doğrulanmalıdır.
 */
data class TaxParameters(
    val year: Int,
    val minimumWageGross: BigDecimal,
    val sgkCeiling: BigDecimal,
    val sgkEmployeeRate: BigDecimal,
    val unemploymentEmployeeRate: BigDecimal,
    val stampTaxRate: BigDecimal,
    val incomeTaxBrackets: List<TaxBracket>,
    val applyMinimumWageExemption: Boolean = true
) {
    companion object {
        /**
         * 2026 varsayılan değerleri (kamuya açık bordro rehberlerine göre; resmî kaynaktan doğrulayın).
         * Brüt asgari ücret 33.030,00 TL — net 28.075,50 TL.
         */
        fun default2026(): TaxParameters = TaxParameters(
            year = 2026,
            minimumWageGross = BigDecimal("33030.00"),
            sgkCeiling = BigDecimal("297270.00"),
            sgkEmployeeRate = BigDecimal("0.14"),
            unemploymentEmployeeRate = BigDecimal("0.01"),
            stampTaxRate = BigDecimal("0.00759"),
            incomeTaxBrackets = listOf(
                TaxBracket(BigDecimal("190000"), BigDecimal("0.15")),
                TaxBracket(BigDecimal("400000"), BigDecimal("0.20")),
                TaxBracket(BigDecimal("1500000"), BigDecimal("0.27")),
                TaxBracket(BigDecimal("5000000"), BigDecimal("0.35")),
                TaxBracket(null, BigDecimal("0.40"))
            )
        )
    }
}
