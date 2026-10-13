package io.github.kumarjit.openstandby.ui.settings

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.net.toUri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import io.github.kumarjit.openstandby.notifications.StandbyNotificationListener

data class PermissionStatus(
    val notificationAccess: Boolean,
    val calendar: Boolean,
    val postNotifications: Boolean,
    val fullScreenIntent: Boolean,
) {
    companion object {
        fun read(context: Context): PermissionStatus = PermissionStatus(
            notificationAccess = StandbyNotificationListener.isEnabled(context),
            calendar = context.granted(Manifest.permission.READ_CALENDAR),
            postNotifications = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                context.granted(Manifest.permission.POST_NOTIFICATIONS),
            fullScreenIntent = Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE ||
                context.getSystemService(NotificationManager::class.java).canUseFullScreenIntent(),
        )

        private fun Context.granted(permission: String) =
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }
}

/** Opens a system settings screen, falling back to the main settings page on odd OEM builds. */
fun Context.openSystemSettings(action: String, withPackage: Boolean = false) {
    val intent = Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (withPackage) intent.data = "package:$packageName".toUri()
    runCatching { startActivity(intent) }
        .onFailure { runCatching { startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }
}
