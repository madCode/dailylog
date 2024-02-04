package com.app.dailylog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.app.dailylog.repository.Repository
import com.app.dailylog.ui.permissions.PermissionChecker
import com.app.dailylog.ui.log.LogFragment
import com.app.dailylog.ui.log.LogViewModel
import com.app.dailylog.ui.settings.SettingsFragment
import com.app.dailylog.ui.settings.SettingsViewModel
import com.app.dailylog.ui.settings.SettingsViewModelFactory
import com.app.dailylog.ui.welcome.WelcomeFragment
import com.app.dailylog.ui.welcome.WelcomeViewModel
import com.app.dailylog.utils.DetermineBuild

class MainActivity : AppCompatActivity() {

    lateinit var repository: Repository
    private lateinit var permissionChecker: PermissionChecker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        permissionChecker = PermissionChecker(this)
        repository = Repository(applicationContext, permissionChecker)
        if (savedInstanceState == null) {
            if (repository.userHasSelectedFile()) {
                openWelcome()
            } else {
                openLog()
            }
        }
    }

    private fun openWelcome() {
        val welcomeViewModel = WelcomeViewModel(repository) { openLog() }
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, WelcomeFragment.newInstance(welcomeViewModel))
            .commitNow()
    }

    private fun openLog() {
        val logViewModel = LogViewModel(repository)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, LogFragment.newInstance(logViewModel) { openSettings() })
            .commitNow()
    }

    private fun openSettings() {
        val settingsViewModel =
            ViewModelProvider(this, SettingsViewModelFactory(repository, DetermineBuild, ::showErrorDialog))[SettingsViewModel::class.java]
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, SettingsFragment.newInstance(settingsViewModel, permissionChecker))
            .addToBackStack(null)
            .commit()
    }

    fun showErrorDialog(message: String) {
        runOnUiThread(Runnable {
            ErrorDialogFragment(message).show(supportFragmentManager, "error")
        })
    }
}