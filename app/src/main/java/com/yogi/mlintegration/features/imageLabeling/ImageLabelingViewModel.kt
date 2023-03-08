package com.yogi.mlintegration.features.imageLabeling

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.yogi.mlintegration.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ImageLabelingViewModel : BaseViewModel() {

    private val _image: MutableLiveData<Bitmap?> = MutableLiveData(null)
    val image get() = _image.asLiveData()
    private val _labels: MutableLiveData<List<ImageLabel>> = MutableLiveData(emptyList())
    val labels get() = _labels.asLiveData()

    fun labelingFromImage(imageBitmap: Bitmap) {
        _image.value = imageBitmap
        viewModelScope.launch {
            _progress.value = true
            val input = InputImage.fromBitmap(imageBitmap, 0)
            val labeler = ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.65f)
                .build()
                .let {
                    ImageLabeling.getClient(it)
                }
            try {
                _labels.value = withContext(Dispatchers.IO) { labeler.getLabelsFromImage(input) }!!
            } catch (e: Exception) {
                _errorData.value = e
            }
            _progress.value = false
        }
    }

    private suspend fun ImageLabeler.getLabelsFromImage(inputImage: InputImage): List<ImageLabel> =
        suspendCoroutine { continuation ->
            process(inputImage)
                .addOnSuccessListener {
                    continuation.resume(it)
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }

}