@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.kumarjit.openstandby.ui.settings

import android.Manifest
import android.content.Intent
import androidx.core.net.toUri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.kumarjit.openstandby.data.AccentColor
import io.github.kumarjit.openstandby.data.DisplayMode
import io.github.kumarjit.openstandby.data.SettingsRepository
import io.github.kumarjit.openstandby.data.StandbySettings
import io.github.kumarjit.openstandby.notifications.NotificationRepository
import io.github.kumarjit.openstandby.standby.ChargingMonitorService
import io.github.kumarjit.openstandby.standby.StandbyActivity
import io.github.kumarjit.openstandby.ui.standby.StandbyScreen
import io.github.kumarjit.openstandby.widget.calendar.CalendarWidgets
import io.github.kumarjit.openstandby.widget.clock.ClockWidgets
import kotlinx.coroutines.launch

const val REPOSITORY_URL = "https://github.com/kumar-jit/StandByMode"

@Composable
fun SettingsScreen(repository: SettingsRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by repository.settings.collectAsStateWithLifecycle(initialValue = StandbySettings())
    val notifications by NotificationRepository.notifications.collectAsStateWithLifecycle()

    var permissionTick by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { permissionTick++ }
    val perms = remember(permissionTick) { PermissionStatus.read(context) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        permissionTick++
    }
    var showAppPicker by remember { mutableStateOf(false) }

    fun update(transform: (StandbySettings) -> StandbySettings) {
        scope.launch { repository.update(transform) }
    }

    fun requestPostNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("OpenStandby") }) }) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(20.dp))) {
                    StandbyScreen(
                        settings = settings,
                        notifications = notifications,
                        onClockTap = { update { it.copy(clockWidgetId = ClockWidgets.nextId(it.clockWidgetId)) } },
                        onCalendarTap = {
                            update { it.copy(calendarWidgetId = CalendarWidgets.nextId(it.calendarWidgetId)) }
                        },
                    )
                }
                Text(
                    "Live preview — tap the clock or calendar to switch styles",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            item {
                Button(
                    onClick = { context.startActivity(StandbyActivity.intent(context, preview = true)) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Start standby now") }
            }

            item {
                Section("Show") {
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        DisplayMode.entries.forEachIndexed { index, mode ->
                            SegmentedButton(
                                selected = settings.displayMode == mode,
                                onClick = { update { it.copy(displayMode = mode) } },
                                shape = SegmentedButtonDefaults.itemShape(index, DisplayMode.entries.size),
                            ) { Text(mode.label, maxLines = 1) }
                        }
                    }
                }
            }
            item {
                Section("Clock") {
                    ClockWidgetPicker(settings, ::update)
                }
            }
            item {
                Section("Calendar") {
                    CalendarWidgetPicker(settings, ::update)
                    if (!perms.calendar) {
                        PermissionRow("Calendar access", "Shows your events from the phone's calendar", false) {
                            permissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                        }
                    }
                }
            }
            item {
                Section("Look") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AccentColor.entries.forEach { accent ->
                            val selected = settings.accent == accent
                            Box(
                                Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(accent.argb))
                                    .then(
                                        if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                        else Modifier,
                                    )
                                    .clickable { update { it.copy(accent = accent) } }
                                    .semantics { contentDescription = accent.label },
                            )
                        }
                    }
                    SwitchRow("24-hour time", settings.use24Hour) { v -> update { it.copy(use24Hour = v) } }
                    SwitchRow("Show seconds", settings.showSeconds) { v -> update { it.copy(showSeconds = v) } }
                    SwitchRow("Night mode", settings.nightMode, "Dim red display for dark rooms") { v ->
                        update { it.copy(nightMode = v) }
                    }
                    SwitchRow("Burn-in protection", settings.burnInProtection, "Shifts content slightly every minute") { v ->
                        update { it.copy(burnInProtection = v) }
                    }
                }
            }
            item {
                Section("Notifications") {
                    SwitchRow(
                        "Show important notifications",
                        settings.showNotifications,
                        "Messages, calls, alarms, reminders and high-priority alerts",
                    ) { v -> update { it.copy(showNotifications = v) } }
                    SwitchRow(
                        "Show message content",
                        settings.showNotificationContent,
                        "Off: only the app name is shown on the lock screen",
                        enabled = settings.showNotifications,
                    ) { v -> update { it.copy(showNotificationContent = v) } }
                    ActionRow(
                        "Priority apps",
                        "${settings.priorityApps.size} selected — always shown",
                        "Choose",
                    ) { showAppPicker = true }
                    PermissionRow("Notification access", "Required to read notifications", perms.notificationAccess) {
                        context.openSystemSettings(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    }
                }
            }
            item {
                Section("When to show") {
                    ActionRow(
                        "Use as screen saver (recommended)",
                        "Settings › Display › Screen saver › OpenStandby, start \"While charging\"",
                        "Open",
                    ) { context.openSystemSettings(Settings.ACTION_DREAM_SETTINGS) }
                    SwitchRow(
                        "Open automatically when charging",
                        settings.autoLaunchOnCharge,
                        "Shows standby when you plug in while the phone is locked",
                    ) { enabled ->
                        scope.launch {
                            repository.update { it.copy(autoLaunchOnCharge = enabled) }
                            ChargingMonitorService.sync(context, enabled)
                        }
                        if (enabled && !perms.postNotifications) requestPostNotifications()
                    }
                    SwitchRow(
                        "Also when locking while charging",
                        settings.launchOnLock,
                        enabled = settings.autoLaunchOnCharge,
                    ) { v -> update { it.copy(launchOnLock = v) } }
                    if (settings.autoLaunchOnCharge) {
                        PermissionRow("Notifications", "Used to launch standby", perms.postNotifications) {
                            requestPostNotifications()
                        }
                        PermissionRow("Full-screen alerts", "Lets standby open over the lock screen", perms.fullScreenIntent) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                context.openSystemSettings(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, withPackage = true)
                            }
                        }
                    }
                }
            }
            item {
                Section("About") {
                    Text(
                        "OpenStandby is free and open source. It works completely offline: " +
                            "the app has no internet permission and nothing leaves your phone.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    TextButton(onClick = {
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, REPOSITORY_URL.toUri())
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            )
                        }
                    }) { Text("Source code & how to contribute") }
                }
            }
        }
    }

    if (showAppPicker) {
        AppPickerDialog(
            initial = settings.priorityApps,
            onDismiss = { showAppPicker = false },
            onConfirm = { apps -> update { it.copy(priorityApps = apps) } },
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}

@Composable
internal fun <T> ChoiceChips(options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(options) { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(label(option)) },
            )
        }
    }
}

@Composable
internal fun SwitchRow(
    title: String,
    checked: Boolean,
    subtitle: String? = null,
    enabled: Boolean = true,
    onChange: (Boolean) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onChange)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RowText(title, subtitle, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = null, enabled = enabled)
    }
}

@Composable
private fun ActionRow(title: String, subtitle: String?, action: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        RowText(title, subtitle, Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        FilledTonalButton(onClick = onClick) { Text(action) }
    }
}

@Composable
private fun PermissionRow(title: String, subtitle: String, granted: Boolean, onGrant: () -> Unit) {
    if (granted) {
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            RowText(title, subtitle, Modifier.weight(1f))
            Text("Allowed", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        }
    } else {
        ActionRow(title, subtitle, "Allow", onGrant)
    }
}

@Composable
private fun RowText(title: String, subtitle: String?, modifier: Modifier) {
    Column(modifier) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
