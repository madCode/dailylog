package com.app.dailylog.ui.settings

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
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import com.app.dailylog.R
import com.app.dailylog.databinding.CreateNewShortcutBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider

interface ShortcutDialogListener {
    fun labelExists(label: String): LiveData<Boolean>
}

open class ModifyShortcutDialogFragment(viewModel: ShortcutDialogViewModel): ShortcutDialogFragment(viewModel) {
    open var keepCursorValueAtMax = true // keep the cursor value at the max it can be
    open var skipUniqueCheck = false
    lateinit var binding: CreateNewShortcutBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            dialog?.window?.setDecorFitsSystemWindows(true)
        } else {
            dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        super.onViewCreated(view, savedInstanceState)
        // Reminder for future devs: binding is defined on the parent class ShortcutDialogFragment
        binding = CreateNewShortcutBinding.bind(view)
        saveButton = binding.btnSaveShortcut

        val textInput = binding.textInput
        val cursorSlider = binding.cursorSlider

        binding.dateTimeCheckbox.setOnCheckedChangeListener { _, isChecked ->
            this.isDateTimeType = isChecked
        }

        binding.labelInput.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {
                binding.labelInputLayout.error = null
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
                binding.previewText.text = getText(string, cursorSlider.value.toInt())
                if (viewModel.isTextValid(string)) {
                    binding.textInputLayout.error = null
                }
            }
        })

        cursorSlider.addOnChangeListener { _, value, _ ->
            binding.previewText.text = getText(textInput.text.toString(), value.toInt())
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

        binding.btnCancelShortcut.setOnClickListener {
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

    fun validateView() {
        valid = true
        clearInvalidLabelMessage()
        val label = binding.labelInput
        val text = binding.textInput
        val isLabelValid = viewModel.isLabelValid(label.text.toString(), skipUniqueCheck)
        if (!isLabelValid) {
            binding.labelInputLayout.error = "Label must be unique and cannot be empty"
            valid = false
        }
        if (!viewModel.isTextValid(text.text.toString())) {
            binding.textInputLayout.error = "Text cannot be empty"
            valid = false
        }
    }

    override fun alertOnInvalidLabel(label: String) {
        binding.labelInputLayout.error = "Label must be unique and cannot be empty"
    }

    private fun clearInvalidLabelMessage() {
        binding.labelInputLayout.error = null
    }
}

open class ShortcutDialogFragment(var viewModel: ShortcutDialogViewModel): DialogFragment() {
    var valid = true
    var numLabelsBeingValidated = 0
    var isDateTimeType = false
    lateinit var saveButton: MaterialButton

    companion object {
        fun newInstance(viewModel: ShortcutDialogViewModel) = ShortcutDialogFragment(viewModel)
    }

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
        requireContext().theme.resolveAttribute(R.attr.colorAccent, value, true)
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

    open fun alertOnInvalidLabel(label: String) {
        TODO("alertOnInvalidLabel not implemented")
    }

    open fun submit() {
        TODO("implement submit")
    }

    open fun savingIndicator() {
        if (numLabelsBeingValidated != 0) {
            saveButton.isEnabled = false
            saveButton.text = getString(R.string.saving)
        } else {
            saveButton.isEnabled = true
            saveButton.text = getString(R.string.save)
        }
    }
}