package com.mesaitakibi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Bordroya eklenen kalem (prim, yol/yemek yardımı, avans kesintisi...).
 * [kind]: EARNING_TAXABLE (vergiye tabi, brüte eklenir), EARNING_NET (istisna, nete eklenir),
 * DEDUCTION_NET (netten düşülür).
 */
@Entity(tableName = "payroll_adjustments")
data class PayrollAdjustmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val year: Int,
    val month: Int,
    val label: String,
    val amount: String,
    val kind: String
) {
    companion object {
        const val EARNING_TAXABLE = "EARNING_TAXABLE"
        const val EARNING_NET = "EARNING_NET"
        const val DEDUCTION_NET = "DEDUCTION_NET"
    }
}
