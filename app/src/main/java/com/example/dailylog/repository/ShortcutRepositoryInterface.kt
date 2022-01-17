package com.example.dailylog.repository

import androidx.lifecycle.LiveData

interface ShortcutRepositoryInterface {
    val shortcutDao: ShortcutDao

    var shortcutLiveData: LiveData<List<Shortcut>>

    private suspend fun saveShortcutToDB(shortcut: Shortcut): Boolean {
        shortcutDao.add(shortcut)
        return true
    }

    private fun createShortcut(label: String, text: String, cursorIndex: Int, type: String): Shortcut {
        return  Shortcut(label = label, value = text, cursorIndex = cursorIndex, position = nextShortcutPosition(), type=type)
    }

    private suspend fun deleteShortcutFromDB(label: String): Boolean {
        shortcutDao.deleteByLabel(label)
        return true
    }

    suspend fun saveAllShortcutsToDb(shortcuts: List<Shortcut>) {
        shortcutDao.updateAll(*shortcuts.toTypedArray())
    }

    suspend fun updateShortcut(label: String, text: String, cursorIndex: Int, position: Int, type: String): Boolean {
        val shortcut = Shortcut(label = label, value = text, cursorIndex = cursorIndex, position = position, type= type)
        shortcutDao.updateAll(shortcut)
        return true
    }

    suspend fun addShortcut(label: String, text: String, cursorIndex: Int, type: String) {
        val shortcut = createShortcut(label, text, cursorIndex, type)
        if (!shortcutDao.labelExistsSuspend(label) && label.isNotEmpty() && text.isNotEmpty()) {
            saveShortcutToDB(shortcut)
        }
    }

    suspend fun bulkAddShortcuts(shortcutInfoList: List<List<String>>): Boolean {
        val results = ArrayList<Shortcut>()
        shortcutInfoList.forEachIndexed {
                index, list ->
            try {
                if (validateShortcutBulkRow(list)) {
                    val label = list[0]
                    val text = list[1]
                    val cursorIndex = list[2].toInt()
                    val type = list[3]
                    results.add(
                        Shortcut(
                            label = label,
                            value = text,
                            cursorIndex = cursorIndex,
                            position = nextShortcutPosition() + index,
                            type = type
                        )
                    )
                }
            } catch (e: java.lang.IllegalArgumentException) {
                throw java.lang.IllegalArgumentException("row " + index + ": " + e.message)
            } catch (e: NumberFormatException) {
                throw java.lang.IllegalArgumentException("row " + index + ": " + e.message)
            }
        }
        shortcutDao.addAll(*results.toTypedArray())
        return true
    }

    private fun nextShortcutPosition(): Int {
        return if (shortcutLiveData.value != null && shortcutLiveData.value!!.isNotEmpty()) {
            shortcutLiveData.value!!.last().position + 1
        } else {
            0
        }
    }

    private fun validateShortcutBulkRow(shortcutInfo: List<String>): Boolean {
        if (shortcutInfo.size != 4) {
            throw IllegalArgumentException("Row should contain exactly 4 items.")
        }
        shortcutInfo[2].toInt() // throws error if cannot be converted to int
        return true
    }

    suspend fun removeShortcut(label: String): Boolean {
        deleteShortcutFromDB(label)
        return true
    }

    suspend fun updateShortcutPositions(shortcuts: List<Shortcut>) {
        shortcuts.forEachIndexed { index, shortcut ->
            shortcut.position = index
        }
        saveAllShortcutsToDb(shortcuts)
    }
}