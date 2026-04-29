package com.app.dailylog.ui.log

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.ScrollView
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.app.dailylog.MainActivity
import com.app.dailylog.R
import com.app.dailylog.repository.Shortcut
import com.app.dailylog.repository.ShortcutDatabase
import com.app.dailylog.repository.ShortcutType
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Regression test for issue #45: the shortcut tray covered the bottom of the log view,
 * preventing users from scrolling to content inserted via a multi-line shortcut.
 *
 * Uses ActivityScenario.onActivity directly (no Espresso perform()) to avoid the
 * InputManager compatibility issue with API 35+.
 */
@RunWith(AndroidJUnit4::class)
class LogScrollTest {

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var db: ShortcutDatabase

    companion object {
        private const val SHORTCUT_LABEL = "MultilineShortcut"
        private val SHORTCUT_VALUE =
            "First line of the shortcut text inserted here.\n" +
            "Second line follows right after this one.\n" +
            "Third line keeps going with more content.\n" +
            "Fourth line ends the shortcut block here.\n"

        // 100 numbered lines — well over 2000 chars
        private val LONG_TEXT = (1..100).joinToString("\n") { i -> "Line $i: ${"x".repeat(25)}" }

        private const val FAKE_IME_HEIGHT_PX = 800
        private const val FAKE_NAV_BAR_PX = 100
    }

    @Before
    fun setUp() {
        ShortcutDatabase.TEST_MODE = true
        db = ShortcutDatabase.getDatabase(context)
        runBlocking {
            db.shortcutDao().add(
                Shortcut(
                    id = "test-scroll-shortcut",
                    label = SHORTCUT_LABEL,
                    value = SHORTCUT_VALUE,
                    cursorIndex = 0,
                    type = ShortcutType.TEXT,
                    position = 0
                )
            )
        }

        // Non-empty URI so MainActivity opens LogFragment rather than WelcomeFragment;
        // readFile() handles an unresolvable URI gracefully and returns "".
        context.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE)
            .edit().putString("filenameFormat", "content://com.app.dailylog.test/fake").apply()

        // Prevent the startup migration dialog from blocking the test
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            .edit().putInt("num_launches", 3).apply()
    }

    @After
    fun tearDown() {
        ShortcutDatabase.resetInstance()
        context.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE)
            .edit().clear().apply()
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            .edit().clear().apply()
    }

    /**
     * Mirrors the manual repro steps for issue #45:
     *
     * 1. Document has 2000+ characters.
     * 2. Shortcut has 50+ chars and 3+ newlines (configured in @Before).
     * 3. Scroll so there is text above and below the viewport.
     * 4. Verify we can scroll all the way to the bottom.
     * 5. Scroll back to the middle and use the shortcut.
     * 6. Verify we can STILL scroll to the true bottom — the fix.
     *
     * The keyboard is simulated by dispatching fake IME insets to the fragment root,
     * exercising the same inset listener path that the real keyboard triggers.
     */
    @Test
    fun canScrollToBottomAfterMultilineShortcutInsert() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->

            // Step 1: load 2000+ char document
            scenario.onActivity { activity ->
                activity.findViewById<EditText>(R.id.todayLog).setText(LONG_TEXT)
            }
            waitForIdle()

            // Step 3: scroll to roughly the middle of the document
            scrollToFraction(scenario, 0.5f)
            waitForIdle()

            // Step 4 (pre-insert baseline): scroll to the bottom and assert no tray overlap
            scrollToBottom(scenario)
            waitForIdle()
            assertScrollViewEndsAtTray(scenario, "before shortcut insert")

            // Return to the middle and place cursor there
            scrollToFraction(scenario, 0.5f)
            scenario.onActivity { activity ->
                activity.findViewById<EditText>(R.id.todayLog)
                    .setSelection(LONG_TEXT.length / 2)
            }
            waitForIdle()

            // Step 5: tap the shortcut chip to insert multi-line text at the cursor
            scenario.onActivity { activity ->
                val tray = activity.findViewById<RecyclerView>(R.id.shortcutTray)
                var found = false
                for (i in 0 until tray.childCount) {
                    val button = tray.getChildAt(i) as? MaterialButton ?: continue
                    if (button.text.toString() == SHORTCUT_LABEL) {
                        button.performClick()
                        found = true
                        break
                    }
                }
                assertTrue(
                    "Shortcut button '$SHORTCUT_LABEL' not found in tray " +
                        "(childCount=${tray.childCount})",
                    found
                )
            }
            waitForIdle()

            // Simulate keyboard being visible by dispatching fake IME insets.
            // This exercises the same setOnApplyWindowInsetsListener path the real IME triggers.
            val layoutLatch = CountDownLatch(1)
            scenario.onActivity { activity ->
                val logView = activity.findViewById<View>(R.id.logView)
                logView.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
                    override fun onLayoutChange(
                        v: View, l: Int, t: Int, r: Int, b: Int,
                        ol: Int, ot: Int, or2: Int, ob: Int
                    ) {
                        v.removeOnLayoutChangeListener(this)
                        layoutLatch.countDown()
                    }
                })
                ViewCompat.dispatchApplyWindowInsets(
                    logView,
                    WindowInsetsCompat.Builder()
                        .setInsets(WindowInsetsCompat.Type.systemBars(), Insets.of(0, 0, 0, FAKE_NAV_BAR_PX))
                        .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, FAKE_IME_HEIGHT_PX))
                        .build()
                )
            }
            // Wait for the layout pass triggered by the padding change to complete
            assertTrue("Layout did not update after IME insets dispatch", layoutLatch.await(2, TimeUnit.SECONDS))
            waitForIdle()

            // Step 6: scroll to the bottom and verify the tray does not cover any content
            scrollToBottom(scenario)
            waitForIdle()
            assertScrollViewEndsAtTray(scenario, "after shortcut insert with keyboard visible")

            // Verify root view absorbed the full IME height as bottom padding.
            // Old code only set systemBars.bottom (100px) here — ignoring the keyboard —
            // causing the tray to float 100px too high and leave a dead zone of hidden content.
            scenario.onActivity { activity ->
                val logView = activity.findViewById<View>(R.id.logView)
                val actualBottom = logView.paddingBottom
                assertTrue(
                    "Root view bottom padding ($actualBottom px) should equal the IME height " +
                        "($FAKE_IME_HEIGHT_PX px). If it only equals the nav bar height " +
                        "($FAKE_NAV_BAR_PX px) the keyboard inset is being ignored and content " +
                        "will be hidden behind the tray.",
                    actualBottom >= FAKE_IME_HEIGHT_PX
                )
            }

            // Verify the EditText itself has not internally scrolled — if its internal scrollY > 0
            // the bottom N lines of content are shifted off-screen even though the ScrollView
            // has room for them (the N-lines-short bug caused by layout_height="0dp" +
            // fillViewport="true" activating EditText's own scroll container).
            scenario.onActivity { activity ->
                val editText = activity.findViewById<EditText>(R.id.todayLog)
                assertTrue(
                    "EditText internal scrollY is ${editText.scrollY} px — content has been " +
                        "shifted up inside the EditText itself, hiding the last lines. " +
                        "This is caused by layout_height=\"0dp\" activating the EditText's " +
                        "internal scroll; fix by using layout_height=\"wrap_content\".",
                    editText.scrollY == 0
                )
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun waitForIdle() {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    private fun scrollToBottom(scenario: ActivityScenario<MainActivity>) {
        scenario.onActivity { activity ->
            activity.findViewById<ScrollView>(R.id.logScrollView).fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun scrollToFraction(scenario: ActivityScenario<MainActivity>, fraction: Float) {
        scenario.onActivity { activity ->
            val sv = activity.findViewById<ScrollView>(R.id.logScrollView)
            val child = sv.getChildAt(0) ?: return@onActivity
            val target = ((child.height - sv.height) * fraction).toInt().coerceAtLeast(0)
            sv.scrollTo(0, target)
        }
    }

    /**
     * Asserts that the ScrollView's bottom edge (in screen coordinates) does not extend
     * past the shortcut tray's top edge — i.e., no text content is hidden behind the tray.
     */
    private fun assertScrollViewEndsAtTray(scenario: ActivityScenario<MainActivity>, context: String) {
        scenario.onActivity { activity ->
            val scrollView = activity.findViewById<ScrollView>(R.id.logScrollView)
            val tray = activity.findViewById<RecyclerView>(R.id.shortcutTray)

            val scrollLoc = intArrayOf(0, 0)
            scrollView.getLocationOnScreen(scrollLoc)
            val scrollViewBottom = scrollLoc[1] + scrollView.height

            val trayLoc = intArrayOf(0, 0)
            tray.getLocationOnScreen(trayLoc)
            val trayTop = trayLoc[1]

            assertTrue(
                "[$context] ScrollView bottom ($scrollViewBottom) extends past " +
                    "shortcut tray top ($trayTop) — content is hidden behind the tray",
                scrollViewBottom <= trayTop + 2  // 2 px rounding tolerance
            )
        }
    }
}
