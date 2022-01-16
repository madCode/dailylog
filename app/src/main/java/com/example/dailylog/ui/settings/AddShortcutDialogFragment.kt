package com.example.dailylog.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.dailylog.R
import com.example.dailylog.repository.ShortcutType
import kotlinx.android.synthetic.main.create_new_shortcut.view.*


class AddShortcutDialogFragment : ModifyShortcutDialogFragment() {

    interface AddShortcutDialogListener {
        fun onFinishAddShortcutDialog(label: String, text: String, cursor: Int, type: String)
    }

    companion object {
        fun newInstance() = AddShortcutDialogFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.create_new_shortcut, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.btnSaveShortcut.setOnClickListener {
            numLabelsBeingValidated = 0
            validateView(view)
            submit()
        }
    }

    override fun submit() {
        if (view == null) {
            return
        }
        val label = view!!.labelInput
        val text = view!!.textInput
        val cursor = view!!.cursorSlider
        val type = if (isDateTimeType) {
            ShortcutType.DATETIME
        } else {
            ShortcutType.TEXT
        }
        if (canSubmit()) {
            val listener: AddShortcutDialogListener = targetFragment as AddShortcutDialogListener
            listener.onFinishAddShortcutDialog(
                label.text.toString(),
                text.text.toString(),
                cursor.value.toInt(),
                type,
            )
            dismiss()
        }
    }
}