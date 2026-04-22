package com.app.dailylog.repository

import junit.framework.TestCase

class ShortcutTypeTest : TestCase() {

    fun testValidTypesContainsTextAndDatetime() {
        val types = ShortcutType.validTypes()
        assertTrue(types.contains("TEXT"))
        assertTrue(types.contains("DATETIME"))
    }

    fun testIsTypeValidText() {
        assertTrue(ShortcutType.isTypeValid("TEXT") == true)
    }

    fun testIsTypeValidDatetime() {
        assertTrue(ShortcutType.isTypeValid("DATETIME") == true)
    }

    fun testIsTypeValidUnknown() {
        assertTrue(ShortcutType.isTypeValid("UNKNOWN") == false)
    }
}