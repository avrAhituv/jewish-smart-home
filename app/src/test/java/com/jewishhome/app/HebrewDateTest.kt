package com.jewishhome.app

import com.jewishhome.app.domain.model.HebrewDate
import com.jewishhome.app.domain.model.HebrewDayOfWeek
import com.jewishhome.app.domain.model.HebrewMonth
import org.junit.Assert.*
import org.junit.Test

class HebrewDateTest {

    @Test
    fun `formatHebrewDay returns correct Hebrew numeral for single digit`() {
        assertEquals("א", HebrewDate.formatHebrewDay(1))
        assertEquals("ה", HebrewDate.formatHebrewDay(5))
        assertEquals("ט", HebrewDate.formatHebrewDay(9))
    }

    @Test
    fun `formatHebrewDay returns correct Hebrew numeral for double digit`() {
        assertEquals("י", HebrewDate.formatHebrewDay(10))
        assertEquals("ט״ו", HebrewDate.formatHebrewDay(15))
        assertEquals("ט״ז", HebrewDate.formatHebrewDay(16))
        assertEquals("כ", HebrewDate.formatHebrewDay(20))
        assertEquals("כ״ח", HebrewDate.formatHebrewDay(28))
        assertEquals("ל", HebrewDate.formatHebrewDay(30))
    }

    @Test
    fun `formatHebrewYear returns correct format`() {
        val year5785 = HebrewDate.formatHebrewYear(5785)
        assertTrue(year5785.contains("תשפ"))
    }

    @Test
    fun `toHebrewString returns formatted date`() {
        val hebrewDate = HebrewDate(
            day = 15,
            month = HebrewMonth.KISLEV,
            year = 5785,
            dayOfWeek = HebrewDayOfWeek.SUNDAY
        )

        val result = hebrewDate.toHebrewString()
        assertTrue(result.contains("ט״ו"))
        assertTrue(result.contains("כסלו"))
    }

    @Test
    fun `toFullHebrewString includes day of week`() {
        val hebrewDate = HebrewDate(
            day = 1,
            month = HebrewMonth.TISHREI,
            year = 5785,
            dayOfWeek = HebrewDayOfWeek.SHABBAT
        )

        val result = hebrewDate.toFullHebrewString()
        assertTrue(result.contains("שבת"))
    }

    @Test
    fun `HebrewMonth fromKosherJava returns correct month`() {
        assertEquals(HebrewMonth.TISHREI, HebrewMonth.fromKosherJava(7, false))
        assertEquals(HebrewMonth.KISLEV, HebrewMonth.fromKosherJava(9, false))
        assertEquals(HebrewMonth.ADAR, HebrewMonth.fromKosherJava(12, false))
        assertEquals(HebrewMonth.ADAR_I, HebrewMonth.fromKosherJava(12, true))
        assertEquals(HebrewMonth.ADAR_II, HebrewMonth.fromKosherJava(13, true))
    }

    @Test
    fun `HebrewDayOfWeek fromDayOfWeek returns correct day`() {
        assertEquals(
            HebrewDayOfWeek.SUNDAY,
            HebrewDayOfWeek.fromDayOfWeek(java.time.DayOfWeek.SUNDAY)
        )
        assertEquals(
            HebrewDayOfWeek.SHABBAT,
            HebrewDayOfWeek.fromDayOfWeek(java.time.DayOfWeek.SATURDAY)
        )
    }
}
