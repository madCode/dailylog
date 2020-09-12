package com.example.dailylog.shortcuts

import android.app.Application
import androidx.room.Room
import com.example.dailylog.entities.Shortcut
import com.example.dailylog.entities.ShortcutDatabase

class ShortcutList constructor(private var shortcutType: String, application: Application) {
    private var shortcutDB: ShortcutDatabase = Room.databaseBuilder(
        application.applicationContext,
        ShortcutDatabase::class.java, "database-name"
    ).allowMainThreadQueries().build()
    lateinit var shortcutList: MutableList<Shortcut>
    lateinit var labelList: MutableSet<String>

    private fun createShortcut(label: String, text: String, cursorIndex: Int): Shortcut {
        return  Shortcut(label = label, text = text, type = shortcutType, cursorIndex = cursorIndex)
    }

    private fun saveShortcutToDB(shortcut: Shortcut): Boolean {
        shortcutDB.shortcutDao().insertAll(shortcuts = arrayOf(shortcut))
        return true
    }

    private fun deleteShortcutFromDB(label: String): Boolean {
        shortcutDB.shortcutDao().deleteByLabel(label)
        return true
    }

    private fun getAllShortcuts(): MutableList<Shortcut> {
        return shortcutDB.shortcutDao().getAllByType(shortcutType).toMutableList()
    }

    fun loadShortcuts(): Boolean {
        shortcutList = getAllShortcuts()
        labelList = HashSet<String>()
        shortcutList.forEach{
            labelList.add(it.label)
        }
        return true
    }

    fun addShortcut(label: String, text: String, cursorIndex: Int): Boolean {
        val shortcut = createShortcut(label, text, cursorIndex)
        return if (!labelList.contains(label)) {
            shortcutList.add(shortcut)
            labelList.add(label)
            saveShortcutToDB(shortcut)
            true
        } else {
            false
        }
    }

    fun removeShortcut(label: String): Boolean {
        labelList.remove(label)
        deleteShortcutFromDB(label)
        return true
    }
}