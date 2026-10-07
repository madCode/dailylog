package com.app.dailylog.ui.log

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ScrollView
import android.widget.TextView

/**
 * A [ScrollView] for the log editor that stops scrolling back to the cursor once the cursor has
 * been scrolled off screen, until the text or the cursor changes.
 *
 * The editor asks to bring its cursor into view on every relayout (spell check, keyboard opening
 * or closing), not only on edits; honouring those while the user reads elsewhere yanks the page back.
 */
class LogScrollView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    ScrollView(context, attrs) {

    // The editor's selection when the user last scrolled the cursor off screen; null while following it.
    private var offscreenSelection: Pair<Int, Int>? = null

    // Off while this view animates toward the cursor, so those frames don't count as scrolling away.
    private var userScrolling = false

    private val editor: TextView? get() = getChildAt(0) as? TextView

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        (child as? TextView)?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                offscreenSelection = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        userScrolling = true
        return super.dispatchTouchEvent(ev)
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        userScrolling = true
        return super.onGenericMotionEvent(event)
    }

    override fun performAccessibilityAction(action: Int, arguments: Bundle?): Boolean {
        userScrolling = true
        return super.performAccessibilityAction(action, arguments)
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        val editor = editor
        if (!userScrolling || editor == null) return
        offscreenSelection = if (isCursorOnScreen(editor)) null else selectionOf(editor)
    }

    override fun requestChildRectangleOnScreen(child: View, rectangle: Rect, immediate: Boolean): Boolean {
        val offscreen = offscreenSelection
        if (offscreen != null) {
            if (child === editor && selectionOf(child as TextView) == offscreen) return false
            offscreenSelection = null
        }
        val scrolled = super.requestChildRectangleOnScreen(child, rectangle, immediate)
        if (scrolled) userScrolling = false
        return scrolled
    }

    private fun selectionOf(editor: TextView) = editor.selectionStart to editor.selectionEnd

    // True unless the cursor's whole line is outside the viewport.
    private fun isCursorOnScreen(editor: TextView): Boolean {
        val layout = editor.layout ?: return true
        val cursor = editor.selectionEnd
        if (cursor < 0) return true
        val line = layout.getLineForOffset(cursor)
        val textTop = editor.top + editor.totalPaddingTop
        return textTop + layout.getLineBottom(line) > scrollY &&
            textTop + layout.getLineTop(line) < scrollY + height
    }
}
