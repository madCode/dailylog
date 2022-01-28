package com.app.dailylog.repository

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

    suspend fun bulkAddShortcuts(shortcutInfoList: List<Array<String>>): Boolean {
        val results = ArrayList<Shortcut>()
        shortcutInfoList.forEachIndexed {
            index, list ->
            if (validateShortcutRow(list, index)) {
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

    fun cleanUpText(text: String): String {
        // If it contains a comma and is surrounded by quotes, remove the quotes
        val containsComma = text.contains(',')
        val startsAndEndsWithQuotes = text.startsWith('"') && text.endsWith('"')
        return if (!containsComma || !startsAndEndsWithQuotes) {
            text
        } else {
            text.substring(1, text.length-2)
        }
    }

    fun isCursorValid(cursor: String, text: String): Boolean {
        return try {
            val value = cursor.toInt()
            value >= 0 && value <= text.length
        } catch (e: NumberFormatException) {
            false
        }
    }

    fun isTextValid(text: String): Boolean {
        return text.isNotEmpty()
    }

    fun isLabelValid(label: String, skipUniqueCheck: Boolean = false): Boolean {
        if (label.isEmpty()) {
            return false
        }
        if (!skipUniqueCheck) {
            for (shortcut in shortcutLiveData.value!!) {
                if (shortcut.label == label) {
                    return false
                }
            }
        }
        return true
    }

    fun validateShortcutRow(shortcutInfo: Array<String>, index: Int): Boolean {
        if (shortcutInfo.size != 4) {
            throw java.lang.IllegalArgumentException("Line $index: need exactly four values")
        }
        var (label, text, cursor, type) = shortcutInfo
        cursor = cursor.trim()
        text = cleanUpText(text)
        val validLabel = isLabelValid(label)
        val validType = ShortcutType.isTypeValid(type)
        if (validType != null && !validType) {
            throw java.lang.IllegalArgumentException("Line $index: shortcut Type must be one of" +
                    " the following: ${ShortcutType.validTypes().contentDeepToString()}")
        }
        if (!validLabel) {
            throw java.lang.IllegalArgumentException("Line $index: label must be unique and cannot be empty")
        }
        if (!isTextValid(text)) {
            throw java.lang.IllegalArgumentException("Line $index: text cannot be empty")
        }
        if (!isCursorValid(cursor, text)) {
            throw java.lang.IllegalArgumentException("Line $index: cursor must be an int. Cursor " +
                    "cannot be less than 0 or greater than the length of text.")
        }
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

    fun getExportRows(): List<List<String>> {
        val results = arrayListOf<List<String>>()
        if (shortcutLiveData.value != null && shortcutLiveData.value!!.isNotEmpty()) {
            val shortcuts = shortcutLiveData.value
            if (shortcuts != null) {
                for (shortcut in shortcuts) {
                    val row = arrayListOf<String>()
                    row.add(shortcut.label)
                    row.add(shortcut.value)
                    row.add(shortcut.cursorIndex.toString())
                    row.add(shortcut.type)
                    results.add(row)
                }
            }
        }
        return results
    }
}