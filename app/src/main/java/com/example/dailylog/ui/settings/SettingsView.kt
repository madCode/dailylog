package com.example.dailylog.ui.settings

import android.app.Application
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailylog.R
import com.example.dailylog.repository.Constants
import com.example.dailylog.repository.Repository
import com.example.dailylog.repository.Shortcut
import kotlinx.android.synthetic.main.settings_view.view.*

class SettingsView(private val application: Application, private var repository: Repository) : Fragment(),
    AddShortcutDialogFragment.AddShortcutDialogListener,
    BulkAddShortcutsDialogFragment.BulkAddListener,
    EditShortcutDialogFragment.EditShortcutDialogListener,
    ShortcutDialogListener {

    var shortcutsLiveData: LiveData<List<Shortcut>> = repository.getAllShortcuts()

    companion object {
        fun newInstance(application: Application, repository: Repository) = SettingsView(application, repository)
    }

    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings_view, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, SettingsViewModelFactory(application, repository, context) { shortcut ->
            onEdit(
                shortcut
            )
        }).get(SettingsViewModel::class.java)
        shortcutsLiveData.observe(viewLifecycleOwner, Observer { shortcuts ->
            // Update the cached copy of the words in the adapter.
            shortcuts.let {
                viewModel.shortcutListAdapter.updateItems(it)
                repository.setShortcutList(it)
                renderShortcutInstructions()
            }
        })
        renderDateFormatRow()
        renderFileNameRow()
        renderShortcutList()
        view?.addShortcutButton?.setOnClickListener {
            val addDialog: AddShortcutDialogFragment =
                AddShortcutDialogFragment.newInstance()
            addDialog.setTargetFragment(this, 300)
            addDialog.show(parentFragmentManager, "fragment_add_shortcut")
        }
        view?.addShortcutButton?.setOnLongClickListener {
            val addBulkDialog: BulkAddShortcutsDialogFragment =
                BulkAddShortcutsDialogFragment.newInstance()
            addBulkDialog.setTargetFragment(this, 300)
            addBulkDialog.show(parentFragmentManager, "fragment_bulk_add")
            return@setOnLongClickListener true
        }
    }

    private fun onEdit(shortcut: Shortcut) {
        val editDialog: EditShortcutDialogFragment = EditShortcutDialogFragment.newInstance(shortcut)
        editDialog.setTargetFragment(this, 300)
        editDialog.show(parentFragmentManager, "fragment_edit")
    }

    private fun renderDateFormatRow() {
        val dateFormatEditText = view?.dateFormat
        dateFormatEditText?.setText(viewModel.dateTimeFormat, TextView.BufferType.EDITABLE)
        dateFormatEditText?.hint = context?.resources?.getString(
            R.string.defaultStringPlaceholder,
            Constants.DATE_TIME_DEFAULT_FORMAT
        )
        dateFormatEditText?.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val saved = viewModel.saveDateTimeFormat(v.text.toString())
                if (!saved) {
                    dateFormatEditText.setTextColor(Color.RED)
                    Toast.makeText(
                        context,
                        "Unsupported date format, try something else",
                        Toast.LENGTH_LONG
                    ).show()
                    return@OnEditorActionListener true
                }
            }
            false
        })
    }

    private fun renderFileNameRow() {
        view?.fileName?.text = viewModel.filename
        view?.selectFileButton?.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                }
            startActivityForResult(Intent.createChooser(intent, "Select a file"), 111)
        }
    }

    private fun renderShortcutList() {
        val adapter = viewModel.shortcutListAdapter
        val recyclerView = view!!.recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        val callback: ItemTouchHelper.Callback = ShortcutTouchHelperCallback(adapter)
        val shortcutTouchHelper = ItemTouchHelper(callback)
        shortcutTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 111 && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val selectedFileUri = data.data;
            if (selectedFileUri != null) {
                viewModel.saveFilename(selectedFileUri.toString())
                val contentResolver = context!!.contentResolver
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                contentResolver.takePersistableUriPermission(selectedFileUri, takeFlags)
                view?.fileName?.text = viewModel.filename
//                TODO("if we didn't get the permissions we needed, ask for permission or have the user select a different file")
            }
        }
    }

    private fun renderShortcutInstructions() {
        if (viewModel.shortcutListAdapter.items.isEmpty()) {
            view!!.noShortcutsMessage.visibility = View.VISIBLE
        } else {
            view!!.noShortcutsMessage.visibility = View.GONE
        }
    }

    override fun onFinishAddShortcutDialog(label: String, text: String, cursor: Int) {
        viewModel.addShortcut(
            label,
            text,
            cursor
        )
    }

    override fun onBulkAddShortcuts(info: List<List<String>>) {
        viewModel.bulkAddShortcuts(info)
    }

    override fun onFinishEditShortcutDialog(label: String, text: String, cursor: Int) {
        viewModel.updateShortcut(
            label,
            text,
            cursor
        )
    }

    override fun labelIsUnique(label: String): Boolean {
        return repository.labelIsUnique(label)
    }

}