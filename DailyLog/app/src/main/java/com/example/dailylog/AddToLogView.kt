package com.example.dailylog

import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast

class AddToLogView(private var view: View, private var context: Context) {
    private var btnRead: ImageButton? = null
    private var btnSave: ImageButton? = null
    private var btnClear: ImageButton? = null
    private lateinit var todayLog: EditText
    private lateinit var previousLogs: TextView
    private lateinit var presenter: AddToLogPresenter

    fun render(presenter: AddToLogPresenter) {
        this.presenter = presenter
        previousLogs = view.findViewById<TextView>(R.id.previousLogs)
        previousLogs.movementMethod = ScrollingMovementMethod()
        todayLog = view.findViewById<EditText>(R.id.todayLog)
        btnRead = view.findViewById<ImageButton>(R.id.btnRead)
        btnRead?.setOnClickListener {
            loadCurrentLogFile()
        }
        loadCurrentLogFile()
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
            } else {
                Toast.makeText(context, "Error save file!!!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun loadCurrentLogFile() {
        previousLogs.text = presenter.readFile();
    }

}