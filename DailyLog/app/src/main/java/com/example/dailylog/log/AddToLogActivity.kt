package com.example.dailylog.log

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.dailylog.Constants
import com.example.dailylog.R
import com.example.dailylog.filemanager.FileHelper
import com.example.dailylog.settings.PermissionChecker
import com.example.dailylog.settings.SettingsActivity
import com.example.dailylog.shortcuts.ShortcutList


class AddToLogActivity : AppCompatActivity() {
    private lateinit var presenter: AddToLogPresenter
    private lateinit var view: AddToLogView
    private lateinit var permissionChecker: PermissionChecker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionChecker = PermissionChecker(this)
        setContentView(R.layout.add_to_log_screen)
        val globalShortcuts = ShortcutList(Constants.SHORTCUTS_LIST_PREF_KEY, application)
        globalShortcuts.loadShortcuts()
        val logView = findViewById<ConstraintLayout>(R.id.addToLogView)
        view = AddToLogView(logView, applicationContext, globalShortcuts)

        val fileHelper = FileHelper
        fileHelper.setUpHelper(application)
        presenter = AddToLogPresenter(applicationContext, fileHelper, permissionChecker)
        view.render(presenter, getDateTimeFormat())
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        setUpActionBar()
    }

    private fun setUpActionBar() {
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_settings_24px);// set drawable icon
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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