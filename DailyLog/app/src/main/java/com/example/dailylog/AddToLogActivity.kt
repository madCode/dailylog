package com.example.dailylog

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity

class AddToLogActivity : AppCompatActivity() {
    private lateinit var presenter: AddToLogPresenter
    private lateinit var view: AddToLogView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_to_log_screen)
        val categoryShortcuts = ShortcutList(Constants.CATEGORIES_LIST_PREF_KEY, application)
        categoryShortcuts.loadShortcuts()
        val globalShortcuts = ShortcutList(Constants.SHORTCUTS_LIST_PREF_KEY, application)
        globalShortcuts.loadShortcuts()
        val logView = findViewById<ScrollView>(R.id.addToLogView)
        view = AddToLogView(logView, applicationContext, categoryShortcuts, globalShortcuts)

        val fileHelper = FileHelper
        fileHelper.setUpHelper(application)
        presenter = AddToLogPresenter(applicationContext, fileHelper)
        view.render(presenter, getDateTimeFormat())
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
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