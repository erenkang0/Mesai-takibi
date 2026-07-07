package com.mesaitakibi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Yıla göre SGK/vergi parametreleri. Gelir vergisi dilimleri JSON metin olarak saklanır.
 * Ayarlar ekranından düzenlenebilir; resmî değerler SGK/GİB'den doğrulanmalıdır.
 */
@Entity(tableName = "tax_parameters")
data class TaxParametersEntity(
    @PrimaryKey val year: Int,
    val minimumWageGross: String,
    val sgkCeiling: String,
    val sgkEmployeeRate: String,
    val unemploymentEmployeeRate: String,
    val stampTaxRate: String,
    val incomeTaxBracketsJson: String,
    val applyMinimumWageExemption: Boolean = true
)
