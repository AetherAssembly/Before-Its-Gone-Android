package org.aetherassembly.beforeitsgone.data.work

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import org.aetherassembly.beforeitsgone.domain.model.ExpiryStatus
import org.aetherassembly.beforeitsgone.domain.repository.InventoryRepository
import org.aetherassembly.beforeitsgone.domain.repository.SettingsRepository
import org.aetherassembly.beforeitsgone.domain.usecase.calculateExpiryStatus

@HiltWorker
class ExpiryCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val inventoryRepository: InventoryRepository,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settings = settingsRepository.getSettings().first()
        val items = inventoryRepository.getAll().first()
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        items.forEach { item ->
            when (calculateExpiryStatus(item.expiresAt, settings.expiryWarningDays)) {
                ExpiryStatus.EXPIRED -> if (settings.notificationsExpired) {
                    nm.notify(
                        item.id.hashCode(),
                        NotificationCompat.Builder(applicationContext, CHANNEL_EXPIRED)
                            .setSmallIcon(android.R.drawable.ic_dialog_alert)
                            .setContentTitle("Expired: ${item.name}")
                            .setContentText("${item.name} expired. Remove or log it.")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setGroup(GROUP_EXPIRY)
                            .build()
                    )
                }
                ExpiryStatus.EXPIRING_SOON -> if (settings.notificationsExpiring) {
                    nm.notify(
                        item.id.hashCode(),
                        NotificationCompat.Builder(applicationContext, CHANNEL_EXPIRING)
                            .setSmallIcon(android.R.drawable.ic_dialog_info)
                            .setContentTitle("Expiring soon: ${item.name}")
                            .setContentText("${item.name} expires within ${settings.expiryWarningDays} days.")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setGroup(GROUP_EXPIRY)
                            .build()
                    )
                }
                ExpiryStatus.FRESH -> Unit
            }

            // Low-stock check (independent of expiry)
            val threshold = item.depletionThreshold
            if (settings.notificationsLowStock &&
                threshold != null &&
                item.quantity <= threshold
            ) {
                nm.notify(
                    ("low_${item.id}").hashCode(),
                    NotificationCompat.Builder(applicationContext, CHANNEL_LOW_STOCK)
                        .setSmallIcon(android.R.drawable.ic_menu_info_details)
                        .setContentTitle("Low stock: ${item.name}")
                        .setContentText("Only ${item.quantity} left (threshold: $threshold).")
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setGroup(GROUP_STOCK)
                        .build()
                )
            }
        }

        return Result.success()
    }

    companion object {
        const val CHANNEL_EXPIRING = "expiring"
        const val CHANNEL_EXPIRED = "expired"
        const val CHANNEL_LOW_STOCK = "low_stock"
        const val WORK_NAME = "expiry_check_daily"
        private const val GROUP_EXPIRY = "group_expiry"
        private const val GROUP_STOCK = "group_stock"
    }
}
