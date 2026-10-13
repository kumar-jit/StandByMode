package io.github.kumarjit.openstandby.widget

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime

class TimeTextTest {

    @Test
    fun hourFormatting() {
        assertEquals("12", TimeText.hour(0, use24Hour = false))
        assertEquals("00", TimeText.hour(0, use24Hour = true))
        assertEquals("1", TimeText.hour(13, use24Hour = false))
        assertEquals("13", TimeText.hour(13, use24Hour = true))
        assertEquals("12", TimeText.hour(12, use24Hour = false))
    }

    @Test
    fun timeFormatting() {
        assertEquals("9:05 AM", TimeText.time(LocalTime.of(9, 5), use24Hour = false))
        assertEquals("21:05", TimeText.time(LocalTime.of(21, 5), use24Hour = true))
    }

    @Test
    fun timeInWords() {
        assertEquals("ten o'clock", TimeText.inWords(10, 1))
        assertEquals("quarter past ten", TimeText.inWords(10, 14))
        assertEquals("twenty-five past ten", TimeText.inWords(10, 26))
        assertEquals("half past ten", TimeText.inWords(10, 30))
        assertEquals("twenty to eleven", TimeText.inWords(10, 40))
        assertEquals("quarter to twelve", TimeText.inWords(23, 45))
        assertEquals("twelve o'clock", TimeText.inWords(11, 58))
    }
}
