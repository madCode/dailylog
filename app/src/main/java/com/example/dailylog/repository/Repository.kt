package com.example.dailylog.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.dailylog.R
import com.example.dailylog.ui.permissions.PermissionChecker
import java.util.ArrayList


class Repository(private val context: Context, permissionChecker: PermissionChecker) {
//    var context: Context = application.applicationContext
    private var fileManager = FileManager(context, permissionChecker)
    private var shortcutsManager = ShortcutRepository(context)

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
        editor.putString(Constants.DATE_TIME_PREF_KEY, format)
        editor.apply()
    }

    fun getCursorIndex(): Int {
        val preferences =
            context.getSharedPreferences(
                context.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
        return preferences.getInt(
            Constants.CURSOR_KEY,
            Constants.DEFAULT_CURSOR_INDEX
        )
    }

    fun setCursorIndex(index: Int) {
        val preferences =
            context.getSharedPreferences(
                context.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
        val editor = preferences.edit()
        editor.putInt(Constants.CURSOR_KEY, index)
        editor.apply()
    }

    suspend fun updateShortcutPosition(label: String, position: Int) {
        shortcutsManager.updateShortcutPosition(label, position)
    }

    suspend fun addShortcut(label: String, text: String, cursorIndex: Int): Boolean {
        return shortcutsManager.addShortcut(label, text, cursorIndex)
    }

    suspend fun removeShortcut(label: String): Boolean {
        return shortcutsManager.removeShortcut(label)
    }

    fun getAllShortcuts(): LiveData<List<Shortcut>> {
        return shortcutsManager.shortcutLiveData
    }

    fun labelIsUnique(label: String): Boolean {
        return !shortcutsManager.labelList.contains(label)
    }

    suspend fun bulkAddShortcuts(shortcutInfo: List<List<String>>): Boolean {
        return shortcutsManager.bulkAddShortcuts(shortcutInfo)
    }

    suspend fun updateShortcut(label: String, text: String, cursorIndex: Int): Boolean {
        val position = shortcutsManager.labelList.indexOf(label)
        return shortcutsManager.updateShortcut(label, text, cursorIndex, position)
    }

    fun setShortcutList(it: List<Shortcut>?) {
        if (it != null) {
            shortcutsManager.shortcutList = it
            val labelList = ArrayList<String>()
            it.forEach{
                labelList.add(it.label)
            }
            shortcutsManager.labelList = labelList
        }
    }
}