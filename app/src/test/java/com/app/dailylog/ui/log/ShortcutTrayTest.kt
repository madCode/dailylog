package com.app.dailylog.ui.log

import android.view.View
import android.widget.EditText
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.dailylog.MainActivity
import com.app.dailylog.R
import com.app.dailylog.repository.Shortcut
import com.app.dailylog.repository.ShortcutType
import com.app.dailylog.testutil.AppRobolectricTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShortcutTrayTest : AppRobolectricTest() {

    override val initialShortcuts = listOf(
        Shortcut("First", "one", 3, ShortcutType.TEXT, 0),
        Shortcut("Second", "two", 3, ShortcutType.TEXT, 1),
    )

    private fun MainActivity.tray(): RecyclerView = findViewById(R.id.shortcutTray)

    private fun assertTrayShowsAllShortcuts(scenario: ActivityScenario<MainActivity>) {
        scenario.onActivity { activity ->
            idleUntil { activity.tray().childCount == initialShortcuts.size }
            val tray = activity.tray()
            assertEquals("adapter item count", initialShortcuts.size, tray.adapter?.itemCount)
            assertEquals("chips laid out", initialShortcuts.size, tray.childCount)
            assertTrue("tray has height", tray.height > 0)
            assertEquals(View.VISIBLE, tray.visibility)
        }
    }

    @Test
    fun coldStart_showsShortcuts() {
        launchApp().use { assertTrayShowsAllShortcuts(it) }
    }

    @Test
    fun returningFromBackground_showsShortcuts() {
        launchApp().use { scenario ->
            assertTrayShowsAllShortcuts(scenario)
            scenario.moveToState(Lifecycle.State.CREATED)
            scenario.moveToState(Lifecycle.State.RESUMED)
            assertTrayShowsAllShortcuts(scenario)
        }
    }

    @Test
    fun activityRecreation_showsShortcuts() {
        launchApp().use { scenario ->
            assertTrayShowsAllShortcuts(scenario)
            scenario.recreate()
            assertTrayShowsAllShortcuts(scenario)
        }
    }

    @Test
    fun tappingShortcut_insertsTextAtCursor() {
        launchApp().use { scenario ->
            assertTrayShowsAllShortcuts(scenario)
            scenario.onActivity { activity ->
                val log = activity.findViewById<EditText>(R.id.todayLog)
                log.setSelection(0)
                activity.tray().getChildAt(0).performClick()
                assertTrue(log.text.toString().startsWith("oneExisting log"))
                assertEquals(3, log.selectionStart)
            }
        }
    }

    @Test
    fun keyboardShown_trayStaysAboveKeyboard() {
        val navBarHeight = 48
        launchApp().use { scenario ->
            assertTrayShowsAllShortcuts(scenario)
            scenario.onActivity { activity ->
                val root = activity.window.decorView
                // Realistic size relative to the (small) Robolectric screen; a keyboard taller
                // than the screen pushes everything off-screen and makes the check meaningless.
                val keyboardHeight = root.height * 2 / 5
                val insets = WindowInsetsCompat.Builder()
                    .setInsets(WindowInsetsCompat.Type.systemBars(), Insets.of(0, 0, 0, navBarHeight))
                    // IME insets are measured from the bottom of the screen, nav bar included
                    .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, keyboardHeight))
                    .setVisible(WindowInsetsCompat.Type.ime(), true)
                    .build()
                ViewCompat.dispatchApplyWindowInsets(root, insets)
                idleUntil { !root.isLayoutRequested }

                val tray = activity.tray()
                val location = IntArray(2)
                tray.getLocationInWindow(location)
                val trayBottom = location[1] + tray.height
                val keyboardTop = root.height - keyboardHeight
                assertTrue("tray is on screen (top ${location[1]})", location[1] >= 0)
                assertTrue(
                    "tray bottom ($trayBottom) should be at or above keyboard top ($keyboardTop)",
                    trayBottom <= keyboardTop
                )
            }
        }
    }
}
