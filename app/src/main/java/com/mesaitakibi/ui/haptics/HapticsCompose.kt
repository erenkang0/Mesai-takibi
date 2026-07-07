package com.mesaitakibi.ui.haptics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

/** Uygulama genelinde erişilebilen haptic denetleyici. */
val LocalAppHaptics = staticCompositionLocalOf<AppHaptics> {
    error("AppHaptics sağlanmadı. MesaiApp içinde CompositionLocalProvider ile sağlayın.")
}

@Composable
fun rememberAppHaptics(enabled: Boolean): AppHaptics {
    val view = LocalView.current
    val context = LocalContext.current
    val haptics = remember(view) { AppHaptics(view, context) }
    haptics.enabled = enabled
    return haptics
}
