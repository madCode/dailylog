package com.example.dailylog

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.example.dailylog.repository.Repository
import com.example.dailylog.ui.permissions.PermissionChecker
import com.example.dailylog.ui.log.LogFragment
import com.example.dailylog.ui.log.LogViewModel
import com.example.dailylog.ui.settings.SettingsFragment
import com.example.dailylog.ui.settings.SettingsViewModel
import com.example.dailylog.ui.settings.SettingsViewModelFactory
import com.example.dailylog.ui.welcome.WelcomeFragment
import com.example.dailylog.ui.welcome.WelcomeViewModel
import com.example.dailylog.utils.DetermineBuild

class MainActivity : AppCompatActivity() {

    lateinit var repository: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        repository = Repository(applicationContext, PermissionChecker(this))
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
        val settingsViewModel = ViewModelProvider(this, SettingsViewModelFactory(application,
            repository, DetermineBuild, ::showErrorDialog)).get(
            SettingsViewModel::class.java)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, SettingsFragment.newInstance(settingsViewModel))
            .addToBackStack(null)
            .commit()
    }

    fun showErrorDialog(message: String) {
        runOnUiThread(Runnable {
            ErrorDialogFragment(message).show(supportFragmentManager, "error")
        })
    }
}