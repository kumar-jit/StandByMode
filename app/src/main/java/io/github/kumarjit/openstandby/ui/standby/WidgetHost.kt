package io.github.kumarjit.openstandby.ui.standby

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import io.github.kumarjit.openstandby.calendar.CalendarRepository
import io.github.kumarjit.openstandby.calendar.CalendarSnapshot
import io.github.kumarjit.openstandby.data.StandbySettings
import io.github.kumarjit.openstandby.ui.theme.StandbyColors
import io.github.kumarjit.openstandby.widget.calendar.CalendarState
import io.github.kumarjit.openstandby.widget.calendar.CalendarWidget
import io.github.kumarjit.openstandby.widget.clock.ClockState
import io.github.kumarjit.openstandby.widget.clock.ClockWidget
import io.github.kumarjit.openstandby.widget.optionsFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

/*
 * The host side of the widget contract: turns app settings and system data into the
 * plain state objects that widgets receive. Widgets themselves never touch any of this.
 */

fun clockStateFor(
    widget: ClockWidget,
    settings: StandbySettings,
    now: LocalDateTime,
    colors: StandbyColors,
) = ClockState(
    now = now,
    use24Hour = settings.use24Hour,
    showSeconds = settings.showSeconds,
    colors = colors,
    options = settings.optionsFor(widget),
)

fun calendarStateFor(
    widget: CalendarWidget,
    settings: StandbySettings,
    now: LocalDateTime,
    colors: StandbyColors,
    calendar: CalendarSnapshot,
) = CalendarState(
    now = now,
    events = calendar.events,
    hasCalendarPermission = calendar.hasPermission,
    use24Hour = settings.use24Hour,
    colors = colors,
    options = settings.optionsFor(widget),
)

/** Loads on-device calendar events for [today] and refreshes them every minute. */
@Composable
fun rememberCalendarSnapshot(today: LocalDate): CalendarSnapshot {
    val context = LocalContext.current
    val snapshot by produceState(CalendarSnapshot.Empty, today) {
        val repository = CalendarRepository(context.applicationContext)
        while (true) {
            value = withContext(Dispatchers.IO) { repository.load(today) }
            delay(60_000L)
        }
    }
    return snapshot
}
