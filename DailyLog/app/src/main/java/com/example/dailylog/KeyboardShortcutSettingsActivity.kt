package com.example.dailylog

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View.generateViewId
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.*


class KeyboardShortcutSettingsActivity : AppCompatActivity() {
    private lateinit var presenter: ShortcutListPresenter
    private lateinit var view: ShortcutListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.generic_keyboard_shortcuts_setup)
        view = ShortcutListView(findViewById<ConstraintLayout>(R.id.genericShortcutLayout))
        presenter = ShortcutListPresenter(view, R.string.shortcutsTitle, "global_shortcuts", application)
        view.initializeView(presenter)
        view.renderView()
    }
}