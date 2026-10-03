package com.app.dailylog.ui.log

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.ScrollView
import android.widget.TextView

/**
 * A [ScrollView] that stops scrolling back to the editor's cursor once the user has scrolled
 * away from it, until they type or move the cursor.
 *
 * The editor asks to bring its cursor into view on every relayout (spell check, keyboard
 * opening or closing), not only on edits, so mid-scroll the page snapped back to the cursor.
 */
class LogScrollView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    ScrollView(context, attrs) {

    private data class Cursor(val textLength: Int, val selectionStart: Int, val selectionEnd: Int)

    private var touching = false

    // The editor's cursor when the user last scrolled; null while the view follows the cursor.
    private var cursorWhenScrolledAway: Cursor? = null

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_DOWN) touching = true
        val handled = super.dispatchTouchEvent(ev)
        if (ev.actionMasked == MotionEvent.ACTION_UP || ev.actionMasked == MotionEvent.ACTION_CANCEL) {
            touching = false
        }
        return handled
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (touching && t != oldt) userScrolled()
    }

    override fun performAccessibilityAction(action: Int, arguments: Bundle?): Boolean {
        val handled = super.performAccessibilityAction(action, arguments)
        if (handled && action in accessibilityScrollActions) userScrolled()
        return handled
    }

    override fun requestChildRectangleOnScreen(child: View, rectangle: Rect, immediate: Boolean): Boolean {
        val scrolledAwayFrom = cursorWhenScrolledAway
        if (scrolledAwayFrom != null) {
            if (cursorOf(child) == scrolledAwayFrom) return false
            cursorWhenScrolledAway = null
        }
        return super.requestChildRectangleOnScreen(child, rectangle, immediate)
    }

    private fun userScrolled() {
        cursorWhenScrolledAway = cursorOf(getChildAt(0))
    }

    private fun cursorOf(child: View?): Cursor? =
        (child as? TextView)?.let { Cursor(it.text.length, it.selectionStart, it.selectionEnd) }

    private companion object {
        val accessibilityScrollActions = setOf(
            AccessibilityNodeInfo.ACTION_SCROLL_FORWARD,
            AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD,
            android.R.id.accessibilityActionScrollUp,
            android.R.id.accessibilityActionScrollDown,
        )
    }
}
