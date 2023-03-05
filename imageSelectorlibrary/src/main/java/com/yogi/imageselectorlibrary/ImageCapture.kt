package com.yogi.imageselectorlibrary

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.yogi.permissionslibrary.ContractInitializationException
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ImageCapture constructor(
    private val activity: ComponentActivity
) : LifecycleEventObserver {

    private val registry get() = activity.activityResultRegistry
    private var imageCaptureLauncher: ActivityResultLauncher<Uri>? = null
    private var tempFilePath: String? = null
    private var imageCaptureCallback: ((filePath: String) -> Unit)? = null

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_CREATE) {
            register(source)
        }
    }

    private fun register(owner: LifecycleOwner) {
        imageCaptureLauncher = registry.register(
            "ImageCapture",
            owner,
            ActivityResultContracts.TakePicture()
        ) {
            if (it) {
                imageCaptureCallback?.invoke(tempFilePath!!)
            }
        }
    }

    fun capture(callBack: (filePath: String) -> Unit) {
        if (imageCaptureLauncher == null) {
            throw ContractInitializationException()
        }
        imageCaptureCallback = callBack
        val file = File(
            activity.cacheDir,
            "image_${SimpleDateFormat("yyyy_dd_MM_HH_mm_ss", Locale.ENGLISH).format(Date())}_.jpg"
        )
        tempFilePath = file.path
        val uri = MyFileProvider.getUriForFile(activity, file)
        imageCaptureLauncher!!.launch(uri)
    }
}