package io.github.kumarjit.openstandby.widget.clock.builtin

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kumarjit.openstandby.ui.common.FitText
import io.github.kumarjit.openstandby.ui.theme.StandbyColors
import io.github.kumarjit.openstandby.widget.clock.ClockState
import io.github.kumarjit.openstandby.widget.clock.ClockWidget

object FlipClock : ClockWidget() {
    override val id = "flip"
    override val displayName = "Flip"
    override val author = "OpenStandby"
    override val description = "Retro flip cards that animate each minute"

    @Composable
    override fun Content(state: ClockState, modifier: Modifier) {
        Row(
            modifier,
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val card = Modifier.weight(1f).fillMaxHeight(0.8f)
            FlipCard(state.hourText.padStart(2, '0'), state.amPm, state.colors, card)
            FlipCard(state.minuteText, null, state.colors, card)
        }
    }

    @Composable
    private fun FlipCard(value: String, label: String?, c: StandbyColors, modifier: Modifier) {
        Box(modifier.clip(RoundedCornerShape(16.dp)).background(c.surface), contentAlignment = Alignment.Center) {
            AnimatedContent(
                targetState = value,
                transitionSpec = {
                    (slideInVertically { -it } + fadeIn()) togetherWith (slideOutVertically { it } + fadeOut())
                },
                modifier = Modifier.fillMaxSize(),
                label = "flip",
            ) { digits ->
                FitText(digits, c.primary, Modifier.fillMaxSize().padding(8.dp), charWidth = 0.62f, lineFill = 0.75f)
            }
            // The "hinge" across the middle of the card.
            Box(Modifier.fillMaxWidth().height(3.dp).background(Color.Black))
            if (label != null) {
                Text(label, color = c.accent, fontSize = 12.sp, modifier = Modifier.align(Alignment.TopStart).padding(10.dp))
            }
        }
    }
}
