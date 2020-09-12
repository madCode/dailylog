package com.example.dailylog.entities

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Shortcut::class), version = 1)
abstract class ShortcutDatabase : RoomDatabase() {
    abstract fun shortcutDao(): ShortcutDao
}