package com.example.dailylog.shortcuts

import android.app.Application
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dailylog.R

class ShortcutListPresenter(private var view: android.view.View,
                            private var preferencesKey: String, private var application: Application
) {
    private lateinit var shortcutList: ShortcutList
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ShortcutListAdapter

    fun initializePresenter() {
        shortcutList = ShortcutList(preferencesKey, application)
        shortcutList.loadShortcuts()
        setUpShortcutList()
        val addShortcutView = AddShortcutView(view) { label: String, text: String, cursor: Int ->
            addShortcut(
                label,
                text,
                cursor
            )
        }
        addShortcutView.renderView()
    }

    private fun setUpShortcutList() {
        adapter = ShortcutListAdapter(shortcutList.shortcutList) { label -> removeShortcut(label) }

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(application.applicationContext)

        val callback: ItemTouchHelper.Callback = ShortcutTouchHelperCallback(adapter)
        val shortcutTouchHelper = ItemTouchHelper(callback)
        shortcutTouchHelper.attachToRecyclerView(recyclerView)
    }

    fun addShortcut(label: String, text: String, cursor: Int): Boolean {
        val added = shortcutList.addShortcut(label, text, cursor)
        adapter.updateItems(shortcutList.shortcutList)
        return added
    }

    private fun removeShortcut(shortcut: String) {
        shortcutList.removeShortcut(shortcut)
    }
}