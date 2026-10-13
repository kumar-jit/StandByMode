package io.github.kumarjit.openstandby.notifications

import io.github.kumarjit.openstandby.data.StandbySettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class StandbyNotification(
    val key: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val postTime: Long,
    /** Messages, calls, alarms, reminders, events, or high-importance channels. */
    val systemImportant: Boolean,
)

/** In-memory store fed by [StandbyNotificationListener]. Nothing is persisted or sent anywhere. */
object NotificationRepository {
    private val _notifications = MutableStateFlow<List<StandbyNotification>>(emptyList())
    val notifications: StateFlow<List<StandbyNotification>> = _notifications.asStateFlow()

    fun replaceAll(items: List<StandbyNotification>) {
        _notifications.value = items.sortedByDescending { it.postTime }
    }

    fun upsert(item: StandbyNotification) {
        _notifications.update { list ->
            (list.filterNot { it.key == item.key } + item).sortedByDescending { it.postTime }
        }
    }

    fun remove(key: String) {
        _notifications.update { list -> list.filterNot { it.key == key } }
    }

    fun clear() {
        _notifications.value = emptyList()
    }
}

fun List<StandbyNotification>.important(settings: StandbySettings): List<StandbyNotification> =
    if (!settings.showNotifications) emptyList()
    else filter { it.systemImportant || it.packageName in settings.priorityApps }
