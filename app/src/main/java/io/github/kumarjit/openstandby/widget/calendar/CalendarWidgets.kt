package io.github.kumarjit.openstandby.widget.calendar

import io.github.kumarjit.openstandby.widget.WidgetRegistry
import io.github.kumarjit.openstandby.widget.calendar.builtin.AgendaCalendar
import io.github.kumarjit.openstandby.widget.calendar.builtin.BigDateCalendar
import io.github.kumarjit.openstandby.widget.calendar.builtin.MonthCalendar
import io.github.kumarjit.openstandby.widget.calendar.builtin.WeekCalendar

/**
 * All calendar faces, in gallery order. The first one is the default.
 *
 * Contributing a calendar? Add your widget object to the end of this list.
 */
object CalendarWidgets : WidgetRegistry<CalendarWidget>(
    listOf(
        MonthCalendar,
        AgendaCalendar,
        BigDateCalendar,
        WeekCalendar,
        // Community calendars go here ↓
    ),
)
