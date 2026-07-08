package com.mesaitakibi.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mesaitakibi.ui.haptics.HapticEvent
import com.mesaitakibi.ui.haptics.LocalAppHaptics
import com.mesaitakibi.ui.haptics.rememberAppHaptics
import com.mesaitakibi.ui.navigation.BottomTab
import com.mesaitakibi.ui.navigation.Routes
import com.mesaitakibi.ui.notification.rememberNotificationPermissionRequester
import com.mesaitakibi.ui.screens.backup.BackupScreen
import com.mesaitakibi.ui.screens.dashboard.DashboardScreen
import com.mesaitakibi.ui.screens.finance.FinanceScreen
import com.mesaitakibi.ui.screens.holidays.HolidaysScreen
import com.mesaitakibi.ui.screens.leave.LeaveScreen
import com.mesaitakibi.ui.screens.onboarding.OnboardingScreen
import com.mesaitakibi.ui.screens.payroll.PayrollScreen
import com.mesaitakibi.ui.screens.settings.SettingsScreen
import com.mesaitakibi.ui.screens.severance.SeveranceScreen
import com.mesaitakibi.ui.screens.shift.ShiftScreen
import com.mesaitakibi.ui.screens.tax.TaxScreen
import com.mesaitakibi.ui.screens.timetracking.TimeTrackingScreen

@Composable
fun MesaiApp(
    hapticsEnabled: Boolean = true,
    needsOnboarding: Boolean = false
) {
    val haptics = rememberAppHaptics(hapticsEnabled)

    CompositionLocalProvider(LocalAppHaptics provides haptics) {
        if (needsOnboarding) {
            OnboardingScreen(onDone = { /* prefs.onboarded akışı ana ekrana geçirir */ })
        } else {
            MainScaffold()
        }
    }
}

@Composable
private fun MainScaffold() {
    val haptics = LocalAppHaptics.current
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    val requestNotificationPermission = rememberNotificationPermissionRequester()
    LaunchedEffect(Unit) { requestNotificationPermission() }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                BottomTab.entries.forEach { tab ->
                    val selected = currentRoute?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            haptics.perform(HapticEvent.TICK)
                            if (!selected) {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    contentPadding = padding,
                    onOpenPayroll = { navController.navigate(Routes.PAYROLL) },
                    onOpenTimeTracking = { navController.navigate(Routes.TIME_TRACKING) }
                )
            }
            composable(Routes.TIME_TRACKING) { TimeTrackingScreen(contentPadding = padding) }
            composable(Routes.PAYROLL) { PayrollScreen(contentPadding = padding) }
            composable(Routes.FINANCE) { FinanceScreen(contentPadding = padding) }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    contentPadding = padding,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }
            composable(Routes.SHIFTS) { ShiftScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.TAX) { TaxScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.HOLIDAYS) { HolidaysScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.BACKUP) { BackupScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.SEVERANCE) { SeveranceScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.LEAVE) { LeaveScreen(onBack = { navController.popBackStack() }) }
        }
    }
}
