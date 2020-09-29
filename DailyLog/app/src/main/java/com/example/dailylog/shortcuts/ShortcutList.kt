package com.example.dailylog.shortcuts

import android.app.Application
import androidx.room.Room
import com.example.dailylog.entities.Shortcut
import com.example.dailylog.entities.ShortcutDatabase

class ShortcutList constructor(application: Application) {
    private var shortcutDB: ShortcutDatabase = Room.databaseBuilder(
        application.applicationContext,
        ShortcutDatabase::class.java, "database-name"
    ).allowMainThreadQueries().build()
    lateinit var shortcutList: MutableList<Shortcut>
    lateinit var labelList: ArrayList<String>

    private fun createShortcut(label: String, text: String, cursorIndex: Int): Shortcut {
        return  Shortcut(label = label, text = text, cursorIndex = cursorIndex, position = shortcutList.size)
    }

    private fun saveShortcutToDB(shortcut: Shortcut): Boolean {
        shortcutDB.shortcutDao().add(shortcut)
        return true
    }

    private fun deleteShortcutFromDB(label: String): Boolean {
        shortcutDB.shortcutDao().deleteByLabel(label)
        return true
    }

    private fun getAllShortcuts(): MutableList<Shortcut> {
        return shortcutDB.shortcutDao().getAll().toMutableList()
    }

    fun loadShortcuts(): Boolean {
        shortcutList = getAllShortcuts()
        labelList = ArrayList<String>()
        shortcutList.forEach{
            labelList.add(it.label)
        }
        return true
    }

    private fun updateAll() {
        shortcutList.forEach{
            it.position = labelList.indexOf(it.label)
        }
        shortcutDB.shortcutDao().updateAll(*shortcutList.toTypedArray())
    }

    fun updateShortcutPosition(label: String, position: Int) {
        labelList.remove(label)
        labelList.add(position, label)
        updateAll()
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