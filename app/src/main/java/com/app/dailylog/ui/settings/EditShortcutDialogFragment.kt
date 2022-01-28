package com.app.dailylog.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.dailylog.R
import com.app.dailylog.repository.Shortcut
import com.app.dailylog.repository.ShortcutType
import kotlinx.android.synthetic.main.create_new_shortcut.view.*


class EditShortcutDialogFragment(private var shortcut: Shortcut,
                                 viewModel: ShortcutDialogViewModel) : ModifyShortcutDialogFragment(viewModel) {
    override var keepCursorValueAtMax = false
    override var skipUniqueCheck = true

    interface EditShortcutDialogListener {
        fun onFinishEditShortcutDialog(label: String, text: String, cursor: Int, position: Int, type: String)
    }

    companion object {
        fun newInstance(shortcut: Shortcut, viewModel: ShortcutDialogViewModel) =
            EditShortcutDialogFragment(shortcut, viewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.create_new_shortcut, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val label = view.labelInput
        label.isEnabled = false
        view.btnSaveShortcut.setOnClickListener {
            validateView(view)
            submit()
        }
        view.addShortcutTitle.text = context?.getString(R.string.editShortcut)
        val textInput = view.textInput
        val cursorSlider = view.cursorSlider

        view.labelInput.setText(shortcut.label)
        updateCursorView(cursorSlider, shortcut.value)
        cursorSlider.value = shortcut.cursorIndex.toFloat()
        textInput.setText(shortcut.value)
        this.isDateTimeType = shortcut.type == ShortcutType.DATETIME
        view.dateTimeCheckbox.isChecked = this.isDateTimeType
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
            val listener: EditShortcutDialogListener = targetFragment as EditShortcutDialogListener
            listener.onFinishEditShortcutDialog(
                label.text.toString(),
                text.text.toString(),
                cursor.value.toInt(),
                shortcut.position,
                type
            )
            dismiss()
        }
    }

}