package io.github.kumarjit.openstandby.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.kumarjit.openstandby.data.StandbySettings
import io.github.kumarjit.openstandby.widget.calendar.CalendarWidgets
import io.github.kumarjit.openstandby.widget.clock.ClockState
import io.github.kumarjit.openstandby.widget.clock.ClockWidget
import io.github.kumarjit.openstandby.widget.clock.ClockWidgets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards contributions: every registered widget must have a valid, unique id and
 * well-formed options. If you added a widget and this fails, the message says why.
 */
class WidgetRegistryTest {

    @Test
    fun builtInRegistriesAreValid() {
        // Touching the registries runs their validation.
        assertTrue(ClockWidgets.all.isNotEmpty())
        assertTrue(CalendarWidgets.all.isNotEmpty())
        val all = ClockWidgets.all + CalendarWidgets.all
        all.forEach { widget ->
            assertTrue("${widget.id} needs an author", widget.author.isNotBlank())
        }
    }

    @Test
    fun defaultsMatchSettingsDefaults() {
        assertEquals(StandbySettings().clockWidgetId, ClockWidgets.default.id)
        assertEquals(StandbySettings().calendarWidgetId, CalendarWidgets.default.id)
    }

    @Test
    fun unknownIdFallsBackToDefault() {
        assertEquals(ClockWidgets.default, ClockWidgets.byId("removed_widget"))
        assertEquals(ClockWidgets.default, ClockWidgets.byId(null))
    }

    @Test
    fun nextIdWrapsAround() {
        val ids = ClockWidgets.all.map { it.id }
        assertEquals(ids[1], ClockWidgets.nextId(ids[0]))
        assertEquals(ids[0], ClockWidgets.nextId(ids.last()))
        assertEquals(ids[0], ClockWidgets.nextId("unknown"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun duplicateIdsAreRejected() {
        WidgetRegistry(listOf(FakeClock("same"), FakeClock("same")))
    }

    @Test(expected = IllegalArgumentException::class)
    fun badIdsAreRejected() {
        WidgetRegistry(listOf(FakeClock("Not Valid")))
    }

    @Test(expected = IllegalArgumentException::class)
    fun choiceDefaultMustBeAChoice() {
        WidgetOption.Choice("size", "Size", listOf("S", "M"), default = "XL")
    }

    @Test
    fun optionsFallBackToDefaultsAndReadSavedValues() {
        val toggle = WidgetOption.Toggle("glow", "Glow", default = true)
        val choice = WidgetOption.Choice("size", "Size", listOf("S", "M", "L"), default = "M")
        val clock = FakeClock("neon", listOf(toggle, choice))

        val empty = StandbySettings().optionsFor(clock)
        assertTrue(empty[toggle])
        assertEquals("M", empty[choice])

        val saved = StandbySettings(
            widgetOptions = mapOf(
                optionStorageKey(clock, toggle) to "false",
                optionStorageKey(clock, choice) to "L",
                "other_widget.glow" to "true",
            ),
        ).optionsFor(clock)
        assertFalse(saved[toggle])
        assertEquals("L", saved[choice])

        val invalid = StandbySettings(widgetOptions = mapOf("neon.size" to "XXL")).optionsFor(clock)
        assertEquals("M", invalid[choice])
    }

    private class FakeClock(
        override val id: String,
        override val options: List<WidgetOption> = emptyList(),
    ) : ClockWidget() {
        override val displayName = "Fake"
        override val author = "test"

        @Composable
        override fun Content(state: ClockState, modifier: Modifier) = Unit
    }
}
