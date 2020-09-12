package com.example.dailylog.log

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import com.example.dailylog.Constants
import com.example.dailylog.filemanager.FileHelper
import com.example.dailylog.R
import com.example.dailylog.shortcuts.ShortcutList

class AddToLogActivity : AppCompatActivity() {
    private lateinit var presenter: AddToLogPresenter
    private lateinit var view: AddToLogView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_to_log_screen)
        val globalShortcuts = ShortcutList(Constants.SHORTCUTS_LIST_PREF_KEY, application)
        globalShortcuts.loadShortcuts()
        val logView = findViewById<ScrollView>(R.id.addToLogView)
        view = AddToLogView(logView, applicationContext, globalShortcuts)

        val fileHelper = FileHelper
        FileHelper.setUpHelper(application)
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
            Constants.DATE_TIME_PREF_KEY, Constants.DATE_TIME_DEFAULT_FORMAT
        ) ?: Constants.DATE_TIME_DEFAULT_FORMAT
    }
}