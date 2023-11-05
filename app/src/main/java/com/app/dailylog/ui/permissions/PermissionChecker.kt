package com.app.dailylog.ui.permissions

/*#######################################################
 *
 *   Maintained by Gregor Santner, 2017-
 *   https://gsantner.net/
 *
 *   License of this file: Apache 2.0 (Commercial upon request)
 *     https://www.apache.org/licenses/LICENSE-2.0
 *     https://github.com/gsantner/opoc/#licensing
 *
#########################################################*/
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class PermissionChecker(private var activity: Activity?) {

    private fun getPermissionsBasedOnAppVersion(): List<String> {
        return when (Build.VERSION.SDK_INT) {
            in 0..18 -> listOf("android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE")
            in 19..29 -> listOf("android.permission.WRITE_EXTERNAL_STORAGE")
            else -> emptyList() // No additional permissions required for modern Android versions.
        }
    }

    fun requestPermissionsBasedOnAppVersion(): Boolean {
        if (activity == null) {
            return false
        }
        val permissionsNeeded = getPermissionsBasedOnAppVersion()

        if (permissionsNeeded.isNotEmpty()) {
            val permissionsToRequest = permissionsNeeded.filter { permission ->
                ContextCompat.checkSelfPermission(activity!!, permission) != PackageManager.PERMISSION_GRANTED
            }

            if (permissionsToRequest.isNotEmpty()) {
                ActivityCompat.requestPermissions(activity!!, permissionsToRequest.toTypedArray(), CODE_PERMISSION_FILE_READ_WRITE_ACCESS)
                return false // Permissions were not granted.
            }
        }
        return true
    }

    companion object {
        private const val CODE_PERMISSION_FILE_READ_WRITE_ACCESS = 4000
    }
}