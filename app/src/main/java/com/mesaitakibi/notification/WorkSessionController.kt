package com.mesaitakibi.notification

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Aktif çalışma oturumu ön plan servisini başlatır/durdurur. */
@Singleton
class WorkSessionController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun start(startMillis: Long, overtimeAtMillis: Long?) {
        val intent = Intent(context, WorkSessionService::class.java).apply {
            putExtra(WorkSessionService.EXTRA_START_MILLIS, startMillis)
            putExtra(WorkSessionService.EXTRA_OVERTIME_AT_MILLIS, overtimeAtMillis ?: -1L)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun stop() {
        context.stopService(Intent(context, WorkSessionService::class.java))
        NotificationManagerCompat.from(context)
            .cancel(NotificationHelper.WORK_SESSION_NOTIF_ID)
    }
}
