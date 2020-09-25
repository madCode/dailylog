package com.example.dailylog.log

import android.content.Context
import android.view.View
import android.widget.*
import com.example.dailylog.R
import com.example.dailylog.shortcuts.ShortcutList
import com.example.dailylog.shortcuts.ShortcutTrayAdapter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class AddToLogView(
    private var view: View,
    private var context: Context,
    var shortcutList: ShortcutList,
    private var saveCursorLocation: (Int) -> Unit,
    cursorStartLocation: Int,
) {
    private var btnSave: ImageButton? = null
    private var btnClear: ImageButton? = null
    private var btnDate: ImageButton? = null
    private var cursor: Int = cursorStartLocation
    private lateinit var todayLog: EditText
    private lateinit var presenter: AddToLogPresenter

    fun render(presenter: AddToLogPresenter, dateTimeFormat: String) {
        this.presenter = presenter
        setUpButtons(dateTimeFormat)
        todayLog = view.findViewById(R.id.todayLog)
        loadCurrentLogFile()
        setUpShortcuts()
        todayLog.requestFocus()
    }

    fun save() {
        if (presenter.saveToFile(todayLog.text.toString())) {
            Toast.makeText(context, "Saved to file", Toast.LENGTH_SHORT).show()
            loadCurrentLogFile()
        } else {
            Toast.makeText(context, "Error saving file!!!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun setUpButtons(dateTimeFormat: String) {
        todayLog = view.findViewById(R.id.todayLog)
        btnDate = view.findViewById(R.id.btnDate)
        btnDate?.setOnClickListener {
            addCurrentDateToLog(dateTimeFormat)
        }
        btnClear = view.findViewById(R.id.btnClear)
        btnClear?.setOnClickListener{
            presenter.clearFile()
            loadCurrentLogFile()
        }
        btnSave = view.findViewById(R.id.btnSave)
        btnSave?.setOnClickListener {
            save()
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

    private fun setUpShortcuts() {
        val inputView = view.findViewById<EditText>(R.id.todayLog)
        val gridView = view.findViewById<GridView>(R.id.shortcutTray)
        val adapter = ShortcutTrayAdapter(context, inputView, R.layout.shortcut_layout, shortcutList.shortcutList)
        if (shortcutList.shortcutList.size == 0) {
            gridView.visibility = View.GONE
        }
        gridView.adapter = adapter
    }

    private fun loadCurrentLogFile() {
        if (todayLog.text.isNotEmpty()) {
            cursor = todayLog.selectionStart
        }
        todayLog.setText(presenter.readFile(), TextView.BufferType.EDITABLE);
        if (cursor == -1 || cursor > todayLog.text.length) {
            cursor = todayLog.text.length
        }
        saveCursorLocation(cursor)
        todayLog.setSelection(cursor)
    }

}