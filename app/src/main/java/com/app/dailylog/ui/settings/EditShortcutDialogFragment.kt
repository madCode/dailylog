package com.app.dailylog.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.dailylog.R
import com.app.dailylog.repository.Shortcut
import com.app.dailylog.repository.ShortcutType

class EditShortcutDialogFragment(viewModel: ShortcutDialogViewModel) : ModifyShortcutDialogFragment(viewModel) {
    // The shortcut lives in arguments and the listener is looked up, so the dialog still
    // works after Android recreates it.
    private val shortcut: Shortcut
        get() = requireArguments().let {
            Shortcut(
                it.getString(ARG_LABEL)!!,
                it.getString(ARG_VALUE)!!,
                it.getInt(ARG_CURSOR_INDEX),
                it.getString(ARG_TYPE)!!,
                it.getInt(ARG_POSITION),
            )
        }
    private val listener get() = parentFragment as EditShortcutDialogListener

    override var keepCursorValueAtMax = false
    override var skipUniqueCheck = true

    interface EditShortcutDialogListener {
        fun onFinishEditShortcutDialog(label: String, text: String, cursor: Int, position: Int, type: String)
    }

    companion object {
        private const val ARG_LABEL = "label"
        private const val ARG_VALUE = "value"
        private const val ARG_CURSOR_INDEX = "cursorIndex"
        private const val ARG_TYPE = "type"
        private const val ARG_POSITION = "position"

        fun newInstance(shortcut: Shortcut, viewModel: ShortcutDialogViewModel) =
            EditShortcutDialogFragment(viewModel).apply {
                arguments = Bundle().apply {
                    putString(ARG_LABEL, shortcut.label)
                    putString(ARG_VALUE, shortcut.value)
                    putInt(ARG_CURSOR_INDEX, shortcut.cursorIndex)
                    putString(ARG_TYPE, shortcut.type)
                    putInt(ARG_POSITION, shortcut.position)
                }
            }
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
        label.isEnabled = false
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