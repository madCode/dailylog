package com.app.dailylog.ui.log

import android.widget.EditText
import android.widget.ScrollView
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
    fun insertingMultiLineShortcut_keepsWholeLogReachable() {
        launchApp().use { scenario ->
            scenario.onActivity { activity ->
                val log = activity.findViewById<EditText>(R.id.todayLog)
                val scrollView = activity.findViewById<ScrollView>(R.id.logScrollView)
                val tray = activity.findViewById<RecyclerView>(R.id.shortcutTray)
                idleUntil { tray.childCount == 1 && log.layout != null }

                log.setSelection(log.text.length / 2)
                tray.getChildAt(0).performClick()
                idleUntil { !log.isLayoutRequested }

                assertEquals("log field scrolled its own content", 0, log.scrollY)
                val contentHeight = log.layout.height + log.totalPaddingTop + log.totalPaddingBottom
                assertTrue(
                    "log field (${log.height}px) is shorter than its text ($contentHeight px)",
                    log.height >= contentHeight
                )
                scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                idleUntil { !scrollView.isLayoutRequested }
                assertTrue(
                    "scroll view can't reach the end of the log",
                    scrollView.scrollY + scrollView.height >= log.bottom
                )
            }
        }
    }
}
