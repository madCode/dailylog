package com.app.dailylog.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.dailylog.R
import com.app.dailylog.repository.Shortcut
import com.app.dailylog.repository.ShortcutType

class EditShortcutDialogFragment(private var shortcut: Shortcut,
                                 viewModel: ShortcutDialogViewModel, private val listener: EditShortcutDialogListener) : ModifyShortcutDialogFragment(viewModel) {
    override var keepCursorValueAtMax = false

    interface EditShortcutDialogListener {
        fun onFinishEditShortcutDialog(id: String, label: String, text: String, cursor: Int, position: Int, type: String)
    }

    companion object {
        fun newInstance(shortcut: Shortcut, viewModel: ShortcutDialogViewModel, listener: EditShortcutDialogListener) =
            EditShortcutDialogFragment(shortcut, viewModel, listener)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.create_new_shortcut, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val label = binding.labelInput
        this.excludeId = shortcut.id
        binding.btnSaveShortcut.setOnClickListener {
            validateView()
            submit()
        }
        binding.addShortcutTitle.text = context?.getString(R.string.editShortcut)
        val textInput = binding.textInput
        val cursorSlider = binding.cursorSlider

        binding.labelInput.setText(shortcut.label)
        updateCursorView(cursorSlider, shortcut.value)
        cursorSlider.value = shortcut.cursorIndex.toFloat()
        textInput.setText(shortcut.value)
        this.isDateTimeType = shortcut.type == ShortcutType.DATETIME
        binding.dateTimeCheckbox.isChecked = this.isDateTimeType
    }

    override fun submit() {
        if (view == null) {
            return
        }
        val label = binding.labelInput
        val text = binding.textInput
        val cursor = binding.cursorSlider
        val type = if (isDateTimeType) {
            ShortcutType.DATETIME
        } else {
            ShortcutType.TEXT
        }
        if (canSubmit()) {
            listener.onFinishEditShortcutDialog(
                shortcut.id,
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