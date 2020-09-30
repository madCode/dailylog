package com.example.dailylog.ui.settings

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dailylog.R
import com.example.dailylog.repository.Constants
import com.example.dailylog.repository.FileManager
import kotlinx.android.synthetic.main.settings_view.view.*

class SettingsView(private var fileManager: FileManager) : Fragment() {

    companion object {
        fun newInstance(fileManager: FileManager) = SettingsView(fileManager)
    }

    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings_view, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = SettingsViewModel(fileManager)
        renderDateFormatRow()
        renderFileNameRow()
    }

    private fun renderDateFormatRow() {
        val dateFormatEditText = view?.dateFormat
        dateFormatEditText?.setText(viewModel.dateTimeFormat, TextView.BufferType.EDITABLE)
        dateFormatEditText?.hint = context?.resources?.getString(
            R.string.defaultStringPlaceholder,
            Constants.DATE_TIME_DEFAULT_FORMAT
        )
        dateFormatEditText?.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val saved = viewModel.saveDateTimeFormat(v.text.toString())
                if (!saved) {
                    dateFormatEditText.setTextColor(Color.RED)
                    Toast.makeText(
                        context,
                        "Unsupported date format, try something else",
                        Toast.LENGTH_LONG
                    ).show()
                    return@OnEditorActionListener true
                } else {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        context?.getColor(R.color.colorPrimaryDark)?.let {
                            dateFormatEditText.setTextColor(
                                it
                            )
                        }
                    }
                }
            }
            false
        })
    }

    private fun renderFileNameRow() {
        view?.fileName?.text = viewModel.filename
        view?.selectFileButton?.setOnClickListener {
            val intent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                }
            } else {
                TODO("VERSION.SDK_INT < KITKAT")
            }
            startActivityForResult(Intent.createChooser(intent, "Select a file"), 111)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
            TODO("VERSION.SDK_INT < KITKAT")
        }
        if (requestCode == 111 && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val selectedFileUri = data.data;
            if (selectedFileUri != null) {
                viewModel.saveFilename(selectedFileUri.toString())
                val contentResolver = context!!.contentResolver
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                contentResolver.takePersistableUriPermission(selectedFileUri, takeFlags)
                view?.fileName?.text = viewModel.filename
//                TODO("if we didn't get the permissions we needed, ask for permission or have the user select a different file")
            }
        }
    }

}