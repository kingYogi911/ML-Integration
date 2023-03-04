package com.yogi.imageselectorlibrary

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.yogi.permissionslibrary.ContractInitializationException

class ImageSelector constructor(
    private val activity: ComponentActivity,
) : LifecycleEventObserver {
    private val registry get() = activity.activityResultRegistry

    private var pickMediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var imageCallback: ((Uri) -> Unit)? = null

    private var pickMultipleImageLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var multipleImageCallback: ((List<Uri>) -> Unit)? = null

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (source.lifecycle.currentState == Lifecycle.State.CREATED) {
            register(source)
        }
    }

    private fun register(owner: LifecycleOwner) {
        pickMediaLauncher = registry.register(
            "PickImage",
            owner,
            PickVisualMedia()
        ) { uri ->
            uri?.let {
                imageCallback?.invoke(it)
            }
        }

        pickMultipleImageLauncher = registry.register(
            "PickMultipleImage",
            owner,
            ActivityResultContracts.PickMultipleVisualMedia()
        ) { uris ->
            multipleImageCallback?.invoke(uris)
        }
    }

    fun selectSingleImage(callback: (Uri) -> Unit) {
        if (pickMediaLauncher == null) {
            throw ContractInitializationException()
        }
        imageCallback = callback
        pickMediaLauncher!!.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
    }

    fun selectMultipleImage(callback: (List<Uri>) -> Unit) {
        if (pickMultipleImageLauncher == null) {
            throw ContractInitializationException()
        }
        multipleImageCallback = callback
        pickMultipleImageLauncher!!.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
    }

}