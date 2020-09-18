package com.example.dailylog.shortcuts

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.example.dailylog.R
import com.example.dailylog.entities.Shortcut
import kotlin.collections.ArrayList


class ShortcutTrayView(
    var context: Context,
    var constraintLayout: ConstraintLayout,
    var shortcuts: ShortcutList,
    var inputView: EditText
) {

//    fun renderTray() {
//        val buttonList: ArrayList<View> = ArrayList()
//
//        shortcuts.shortcutList.forEach {
//            val button = createShortcutButton(context, it)
//            buttonList.add(button)
//            constraintLayout.addView(button)
//        }
//        for (i in 0 until buttonList.size) {
//            val button = buttonList[i]
//            constraintLayout.addView(button)
//        }
//    }

    fun renderTray() {
        val set = ConstraintSet()
        val buttonList: ArrayList<View> = ArrayList()

        var prevId = ConstraintSet.PARENT_ID
        shortcuts.shortcutList.forEach {
            val button = createShortcutButton(context, it)
            buttonList.add(button)
            constraintLayout.addView(button)
        }
        set.clone(constraintLayout)
        for (i in 0 until buttonList.size) {
            val button = buttonList[i]
            val nextId: Int = if (i >= buttonList.size - 1) {
                ConstraintSet.PARENT_ID
            } else {
                buttonList[i + 1].id
            }
            set.addToHorizontalChain(button.id, prevId, nextId)
            prevId = button.id
        }

        set.applyTo(constraintLayout)

    }

    private fun createShortcutButton(context: Context, shortcut: Shortcut): Button {
        val button = Button(context)
        val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        button.layoutParams = params
        button.text = shortcut.label
        button.setTextAppearance(R.style.textButton)
        button.setBackgroundResource(R.drawable.button)
        button.setOnClickListener {
            val start: Int = inputView.selectionStart
            inputView.text.insert(start, shortcut.text)
            inputView.setSelection(start + shortcut.cursorIndex);
        }
        button.id = View.generateViewId()
        return button
    }
}