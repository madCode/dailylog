package com.app.dailylog.repository

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShortcutDatabaseMigrationTest {

    private val dbName = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ShortcutDatabase::class.java,
    )

    @Test
    fun migrate4To5_keepsRowsAndUsesLabelAsId() {
        helper.createDatabase(dbName, 4).apply {
            execSQL("INSERT INTO Shortcut(label, value, cursorIndex, type, position) VALUES ('a', 'one', 1, 'TEXT', 0)")
            execSQL("INSERT INTO Shortcut(label, value, cursorIndex, type, position) VALUES ('b', '{DATETIME: yyyy}', 0, 'DATETIME', 1)")
            close()
        }

        val db = helper.runMigrationsAndValidate(dbName, 5, true, ShortcutDatabase.MIGRATION_4_5)

        val rows = mutableListOf<List<Any>>()
        db.query("SELECT id, label, value, cursorIndex, type, position FROM Shortcut ORDER BY position").use {
            while (it.moveToNext()) {
                rows += listOf(it.getString(0), it.getString(1), it.getString(2), it.getInt(3), it.getString(4), it.getInt(5))
            }
        }
        assertEquals(
            listOf(
                listOf("a", "a", "one", 1, "TEXT", 0),
                listOf("b", "b", "{DATETIME: yyyy}", 0, "DATETIME", 1),
            ),
            rows,
        )
    }
}
