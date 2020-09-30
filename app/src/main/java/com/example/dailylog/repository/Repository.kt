package com.example.dailylog.repository

import android.content.Context
import com.example.dailylog.R
import com.example.dailylog.ui.permissions.PermissionChecker

class Repository(private var context: Context, permissionChecker: PermissionChecker) {
    var fileManager = FileManager(context, permissionChecker)
    var shortcutsManager = ShortcutsManager(context)

    fun getFilename(): String {
        return fileManager.getFilename()
    }

    fun setFilename(filename: String) {
        fileManager.setFilename(filename)
    }

    fun readFile(): String {
        return fileManager.readFile()
    }

    fun saveToFile(data: String): Boolean {
        return fileManager.saveToFile(data)
    }

    fun getDateTimeFormat(): String? {
        val preferences =
            context.getSharedPreferences(
                context.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
        return preferences.getString(
            Constants.DATE_TIME_PREF_KEY,
            Constants.DATE_TIME_DEFAULT_FORMAT
        )
    }

    fun setDateTimeFormat(format: String) {
        val preferences =
            context.getSharedPreferences(
                context.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
        val editor = preferences.edit()
        editor.putString("dateFormat", format)
        editor.apply()
    }

    fun updateShortcutPosition(label: String, position: Int) {
        shortcutsManager.updateShortcutPosition(label, position)
    }

    fun addShortcut(label: String, text: String, cursorIndex: Int): Boolean {
        return shortcutsManager.addShortcut(label, text, cursorIndex)
    }

    fun removeShortcut(label: String): Boolean {
        return shortcutsManager.removeShortcut(label)
    }

    fun getAllShortcuts(): MutableList<Shortcut> {
        return shortcutsManager.shortcutList
    }
}