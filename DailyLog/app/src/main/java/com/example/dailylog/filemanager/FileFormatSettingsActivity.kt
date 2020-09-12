package com.example.dailylog.filemanager

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.dailylog.Constants
import com.example.dailylog.R

class FileFormatSettingsActivity : AppCompatActivity() {
    private lateinit var presenter: FileFormatSettingsPresenter
    private lateinit var view: FileFormatSettingsView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.file_format_settings)
        view = FileFormatSettingsView(getDateTimeFormat(),
            FileHelper.getFilename(application), findViewById<ConstraintLayout>(R.id.fileSettingsLayout),
            applicationContext
        )
        presenter = FileFormatSettingsPresenter(
            { format: String -> setDateTimeFormat(format) },
            { format: String -> FileHelper.setFilename(format, application) }
        )
        view.setPresenter(presenter)
        view.render()
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