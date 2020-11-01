package com.example.dailylog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.dailylog.repository.Repository
import com.example.dailylog.ui.permissions.PermissionChecker
import com.example.dailylog.ui.log.LogView
import com.example.dailylog.ui.log.LogViewModel
import com.example.dailylog.ui.settings.SettingsView
import com.example.dailylog.ui.settings.SettingsViewModel
import com.example.dailylog.ui.settings.SettingsViewModelFactory
import com.example.dailylog.utils.DetermineBuild

class MainActivity : AppCompatActivity() {

    lateinit var logViewModel: LogViewModel
    lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        val repository = Repository(applicationContext, PermissionChecker(this))
        logViewModel = LogViewModel(repository)
        settingsViewModel = ViewModelProvider(this, SettingsViewModelFactory(application, repository, DetermineBuild)).get(
            SettingsViewModel::class.java)
        if (savedInstanceState == null) {
            openLog()
        }
    }

    private fun openLog() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, LogView.newInstance(logViewModel) { openSettings() })
            .commitNow()
    }

    private fun openSettings() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, SettingsView.newInstance(settingsViewModel))
            .addToBackStack(null)
            .commit()
    }
}