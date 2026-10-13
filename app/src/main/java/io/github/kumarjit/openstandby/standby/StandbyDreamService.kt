package io.github.kumarjit.openstandby.standby

import android.service.dreams.DreamService
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import io.github.kumarjit.openstandby.data.SettingsRepository
import io.github.kumarjit.openstandby.notifications.NotificationRepository
import io.github.kumarjit.openstandby.ui.standby.StandbyScreen

/**
 * Android "screen saver" (Daydream). Pick OpenStandby under Settings > Display > Screen saver
 * and choose "While charging" — the most reliable way to get standby on the lock screen,
 * because the system itself starts it.
 */
class StandbyDreamService : DreamService(), LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false
        isFullscreen = true

        val repository = SettingsRepository(this)
        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@StandbyDreamService)
            setViewTreeSavedStateRegistryOwner(this@StandbyDreamService)
            setContent {
                val settings by repository.settings.collectAsStateWithLifecycle(initialValue = null)
                val notifications by NotificationRepository.notifications.collectAsStateWithLifecycle()
                val current = settings
                if (current != null) {
                    LaunchedEffect(current.nightMode) { isScreenBright = !current.nightMode }
                    StandbyScreen(current, notifications)
                }
            }
        }
        setContentView(view)
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    override fun onDreamingStopped() {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        super.onDreamingStopped()
    }

    override fun onDetachedFromWindow() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        super.onDetachedFromWindow()
    }
}
