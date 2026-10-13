package io.github.kumarjit.openstandby.widget.clock.builtin

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import io.github.kumarjit.openstandby.widget.TimeText
import io.github.kumarjit.openstandby.widget.clock.ClockState
import io.github.kumarjit.openstandby.widget.clock.ClockWidget

object WordsClock : ClockWidget() {
    override val id = "words"
    override val displayName = "Words"
    override val author = "OpenStandby"
    override val description = "\"it's quarter past ten\""

    @Composable
    override fun Content(state: ClockState, modifier: Modifier) {
        val c = state.colors
        val hour = state.now.hour
        val minute = state.now.minute
        val lines = remember(hour, minute) { listOf("it's") + TimeText.inWords(hour, minute).split(" ") }
        BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
            val longest = lines.maxOf { it.length }.coerceAtLeast(4)
            val size = minOf(maxWidth / (longest * 0.58f), maxHeight / (lines.size * 1.15f))
            val fontSize = with(LocalDensity.current) { size.toSp() }
            Column(horizontalAlignment = Alignment.Start) {
                lines.forEachIndexed { i, line ->
                    Text(
                        text = line,
                        color = when (i) {
                            0 -> c.secondary
                            lines.lastIndex -> c.accent
                            else -> c.primary
                        },
                        fontSize = fontSize,
                        lineHeight = fontSize * 1.1f,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
    }
}
