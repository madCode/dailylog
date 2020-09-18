package com.example.dailylog.log

import android.content.Context
import android.view.View
import android.widget.*
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
    private var btnDate: ImageButton? = null
    private var cursor: Int = -1
    private lateinit var todayLog: EditText
    private lateinit var presenter: AddToLogPresenter
    private lateinit var shortcutTrayView: ShortcutTrayView

    fun render(presenter: AddToLogPresenter, dateTimeFormat: String) {
        this.presenter = presenter
        setUpButtons(dateTimeFormat)
        todayLog = view.findViewById(R.id.todayLog)
        loadCurrentLogFile()
        setUpTrays()
        shortcutTrayView.renderTray()
        todayLog.requestFocus()
    }

    private fun setUpButtons(dateTimeFormat: String) {
        todayLog = view.findViewById(R.id.todayLog)
        btnRead = view.findViewById(R.id.btnRead)
        btnDate = view.findViewById(R.id.btnDate)
        btnDate?.setOnClickListener {
            addCurrentDateToLog(dateTimeFormat)
        }
        btnRead?.setOnClickListener {
            loadCurrentLogFile()
        }
        btnClear = view.findViewById(R.id.btnClear)
        btnClear?.setOnClickListener{
            presenter.clearFile()
            loadCurrentLogFile()
        }
        btnSave = view.findViewById(R.id.btnSave)
        btnSave?.setOnClickListener {
            if (presenter.saveToFile(todayLog.text.toString())) {
                Toast.makeText(context, "Saved to file", Toast.LENGTH_SHORT).show()
                loadCurrentLogFile()
            } else {
                Toast.makeText(context, "Error saving file!!!", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }
    private fun addCurrentDateToLog(dateTimeFormat: String) {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern(dateTimeFormat)
        val start: Int = todayLog.selectionStart
        val dateString: String = current.format(formatter)+ "\t"
        todayLog.text.insert(start, dateString)
        todayLog.setSelection(start + dateString.length);
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
        cursor = todayLog.selectionStart
        todayLog.setText(presenter.readFile(), TextView.BufferType.EDITABLE);
        if (cursor == -1) {
            cursor = todayLog.text.length
        }
        todayLog.setSelection(cursor)
    }

}