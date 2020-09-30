package com.example.dailylog.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.dailylog.R
import kotlinx.android.synthetic.main.add_to_log_view.view.*


class LogView : Fragment() {

    companion object {
        fun newInstance() = LogView()
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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.add_to_log_view, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = LogViewModel()
        if (view == null) {
            return
        }
        val todayLog = view!!.todayLog
        todayLog.setText(viewModel.getLog(), TextView.BufferType.EDITABLE)
        todayLog.setSelection(viewModel.cursorIndex)

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
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, SettingsView.newInstance())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onPause() {
        super.onPause()
        save()
    }

}