package io.github.kumarjit.openstandby.ui.standby

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.kumarjit.openstandby.data.DisplayMode
import io.github.kumarjit.openstandby.data.StandbySettings
import io.github.kumarjit.openstandby.notifications.StandbyNotification
import io.github.kumarjit.openstandby.notifications.important
import io.github.kumarjit.openstandby.ui.common.rememberNow
import io.github.kumarjit.openstandby.ui.theme.LocalStandbyColors
import io.github.kumarjit.openstandby.ui.theme.standbyColors
import io.github.kumarjit.openstandby.widget.calendar.CalendarWidgets
import io.github.kumarjit.openstandby.widget.clock.ClockWidgets

/**
 * The full standby display. Used by [io.github.kumarjit.openstandby.standby.StandbyActivity],
 * the screen-saver dream, and the live preview in settings.
 */
@Composable
fun StandbyScreen(
    settings: StandbySettings,
    notifications: List<StandbyNotification>,
    modifier: Modifier = Modifier,
    onClockTap: (() -> Unit)? = null,
    onCalendarTap: (() -> Unit)? = null,
) {
    val now = rememberNow()
    val calendar = rememberCalendarSnapshot(now.toLocalDate())
    val colors = remember(settings.accent, settings.nightMode) { standbyColors(settings.accent, settings.nightMode) }
    val visible = remember(notifications, settings) { notifications.important(settings) }

    val target = if (settings.burnInProtection) burnInOffset(now.hour * 60 + now.minute) else DpOffset.Zero
    val dx by animateDpAsState(target.x, tween(2_000), label = "burnInX")
    val dy by animateDpAsState(target.y, tween(2_000), label = "burnInY")

    CompositionLocalProvider(LocalStandbyColors provides colors) {
        BoxWithConstraints(modifier.fillMaxSize().background(Color.Black)) {
            val landscape = maxWidth > maxHeight
            val gap = minOf(maxWidth, maxHeight) * 0.06f
            val clockWidget = ClockWidgets.byId(settings.clockWidgetId)
            val calendarWidget = CalendarWidgets.byId(settings.calendarWidgetId)
            val clock: @Composable (Modifier) -> Unit = { m ->
                clockWidget.Content(clockStateFor(clockWidget, settings, now, colors), m.onTap(onClockTap))
            }
            val cal: @Composable (Modifier) -> Unit = { m ->
                calendarWidget.Content(
                    calendarStateFor(calendarWidget, settings, now, colors, calendar),
                    m.onTap(onCalendarTap),
                )
            }

            Column(Modifier.fillMaxSize().offset { IntOffset(dx.roundToPx(), dy.roundToPx()) }.padding(gap)) {
                val body = Modifier.fillMaxWidth().weight(1f)
                when (settings.displayMode) {
                    DisplayMode.CLOCK_ONLY -> clock(body)
                    DisplayMode.CALENDAR_ONLY -> cal(body)
                    DisplayMode.CLOCK_AND_CALENDAR -> if (landscape) {
                        Row(body, horizontalArrangement = Arrangement.spacedBy(gap)) {
                            clock(Modifier.weight(1f).fillMaxHeight())
                            cal(Modifier.weight(1f).fillMaxHeight())
                        }
                    } else {
                        Column(body, verticalArrangement = Arrangement.spacedBy(gap)) {
                            clock(Modifier.weight(1f).fillMaxWidth())
                            cal(Modifier.weight(1f).fillMaxWidth())
                        }
                    }
                }
                AnimatedVisibility(visible = visible.isNotEmpty()) {
                    NotificationTray(
                        items = visible,
                        showContent = settings.showNotificationContent,
                        modifier = Modifier.fillMaxWidth().padding(top = gap / 2).height(56.dp),
                    )
                }
            }
        }
    }
}

private fun Modifier.onTap(action: (() -> Unit)?): Modifier =
    if (action == null) this else pointerInput(action) { detectTapGestures(onTap = { action() }) }

/** A small, slowly wandering offset so static pixels don't burn into OLED panels. */
private fun burnInOffset(minuteOfDay: Int): DpOffset {
    val x = (minuteOfDay * 7) % 13 - 6
    val y = (minuteOfDay * 11) % 9 - 4
    return DpOffset(x.dp, y.dp)
}
