package com.example.dailylog.ui.log

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.example.dailylog.R
import com.example.dailylog.repository.Shortcut
import com.google.android.material.chip.Chip


class ShortcutTrayAdapter internal constructor(
    context: Context?,
    private var inputView: EditText,
    private var itemList: List<Shortcut>
) :
    RecyclerView.Adapter<ShortcutTrayAdapter.ViewHolder>() {
    private val shortcutInflator: LayoutInflater = LayoutInflater.from(context)

    // inflates the cell layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = shortcutInflator.inflate(R.layout.shortcut_layout, parent, false)
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
        var shortcutChip: Chip = itemView.findViewById(R.id.shortcutChip)
    }
}