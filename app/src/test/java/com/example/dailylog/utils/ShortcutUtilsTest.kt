package com.example.dailylog.utils

import com.example.dailylog.repository.Shortcut
import com.example.dailylog.repository.ShortcutType
import junit.framework.TestCase
import org.mockito.Mockito
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class ShortcutUtilsTest : TestCase() {

    fun testGetValueOfShortcut() {
        val buildMock = Mockito.mock(DetermineBuild::class.java)
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
}