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

        setUpShortcutTray()
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

    // Called once per view (from onViewCreated). This used to run on every onResume, which
    // swapped in a fresh, empty adapter and layout manager and stacked another LiveData
    // observer on each resume. The tray then depended on the new observer re-delivering
    // the list in time.
    private fun setUpShortcutTray() {
        val tray = binding.shortcutTray
        tray.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.HORIZONTAL)
        val adapter = ShortcutTrayAdapter(binding.todayLog)
        tray.adapter = adapter
        viewModel.getAllShortcuts().observe(viewLifecycleOwner, Observer { shortcuts ->
            adapter.itemList = shortcuts
        })

        // Keep the tray above the keyboard. The root view is already padded by the system bar
        // inset, and the IME inset includes the navigation bar, so subtract it to avoid
        // counting it twice.
        ViewCompat.setOnApplyWindowInsetsListener(tray) { view, insets ->
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            val params = view.layoutParams as ViewGroup.MarginLayoutParams
            val bottomMargin = (imeBottom - navBottom).coerceAtLeast(0)
            if (params.bottomMargin != bottomMargin) {
                params.bottomMargin = bottomMargin
                view.layoutParams = params
            }
            insets
        }
        ViewCompat.requestApplyInsets(tray)
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