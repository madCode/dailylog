package com.example.dailylog

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class AddToLogActivity : AppCompatActivity() {
    private lateinit var presenter: AddToLogPresenter
    private lateinit var view: AddToLogView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_to_log_screen)
        val categoryShortcuts = ShortcutList(Constants.CATEGORIES_LIST_PREF_KEY)
        categoryShortcuts.loadShortcuts(application)
        val globalShortcuts = ShortcutList(Constants.SHORTCUTS_LIST_PREF_KEY)
        globalShortcuts.loadShortcuts(application)
        view = AddToLogView(findViewById<ConstraintLayout>(R.id.addToLogView), applicationContext, categoryShortcuts, globalShortcuts)

        val fileHelper = FileHelper
        fileHelper.setUpHelper(application)
        presenter = AddToLogPresenter(applicationContext, FileHelper)
        view.render(presenter, getDateTimeFormat())
    }

    private fun getDateTimeFormat(): String {
        val preferences =
            application.getSharedPreferences(
                application.applicationContext.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
        return preferences.getString(
            Constants.DATE_TIME_PREF_KEY, Constants.DATE_TIME_DEFAULT_FORMAT) ?: Constants.DATE_TIME_DEFAULT_FORMAT
    }
}