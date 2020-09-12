package com.example.dailylog.shortcuts

import android.content.Context
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.dailylog.shortcuts.ShortcutList
import com.example.dailylog.shortcuts.ShortcutTrayView


class CategoryTrayView(override var context: Context,
                       override var constraintLayout: ConstraintLayout,
                       override var shortcuts: ShortcutList,
                       override var inputView: EditText,
                       override var otherTray: ShortcutTrayView?): ShortcutTrayView {

    override var showNextButton = true
    override var showPrevButton= false
}