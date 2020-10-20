package com.example.dailylog.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.dailylog.R


class AddShortcutDialogFragment : ModifyShortcutDialogFragment() {

    interface AddShortcutDialogListener {
        fun onFinishAddShortcutDialog(label: String, text: String, cursor: Int)
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
}