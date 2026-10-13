package io.github.kumarjit.openstandby.calendar

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract.Instances
import androidx.core.content.ContextCompat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

data class CalendarEvent(
    val id: Long,
    val title: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val allDay: Boolean,
    val color: Int?,
)

/** Every local date touched by at least one of [events]. */
fun eventDays(events: List<CalendarEvent>): Set<LocalDate> = buildSet {
    for (e in events) {
        var day = e.start.toLocalDate()
        val last = maxOf(day, e.end.minusNanos(1).toLocalDate())
        var guard = 0
        while (!day.isAfter(last) && guard++ < 62) {
            add(day)
            day = day.plusDays(1)
        }
    }
}

data class CalendarSnapshot(val hasPermission: Boolean, val events: List<CalendarEvent>) {
    companion object {
        val Empty = CalendarSnapshot(hasPermission = false, events = emptyList())
    }
}

/**
 * Reads events from the on-device calendar provider. This is the phone's local copy,
 * so it works fully offline; the app itself never syncs anything.
 */
class CalendarRepository(private val context: Context) {

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) ==
            PackageManager.PERMISSION_GRANTED

    /** Events for the current month plus at least the next 8 days. */
    fun load(today: LocalDate): CalendarSnapshot {
        if (!hasPermission()) return CalendarSnapshot.Empty
        val zone = ZoneId.systemDefault()
        val from = today.withDayOfMonth(1)
        val until = maxOf(today.plusDays(8), from.plusMonths(1))
        val events = query(
            from.atStartOfDay(zone).toInstant().toEpochMilli(),
            until.atStartOfDay(zone).toInstant().toEpochMilli(),
        )
        return CalendarSnapshot(hasPermission = true, events = events)
    }

    private fun query(begin: Long, end: Long): List<CalendarEvent> {
        val uri = Instances.CONTENT_URI.buildUpon().also {
            ContentUris.appendId(it, begin)
            ContentUris.appendId(it, end)
        }.build()
        val projection = arrayOf(
            Instances.EVENT_ID,
            Instances.TITLE,
            Instances.BEGIN,
            Instances.END,
            Instances.ALL_DAY,
            Instances.DISPLAY_COLOR,
        )
        val events = mutableListOf<CalendarEvent>()
        runCatching {
            context.contentResolver.query(uri, projection, null, null, "${Instances.BEGIN} ASC")?.use { c ->
                while (c.moveToNext()) {
                    val allDay = c.getInt(4) == 1
                    // All-day instances are stored as UTC midnights.
                    val zone: ZoneId = if (allDay) ZoneOffset.UTC else ZoneId.systemDefault()
                    events += CalendarEvent(
                        id = c.getLong(0),
                        title = c.getString(1)?.takeIf { it.isNotBlank() } ?: "(No title)",
                        start = toLocal(c.getLong(2), zone),
                        end = toLocal(c.getLong(3), zone),
                        allDay = allDay,
                        color = if (c.isNull(5)) null else c.getInt(5),
                    )
                }
            }
        }
        return events
    }

    private fun toLocal(millis: Long, zone: ZoneId): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), zone)
}
