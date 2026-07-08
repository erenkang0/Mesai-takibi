package com.mesaitakibi.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mesaitakibi.data.prefs.ThemeMode

private val DarkColors = darkColorScheme(
    primary = Accent,
    onPrimary = Color(0xFF091018),
    primaryContainer = AccentContainerDark,
    onPrimaryContainer = Color(0xFFC7D4FF),
    secondary = DarkOnSurfaceVariant,
    onSecondary = DarkBackground,
    tertiary = Mint,
    onTertiary = Color(0xFF06231D),
    tertiaryContainer = MintContainerDark,
    onTertiaryContainer = Color(0xFFB6F0E2),
    error = Coral,
    onError = Color(0xFF2A0A0A),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutline
)

private val LightColors = lightColorScheme(
    primary = AccentLight,
    onPrimary = Color.White,
    primaryContainer = AccentContainerLight,
    onPrimaryContainer = Color(0xFF0A245A),
    secondary = LightOnSurfaceVariant,
    onSecondary = Color.White,
    tertiary = MintLight,
    onTertiary = Color.White,
    tertiaryContainer = MintContainerLight,
    onTertiaryContainer = Color(0xFF06231D),
    error = Color(0xFFD64545),
    onError = Color.White,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = Color(0xFFF0F2F5),
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutline
)

/** Özel köşe ölçeği. */
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * Uygulama teması. Varsayılan: KARANLIK + **özel (bespoke) palet**.
 * [dynamicColor] açıksa (Ayarlar'dan) Android 12+ üzerinde Material You renkleri kullanılır;
 * varsayılan olarak kapalıdır ki tasarlanan özel palet öne çıksın.
 */
@Composable
fun MesaiTakibiTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
