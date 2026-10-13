package io.github.kumarjit.openstandby.widget.calendar.builtin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import io.github.kumarjit.openstandby.ui.common.FitText
import io.github.kumarjit.openstandby.widget.WidgetOption
import io.github.kumarjit.openstandby.widget.calendar.CalendarState
import io.github.kumarjit.openstandby.widget.calendar.CalendarWidget
import io.github.kumarjit.openstandby.widget.calendar.eventWhenLabel
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

object BigDateCalendar : CalendarWidget() {
    override val id = "big_date"
    override val displayName = "Big date"
    override val author = "OpenStandby"
    override val description = "Huge day number with the next event"

    private val nextEvent = WidgetOption.Toggle("next_event", "Show next event", default = true)
    override val options: List<WidgetOption> = listOf(nextEvent)

    @Composable
    override fun Content(state: CalendarState, modifier: Modifier) {
        val c = state.colors
        val today = state.today
        val next = if (state.options[nextEvent]) state.events.firstOrNull { it.end.isAfter(state.now) } else null
        Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            FitText(
                today.dayOfWeek.getDisplayName(TextStyle.FULL, state.locale).uppercase(state.locale),
                c.accent,
                Modifier.fillMaxWidth().weight(0.8f),
                charWidth = 0.72f,
            )
            FitText(today.dayOfMonth.toString(), c.primary, Modifier.fillMaxWidth().weight(2.4f))
            FitText(
                today.format(DateTimeFormatter.ofPattern("MMMM yyyy", state.locale)),
                c.secondary,
                Modifier.fillMaxWidth().weight(0.6f),
                fontWeight = FontWeight.Medium,
            )
            if (next != null) {
                FitText(
                    "${next.title} · ${eventWhenLabel(next, state)}",
                    c.secondary,
                    Modifier.fillMaxWidth().weight(0.45f),
                    fontWeight = FontWeight.Normal,
                    charWidth = 0.52f,
                )
            }
        }
    }
}
