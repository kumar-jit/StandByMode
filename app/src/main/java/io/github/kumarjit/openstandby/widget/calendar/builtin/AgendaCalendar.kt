package io.github.kumarjit.openstandby.widget.calendar.builtin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import io.github.kumarjit.openstandby.widget.WidgetOption
import io.github.kumarjit.openstandby.widget.calendar.showCalendarEmptyHint
import io.github.kumarjit.openstandby.widget.calendar.CalendarState
import io.github.kumarjit.openstandby.widget.calendar.CalendarWidget
import io.github.kumarjit.openstandby.widget.calendar.EventRow
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

object AgendaCalendar : CalendarWidget() {
    override val id = "agenda"
    override val displayName = "Agenda"
    override val author = "OpenStandby"
    override val description = "Today's date and your next events"

    private val lookAhead = WidgetOption.Choice("look_ahead", "Show events for", listOf("Today", "3 days", "7 days"), "7 days")
    override val options: List<WidgetOption> = listOf(lookAhead)

    @Composable
    override fun Content(state: CalendarState, modifier: Modifier) {
        val c = state.colors
        val days = when (state.options[lookAhead]) {
            "Today" -> 0L
            "3 days" -> 3L
            else -> 7L
        }
        val upcoming = state.upcoming(days)
        BoxWithConstraints(modifier) {
            val unit = minOf(maxHeight / 10f, maxWidth / 14f)
            val fontSize = with(LocalDensity.current) { unit.toSp() }
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                Text(
                    text = state.today.dayOfWeek.getDisplayName(TextStyle.FULL, state.locale).uppercase(state.locale),
                    color = c.accent,
                    fontSize = fontSize * 0.9f,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = state.today.format(DateTimeFormatter.ofPattern("MMMM d", state.locale)),
                    color = c.primary,
                    fontSize = fontSize * 2f,
                    lineHeight = fontSize * 2.1f,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Spacer(Modifier.height(unit * 0.6f))
                if (!showCalendarEmptyHint(state, upcoming, unit, "No upcoming events")) {
                    upcoming.take(4).forEach { EventRow(it, state, unit) }
                }
            }
        }
    }
}
