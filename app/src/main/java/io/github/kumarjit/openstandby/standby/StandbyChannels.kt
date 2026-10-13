package io.github.kumarjit.openstandby.standby

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat

object StandbyChannels {
    const val MONITOR = "standby_monitor"
    const val LAUNCH = "standby_launch"

    const val MONITOR_NOTIFICATION_ID = 1
    const val LAUNCH_NOTIFICATION_ID = 2

    fun ensure(context: Context) {
        val manager = NotificationManagerCompat.from(context)
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(MONITOR, NotificationManagerCompat.IMPORTANCE_MIN)
                .setName("Standby monitor")
                .setDescription("Keeps standby ready to open while charging")
                .setShowBadge(false)
                .build(),
        )
        // Full-screen intents need a high-importance channel; keep it silent.
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(LAUNCH, NotificationManagerCompat.IMPORTANCE_HIGH)
                .setName("Standby launch")
                .setDescription("Opens the standby screen when the phone is charging")
                .setSound(null, null)
                .setVibrationEnabled(false)
                .setShowBadge(false)
                .build(),
        )
    }
}
