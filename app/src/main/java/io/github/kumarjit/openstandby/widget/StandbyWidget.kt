package io.github.kumarjit.openstandby.widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import io.github.kumarjit.openstandby.data.StandbySettings

/**
 * The contract every standby widget implements: clock faces ([clock.ClockWidget])
 * and calendar faces ([calendar.CalendarWidget]).
 *
 * A widget is a stateless drawing of [S]: everything it needs (time, events, colours,
 * user settings, its own options) arrives in the state object, so a widget never reads
 * system services, settings storage or the clock by itself. That keeps widgets easy to
 * write, preview and test, and lets the same widget render full-screen, in split view
 * and as a thumbnail in the settings gallery.
 */
interface StandbyWidget<S> {

    /**
     * Stable unique id, saved in the user's settings. Use `lowercase_snake_case` and
     * never change it after release, or users who picked the widget lose their choice.
     */
    val id: String

    /** Short name shown in the settings gallery, e.g. "Flip". */
    val displayName: String

    /** Who made it, e.g. your GitHub handle. Shown under the thumbnail. */
    val author: String

    /** One-line description shown when the widget is selected. */
    val description: String get() = ""

    /**
     * Options the user can tweak for this widget. Settings renders them automatically;
     * read the current values from the state's `options`.
     */
    val options: List<WidgetOption> get() = emptyList()

    /**
     * Draws the widget. Fill [modifier]'s bounds and size everything relative to them:
     * the available space ranges from a 150dp thumbnail to a full tablet screen.
     */
    @Composable
    fun Content(state: S, modifier: Modifier)
}

/** A user-tweakable widget setting. [key] only has to be unique within one widget. */
sealed interface WidgetOption {
    val key: String
    val label: String

    data class Toggle(
        override val key: String,
        override val label: String,
        val default: Boolean,
    ) : WidgetOption

    data class Choice(
        override val key: String,
        override val label: String,
        val choices: List<String>,
        val default: String,
    ) : WidgetOption {
        init {
            require(default in choices) { "Default '$default' of option '$key' is not one of $choices" }
        }
    }
}

/** Current values of one widget's [WidgetOption]s; missing or invalid values fall back to the default. */
@Immutable
class WidgetOptions(private val values: Map<String, String> = emptyMap()) {

    operator fun get(option: WidgetOption.Toggle): Boolean =
        values[option.key]?.toBooleanStrictOrNull() ?: option.default

    operator fun get(option: WidgetOption.Choice): String =
        values[option.key]?.takeIf { it in option.choices } ?: option.default

    override fun equals(other: Any?) = other is WidgetOptions && other.values == values
    override fun hashCode() = values.hashCode()

    companion object {
        val Defaults = WidgetOptions()
    }
}

/** The user's saved option values for [widget]. */
fun StandbySettings.optionsFor(widget: StandbyWidget<*>): WidgetOptions {
    val prefix = "${widget.id}."
    return WidgetOptions(
        widgetOptions.filterKeys { it.startsWith(prefix) }.mapKeys { it.key.removePrefix(prefix) },
    )
}

/** Storage key used in [StandbySettings.widgetOptions]. */
fun optionStorageKey(widget: StandbyWidget<*>, option: WidgetOption): String = "${widget.id}.${option.key}"

/**
 * An ordered list of widgets of one kind. The first entry is the default.
 * Validates ids up front, so a bad contribution fails fast (and in unit tests).
 */
open class WidgetRegistry<W : StandbyWidget<*>>(val all: List<W>) {

    init {
        require(all.isNotEmpty()) { "A widget registry needs at least one widget" }
        all.forEach { w ->
            require(ID_PATTERN.matches(w.id)) { "Widget id '${w.id}' must be lowercase_snake_case" }
            require(w.displayName.isNotBlank()) { "Widget '${w.id}' needs a displayName" }
            val keys = w.options.map { it.key }
            require(keys.size == keys.toSet().size) { "Widget '${w.id}' has duplicate option keys" }
        }
        val duplicates = all.groupBy { it.id }.filterValues { it.size > 1 }.keys
        require(duplicates.isEmpty()) { "Duplicate widget ids: $duplicates" }
    }

    val default: W get() = all.first()

    /** The widget with [id], or [default] if it was removed or never existed. */
    fun byId(id: String?): W = all.firstOrNull { it.id == id } ?: default

    /** Id of the widget after [id], wrapping around; used for tap-to-cycle. */
    fun nextId(id: String?): String {
        val index = all.indexOfFirst { it.id == id }
        return all[(index + 1) % all.size].id
    }

    private companion object {
        val ID_PATTERN = Regex("[a-z][a-z0-9_]*")
    }
}
