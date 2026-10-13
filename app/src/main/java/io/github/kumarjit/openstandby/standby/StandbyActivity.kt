package io.github.kumarjit.openstandby.standby

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.kumarjit.openstandby.data.SettingsRepository
import io.github.kumarjit.openstandby.notifications.NotificationRepository
import io.github.kumarjit.openstandby.ui.standby.StandbyScreen
import io.github.kumarjit.openstandby.widget.calendar.CalendarWidgets
import io.github.kumarjit.openstandby.widget.clock.ClockWidgets
import kotlinx.coroutines.launch

/**
 * Full-screen standby shown over the lock screen. Tap the clock or calendar to cycle styles;
 * it closes itself when the charger is unplugged (unless opened as a preview).
 */
class StandbyActivity : ComponentActivity() {

    private val isPreview get() = intent.getBooleanExtra(EXTRA_PREVIEW, false)

    private val powerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!isPreview) finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverLockScreen()
        NotificationManagerCompat.from(this).cancel(StandbyChannels.LAUNCH_NOTIFICATION_ID)
        ContextCompat.registerReceiver(
            this,
            powerReceiver,
            IntentFilter(Intent.ACTION_POWER_DISCONNECTED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )

        val repository = SettingsRepository(this)
        setContent {
            val settings by repository.settings.collectAsStateWithLifecycle(initialValue = null)
            val notifications by NotificationRepository.notifications.collectAsStateWithLifecycle()
            val scope = rememberCoroutineScope()
            val current = settings
            if (current != null) {
                LaunchedEffect(current.nightMode) { setDim(current.nightMode) }
                StandbyScreen(
                    settings = current,
                    notifications = notifications,
                    onClockTap = {
                        scope.launch {
                            repository.update { it.copy(clockWidgetId = ClockWidgets.nextId(it.clockWidgetId)) }
                        }
                    },
                    onCalendarTap = {
                        scope.launch {
                            repository.update { it.copy(calendarWidgetId = CalendarWidgets.nextId(it.calendarWidgetId)) }
                        }
                    },
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        isShowing = true
    }

    override fun onStop() {
        super.onStop()
        isShowing = false
        lastStoppedAt = SystemClock.elapsedRealtime()
    }

    override fun onDestroy() {
        unregisterReceiver(powerReceiver)
        super.onDestroy()
    }

    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun setDim(dim: Boolean) {
        window.attributes = window.attributes.apply {
            screenBrightness = if (dim) 0.02f else WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        }
    }

    companion object {
        private const val EXTRA_PREVIEW = "preview"

        /** Whether standby is currently on screen; used to avoid relaunch loops. */
        @Volatile
        var isShowing = false
            private set

        @Volatile
        var lastStoppedAt = 0L
            private set

        fun intent(context: Context, preview: Boolean): Intent =
            Intent(context, StandbyActivity::class.java)
                .putExtra(EXTRA_PREVIEW, preview)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
