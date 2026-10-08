package com.app.dailylog.ui.settings

import android.content.res.Configuration
import android.view.View
import android.view.Window
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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

/**
 * The settings bar is pale mint in both themes, so the status bar icons drawn over it must be
 * dark in both. In night mode the system would otherwise draw them white, which is 1.39:1
 * against #BEE3DB and effectively invisible.
 */
fun useDarkStatusBarIcons(window: Window) {
    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
}

/** Hands the status bar back to the theme: dark icons on the light theme, light on the dark one. */
fun restoreThemeStatusBarIcons(window: Window) {
    val night = (window.context.resources.configuration.uiMode and
        Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = !night
}
