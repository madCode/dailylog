package com.app.dailylog.ui.log

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

        // Apply window insets to handle notch and navigation areas
        ViewCompat.setOnApplyWindowInsetsListener(view) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,    // avoids notch/status bar
                systemBars.right,
                systemBars.bottom  // avoids nav buttons
            )
            insets
        }

        binding.btnSave.setOnClickListener {
            // If the save button is pressed, force-save
            save(true)
        }

        binding.btnSettings.setOnClickListener {
            save(false)
            goToSettings()
        }

        viewModel.saveComplete.observe(viewLifecycleOwner) {
            Toast.makeText(context, "Saved file", Toast.LENGTH_SHORT).show()
        }

        viewModel.saveError.observe(viewLifecycleOwner) { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
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
        renderShortcutTray()
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
        if (forceSave) {
            viewModel.forceSave(todayLog.text.toString())
        } else {
            viewModel.smartSave(todayLog.text.toString())
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
        
        // Add keyboard visibility listener to adjust shortcut tray position
        ViewCompat.setOnApplyWindowInsetsListener(tray) { view, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            if (imeHeight > 0) {
                // Keyboard is visible, adjust the layout
                val params = view.layoutParams as ViewGroup.MarginLayoutParams
                params.bottomMargin = imeHeight
                view.layoutParams = params
            } else {
                // Keyboard is hidden, reset margin
                val params = view.layoutParams as ViewGroup.MarginLayoutParams
                params.bottomMargin = 0
                view.layoutParams = params
            }
            insets
        }
    }

    private fun getCursorIndex(text: String): Int {
        return if (viewModel.cursorIndex > -1 && viewModel.cursorIndex < text.length) viewModel.cursorIndex else text.length
    }

    private fun loadFile() {
        val todayLog = binding.todayLog
        todayLog.setText(viewModel.getLog(), TextView.BufferType.EDITABLE)
        todayLog.setSelection(getCursorIndex(todayLog.text!!.toString()))
        Toast.makeText(context, "Loaded file", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.add_to_log_view, container, false)
    }

}