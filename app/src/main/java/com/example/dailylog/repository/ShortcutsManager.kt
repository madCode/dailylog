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

    fun updateShortcut(label: String, text: String, cursorIndex: Int, position: Int): Boolean {
        val shortcut = Shortcut(label = label, text = text, cursorIndex = cursorIndex, position = position)
        shortcutDB.shortcutDao().updateAll(shortcut)
        return true
    }

    fun updateShortcutPosition(label: String, position: Int) {
        labelList.remove(label)
        if (position > labelList.size) {
            labelList.add(label)
        } else {
            labelList.add(position, label)
        }
        updateAll()
    }

    fun addShortcut(label: String, text: String, cursorIndex: Int): Boolean {
        val shortcut = createShortcut(label, text, cursorIndex)
        return if (!labelList.contains(label) && label.isNotEmpty() && text.isNotEmpty()) {
            shortcutList.add(shortcut)
            labelList.add(label)
            saveShortcutToDB(shortcut)
            true
        } else {
            false
        }
    }

    fun bulkAddShortcuts(shortcutInfoList: List<List<String>>): Boolean {
        val results = ArrayList<Shortcut>()
        shortcutInfoList.forEachIndexed {
                index, list ->
            try {
                if (validateShortcutBulkRow(list)) {
                    val label = list[0]
                    val text = list[1]
                    val cursorIndex = list[2].toInt()
                    results.add(
                        Shortcut(
                            label = label,
                            text = text,
                            cursorIndex = cursorIndex,
                            position = shortcutList.size + index
                        )
                    )
                }
            } catch (e: java.lang.IllegalArgumentException) {
                throw java.lang.IllegalArgumentException("row " + index + ": " + e.message)
            } catch (e: NumberFormatException) {
                throw java.lang.IllegalArgumentException("row " + index + ": " + e.message)
            }
        }
        shortcutDB.shortcutDao().addAll(*results.toTypedArray())
        shortcutList = getAllShortcuts()
        labelList = ArrayList()
        shortcutList.forEach{
            labelList.add(it.label)
        }
        return true
    }

    private fun validateShortcutBulkRow(shortcutInfo: List<String>): Boolean {
        if (shortcutInfo.size != 3) {
            throw IllegalArgumentException("Row should contain exactly 3 items.")
        }
        shortcutInfo[2].toInt() // throws error if cannot be converted to int
        return true
    }

    fun removeShortcut(label: String): Boolean {
        labelList.remove(label)
        deleteShortcutFromDB(label)
        return true
    }
}