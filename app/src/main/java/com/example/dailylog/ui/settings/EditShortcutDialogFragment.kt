package com.example.dailylog.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.dailylog.R
import com.example.dailylog.repository.Shortcut
import kotlinx.android.synthetic.main.create_new_shortcut.view.*


class EditShortcutDialogFragment(private var shortcut: Shortcut) : ModifyShortcutDialogFragment() {
    override var keepCursorValueAtMax = false

    interface EditShortcutDialogListener {
        fun onFinishEditShortcutDialog(label: String, text: String, cursor: Int, position: Int)
    }

    companion object {
        fun newInstance(shortcut: Shortcut) = EditShortcutDialogFragment(shortcut)
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
    }

    override fun submit() {
        if (view == null) {
            return
        }
        val label = view!!.labelInput
        val text = view!!.textInput
        val cursor = view!!.cursorSlider
        if (canSubmit()) {
            val listener: EditShortcutDialogListener = targetFragment as EditShortcutDialogListener
            listener.onFinishEditShortcutDialog(
                label.text.toString(),
                text.text.toString(),
                cursor.value.toInt(),
                shortcut.position
            )
            dismiss()
        }
    }

    override fun isLabelValid(label: String): Boolean {
        return label.isNotEmpty()
    }

}