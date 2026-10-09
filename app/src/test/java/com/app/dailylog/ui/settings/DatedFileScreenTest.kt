package com.app.dailylog.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.dailylog.MainActivity
import com.app.dailylog.R
import com.app.dailylog.testutil.AppRobolectricTest
import com.app.dailylog.testutil.TestDocumentsProvider
import com.app.dailylog.utils.FileNameTemplate
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputLayout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowToast
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RunWith(AndroidJUnit4::class)
class DatedFileScreenTest : AppRobolectricTest() {

    private val folderDir = File(context.filesDir, "Journal")
    private lateinit var folder: Uri
    private val today = LocalDate.now()
    private val todaysDefaultFile get() = File(folderDir, "${today}-journal.md")

    @Before
    fun installFolder() {
        folder = TestDocumentsProvider.install(context, folderDir)
    }

    @After
    fun removeFolder() {
        folderDir.deleteRecursively()
    }

    /** The file controls live on their own screen since #87: Settings, then the File row. */
    private fun openFileSettings(): ActivityScenario<MainActivity> =
        launchApp().also {
            onView(withId(R.id.btnSettings)).perform(click())
            it.onActivity { activity ->
                activity.findViewById<View>(R.id.logFileRow).performClick()
                idleUntil(200) { false }
            }
        }

    private fun ActivityScenario<MainActivity>.reopenFileSettings() {
        onView(withId(R.id.btnSettings)).perform(click())
        onActivity { activity ->
            activity.findViewById<View>(R.id.logFileRow).performClick()
            idleUntil(200) { false }
        }
    }

    /** File screen -> Settings -> log. */
    private fun backToLog() {
        pressBack()
        pressBack()
    }

    private fun MainActivity.answerFolderPicker(result: Int, uri: Uri?) {
        val request = shadowOf(this).nextStartedActivityForResult
        assertNotNull("expected a folder picker", request)
        assertEquals(Intent.ACTION_OPEN_DOCUMENT_TREE, request.intent.action)
        shadowOf(this).receiveResult(request.intent, result, uri?.let { Intent().setData(it) })
        idleUntil(200) { false }
    }

    private fun MainActivity.visible(id: Int) = findViewById<View>(id).visibility == View.VISIBLE

    private fun MainActivity.log() = findViewById<EditText>(R.id.todayLog)

    @Test
    fun choosingNewFileByDate_asksForAFolderAndShowsTodaysFile() {
        openFileSettings().use { scenario ->
            scenario.onActivity { activity ->
                assertTrue(activity.visible(R.id.fileRow))
                assertFalse(activity.visible(R.id.datedFileGroup))

                activity.findViewById<View>(R.id.datedFileButton).performClick()
                activity.answerFolderPicker(Activity.RESULT_OK, folder)

                assertFalse(activity.visible(R.id.fileRow))
                assertTrue(activity.visible(R.id.datedFileGroup))
                assertEquals("Journal", activity.findViewById<TextView>(R.id.folderName).text.toString())
                assertEquals(
                    "Today's file: Journal/${today}-journal.md",
                    activity.findViewById<TextView>(R.id.todaysFilePreview).text.toString()
                )
            }
        }
    }

    @Test
    fun cancellingTheFolderPicker_staysOnOneFile() {
        openFileSettings().use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.datedFileButton).performClick()
                activity.answerFolderPicker(Activity.RESULT_CANCELED, null)

                assertTrue(activity.visible(R.id.fileRow))
                assertFalse(activity.visible(R.id.datedFileGroup))
                assertFalse(activity.repository.isDatedMode())
            }
        }
    }

    @Test
    fun newDay_startsEmptyAndTheFirstSaveCreatesTheFileInItsYearFolder() {
        openFileSettings().use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.datedFileButton).performClick()
                activity.answerFolderPicker(Activity.RESULT_OK, folder)
                activity.findViewById<EditText>(R.id.fileNameTemplate)
                    .setText("{DATETIME: yyyy}/{DATETIME: MM-dd}-journal.md")
            }
            backToLog()
            scenario.onActivity { activity ->
                idleUntil(200) { false }
                val path = today.format(DateTimeFormatter.ofPattern("yyyy/MM-dd")) + "-journal.md"
                assertEquals("", activity.log().text.toString())
                assertEquals("Today's file: $path (new)", ShadowToast.getTextOfLatestToast())
                assertFalse("opening the app makes no file", File(folderDir, today.year.toString()).exists())

                activity.log().setText("went for a run\n")
                activity.findViewById<View>(R.id.btnSave).performClick()

                assertEquals("went for a run\n", File(folderDir, path).readText())
                assertEquals("Existing log\n", logFile.readText())
            }
        }
    }

    @Test
    fun changingTheName_switchesFileWhenTheLogReopens() {
        File(folderDir, "${today}-journal.md").writeText("this morning\n")
        openFileSettings().use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.datedFileButton).performClick()
                activity.answerFolderPicker(Activity.RESULT_OK, folder)
            }
            backToLog()
            scenario.onActivity { activity ->
                idleUntil(200) { false }
                assertEquals("this morning\n", activity.log().text.toString())
                activity.log().setText("this morning\nand later\n")
            }
            // Leaving the log saves to the file it was showing, before the switch.
            scenario.reopenFileSettings()
            scenario.onActivity { activity ->
                activity.findViewById<EditText>(R.id.fileNameTemplate).setText("{DATETIME: yyyy-MM-dd}-fitness.md")
            }
            backToLog()
            scenario.onActivity { activity ->
                idleUntil(200) { false }
                assertEquals("", activity.log().text.toString())
                assertEquals("this morning\nand later\n", todaysDefaultFile.readText())
            }
        }
    }

    @Test
    fun switchingBackToOneFile_reopensIt() {
        openFileSettings().use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.datedFileButton).performClick()
                activity.answerFolderPicker(Activity.RESULT_OK, folder)
                activity.findViewById<View>(R.id.oneFileButton).performClick()
                assertTrue(activity.visible(R.id.fileRow))
            }
            backToLog()
            scenario.onActivity { activity ->
                idleUntil(200) { false }
                assertEquals("Existing log\n", activity.log().text.toString())
            }
        }
    }

    @Test
    fun invalidName_showsAnErrorAndKeepsTheLastGoodOne() {
        openFileSettings().use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.datedFileButton).performClick()
                activity.answerFolderPicker(Activity.RESULT_OK, folder)
                activity.findViewById<EditText>(R.id.fileNameTemplate).setText("{DATETIME: yyyy}/")

                assertNotNull(activity.findViewById<TextInputLayout>(R.id.fileNameTemplateLayout).error)
                assertEquals("", activity.findViewById<TextView>(R.id.todaysFilePreview).text.toString())
                assertEquals("{DATETIME: yyyy-MM-dd}-journal.md", activity.repository.retrieveFileNameTemplate())
            }
        }
    }

    @Test
    fun aPreset_changesTheDateOrderAndKeepsTheName() {
        openFileSettings().use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.datedFileButton).performClick()
                activity.answerFolderPicker(Activity.RESULT_OK, folder)
                val template = activity.findViewById<EditText>(R.id.fileNameTemplate)
                template.setText("{DATETIME: yyyy-MM-dd}-fitness.md")

                val presets = activity.findViewById<ChipGroup>(R.id.templatePresets)
                val dayFirst = presets.getChildAt(FileNameTemplate.PRESET_DATE_PARTS.indexOf("{DATETIME: dd-MM-yyyy}"))
                assertEquals(today.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")), (dayFirst as TextView).text.toString())
                dayFirst.performClick()

                assertEquals("{DATETIME: dd-MM-yyyy}-fitness.md", template.text.toString())
                assertNull(activity.findViewById<TextInputLayout>(R.id.fileNameTemplateLayout).error)
                assertEquals("{DATETIME: dd-MM-yyyy}-fitness.md", activity.repository.retrieveFileNameTemplate())
            }
        }
    }
}
