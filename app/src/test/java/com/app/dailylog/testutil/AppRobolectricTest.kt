package com.app.dailylog.testutil

import android.content.Context
import android.net.Uri
import android.os.Looper
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.app.dailylog.MainActivity
import com.app.dailylog.R
import com.app.dailylog.repository.Constants
import com.app.dailylog.repository.Shortcut
import com.app.dailylog.repository.ShortcutDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.robolectric.Shadows.shadowOf
import java.io.File

// Uses the real file-backed database, closed before launch, so the app opens it from disk like a cold start.
abstract class AppRobolectricTest {

    protected val context: Context = ApplicationProvider.getApplicationContext()
    protected val logFile = File(context.filesDir, "log.md")

    protected open val initialShortcuts: List<Shortcut> = emptyList()

    protected open val fileSelected = true

    @Before
    fun setUpApp() {
        // Suppress the startup warning dialog (shown for the first 3 launches)
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            .edit().putInt("num_launches", 3).commit()

        if (fileSelected) {
            logFile.writeText("Existing log\n")
            context.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE)
                .edit().putString(Constants.FILENAME_PREF_KEY, Uri.fromFile(logFile).toString()).commit()
        }

        if (initialShortcuts.isNotEmpty()) {
            val dao = ShortcutDatabase.getDatabase(context).shortcutDao()
            runBlocking { initialShortcuts.forEach { dao.add(it) } }
        }
        ShortcutDatabase.resetForTesting()
    }

    @After
    fun tearDownApp() {
        ShortcutDatabase.resetForTesting()
        context.deleteDatabase("shortcut_database")
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE).edit().clear().commit()
        logFile.delete()
    }

    // Waits for the shortcut tray to fill: it grows when shortcuts load, shifting the buttons above it.
    protected fun launchApp(): ActivityScenario<MainActivity> =
        ActivityScenario.launch(MainActivity::class.java).also { scenario ->
            if (fileSelected && initialShortcuts.isNotEmpty()) {
                scenario.onActivity { activity ->
                    idleUntil {
                        activity.findViewById<RecyclerView>(R.id.shortcutTray).childCount == initialShortcuts.size
                    }
                }
            }
        }

    // Room queries on a background thread, so a single idle() can run before the result is posted.
    protected fun idleUntil(timeoutMs: Long = 5000, condition: () -> Boolean): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (true) {
            shadowOf(Looper.getMainLooper()).idle()
            if (condition()) return true
            if (System.currentTimeMillis() > deadline) return false
            Thread.sleep(10)
        }
    }
}
