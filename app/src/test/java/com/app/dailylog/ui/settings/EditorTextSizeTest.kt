package com.app.dailylog.ui.settings

import android.content.Context
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.dailylog.MainActivity
import com.app.dailylog.R
import com.app.dailylog.repository.Constants
import com.app.dailylog.testutil.AppRobolectricTest
import com.app.dailylog.utils.EditorTextSize
import com.google.android.material.slider.Slider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class EditorTextSizeTest : AppRobolectricTest() {

    private val prefs get() = context.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE)

    // A large phone font size, so a chosen size and the phone's size can't coincide.
    @Before
    fun largePhoneFont() {
        RuntimeEnvironment.setFontScale(1.5f)
    }

    private fun MainActivity.dp(value: Int) = value * resources.displayMetrics.density

    private fun MainActivity.editorSize() = findViewById<TextView>(R.id.todayLog).textSize

    private fun MainActivity.backToLog() {
        onBackPressedDispatcher.onBackPressed()
        idleUntil(500) { false }
    }

    @Test
    fun choosingASize_setsTheEditorToItWhateverThePhoneFontSize() {
        launchApp().use { scenario ->
            var phoneSize = 0f
            scenario.onActivity { phoneSize = it.editorSize() }
            onView(withId(R.id.btnSettings)).perform(click())

            scenario.onActivity { activity ->
                val value = activity.findViewById<TextView>(R.id.editorTextSizeValue)
                assertEquals("Default", value.text)
                assertFalse(activity.findViewById<View>(R.id.editorTextSizeReset).isEnabled)

                tapSliderEnd(activity.findViewById(R.id.editorTextSizeSlider))

                assertEquals("28", value.text)
                assertTrue(activity.findViewById<View>(R.id.editorTextSizeReset).isEnabled)
                assertEquals(
                    activity.dp(28),
                    activity.findViewById<TextView>(R.id.editorTextSizePreview).textSize,
                    0.01f
                )
                assertEquals(28, prefs.getInt(Constants.EDITOR_TEXT_SIZE_KEY, 0))

                activity.backToLog()
                assertEquals(activity.dp(28), activity.editorSize(), 0.01f)
                assertNotEquals(phoneSize, activity.editorSize(), 0.01f)
            }
        }
    }

    @Test
    fun reset_returnsTheEditorToThePhoneFontSize() {
        prefs.edit().putInt(Constants.EDITOR_TEXT_SIZE_KEY, 14).commit()
        launchApp().use { scenario ->
            scenario.onActivity { assertEquals(it.dp(14), it.editorSize(), 0.01f) }
            onView(withId(R.id.btnSettings)).perform(click())

            scenario.onActivity { activity ->
                assertEquals("14", activity.findViewById<TextView>(R.id.editorTextSizeValue).text)
                activity.findViewById<View>(R.id.editorTextSizeReset).performClick()

                assertEquals("Default", activity.findViewById<TextView>(R.id.editorTextSizeValue).text)
                assertFalse(prefs.contains(Constants.EDITOR_TEXT_SIZE_KEY))

                activity.backToLog()
                assertEquals(EditorTextSize.defaultPx(activity), activity.editorSize(), 0.01f)
                assertNotEquals(activity.dp(14), activity.editorSize(), 0.01f)
            }
        }
    }

    /** Taps the far end of the slider's track, as a user picking the largest size would. */
    private fun tapSliderEnd(slider: Slider) {
        idleUntil { slider.width > 0 }
        val x = slider.width - 1f
        val y = slider.height / 2f
        val time = SystemClock.uptimeMillis()
        slider.dispatchTouchEvent(MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN, x, y, 0))
        slider.dispatchTouchEvent(MotionEvent.obtain(time, time + 50, MotionEvent.ACTION_UP, x, y, 0))
        idleUntil(500) { false }
    }
}
