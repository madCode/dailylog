package com.example.dailylog.log

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.dailylog.shortcuts.CategoryTrayView
import com.example.dailylog.shortcuts.KeyboardShortcutsTrayView
import com.example.dailylog.R
import com.example.dailylog.shortcuts.ShortcutList
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class AddToLogView(
    private var view: View,
    private var context: Context,
    private var categoryShortcutList: ShortcutList,
    private var globalShortcutList: ShortcutList
) {
    private var btnRead: ImageButton? = null
    private var btnSave: ImageButton? = null
    private var btnClear: ImageButton? = null
    private lateinit var todayLog: EditText
    private lateinit var presenter: AddToLogPresenter

    private lateinit var categoryShortcutTrayView: CategoryTrayView
    private lateinit var keyboardShortcutsTrayView: KeyboardShortcutsTrayView

    fun render(presenter: AddToLogPresenter, dateTimeFormat: String) {
        this.presenter = presenter
        todayLog = view.findViewById<EditText>(R.id.todayLog)
        btnRead = view.findViewById<ImageButton>(R.id.btnRead)
        btnRead?.setOnClickListener {
            loadCurrentLogFile()
        }
        loadCurrentLogFile()
        addCurrentDateToLog(dateTimeFormat)
        btnClear = view.findViewById<ImageButton>(R.id.btnClear)
        btnClear?.setOnClickListener{
            presenter.clearFile()
            loadCurrentLogFile()
        }
        btnSave = view.findViewById<ImageButton>(R.id.btnSave)
        btnSave?.setOnClickListener {
            if (presenter.saveToFile(todayLog.text.toString())) {
                Toast.makeText(context, "Saved to file", Toast.LENGTH_SHORT).show()
                todayLog.text.clear()
                loadCurrentLogFile()
                todayLog.setSelection(todayLog.text.length)
            } else {
                Toast.makeText(context, "Error saving file!!!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        setUpTrays()
        categoryShortcutTrayView.renderTray()
        todayLog.requestFocus()
    }

    private fun addCurrentDateToLog(dateTimeFormat: String) {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern(dateTimeFormat)
        todayLog.text.append("\n" + current.format(formatter) + "\t")
        todayLog.setSelection(todayLog.text.length)
    }

    private fun setUpTrays() {
        val trayView = view.findViewById<ConstraintLayout>(R.id.shortcutTray)
        val inputView = view.findViewById<EditText>(R.id.todayLog)
        categoryShortcutTrayView = CategoryTrayView(
            context,
            trayView,
            categoryShortcutList,
            inputView,
            null
        )
        keyboardShortcutsTrayView = KeyboardShortcutsTrayView(
            context,
            trayView,
            globalShortcutList,
            inputView,
            categoryShortcutTrayView
        )
        categoryShortcutTrayView.otherTray=keyboardShortcutsTrayView

    }

    private fun loadCurrentLogFile() {
        todayLog.setText(presenter.readFile(), TextView.BufferType.EDITABLE);
    }

}