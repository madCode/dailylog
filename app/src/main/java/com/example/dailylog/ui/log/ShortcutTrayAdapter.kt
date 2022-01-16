package com.example.dailylog.ui.log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.example.dailylog.R
import com.example.dailylog.repository.Shortcut
import com.example.dailylog.utils.ShortcutUtils
import com.example.dailylog.utils.ShortcutUtils.getAppliedShortcutCursorIndex


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
            val value = ShortcutUtils.getValueOfShortcut(shortcut)
            val appliedCursorIndex = getAppliedShortcutCursorIndex(shortcut, value)
            inputView.text.insert(start, value)
            inputView.setSelection(start + appliedCursorIndex)
        }
    }

    // total number of cells
    override fun getItemCount(): Int {
        return itemList.size
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var shortcutChip: Button = itemView.findViewById(R.id.shortcutChip)
    }
}