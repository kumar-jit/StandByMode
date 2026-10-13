package io.github.kumarjit.openstandby.widget.clock

import io.github.kumarjit.openstandby.widget.WidgetRegistry
import io.github.kumarjit.openstandby.widget.clock.builtin.AnalogClock
import io.github.kumarjit.openstandby.widget.clock.builtin.DigitalClock
import io.github.kumarjit.openstandby.widget.clock.builtin.FlipClock
import io.github.kumarjit.openstandby.widget.clock.builtin.MinimalClock
import io.github.kumarjit.openstandby.widget.clock.builtin.WordsClock

/**
 * All clock faces, in gallery order. The first one is the default.
 *
 * Contributing a clock? Add your widget object to the end of this list. That's the only
 * place you need to touch outside your own file.
 */
object ClockWidgets : WidgetRegistry<ClockWidget>(
    listOf(
        DigitalClock,
        AnalogClock,
        FlipClock,
        MinimalClock,
        WordsClock,
        // Community clocks go here ↓
    ),
)
