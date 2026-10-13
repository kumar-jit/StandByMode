package io.github.kumarjit.openstandby.widget.clock.builtin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import io.github.kumarjit.openstandby.ui.common.FitText
import io.github.kumarjit.openstandby.widget.clock.ClockState
import io.github.kumarjit.openstandby.widget.clock.ClockWidget

object MinimalClock : ClockWidget() {
    override val id = "minimal"
    override val displayName = "Minimal"
    override val author = "OpenStandby"
    override val description = "Thin stacked hours and minutes"

    @Composable
    override fun Content(state: ClockState, modifier: Modifier) {
        Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            FitText(
                state.hourText.padStart(2, '0'),
                state.colors.accent,
                Modifier.fillMaxWidth().weight(1f),
                fontWeight = FontWeight.Thin,
                charWidth = 0.55f,
                lineFill = 1f,
            )
            FitText(
                state.minuteText,
                state.colors.primary,
                Modifier.fillMaxWidth().weight(1f),
                fontWeight = FontWeight.Thin,
                charWidth = 0.55f,
                lineFill = 1f,
            )
        }
    }
}
