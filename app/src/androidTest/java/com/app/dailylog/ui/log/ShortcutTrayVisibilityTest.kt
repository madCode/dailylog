package com.app.dailylog.ui.log

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.dailylog.MainActivity
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

/**
 * Reproduces: shortcut created before app close is not visible in the tray on relaunch,
 * but becomes visible after navigating to Settings and back.
 *
 * Root cause: ShortcutTrayAdapter.itemList had no custom setter, so the RecyclerView was
 * never notified to redraw when Room's LiveData delivered its value asynchronously.
 */
@RunWith(AndroidJUnit4::class)
class ShortcutTrayVisibilityTest {

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

        // Pre-populate the in-memory DB with a shortcut before the Activity is created,
        // simulating a shortcut that was created in a previous session.
        val dao = ShortcutDatabase.getDatabase(context).shortcutDao()
        runBlocking { dao.add(Shortcut("TestShortcut", "test value", 0, ShortcutType.TEXT, 0)) }
    }

    @After
    fun tearDown() {
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE).edit().clear().apply()
        context.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE).edit().clear().apply()
        ShortcutDatabase.resetForTesting()
    }

    @Test
    fun shortcut_isVisibleInTray_onLaunch() {
        val shortcutsLoaded = CountDownLatch(1)

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // Wait until the repository's LiveData has emitted the shortcut list.
            // The adapter observer is registered before this one, so by the time
            // the latch counts down the adapter has already received the data.
            scenario.onActivity { activity ->
                activity.repository.getAllShortcuts().observeForever { shortcuts ->
                    if (shortcuts.isNotEmpty()) shortcutsLoaded.countDown()
                }
            }

            assertTrue(
                "Shortcuts LiveData did not emit within 5 seconds",
                shortcutsLoaded.await(5, TimeUnit.SECONDS)
            )

            // Espresso syncs with the main thread before the check, allowing any
            // pending RecyclerView layout passes (triggered by notifyDataSetChanged)
            // to complete before asserting visibility.
            onView(withText("TestShortcut")).check(matches(isDisplayed()))
        }
    }
}
