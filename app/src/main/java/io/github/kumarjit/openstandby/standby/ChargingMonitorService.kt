package io.github.kumarjit.openstandby.standby

import android.Manifest
import android.app.KeyguardManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import io.github.kumarjit.openstandby.MainActivity
import io.github.kumarjit.openstandby.R
import io.github.kumarjit.openstandby.data.SettingsRepository
import kotlinx.coroutines.launch

/**
 * Optional foreground service for "open automatically when charging".
 *
 * Android does not let background apps start activities, so standby is launched through a
 * full-screen-intent notification, which the system turns into the activity when the
 * phone is locked or the screen is off.
 */
class ChargingMonitorService : LifecycleService() {

    @Volatile
    private var launchOnLock = true

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_POWER_CONNECTED -> maybeLaunch()
                Intent.ACTION_SCREEN_OFF -> if (launchOnLock) maybeLaunch()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        StandbyChannels.ensure(this)
        startInForeground()
        ContextCompat.registerReceiver(
            this,
            receiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_SCREEN_OFF)
            },
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        lifecycleScope.launch {
            SettingsRepository(this@ChargingMonitorService).settings.collect { s ->
                launchOnLock = s.launchOnLock
                if (!s.autoLaunchOnCharge) stopSelf()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    private fun startInForeground() {
        val openSettings = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(this, StandbyChannels.MONITOR)
            .setSmallIcon(R.drawable.ic_standby)
            .setContentTitle("Standby is ready")
            .setContentText("Opens automatically while charging")
            .setContentIntent(openSettings)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
        ServiceCompat.startForeground(
            this,
            StandbyChannels.MONITOR_NOTIFICATION_ID,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            },
        )
    }

    private fun maybeLaunch() {
        if (!isCharging()) return
        // Standby is up, or the user just turned the screen off from it: don't fight them.
        if (StandbyActivity.isShowing || SystemClock.elapsedRealtime() - StandbyActivity.lastStoppedAt < 3_000) return

        val keyguard = getSystemService(KeyguardManager::class.java)
        val power = getSystemService(PowerManager::class.java)
        if (!keyguard.isKeyguardLocked && power.isInteractive) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val fullScreen = PendingIntent.getActivity(
            this,
            1,
            StandbyActivity.intent(this, preview = false),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(this, StandbyChannels.LAUNCH)
            .setSmallIcon(R.drawable.ic_standby)
            .setContentTitle("Standby")
            .setContentText("Tap to open standby")
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(fullScreen)
            .setFullScreenIntent(fullScreen, true)
            .setAutoCancel(true)
            .setTimeoutAfter(60_000)
            .build()
        NotificationManagerCompat.from(this).notify(StandbyChannels.LAUNCH_NOTIFICATION_ID, notification)
    }

    private fun isCharging(): Boolean {
        val battery = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: return false
        return battery.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0
    }

    companion object {
        fun sync(context: Context, enabled: Boolean) {
            val intent = Intent(context, ChargingMonitorService::class.java)
            if (enabled) ContextCompat.startForegroundService(context, intent) else context.stopService(intent)
        }
    }
}
