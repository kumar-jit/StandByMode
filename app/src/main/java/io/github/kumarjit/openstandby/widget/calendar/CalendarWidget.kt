package io.github.kumarjit.openstandby.widget.calendar

import androidx.compose.runtime.Immutable
import io.github.kumarjit.openstandby.calendar.CalendarEvent
import io.github.kumarjit.openstandby.calendar.eventDays
import io.github.kumarjit.openstandby.data.AccentColor
import io.github.kumarjit.openstandby.ui.theme.StandbyColors
import io.github.kumarjit.openstandby.ui.theme.standbyColors
import io.github.kumarjit.openstandby.widget.StandbyWidget
import io.github.kumarjit.openstandby.widget.WidgetOptions
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * Everything a calendar face receives.
 *
 * @property now current local time (ticks once per second)
 * @property events on-device events covering at least the current month and the next 8 days, sorted by start
 * @property hasCalendarPermission false until the user grants calendar access; show a hint instead of events
 * @property use24Hour the user's 12/24-hour preference, for event times
 * @property colors palette to draw with; respects accent colour and night mode
 * @property options values of this widget's own [StandbyWidget.options]
 * @property locale for month/day names
 * @property firstDayOfWeek locale-aware start of the week
 */
@Immutable
data class CalendarState(
    val now: LocalDateTime,
    val events: List<CalendarEvent>,
    val hasCalendarPermission: Boolean,
    val use24Hour: Boolean,
    val colors: StandbyColors,
    val options: WidgetOptions = WidgetOptions.Defaults,
    val locale: Locale = Locale.getDefault(),
    val firstDayOfWeek: DayOfWeek = WeekFields.of(locale).firstDayOfWeek,
) {
    val today: LocalDate get() = now.toLocalDate()

    /** Every date touched by at least one event, for dots and highlights. */
    val eventDays: Set<LocalDate> by lazy { eventDays(events) }

    /** Events not yet finished that start within [days] days from today. */
    fun upcoming(days: Long = 7): List<CalendarEvent> =
        events.filter { it.end.isAfter(now) && !it.start.toLocalDate().isAfter(today.plusDays(days)) }

    companion object {
        /** Fixed sample with a few events, for previews and screenshots. */
        fun sample(
            colors: StandbyColors = standbyColors(AccentColor.ORANGE, night = false),
            options: WidgetOptions = WidgetOptions.Defaults,
        ): CalendarState {
            val now = LocalDateTime.of(2026, 1, 15, 10, 9, 30)
            val day = now.toLocalDate()
            return CalendarState(
                now = now,
                events = listOf(
                    CalendarEvent(1, "Team standup", day.atTime(11, 0), day.atTime(11, 15), false, null),
                    CalendarEvent(2, "Lunch with Sam", day.atTime(13, 0), day.atTime(14, 0), false, 0xFF30D158.toInt()),
                    CalendarEvent(3, "Gym", day.plusDays(1).atTime(7, 0), day.plusDays(1).atTime(8, 0), false, null),
                    CalendarEvent(4, "Holiday", day.plusDays(5).atStartOfDay(), day.plusDays(6).atStartOfDay(), true, null),
                ),
                hasCalendarPermission = true,
                use24Hour = false,
                colors = colors,
                options = options,
                locale = Locale.US,
            )
        }
    }
}

/**
 * Base class for calendar faces. Extend it with a Kotlin `object` and register it in [CalendarWidgets].
 * Reusable pieces (event rows, hints, time labels) live in `CalendarKit.kt`.
 */
abstract class CalendarWidget : StandbyWidget<CalendarState>
