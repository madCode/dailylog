package com.example.dailylog.settings


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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class PermissionChecker(var _activity: Activity) {
    fun doIfExtStoragePermissionGranted(): Boolean {
        if (ContextCompat.checkSelfPermission(
                _activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                _activity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                CODE_PERMISSION_EXTERNAL_STORAGE
            )
            return false
        }
        return true
    }

    companion object {
        private const val CODE_PERMISSION_EXTERNAL_STORAGE = 4000
    }
}