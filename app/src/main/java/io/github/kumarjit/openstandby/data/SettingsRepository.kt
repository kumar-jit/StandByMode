package io.github.kumarjit.openstandby.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "standby_settings")

/** Local-only settings storage (Jetpack DataStore). */
class SettingsRepository(context: Context) {

    private val store = context.applicationContext.dataStore

    val settings: Flow<StandbySettings> = store.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it.toSettings() }
        .distinctUntilChanged()

    suspend fun update(transform: (StandbySettings) -> StandbySettings) {
        store.edit { prefs -> prefs.write(transform(prefs.toSettings())) }
    }

    private companion object {
        /** Widget options are stored as one string preference each: "widget_option.<widgetId>.<key>". */
        const val OPTION_PREFIX = "widget_option."
    }

    private object Keys {
        val DISPLAY_MODE = stringPreferencesKey("display_mode")
        val CLOCK_STYLE = stringPreferencesKey("clock_style")
        val CALENDAR_STYLE = stringPreferencesKey("calendar_style")
        val ACCENT = stringPreferencesKey("accent")
        val USE_24_HOUR = booleanPreferencesKey("use_24_hour")
        val SHOW_SECONDS = booleanPreferencesKey("show_seconds")
        val NIGHT_MODE = booleanPreferencesKey("night_mode")
        val BURN_IN = booleanPreferencesKey("burn_in_protection")
        val SHOW_NOTIFICATIONS = booleanPreferencesKey("show_notifications")
        val SHOW_NOTIFICATION_CONTENT = booleanPreferencesKey("show_notification_content")
        val PRIORITY_APPS = stringSetPreferencesKey("priority_apps")
        val AUTO_LAUNCH = booleanPreferencesKey("auto_launch_on_charge")
        val LAUNCH_ON_LOCK = booleanPreferencesKey("launch_on_lock")
    }

    private fun Preferences.toSettings(): StandbySettings {
        val d = StandbySettings()
        return StandbySettings(
            displayMode = this[Keys.DISPLAY_MODE].toEnum(d.displayMode),
            clockWidgetId = this[Keys.CLOCK_STYLE]?.lowercase() ?: d.clockWidgetId,
            calendarWidgetId = this[Keys.CALENDAR_STYLE]?.lowercase() ?: d.calendarWidgetId,
            widgetOptions = asMap().entries
                .filter { it.key.name.startsWith(OPTION_PREFIX) }
                .mapNotNull { (key, value) -> (value as? String)?.let { key.name.removePrefix(OPTION_PREFIX) to it } }
                .toMap(),
            accent = this[Keys.ACCENT].toEnum(d.accent),
            use24Hour = this[Keys.USE_24_HOUR] ?: d.use24Hour,
            showSeconds = this[Keys.SHOW_SECONDS] ?: d.showSeconds,
            nightMode = this[Keys.NIGHT_MODE] ?: d.nightMode,
            burnInProtection = this[Keys.BURN_IN] ?: d.burnInProtection,
            showNotifications = this[Keys.SHOW_NOTIFICATIONS] ?: d.showNotifications,
            showNotificationContent = this[Keys.SHOW_NOTIFICATION_CONTENT] ?: d.showNotificationContent,
            priorityApps = this[Keys.PRIORITY_APPS] ?: d.priorityApps,
            autoLaunchOnCharge = this[Keys.AUTO_LAUNCH] ?: d.autoLaunchOnCharge,
            launchOnLock = this[Keys.LAUNCH_ON_LOCK] ?: d.launchOnLock,
        )
    }

    private fun MutablePreferences.write(s: StandbySettings) {
        this[Keys.DISPLAY_MODE] = s.displayMode.name
        this[Keys.CLOCK_STYLE] = s.clockWidgetId
        this[Keys.CALENDAR_STYLE] = s.calendarWidgetId
        asMap().keys.filter { it.name.startsWith(OPTION_PREFIX) }.forEach {
            @Suppress("UNCHECKED_CAST")
            remove(it as Preferences.Key<Any>)
        }
        s.widgetOptions.forEach { (key, value) -> this[stringPreferencesKey(OPTION_PREFIX + key)] = value }
        this[Keys.ACCENT] = s.accent.name
        this[Keys.USE_24_HOUR] = s.use24Hour
        this[Keys.SHOW_SECONDS] = s.showSeconds
        this[Keys.NIGHT_MODE] = s.nightMode
        this[Keys.BURN_IN] = s.burnInProtection
        this[Keys.SHOW_NOTIFICATIONS] = s.showNotifications
        this[Keys.SHOW_NOTIFICATION_CONTENT] = s.showNotificationContent
        this[Keys.PRIORITY_APPS] = s.priorityApps
        this[Keys.AUTO_LAUNCH] = s.autoLaunchOnCharge
        this[Keys.LAUNCH_ON_LOCK] = s.launchOnLock
    }
}

private inline fun <reified T : Enum<T>> String?.toEnum(default: T): T =
    this?.let { name -> enumValues<T>().firstOrNull { it.name == name } } ?: default
