package com.app.dailylog

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.dailylog.repository.Constants
import com.app.dailylog.repository.Shortcut
import com.app.dailylog.repository.ShortcutDatabase
import com.app.dailylog.repository.ShortcutType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class ActivityRecreationTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setUp() {
        ShortcutDatabase.TEST_MODE = true

        // Suppress the startup warning dialog (shown for first 3 launches)
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            .edit().putInt("num_launches", 3).apply()

        // Set a non-empty filename so MainActivity opens LogFragment instead of WelcomeFragment
        context.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE)
            .edit().putString(Constants.FILENAME_PREF_KEY, "content://fake/file").apply()

        val dao = ShortcutDatabase.getDatabase(context).shortcutDao()
        runBlocking { dao.add(Shortcut("TestShortcut", "test value", 0, ShortcutType.TEXT, 0)) }
    }

    @After
    fun tearDown() {
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE).edit().clear().apply()
        context.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE).edit().clear().apply()
        ShortcutDatabase.resetForTesting()
    }

    // The tray grows when shortcuts load, shifting the buttons above it, so wait before tapping.
    private fun launchWithShortcutsLoaded(): ActivityScenario<MainActivity> {
        val loaded = CountDownLatch(1)
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            activity.repository.getAllShortcuts().observeForever { if (it.isNotEmpty()) loaded.countDown() }
        }
        assertTrue("Shortcuts did not load within 5 seconds", loaded.await(5, TimeUnit.SECONDS))
        return scenario
    }

    // Espresso gives the root view 10 seconds to hold window focus and stop requesting layout
    // before it gives up with RootViewWithoutFocusException. A freshly recreated activity on the
    // API 23 emulator does not always get there in time, which failed all three of these tests
    // on #108 and #112 and then passed on a re-run.
    //
    // Best effort, deliberately not an assertion: when a dialog is open it holds the focus and
    // the activity's own decor view never regains it, so requiring focus here failed
    // addShortcutDialog_survivesRecreation on every API level. Give the window a few seconds to
    // settle and let Espresso be the one to judge.
    private fun ActivityScenario<MainActivity>.recreateAndSettle() {
        recreate()
        val deadline = System.currentTimeMillis() + 5_000
        while (System.currentTimeMillis() < deadline) {
            var settled = false
            onActivity { settled = it.window.decorView.let { v -> v.hasWindowFocus() && !v.isLayoutRequested } }
            if (settled) return
            Thread.sleep(50)
        }
    }

    // The log field's keyboard can cover the settings button on a small screen.
    private fun openSettings() {
        onView(withId(R.id.btnSettings)).perform(closeSoftKeyboard(), click())
    }

    @Test
    fun logScreen_survivesRecreation() {
        launchWithShortcutsLoaded().use { scenario ->
            scenario.recreateAndSettle()

            onView(withId(R.id.todayLog)).check(matches(isDisplayed()))
            onView(withText("TestShortcut")).check(matches(isDisplayed()))
        }
    }

    @Test
    fun settingsScreen_survivesRecreation_andBackReturnsToLog() {
        launchWithShortcutsLoaded().use { scenario ->
            openSettings()
            onView(withId(R.id.addShortcutButton)).check(matches(isDisplayed()))

            scenario.recreateAndSettle()

            onView(withId(R.id.addShortcutButton)).check(matches(isDisplayed()))
            onView(withText("TestShortcut")).check(matches(isDisplayed()))

            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            onView(withId(R.id.todayLog)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun addShortcutDialog_survivesRecreation() {
        launchWithShortcutsLoaded().use { scenario ->
            openSettings()
            onView(withId(R.id.addShortcutButton)).perform(click())
            // On the small API 23 emulator the open keyboard pushes the dialog title off screen.
            val dialogTitle = onView(withId(R.id.addShortcutTitle)).inRoot(isDialog())
            dialogTitle.perform(closeSoftKeyboard()).check(matches(isDisplayed()))

            scenario.recreateAndSettle()

            dialogTitle.perform(closeSoftKeyboard()).check(matches(isDisplayed()))
        }
    }
}
