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
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ImageLabelingViewModel : BaseViewModel() {

    private val _text = MutableLiveData("")
    val text: LiveData<String> = _text
    private val _image: MutableLiveData<Bitmap?> = MutableLiveData(null)
    val image get() = _image.asLiveData()

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
                _text.value = labeler.getLabelsFromImage(input).joinToString("\n") {
                    "Index : ${it.index}\n" +
                            "Label : ${it.text}\n" +
                            "Confidence : ${it.confidence}\n"
                }
            } catch (e: Exception) {
                _text.value = e.message
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