package io.github.kumarjit.openstandby.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import io.github.kumarjit.openstandby.data.AccentColor

/** Palette used by every standby face; read it with `LocalStandbyColors.current`. */
@Immutable
data class StandbyColors(
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val surface: Color,
    /** True in night mode: faces should avoid any non-red color (app icons, event colors). */
    val monochrome: Boolean,
)

fun standbyColors(accent: AccentColor, night: Boolean): StandbyColors =
    if (night) {
        StandbyColors(
            primary = Color(0xFFE53935),
            secondary = Color(0xFF8E2A26),
            accent = Color(0xFFFF5252),
            surface = Color(0xFF1F0605),
            monochrome = true,
        )
    } else {
        StandbyColors(
            primary = Color.White,
            secondary = Color(0xFF8E8E93),
            accent = Color(accent.argb),
            surface = Color(0xFF1C1C1E),
            monochrome = false,
        )
    }

val LocalStandbyColors = staticCompositionLocalOf { standbyColors(AccentColor.ORANGE, night = false) }

@Composable
fun OpenStandbyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFFF9F0A),
            onPrimary = Color.Black,
            background = Color.Black,
            surface = Color.Black,
        ),
        content = content,
    )
}
