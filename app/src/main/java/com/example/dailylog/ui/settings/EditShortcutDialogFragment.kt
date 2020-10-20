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


class EditShortcutDialogFragment(private var shortcut: Shortcut) : ModifyShortcutDialogFragment() {

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
        super.onViewCreated(view, savedInstanceState)
        view.addShortcutTitle.text = context?.getString(R.string.editShortcut)
        val textInput = view.textInput
        val cursorSlider = view.cursorSlider

        view.labelInput.setText(shortcut.label)
        updateCursorView(cursorSlider, shortcut.text)
        cursorSlider.value = shortcut.cursorIndex.toFloat()
        textInput.setText(shortcut.text)
    }

}