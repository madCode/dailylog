package com.example.dailylog.ui.settings

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailylog.R
import com.example.dailylog.repository.Constants
import com.example.dailylog.repository.Shortcut
import kotlinx.android.synthetic.main.settings_view.view.*

class SettingsFragment(private val viewModel: SettingsViewModel) : Fragment(),
    AddShortcutDialogFragment.AddShortcutDialogListener,
    BulkAddShortcutsDialogFragment.BulkAddListener,
    EditShortcutDialogFragment.EditShortcutDialogListener,
    ShortcutDialogListener {

    var shortcutsLiveData: LiveData<List<Shortcut>> = viewModel.getAllShortcuts()
    private lateinit var adapter: ShortcutListAdapter

    companion object {
        fun newInstance(viewModel: SettingsViewModel) = SettingsFragment(viewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings_view, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val value = TypedValue()
        context?.theme?.resolveAttribute(R.attr.colorAccent, value, true)
        adapter = ShortcutListAdapter(
            removeCallback = { label -> viewModel.removeCallback(label) },
            updateShortcutPositions = { shortcuts ->
                viewModel.updateShortcutPositions(
                    shortcuts
                )
            },
            editCallback = { shortcut ->
                onEdit(shortcut)
            },
            cursorColor = if (value.type == TypedValue.TYPE_INT_COLOR_RGB8 || value.type == TypedValue.TYPE_INT_COLOR_RGB4  || value.type == TypedValue.TYPE_INT_COLOR_ARGB4   || value.type == TypedValue.TYPE_INT_COLOR_ARGB8) value.data else -0x10000
        )
        shortcutsLiveData.observe(viewLifecycleOwner, Observer { shortcuts ->
            // Update the cached copy of the words in the adapter.
            shortcuts.let {
                adapter.updateItems(it)
                renderShortcutInstructions()
            }
        })
        if (Build.VERSION.SDK_INT >= 26) {
            renderDateFormatRow()
        } else {
           view?.dateFormatRow?.visibility = View.INVISIBLE
        }
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
        view?.fileName?.text = viewModel.getFilename()
        view?.selectFileButton?.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/*"
                }
            startActivityForResult(Intent.createChooser(intent, "Select a file"), 111)
        }
    }

    private fun renderShortcutList() {
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
                view?.fileName?.text = viewModel.getFilename()
//                TODO("if we didn't get the permissions we needed, ask for permission or have the user select a different file")
            }
        }
    }

    private fun renderShortcutInstructions() {
        if (adapter.items.isEmpty()) {
            view!!.noShortcutsMessage.visibility = View.VISIBLE
        } else {
            view!!.noShortcutsMessage.visibility = View.GONE
        }
    }

    override fun onFinishAddShortcutDialog(label: String, text: String, cursor: Int, type: String) {
        viewModel.addShortcut(
            label,
            text,
            cursor,
            type
        )
    }

    override fun onBulkAddShortcuts(info: List<List<String>>) {
        viewModel.bulkAddShortcuts(info)
    }

    override fun onFinishEditShortcutDialog(label: String, text: String, cursor: Int, position: Int, type: String) {
        viewModel.updateShortcut(
            label,
            text,
            cursor,
            position,
            type
        )
    }

    override fun labelExists(label: String): LiveData<Boolean> {
        return viewModel.labelExists(label)
    }

}