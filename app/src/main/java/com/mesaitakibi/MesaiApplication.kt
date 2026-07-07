package com.mesaitakibi

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.mesaitakibi.data.local.DatabaseSeeder
import com.mesaitakibi.data.repository.TimeTrackingRepository
import com.mesaitakibi.notification.NotificationScheduler
import com.mesaitakibi.notification.WorkSessionController
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.ZoneId
import javax.inject.Inject

@HiltAndroidApp
class MesaiApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var seeder: DatabaseSeeder
    @Inject lateinit var notificationScheduler: NotificationScheduler
    @Inject lateinit var timeTracking: TimeTrackingRepository
    @Inject lateinit var workSessionController: WorkSessionController

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        notificationScheduler.ensureScheduled()
        appScope.launch {
            seeder.seedIfEmpty()
            // Uygulama yeniden başlarken açık bir oturum varsa canlı bildirimi sürdür.
            timeTracking.openSession()?.let { open ->
                val zone = ZoneId.systemDefault()
                val startMillis = open.clockIn.atZone(zone).toInstant().toEpochMilli()
                val overtimeAt = timeTracking.plannedEndForDate(open.date)?.let { end ->
                    java.time.LocalDateTime.of(open.date, end).atZone(zone).toInstant().toEpochMilli()
                }
                workSessionController.start(startMillis, overtimeAt)
            }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
