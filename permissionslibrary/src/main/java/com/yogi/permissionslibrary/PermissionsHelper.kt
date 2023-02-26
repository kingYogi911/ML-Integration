package com.yogi.permissionslibrary

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionsHelper(
    val manifestPermission: String,
    private val activity: Activity
) {

    fun isGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            manifestPermission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun shouldShowRationale(): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            manifestPermission
        )
    }

    fun isRationaleAlreadyShown(): Boolean {
        return PermissionsPreferences(activity).check(this)
    }

    fun setRationaleShown() {
        PermissionsPreferences(activity).save(this)
    }
}