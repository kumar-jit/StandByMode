package io.github.kumarjit.openstandby.ui.standby

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kumarjit.openstandby.notifications.StandbyNotification
import io.github.kumarjit.openstandby.ui.common.AppIcon
import io.github.kumarjit.openstandby.ui.theme.LocalStandbyColors

private const val MAX_VISIBLE = 3

@Composable
fun NotificationTray(items: List<StandbyNotification>, showContent: Boolean, modifier: Modifier = Modifier) {
    val c = LocalStandbyColors.current
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        items.take(MAX_VISIBLE).forEach { NotificationPill(it, showContent, Modifier.weight(1f).fillMaxHeight()) }
        if (items.size > MAX_VISIBLE) {
            Text("+${items.size - MAX_VISIBLE}", color = c.secondary, fontSize = 14.sp)
        }
    }
}

@Composable
private fun NotificationPill(item: StandbyNotification, showContent: Boolean, modifier: Modifier) {
    val c = LocalStandbyColors.current
    Row(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(c.surface)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(item.packageName, Modifier.size(28.dp), tint = if (c.monochrome) c.primary else null)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = if (showContent) item.title.ifBlank { item.appName } else item.appName,
                color = c.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (showContent) item.text else "New notification",
                color = c.secondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
