package com.example.dailylog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout


class CategorySettingsActivity : AppCompatActivity() {
    private lateinit var presenter: ShortcutListPresenter
    private lateinit var view: ShortcutListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.generic_keyboard_shortcuts_setup)
        view = ShortcutListView(findViewById<ConstraintLayout>(R.id.genericShortcutLayout))
        presenter = ShortcutListPresenter(view, R.string.categoriesTitle, "categories", application)
        view.initializeView(presenter)
        view.renderView()
    }
}