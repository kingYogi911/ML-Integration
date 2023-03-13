package com.yogi.mlintegration.features.objectDetection

import android.Manifest
import android.graphics.*
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yogi.imageselectorlibrary.ImageCaptureDefault
import com.yogi.imageselectorlibrary.ImageSelector
import com.yogi.mlintegration.R
import com.yogi.mlintegration.base.BaseFragment
import com.yogi.mlintegration.databinding.FragmentObjectDetectionBinding
import com.yogi.mlintegration.features.objectDetection.ObjectDetectionViewModel.MODE
import com.yogi.permissionslibrary.PermissionHandler
import java.io.ByteArrayOutputStream


class ObjectDetectionFragment : BaseFragment<FragmentObjectDetectionBinding>(
    R.layout.fragment_object_detection
) {
    private val viewModel: ObjectDetectionViewModel by viewModels()
    private lateinit var permissionHandler: PermissionHandler
    private lateinit var imageSelector: ImageSelector
    private lateinit var imageCaptureDefault: ImageCaptureDefault

    private val adapter = DetectedObjectsAdapter(
        onClickCard = { viewModel.markObjectOnImage(it) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector = ImageSelector(requireActivity())
        lifecycle.addObserver(imageSelector)
        imageCaptureDefault = ImageCaptureDefault(requireActivity())
        lifecycle.addObserver(imageCaptureDefault)
        permissionHandler = PermissionHandler(requireActivity())
        lifecycle.addObserver(permissionHandler)
    }

    override fun bindView(view: View) = FragmentObjectDetectionBinding.bind(view)

    override fun initViews() {
        binding.apply {
            btFromImage.setOnClickListener {
                viewModel.setImageMode(MODE.GALLERY)
                showImageSelectionOptions(
                    onSelectCamera = {
                        cameraPermissions {
                            imageCaptureDefault.capture { filePath ->
                                viewModel.detectObjectsFromImage(BitmapFactory.decodeFile(filePath))
                            }
                        }
                    },
                    onSelectGallery = {
                        imageSelector.selectSingleImage { uri ->
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                                MediaStore.Images.Media.getBitmap(
                                    requireContext().contentResolver,
                                    uri
                                )
                            } else {
                                ImageDecoder.createSource(requireContext().contentResolver, uri)
                                    .let { source ->
                                        ImageDecoder.decodeBitmap(source)
                                    }
                            }.let { imageBitmap ->
                                viewModel.detectObjectsFromImage(imageBitmap)
                            }
                        }
                    }
                )
            }
            rv.adapter = adapter
            btLiveCamera.setOnClickListener {
                viewModel.setImageMode(MODE.CAMERA_IMAGE)
            }
        }
        viewModel.progress.observe(viewLifecycleOwner) {
            binding.apply {
                rv.isVisible = it == false
                progressIndicator.isVisible = it == true
            }
        }
        viewModel.image.observe(viewLifecycleOwner, binding.iv::setImageBitmap)
        viewModel.detectedObjects.observe(viewLifecycleOwner, adapter::setData)
        viewModel.selected.observe(viewLifecycleOwner, adapter::selected::set)
        viewModel.mode.observe(viewLifecycleOwner) { mode ->
            binding.apply {
                iv.isVisible = mode == MODE.GALLERY
                preview.isVisible = mode == MODE.CAMERA_IMAGE
                btCapture.isVisible = mode == MODE.CAMERA_IMAGE
                if (mode == MODE.CAMERA_IMAGE) {
                    cameraPermissions {
                        startCamera(preview) { imageCapture ->
                            btCapture.setOnClickListener {
                                captureImage(imageCapture)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showImageSelectionOptions(
        onSelectCamera: () -> Unit,
        onSelectGallery: () -> Unit
    ) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.image_option_bottom_sheet_fragment)
        bottomSheetDialog.findViewById<LinearLayout>(R.id.bt_camera)
            ?.setOnClickListener {
                onSelectCamera()
                bottomSheetDialog.dismiss()
            }
        bottomSheetDialog.findViewById<LinearLayout>(R.id.bt_gallery)
            ?.setOnClickListener {
                onSelectGallery()
                bottomSheetDialog.dismiss()
            }
        bottomSheetDialog.show()
    }

    private fun cameraPermissions(onAccept: () -> Unit) {
        permissionHandler.requestPermission(
            Manifest.permission.CAMERA,
            showRationale = { onRationaleAccepted ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Camera Permission")
                    .setMessage("Camera Permission is required to capture image")
                    .setPositiveButton("Ok") { d, _ ->
                        onRationaleAccepted()
                        d.dismiss()
                    }
                    .show()
            },
            permanentlyDeclinedCallback = { openSettings ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Camera Permission")
                    .setMessage("Camera Permission is required to capture image, as you have declined the permission request multiple times \n Please provide it from Settings")
                    .setPositiveButton("Ok") { d, _ ->
                        openSettings()
                        d.dismiss()
                    }
                    .show()
            }
        ) {
            if (it) {
                onAccept()
            }
        }
    }

    private fun startCamera(
        previewView: PreviewView,
        imageCaptureUseCase: ((ImageCapture) -> Unit)? = null
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val imageCapture: ImageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
                imageCaptureUseCase?.invoke(imageCapture)
            } catch (exc: Exception) {
                Log.e(this.javaClass.simpleName, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun captureImage(
        imageCapture: ImageCapture
    ) {
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    image.format
                    viewModel.detectObjectsFromImage(image.convertImageProxyToBitmap())
                    viewModel.setImageMode(MODE.GALLERY)
                }
            }
        )
    }

    fun ImageProxy.convertImageProxyToBitmap(): Bitmap {
        val buffer = planes[0].buffer
        buffer.rewind()
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
    fun Image.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer // Y
        val vuBuffer = planes[2].buffer // VU

        val ySize = yBuffer.remaining()
        val vuSize = vuBuffer.remaining()

        val nv21 = ByteArray(ySize + vuSize)

        yBuffer.get(nv21, 0, ySize)
        vuBuffer.get(nv21, ySize, vuSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

}