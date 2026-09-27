package com.app.dailylog.ui.welcome

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.dailylog.MainActivity
import com.app.dailylog.R
import com.app.dailylog.testutil.AppRobolectricTest
import com.app.dailylog.ui.log.LogFragment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import java.io.File

@RunWith(AndroidJUnit4::class)
class WelcomeScreenTest : AppRobolectricTest() {
    override val fileSelected = false

    private fun MainActivity.current() = supportFragmentManager.findFragmentById(R.id.container)

    @Test
    fun noFileSelected_showsWelcome_andSurvivesRecreation() {
        launchApp().use { scenario ->
            scenario.onActivity { assertTrue(it.current() is WelcomeFragment) }
            scenario.recreate()
            scenario.onActivity { assertTrue(it.current() is WelcomeFragment) }
        }
    }

    private fun pickFileWith(buttonId: Int, expectedAction: String) {
        val chosen = File(context.filesDir, "journal.md").apply { writeText("") }
        launchApp().use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(buttonId).performClick()
                val request = shadowOf(activity).nextStartedActivityForResult
                val picker = request.intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
                assertEquals(expectedAction, picker.action)
                shadowOf(activity).receiveResult(request.intent, Activity.RESULT_OK, Intent().setData(Uri.fromFile(chosen)))
                idleUntil { activity.current() is LogFragment }
                assertTrue(activity.current() is LogFragment)
                assertEquals(Uri.fromFile(chosen).toString(), activity.repository.filename)
            }
        }
        chosen.delete()
    }

    @Test
    fun createFile_opensLog() = pickFileWith(R.id.createFileButton, Intent.ACTION_CREATE_DOCUMENT)

    @Test
    fun selectFile_opensLog() = pickFileWith(R.id.selectFileButton, Intent.ACTION_OPEN_DOCUMENT)

    @Test
    fun cancelledPicker_staysOnWelcome() {
        launchApp().use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.selectFileButton).performClick()
                val request = shadowOf(activity).nextStartedActivityForResult
                shadowOf(activity).receiveResult(request.intent, Activity.RESULT_CANCELED, null)
                idleUntil(500) { false }
                assertTrue(activity.current() is WelcomeFragment)
            }
        }
    }
}
