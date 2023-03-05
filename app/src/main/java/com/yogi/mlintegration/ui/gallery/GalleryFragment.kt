package com.yogi.mlintegration.ui.gallery

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yogi.imageselectorlibrary.ImageCapture
import com.yogi.imageselectorlibrary.ImageSelector
import com.yogi.imageselectorlibrary.MyFileProvider
import com.yogi.mlintegration.databinding.FragmentGalleryBinding
import com.yogi.permissionslibrary.PermissionHandler
import java.io.File

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GalleryViewModel by viewModels()

    private lateinit var imageSelector: ImageSelector
    private lateinit var imageCapture: ImageCapture
    private lateinit var permissionHandler: PermissionHandler

    private val adapter = GalleryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector = ImageSelector(requireActivity())
        lifecycle.addObserver(imageSelector)
        imageCapture = ImageCapture(requireActivity())
        lifecycle.addObserver(imageCapture)
        permissionHandler = PermissionHandler(requireActivity())
        lifecycle.addObserver(permissionHandler)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        binding.apply {
            rv.adapter = adapter
            btOneImage.setOnClickListener {
                imageSelector.selectSingleImage {
                    viewModel.addImages(listOf(it))
                }
            }
            btMiltipleImage.setOnClickListener {
                imageSelector.selectMultipleImage {
                    viewModel.addImages(it)
                }
            }
            btCameraImage.setOnClickListener {
                cameraPermissions {
                    imageCapture.capture {
                        viewModel.addImages(
                            listOf(
                                MyFileProvider.getUriForFile(
                                    requireContext(),
                                    File(it)
                                )
                            )
                        )
                    }
                }
            }
        }
        viewModel.uris.observe(viewLifecycleOwner) {
            adapter.setData(it)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun cameraPermissions(onAccept: () -> Unit) {
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