package com.example.dailylog.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.dailylog.Constants
import com.example.dailylog.R
import com.example.dailylog.filemanager.FileFormatSettingsPresenter
import com.example.dailylog.filemanager.FileFormatSettingsView
import com.example.dailylog.filemanager.FileHelper
import com.example.dailylog.log.AddToLogActivity
import com.example.dailylog.shortcuts.ShortcutListPresenter
import com.example.dailylog.shortcuts.ShortcutListView

class SettingsActivity : AppCompatActivity() {
    private lateinit var fileSettingsPresenter: FileFormatSettingsPresenter
    private lateinit var fileSettingsView: FileFormatSettingsView
    private lateinit var shortcutPresenter: ShortcutListPresenter
    private lateinit var shortcutView: ShortcutListView
    private lateinit var selectFile: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_screen)
        fileSettingsView = FileFormatSettingsView(getDateTimeFormat(),
            FileHelper.getFilename(application), findViewById<ConstraintLayout>(R.id.fileSettingsLayout),
            applicationContext
        )
        fileSettingsPresenter = FileFormatSettingsPresenter(
            { format: String -> setDateTimeFormat(format) },
            { format: String -> FileHelper.setFilename(format, application) }
        )
        fileSettingsView.setPresenter(fileSettingsPresenter)
        fileSettingsView.render()
        shortcutView = ShortcutListView(findViewById<ConstraintLayout>(R.id.shortcutLayout))
        shortcutPresenter = ShortcutListPresenter(shortcutView, R.string.shortcutsTitle, R.string.shortcutsDescription,
            "global_shortcuts", application)
        shortcutView.initializeView(shortcutPresenter)
        shortcutView.renderView()
        selectFile = findViewById(R.id.selectFileButton)
        setUpFileChooser()
        setUpActionBar()
    }

    private fun setUpActionBar() {
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_ios_24px);// set drawable icon
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setUpFileChooser() {
        selectFile.setOnClickListener {
            val intent = Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT)

            startActivityForResult(Intent.createChooser(intent, "Select a file"), 111)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 111 && resultCode == RESULT_OK && data != null) {
            FileHelper.setFilename(data.data.toString(), application) //The uri with the location of the file
        }
    }

    private fun getDateTimeFormat(): String? {
        val preferences =
            application.getSharedPreferences(
                application.applicationContext.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
        return preferences.getString(
            Constants.DATE_TIME_PREF_KEY,
            Constants.DATE_TIME_DEFAULT_FORMAT
        )
    }

    private fun setDateTimeFormat(format: String) {
        val preferences =
            application.getSharedPreferences(application.applicationContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("dateFormat", format)
        editor.apply()
    }
}