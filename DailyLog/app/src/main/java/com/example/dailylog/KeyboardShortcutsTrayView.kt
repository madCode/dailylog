package com.example.dailylog

import android.content.Context
import android.view.View
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout

class KeyboardShortcutsTrayView(override var context: Context,
                                override var constraintLayout: ConstraintLayout,
                                override var shortcuts: ShortcutList,
                                override var inputView: EditText,
                                override var otherTray: ShortcutTrayView?): ShortcutTrayView {

    override var showNextButton = false
    override var showPrevButton= true
}