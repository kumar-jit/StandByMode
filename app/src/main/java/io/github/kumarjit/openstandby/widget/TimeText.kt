package io.github.kumarjit.openstandby.widget

import java.time.LocalTime

/** Pure formatting helpers for widgets (unit tested). */
object TimeText {

    private val HOUR_NAMES = listOf(
        "twelve", "one", "two", "three", "four", "five",
        "six", "seven", "eight", "nine", "ten", "eleven",
    )

    fun twoDigits(value: Int): String = value.toString().padStart(2, '0')

    fun hour(hour: Int, use24Hour: Boolean): String =
        if (use24Hour) twoDigits(hour) else (if (hour % 12 == 0) 12 else hour % 12).toString()

    fun amPm(hour: Int): String = if (hour < 12) "AM" else "PM"

    fun time(time: LocalTime, use24Hour: Boolean): String {
        val hm = "${hour(time.hour, use24Hour)}:${twoDigits(time.minute)}"
        return if (use24Hour) hm else "$hm ${amPm(time.hour)}"
    }

    /** Time rounded to the nearest five minutes, e.g. "quarter past ten". */
    fun inWords(hour: Int, minute: Int): String {
        val rounded = ((minute + 2) / 5) * 5
        val hourName = HOUR_NAMES[(if (rounded > 30) hour + 1 else hour) % 12]
        return when (rounded) {
            0, 60 -> "$hourName o'clock"
            15 -> "quarter past $hourName"
            30 -> "half past $hourName"
            45 -> "quarter to $hourName"
            in 5..25 -> "${minuteWord(rounded)} past $hourName"
            else -> "${minuteWord(60 - rounded)} to $hourName"
        }
    }

    private fun minuteWord(minutes: Int): String = when (minutes) {
        5 -> "five"
        10 -> "ten"
        20 -> "twenty"
        25 -> "twenty-five"
        else -> minutes.toString()
    }
}
