package com.app.dailylog.repository

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    companion object {
        private const val TEST_DB = "migration-test"
    }

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ShortcutDatabase::class.java
    )

    @Test
    fun migrate4To5_preservesAllShortcutsWithLabelSeedingId() {
        helper.createDatabase(TEST_DB, 4).use { db ->
            db.execSQL("INSERT INTO Shortcut (label, value, cursorIndex, type, position) VALUES ('myLabel', 'myValue', 3, 'TEXT', 0)")
            db.execSQL("INSERT INTO Shortcut (label, value, cursorIndex, type, position) VALUES ('dtLabel', '{DATETIME: yyyy-MM-dd}', 0, 'DATETIME', 1)")
        }

        helper.runMigrationsAndValidate(TEST_DB, 5, true, ShortcutDatabase.MIGRATION_4_5).use { db ->
            val cursor = db.query("SELECT id, label, value, cursorIndex, type, position FROM Shortcut ORDER BY position ASC")
            assertEquals(2, cursor.count)

            cursor.moveToFirst()
            assertEquals("myLabel", cursor.getString(cursor.getColumnIndexOrThrow("id")))
            assertEquals("myLabel", cursor.getString(cursor.getColumnIndexOrThrow("label")))
            assertEquals("myValue", cursor.getString(cursor.getColumnIndexOrThrow("value")))
            assertEquals(3, cursor.getInt(cursor.getColumnIndexOrThrow("cursorIndex")))
            assertEquals("TEXT", cursor.getString(cursor.getColumnIndexOrThrow("type")))
            assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("position")))

            cursor.moveToNext()
            assertEquals("dtLabel", cursor.getString(cursor.getColumnIndexOrThrow("id")))
            assertEquals("dtLabel", cursor.getString(cursor.getColumnIndexOrThrow("label")))
            assertEquals("{DATETIME: yyyy-MM-dd}", cursor.getString(cursor.getColumnIndexOrThrow("value")))
            assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("cursorIndex")))
            assertEquals("DATETIME", cursor.getString(cursor.getColumnIndexOrThrow("type")))
            assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow("position")))

            cursor.close()
        }
    }

    @Test
    fun migrate4To5_emptyDatabaseMigratesCleanly() {
        helper.createDatabase(TEST_DB, 4).use { /* no rows */ }
        helper.runMigrationsAndValidate(TEST_DB, 5, true, ShortcutDatabase.MIGRATION_4_5).use { db ->
            val cursor = db.query("SELECT COUNT(*) FROM Shortcut")
            cursor.moveToFirst()
            assertEquals(0, cursor.getInt(0))
            cursor.close()
        }
    }
}
