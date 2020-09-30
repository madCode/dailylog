package com.example.dailylog.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.dailylog.entities.Shortcut
import com.example.dailylog.entities.ShortcutDao

@Database(entities = [Shortcut::class], version = 3)
abstract class ShortcutDatabase : RoomDatabase() {
    abstract fun shortcutDao(): ShortcutDao
}