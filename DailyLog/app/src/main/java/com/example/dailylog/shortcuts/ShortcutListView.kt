package com.example.dailylog.shortcuts

import android.graphics.Color
import android.view.View
import android.view.View.generateViewId
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.*
import com.example.dailylog.R


class ShortcutListView(private var view: View) : ShortcutListPresenter.View {
    private lateinit var label: EditText
    private lateinit var text: EditText
    private lateinit var cursor: EditText
    private var presenter: ShortcutListPresenter? = null;

    fun initializeView(presenter: ShortcutListPresenter) {
        this.presenter = presenter
    }

    fun renderView() {
        presenter!!.initializePresenter()
        label = view.findViewById(R.id.labelInput)
        text = view.findViewById(R.id.textInput)
        cursor = view.findViewById(R.id.cursorInput)
        text.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (cursor.text.equals("")) {
                    cursor.setText((text.text.toString().length -1).toString())
                }
                return@OnEditorActionListener true
            }
            false
        })
        val btnSaveShortcut = view.findViewById<ImageButton>(R.id.btnSaveShortcut)
        btnSaveShortcut.setOnClickListener{
            val labelString = label.text.toString()
            val textString = text.text.toString()
            val cursorInt = Integer.parseInt(cursor.text.toString())
            val added = presenter?.addShortcut(labelString, textString, cursorInt)
            if (added == true) {
                label.text.clear()
                text.text.clear()
                cursor.text.clear()
            } else {
                // TODO: show an error and also validate that cursorInt is shorter than textString
            }
        }
    }

    override fun renderShortcutList(shortcutList: MutableSet<String>) {
        val linearLayout = view.findViewById<LinearLayout>(R.id.shortcutList)
        linearLayout.removeAllViews()
        shortcutList.forEachIndexed {index, category ->
            linearLayout.addView(getShortcutRow(category, index == shortcutList.size-1))
        }
    }

    private fun getShortcutRow(category: String, lastRow: Boolean): ConstraintLayout {
        val removeButton = ImageButton(view.context)
        removeButton.setImageResource(R.drawable.ic_delete_outline_24px)
        removeButton.setBackgroundColor(Color.TRANSPARENT)
        removeButton.setOnClickListener {
            presenter?.removeShortcut(category)
        }
        removeButton.id = generateViewId()

        return createShortcutRow(category, arrayListOf(removeButton), lastRow)
    }

    private fun createShortcutRow(shortcut: String, buttonList: ArrayList<ImageButton>, lastRow: Boolean): ConstraintLayout {
        val row = ConstraintLayout(view.context)
//        if (!lastRow) {
        row.setBackgroundResource(R.drawable.underline)
//        }

        val textView = TextView(view.context)
        textView.text = shortcut
        textView.layoutParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        textView.id = generateViewId()

        row.addView(textView)

        val set = ConstraintSet()

        buttonList.forEach {
            row.addView(it)
        }

        set.clone(row)
        set.connect(textView.id, START, PARENT_ID, START, 30)
        set.connect(textView.id, TOP, PARENT_ID, TOP)
        set.connect(textView.id, BOTTOM, PARENT_ID, BOTTOM)
        for (i in 0 until buttonList.size) {
            val button = buttonList[i]
            if (i >= buttonList.size-1) {
                set.connect(button.id, END, PARENT_ID, END, 30)
            } else {
                set.connect(button.id, END, buttonList[i+1].id, START, 10)
            }
            set.connect(button.id, TOP, textView.id, TOP)
            set.connect(button.id, BOTTOM, textView.id, BOTTOM)
        }

        set.applyTo(row)
        return row
    }

    override fun setTitle(title: Int) {
        val shortcutTitle = view.findViewById<TextView>(R.id.shortcutTitle)
        shortcutTitle.setText(title)
    }

    override fun setDescription(descriptionResId: Int) {
        val shortcutTitle = view.findViewById<TextView>(R.id.shortcutDescription)
        shortcutTitle.setText(descriptionResId)
    }
}