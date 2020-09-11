package com.example.dailylog

import android.content.Context
import android.view.View
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout

class KeyboardShortcutsTrayView(override var context: Context,
                                override var constraintLayout: ConstraintLayout,
                                override var shortcuts: ShortcutList,
                                private var inputView: EditText,
                                override var otherTray: ShortcutTrayView?): ShortcutTrayView {

    override var showNextButton = false
    override var showPrevButton= true

    override fun getShortcutClickListener(shortcut: String): View.OnClickListener {
        return View.OnClickListener {
            val start: Int = inputView.selectionStart
            if (start == 0 || (inputView.text.length > start && inputView.text[start].isWhitespace())) {
                inputView.text.insert(start, "$shortcut ")
            } else {
                inputView.text.insert(start, " $shortcut ")
            }
            otherTray?.renderTray()
        }
    }
}