package com.example.dailylog.ui.permissions

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
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class PermissionChecker(private var activity: Activity) {

    fun doIfExtStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT < 30) {
            doIfExtStoragePermissionGrantedOld()
        } else {
            doIfExtStoragePermissionGrantedOld()
        }
    }

    private fun doIfExtStoragePermissionGrantedOld(): Boolean {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                CODE_PERMISSION_EXTERNAL_STORAGE
            )
            return false
        }
        return true
    }

//    fun getAndroid10WritePermissions() {
//        if (Build.VERSION.SDK_INT >= 29) {
//            MaterialAlertDialogBuilder(activity)
//                .setTitle(activity.resources.getString(R.string.permissionNeeded))
//                .setMessage(activity.resources.getString(R.string.permissionNeededExplanation))
//                .setNeutralButton(activity.resources.getString(R.string.cancel)) { dialog, _ ->
//                    dialog.dismiss()
//                }
//                .setPositiveButton(activity.resources.getString(R.string.ok)) { dialog, _ ->
//                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
//                    dialog.dismiss()
//                    activity.startActivity(intent)
//                }
//                .show()
//        }
//    }

    companion object {
        private const val CODE_PERMISSION_EXTERNAL_STORAGE = 4000
    }
}