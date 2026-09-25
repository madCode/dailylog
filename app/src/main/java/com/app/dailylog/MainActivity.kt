package com.app.dailylog

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.app.dailylog.repository.Repository
import com.app.dailylog.ui.permissions.PermissionChecker
import com.app.dailylog.ui.log.LogFragment
import com.app.dailylog.ui.log.LogViewModel
import com.app.dailylog.ui.settings.AddShortcutDialogFragment
import com.app.dailylog.ui.settings.BulkAddShortcutsDialogFragment
import com.app.dailylog.ui.settings.EditShortcutDialogFragment
import com.app.dailylog.ui.settings.ShortcutDialogViewModel
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
        permissionChecker = PermissionChecker(this)
        repository = Repository(applicationContext, permissionChecker)
        // Must be set before super.onCreate, which recreates any fragments that were showing
        // before the Activity was destroyed (dark mode switch, rotation, process death).
        supportFragmentManager.fragmentFactory = AppFragmentFactory()
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.main_activity)
        
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

    /**
     * Supplies fragments with their dependencies, both when this Activity creates them and when
     * Android recreates them from saved state. Without it, recreation needs a no-argument
     * constructor and the app crashes.
     */
    private inner class AppFragmentFactory : FragmentFactory() {
        override fun instantiate(classLoader: ClassLoader, className: String): Fragment =
            when (loadFragmentClass(classLoader, className)) {
                WelcomeFragment::class.java -> WelcomeFragment(WelcomeViewModel(repository) { openLog() })
                LogFragment::class.java -> LogFragment(LogViewModel(repository)) { openSettings() }
                SettingsFragment::class.java -> SettingsFragment(getSettingsViewModel(), permissionChecker)
                AddShortcutDialogFragment::class.java -> AddShortcutDialogFragment(ShortcutDialogViewModel(repository))
                EditShortcutDialogFragment::class.java -> EditShortcutDialogFragment(ShortcutDialogViewModel(repository))
                BulkAddShortcutsDialogFragment::class.java -> BulkAddShortcutsDialogFragment(ShortcutDialogViewModel(repository))
                else -> super.instantiate(classLoader, className)
            }
    }

    private fun getSettingsViewModel(): SettingsViewModel {
        val settingsViewModel =
            ViewModelProvider(this, SettingsViewModelFactory(repository, DetermineBuild, ::showErrorDialog))[SettingsViewModel::class.java]
        // The ViewModel outlives this Activity on recreation; point it at the current one.
        settingsViewModel.showToastOnActivity = ::showErrorDialog
        return settingsViewModel
    }

    private fun showFragment(fragmentClass: Class<out Fragment>, addToBackStack: Boolean) {
        val fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, fragmentClass.name)
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
        if (addToBackStack) {
            transaction.addToBackStack(null).commit()
        } else {
            transaction.commitNow()
        }
    }

    private fun openWelcome() = showFragment(WelcomeFragment::class.java, addToBackStack = false)

    private fun openLog() = showFragment(LogFragment::class.java, addToBackStack = false)

    private fun openSettings() = showFragment(SettingsFragment::class.java, addToBackStack = true)

    fun showErrorDialog(message: String) {
        runOnUiThread(Runnable {
            ErrorDialogFragment.newInstance(message).show(supportFragmentManager, "error")
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