package com.app.dailylog

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
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
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.main_activity)
        permissionChecker = PermissionChecker(this)
        repository = Repository(applicationContext, permissionChecker)
        
        // Initialize preferences
        prefs = getSharedPreferences("app_preferences", MODE_PRIVATE)
        
        if (savedInstanceState == null) {
            if (repository.userMustSelectFile()) {
                openWelcome()
            } else {
                // Show startup warning if needed.
                // Don't show the warning if the user still needs to select a file and do setup.
                showStartupWarning()
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
    
    private fun showStartupWarning() {
        // Check if this is the first time the app is launched
        val numLaunches = prefs.getInt("num_launches", 0)
        
        if (numLaunches < 3) {
            // Show the warning popup
            showStartupWarningDialog()
            
            // Mark that we've shown the dialog
            prefs.edit().putInt("num_launches", numLaunches + 1).apply()
        }
    }
    
    private fun showStartupWarningDialog() {
        val numLaunches = prefs.getInt("num_launches", 1)
        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
            .setTitle("Shortcuts Export Update")
            .setMessage("We've updated the shortcuts export format.\n\nUse the button below to navigate to the settings page. There, click on the three-dot menu and export a backup of your shortcuts.\n\nImporting the old CSV format will be removed in the next app version.\n\n\n(Reminder $numLaunches of 3)")
            .setPositiveButton("Go to settings") { _, _ ->
                // Navigate to settings
                openSettings()
            }
            .setNegativeButton("Ignore") { _, _ ->
                // Close the dialog, do nothing
            }
            .create()
            
        dialog.show()
    }
}