package com.example.dailylog.ui.settings

import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.dailylog.R
import com.example.dailylog.repository.Shortcut
import com.google.android.material.slider.Slider
import kotlinx.android.synthetic.main.create_new_shortcut.view.*


class EditShortcutDialogFragment(private var shortcut: Shortcut) : DialogFragment() {

    interface EditShortcutDialogListener {
        fun onFinishEditShortcutDialog(label: String, text: String, cursor: Int)
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
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        super.onViewCreated(view, savedInstanceState)
        view.addShortcutTitle.text = context?.getString(R.string.editShortcut)
        val textInput = view.textInput
        val cursorSlider = view.cursorSlider

        view.labelInput.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (isLabelValid(s.toString())) {
                    view.labelInputLayout.error = null
                }
            }
        })

        textInput.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {
                updateCursorView(cursorSlider, s.toString())
                view.previewText.text = getText(s.toString(), cursorSlider.value.toInt())
                if (isTextValid(s.toString())) {
                    view.textInputLayout.error = null
                }
            }
        })

        view.labelInput.setText(shortcut.label)
        updateCursorView(cursorSlider, shortcut.text)
        cursorSlider.value = shortcut.cursorIndex.toFloat()
        textInput.setText(shortcut.text)

        cursorSlider.addOnChangeListener { _, value, _ ->
            view.previewText.text = getText(textInput.text.toString(), value.toInt())
        }

        view.btnCancelShortcut.setOnClickListener {
            dismiss()
        }

        view.btnSaveShortcut.setOnClickListener {
            val label = view.labelInput
            val text = view.textInput
            val cursor = view.cursorSlider
            var valid = true
            if (!isLabelValid(label.text.toString())) {
                view.labelInputLayout.error = "Label must be unique and cannot be empty"
                valid = false
            }
            if (!isTextValid(text.text.toString())) {
                view.textInputLayout.error = "Text cannot be empty"
                valid = false
            }
            if (valid) {
                val listener: EditShortcutDialogListener = targetFragment as EditShortcutDialogListener
                listener.onFinishEditShortcutDialog(
                    label.text.toString(),
                    text.text.toString(),
                    cursor.value.toInt()
                )
                dismiss()
            }
        }
    }

    private fun updateCursorView(cursorView: Slider, text: String) {
        val curr = cursorView.value
        val newMax = text.length
        if (newMax <= 0) {
            cursorView.value = 0F
            cursorView.valueTo = 1F
        } else {
            cursorView.value = minOf(newMax.toFloat(), curr)
            cursorView.valueTo = newMax.toFloat()
        }
    }

    private fun getText(text: String, cursorIndex: Int): SpannableStringBuilder {
        if (text.isEmpty()) {
            return SpannableStringBuilder("")
        }
        val firstHalf = text.subSequence(0, cursorIndex).toString()
        val secondHalf = text.subSequence(cursorIndex, text.length).toString()
        val result = SpannableStringBuilder("$firstHalf|$secondHalf")
        val start = firstHalf.length
        val end = start + 1
        val value = TypedValue()
        context!!.theme.resolveAttribute(R.attr.colorAccent, value, true)
        result.setSpan(
            ForegroundColorSpan(value.data),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return result
    }

    private fun isLabelValid(label: String): Boolean {
        return label.isNotEmpty()
    }

    private fun isTextValid(text: String): Boolean {
        return text.isNotEmpty()
    }
}