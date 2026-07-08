package com.mesaitakibi.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Tek bir tutarlı boşluk (spacing) ölçeği. 4'ün katları temel alınır; tüm ekranlarda
 * aynı ritim için buradaki değerler kullanılır — sihirli sayılardan kaçınılır.
 */
object Spacing {
    val none = 0.dp
    val xxs = 2.dp
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val huge = 40.dp

    /** Ekran kenar boşluğu (yatay). */
    val screenH = 20.dp
    /** Ekran üst boşluğu. */
    val screenTop = 16.dp
    /** Kart iç boşluğu. */
    val card = 20.dp
    /** Kartlar/bölümler arası dikey boşluk. */
    val section = 14.dp
    /** Liste öğeleri arası boşluk. */
    val item = 12.dp
}
