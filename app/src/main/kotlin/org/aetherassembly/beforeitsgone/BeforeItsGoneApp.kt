package org.aetherassembly.beforeitsgone

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import org.aetherassembly.beforeitsgone.data.work.ExpiryCheckWorker
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class BeforeItsGoneApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        scheduleExpiryCheck()
    }

    private fun createNotificationChannels() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        listOf(
            NotificationChannel(
                ExpiryCheckWorker.CHANNEL_EXPIRING,
                "Expiring soon",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Items nearing their expiry date" },
            NotificationChannel(
                ExpiryCheckWorker.CHANNEL_EXPIRED,
                "Expired items",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Items that have expired" },
            NotificationChannel(
                ExpiryCheckWorker.CHANNEL_LOW_STOCK,
                "Low stock",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Items below their low-stock threshold" }
        ).forEach(nm::createNotificationChannel)
    }

    private fun scheduleExpiryCheck() {
        val initialDelay = millisUntilNextMorning()
        val request = PeriodicWorkRequestBuilder<ExpiryCheckWorker>(1, TimeUnit.DAYS, 1, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            ExpiryCheckWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun millisUntilNextMorning(targetHour: Int = 8): Long {
        val now = LocalDateTime.now()
        var next = now.withHour(targetHour).withMinute(0).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return ChronoUnit.MILLIS.between(now, next)
    }
}
