package com.app.dailylog.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import junit.framework.TestCase
import org.mockito.Mockito.mock

class ShortcutRepositoryInterfaceTest : TestCase() {

    private fun makeRepo(initialShortcuts: List<Shortcut> = emptyList()): ShortcutRepositoryInterface {
        return object : ShortcutRepositoryInterface {
            override val shortcutDao: ShortcutDao = mock(ShortcutDao::class.java)
            override var shortcutLiveData: LiveData<List<Shortcut>> = MutableLiveData(initialShortcuts)
        }
    }

    // cleanUpText

    fun testCleanUpTextNoComma() {
        assertEquals("hello", makeRepo().cleanUpText("hello"))
    }

    fun testCleanUpTextCommaNoQuotes() {
        assertEquals("hello,world", makeRepo().cleanUpText("hello,world"))
    }

    fun testCleanUpTextQuotesAndComma() {
        assertEquals("hello,world", makeRepo().cleanUpText("\"hello,world\""))
    }

    // isCursorValid

    fun testIsCursorValidAtZero() {
        assertTrue(makeRepo().isCursorValid("0", "hello"))
    }

    fun testIsCursorValidAtLength() {
        assertTrue(makeRepo().isCursorValid("5", "hello"))
    }

    fun testIsCursorValidBeyondLength() {
        assertFalse(makeRepo().isCursorValid("6", "hello"))
    }

    fun testIsCursorValidNegative() {
        assertFalse(makeRepo().isCursorValid("-1", "hello"))
    }

    fun testIsCursorValidNotANumber() {
        assertFalse(makeRepo().isCursorValid("abc", "hello"))
    }

    // isTextValid

    fun testIsTextValidNonEmpty() {
        assertTrue(makeRepo().isTextValid("hello"))
    }

    fun testIsTextValidEmpty() {
        assertFalse(makeRepo().isTextValid(""))
    }

    // isLabelValid

    fun testIsLabelValidEmpty() {
        assertFalse(makeRepo().isLabelValid(""))
    }

    fun testIsLabelValidNewLabel() {
        assertTrue(makeRepo().isLabelValid("newLabel"))
    }

    fun testIsLabelValidDuplicate() {
        val existing = Shortcut(label = "taken", value = "v", cursorIndex = 0, type = "TEXT", position = 0)
        assertFalse(makeRepo(listOf(existing)).isLabelValid("taken"))
    }

    fun testIsLabelValidDuplicateSkipped() {
        val existing = Shortcut(label = "taken", value = "v", cursorIndex = 0, type = "TEXT", position = 0)
        assertTrue(makeRepo(listOf(existing)).isLabelValid("taken", skipUniqueCheck = true))
    }

    // validateShortcutRow

    fun testValidateShortcutRowValidText() {
        assertTrue(makeRepo().validateShortcutRow(arrayOf("lbl", "txt", "2", "TEXT"), 0))
    }

    fun testValidateShortcutRowValidDatetime() {
        assertTrue(makeRepo().validateShortcutRow(arrayOf("lbl", "txt", "2", "DATETIME"), 0))
    }

    fun testValidateShortcutRowWrongFieldCount() {
        try {
            makeRepo().validateShortcutRow(arrayOf("lbl", "txt"), 0)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) { }
    }

    fun testValidateShortcutRowInvalidType() {
        try {
            makeRepo().validateShortcutRow(arrayOf("lbl", "txt", "0", "BOGUS"), 0)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) { }
    }

    fun testValidateShortcutRowEmptyText() {
        try {
            makeRepo().validateShortcutRow(arrayOf("lbl", "", "0", "TEXT"), 0)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) { }
    }

    fun testValidateShortcutRowCursorOutOfRange() {
        try {
            makeRepo().validateShortcutRow(arrayOf("lbl", "txt", "999", "TEXT"), 0)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) { }
    }

    // Regression test for off-by-one bug in cleanUpText (text.length-2 → text.length-1).
    // When a bulk-add row has a comma-containing value wrapped in quotes, cleanUpText strips
    // the quotes before cursor validation. The bug made it strip one extra character, so a
    // cursor pointing to the last position of the real text was rejected as out of bounds.
    fun testValidateShortcutRowQuotedCommaTextWithCursorAtEnd() {
        // "hello,world" is 11 chars. Cursor 11 = end of unquoted text.
        // With the old bug, cleanUpText returned "hello,worl" (10 chars) and cursor 11 failed.
        assertTrue(makeRepo().validateShortcutRow(arrayOf("lbl", "\"hello,world\"", "11", "TEXT"), 0))
    }

    // getExportRows

    fun testGetExportRowsEmpty() {
        assertTrue(makeRepo().getExportRows().isEmpty())
    }

    fun testGetExportRowsWithData() {
        val shortcuts = listOf(
            Shortcut(label = "a", value = "val1", cursorIndex = 2, type = "TEXT", position = 0),
            Shortcut(label = "b", value = "val2", cursorIndex = 0, type = "DATETIME", position = 1)
        )
        val rows = makeRepo(shortcuts).getExportRows()
        assertEquals(2, rows.size)
        assertEquals("a", rows[0][0])
        assertEquals("val1", rows[0][1])
        assertEquals("2", rows[0][2])
        assertEquals("TEXT", rows[0][3])
        assertEquals("b", rows[1][0])
        assertEquals("DATETIME", rows[1][3])
    }
}