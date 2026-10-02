package com.app.dailylog.utils

import android.content.Context
import android.util.TypedValue
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText

/**
 * The log editor's text size. A chosen size is in dp, not sp, so it stays the same whatever the
 * phone's font size is; with none chosen the editor keeps its theme size, which follows the phone.
 */
object EditorTextSize {
    const val MIN = 12
    // Well above a large phone font's default (about 30dp at 200%), so it can still be made bigger.
    const val MAX = 40
    const val STEP = 2

    /** The theme's size for the editor, in px, which already includes the phone's font scale. */
    fun defaultPx(context: Context): Float = TextInputEditText(context).textSize

    fun apply(view: TextView, size: Int?) {
        if (size == null) {
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultPx(view.context))
        } else {
            view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size.toFloat())
        }
    }

    /** The slider position that best shows the default size, which can fall between steps. */
    fun nearestStep(context: Context): Int =
        snap(defaultPx(context) / context.resources.displayMetrics.density)

    /** The nearest size the slider can show: Slider throws on any other value, e.g. one saved under an older range. */
    fun snap(size: Float): Int = (Math.round((size - MIN) / STEP) * STEP + MIN).coerceIn(MIN, MAX)
}
