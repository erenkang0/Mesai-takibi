package com.mesaitakibi.notification

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Aktif çalışma oturumu boyunca çalışan ön plan (foreground) servisi. Kronometreli
 * "Live Update" bildirimini gösterir; normal bitiş saati geçilince bildirimi "mesai"
 * durumuna günceller ve bir uyarı bildirimi yollar.
 */
class WorkSessionService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        NotificationHelper.ensureChannels(this)
        val startMillis = intent?.getLongExtra(EXTRA_START_MILLIS, System.currentTimeMillis())
            ?: System.currentTimeMillis()
        val overtimeAtMillis = intent?.getLongExtra(EXTRA_OVERTIME_AT_MILLIS, -1L) ?: -1L
        val alreadyOvertime = overtimeAtMillis in 0 until System.currentTimeMillis()

        startForeground(
            NotificationHelper.WORK_SESSION_NOTIF_ID,
            NotificationHelper.workSessionNotification(this, startMillis, alreadyOvertime),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )

        if (!alreadyOvertime && overtimeAtMillis > 0) {
            scope.launch {
                val wait = overtimeAtMillis - System.currentTimeMillis()
                if (wait > 0) delay(wait)
                switchToOvertime(startMillis)
            }
        }

        return START_STICKY
    }

    private fun switchToOvertime(startMillis: Long) {
        val manager = NotificationManagerCompat.from(this)
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) return
        manager.notify(
            NotificationHelper.WORK_SESSION_NOTIF_ID,
            NotificationHelper.workSessionNotification(this, startMillis, overtime = true)
        )
        manager.notify(
            NotificationHelper.OVERTIME_NOTIF_ID,
            NotificationHelper.overtimeAlertNotification(this)
        )
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_STOP = "com.mesaitakibi.action.STOP_WORK_SESSION"
        const val EXTRA_START_MILLIS = "extra_start_millis"
        const val EXTRA_OVERTIME_AT_MILLIS = "extra_overtime_at_millis"
    }
}
