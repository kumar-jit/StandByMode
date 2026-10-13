package io.github.kumarjit.openstandby.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.kumarjit.openstandby.data.StandbySettings
import io.github.kumarjit.openstandby.ui.common.rememberNow
import io.github.kumarjit.openstandby.ui.standby.calendarStateFor
import io.github.kumarjit.openstandby.ui.standby.clockStateFor
import io.github.kumarjit.openstandby.ui.standby.rememberCalendarSnapshot
import io.github.kumarjit.openstandby.ui.theme.LocalStandbyColors
import io.github.kumarjit.openstandby.ui.theme.standbyColors
import io.github.kumarjit.openstandby.widget.StandbyWidget
import io.github.kumarjit.openstandby.widget.WidgetOption
import io.github.kumarjit.openstandby.widget.calendar.CalendarWidgets
import io.github.kumarjit.openstandby.widget.clock.ClockWidgets
import io.github.kumarjit.openstandby.widget.optionStorageKey
import io.github.kumarjit.openstandby.widget.optionsFor

/*
 * Settings UI for widgets. Everything here is generated from the registries, so a newly
 * contributed widget shows up with a live thumbnail and its options without any UI work.
 */

@Composable
fun ClockWidgetPicker(settings: StandbySettings, update: ((StandbySettings) -> StandbySettings) -> Unit) {
    val now = rememberNow()
    val colors = remember(settings.accent, settings.nightMode) { standbyColors(settings.accent, settings.nightMode) }
    WidgetPicker(
        widgets = ClockWidgets.all,
        selectedId = ClockWidgets.byId(settings.clockWidgetId).id,
        settings = settings,
        update = update,
        onSelect = { id -> update { it.copy(clockWidgetId = id) } },
    ) { widget, modifier ->
        CompositionLocalProvider(LocalStandbyColors provides colors) {
            val clock = ClockWidgets.byId(widget.id)
            clock.Content(clockStateFor(clock, settings, now, colors), modifier)
        }
    }
}

@Composable
fun CalendarWidgetPicker(settings: StandbySettings, update: ((StandbySettings) -> StandbySettings) -> Unit) {
    val now = rememberNow()
    val calendar = rememberCalendarSnapshot(now.toLocalDate())
    val colors = remember(settings.accent, settings.nightMode) { standbyColors(settings.accent, settings.nightMode) }
    WidgetPicker(
        widgets = CalendarWidgets.all,
        selectedId = CalendarWidgets.byId(settings.calendarWidgetId).id,
        settings = settings,
        update = update,
        onSelect = { id -> update { it.copy(calendarWidgetId = id) } },
    ) { widget, modifier ->
        CompositionLocalProvider(LocalStandbyColors provides colors) {
            val cal = CalendarWidgets.byId(widget.id)
            cal.Content(calendarStateFor(cal, settings, now, colors, calendar), modifier)
        }
    }
}

/** Horizontal gallery of live thumbnails, plus the description and options of the selected widget. */
@Composable
private fun WidgetPicker(
    widgets: List<StandbyWidget<*>>,
    selectedId: String,
    settings: StandbySettings,
    update: ((StandbySettings) -> StandbySettings) -> Unit,
    onSelect: (String) -> Unit,
    thumbnail: @Composable (StandbyWidget<*>, Modifier) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(widgets, key = { it.id }) { widget ->
            val selected = widget.id == selectedId
            Column(
                Modifier
                    .width(140.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelect(widget.id) }
                    .semantics {
                        this.selected = selected
                        role = Role.RadioButton
                    },
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black)
                        .border(
                            width = if (selected) 3.dp else 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(14.dp),
                        )
                        .padding(10.dp),
                ) {
                    thumbnail(widget, Modifier.fillMaxSize())
                }
                Text(
                    widget.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp, start = 4.dp),
                )
                Text(
                    "by ${widget.author}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                )
            }
        }
    }

    val selectedWidget = widgets.firstOrNull { it.id == selectedId } ?: return
    if (selectedWidget.description.isNotBlank()) {
        Text(
            selectedWidget.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    WidgetOptionsEditor(selectedWidget, settings, update)
}

/** Renders a [StandbyWidget]'s declared [WidgetOption]s as switches and chips. */
@Composable
private fun WidgetOptionsEditor(
    widget: StandbyWidget<*>,
    settings: StandbySettings,
    update: ((StandbySettings) -> StandbySettings) -> Unit,
) {
    val values = settings.optionsFor(widget)
    fun set(option: WidgetOption, value: String) = update {
        it.copy(widgetOptions = it.widgetOptions + (optionStorageKey(widget, option) to value))
    }
    widget.options.forEach { option ->
        when (option) {
            is WidgetOption.Toggle -> SwitchRow(option.label, values[option]) { set(option, it.toString()) }
            is WidgetOption.Choice -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(option.label, style = MaterialTheme.typography.bodyLarge)
                ChoiceChips(option.choices, values[option], { it }) { set(option, it) }
            }
        }
    }
}
