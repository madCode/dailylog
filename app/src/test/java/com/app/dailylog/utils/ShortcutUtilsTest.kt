package com.app.dailylog.utils

import com.app.dailylog.repository.Constants
import com.app.dailylog.repository.Shortcut
import com.app.dailylog.repository.ShortcutType
import java.time.Clock
import java.time.Instant
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito
import java.time.ZoneId

class ShortcutUtilsTest {

    @Test
    fun testGetValueOfShortcut() {
        val buildMock = Mockito.mock(DetermineBuildInterface::class.java)
        Mockito.`when`(buildMock.isOreoOrGreater()).thenReturn(true)
        val clock = Clock.fixed(
            Instant.parse("2018-08-22T10:03:02Z"),
            ZoneId.of("UTC"))
        val shortcut = Shortcut(
            id = "test-id",
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
    fun testDateProperlyFormattedUsingDefaultDateString() {
        val buildMock = Mockito.mock(DetermineBuildInterface::class.java)
        Mockito.`when`(buildMock.isOreoOrGreater()).thenReturn(true)
        val clock = Clock.fixed(
            Instant.parse("2018-08-22T10:03:02Z"),
            ZoneId.of("UTC"))
        val shortcut = Shortcut(
            id = "test-id",
            label = "TEST",
            value = "{DATETIME: ${Constants.DATE_TIME_DEFAULT_FORMAT}}",
            cursorIndex = 0,
            position = 0,
            type = ShortcutType.DATETIME
        )
        val result = ShortcutUtils.getValueOfShortcut(shortcut, clock, buildMock)
        assertTrue("Result should contain the expected date format: $result",
            result.contains("Wed Aug-22-2018 10:03 AM"))
    }

    @Test
    fun testDateProperlyFormattedWithTimezone() {
        val buildMock = Mockito.mock(DetermineBuildInterface::class.java)
        Mockito.`when`(buildMock.isOreoOrGreater()).thenReturn(true)
        val format = "E LLL-dd-yyyy h:mm a z"
        val clock = Clock.fixed(
            Instant.parse("2018-08-22T10:00:00Z"),
            ZoneId.of("UTC"))
        val shortcut = Shortcut(
            id = "test-id",
            label = "TEST",
            value = "{DATETIME: ${format}}",
            cursorIndex = 0,
            position = 0,
            type = ShortcutType.DATETIME
        )
        val dateString = ShortcutUtils.getValueOfShortcut(shortcut, clock, buildMock)
        assertTrue("Result should contain the expected date format: $dateString",
            dateString.contains("Wed Aug-22-2018 10:00 AM"))
    }

    @Test
    fun testPrintErrorMessageWhenDateCantBeFormatted() {
        val buildMock = Mockito.mock(DetermineBuildInterface::class.java)
        Mockito.`when`(buildMock.isOreoOrGreater()).thenReturn(true)
        val format = "EEEEEE"
        val clock = Clock.fixed(
            Instant.parse("2018-08-22T10:00:00Z"),
            ZoneId.of("UTC"))
        val shortcut = Shortcut(
            id = "test-id",
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

    @Test
    fun testGetValueOfShortcutWithTEXTType() {
        val buildMock = Mockito.mock(DetermineBuildInterface::class.java)
        Mockito.`when`(buildMock.isOreoOrGreater()).thenReturn(true)
        val shortcut = Shortcut(
            id = "test-id",
            label = "TEST",
            value = "plain text",
            cursorIndex = 0,
            position = 0,
            type = ShortcutType.TEXT
        )
        val result = ShortcutUtils.getValueOfShortcut(shortcut, null, buildMock)
        assertEquals("plain text", result)
    }

    @Test
    fun testGetValueOfShortcutWithUnknownType() {
        val buildMock = Mockito.mock(DetermineBuildInterface::class.java)
        Mockito.`when`(buildMock.isOreoOrGreater()).thenReturn(true)
        val shortcut = Shortcut(
            id = "test-id",
            label = "TEST",
            value = "val",
            cursorIndex = 0,
            position = 0,
            type = "UNKNOWN"
        )
        val result = ShortcutUtils.getValueOfShortcut(shortcut, null, buildMock)
        assertTrue(result.contains("UNKNOWN"))
        assertTrue(result.contains("val"))
    }

    @Test
    fun testGetAppliedShortcutCursorIndexWithTEXTType() {
        val shortcut = Shortcut(
            id = "test-id",
            label = "TEST",
            value = "plain text",
            cursorIndex = 3,
            position = 0,
            type = ShortcutType.TEXT
        )
        val result = ShortcutUtils.getAppliedShortcutCursorIndex(shortcut)
        assertEquals(3, result)
    }

    @Test
    fun testGetAppliedShortcutCursorIndexWithDATETIMETypeBeforeToken() {
        val shortcut = Shortcut(
            id = "test-id",
            label = "TEST",
            value = "AB{DATETIME: HH:mm:ss}CD",
            cursorIndex = 2,
            position = 0,
            type = ShortcutType.DATETIME
        )
        val result = ShortcutUtils.getAppliedShortcutCursorIndex(shortcut)
        assertEquals(2, result)
    }

    @Test
    fun testGetAppliedShortcutCursorIndexWithDATETIMETypeAfterToken() {
        val shortcut = Shortcut(
            id = "test-id",
            label = "TEST",
            value = "{DATETIME: HH:mm:ss}AB",
            cursorIndex = 20,
            position = 0,
            type = ShortcutType.DATETIME
        )
        val result = ShortcutUtils.getAppliedShortcutCursorIndex(shortcut)
        assertTrue(result >= 8)
    }

    @Test
    fun testGetValueOfShortcutBelowOreo() {
        val buildMock = Mockito.mock(DetermineBuildInterface::class.java)
        Mockito.`when`(buildMock.isOreoOrGreater()).thenReturn(false)
        val shortcut = Shortcut(
            id = "test-id",
            label = "TEST",
            value = "{DATETIME: HH:mm:ss}",
            cursorIndex = 0,
            position = 0,
            type = ShortcutType.DATETIME
        )
        val result = ShortcutUtils.getValueOfShortcut(shortcut, null, buildMock)
        assertTrue(result.contains("DATETIME shortcuts only supported in SDKs O and higher"))
    }

    @Test
    fun testGetAppliedShortcutCursorIndexWithUnknownTypeReturnsDefault() {
        val shortcut = Shortcut(
            id = "test-id",
            label = "TEST",
            value = "val",
            cursorIndex = 3,
            position = 0,
            type = "UNKNOWN"
        )
        val result = ShortcutUtils.getAppliedShortcutCursorIndex(shortcut)
        assertEquals(3, result)
    }
}
