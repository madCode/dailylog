package com.app.dailylog.ui.welcome

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.dailylog.R
import com.app.dailylog.testutil.AppRobolectricTest
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WelcomeScreenTest : AppRobolectricTest() {
    override val fileSelected = false

    @Test
    fun noFileSelected_showsWelcome_andSurvivesRecreation() {
        launchApp().use { scenario ->
            scenario.onActivity {
                assertTrue(it.supportFragmentManager.findFragmentById(R.id.container) is WelcomeFragment)
            }
            scenario.recreate()
            scenario.onActivity {
                assertTrue(it.supportFragmentManager.findFragmentById(R.id.container) is WelcomeFragment)
            }
        }
    }
}
