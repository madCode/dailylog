package com.app.dailylog.repository

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


@Database(entities = [Shortcut::class], version = 4, exportSchema = true)
abstract class ShortcutDatabase : RoomDatabase() {
    abstract fun shortcutDao(): ShortcutDao

    companion object {
        var TEST_MODE = false
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: ShortcutDatabase? = null

        /**
         * Migrate from:
         * version 3 - no shortcut "type"
         * to
         * version 4 - type column added to shortcut table
         */
        @VisibleForTesting
        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Shout out to Google for not supporting the column rename SQL command yet
                database.execSQL("ALTER TABLE Shortcut ADD COLUMN type STRING DEFAULT 'TEXT';")
                database.execSQL("CREATE TABLE shortcut_tmp(label TEXT NOT NULL, value TEXT NOT NULL, cursorIndex INTEGER NOT NULL, type TEXT NOT NULL DEFAULT 'TEXT', position INTEGER NOT NULL, PRIMARY KEY(label));")
                database.execSQL("INSERT INTO shortcut_tmp(label, value, cursorIndex, type, position) SELECT label, text, cursorIndex, type, position FROM Shortcut;")
                database.execSQL("DROP TABLE Shortcut;")
                database.execSQL("ALTER TABLE shortcut_tmp RENAME TO Shortcut;")
            }
        }

        fun getDatabase(context: Context): ShortcutDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                if (TEST_MODE) {
                    val instance = Room.inMemoryDatabaseBuilder(
                        context,
                        ShortcutDatabase::class.java
                    ).allowMainThreadQueries().build()
                    INSTANCE = instance
                    return instance
                } else {
                    val instance = Room.databaseBuilder(
                        context,
                        ShortcutDatabase::class.java,
                        "shortcut_database"
                    ).addMigrations(MIGRATION_3_4).build()
                    INSTANCE = instance
                    return instance
                }
            }
        }
    }
}