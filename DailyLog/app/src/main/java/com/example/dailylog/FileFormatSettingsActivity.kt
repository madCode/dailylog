package com.example.dailylog

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class FileFormatSettingsActivity : AppCompatActivity() {
    private lateinit var presenter: FileFormatSettingsPresenter
    private lateinit var view: FileFormatSettingsView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.file_format_settings)
        view = FileFormatSettingsView(getDateTimeFormat(), getDefaultDateTimeFormat(),
            getFilenameFormat(), findViewById<ConstraintLayout>(R.id.fileSettingsLayout),
            applicationContext, resources
        )
        presenter = FileFormatSettingsPresenter(
            { format: String -> setDateTimeFormat(format) },
            { format: String -> setFilenameFormat(format) }
        )
        view.setPresenter(presenter)
        view.render()
    }

    private fun getDefaultDateTimeFormat(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) "yyyy-MM-dd HH:mm:ss.SSS" else "dd-MMM-yyyy"
    }

    private fun getDateTimeFormat(): String? {
        val preferences =
            application.getSharedPreferences(
                application.applicationContext.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
        return preferences.getString("dateFormat", getDefaultDateTimeFormat())
    }

    private fun setDateTimeFormat(format: String) {
        val preferences =
            application.getSharedPreferences(application.applicationContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("dateFormat", format)
        editor.apply()
    }

    private fun getFilenameFormat(): String? {
        return FileHelper.getFilenameFormat(application)
    }

    private fun setFilenameFormat(format: String) {
        val preferences =
            application.getSharedPreferences(
                application.applicationContext.getString(
                    R.string.preference_file_key), Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString("filenameFormat", format)
        editor.apply()
    }
}