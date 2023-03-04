package com.yogi.permissionslibrary

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

class PermissionHandler constructor(
    private val activity: ComponentActivity,
) : LifecycleEventObserver {

    private val registry get() = activity.activityResultRegistry
    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null
    private var singlePermissionCallback: ((Boolean) -> Unit)? = null
    private lateinit var appSettingsLauncher: ActivityResultLauncher<Intent>
    private var lastRequestedPermissions: List<String> = listOf()
    private var isSinglePermissionRequested: Boolean = true
    private var permanentlyDeclinedCallback: ((openSettings: () -> Unit) -> Unit)? = null
    private var multiplePermanentlyDeclinedCallback: ((permission:String,openSettings: () -> Unit) -> Unit)? = null

    private var multiplePermissionCallback: ((Boolean) -> Unit)? = null

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (source.lifecycle.currentState == Lifecycle.State.CREATED) {
            register(source)
        }
    }

    private fun register(owner: LifecycleOwner) {
        permissionLauncher = registry.register(
            "LocationPermissions", owner,
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionsStatus ->
            if (permissionsStatus.isNotEmpty()) {
                if (isSinglePermissionRequested) {
                    singlePermissionCallback?.invoke(permissionsStatus!!.toList().first().second)
                }
            }
        }
        appSettingsLauncher = registry.register(
            "AppSettings", owner, ActivityResultContracts.StartActivityForResult()
        ) {
            if (isSinglePermissionRequested) {
                requestPermission(
                    permission = lastRequestedPermissions.first(),
                    showRationale = {},
                    permanentlyDeclinedCallback = permanentlyDeclinedCallback!!,
                    callBack = singlePermissionCallback!!
                )
            }
        }
    }

    @Throws(ContractInitializationException::class)
    fun requestPermission(
        permission: String,
        showRationale: (onRationaleAccepted: () -> Unit) -> Unit,
        permanentlyDeclinedCallback: (openSettings: () -> Unit) -> Unit,
        callBack: (Boolean) -> Unit
    ) {
        if (permissionLauncher == null) {
            throw ContractInitializationException()
        }
        isSinglePermissionRequested = true
        lastRequestedPermissions = listOf(permission)
        singlePermissionCallback = callBack
        this.permanentlyDeclinedCallback = permanentlyDeclinedCallback

        val permissionHelper = PermissionsHelper(permission, activity)

        when {
            permissionHelper.isGranted() -> {
                singlePermissionCallback?.invoke(true)
            }
            permissionHelper.isRationaleAlreadyShown() -> {
                permanentlyDeclinedCallback {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", activity.packageName, null)
                    }
                    appSettingsLauncher.launch(intent)
                }
            }
            permissionHelper.shouldShowRationale() -> {
                permissionHelper.setRationaleShown()
                showRationale.invoke {
                    permissionLauncher!!.launch(arrayOf(permission))
                }
            }
            else -> {
                permissionLauncher!!.launch(arrayOf(permission))
            }
        }
    }

    fun requestPermissionLoop(
        permission: String,
        showRationale: (onRationaleAccepted: () -> Unit) -> Unit,
        permanentlyDeclinedCallback: (openSettings: () -> Unit) -> Unit,
        onPermissionGranted: () -> Unit
    ) {
        requestPermission(
            permission = permission,
            showRationale = showRationale,
            permanentlyDeclinedCallback = permanentlyDeclinedCallback,
            callBack = { isGranted ->
                if (isGranted) {
                    onPermissionGranted()
                } else {
                    requestPermissionLoop(
                        permission,
                        showRationale,
                        permanentlyDeclinedCallback,
                        onPermissionGranted
                    )
                }
            }
        )
    }

    fun requestMultiplePermissions(
        vararg permissions: String,
        showRationale: (permission: String, onRationaleAccepted: () -> Unit) -> Unit,
        permanentlyDeclinedCallback: (permission: String, openSettings: () -> Unit) -> Unit,
        callBack: (Boolean) -> Unit
    ) {
        if (permissionLauncher == null) {
            throw ContractInitializationException()
        }
        isSinglePermissionRequested = false
        lastRequestedPermissions = permissions.toList()
        multiplePermissionCallback = callBack
        this.multiplePermanentlyDeclinedCallback = permanentlyDeclinedCallback

        val permissionHelpers = lastRequestedPermissions.map { PermissionsHelper(it,activity) }
        val granted = permissionHelpers.filter { it.isGranted() }
        val declined = permissionHelpers.filter { it.isRationaleAlreadyShown() }.map { it.manifestPermission }
        val forRationale = permissionHelpers.filter { it.shouldShowRationale() }.map { it.manifestPermission }
        when{
            permissionHelpers.size == granted.size -> {
                multiplePermissionCallback?.invoke(true)
            }
            forRationale.isNotEmpty() -> {
                forRationale.forEach { permission->
                    showRationale.invoke(permission){
                        permissionLauncher!!.launch(arrayOf(permission))
                    }
                }
            }
            declined.isNotEmpty() -> {
                declined.forEach { permission->
                    permanentlyDeclinedCallback(permission) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", activity.packageName, null)
                        }
                        appSettingsLauncher.launch(intent)
                    }
                }
            }
            else->{

            }
        }
    }
}