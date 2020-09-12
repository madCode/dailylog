package com.example.dailylog

import android.app.Application

class ShortcutListPresenter(private var view: ShortcutListView,
                            private var titleResId: Int, private var descriptionResId: Int,
                            private var preferencesKey: String, private var application: Application
) {
    private lateinit var shortcutList: ShortcutList

    fun initializePresenter() {
        shortcutList = ShortcutList(preferencesKey, application)
        shortcutList.loadShortcuts()
        view.setTitle(titleResId)
        view.setDescription(descriptionResId)
        view.renderShortcutList(shortcutList.labelList)
    }

    fun addShortcut(label: String, text: String, cursor: Int): Boolean {
        val added = shortcutList.addShortcut(label, text, cursor)
        view.renderShortcutList(shortcutList.labelList)
        return added
    }

    fun removeShortcut(shortcut: String): Boolean {
        shortcutList.removeShortcut(shortcut)
        view.renderShortcutList(shortcutList.labelList)
        return true
    }

    interface View {
        fun renderShortcutList(shortcutList: MutableSet<String>)
        fun setTitle(title: Int)
        fun setDescription(descriptionResId: Int)
    }
}