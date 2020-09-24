package com.example.dailylog.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.dailylog.Constants
import com.example.dailylog.R
import com.example.dailylog.filemanager.FileFormatSettingsPresenter
import com.example.dailylog.filemanager.FileFormatSettingsView
import com.example.dailylog.filemanager.FileHelper
import com.example.dailylog.shortcuts.ShortcutListPresenter


class SettingsActivity : AppCompatActivity() {
    private lateinit var fileSettingsPresenter: FileFormatSettingsPresenter
    private lateinit var fileSettingsView: FileFormatSettingsView
    private lateinit var shortcutPresenter: ShortcutListPresenter
    private lateinit var selectFile: Button
    private lateinit var fileHelper: FileHelper
    private lateinit var view: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_screen)
        view = findViewById<ConstraintLayout>(R.id.settingsView)
        setUpFileSettings()
        shortcutPresenter = ShortcutListPresenter(
            view, "global_shortcuts", application
        )
        shortcutPresenter.initializePresenter()
        selectFile = findViewById(R.id.selectFileButton)
        setUpFileChooser()
    }

    private fun setUpFileSettings() {
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
                var path = fileHelper.getPathFromUri(applicationContext, selectedFileUri)
                if (path == null) {
                    path = Constants.FILENAME_DEFAULT
                }
                fileHelper.setFilename(
                    path,
                    application
                ) //The uri with the location of the file
                fileSettingsView.render()
            }
        }
    }
}