package com.mesaitakibi.ui.haptics

/** Uygulamadaki semantik haptic olayları. */
enum class HapticEvent {
    TICK,           // hafif dokunuş (liste kaydırma, segment seçimi)
    CLICK,          // standart buton dokunuşu
    HEAVY,          // önemli/uzun basış
    CONFIRM,        // onay
    REJECT,         // hata / geçersiz işlem
    TOGGLE_ON,      // anahtar açık
    TOGGLE_OFF,     // anahtar kapalı
    GESTURE_END,    // sürükleme/işlem bitişi
    CLOCK_IN,       // işe başla (kahraman an)
    CLOCK_OUT,      // işi bitir (kahraman an)
    OVERTIME_ALERT, // mesai eşiği aşıldı
    SUCCESS         // kayıt/işlem başarıyla tamamlandı
}
