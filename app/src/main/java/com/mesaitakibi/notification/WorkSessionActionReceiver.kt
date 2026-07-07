package com.mesaitakibi.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mesaitakibi.data.repository.TimeTrackingRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Bildirimdeki "İşi Bitir" aksiyonunu işler: çıkış yapar ve servisi durdurur. */
@AndroidEntryPoint
class WorkSessionActionReceiver : BroadcastReceiver() {

    @Inject lateinit var timeTracking: TimeTrackingRepository
    @Inject lateinit var controller: WorkSessionController

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_CLOCK_OUT) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                timeTracking.clockOut()
                controller.stop()
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_CLOCK_OUT = "com.mesaitakibi.action.CLOCK_OUT"
    }
}
