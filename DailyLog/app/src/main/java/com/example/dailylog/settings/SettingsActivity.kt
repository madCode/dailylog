package com.example.dailylog.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.dailylog.R
import com.example.dailylog.filemanager.FileFormatSettingsPresenter
import com.example.dailylog.filemanager.FileFormatSettingsView
import com.example.dailylog.filemanager.FileHelper
import com.example.dailylog.shortcuts.ShortcutListPresenter
import com.example.dailylog.shortcuts.ShortcutListView

class SettingsActivity : AppCompatActivity() {
    private lateinit var fileSettingsPresenter: FileFormatSettingsPresenter
    private lateinit var fileSettingsView: FileFormatSettingsView
    private lateinit var shortcutPresenter: ShortcutListPresenter
    private lateinit var shortcutView: ShortcutListView
    private lateinit var selectFile: Button
    private lateinit var fileHelper: FileHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_screen)
        val view = findViewById<ConstraintLayout>(R.id.settingsView)
        fileHelper = FileHelper
        fileHelper.setUpHelper(application)
        fileSettingsView = FileFormatSettingsView(
            fileHelper.getDateTimeFormat(application),
            fileHelper.getFilename(application),
            view,
            applicationContext
        )
        fileSettingsPresenter = FileFormatSettingsPresenter(
            { format: String -> fileHelper.setDateTimeFormat(format, application) },
            { format: String -> fileHelper.setFilename(format, application) }
        )
        fileSettingsView.setPresenter(fileSettingsPresenter)
        fileSettingsView.render()
        shortcutView = ShortcutListView(view)
        shortcutPresenter = ShortcutListPresenter(
            shortcutView, R.string.shortcutsTitle, R.string.shortcutsDescription,
            "global_shortcuts", application
        )
        shortcutView.initializeView(shortcutPresenter)
        shortcutView.renderView()
        selectFile = findViewById(R.id.selectFileButton)
        setUpFileChooser()
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
            val selectedFileUri = data.data;
            if (selectedFileUri != null && selectedFileUri.path != null) {
                //val file = File(selectedFileUri.path)
                val path = fileHelper.getPathFromUri(applicationContext, selectedFileUri)
                fileHelper.setFilename(
                    path,
                    application
                ) //The uri with the location of the file
                fileSettingsView.render()
            }
        }
    }
}