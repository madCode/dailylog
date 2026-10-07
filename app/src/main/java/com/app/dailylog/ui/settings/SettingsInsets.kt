package com.app.dailylog.ui.settings

import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Keeps content clear of the system bars while letting [toolbar] draw behind the status bar.
 *
 * The activity is edge-to-edge, and from Android 15 the status bar is always transparent, so a
 * primary-coloured bar that doesn't consume the top inset itself leaves a pale strip above it.
 */
fun applySettingsInsets(root: View, toolbar: View) {
    ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
        val bars: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.setPadding(bars.left, 0, bars.right, bars.bottom)
        toolbar.updatePadding(top = bars.top)
        insets
    }
    ViewCompat.requestApplyInsets(root)
}
