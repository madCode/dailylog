package com.example.dailylog.ui.log

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.example.dailylog.R
import com.example.dailylog.repository.Repository
import com.example.dailylog.ui.settings.SettingsView
import kotlinx.android.synthetic.main.add_to_log_view.view.*


class LogView(private var repository: Repository) : Fragment() {

    companion object {
        fun newInstance(repository: Repository) = LogView(repository)
    }

    private lateinit var viewModel: LogViewModel

    private fun save() {
        if (view == null) {
            return
        }
        val todayLog = view!!.todayLog
        if (todayLog.text != null && todayLog.text!!.isNotEmpty()) {
            viewModel.saveCursorIndex(todayLog.selectionStart)
        }
        viewModel.save(todayLog.text.toString())
        todayLog.setText(viewModel.getLog(), TextView.BufferType.EDITABLE)
        todayLog.setSelection(viewModel.cursorIndex)
        Toast.makeText(context, "Saved file", Toast.LENGTH_SHORT).show()
    }

    private fun renderShortcutTray() {
        if (context == null) {
            return
        }
        val adapter = ShortcutTrayAdapter(context!!, view!!.todayLog, R.layout.shortcut_layout, repository.getAllShortcuts())
        val tray = view!!.shortcutTray
        tray.adapter = adapter
    }

    private fun loadFile() {
        val todayLog = view!!.todayLog
        todayLog.setText(viewModel.getLog(), TextView.BufferType.EDITABLE)
        val cursorIndex = if (viewModel.cursorIndex > -1 && viewModel.cursorIndex < todayLog.text!!.length) viewModel.cursorIndex else todayLog.text!!.length
        todayLog.setSelection(cursorIndex)
        Toast.makeText(context, "Loaded file", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.add_to_log_view, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (view == null || context == null || activity == null) {
            error("view or context or activity is null")
        }
        viewModel = LogViewModel(repository)
        val todayLog = view!!.todayLog

        view!!.btnSave.setOnClickListener {
            save()
        }

        view!!.btnDate.setOnClickListener {
            val start: Int = todayLog.selectionStart
            val dateString: String = viewModel.getDateString()
            todayLog.text?.insert(start, dateString)
            todayLog.setSelection(start + dateString.length)
        }

        view!!.btnSettings.setOnClickListener {
            save()
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, SettingsView.newInstance(repository))
                .addToBackStack(null)
                .commit()
        }
        renderShortcutTray()
    }

    override fun onPause() {
        super.onPause()
        save()
    }

    override fun onResume() {
        // gets called onResume but also after onActivityCreated
        super.onResume()
        loadFile()
        view!!.todayLog.requestFocus()
        val inputMethodManager =
            getSystemService(context!!, InputMethodManager::class.java) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

}