package com.app.dailylog.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ShortcutRepositoryInterfaceTest {

    private fun makeRepo(initialShortcuts: List<Shortcut> = emptyList()): ShortcutRepositoryInterface {
        return object : ShortcutRepositoryInterface {
            override val shortcutDao: ShortcutDao = object : ShortcutDao {
                override fun getAll(): LiveData<List<Shortcut>> = MutableLiveData(emptyList())
                override suspend fun updateAll(vararg shortcuts: Shortcut) {}
                override suspend fun add(shortcut: Shortcut) {}
                override suspend fun addAll(vararg shortcuts: Shortcut) {}
                override suspend fun deleteById(id: String) {}
                override fun labelExists(label: String): LiveData<Boolean> = MutableLiveData(false)
                override suspend fun labelExistsSuspend(label: String): Boolean = false
            }
            override var shortcutLiveData: LiveData<List<Shortcut>> = MutableLiveData(initialShortcuts)
        }
    }

    // cleanUpText

    @Test
    fun testCleanUpTextNoComma() {
        assertEquals("hello", makeRepo().cleanUpText("hello"))
    }

    @Test
    fun testCleanUpTextCommaNoQuotes() {
        assertEquals("hello,world", makeRepo().cleanUpText("hello,world"))
    }

    @Test
    fun testCleanUpTextQuotesAndComma() {
        assertEquals("hello,world", makeRepo().cleanUpText("\"hello,world\""))
    }

    // isCursorValid

    @Test
    fun testIsCursorValidAtZero() {
        assertTrue(makeRepo().isCursorValid("0", "hello"))
    }

    @Test
    fun testIsCursorValidAtLength() {
        assertTrue(makeRepo().isCursorValid("5", "hello"))
    }

    @Test
    fun testIsCursorValidBeyondLength() {
        assertFalse(makeRepo().isCursorValid("6", "hello"))
    }

    @Test
    fun testIsCursorValidNegative() {
        assertFalse(makeRepo().isCursorValid("-1", "hello"))
    }

    @Test
    fun testIsCursorValidNotANumber() {
        assertFalse(makeRepo().isCursorValid("abc", "hello"))
    }

    // isTextValid

    @Test
    fun testIsTextValidNonEmpty() {
        assertTrue(makeRepo().isTextValid("hello"))
    }

    @Test
    fun testIsTextValidEmpty() {
        assertFalse(makeRepo().isTextValid(""))
    }

    // isLabelValid

    @Test
    fun testIsLabelValidEmpty() {
        assertFalse(makeRepo().isLabelValid(""))
    }

    @Test
    fun testIsLabelValidNewLabel() {
        assertTrue(makeRepo().isLabelValid("newLabel"))
    }

    @Test
    fun testIsLabelValidDuplicate() {
        val existing = Shortcut(id = "id-1", label = "taken", value = "v", cursorIndex = 0, type = "TEXT", position = 0)
        assertFalse(makeRepo(listOf(existing)).isLabelValid("taken"))
    }

    @Test
    fun testIsLabelValidExcludeIdAllowsSameLabelOnOwnShortcut() {
        val existing = Shortcut(id = "id-1", label = "taken", value = "v", cursorIndex = 0, type = "TEXT", position = 0)
        assertTrue(makeRepo(listOf(existing)).isLabelValid("taken", excludeId = "id-1"))
    }

    @Test
    fun testIsLabelValidExcludeIdBlocksDuplicateOnDifferentShortcut() {
        val existing = Shortcut(id = "id-1", label = "taken", value = "v", cursorIndex = 0, type = "TEXT", position = 0)
        assertFalse(makeRepo(listOf(existing)).isLabelValid("taken", excludeId = "id-2"))
    }

    @Test
    fun testIsLabelValidExcludeIdNullBehavesLikeDefault() {
        val existing = Shortcut(id = "id-1", label = "taken", value = "v", cursorIndex = 0, type = "TEXT", position = 0)
        assertFalse(makeRepo(listOf(existing)).isLabelValid("taken", excludeId = null))
    }

    // validateShortcutRow

    @Test
    fun testValidateShortcutRowValidText() {
        assertTrue(makeRepo().validateShortcutRow(arrayOf("lbl", "txt", "2", "TEXT"), 0))
    }

    @Test
    fun testValidateShortcutRowValidDatetime() {
        assertTrue(makeRepo().validateShortcutRow(arrayOf("lbl", "txt", "2", "DATETIME"), 0))
    }

    @Test
    fun testValidateShortcutRowWrongFieldCount() {
        try {
            makeRepo().validateShortcutRow(arrayOf("lbl", "txt"), 0)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) { }
    }

    @Test
    fun testValidateShortcutRowInvalidType() {
        try {
            makeRepo().validateShortcutRow(arrayOf("lbl", "txt", "0", "BOGUS"), 0)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) { }
    }

    @Test
    fun testValidateShortcutRowEmptyText() {
        try {
            makeRepo().validateShortcutRow(arrayOf("lbl", "", "0", "TEXT"), 0)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) { }
    }

    @Test
    fun testValidateShortcutRowCursorOutOfRange() {
        try {
            makeRepo().validateShortcutRow(arrayOf("lbl", "txt", "999", "TEXT"), 0)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) { }
    }

    @Test
    fun testValidateShortcutRowQuotedCommaTextWithCursorAtEnd() {
        assertTrue(makeRepo().validateShortcutRow(arrayOf("lbl", "\"hello,world\"", "11", "TEXT"), 0))
    }

    // bulkAddShortcuts

    @Test
    fun testBulkAddShortcutsWithValidRowReturnsTrue() {
        val result = runBlocking {
            makeRepo().bulkAddShortcuts(listOf(arrayOf("lbl", "txt", "0", "TEXT")))
        }
        assertTrue(result)
    }

    @Test
    fun testBulkAddShortcutsWithEmptyListReturnsTrue() {
        val result = runBlocking {
            makeRepo().bulkAddShortcuts(emptyList())
        }
        assertTrue(result)
    }

    @Test
    fun testBulkAddShortcutsWithExistingShortcutCoversNextPosition() {
        val existing = Shortcut(id = "id-1", label = "existing", value = "v", cursorIndex = 0, type = "TEXT", position = 5)
        val result = runBlocking {
            makeRepo(listOf(existing)).bulkAddShortcuts(listOf(arrayOf("newlbl", "txt", "0", "TEXT")))
        }
        assertTrue(result)
    }

    // addShortcut

    @Test
    fun testAddShortcutWithValidDataCompletesWithoutError() {
        runBlocking {
            makeRepo().addShortcut("newlabel", "text", 0, "TEXT")
        }
    }

    @Test
    fun testAddShortcutWithExistingShortcutCoversNextPosition() {
        val existing = Shortcut(id = "id-1", label = "existing", value = "v", cursorIndex = 0, type = "TEXT", position = 3)
        runBlocking {
            makeRepo(listOf(existing)).addShortcut("newlabel", "text", 0, "TEXT")
        }
    }

    // removeShortcut

    @Test
    fun testRemoveShortcutReturnsTrue() {
        val result = runBlocking { makeRepo().removeShortcut("id-1") }
        assertTrue(result)
    }

    // updateShortcut

    @Test
    fun testUpdateShortcutReturnsTrue() {
        val result = runBlocking {
            makeRepo().updateShortcut("id-1", "lbl", "txt", 0, 0, "TEXT")
        }
        assertTrue(result)
    }

    // updateShortcutPositions

    @Test
    fun testUpdateShortcutPositionsReassignsPositions() {
        val s1 = Shortcut(id = "id-1", label = "a", value = "v", cursorIndex = 0, type = "TEXT", position = 5)
        val s2 = Shortcut(id = "id-2", label = "b", value = "v", cursorIndex = 0, type = "TEXT", position = 99)
        val list = mutableListOf(s1, s2)
        runBlocking { makeRepo(list).updateShortcutPositions(list) }
        assertEquals(0, s1.position)
        assertEquals(1, s2.position)
    }

    // saveAllShortcutsToDb

    @Test
    fun testSaveAllShortcutsToDbCompletesWithoutError() {
        val shortcuts = listOf(
            Shortcut(id = "id-1", label = "a", value = "v", cursorIndex = 0, type = "TEXT", position = 0)
        )
        runBlocking { makeRepo(shortcuts).saveAllShortcutsToDb(shortcuts) }
    }

    // label rename end-to-end

    @Test
    fun testAfterRenameOldLabelBecomesAvailableAndNewLabelIsTaken() {
        val stubDao = object : ShortcutDao {
            override fun getAll(): LiveData<List<Shortcut>> = MutableLiveData(emptyList())
            override suspend fun updateAll(vararg shortcuts: Shortcut) {}
            override suspend fun add(shortcut: Shortcut) {}
            override suspend fun addAll(vararg shortcuts: Shortcut) {}
            override suspend fun deleteById(id: String) {}
            override fun labelExists(label: String): LiveData<Boolean> = MutableLiveData(false)
            override suspend fun labelExistsSuspend(label: String): Boolean = false
        }
        val repo = object : ShortcutRepositoryInterface {
            override val shortcutDao: ShortcutDao = stubDao
            override var shortcutLiveData: LiveData<List<Shortcut>> = MutableLiveData(
                listOf(Shortcut(id = "id-1", label = "original", value = "v", cursorIndex = 0, type = "TEXT", position = 0))
            )
        }

        // Before rename: "original" is taken, "renamed" is free
        assertFalse(repo.isLabelValid("original"))
        assertTrue(repo.isLabelValid("renamed"))

        // Simulate the DB update propagating back through live data (reassign the reference
        // rather than calling setValue, which requires the main thread in unit tests)
        repo.shortcutLiveData = MutableLiveData(
            listOf(Shortcut(id = "id-1", label = "renamed", value = "v", cursorIndex = 0, type = "TEXT", position = 0))
        )

        // After rename: "original" is free, "renamed" is taken
        assertTrue(repo.isLabelValid("original"))
        assertFalse(repo.isLabelValid("renamed"))
        // ...but "renamed" is still valid when editing that same shortcut (excludeId)
        assertTrue(repo.isLabelValid("renamed", excludeId = "id-1"))
    }
}
