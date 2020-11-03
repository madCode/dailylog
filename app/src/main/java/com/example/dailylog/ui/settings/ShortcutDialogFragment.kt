package com.example.dailylog.ui.settings

import android.os.Build
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
import androidx.lifecycle.LiveData
import com.example.dailylog.R
import com.google.android.material.slider.Slider
import kotlinx.android.synthetic.main.create_new_shortcut.view.*
import kotlin.properties.Delegates
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface ShortcutDialogListener {
    fun labelExists(label: String): LiveData<Boolean>
}

open class ModifyShortcutDialogFragment: ShortcutDialogFragment() {
    open var keepCursorValueAtMax = true // keep the cursor value at the max it can be

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            dialog?.window?.setDecorFitsSystemWindows(true)
        } else {
            dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        super.onViewCreated(view, savedInstanceState)
        val textInput = view.textInput
        val cursorSlider = view.cursorSlider

        view.labelInput.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {
                view.labelInputLayout.error = null
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

    fun validateView(view: View) {
        valid = true
        clearInvalidLabelMessage()
        val label = view.labelInput
        val text = view.textInput
        val isLabelValid = isLabelValid(label.text.toString())
        if (isLabelValid != null && !isLabelValid) {
            view.labelInputLayout.error = "Label must be unique and cannot be empty"
            valid = false
        }
        if (!isTextValid(text.text.toString())) {
            view.textInputLayout.error = "Text cannot be empty"
            valid = false
        }
    }

    override fun alertOnInvalidLabel(label: String) {
        view?.labelInputLayout?.error = "Label must be unique and cannot be empty"
    }

    private fun clearInvalidLabelMessage() {
        view?.labelInputLayout?.error = null
    }
}

open class ShortcutDialogFragment: DialogFragment() {
    var valid = true
    var numLabelsBeingValidated = 0

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

    open fun canSubmit(): Boolean {
        return valid && numLabelsBeingValidated == 0
    }

    open fun isLabelValid(label: String): Boolean? {
        numLabelsBeingValidated += 1
        if (label.isEmpty()) {
            numLabelsBeingValidated -= 1
            return false
        }
        savingIndicator()
        val listener: ShortcutDialogListener = targetFragment as ShortcutDialogListener
        listener.labelExists(label).observe(viewLifecycleOwner){
            val alreadyExists = it
            numLabelsBeingValidated -= 1
            savingIndicator()
            if (alreadyExists) {
                alertOnInvalidLabel(label)
                valid = false
            } else {
                submit()
            }
        }
        return null
    }

    fun isTextValid(text: String): Boolean {
        return text.isNotEmpty()
    }

    open fun alertOnInvalidLabel(label: String) {
        TODO("alertOnInvalidLabel not implemented")
    }

    open fun submit() {
        TODO("implement submit")
    }

    open fun savingIndicator() {
        val button = view?.btnSaveShortcut
        if (numLabelsBeingValidated != 0) {
            button?.isEnabled = false
            button?.text = getString(R.string.saving)
        } else {
            button?.isEnabled = true
            button?.text = getString(R.string.save)
        }
    }
}