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
import kotlinx.android.synthetic.main.create_new_shortcut.view.*


class AddShortcutDialogFragment : DialogFragment() {

    interface AddShortcutDialogListener {
        fun onFinishAddShortcutDialog(label: String, text: String, cursor: Int)
        fun labelIsUnique(label: String): Boolean
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
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        super.onViewCreated(view, savedInstanceState)
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
                cursorSlider.valueTo = s.length.toFloat()
                if (isTextValid(s.toString())) {
                    view.textInputLayout.error = null
                    view.previewText.text = getText(s.toString(), cursorSlider.value.toInt())
                }
            }
        })

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
                val listener: AddShortcutDialogListener = targetFragment as AddShortcutDialogListener
                listener.onFinishAddShortcutDialog(
                    label.text.toString(),
                    text.text.toString(),
                    cursor.value.toInt()
                )
                dismiss()
            }
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
        val listener: AddShortcutDialogListener = targetFragment as AddShortcutDialogListener
        return label.isNotEmpty() && listener.labelIsUnique(label)
    }

    private fun isTextValid(text: String): Boolean {
        return text.isNotEmpty()
    }
}