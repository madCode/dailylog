package com.example.dailylog.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            }

            startActivityForResult(Intent.createChooser(intent, "Select a file"), 111)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 111 && resultCode == RESULT_OK && data != null) {
            val selectedFileUri = data.data;
            if (selectedFileUri != null) {
                fileHelper.setFilename(selectedFileUri.toString(), application)
                val contentResolver = applicationContext.contentResolver
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                // Check for the freshest data.
                contentResolver.takePersistableUriPermission(selectedFileUri, takeFlags)
                fileSettingsView.filename = selectedFileUri.toString()
                fileSettingsView.render()
            }
        }
    }
}