package com.example.dailylog.log

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.dailylog.R
import com.example.dailylog.shortcuts.ShortcutList
import com.example.dailylog.shortcuts.ShortcutTrayView
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class AddToLogView(
    private var view: View,
    private var context: Context,
    private var shortcutList: ShortcutList
) {
    private var btnRead: ImageButton? = null
    private var btnSave: ImageButton? = null
    private var btnClear: ImageButton? = null
    private lateinit var todayLog: EditText
    private lateinit var presenter: AddToLogPresenter

    private lateinit var shortcutTrayView: ShortcutTrayView

    fun render(presenter: AddToLogPresenter, dateTimeFormat: String) {
        this.presenter = presenter
        todayLog = view.findViewById(R.id.todayLog)
        btnRead = view.findViewById(R.id.btnRead)
        btnRead?.setOnClickListener {
            loadCurrentLogFile()
        }
        loadCurrentLogFile()
        addCurrentDateToLog(dateTimeFormat)
        btnClear = view.findViewById(R.id.btnClear)
        btnClear?.setOnClickListener{
            presenter.clearFile()
            loadCurrentLogFile()
        }
        btnSave = view.findViewById(R.id.btnSave)
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
        shortcutTrayView.renderTray()
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
        shortcutTrayView = ShortcutTrayView(
            context,
            trayView,
            shortcutList,
            inputView,
        )

    }

    private fun loadCurrentLogFile() {
        todayLog.setText(presenter.readFile(), TextView.BufferType.EDITABLE);
    }

}