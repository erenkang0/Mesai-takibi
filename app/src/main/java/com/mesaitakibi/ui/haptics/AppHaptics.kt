package com.mesaitakibi.ui.haptics

import android.content.Context
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Uygulamanın haptic (dokunsal geri bildirim) denetleyicisi. Android 16 (API 36) tabanı
 * varsayıldığından tüm modern API'ler koşulsuz kullanılır; yalnızca donanımın hangi
 * titreşim primitiflerini desteklediği çalışma anında kontrol edilir.
 *
 * Katmanlı strateji (Android resmî öneri sırası):
 *  1. Basit etkileşimler → [View.performHapticFeedback] + [HapticFeedbackConstants]
 *     (sistemle tutarlı, en geniş destek).
 *  2. "Kahraman" anlar (işe başla/bitir, mesai uyarısı) → [VibrationEffect.Composition]
 *     primitifleri (QUICK_RISE, CLICK, THUD...) — donanım desteği kontrol edilerek;
 *     insan dokunuşuna en yakın, zengin ve pürüzsüz his.
 *  3. Yedek → [VibrationEffect.createPredefined] (EFFECT_CLICK/HEAVY_CLICK/DOUBLE_CLICK).
 *
 * Eski "one-shot" (createOneShot) titreşimlerinden kaçınılır; ucuz aktüatörlerde
 * "vızıltı" gibi hissedilir.
 */
class AppHaptics(
    private val view: View,
    context: Context
) {
    var enabled: Boolean = true

    private val vibrator: Vibrator? =
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator

    private val hasVibrator: Boolean = vibrator?.hasVibrator() == true

    private val touchAttributes: VibrationAttributes =
        VibrationAttributes.Builder().setUsage(VibrationAttributes.USAGE_TOUCH).build()

    fun perform(event: HapticEvent) {
        if (!enabled) return
        when (event) {
            HapticEvent.TICK -> viewHaptic(HapticFeedbackConstants.SEGMENT_TICK)
            HapticEvent.CLICK -> viewHaptic(HapticFeedbackConstants.KEYBOARD_TAP)
            HapticEvent.HEAVY -> viewHaptic(HapticFeedbackConstants.LONG_PRESS)
            HapticEvent.CONFIRM -> viewHaptic(HapticFeedbackConstants.CONFIRM)
            HapticEvent.REJECT -> if (!richReject()) viewHaptic(HapticFeedbackConstants.REJECT)
            HapticEvent.TOGGLE_ON -> viewHaptic(HapticFeedbackConstants.TOGGLE_ON)
            HapticEvent.TOGGLE_OFF -> viewHaptic(HapticFeedbackConstants.TOGGLE_OFF)
            HapticEvent.GESTURE_END -> viewHaptic(HapticFeedbackConstants.GESTURE_END)
            HapticEvent.CLOCK_IN -> if (!richClockIn()) viewHaptic(HapticFeedbackConstants.CONFIRM)
            HapticEvent.CLOCK_OUT -> if (!richClockOut()) viewHaptic(HapticFeedbackConstants.KEYBOARD_TAP)
            HapticEvent.OVERTIME_ALERT -> if (!richOvertime()) viewHaptic(HapticFeedbackConstants.REJECT)
            HapticEvent.SUCCESS -> if (!richSuccess()) viewHaptic(HapticFeedbackConstants.CONFIRM)
        }
    }

    private fun viewHaptic(constant: Int) {
        view.performHapticFeedback(constant, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING)
    }

    // --- Zengin composition haptics (insan dokunuşu hissi) ---

    /** İşe başla: yumuşak yükseliş + net tık → onaylayıcı "başladı" hissi. */
    private fun richClockIn(): Boolean = composition(
        VibrationEffect.Composition.PRIMITIVE_QUICK_RISE to 0.6f,
        VibrationEffect.Composition.PRIMITIVE_CLICK to 1.0f
    ) || predefined(VibrationEffect.EFFECT_HEAVY_CLICK)

    /** İşi bitir: net tık + hafif düşüş → "tamamlandı" hissi. */
    private fun richClockOut(): Boolean = composition(
        VibrationEffect.Composition.PRIMITIVE_CLICK to 1.0f,
        VibrationEffect.Composition.PRIMITIVE_QUICK_FALL to 0.7f
    ) || predefined(VibrationEffect.EFFECT_CLICK)

    /** Mesai uyarısı: tok vuruş + net tık → dikkat çekici ama rahatsız etmeyen. */
    private fun richOvertime(): Boolean = composition(
        VibrationEffect.Composition.PRIMITIVE_THUD to 0.8f,
        VibrationEffect.Composition.PRIMITIVE_CLICK to 1.0f
    ) || predefined(VibrationEffect.EFFECT_DOUBLE_CLICK)

    /** Başarı: hafif yükselen çift tık. */
    private fun richSuccess(): Boolean = composition(
        VibrationEffect.Composition.PRIMITIVE_SLOW_RISE to 0.5f,
        VibrationEffect.Composition.PRIMITIVE_CLICK to 0.9f
    ) || predefined(VibrationEffect.EFFECT_HEAVY_CLICK)

    private fun richReject(): Boolean = composition(
        VibrationEffect.Composition.PRIMITIVE_LOW_TICK to 0.9f,
        VibrationEffect.Composition.PRIMITIVE_CLICK to 0.6f
    )

    /**
     * Verilen primitiflerle ardışık bir composition oynatır. Donanım tüm primitifleri
     * desteklemiyorsa hiçbir şey yapmaz ve false döner (fallback tetiklenir).
     */
    private fun composition(vararg primitives: Pair<Int, Float>): Boolean {
        val vib = vibrator ?: return false
        if (!hasVibrator) return false
        val ids = primitives.map { it.first }.toIntArray()
        if (!vib.areAllPrimitivesSupported(*ids)) return false

        val composition = VibrationEffect.startComposition()
        primitives.forEach { (id, scale) -> composition.addPrimitive(id, scale) }
        vib.vibrate(composition.compose(), touchAttributes)
        return true
    }

    private fun predefined(effectId: Int): Boolean {
        val vib = vibrator ?: return false
        if (!hasVibrator) return false
        return runCatching {
            vib.vibrate(VibrationEffect.createPredefined(effectId), touchAttributes)
            true
        }.getOrDefault(false)
    }
}
