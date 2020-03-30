package com.example.dailylog

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

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
        val buttonList: ArrayList<Button> = ArrayList()

        var prevId = ConstraintSet.PARENT_ID
        if (showPrevButton) {
            buttonList.add(createToggleButton(R.drawable.ic_arrow_back_ios_24px))
        }
        shortcuts.shortcutList.forEach {
            val button = createShortcutButton(context, it, getShortcutClickListener(it))
            buttonList.add(button)
            constraintLayout.addView(button)
        }
        if (showNextButton) {
            buttonList.add(createToggleButton(R.drawable.ic_chevron_right_24px))
        }
        set.clone(constraintLayout)
        for (i in 0 until buttonList.size) {
            val button = buttonList[i]
            val nextId: Int = if (i >= shortcuts.shortcutList.size - 1) {
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

    private fun createToggleButton(iconResource: Int): Button {
        val button = Button(context)
        val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        button.layoutParams = params
        button.setBackgroundResource(iconResource)
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