package com.example.dailylog.shortcuts

import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.example.dailylog.R


class AddShortcutView(private var view: View, private var addShortcutCallback: (String, String, Int) -> Boolean) {
    private lateinit var label: EditText
    private lateinit var text: EditText
    private lateinit var cursor: EditText

    fun renderView() {
        label = view.findViewById(R.id.labelInput)
        text = view.findViewById(R.id.textInput)
        cursor = view.findViewById(R.id.cursorInput)
        text.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (cursor.text.equals("")) {
                    cursor.setText((text.text.toString().length - 1).toString())
                }
                return@OnEditorActionListener true
            }
            false
        })
        val btnSaveShortcut = view.findViewById<ImageButton>(R.id.btnSaveShortcut)
        btnSaveShortcut.setOnClickListener {
            val labelString = label.text.toString()
            val textString = text.text.toString()
            val cursorInt = Integer.parseInt(cursor.text.toString())
            val added = addShortcutCallback(labelString, textString, cursorInt)
            if (added == true) {
                label.text.clear()
                text.text.clear()
                cursor.text.clear()
            } else {
                // TODO: show an error and also validate that cursorInt is shorter than textString
            }
        }
    }
}