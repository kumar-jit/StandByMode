package io.github.kumarjit.openstandby.widget.clock.builtin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import io.github.kumarjit.openstandby.widget.WidgetOption
import io.github.kumarjit.openstandby.widget.clock.ClockState
import io.github.kumarjit.openstandby.widget.clock.ClockWidget
import kotlin.math.cos
import kotlin.math.sin

object AnalogClock : ClockWidget() {
    override val id = "analog"
    override val displayName = "Analog"
    override val author = "OpenStandby"
    override val description = "Classic watch face with hands"

    private val minuteTicks = WidgetOption.Toggle("minute_ticks", "Minute ticks", default = true)
    override val options: List<WidgetOption> = listOf(minuteTicks)

    @Composable
    override fun Content(state: ClockState, modifier: Modifier) {
        val c = state.colors
        val now = state.now
        val showMinuteTicks = state.options[minuteTicks]
        Canvas(modifier.fillMaxSize()) {
            val r = size.minDimension / 2f * 0.95f
            drawCircle(c.surface, radius = r, center = center)
            for (i in 0 until 60) {
                val isHour = i % 5 == 0
                if (!isHour && !showMinuteTicks) continue
                val rad = Math.toRadians(i * 6.0 - 90.0)
                val outer = r * 0.94f
                val inner = if (isHour) r * 0.80f else r * 0.88f
                drawLine(
                    color = if (isHour) c.primary else c.secondary,
                    start = Offset(center.x + cos(rad).toFloat() * inner, center.y + sin(rad).toFloat() * inner),
                    end = Offset(center.x + cos(rad).toFloat() * outer, center.y + sin(rad).toFloat() * outer),
                    strokeWidth = if (isHour) r * 0.025f else r * 0.008f,
                    cap = StrokeCap.Round,
                )
            }
            hand((now.hour % 12 + now.minute / 60f) * 30f, r * 0.5f, r * 0.06f, c.primary)
            hand((now.minute + now.second / 60f) * 6f, r * 0.78f, r * 0.04f, c.primary)
            if (state.showSeconds) hand(now.second * 6f, r * 0.85f, r * 0.015f, c.accent)
            drawCircle(c.accent, radius = r * 0.045f, center = center)
        }
    }

    private fun DrawScope.hand(angleDegrees: Float, length: Float, width: Float, color: Color) {
        val rad = Math.toRadians(angleDegrees - 90.0)
        val end = Offset(center.x + cos(rad).toFloat() * length, center.y + sin(rad).toFloat() * length)
        drawLine(color, center, end, strokeWidth = width, cap = StrokeCap.Round)
    }
}
