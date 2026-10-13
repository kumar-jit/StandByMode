# Writing a clock or calendar widget

Every clock face and calendar layout in OpenStandby is a **widget**: a small, self-contained
Kotlin object that implements one standard interface. You only need to know Jetpack Compose.
No Android services, permissions, or settings code.

```
widget/
├── StandbyWidget.kt          The contract: StandbyWidget<S>, WidgetOption, WidgetOptions, WidgetRegistry
├── TimeText.kt               Time formatting helpers
├── clock/
│   ├── ClockWidget.kt        ClockState + abstract ClockWidget
│   ├── ClockWidgets.kt       ← register clocks here
│   └── builtin/              Built-in clocks (DigitalClock.kt is the reference example)
├── calendar/
│   ├── CalendarWidget.kt     CalendarState + abstract CalendarWidget
│   ├── CalendarWidgets.kt    ← register calendars here
│   ├── CalendarKit.kt        EventRow, hints, event colours, time labels
│   └── builtin/              Built-in calendars (MonthCalendar.kt is the reference example)
└── preview/                  Android Studio preview helpers
```

## The contract

```kotlin
interface StandbyWidget<S> {
    val id: String                       // stable, unique, lowercase_snake_case
    val displayName: String              // shown in the gallery
    val author: String                   // your name / GitHub handle
    val description: String              // optional one-liner
    val options: List<WidgetOption>      // optional user settings

    @Composable
    fun Content(state: S, modifier: Modifier)
}

abstract class ClockWidget : StandbyWidget<ClockState>
abstract class CalendarWidget : StandbyWidget<CalendarState>
```

A widget is a **pure function of its state**. All input arrives in the state object, and the host app
takes care of ticking the clock, loading calendar events, reading settings, and night mode.

### `ClockState`

| Field | Meaning |
| --- | --- |
| `now: LocalDateTime` | Current time, updated every second |
| `use24Hour`, `showSeconds` | User preferences |
| `colors: StandbyColors` | `primary`, `secondary`, `accent`, `surface`, `monochrome` (night mode) |
| `options: WidgetOptions` | Values of *your* widget's options |
| `hourText`, `minuteText`, `secondText`, `amPm` | Ready-formatted strings (12/24h already applied) |

### `CalendarState`

| Field | Meaning |
| --- | --- |
| `now`, `today` | Current time and date |
| `events: List<CalendarEvent>` | This month + at least the next 8 days, sorted by start |
| `hasCalendarPermission` | `false` → show a hint instead of events |
| `eventDays: Set<LocalDate>` | Dates that have events (for dots) |
| `upcoming(days)` | Unfinished events starting within `days` days |
| `use24Hour`, `colors`, `options` | As above |
| `locale`, `firstDayOfWeek` | Use these for names and week layout |

## Step by step: a new clock

**1. Create a file** `widget/clock/builtin/NeonClock.kt` (community widgets are welcome in `builtin/` too):

```kotlin
object NeonClock : ClockWidget() {
    override val id = "neon"
    override val displayName = "Neon"
    override val author = "your-github-handle"
    override val description = "Glowing tube digits"

    private val glow = WidgetOption.Toggle("glow", "Glow effect", default = true)
    private val tube = WidgetOption.Choice("tube", "Tube colour", listOf("Accent", "White"), default = "Accent")
    override val options: List<WidgetOption> = listOf(glow, tube)

    @Composable
    override fun Content(state: ClockState, modifier: Modifier) {
        val color = if (state.options[tube] == "White") state.colors.primary else state.colors.accent
        FitText(
            text = "${state.hourText}:${state.minuteText}",
            color = color,
            modifier = modifier,
            fontWeight = if (state.options[glow]) FontWeight.Bold else FontWeight.Light,
        )
    }
}

@Preview(widthDp = 400, heightDp = 240)
@Composable
private fun NeonClockPreview() = ClockWidgetPreview(NeonClock)
```

**2. Register it.** Add one line to the end of the list in `ClockWidgets.kt`:

```kotlin
        WordsClock,
        // Community clocks go here ↓
        NeonClock,
```

**3. Done.** The settings screen now shows a live thumbnail of your clock in the gallery, and
your `Glow effect` switch and `Tube colour` chips appear when it's selected. Run the tests:

```bash
./gradlew testDebugUnitTest
```

`WidgetRegistryTest` checks every registered widget (valid unique id, author, option keys).

A calendar works the same way: extend `CalendarWidget`, register it in `CalendarWidgets.kt`, and preview it with `CalendarWidgetPreview`.

## Options

| Type | Settings UI | Read it with |
| --- | --- | --- |
| `WidgetOption.Toggle(key, label, default)` | Switch | `state.options[toggle]` → `Boolean` |
| `WidgetOption.Choice(key, label, choices, default)` | Chips | `state.options[choice]` → `String` |

Values are saved per widget. Keys only need to be unique within your widget. Saved values that are
missing or no longer valid automatically fall back to the default, so you can add or rename choices safely.

## Rules of thumb

1. **Only use `state.colors`.** Never hard-code colours (pure black backgrounds are fine). Night mode turns the palette red, and `colors.monochrome` tells you to avoid other colours (event colours, icons).
2. **Size relative to your bounds.** The same widget is drawn as a 140dp thumbnail, half a screen, or a full tablet. Use `FitText`, `BoxWithConstraints`, `weight`, or `Canvas` `size`, never fixed `dp`/`sp` for the main content.
3. **Fill the `modifier`** you're given and don't add outer padding. The host already does.
4. **Stay pure.** No `LocalContext`, system services, network, disk, or `LocalDateTime.now()`. Everything you need is in the state. If something is missing, open an issue to add it to the state for everyone.
5. **Mind OLED burn-in.** Avoid large, static, bright areas. The host shifts content slightly every minute, and dark designs help.
6. **Never change an `id`** after it ships. Users' saved selections reference it.
7. Calendar widgets: handle `!hasCalendarPermission` and "no events". `showCalendarEmptyHint(...)` does both.

## Checklist for a widget PR

- [ ] One new file (plus one registry line), with an `@Preview`
- [ ] Looks right as a thumbnail, half-screen and full-screen, portrait and landscape
- [ ] Looks right in night mode (`ClockWidgetPreview(MyClock, night = true)`)
- [ ] `./gradlew assembleDebug lintDebug testDebugUnitTest` passes
- [ ] Screenshot in the PR description
