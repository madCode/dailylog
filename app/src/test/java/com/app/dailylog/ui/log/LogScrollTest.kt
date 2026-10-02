package com.app.dailylog.ui.log

import android.os.SystemClock
import android.view.MotionEvent
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.dailylog.R
import com.app.dailylog.repository.Shortcut
import com.app.dailylog.repository.ShortcutType
import com.app.dailylog.testutil.AppRobolectricTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LogScrollTest : AppRobolectricTest() {

    private val multiLine = "one\ntwo\nthree\nfour\nfive\n"

    override val initialShortcuts = listOf(
        Shortcut("Lines", multiLine, multiLine.length, ShortcutType.TEXT, 0),
    )

    @Before
    fun longLog() {
        logFile.writeText((1..200).joinToString("\n") { "line $it" })
    }

    @Test
    fun draggingDown_reachesTheTopOfALongLog() {
        launchApp().use { scenario ->
            scenario.onActivity { activity ->
                val log = activity.findViewById<EditText>(R.id.todayLog)
                idleUntil { log.layout != null && !log.isLayoutRequested }
                assertTrue("log opens at the cursor, at the end", log.scrollY > 0)

                repeat(DRAGS) { drag(log, log.height / 2f) }

                assertEquals(0, log.scrollY)
            }
        }
    }

    @Test
    fun insertingMultiLineShortcut_keepsWholeLogReachable() {
        launchApp().use { scenario ->
            scenario.onActivity { activity ->
                val log = activity.findViewById<EditText>(R.id.todayLog)
                val tray = activity.findViewById<RecyclerView>(R.id.shortcutTray)
                idleUntil { tray.childCount == 1 && log.layout != null }

                log.setSelection(log.text.length / 2)
                tray.getChildAt(0).performClick()
                idleUntil { !log.isLayoutRequested }

                assertTrue(
                    "log field (${log.bottom}px) runs under the shortcut tray (${tray.top}px)",
                    log.bottom <= tray.top
                )
                repeat(DRAGS) { drag(log, -log.height / 2f) }
                val visibleHeight = log.height - log.totalPaddingTop - log.totalPaddingBottom
                assertTrue(
                    "can't scroll to the end of the log",
                    log.scrollY + visibleHeight >= log.layout.height
                )
                repeat(DRAGS) { drag(log, log.height / 2f) }
                assertEquals("can't scroll back to the start of the log", 0, log.scrollY)
            }
        }
    }

    /** Drags a finger [dy] pixels down the field (negative drags up), the way a user scrolls. */
    private fun drag(view: EditText, dy: Float) {
        val x = view.width / 2f
        val startY = if (dy > 0) view.height * 0.2f else view.height * 0.8f
        val time = SystemClock.uptimeMillis()
        val steps = 10
        view.dispatchTouchEvent(MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN, x, startY, 0))
        for (i in 1..steps) {
            view.dispatchTouchEvent(
                MotionEvent.obtain(time, time + i * 10L, MotionEvent.ACTION_MOVE, x, startY + dy * i / steps, 0)
            )
        }
        view.dispatchTouchEvent(
            MotionEvent.obtain(time, time + (steps + 1) * 10L, MotionEvent.ACTION_UP, x, startY + dy, 0)
        )
    }

    private companion object {
        // Enough half-screen drags to cross the 200-line log from either end.
        const val DRAGS = 40
    }
}
