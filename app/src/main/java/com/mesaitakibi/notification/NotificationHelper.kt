package com.mesaitakibi.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.mesaitakibi.MainActivity
import com.mesaitakibi.R

/**
 * Bildirim kanalları ve bildirim oluşturucular.
 *
 * Aktif çalışma oturumu bildirimi, Android 16 "Live Updates" (promoted ongoing)
 * olarak işaretlenir ([NotificationCompat.Builder.setRequestPromotedOngoing]).
 * Bu sayede Samsung One UI 7 "Now Bar"da, kilit ekranında ve durum çubuğu çipinde
 * canlı kronometreyle görünür.
 */
object NotificationHelper {

    const val CHANNEL_WORK_SESSION = "work_session"
    const val CHANNEL_ALERTS = "alerts"

    const val WORK_SESSION_NOTIF_ID = 1001
    const val OVERTIME_NOTIF_ID = 1002
    const val REMINDER_NOTIF_ID = 1003

    fun ensureChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        val session = NotificationChannel(
            CHANNEL_WORK_SESSION,
            "Aktif çalışma oturumu",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Çalışırken canlı süre ve mesai durumu (Now Bar)."
            setShowBadge(false)
        }

        val alerts = NotificationChannel(
            CHANNEL_ALERTS,
            "Uyarılar ve hatırlatmalar",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Mesai uyarıları ve çıkış hatırlatmaları."
        }

        manager.createNotificationChannel(session)
        manager.createNotificationChannel(alerts)
    }

    private fun contentIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun clockOutAction(context: Context): NotificationCompat.Action {
        val intent = Intent(context, WorkSessionActionReceiver::class.java).apply {
            action = WorkSessionActionReceiver.ACTION_CLOCK_OUT
        }
        val pending = PendingIntent.getBroadcast(
            context, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action.Builder(
            R.drawable.ic_stat_work, "İşi Bitir", pending
        ).build()
    }

    /**
     * Aktif çalışma oturumu için canlı (promoted ongoing) bildirim.
     * @param startMillis oturum başlangıç zamanı (kronometre bundan sayar)
     * @param overtime normal bitiş saati geçildiyse true (mesai durumu)
     */
    fun workSessionNotification(
        context: Context,
        startMillis: Long,
        overtime: Boolean
    ): Notification {
        val title = if (overtime) "Mesai yapıyorsun ⚡" else "Çalışıyorsun"
        val text = if (overtime) "Normal mesai bitti — fazla çalışma sürüyor" else "Süre işliyor"

        return NotificationCompat.Builder(context, CHANNEL_WORK_SESSION)
            .setSmallIcon(R.drawable.ic_stat_work)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setUsesChronometer(true)
            .setWhen(startMillis)
            .setShowWhen(true)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setColorized(true)
            .setColor(if (overtime) 0xFFF9A825.toInt() else 0xFF1565C0.toInt())
            .setContentIntent(contentIntent(context))
            .addAction(clockOutAction(context))
            .setRequestPromotedOngoing(true)
            .build()
    }

    fun overtimeAlertNotification(context: Context): Notification =
        NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_stat_work)
            .setContentTitle("Mesai başladı ⚡")
            .setContentText("Normal çalışma saatini geçtin, artık mesai sayılıyor.")
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentIntent(context))
            .build()

    fun reminderNotification(context: Context, message: String): Notification =
        NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_stat_work)
            .setContentTitle("Mesai Takibi")
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(contentIntent(context))
            .build()
}
