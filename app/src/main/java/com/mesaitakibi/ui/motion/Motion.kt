package com.mesaitakibi.ui.motion

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import com.mesaitakibi.ui.haptics.AppHaptics
import com.mesaitakibi.ui.haptics.HapticEvent

/**
 * Material 3 hareket fiziğine (yay/spring) dayalı ortak animasyon spesifikasyonları.
 * - [spatial]: konum/boyut geçişleri için pürüzsüz, hafif yaylanan.
 * - [expressive]: kahraman anlar için daha canlı, belirgin yaylanan.
 * - [effects]: renk/alfa gibi görsel efektler için yaylanmasız.
 */
object Motion {
    fun <T> spatial() = spring<T>(
        dampingRatio = 0.9f,
        stiffness = Spring.StiffnessMediumLow
    )

    fun <T> expressive() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    fun <T> effects() = spring<T>(
        dampingRatio = 1f,
        stiffness = Spring.StiffnessMedium
    )

    const val PRESS_SCALE = 0.96f
}

/**
 * Basıldığında yumuşakça küçülen (yay animasyonu) ve haptic geri bildirim veren
 * tıklama alanı. İnsan dokunuşu hissi için basış/bırakış anında görsel + dokunsal tepki.
 */
fun Modifier.pressableClick(
    haptics: AppHaptics,
    event: HapticEvent = HapticEvent.CLICK,
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) Motion.PRESS_SCALE else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "pressScale"
    )
    this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = ripple(),
            enabled = enabled
        ) {
            haptics.perform(event)
            onClick()
        }
}
