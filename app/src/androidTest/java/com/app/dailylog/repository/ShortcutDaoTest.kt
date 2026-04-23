package com.app.dailylog.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class ShortcutDaoTest {

    private lateinit var db: ShortcutDatabase
    private lateinit var dao: ShortcutDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ShortcutDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.shortcutDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun <T> LiveData<T>.getOrAwait(timeoutSeconds: Long = 2): T {
        var value: T? = null
        val latch = CountDownLatch(1)
        val observer = Observer<T> { v ->
            value = v
            latch.countDown()
        }
        InstrumentationRegistry.getInstrumentation().runOnMainSync { observeForever(observer) }
        assertTrue("LiveData value never posted within ${timeoutSeconds}s", latch.await(timeoutSeconds, TimeUnit.SECONDS))
        InstrumentationRegistry.getInstrumentation().runOnMainSync { removeObserver(observer) }
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    private fun shortcut(id: String, label: String, position: Int, type: String = ShortcutType.TEXT) =
        Shortcut(id = id, label = label, value = "value-$id", cursorIndex = 0, type = type, position = position)

    // add / getAll

    @Test
    fun testAddSingleShortcutAndRetrieveIt() = runBlocking {
        dao.add(shortcut("id-1", "Alpha", 0))
        val all = dao.getAll().getOrAwait()
        assertEquals(1, all.size)
        assertEquals("id-1", all[0].id)
        assertEquals("Alpha", all[0].label)
    }

    @Test
    fun testGetAllReturnsEmptyListInitially() {
        val all = dao.getAll().getOrAwait()
        assertTrue(all.isEmpty())
    }

    @Test
    fun testGetAllOrderedByPositionAscending() = runBlocking {
        dao.add(shortcut("id-1", "B", position = 2))
        dao.add(shortcut("id-2", "A", position = 0))
        dao.add(shortcut("id-3", "C", position = 1))
        val all = dao.getAll().getOrAwait()
        assertEquals("A", all[0].label)
        assertEquals("C", all[1].label)
        assertEquals("B", all[2].label)
    }

    // addAll

    @Test
    fun testAddAllInsertsMultipleShortcuts() = runBlocking {
        dao.addAll(
            shortcut("id-1", "First", 0),
            shortcut("id-2", "Second", 1)
        )
        val all = dao.getAll().getOrAwait()
        assertEquals(2, all.size)
    }

    @Test
    fun testAddAllIgnoresDuplicateId() = runBlocking {
        dao.add(shortcut("id-1", "Original", 0))
        dao.addAll(shortcut("id-1", "Duplicate", 0))
        val all = dao.getAll().getOrAwait()
        assertEquals(1, all.size)
        assertEquals("Original", all[0].label)
    }

    // updateAll

    @Test
    fun testUpdateAllChangesExistingRow() = runBlocking {
        dao.add(shortcut("id-1", "Before", 0))
        dao.updateAll(shortcut("id-1", "After", 0))
        val all = dao.getAll().getOrAwait()
        assertEquals("After", all[0].label)
    }

    @Test
    fun testUpdateAllPreservesOtherRows() = runBlocking {
        dao.add(shortcut("id-1", "One", 0))
        dao.add(shortcut("id-2", "Two", 1))
        dao.updateAll(shortcut("id-1", "OneUpdated", 0))
        val all = dao.getAll().getOrAwait()
        assertEquals(2, all.size)
        assertEquals("OneUpdated", all[0].label)
        assertEquals("Two", all[1].label)
    }

    // deleteById

    @Test
    fun testDeleteByIdRemovesCorrectRow() = runBlocking {
        dao.add(shortcut("id-1", "ToDelete", 0))
        dao.add(shortcut("id-2", "ToKeep", 1))
        dao.deleteById("id-1")
        val all = dao.getAll().getOrAwait()
        assertEquals(1, all.size)
        assertEquals("ToKeep", all[0].label)
    }

    @Test
    fun testDeleteByIdWithNonExistentIdIsNoOp() = runBlocking {
        dao.add(shortcut("id-1", "Exists", 0))
        dao.deleteById("no-such-id")
        assertEquals(1, dao.getAll().getOrAwait().size)
    }

    @Test
    fun testDeleteByIdLeavesEmptyTable() = runBlocking {
        dao.add(shortcut("id-1", "Only", 0))
        dao.deleteById("id-1")
        assertTrue(dao.getAll().getOrAwait().isEmpty())
    }

    // labelExistsSuspend

    @Test
    fun testLabelExistsSuspendReturnsTrueWhenPresent() = runBlocking {
        dao.add(shortcut("id-1", "hello", 0))
        assertTrue(dao.labelExistsSuspend("hello"))
    }

    @Test
    fun testLabelExistsSuspendReturnsFalseWhenAbsent() = runBlocking {
        assertFalse(dao.labelExistsSuspend("nothere"))
    }

    @Test
    fun testLabelExistsSuspendIsCaseSensitive() = runBlocking {
        dao.add(shortcut("id-1", "Hello", 0))
        assertFalse(dao.labelExistsSuspend("hello"))
    }

    // labelExists (LiveData)

    @Test
    fun testLabelExistsLiveDataReturnsTrueWhenPresent() = runBlocking {
        dao.add(shortcut("id-1", "hello", 0))
        assertTrue(dao.labelExists("hello").getOrAwait())
    }

    @Test
    fun testLabelExistsLiveDataReturnsFalseWhenAbsent() {
        assertFalse(dao.labelExists("nothere").getOrAwait())
    }

    // field round-trip

    @Test
    fun testAllFieldsStoredAndRetrievedCorrectly() = runBlocking {
        val original = Shortcut(
            id = "round-trip-id",
            label = "myLabel",
            value = "myValue",
            cursorIndex = 7,
            type = ShortcutType.DATETIME,
            position = 3
        )
        dao.add(original)
        val retrieved = dao.getAll().getOrAwait()[0]
        assertEquals(original.id, retrieved.id)
        assertEquals(original.label, retrieved.label)
        assertEquals(original.value, retrieved.value)
        assertEquals(original.cursorIndex, retrieved.cursorIndex)
        assertEquals(original.type, retrieved.type)
        assertEquals(original.position, retrieved.position)
    }
}
