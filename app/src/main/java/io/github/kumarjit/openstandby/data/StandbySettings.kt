package io.github.kumarjit.openstandby.data

/** What the standby screen shows. */
enum class DisplayMode(val label: String) {
    CLOCK_AND_CALENDAR("Both"),
    CLOCK_ONLY("Clock"),
    CALENDAR_ONLY("Calendar"),
}

enum class AccentColor(val label: String, val argb: Long) {
    ORANGE("Orange", 0xFFFF9F0A),
    BLUE("Blue", 0xFF0A84FF),
    GREEN("Green", 0xFF30D158),
    PINK("Pink", 0xFFFF375F),
    PURPLE("Purple", 0xFFBF5AF2),
    WHITE("White", 0xFFFFFFFF),
}

data class StandbySettings(
    val displayMode: DisplayMode = DisplayMode.CLOCK_AND_CALENDAR,
    /** Id of the selected clock widget (see `ClockWidgets`). Unknown ids fall back to the default. */
    val clockWidgetId: String = "digital",
    /** Id of the selected calendar widget (see `CalendarWidgets`). */
    val calendarWidgetId: String = "month",
    /** Per-widget option values, keyed `"<widgetId>.<optionKey>"`. */
    val widgetOptions: Map<String, String> = emptyMap(),
    val accent: AccentColor = AccentColor.ORANGE,
    val use24Hour: Boolean = false,
    val showSeconds: Boolean = false,
    /** Dim red-only palette for dark bedrooms. */
    val nightMode: Boolean = false,
    /** Nudges the content a few dp every minute to avoid OLED burn-in. */
    val burnInProtection: Boolean = true,
    val showNotifications: Boolean = true,
    /** When false only the app name is shown, since the phone is usually locked. */
    val showNotificationContent: Boolean = false,
    /** Apps whose notifications are always treated as important. */
    val priorityApps: Set<String> = emptySet(),
    val autoLaunchOnCharge: Boolean = false,
    /** Also open standby when the screen locks while already charging. */
    val launchOnLock: Boolean = true,
)
