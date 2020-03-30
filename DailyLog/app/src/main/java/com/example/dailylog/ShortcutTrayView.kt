package com.example.dailylog

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import kotlin.collections.ArrayList


interface ShortcutTrayView {
    var context: Context
    var constraintLayout: ConstraintLayout
    var shortcuts: ShortcutList
    fun getShortcutClickListener(shortcut: String):View.OnClickListener
    var otherTray: ShortcutTrayView?
    var showNextButton: Boolean
    var showPrevButton: Boolean

    fun renderTray() {
        otherTray?.clearTray()

        val set = ConstraintSet()
        val buttonList: ArrayList<View> = ArrayList()

        var prevId = ConstraintSet.PARENT_ID
        if (showPrevButton) {
            val button = createToggleButton(R.drawable.ic_arrow_back_ios_24px)
            buttonList.add(button)
            constraintLayout.addView(button)
        }
        shortcuts.shortcutList.forEach {
            val button = createShortcutButton(context, it, getShortcutClickListener(it))
            buttonList.add(button)
            constraintLayout.addView(button)
        }
        if (showNextButton) {
            val button = createToggleButton(R.drawable.ic_chevron_right_24px)
            buttonList.add(button)
            constraintLayout.addView(button)
        }
        set.clone(constraintLayout)
        for (i in 0 until buttonList.size) {
            val button = buttonList[i]
            val nextId: Int = if (i >= buttonList.size - 1) {
                ConstraintSet.PARENT_ID
            } else {
                buttonList[i+1].id
            }
            set.addToHorizontalChain(button.id, prevId, nextId)
            prevId = button.id
        }

        set.applyTo(constraintLayout)

    }

    fun clearTray() {
        constraintLayout.removeAllViews()
    }

    private fun createToggleButton(iconResource: Int): ImageButton {
        val button = ImageButton(context)
        val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        button.layoutParams = params
        button.setImageResource(iconResource)
        button.setOnClickListener{
            if (otherTray != null) {
                clearTray()
                otherTray?.renderTray()
            }
        }
        button.id = View.generateViewId()
        return button
    }

    private fun createShortcutButton(context: Context, shortcut: String, shortcutClickListener: View.OnClickListener): Button {
        val button = Button(context)
        val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        button.layoutParams = params
        button.text = shortcut
        button.setTextAppearance(R.style.textButton)
        button.setBackgroundResource(R.drawable.button)
        button.setOnClickListener(shortcutClickListener)
        button.id = View.generateViewId()
        return button
    }
}