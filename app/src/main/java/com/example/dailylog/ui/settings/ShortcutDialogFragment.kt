package com.example.dailylog.ui.settings

import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.dailylog.R
import com.google.android.material.slider.Slider
import kotlinx.android.synthetic.main.create_new_shortcut.view.*

interface ShortcutDialogListener {
    fun labelIsUnique(label: String): Boolean
}

open class ModifyShortcutDialogFragment: ShortcutDialogFragment() {
    var keepCursorValueAtMax = true // keep the cursor value at the max it can be

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
                val string = s.toString()
                if (string.isEmpty()) {
                    // if the string is empty, assume you want the cursor
                    // to default to the end when we start typing again.
                    keepCursorValueAtMax = true
                }
                updateCursorView(cursorSlider, string)
                view.previewText.text = getText(string, cursorSlider.value.toInt())
                if (isTextValid(string)) {
                    view.textInputLayout.error = null
                }
            }
        })

        cursorSlider.addOnChangeListener { _, value, _ ->
            view.previewText.text = getText(textInput.text.toString(), value.toInt())
        }

        cursorSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                // If we've touched the slider, don't move it when the text changes
                keepCursorValueAtMax = false
            }

            override fun onStopTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being stopped
            }
        })

        view.btnCancelShortcut.setOnClickListener {
            dismiss()
        }
    }

    fun updateCursorView(cursorView: Slider, text: String) {
        val curr = cursorView.value
        val newMax = text.length
        if (newMax <= 0) {
            cursorView.value = 0F
            cursorView.valueTo = 1F
        } else {
            cursorView.value = minOf(newMax.toFloat(), curr)
            cursorView.valueTo = newMax.toFloat()

            if (keepCursorValueAtMax) {
                cursorView.value = newMax.toFloat()
            }
        }
    }

    fun isValid(view: View): Boolean {
        val label = view.labelInput
        val text = view.textInput
        var valid = true
        if (!isLabelValid(label.text.toString())) {
            view.labelInputLayout.error = "Label must be unique and cannot be empty"
            valid = false
        }
        if (!isTextValid(text.text.toString())) {
            view.textInputLayout.error = "Text cannot be empty"
            valid = false
        }
        return valid
    }
}

open class ShortcutDialogFragment: DialogFragment() {
    fun getText(text: String, cursorIndex: Int): SpannableStringBuilder {
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

    open fun isLabelValid(label: String): Boolean {
        val listener: ShortcutDialogListener = targetFragment as ShortcutDialogListener
        return label.isNotEmpty() && listener.labelIsUnique(label)
    }

    fun isTextValid(text: String): Boolean {
        return text.isNotEmpty()
    }
}