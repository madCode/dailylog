package com.app.dailylog.ui.log

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.app.dailylog.R
import com.app.dailylog.databinding.AddToLogViewBinding

class LogFragment(private val viewModel: LogViewModel, private val goToSettings: () -> Unit) : Fragment() {
    private lateinit var binding: AddToLogViewBinding
    companion object {
        fun newInstance(viewModel: LogViewModel, goToSettings: () -> Unit) = LogFragment(
            viewModel,
            goToSettings
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (context == null || activity == null) {
            error("context or activity is null")
        }

        binding = AddToLogViewBinding.bind(view)

        binding.btnSave.setOnClickListener {
            // If the save button is pressed, force-save
            save(true)
        }

        binding.btnSettings.setOnClickListener {
            save(false)
            goToSettings()
        }

        renderShortcutTray()
    }

    override fun onPause() {
        super.onPause()
        save(false)
    }

    override fun onResume() {
        super.onResume()
        loadFile()
        binding.todayLog.requestFocus()

        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(binding.todayLog, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun save(forceSave: Boolean) {
        if (view == null) {
            Toast.makeText(context, "Could not save. View not loaded yet.", Toast.LENGTH_SHORT).show()
            return
        }
        val todayLog = binding.todayLog
        if (todayLog.text != null && todayLog.text!!.isNotEmpty()) {
            viewModel.saveCursorIndex(todayLog.selectionStart)
        }
        val saved = if (forceSave) {
            viewModel.forceSave(todayLog.text.toString())
        } else {
            viewModel.smartSave(todayLog.text.toString())
        }
        if (saved) {
            todayLog.setText(viewModel.getLog(), TextView.BufferType.EDITABLE)
            todayLog.setSelection(getCursorIndex(todayLog.text.toString()))
            Toast.makeText(context, "Saved file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun renderShortcutTray() {
        if (context == null) {
            return
        }
        val shortcutsLiveData = viewModel.getAllShortcuts()
        val tray = binding.shortcutTray
        tray.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.HORIZONTAL)
        val adapter = ShortcutTrayAdapter(binding.todayLog)
        shortcutsLiveData.observe(viewLifecycleOwner, Observer { shortcuts ->
            // Update the cached copy of the words in the adapter.
            shortcuts.let { adapter.itemList = it; }
        })
        tray.adapter = adapter
    }

    private fun getCursorIndex(text: String): Int {
        return if (viewModel.cursorIndex > -1 && viewModel.cursorIndex < text.length) viewModel.cursorIndex else text.length
    }

    private fun loadFile() {
        val todayLog = binding.todayLog
        todayLog.setText(viewModel.getLog(), TextView.BufferType.EDITABLE)
        val cursorIndex = getCursorIndex(todayLog.text!!.toString())
        todayLog.setSelection(cursorIndex)
        Toast.makeText(context, "Loaded file", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.add_to_log_view, container, false)
    }

}