package com.example.dailylog.repository

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Shortcut::class], version = 3)
abstract class ShortcutDatabase : RoomDatabase() {
    abstract fun shortcutDao(): ShortcutDao
}