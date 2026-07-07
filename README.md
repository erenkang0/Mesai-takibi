# Mesai Takibi

Türkiye'de çalışan **tek bir kişinin** kendi çalışma zamanını kaydettiği, **mesai
yaptığında bunu otomatik algılayan** ve **Türk iş hukukuna uygun maaş/bordro** ile
**kişisel finans** takibi yapan bir **Android** uygulaması.

> Kotlin + Jetpack Compose · Material You (dinamik renk) · varsayılan karanlık tema ·
> zengin haptic geri bildirim · Android 16 "Live Updates" (Samsung **Now Bar**) canlı bildirimi.

---

## Öne çıkan özellikler

- **İşe başla / işi bitir**: Ana ekrandaki büyük butonla tek dokunuşta giriş-çıkış,
  canlı geçen süre sayacı ve güçlü dokunsal geri bildirim.
- **Mesai algılama (çift yöntem)**:
  - *Plan bazlı*: Onboarding'de girdiğin **normal çalışma saatlerinden** sonrası anında
    "mesai" olarak işaretlenir ve bildirimle uyarılırsın.
  - *Haftalık yasal kural (4857 sayılı İş Kanunu)*: Haftalık 45 saati aşan kısım **fazla
    çalışma (×1,50)**, sözleşme saati ile 45 arası **fazla sürelerle çalışma (×1,25)**.
    Gece çalışması ve resmî tatil çalışması ayrıca izlenir.
- **Bordro (brüt → net)**: SGK işçi payı (%14), işsizlik (%1), artan oranlı **gelir
  vergisi** (kümülatif matrah), **damga vergisi** ve **asgari ücret vergi istisnası**.
- **Kişisel finans**: Gelir-gider işlemleri, aylık özet ve kategori dağılımı.
- **Samsung Now Bar / Canlı bildirim**: Çalışırken kronometreli, mesai durumuna göre
  renk değiştiren "Live Update" (promoted ongoing) bildirimi.
- **Kapsamlı veri yönetimi**: Tüm verinin JSON **yedeği**, **geri yükleme**, **CSV**
  dışa aktarma ve **Paylaş** menüsüyle Google Drive'a (veya herhangi bir uygulamaya)
  gönderme. *(Google Drive API kullanılmaz; paylaşım Android'in kendi menüsüyle yapılır.)*
- **Kişiselleştirme**: İlk açılışta onboarding ile ad, ücret, haftalık saat, normal
  çalışma saatleri ve çalışma günleri sorulur; uygulama sana göre şekillenir.

---

## Mimari

Çok modüllü Gradle projesi:

```
:domain   → saf Kotlin/JVM. İş mantığı (mesai + bordro + finans). Android'e bağımlı DEĞİL.
:app      → Android (Compose, Room, Hilt, WorkManager, bildirimler, UI).
```

- **:domain** Android'den bağımsız olduğu için iş mantığı **birim testleriyle** doğrulanır.
- **:app** MVVM + katmanlı yapı: `data/` (Room, repository, backup, prefs), `notification/`
  (Now Bar canlı bildirim), `ui/` (Compose ekranlar, tema, haptic, motion).

### Çekirdek motorlar (`:domain`)
- `overtime/OvertimeDetector` — haftalık mesai analizi (fazla çalışma / fazla süre / gece / tatil).
- `payroll/PayrollCalculator` + `payroll/TaxParameters` — brüt→net bordro.
- `finance/BudgetCalculator` — aylık gelir-gider özeti.

---

## Türk iş hukuku & 2026 parametreleri

Uygulama, **2026** yılı varsayılan değerleriyle gelir (Ayarlar > Vergi ve SGK'dan
düzenlenebilir — değerler her yıl değişir, resmî kaynaktan doğrulayın):

| Parametre | 2026 değeri |
|---|---|
| Brüt asgari ücret | 33.030,00 ₺ (net 28.075,50 ₺) |
| SGK tavanı | 297.270,00 ₺ |
| SGK işçi payı | %14 |
| İşsizlik işçi payı | %1 |
| Damga vergisi | binde 7,59 |
| Gelir vergisi dilimleri | 190.000 / 400.000 / 1.500.000 / 5.000.000 ₺ eşikleriyle %15–%40 |

Kaynaklar: kamuya açık 2026 bordro rehberleri (ör. Kolay İK, CottGroup, MuhasebeTR).
Resmî değerler için **SGK** ve **GİB**'i esas alın.

---

## Haptic (dokunsal geri bildirim) tasarımı

Android'in resmî öneri sırasına göre **katmanlı** bir yaklaşım (`ui/haptics/AppHaptics.kt`):

1. **Basit etkileşimler** → `View.performHapticFeedback` + `HapticFeedbackConstants`
   (izin gerektirmez, sistemle tutarlı).
2. **"Kahraman" anlar** (işe başla/bitir, mesai uyarısı) → `VibrationEffect.Composition`
   primitifleri (`QUICK_RISE`, `CLICK`, `THUD`, `QUICK_FALL`...) — **donanım desteği
   çalışma anında kontrol edilir**; insan dokunuşuna en yakın, pürüzsüz his.
3. **Yedek** → `VibrationEffect.createPredefined` (CLICK / HEAVY_CLICK / DOUBLE_CLICK).

Eski `createOneShot` titreşimlerinden bilinçli olarak kaçınılır. Haptic, Ayarlar'dan
kapatılabilir.

## Animasyon

Material 3 hareket fiziği (yay/spring) temel alınır (`ui/motion/Motion.kt`): basılınca
yumuşakça küçülen butonlar, `AnimatedContent` ile içerik geçişleri, renk animasyonları.

## Samsung Now Bar (Android 16 Live Updates)

Aktif çalışma oturumu, bir **ön plan servisi** (`notification/WorkSessionService.kt`)
tarafından **promoted ongoing** ("Live Update") bildirimi olarak gösterilir
(`NotificationCompat.Builder.setRequestPromotedOngoing`). Kronometre canlı işler, normal
bitiş saati geçilince bildirim "mesai" durumuna geçer. Samsung One UI 7 bunu **Now Bar**'da,
kilit ekranında ve durum çubuğu çipinde canlı gösterir. Bildirimdeki **"İşi Bitir"** aksiyonu
uygulamayı açmadan çıkış yaptırır.

---

## Gereksinimler & derleme

- **Android Studio** (güncel sürüm), **JDK 17+**
- **minSdk = 36 (Android 16)**, compileSdk/targetSdk 36
- Android Gradle Plugin 8.11.1, Kotlin 2.0.21, Gradle 8.14.3

```bash
# Domain (saf Kotlin) birim testleri:
./gradlew :domain:test

# Uygulamayı derle:
./gradlew :app:assembleDebug
```

> Not: `minSdk 36` yalnızca Android 16 ve üzeri cihazlarda kuruluma izin verir. Daha
> geniş cihaz desteği isterseniz `app/build.gradle.kts` içinde `minSdk` değerini
> düşürebilirsiniz (bazı en yeni haptic/bildirim özellikleri için sürüm kontrolü gerekebilir).

### Doğrulama
`:domain` modülündeki iş mantığı JUnit testleriyle doğrulanmıştır; örneğin:
- Asgari ücret **33.030 ₺ → net 28.075,50 ₺**
- 40 saatlik sözleşmede 50 saat çalışma → 5 sa fazla süre + 5 sa fazla çalışma

---

## Yol haritası (sonraki adımlar)
- Bordro PDF çıktısı ve paylaşımı
- Grafiklerin zenginleştirilmesi (aylık trend, kategori pasta grafiği)
- Dini bayram tarihlerinin otomatik güncellenmesi
- Vergi dilimlerinin uygulama içi düzenleyicisi
