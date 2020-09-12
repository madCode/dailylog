package com.example.dailylog

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.dailylog.entities.Shortcut
import com.example.dailylog.entities.ShortcutDatabase

class ShortcutList constructor(private var shortcutType: String, application: Application) {
    private var shortcutDB: ShortcutDatabase = Room.databaseBuilder(
        application.applicationContext,
        ShortcutDatabase::class.java, "database-name"
    ).allowMainThreadQueries().build()
    lateinit var shortcutList: MutableSet<String>

    private fun saveShortcutToDB(label: String, text: String, cursorIndex: Int): Boolean {
        val shortcut = Shortcut(label = label, text = text, type = shortcutType, cursorIndex = cursorIndex)
        shortcutDB.shortcutDao().insertAll(shortcuts = arrayOf(shortcut))
        return true
    }

    private fun deleteShortcutFromDB(label: String): Boolean {
        shortcutDB.shortcutDao().deleteByLabel(label)
        return true
    }

    private fun getAllShortcuts(): MutableSet<String> {
        val shortcuts = shortcutDB.shortcutDao().getAllByType(shortcutType)
        val mutableSet : MutableSet<String> = HashSet()
        shortcuts.forEach { shortcut -> mutableSet.add(shortcut.label) }
        return mutableSet
    }

    fun loadShortcuts(): Boolean {
        shortcutList = getAllShortcuts()
        return true
    }

    fun addShortcut(shortcut: String): Boolean {
        return if (!shortcutList.contains(shortcut)) {
            shortcutList.add(shortcut)
            saveShortcutToDB(shortcut, "$shortcut{}; ", shortcut.length+1)
            true
        } else {
            false
        }
    }

    fun removeShortcut(shortcut: String): Boolean {
        shortcutList.remove(shortcut)
        deleteShortcutFromDB(shortcut)
        return true
    }
}