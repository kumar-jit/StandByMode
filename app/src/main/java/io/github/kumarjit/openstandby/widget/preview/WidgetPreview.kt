package io.github.kumarjit.openstandby.widget.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.kumarjit.openstandby.data.AccentColor
import io.github.kumarjit.openstandby.ui.theme.LocalStandbyColors
import io.github.kumarjit.openstandby.ui.theme.StandbyColors
import io.github.kumarjit.openstandby.ui.theme.standbyColors
import io.github.kumarjit.openstandby.widget.WidgetOptions
import io.github.kumarjit.openstandby.widget.calendar.CalendarState
import io.github.kumarjit.openstandby.widget.calendar.CalendarWidget
import io.github.kumarjit.openstandby.widget.clock.ClockState
import io.github.kumarjit.openstandby.widget.clock.ClockWidget

/*
 * Android Studio preview helpers for widget authors:
 *
 *   @Preview(widthDp = 400, heightDp = 240)
 *   @Composable
 *   private fun MyClockPreview() = ClockWidgetPreview(MyClock)
 *
 * Add `night = true` to check night mode, and try a few sizes.
 */

@Composable
fun ClockWidgetPreview(
    widget: ClockWidget,
    accent: AccentColor = AccentColor.ORANGE,
    night: Boolean = false,
    options: Map<String, String> = emptyMap(),
) {
    val colors = standbyColors(accent, night)
    WidgetFrame(colors) {
        widget.Content(ClockState.sample(colors, WidgetOptions(options)), Modifier.fillMaxSize())
    }
}

@Composable
fun CalendarWidgetPreview(
    widget: CalendarWidget,
    accent: AccentColor = AccentColor.ORANGE,
    night: Boolean = false,
    options: Map<String, String> = emptyMap(),
) {
    val colors = standbyColors(accent, night)
    WidgetFrame(colors) {
        widget.Content(CalendarState.sample(colors, WidgetOptions(options)), Modifier.fillMaxSize())
    }
}

@Composable
private fun WidgetFrame(colors: StandbyColors, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalStandbyColors provides colors) {
        Box(Modifier.fillMaxSize().background(Color.Black).padding(16.dp)) { content() }
    }
}
