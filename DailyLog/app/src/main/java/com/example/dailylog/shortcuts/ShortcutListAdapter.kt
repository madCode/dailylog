package com.example.dailylog.shortcuts

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dailylog.R
import com.example.dailylog.entities.Shortcut


/**
 * adopted from https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-b9456d2b1aaf
 */
class ShortcutListAdapter(private var items: MutableList<Shortcut>, private var removeCallback: (String) -> Unit) : RecyclerView.Adapter<ShortcutListAdapter.ItemViewHolder>(),
    ShortcutTouchHelperAdapter {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.shortcut_row_layout, parent, false)
        return ItemViewHolder(view)
    }

    fun updateItems(newItems: MutableList<Shortcut>) {
        items = newItems
        notifyDataSetChanged();
    }

    private fun getText(text: String, cursorIndex: Int): SpannableStringBuilder {
        val firstHalf = text.subSequence(0,cursorIndex).toString()
        val secondHalf = text.subSequence(cursorIndex, text.length).toString()
        val result = SpannableStringBuilder("$firstHalf|$secondHalf")
        val start = firstHalf.length
        val end = start + 1
        result.setSpan(ForegroundColorSpan(-0x10000), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return result
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val shortcut: Shortcut = items[position]
        holder.label.text = shortcut.label
        holder.text.text = getText(shortcut.text, shortcut.cursorIndex)
        holder.removeButton.setOnClickListener { onItemDismiss(position) }
    }

    override fun onItemDismiss(position: Int) {
        val item = items[position]
        items.removeAt(position)
        removeCallback(item.label)
        notifyItemRemoved(position)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        val prev = items.removeAt(fromPosition)
        items.add(if (toPosition > fromPosition) toPosition - 1 else toPosition, prev)
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        ShortcutTouchHelperViewHolder {
        val label: TextView = itemView.findViewById(R.id.label) as TextView
        val text: TextView = itemView.findViewById(R.id.text) as TextView
        val removeButton: ImageButton = itemView.findViewById(R.id.removeShortcutButton)

        override fun onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY)
        }

        override fun onItemClear() {
            itemView.setBackgroundResource(R.drawable.underline)
        }

    }
}