package com.example.dailylog.ui.log

import android.os.Build
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
import com.example.dailylog.R
import com.example.dailylog.utils.getDateString
import kotlinx.android.synthetic.main.add_to_log_view.view.*


class LogFragment(private val viewModel: LogViewModel, private val goToSettings: () -> Unit) : Fragment() {

    companion object {
        fun newInstance(viewModel: LogViewModel, goToSettings: () -> Unit) = LogFragment(
            viewModel,
            goToSettings
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (view == null || context == null || activity == null) {
            error("view or context or activity is null")
        }
        val todayLog = view!!.todayLog

        view!!.btnSave.setOnClickListener {
            save()
        }

        if (Build.VERSION.SDK_INT >= 26) {
            view!!.btnDate.visibility = View.VISIBLE
            view!!.btnDate.setOnClickListener {
                val start: Int = todayLog.selectionStart
                val dateString: String = viewModel.getDateString()
                todayLog.text?.insert(start, dateString)
                todayLog.setSelection(start + dateString.length)
            }
        } else {
            view!!.btnDate.visibility = View.GONE
        }

        view!!.btnSettings.setOnClickListener {
            save()
            goToSettings()
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
        todayLog.setSelection(getCursorIndex(todayLog.text.toString()))
        Toast.makeText(context, "Saved file", Toast.LENGTH_SHORT).show()
    }

    private fun renderShortcutTray() {
        if (context == null) {
            return
        }
        val shortcutsLiveData = viewModel.getAllShortcuts()
        val tray = view!!.shortcutTray
        tray.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.HORIZONTAL)
        //tray.layoutManager = GridLayoutManager(context, 5)
        val adapter = ShortcutTrayAdapter(view!!.todayLog)
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
        val todayLog = view!!.todayLog
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