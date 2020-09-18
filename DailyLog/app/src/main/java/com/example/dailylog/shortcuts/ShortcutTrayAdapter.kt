package com.example.dailylog.shortcuts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.dailylog.R
import com.example.dailylog.entities.Shortcut

internal class ShortcutTrayAdapter internal constructor(context: Context, var inputView: EditText, private val resource: Int, private val itemList: List<Shortcut>?) : ArrayAdapter<Button>(context, resource) {

    override fun getCount(): Int {
        return if (this.itemList != null) this.itemList.size else 0
    }

    override fun getView(position: Int, convertedView: View?, parent: ViewGroup): View {
        var convertView = convertedView
        val button: Button
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, null)
            button = convertView!!.findViewById(R.id.shortcutButton)
            convertView.tag = button
        } else {
            button = convertView.tag as Button
        }
        val shortcut = this.itemList!![position]
        button.text = shortcut.label
        button.setOnClickListener {
            val start: Int = inputView.selectionStart
            inputView.text.insert(start, shortcut.text)
            inputView.setSelection(start + shortcut.cursorIndex);
        }
        return convertView
    }
}