package com.app.dailylog.ui.settings

import androidx.fragment.app.DialogFragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.dailylog.MainActivity
import com.app.dailylog.R
import com.app.dailylog.testutil.AppRobolectricTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

// Full screen on compact windows; floating once the window is medium width and not compact height.
@RunWith(AndroidJUnit4::class)
class ShortcutDialogSizeTest : AppRobolectricTest() {

    private fun assertAddDialogFloating(expected: Boolean) {
        launchApp().use { scenario ->
            onView(withId(R.id.btnSettings)).perform(click())
            onView(withId(R.id.addShortcutButton)).perform(click())
            scenario.onActivity { activity: MainActivity ->
                val settings = activity.supportFragmentManager.findFragmentById(R.id.container) as SettingsFragment
                val dialog = settings.childFragmentManager.findFragmentByTag("fragment_add_shortcut") as DialogFragment
                val window = dialog.requireDialog().window!!
                assertEquals(expected, window.isFloating)
                val screenWidth = activity.window.decorView.width
                val dialogWidth = window.decorView.width
                if (expected) {
                    assert(dialogWidth < screenWidth) { "dialog $dialogWidth px should be narrower than $screenWidth px" }
                } else {
                    assertEquals(screenWidth, dialogWidth)
                    assertEquals(activity.window.decorView.height, window.decorView.height)
                }
            }
        }
    }

    @Test
    fun phone_isFullScreen() = assertAddDialogFloating(false)

    @Test
    @Config(qualifiers = "w800dp-h360dp-land")
    fun landscapePhone_isFullScreen() = assertAddDialogFloating(false)

    @Test
    @Config(qualifiers = "w840dp-h900dp")
    fun unfoldedFoldable_isFloating() = assertAddDialogFloating(true)
}
