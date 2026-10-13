package io.github.kumarjit.openstandby.widget.calendar.builtin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import io.github.kumarjit.openstandby.widget.WidgetOption
import io.github.kumarjit.openstandby.widget.calendar.CalendarState
import io.github.kumarjit.openstandby.widget.calendar.CalendarWidget
import io.github.kumarjit.openstandby.widget.preview.CalendarWidgetPreview
import java.time.YearMonth
import java.time.format.TextStyle

/** Reference implementation: copy this file to start a new calendar face. */
object MonthCalendar : CalendarWidget() {
    override val id = "month"
    override val displayName = "Month"
    override val author = "OpenStandby"
    override val description = "Month grid with today highlighted"

    private val eventDots = WidgetOption.Toggle("event_dots", "Dots on days with events", default = true)
    override val options: List<WidgetOption> = listOf(eventDots)

    @Composable
    override fun Content(state: CalendarState, modifier: Modifier) {
        val c = state.colors
        val today = state.today
        val month = YearMonth.from(today)
        val offset = (month.atDay(1).dayOfWeek.value - state.firstDayOfWeek.value + 7) % 7
        val rows = (offset + month.lengthOfMonth() + 6) / 7
        val showDots = state.options[eventDots]

        BoxWithConstraints(modifier) {
            val cell = minOf(maxHeight / (rows + 2), maxWidth / 7)
            val fontSize = with(LocalDensity.current) { (cell * 0.42f).toSp() }
            Column(Modifier.align(Alignment.Center).width(cell * 7)) {
                Box(Modifier.fillMaxWidth().height(cell), contentAlignment = Alignment.CenterStart) {
                    Text(
                        text = month.month.getDisplayName(TextStyle.FULL, state.locale).uppercase(state.locale),
                        color = c.accent,
                        fontSize = fontSize * 1.1f,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = cell * 0.2f),
                    )
                }
                Row(Modifier.fillMaxWidth()) {
                    for (i in 0 until 7) {
                        Box(Modifier.weight(1f).height(cell), contentAlignment = Alignment.Center) {
                            Text(
                                text = state.firstDayOfWeek.plus(i.toLong()).getDisplayName(TextStyle.NARROW, state.locale),
                                color = c.secondary,
                                fontSize = fontSize * 0.85f,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
                for (row in 0 until rows) {
                    Row(Modifier.fillMaxWidth()) {
                        for (col in 0 until 7) {
                            val day = row * 7 + col - offset + 1
                            Box(Modifier.weight(1f).height(cell), contentAlignment = Alignment.Center) {
                                if (day in 1..month.lengthOfMonth()) {
                                    val date = month.atDay(day)
                                    val isToday = date == today
                                    if (isToday) {
                                        Box(Modifier.size(cell * 0.86f).clip(CircleShape).background(c.accent))
                                    }
                                    Text(
                                        text = day.toString(),
                                        color = when {
                                            isToday -> Color.Black
                                            date.isBefore(today) -> c.secondary
                                            else -> c.primary
                                        },
                                        fontSize = fontSize,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                    )
                                    if (showDots && !isToday && date in state.eventDays) {
                                        Box(
                                            Modifier
                                                .align(Alignment.BottomCenter)
                                                .padding(bottom = cell * 0.06f)
                                                .size(cell * 0.1f)
                                                .clip(CircleShape)
                                                .background(c.accent),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 300, heightDp = 300)
@Composable
private fun MonthCalendarPreview() = CalendarWidgetPreview(MonthCalendar)
