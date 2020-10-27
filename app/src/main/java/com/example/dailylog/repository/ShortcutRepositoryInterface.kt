package com.example.dailylog.repository

import androidx.lifecycle.LiveData

interface ShortcutRepositoryInterface {
    val shortcutDao: ShortcutDao
    var labelList: ArrayList<String>

    var shortcutLiveData: LiveData<List<Shortcut>>
    var shortcutList: List<Shortcut>

    private suspend fun saveShortcutToDB(shortcut: Shortcut): Boolean {
        shortcutDao.add(shortcut)
        return true
    }

    private fun createShortcut(label: String, text: String, cursorIndex: Int): Shortcut {
        return  Shortcut(label = label, text = text, cursorIndex = cursorIndex, position = shortcutList.size)
    }

    private suspend fun deleteShortcutFromDB(label: String): Boolean {
        shortcutDao.deleteByLabel(label)
        return true
    }

    private fun getAllShortcuts(): LiveData<List<Shortcut>> {
        return shortcutDao.getAll()
    }

    private suspend fun updateAll() {
        shortcutList.forEach{
            it.position = labelList.indexOf(it.label)
        }
        shortcutDao.updateAll(*shortcutList.toTypedArray())
    }

    suspend fun updateShortcut(label: String, text: String, cursorIndex: Int, position: Int): Boolean {
        val shortcut = Shortcut(label = label, text = text, cursorIndex = cursorIndex, position = position)
        shortcutDao.updateAll(shortcut)
        return true
    }

    suspend fun updateShortcutPosition(label: String, position: Int) {
        labelList.remove(label)
        if (position > labelList.size) {
            labelList.add(label)
        } else {
            labelList.add(position, label)
        }
        updateAll()
    }

    suspend fun addShortcut(label: String, text: String, cursorIndex: Int): Boolean {
        val shortcut = createShortcut(label, text, cursorIndex)
        return if (!labelList.contains(label) && label.isNotEmpty() && text.isNotEmpty()) {
            saveShortcutToDB(shortcut)
            true
        } else {
            false
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
        shortcutDao.addAll(*results.toTypedArray())
        shortcutLiveData = getAllShortcuts()
        return true
    }

    private fun validateShortcutBulkRow(shortcutInfo: List<String>): Boolean {
        if (shortcutInfo.size != 3) {
            throw IllegalArgumentException("Row should contain exactly 3 items.")
        }
        shortcutInfo[2].toInt() // throws error if cannot be converted to int
        return true
    }

    suspend fun removeShortcut(label: String): Boolean {
        labelList.remove(label)
        deleteShortcutFromDB(label)
        return true
    }
}