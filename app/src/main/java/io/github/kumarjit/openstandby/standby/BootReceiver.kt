package io.github.kumarjit.openstandby.standby

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.kumarjit.openstandby.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Restarts the charging monitor after a reboot or app update, if the user enabled it. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        val pending = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (SettingsRepository(appContext).settings.first().autoLaunchOnCharge) {
                    runCatching { ChargingMonitorService.sync(appContext, enabled = true) }
                }
            } finally {
                pending.finish()
            }
        }
    }
}
