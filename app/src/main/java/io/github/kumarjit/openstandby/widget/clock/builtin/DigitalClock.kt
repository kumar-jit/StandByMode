package io.github.kumarjit.openstandby.widget.clock.builtin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import io.github.kumarjit.openstandby.ui.common.FitText
import io.github.kumarjit.openstandby.widget.WidgetOption
import io.github.kumarjit.openstandby.widget.clock.ClockState
import io.github.kumarjit.openstandby.widget.clock.ClockWidget
import io.github.kumarjit.openstandby.widget.preview.ClockWidgetPreview

/** Reference implementation: copy this file to start a new clock face. */
object DigitalClock : ClockWidget() {
    override val id = "digital"
    override val displayName = "Digital"
    override val author = "OpenStandby"
    override val description = "Big, bold digits"

    private val font = WidgetOption.Choice("font", "Font", listOf("Sans", "Serif", "Mono"), default = "Sans")
    override val options: List<WidgetOption> = listOf(font)

    @Composable
    override fun Content(state: ClockState, modifier: Modifier) {
        val family = when (state.options[font]) {
            "Serif" -> FontFamily.Serif
            "Mono" -> FontFamily.Monospace
            else -> FontFamily.SansSerif
        }
        val text = buildString {
            append(state.hourText).append(':').append(state.minuteText)
            if (state.showSeconds) append(':').append(state.secondText)
        }
        Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            FitText(
                text,
                state.colors.primary,
                Modifier.fillMaxWidth().weight(1f),
                fontWeight = FontWeight.SemiBold,
                fontFamily = family,
            )
            state.amPm?.let { FitText(it, state.colors.accent, Modifier.fillMaxWidth().weight(0.18f), fontFamily = family) }
        }
    }
}

@Preview(widthDp = 400, heightDp = 240)
@Composable
private fun DigitalClockPreview() = ClockWidgetPreview(DigitalClock)
