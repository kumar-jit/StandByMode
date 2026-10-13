package io.github.kumarjit.openstandby

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import io.github.kumarjit.openstandby.data.SettingsRepository
import io.github.kumarjit.openstandby.standby.ChargingMonitorService
import io.github.kumarjit.openstandby.ui.settings.SettingsScreen
import io.github.kumarjit.openstandby.ui.theme.OpenStandbyTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val repository = SettingsRepository(this)

        // Bring the charging monitor back if the system killed it.
        lifecycleScope.launch {
            if (repository.settings.first().autoLaunchOnCharge) {
                ChargingMonitorService.sync(this@MainActivity, enabled = true)
            }
        }

        setContent {
            OpenStandbyTheme {
                SettingsScreen(repository)
            }
        }
    }
}
