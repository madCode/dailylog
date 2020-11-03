package com.example.dailylog.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Shortcut::class], version = 3, exportSchema = false)
abstract class ShortcutDatabase : RoomDatabase() {
    abstract fun shortcutDao(): ShortcutDao

    companion object {
        var TEST_MODE = false
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: ShortcutDatabase? = null

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
                    ).build()
                    INSTANCE = instance
                    return instance
                }
            }
        }
    }
}