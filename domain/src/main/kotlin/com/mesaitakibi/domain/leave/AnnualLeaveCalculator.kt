package com.mesaitakibi.domain.leave

/**
 * Yıllık ücretli izin hakkı hesaplayıcı (4857 sayılı İş Kanunu md. 53).
 *
 * - 1–5 yıl (5 dahil): 14 gün
 * - 5–15 yıl (arası): 20 gün
 * - 15 yıl ve üzeri: 26 gün
 * - 18 yaşından küçük veya 50 yaşından büyük işçilere en az 20 gün.
 * - İzne hak kazanmak için en az 1 yıl çalışma gerekir.
 */
class AnnualLeaveCalculator {

    fun entitlementDays(serviceYears: Int, age: Int): Int {
        if (serviceYears < 1) return 0
        val base = when {
            serviceYears <= 5 -> 14
            serviceYears < 15 -> 20
            else -> 26
        }
        return if (age < 18 || age >= 50) maxOf(base, 20) else base
    }
}
