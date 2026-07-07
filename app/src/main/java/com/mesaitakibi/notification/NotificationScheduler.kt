package com.mesaitakibi.notification

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Uygulama açılışında bildirim kanallarını hazırlar. */
@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun ensureScheduled() {
        NotificationHelper.ensureChannels(context)
    }
}
