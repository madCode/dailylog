package com.app.dailylog

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
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
import com.app.dailylog.testutil.grantStoragePermission
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class ActivityRecreationTest {

    @get:Rule
    val storagePermission = grantStoragePermission()

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

    @Test
    fun logScreen_survivesRecreation() {
        launchWithShortcutsLoaded().use { scenario ->
            scenario.recreate()

            onView(withId(R.id.todayLog)).check(matches(isDisplayed()))
            onView(withText("TestShortcut")).check(matches(isDisplayed()))
        }
    }

    @Test
    fun settingsScreen_survivesRecreation_andBackReturnsToLog() {
        launchWithShortcutsLoaded().use { scenario ->
            onView(withId(R.id.btnSettings)).perform(click())
            onView(withId(R.id.addShortcutButton)).check(matches(isDisplayed()))

            scenario.recreate()

            onView(withId(R.id.addShortcutButton)).check(matches(isDisplayed()))
            onView(withText("TestShortcut")).check(matches(isDisplayed()))

            scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            onView(withId(R.id.todayLog)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun addShortcutDialog_survivesRecreation() {
        launchWithShortcutsLoaded().use { scenario ->
            onView(withId(R.id.btnSettings)).perform(click())
            onView(withId(R.id.addShortcutButton)).perform(click())
            // Otherwise, on the small API 23 emulator, the keyboard pushes the dialog title off screen.
            closeSoftKeyboard()
            onView(withId(R.id.addShortcutTitle)).inRoot(isDialog()).check(matches(isDisplayed()))

            scenario.recreate()
            closeSoftKeyboard()

            onView(withId(R.id.addShortcutTitle)).inRoot(isDialog()).check(matches(isDisplayed()))
        }
    }
}
