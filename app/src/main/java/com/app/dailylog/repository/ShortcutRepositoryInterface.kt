package com.app.dailylog.repository

import androidx.lifecycle.LiveData
import java.util.UUID

interface ShortcutRepositoryInterface {
    val shortcutDao: ShortcutDao

    var shortcutLiveData: LiveData<List<Shortcut>>

    private suspend fun saveShortcutToDB(shortcut: Shortcut): Boolean {
        shortcutDao.add(shortcut)
        return true
    }

    private fun createShortcut(label: String, text: String, cursorIndex: Int, type: String): Shortcut {
        return  Shortcut(id = UUID.randomUUID().toString(), label = label, value = text, cursorIndex = cursorIndex, position = nextShortcutPosition(), type=type)
    }

    private suspend fun deleteShortcutFromDB(id: String): Boolean {
        shortcutDao.deleteById(id)
        return true
    }

    suspend fun saveAllShortcutsToDb(shortcuts: List<Shortcut>) {
        shortcutDao.updateAll(*shortcuts.toTypedArray())
    }

    suspend fun updateShortcut(id: String, label: String, text: String, cursorIndex: Int, position: Int, type: String): Boolean {
        val shortcut = Shortcut(id = id, label = label, value = text, cursorIndex = cursorIndex, position = position, type= type)
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
            //TODO: create popup or Toast with failures
            if (validateShortcutRow(list, index)) {
                val label = list[0]
                val text = list[1]
                val cursorIndex = list[2].toInt()
                val type = list[3]
                results.add(
                    Shortcut(
                        id = UUID.randomUUID().toString(),
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
        // Commas are the field separator in the bulk-add format. If a shortcut's text value
        // contains a comma, the user must wrap it in quotes (standard CSV escaping) so the
        // parser doesn't treat that comma as a field boundary. A simple split-on-comma parser
        // leaves those surrounding quotes in place, so we strip them here to recover the
        // actual text. We only strip quotes when a comma is present: quotes around text with
        // no comma are intentional content, not CSV escaping.
        val containsComma = text.contains(',')
        val startsAndEndsWithQuotes = text.startsWith('"') && text.endsWith('"')
        return if (!containsComma || !startsAndEndsWithQuotes) {
            text
        } else {
            text.substring(1, text.length-1)
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

    fun isLabelValid(label: String, excludeId: String? = null): Boolean {
        if (label.isEmpty()) {
            return false
        }
        for (shortcut in shortcutLiveData.value ?: emptyList()) {
            if (shortcut.label == label && shortcut.id != excludeId) {
                return false
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

    suspend fun removeShortcut(id: String): Boolean {
        deleteShortcutFromDB(id)
        return true
    }

    suspend fun updateShortcutPositions(shortcuts: List<Shortcut>) {
        shortcuts.forEachIndexed { index, shortcut ->
            shortcut.position = index
        }
        saveAllShortcutsToDb(shortcuts)
    }
}
