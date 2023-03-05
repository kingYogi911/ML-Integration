package com.yogi.mlintegration.features.imageLabeling

import android.Manifest
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yogi.imageselectorlibrary.ImageCapture
import com.yogi.imageselectorlibrary.ImageSelector
import com.yogi.mlintegration.R
import com.yogi.mlintegration.base.BaseFragment
import com.yogi.mlintegration.databinding.FragmentImageLabelingBinding
import com.yogi.permissionslibrary.PermissionHandler


class ImageLabelingFragment : BaseFragment<FragmentImageLabelingBinding>(
    R.layout.fragment_image_labeling
) {
    private val viewModel: ImageLabelingViewModel by viewModels()
    private lateinit var permissionHandler: PermissionHandler
    private lateinit var imageSelector: ImageSelector
    private lateinit var imageCapture: ImageCapture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector = ImageSelector(requireActivity())
        lifecycle.addObserver(imageSelector)
        imageCapture = ImageCapture(requireActivity())
        lifecycle.addObserver(imageCapture)
        permissionHandler = PermissionHandler(requireActivity())
        lifecycle.addObserver(permissionHandler)
    }

    override fun bindView(view: View) = FragmentImageLabelingBinding.bind(view)

    override fun initViews() {
        binding.apply {
            btFromImage.setOnClickListener {
                showImageSelectionOptions(
                    onSelectCamera = {
                        cameraPermissions {
                            imageCapture.capture { filePath ->
                                viewModel.labelingFromImage(BitmapFactory.decodeFile(filePath))
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
                                viewModel.labelingFromImage(imageBitmap)
                            }
                        }
                    }
                )
            }
        }
        viewModel.text.observe(viewLifecycleOwner) {
            binding.tv.text = it
        }
        viewModel.image.observe(viewLifecycleOwner) {
            binding.iv.setImageBitmap(it)
        }
        viewModel.progress.observe(viewLifecycleOwner) {
            binding.apply {
                sv.isVisible = it == false
                progressIndicator.isVisible = it == true
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
}