package com.example.dailylog.ui.log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.example.dailylog.R
import com.example.dailylog.repository.Shortcut


class ShortcutTrayAdapter internal constructor(
    private var inputView: EditText
) :
    RecyclerView.Adapter<ShortcutTrayAdapter.ViewHolder>() {

    var itemList: List<Shortcut> = ArrayList()

    // inflates the cell layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.shortcut_layout, parent, false)
        return ViewHolder(view)
    }

    // binds the data to the TextView in each cell
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val shortcut = itemList[position]
        holder.shortcutChip.text = shortcut.label
        holder.shortcutChip.setOnClickListener {
            val start: Int = inputView.selectionStart
            inputView.text.insert(start, shortcut.text)
            inputView.setSelection(start + shortcut.cursorIndex);
        }
    }

    // total number of cells
    override fun getItemCount(): Int {
        return if (itemList.size < 14) itemList.size else 14
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var shortcutChip: Button = itemView.findViewById(R.id.shortcutChip)
    }
}