package io.github.kumarjit.openstandby.widget.calendar.builtin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import io.github.kumarjit.openstandby.widget.calendar.showCalendarEmptyHint
import io.github.kumarjit.openstandby.widget.calendar.CalendarState
import io.github.kumarjit.openstandby.widget.calendar.CalendarWidget
import io.github.kumarjit.openstandby.widget.calendar.EventRow
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters

object WeekCalendar : CalendarWidget() {
    override val id = "week"
    override val displayName = "Week"
    override val author = "OpenStandby"
    override val description = "This week at a glance plus today's events"

    @Composable
    override fun Content(state: CalendarState, modifier: Modifier) {
        val c = state.colors
        val today = state.today
        val start = today.with(TemporalAdjusters.previousOrSame(state.firstDayOfWeek))
        val todayEvents = state.upcoming(days = 0)
        BoxWithConstraints(modifier) {
            val unit = minOf(maxWidth / 9f, maxHeight / 6f)
            val fontSize = with(LocalDensity.current) { (unit * 0.42f).toSp() }
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                Text(
                    text = today.format(DateTimeFormatter.ofPattern("MMMM yyyy", state.locale)).uppercase(state.locale),
                    color = c.accent,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(unit * 0.2f))
                Row(Modifier.fillMaxWidth()) {
                    for (i in 0 until 7) {
                        val date = start.plusDays(i.toLong())
                        val isToday = date == today
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = date.dayOfWeek.getDisplayName(TextStyle.NARROW, state.locale),
                                color = c.secondary,
                                fontSize = fontSize * 0.8f,
                            )
                            Box(
                                Modifier
                                    .size(unit * 0.95f)
                                    .clip(CircleShape)
                                    .background(if (isToday) c.accent else Color.Transparent),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    color = if (isToday) Color.Black else c.primary,
                                    fontSize = fontSize * 1.1f,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Box(
                                Modifier
                                    .size(unit * 0.1f)
                                    .clip(CircleShape)
                                    .background(if (date in state.eventDays) c.accent else Color.Transparent),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(unit * 0.3f))
                val rowUnit = unit * 0.55f
                if (!showCalendarEmptyHint(state, todayEvents, rowUnit, "Nothing else today")) {
                    todayEvents.take(3).forEach { EventRow(it, state, rowUnit) }
                }
            }
        }
    }
}
