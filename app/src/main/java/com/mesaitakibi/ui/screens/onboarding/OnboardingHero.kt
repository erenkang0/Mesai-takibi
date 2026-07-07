package com.mesaitakibi.ui.screens.onboarding

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin

/**
 * Premium, tamamen kod ile çizilmiş animasyonlu vektör hero'su: dönen akrep/yelkovanlı
 * saat (mesai), nabız gibi atan hâle halkaları ve süzülen madeni paralar (maaş).
 * Harici bağımlılık/asset gerektirmez; sonsuz (infinite) animasyonlarla canlı durur.
 */
@Composable
fun OnboardingHero(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "hero")

    val secondRot by transition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "second"
    )
    val hourRot by transition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(54000, easing = LinearEasing)),
        label = "hour"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.9f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(1900, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val floatY by transition.animateFloat(
        initialValue = -10f, targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Reverse),
        label = "floatY"
    )

    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val secondary = MaterialTheme.colorScheme.secondary

    Canvas(modifier) {
        val c = center
        val r = size.minDimension * 0.30f

        // Hâle halkaları (nabız)
        drawCircle(primary.copy(alpha = 0.14f), radius = r * 1.7f * pulse, center = c)
        drawCircle(tertiary.copy(alpha = 0.10f), radius = r * 2.2f * pulse, center = c)

        // Süzülen paralar (maaş)
        val coinR = r * 0.30f
        val coins = listOf(
            Triple(c.x - r * 1.85f, c.y - r * 0.1f + floatY, tertiary),
            Triple(c.x + r * 1.9f, c.y + r * 0.2f - floatY, secondary),
            Triple(c.x + r * 1.25f, c.y - r * 1.5f + floatY * 0.6f, tertiary)
        )
        coins.forEach { (x, y, col) ->
            val p = Offset(x, y)
            drawCircle(col.copy(alpha = 0.92f), radius = coinR, center = p)
            drawCircle(col, radius = coinR, center = p, style = Stroke(width = coinR * 0.16f))
            drawCircle(col.copy(alpha = 0.35f), radius = coinR * 0.5f, center = p, style = Stroke(width = coinR * 0.12f))
        }

        // Saat kadranı
        drawCircle(primary.copy(alpha = 0.10f), radius = r, center = c)
        drawCircle(primary, radius = r, center = c, style = Stroke(width = r * 0.09f))

        // Saat çizgileri
        for (i in 0 until 12) {
            val a = Math.toRadians((i * 30).toDouble())
            val outer = Offset(c.x + (r * 0.95f) * sin(a).toFloat(), c.y - (r * 0.95f) * cos(a).toFloat())
            val inner = Offset(c.x + (r * 0.80f) * sin(a).toFloat(), c.y - (r * 0.80f) * cos(a).toFloat())
            drawLine(primary.copy(alpha = 0.55f), inner, outer, strokeWidth = r * 0.03f, cap = StrokeCap.Round)
        }

        // Akrep (yavaş)
        rotate(hourRot, pivot = c) {
            drawLine(primary, c, Offset(c.x, c.y - r * 0.48f), strokeWidth = r * 0.075f, cap = StrokeCap.Round)
        }
        // Yelkovan/saniye (hızlı)
        rotate(secondRot, pivot = c) {
            drawLine(tertiary, c, Offset(c.x, c.y - r * 0.74f), strokeWidth = r * 0.045f, cap = StrokeCap.Round)
        }
        drawCircle(primary, radius = r * 0.07f, center = c)
    }
}

/** Yavaşça hareket eden, tema renklerinden türeyen animasyonlu "aurora" arka planı. */
@Composable
fun AuroraBackground(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "aurora")
    val p1 by transition.animateFloat(
        0f, 1f, infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse), label = "p1"
    )
    val p2 by transition.animateFloat(
        0f, 1f, infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Reverse), label = "p2"
    )
    val c1 = MaterialTheme.colorScheme.primary
    val c2 = MaterialTheme.colorScheme.tertiary

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val center1 = Offset(w * (0.22f + 0.18f * p1), h * (0.14f + 0.10f * p2))
        val center2 = Offset(w * (0.84f - 0.18f * p2), h * (0.78f - 0.10f * p1))
        drawCircle(
            brush = Brush.radialGradient(
                listOf(c1.copy(alpha = 0.22f), c1.copy(alpha = 0f)),
                center = center1, radius = w * 0.75f
            ),
            radius = w * 0.75f, center = center1
        )
        drawCircle(
            brush = Brush.radialGradient(
                listOf(c2.copy(alpha = 0.18f), c2.copy(alpha = 0f)),
                center = center2, radius = w * 0.85f
            ),
            radius = w * 0.85f, center = center2
        )
    }
}
