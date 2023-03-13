package com.yogi.mlintegration.features.objectDetection

import android.graphics.*
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.yogi.mlintegration.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ObjectDetectionViewModel : BaseViewModel() {

    private val _mode = MutableLiveData(MODE.GALLERY)
    val mode get() = _mode.asLiveData()

    private var _originalImage: Bitmap? = null
    private val _image: MutableLiveData<Bitmap?> = MutableLiveData(null)
    val image get() = _image.asLiveData()
    private val _detectedObjects: MutableLiveData<List<DetectedObject>> =
        MutableLiveData(emptyList())
    val detectedObjects get() = _detectedObjects.asLiveData()
    private val _selected: MutableLiveData<DetectedObject?> = MutableLiveData(null)
    val selected get() = _selected.asLiveData()

    fun setImageMode(mode: MODE) {
        if (_mode.value != mode) {
            _mode.value = mode
        }
    }

    fun detectObjectsFromImage(imageBitmap: Bitmap) {
        _originalImage = imageBitmap
        _image.value = imageBitmap
        viewModelScope.launch {
            _progress.value = true
            val input = InputImage.fromBitmap(imageBitmap, 0)
            val objectDetector = ObjectDetectorOptions.Builder()
                .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                .enableMultipleObjects()
                .enableClassification()
                .build()
                .let {
                    ObjectDetection.getClient(it)
                }
            try {

                withContext(Dispatchers.IO) { objectDetector.getDetectedObjectsInImage(input) }.also {
                    _detectedObjects.value = it
                    if (_detectedObjects.value!!.isNotEmpty()) {
                        _selected.value = _detectedObjects.value!!.first()
                        markObjectOnImage(_selected.value!!)
                    }
                }

            } catch (e: Exception) {
                _errorData.value = e
            }
            _progress.value = false
        }
    }

    private suspend fun ObjectDetector.getDetectedObjectsInImage(inputImage: InputImage): List<DetectedObject> =
        suspendCoroutine { continuation ->
            process(inputImage)
                .addOnSuccessListener {
                    continuation.resume(it)
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }

    fun markObjectOnImage(obj: DetectedObject) {
        _selected.value = obj
        viewModelScope.launch {
            _image.value =
                withContext(Dispatchers.IO) { _originalImage!!.drawerRectOnBitmap(obj.boundingBox) }
        }
    }

    private fun Bitmap.drawerRectOnBitmap(rect: Rect): Bitmap {
        val p = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 10f
            isAntiAlias = true
            isFilterBitmap = true
            isDither = true
            color = Color.RED
        }

        val bitmap = this.copy(Bitmap.Config.ARGB_8888, true)

        val c = Canvas(bitmap)
        c.drawRect(
            rect.left.toFloat(), rect.top.toFloat(), rect.right.toFloat(),
            rect.bottom.toFloat(), p
        )

        return bitmap
    }

    enum class MODE {
        GALLERY, CAMERA_IMAGE
    }
}