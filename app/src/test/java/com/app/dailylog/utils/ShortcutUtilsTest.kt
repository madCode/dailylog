package com.app.dailylog.utils

import com.app.dailylog.repository.Constants
import com.app.dailylog.repository.Shortcut
import com.app.dailylog.repository.ShortcutType
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import junit.framework.TestCase
import org.junit.Test
import org.mockito.Mockito

class ShortcutUtilsTest : TestCase() {

    fun testGetValueOfShortcut() {
        val buildMock = Mockito.mock(DetermineBuildInterface::class.java)
        Mockito.`when`(buildMock.isOreoOrGreater()).thenReturn(true)
        val clock = Clock.fixed(
            Instant.parse("2018-08-22T10:03:02Z"),
            ZoneOffset.UTC)
        val shortcut = Shortcut(
            label = "TEST",
            value = "hello darkness my old{DATETIME: HH:mm:ss} sldkfjsldfkj",
            cursorIndex = 0,
            position = 0,
            type = ShortcutType.DATETIME
        )
        val result = ShortcutUtils.getValueOfShortcut(shortcut, clock, buildMock)
        assertEquals("hello darkness my old10:03:02 sldkfjsldfkj", result)
    }


    @Test
    fun `test date properly formatted using default date string`() {
        val buildMock = Mockito.mock(DetermineBuildInterface::class.java)
        Mockito.`when`(buildMock.isOreoOrGreater()).thenReturn(true)
        val clock = Clock.fixed(
            Instant.parse("2018-08-22T10:00:00Z"),
            ZoneOffset.UTC)
        val shortcut = Shortcut(
            label = "TEST",
            value = "{DATETIME: ${Constants.DATE_TIME_DEFAULT_FORMAT}}",
            cursorIndex = 0,
            position = 0,
            type = ShortcutType.DATETIME
        )
        val result = ShortcutUtils.getValueOfShortcut(shortcut, clock, buildMock)
        assertEquals("Wed Aug-22-2018 10:00 AM UTC", result)
    }

    @Test
    fun `test date properly formatted with timezone`() {
        val buildMock = Mockito.mock(DetermineBuildInterface::class.java)
        Mockito.`when`(buildMock.isOreoOrGreater()).thenReturn(true)
        val format = "E LLL-dd-yyyy h:mm a z"
        val clock = Clock.fixed(
            Instant.parse("2018-08-22T10:00:00Z"),
            ZoneOffset.UTC)
        val shortcut = Shortcut(
            label = "TEST",
            value = "{DATETIME: ${format}}",
            cursorIndex = 0,
            position = 0,
            type = ShortcutType.DATETIME
        )
        val dateString = ShortcutUtils.getValueOfShortcut(shortcut, clock, buildMock)
        assertEquals("Wed Aug-22-2018 10:00 AM UTC", dateString)
    }

    @Test
    fun `test print error message when date can't be formatted`() {
        val buildMock = Mockito.mock(DetermineBuildInterface::class.java)
        Mockito.`when`(buildMock.isOreoOrGreater()).thenReturn(true)
        val format = "EEEEEE"
        val clock = Clock.fixed(
            Instant.parse("2018-08-22T10:00:00Z"),
            ZoneOffset.UTC)
        val shortcut = Shortcut(
            label = "TEST",
            value = "{DATETIME: ${format}}",
            cursorIndex = 0,
            position = 0,
            type = ShortcutType.DATETIME
        )
        val dateString = ShortcutUtils.getValueOfShortcut(shortcut, clock, buildMock)
        assertEquals(
            "Issue with date time string. Please change Date Format in settings " +
                    "screen. Error message: Too many pattern letters: E",
            dateString)
    }
}