package com.mesaitakibi.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/** Uygulama rotaları. */
object Routes {
    const val DASHBOARD = "dashboard"
    const val TIME_TRACKING = "timetracking"
    const val PAYROLL = "payroll"
    const val FINANCE = "finance"
    const val SETTINGS = "settings"
    const val SHIFTS = "shifts"
    const val TAX = "tax"
    const val HOLIDAYS = "holidays"
    const val BACKUP = "backup"
    const val SEVERANCE = "severance"
    const val LEAVE = "leave"
}

/** Alt gezinme çubuğundaki sekmeler. */
enum class BottomTab(val route: String, val label: String, val icon: ImageVector) {
    DASHBOARD(Routes.DASHBOARD, "Ana Sayfa", Icons.Filled.Home),
    TIME(Routes.TIME_TRACKING, "Puantaj", Icons.Filled.Schedule),
    PAYROLL(Routes.PAYROLL, "Bordro", Icons.Filled.Payments),
    FINANCE(Routes.FINANCE, "Finans", Icons.Filled.AccountBalanceWallet),
    SETTINGS(Routes.SETTINGS, "Ayarlar", Icons.Filled.Settings)
}
