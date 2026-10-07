package com.app.dailylog.ui.log

import android.os.Looper
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.EditText
import android.widget.ScrollView
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.dailylog.MainActivity
import com.app.dailylog.R
import com.app.dailylog.testutil.AppRobolectricTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.GraphicsMode
import java.time.Duration

// Robolectric has no IME or spell checker, so a requestLayout() stands in for the relayouts they cause.
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LogScrollViewTest : AppRobolectricTest() {

    @Before
    fun longLog() {
        logFile.writeText((1..200).joinToString("\n") { "line $it" })
    }

    private class Screen(activity: MainActivity) {
        val log: EditText = activity.findViewById(R.id.todayLog)
        val scrollView: ScrollView = activity.findViewById(R.id.logScrollView)
        val tray: View = activity.findViewById(R.id.shortcutTray)
    }

    private fun withLog(test: Screen.() -> Unit) {
        launchApp().use { scenario ->
            scenario.onActivity { activity ->
                val screen = Screen(activity)
                // The log opens with the cursor at the end, scrolled to it.
                assertTrue("never scrolled to the cursor", idleUntil { screen.cursorVisible() && screen.scrollView.scrollY > 0 })
                screen.test()
            }
        }
    }

    private fun settle() {
        repeat(3) { shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(1)) }
    }

    private fun Screen.relayout() {
        log.requestLayout()
        settle()
    }

    private fun Screen.cursorVisible(): Boolean {
        val layout = log.layout ?: return false
        val line = layout.getLineForOffset(log.selectionEnd)
        val top = log.top + log.totalPaddingTop + layout.getLineTop(line)
        val bottom = log.top + log.totalPaddingTop + layout.getLineBottom(line)
        return top >= scrollView.scrollY && bottom <= scrollView.scrollY + scrollView.height
    }

    // The tray's bottom margin is how LogFragment makes room for the keyboard.
    private fun Screen.openKeyboard() {
        val params = tray.layoutParams as ViewGroup.MarginLayoutParams
        params.bottomMargin = scrollView.height / 2
        tray.layoutParams = params
        settle()
    }

    // Drags down by [distance] px, pausing before lifting so the gesture doesn't end in a fling.
    private fun Screen.dragDown(distance: Int) {
        val x = scrollView.width / 2f
        val startY = scrollView.height / 4f
        val downTime = SystemClock.uptimeMillis()
        var time = downTime
        fun send(action: Int, y: Float) {
            val event = MotionEvent.obtain(downTime, time, action, x, y, 0)
            scrollView.dispatchTouchEvent(event)
            event.recycle()
        }
        send(MotionEvent.ACTION_DOWN, startY)
        val steps = 10
        for (step in 1..steps) {
            time += 20
            send(MotionEvent.ACTION_MOVE, startY + distance * step / steps)
        }
        time += 1000
        send(MotionEvent.ACTION_MOVE, startY + distance)
        send(MotionEvent.ACTION_UP, startY + distance)
        settle()
    }

    @Test
    fun relayoutAfterUserScrolls_keepsScrollPositionAndCursor() = withLog {
        val cursor = log.selectionStart
        val bottom = scrollView.scrollY
        dragDown(scrollView.height / 2)
        val scrolledTo = scrollView.scrollY
        assertTrue("drag didn't scroll up", scrolledTo < bottom)

        relayout()

        assertEquals("relayout scrolled back toward the cursor", scrolledTo, scrollView.scrollY)
        assertEquals("scrolling moved the cursor", cursor, log.selectionStart)
    }

    @Test
    fun relayoutAfterTalkBackScroll_keepsScrollPosition() = withLog {
        assertTrue(scrollView.performAccessibilityAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD, null))
        settle()
        val scrolledTo = scrollView.scrollY

        relayout()

        assertEquals("relayout scrolled back toward the cursor", scrolledTo, scrollView.scrollY)
    }

    @Test
    fun relayoutAfterMouseWheelScroll_keepsScrollPosition() = withLog {
        val bottom = scrollView.scrollY
        val time = SystemClock.uptimeMillis()
        val pointer = MotionEvent.PointerProperties().apply { toolType = MotionEvent.TOOL_TYPE_MOUSE }
        val coords = MotionEvent.PointerCoords().apply {
            x = scrollView.width / 2f
            y = scrollView.height / 2f
            setAxisValue(MotionEvent.AXIS_VSCROLL, 10f)
        }
        val wheelUp = MotionEvent.obtain(
            time, time, MotionEvent.ACTION_SCROLL, 1, arrayOf(pointer), arrayOf(coords),
            0, 0, 1f, 1f, 0, 0, InputDevice.SOURCE_MOUSE, 0
        )
        scrollView.dispatchGenericMotionEvent(wheelUp)
        wheelUp.recycle()
        settle()
        val scrolledTo = scrollView.scrollY
        assertTrue("wheel didn't scroll up", scrolledTo < bottom)

        relayout()

        assertEquals("relayout scrolled back toward the cursor", scrolledTo, scrollView.scrollY)
    }

    @Test
    fun typingAfterUserScrolls_bringsCursorBackIntoView() = withLog {
        dragDown(scrollView.height / 2)
        assertTrue("cursor still visible after scrolling away", !cursorVisible())

        log.text.insert(log.selectionStart, "x")
        settle()

        assertTrue("typing didn't bring the cursor into view", cursorVisible())
    }

    @Test
    fun movingCursorAfterUserScrolls_followsCursor() = withLog {
        dragDown(scrollView.height / 2)

        log.setSelection(0)
        settle()

        assertEquals(0, scrollView.scrollY)
    }

    @Test
    fun replacingTextAfterUserScrolls_bringsCursorBackIntoView() = withLog {
        dragDown(scrollView.height / 2)
        val cursor = log.selectionStart

        // Same length and cursor, like tapping an autocorrect suggestion.
        log.text.replace(cursor - 1, cursor, "Z")
        settle()

        assertTrue("the edit didn't bring the cursor into view", cursorVisible())
    }

    @Test
    fun keyboardOpeningAfterSmallScroll_keepsCursorInView() = withLog {
        val layout = log.layout
        val lineAtThreeQuarters = layout.getLineForVertical(
            scrollView.scrollY + scrollView.height * 3 / 4 - log.top - log.totalPaddingTop
        )
        log.setSelection(layout.getLineStart(lineAtThreeQuarters))
        settle()
        dragDown(40)
        assertTrue("cursor left the screen", cursorVisible())

        openKeyboard()

        assertTrue("keyboard covered the cursor", cursorVisible())
    }

    @Test
    fun keyboardOpeningWhileScrollingToCursor_keepsCursorInView() = withLog {
        dragDown(scrollView.height / 2)
        log.setSelection(0)
        settle()

        // Robolectric runs smooth scrolls instantly, so this stands in for a frame of the
        // view's own animation toward the cursor, before the cursor is on screen.
        scrollView.scrollTo(0, scrollView.height * 2)
        openKeyboard()

        assertTrue("keyboard covered the cursor", cursorVisible())
    }

    @Test
    fun returningToTheApp_bringsCursorBackIntoView() {
        launchApp().use { scenario ->
            scenario.onActivity { activity ->
                val screen = Screen(activity)
                idleUntil { screen.cursorVisible() }
                screen.dragDown(screen.scrollView.height / 2)
                assertTrue(!screen.cursorVisible())
            }
            scenario.moveToState(Lifecycle.State.CREATED)
            scenario.moveToState(Lifecycle.State.RESUMED)
            scenario.onActivity { activity ->
                val screen = Screen(activity)
                settle()
                assertTrue("resuming didn't bring the cursor into view", screen.cursorVisible())
            }
        }
    }
}
