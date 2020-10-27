package com.example.dailylog.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Shortcut::class], version = 3, exportSchema = false)
abstract class ShortcutDatabase : RoomDatabase() {
    abstract fun shortcutDao(): ShortcutDao

    companion object {
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