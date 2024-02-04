package com.app.dailylog.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.dailylog.R
import com.app.dailylog.repository.ShortcutType

class AddShortcutDialogFragment(viewModel: ShortcutDialogViewModel, private val listener: AddShortcutDialogListener) : ModifyShortcutDialogFragment(viewModel) {
    interface AddShortcutDialogListener {
        fun onFinishAddShortcutDialog(label: String, text: String, cursor: Int, type: String)
    }

    companion object {
        fun newInstance(viewModel: ShortcutDialogViewModel, listener: AddShortcutDialogListener) = AddShortcutDialogFragment(viewModel, listener)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.create_new_shortcut, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSaveShortcut.setOnClickListener {
            numLabelsBeingValidated = 0
            validateView()
            submit()
        }
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