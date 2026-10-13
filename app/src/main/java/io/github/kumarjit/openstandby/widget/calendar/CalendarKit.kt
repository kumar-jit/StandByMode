package io.github.kumarjit.openstandby.widget.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import io.github.kumarjit.openstandby.calendar.CalendarEvent
import io.github.kumarjit.openstandby.widget.TimeText
import java.time.format.TextStyle

/*
 * Shared building blocks for calendar widgets. Use them so community widgets look and
 * behave consistently (permission hints, event colours in night mode, time formats).
 */

/** Secondary-coloured one-line message, e.g. "No upcoming events". [unit] sets the text size. */
@Composable
fun CalendarHint(text: String, state: CalendarState, unit: Dp) {
    val fontSize = with(LocalDensity.current) { (unit * 0.8f).toSp() }
    Text(text, color = state.colors.secondary, fontSize = fontSize)
}

/** Shows the standard hint when calendar access is missing or there are no events; returns true if it did. */
@Composable
fun showCalendarEmptyHint(state: CalendarState, events: List<CalendarEvent>, unit: Dp, emptyText: String): Boolean {
    when {
        !state.hasCalendarPermission -> CalendarHint("Allow calendar access to see events", state, unit)
        events.isEmpty() -> CalendarHint(emptyText, state, unit)
        else -> return false
    }
    return true
}

/** Colour bar + title + time line for one event. [unit] sets the text size. */
@Composable
fun EventRow(event: CalendarEvent, state: CalendarState, unit: Dp) {
    val c = state.colors
    val fontSize = with(LocalDensity.current) { (unit * 0.8f).toSp() }
    Row(
        Modifier.padding(vertical = unit * 0.15f).height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(unit * 0.18f).fillMaxHeight().clip(RoundedCornerShape(50)).background(eventColor(event, state)))
        Spacer(Modifier.width(unit * 0.4f))
        Column {
            Text(
                text = event.title,
                color = c.primary,
                fontSize = fontSize,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(text = eventWhenLabel(event, state), color = c.secondary, fontSize = fontSize * 0.85f, maxLines = 1)
        }
    }
}

/** The event's calendar colour, or the accent in night mode / when it has none. */
fun eventColor(event: CalendarEvent, state: CalendarState): Color =
    if (state.colors.monochrome || event.color == null) state.colors.accent
    else Color(event.color or 0xFF000000.toInt())

/** "10:00 AM – 11:00 AM", "Tomorrow · All day", "Fri · 9:00 – 10:00". */
fun eventWhenLabel(event: CalendarEvent, state: CalendarState): String {
    val today = state.today
    val day = event.start.toLocalDate()
    val dayLabel = when {
        !day.isAfter(today) -> ""
        day == today.plusDays(1) -> "Tomorrow · "
        else -> day.dayOfWeek.getDisplayName(TextStyle.SHORT, state.locale) + " · "
    }
    val time = if (event.allDay) {
        "All day"
    } else {
        "${TimeText.time(event.start.toLocalTime(), state.use24Hour)} – " +
            TimeText.time(event.end.toLocalTime(), state.use24Hour)
    }
    return dayLabel + time
}
