package com.example.dailylog

import android.app.Application
import android.content.Context

class ShortcutList(preferencesKey: String) {
    private var preferencesKey: String = preferencesKey
    lateinit var shortcutList: MutableSet<String>

    private fun saveShortcuts(application: Application): Boolean {
        val preferences =
            application.getSharedPreferences(application.applicationContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.remove(preferencesKey)
        editor.apply()
        editor.putStringSet(preferencesKey, shortcutList)
        editor.apply()
        return true
    }

    fun loadShortcuts(application: Application): Boolean {
        val preferences =
            application.getSharedPreferences(application.applicationContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        var list = preferences.getStringSet(preferencesKey, HashSet<String>())
        if (list == null) {
            list = HashSet<String>()
        }
        shortcutList = list
        return true
    }

    fun addShortcut(shortcut: String, application: Application): Boolean {
        return if (!shortcutList.contains(shortcut)) {
            shortcutList.add(shortcut)
            saveShortcuts(application)
            true
        } else {
            false
        }
    }

    fun removeShortcut(shortcut: String, application: Application): Boolean {
        shortcutList.remove(shortcut)
        saveShortcuts(application)
        return true
    }
}