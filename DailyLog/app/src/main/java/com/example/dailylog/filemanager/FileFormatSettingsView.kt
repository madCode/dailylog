package com.example.dailylog.filemanager

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import com.example.dailylog.Constants
import com.example.dailylog.R


class FileFormatSettingsView(private var dateTimeFormat: String?, private var filename: String?, private var view: View, private var context: Context) {
    private var presenter: FileFormatSettingsPresenter? = null

    fun setPresenter(presenter: FileFormatSettingsPresenter) {
        this.presenter = presenter
    }

    fun render() {
        renderDateFormatRow()
        renderFileNameRow()
    }

    private fun renderDateFormatRow() {
        val dateFormatEditText = view.findViewById<TextView>(R.id.dateFormat)
        dateFormatEditText.text = dateTimeFormat
        dateFormatEditText.hint = context.resources.getString(
            R.string.defaultStringPlaceholder,
            Constants.DATE_TIME_DEFAULT_FORMAT
        )
        dateFormatEditText.setOnEditorActionListener(OnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val saved = this.presenter?.saveDateTimeFormat(v.text.toString())
                if (saved == null || !saved) {
                    dateFormatEditText.setTextColor(Color.RED)
                    Toast.makeText(context, "Unsupported date format, try something else", Toast.LENGTH_LONG).show()
                    return@OnEditorActionListener true
                } else {
                    dateFormatEditText.setTextColor(context.getColor(R.color.primaryText))
                }
            }
            false
        })
    }

    private fun renderFileNameRow() {
        val fileNameEditText = view.findViewById<TextView>(R.id.fileNameEditText)
        fileNameEditText.text = filename
        fileNameEditText.hint = context.resources.getString(
            R.string.defaultStringPlaceholder,
            Constants.FILENAME_DEFAULT_FORMAT
        )
        fileNameEditText.setOnEditorActionListener(OnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val saved = this.presenter?.saveFilename(v.text.toString())
                if (saved == null || !saved) {
                    fileNameEditText.setTextColor(Color.RED)
                    Toast.makeText(context, "Unable to save, try again", Toast.LENGTH_LONG).show()
                    return@OnEditorActionListener true
                } else {
                    Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                    fileNameEditText.setTextColor(context.getColor(R.color.primaryText))
                }
            }
            false
        })
    }

}
