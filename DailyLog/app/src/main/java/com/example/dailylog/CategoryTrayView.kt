package com.example.dailylog

import android.content.Context
import android.view.View
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout


class CategoryTrayView(override var context: Context,
                       override var constraintLayout: ConstraintLayout,
                       override var shortcuts: ShortcutList,
                       private var inputView: EditText,
                       override var otherTray: ShortcutTrayView?): ShortcutTrayView {

    override var showNextButton = false
    override var showPrevButton= false

    override fun getShortcutClickListener(shortcut: String): View.OnClickListener {
        return View.OnClickListener {
            val start: Int = inputView.selectionStart
            inputView.text.insert(start, "$shortcut{\n\n};")
            inputView.setSelection(inputView.text.length - 3);
            otherTray?.renderTray()
        }
    }
}