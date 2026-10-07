package com.app.dailylog.ui.settings

import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.dailylog.MainActivity
import com.app.dailylog.R
import com.app.dailylog.repository.Shortcut
import com.app.dailylog.repository.ShortcutType
import com.app.dailylog.testutil.AppRobolectricTest
import com.google.android.material.appbar.MaterialToolbar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * The point of the restructure: configuration sits in fixed-height rows, so the shortcut list
 * keeps its space as settings are added.
 */
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w411dp-h891dp")
class SettingsStructureTest : AppRobolectricTest() {

    override val initialShortcuts = (1..8).map {
        Shortcut("label$it", "value $it", 0, ShortcutType.TEXT, it)
    }

    private fun openSettings() = launchApp().also { onView(withId(R.id.btnSettings)).perform(click()) }

    @Test
    fun settingsScreen_hasAToolbarWithTitleUpAndOverflow() {
        openSettings().use { scenario ->
            scenario.onActivity { activity ->
                val toolbar = activity.findViewById<MaterialToolbar>(R.id.settingsToolbar)
                assertEquals(activity.getString(R.string.settings_title), toolbar.title)
                assertNotNull("no up affordance", toolbar.navigationIcon)
                assertEquals(
                    activity.getString(R.string.navigate_up),
                    toolbar.navigationContentDescription
                )
                assertNotNull("overflow not in the toolbar", toolbar.menu.findItem(R.id.bulkAdd))
            }
        }
    }

    /** The up arrow is a child ImageButton carrying the navigation content description. */
    private fun MaterialToolbar.upButton(description: String): View? =
        children.filterIsInstance<ImageButton>().firstOrNull { it.contentDescription == description }

    @Test
    fun upButton_leavesSettings() {
        openSettings().use { scenario ->
            scenario.onActivity { activity ->
                val toolbar = activity.findViewById<MaterialToolbar>(R.id.settingsToolbar)
                val up = toolbar.upButton(activity.getString(R.string.navigate_up))
                assertNotNull("no up button in the toolbar", up)
                up!!.performClick()
                idleUntil(500) { false }
                assertTrue(
                    "up didn't leave Settings",
                    activity.supportFragmentManager.findFragmentById(R.id.container) !is SettingsFragment
                )
            }
        }
    }

    @Test
    fun logFileRow_opensTheSubScreenAndUpComesBack() {
        openSettings().use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.logFileRow).performClick()
                idleUntil(500) { false }
                val sub = activity.supportFragmentManager.findFragmentById(R.id.container)
                assertTrue("log file row didn't open the sub-screen", sub is LogFileSettingsFragment)

                activity.onBackPressedDispatcher.onBackPressed()
                idleUntil(500) { false }
                assertTrue(
                    "up didn't return to Settings",
                    activity.supportFragmentManager.findFragmentById(R.id.container) is SettingsFragment
                )
            }
        }
    }

    // The bar is primary-coloured and edge-to-edge: if it doesn't consume the top inset itself,
    // a pale strip is left above it. Robolectric can't see colour, but it can see the padding.
    @Test
    fun toolbar_consumesTheTopInsetSoTheBarRunsBehindTheStatusBar() {
        openSettings().use { scenario ->
            scenario.onActivity { activity ->
                val root = activity.findViewById<ViewGroup>(R.id.container).getChildAt(0)
                val toolbar = activity.findViewById<MaterialToolbar>(R.id.settingsToolbar)
                val statusBar = 64
                ViewCompat.dispatchApplyWindowInsets(
                    root,
                    WindowInsetsCompat.Builder()
                        .setInsets(
                            WindowInsetsCompat.Type.systemBars(),
                            Insets.of(0, statusBar, 0, 48)
                        )
                        .build()
                )
                assertEquals("toolbar should hold the status bar inset", statusBar, toolbar.paddingTop)
                assertEquals("root must not also pad the top", 0, root.paddingTop)
            }
        }
    }

    /**
     * The invariant this restructure exists to protect: everything between the toolbar and the
     * shortcut list is fixed-height configuration, so the list keeps the rest of the screen.
     *
     * The budget leaves room for a second row (#82's Appearance) and fails loudly if a whole
     * block of controls is put back above the list, which is what #84 would otherwise do.
     */
    @Test
    fun configurationAboveTheListStaysWithinItsBudget() {
        openSettings().use { scenario ->
            scenario.onActivity { activity ->
                val toolbar = activity.findViewById<MaterialToolbar>(R.id.settingsToolbar)
                val list = activity.findViewById<RecyclerView>(R.id.recycler_view)
                idleUntil { list.childCount > 0 }

                val density = activity.resources.displayMetrics.density
                val budgetDp = 160
                val usedDp = ((list.top - toolbar.bottom) / density).toInt()
                assertTrue(
                    "configuration above the shortcut list is ${'$'}usedDp dp, over the ${'$'}budgetDp dp budget",
                    usedDp <= budgetDp
                )
            }
        }
    }

    /** The list, not the header, absorbs the leftover height. */
    @Test
    fun shortcutListTakesTheRemainingHeight() {
        openSettings().use { scenario ->
            scenario.onActivity { activity ->
                val root = activity.findViewById<ViewGroup>(R.id.container).getChildAt(0)
                val list = activity.findViewById<RecyclerView>(R.id.recycler_view)
                idleUntil { list.childCount > 0 }
                assertEquals("list should run to the bottom", root.height, list.bottom)
            }
        }
    }
}
