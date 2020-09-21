package com.example.dailylog.entities

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Shortcut::class], version = 2)
abstract class ShortcutDatabase : RoomDatabase() {
    abstract fun shortcutDao(): ShortcutDao
}