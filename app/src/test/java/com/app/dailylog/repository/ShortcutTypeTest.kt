package com.app.dailylog.repository

import org.junit.Assert.*
import org.junit.Test

class ShortcutTypeTest {

    @Test
    fun testValidTypesContainsTextAndDatetime() {
        val types = ShortcutType.validTypes()
        assertTrue(types.contains("TEXT"))
        assertTrue(types.contains("DATETIME"))
    }

    @Test
    fun testIsTypeValidText() {
        assertTrue(ShortcutType.isTypeValid("TEXT") == true)
    }

    @Test
    fun testIsTypeValidDatetime() {
        assertTrue(ShortcutType.isTypeValid("DATETIME") == true)
    }

    @Test
    fun testIsTypeValidUnknown() {
        assertTrue(ShortcutType.isTypeValid("UNKNOWN") == false)
    }
}