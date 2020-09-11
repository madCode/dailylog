package com.example.dailylog

import android.app.Application

class ShortcutListPresenter(private var view: ShortcutListView,
                            private var titleResId: Int, private var descriptionResId: Int,
                            private var preferencesKey: String, private var application: Application
) {
    private lateinit var shortcutList: ShortcutList

    fun initializePresenter() {
        this.view = view
        shortcutList = ShortcutList(preferencesKey)
        shortcutList.loadShortcuts(application)
        view.setTitle(titleResId)
        view.setDescription(descriptionResId)
        view.renderShortcutList(shortcutList.shortcutList)
    }

    fun addShortcut(shortcut: String): Boolean {
        shortcutList.addShortcut(shortcut, application)
        view.renderShortcutList(shortcutList.shortcutList)
        return true
    }

    fun removeShortcut(shortcut: String): Boolean {
        shortcutList.removeShortcut(shortcut, application)
        view.renderShortcutList(shortcutList.shortcutList)
        return true
    }

    interface View {
        fun renderShortcutList(shortcutList: MutableSet<String>)
        fun setTitle(title: Int)
        fun setDescription(descriptionResId: Int)
    }
}