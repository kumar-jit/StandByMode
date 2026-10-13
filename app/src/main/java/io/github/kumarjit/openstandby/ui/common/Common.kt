package io.github.kumarjit.openstandby.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay
import java.time.LocalDateTime

/** Current time, updated on every second boundary. */
@Composable
fun rememberNow(): LocalDateTime {
    val now by produceState(initialValue = LocalDateTime.now()) {
        while (true) {
            value = LocalDateTime.now()
            delay(1_000L - System.currentTimeMillis() % 1_000L)
        }
    }
    return now
}

/**
 * Single-line text scaled to fill the available space.
 * [charWidth] is the average glyph width as a fraction of the font size.
 */
@Composable
fun FitText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.Bold,
    fontFamily: FontFamily? = null,
    charWidth: Float = 0.6f,
    lineFill: Float = 0.85f,
    textAlign: TextAlign = TextAlign.Center,
) {
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val byWidth = maxWidth / (text.length.coerceAtLeast(1) * charWidth)
        val byHeight = maxHeight * lineFill
        val size = when {
            !constraints.hasBoundedHeight -> byWidth
            !constraints.hasBoundedWidth -> byHeight
            else -> minOf(byWidth, byHeight)
        }
        val fontSize = with(LocalDensity.current) { size.toSp() }
        Text(
            text = text,
            color = color,
            fontSize = fontSize,
            lineHeight = fontSize,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            textAlign = textAlign,
            maxLines = 1,
            softWrap = false,
            style = TextStyle(fontFeatureSettings = "tnum"),
        )
    }
}

/** Launcher icon of [packageName], optionally flattened to a single [tint]. */
@Composable
fun AppIcon(packageName: String, modifier: Modifier = Modifier, tint: Color? = null) {
    val context = LocalContext.current
    val bitmap = remember(packageName) {
        runCatching {
            context.packageManager.getApplicationIcon(packageName).toBitmap(96, 96).asImageBitmap()
        }.getOrNull()
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = modifier,
            colorFilter = tint?.let { ColorFilter.tint(it) },
        )
    } else {
        Box(modifier.clip(CircleShape).background(Color.DarkGray))
    }
}
