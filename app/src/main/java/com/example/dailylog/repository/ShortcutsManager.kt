package com.example.dailylog.repository

import android.content.Context
import androidx.room.Room

class ShortcutsManager constructor(context: Context) {
    private var shortcutDB: ShortcutDatabase = Room.databaseBuilder(
       context,
       ShortcutDatabase::class.java, "database-name"
   ).allowMainThreadQueries().build()
    var shortcutList: MutableList<Shortcut> = getAllShortcuts()
    var labelList: ArrayList<String> = ArrayList()

    init {
        shortcutList.forEach{
            labelList.add(it.label)
        }
    }

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