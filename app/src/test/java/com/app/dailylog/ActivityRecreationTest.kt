package com.app.dailylog

import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.dailylog.repository.Shortcut
import com.app.dailylog.repository.ShortcutType
import com.app.dailylog.testutil.AppRobolectricTest
import com.app.dailylog.ui.log.LogFragment
import com.app.dailylog.ui.settings.EditShortcutDialogFragment
import com.app.dailylog.ui.settings.SettingsFragment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Dark mode switches, rotation and process death recreate MainActivity, and Android recreates
 * whichever fragments were showing. JVM counterpart of the instrumentation test of the same name.
 */
@RunWith(AndroidJUnit4::class)
class ActivityRecreationTest : AppRobolectricTest() {

    override val initialShortcuts = listOf(Shortcut("TestShortcut", "test value", 2, ShortcutType.TEXT, 0))

    private fun MainActivity.currentFragment(): Fragment? =
        supportFragmentManager.findFragmentById(R.id.container)

    @Test
    fun logScreen_survivesRecreation() {
        launchApp().use { scenario ->
            scenario.recreate()
            scenario.onActivity { assertTrue(it.currentFragment() is LogFragment) }
            onView(withId(R.id.todayLog)).check(matches(withText("Existing log\n")))
        }
    }

    @Test
    fun settingsScreen_survivesRecreation_andBackReturnsToLog() {
        launchApp().use { scenario ->
            onView(withId(R.id.btnSettings)).perform(click())
            scenario.recreate()

            scenario.onActivity { assertTrue(it.currentFragment() is SettingsFragment) }
            onView(withId(R.id.addShortcutButton)).check(matches(isDisplayed()))

            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            scenario.onActivity { assertTrue(it.currentFragment() is LogFragment) }
        }
    }

    @Test
    fun addShortcutDialog_survivesRecreation() {
        launchApp().use { scenario ->
            onView(withId(R.id.btnSettings)).perform(click())
            onView(withId(R.id.addShortcutButton)).perform(click())
            scenario.recreate()

            scenario.onActivity { activity ->
                val settings = activity.currentFragment() as SettingsFragment
                val dialog = settings.childFragmentManager.findFragmentByTag("fragment_add_shortcut")
                assertTrue(dialog is DialogFragment && dialog.dialog?.isShowing == true)
            }
        }
    }

    @Test
    fun editShortcutDialog_keepsShortcutAcrossRecreation() {
        launchApp().use { scenario ->
            onView(withId(R.id.btnSettings)).perform(click())
            scenario.onActivity { idleUntil { it.findViewById<android.view.View>(R.id.label) != null } }
            onView(withText("TestShortcut")).perform(click())
            scenario.recreate()

            scenario.onActivity { activity ->
                val settings = activity.currentFragment() as SettingsFragment
                val dialog = settings.childFragmentManager
                    .findFragmentByTag("fragment_edit") as EditShortcutDialogFragment
                val root = dialog.requireView()
                assertEquals("TestShortcut", root.findViewById<EditText>(R.id.labelInput).text.toString())
                assertEquals("test value", root.findViewById<EditText>(R.id.textInput).text.toString())
            }
        }
    }
}
