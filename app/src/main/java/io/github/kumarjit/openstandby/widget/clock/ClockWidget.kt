package io.github.kumarjit.openstandby.widget.clock

import androidx.compose.runtime.Immutable
import io.github.kumarjit.openstandby.data.AccentColor
import io.github.kumarjit.openstandby.ui.theme.StandbyColors
import io.github.kumarjit.openstandby.ui.theme.standbyColors
import io.github.kumarjit.openstandby.widget.StandbyWidget
import io.github.kumarjit.openstandby.widget.TimeText
import io.github.kumarjit.openstandby.widget.WidgetOptions
import java.time.LocalDateTime

/**
 * Everything a clock face receives. Ticks once per second.
 *
 * @property now current local time
 * @property use24Hour the user's 12/24-hour preference; [hourText] already applies it
 * @property showSeconds whether the user wants seconds (faces may ignore it if it doesn't fit the design)
 * @property colors palette to draw with; respects accent colour and night mode
 * @property options values of this widget's own [StandbyWidget.options]
 */
@Immutable
data class ClockState(
    val now: LocalDateTime,
    val use24Hour: Boolean,
    val showSeconds: Boolean,
    val colors: StandbyColors,
    val options: WidgetOptions = WidgetOptions.Defaults,
) {
    /** "9" / "12" in 12-hour mode, "09" / "21" in 24-hour mode. */
    val hourText: String get() = TimeText.hour(now.hour, use24Hour)
    val minuteText: String get() = TimeText.twoDigits(now.minute)
    val secondText: String get() = TimeText.twoDigits(now.second)

    /** "AM"/"PM" in 12-hour mode, null in 24-hour mode. */
    val amPm: String? get() = if (use24Hour) null else TimeText.amPm(now.hour)

    companion object {
        /** Fixed sample for previews and screenshots: 10:09:30, the classic watch-ad time. */
        fun sample(
            colors: StandbyColors = standbyColors(AccentColor.ORANGE, night = false),
            options: WidgetOptions = WidgetOptions.Defaults,
        ) = ClockState(
            now = LocalDateTime.of(2026, 1, 15, 10, 9, 30),
            use24Hour = false,
            showSeconds = true,
            colors = colors,
            options = options,
        )
    }
}

/**
 * Base class for clock faces. Extend it with a Kotlin `object` and register it in [ClockWidgets]:
 *
 * ```
 * object NeonClock : ClockWidget() {
 *     override val id = "neon"
 *     override val displayName = "Neon"
 *     override val author = "your-github-handle"
 *
 *     @Composable
 *     override fun Content(state: ClockState, modifier: Modifier) {
 *         FitText("${state.hourText}:${state.minuteText}", state.colors.accent, modifier)
 *     }
 * }
 * ```
 */
abstract class ClockWidget : StandbyWidget<ClockState>
