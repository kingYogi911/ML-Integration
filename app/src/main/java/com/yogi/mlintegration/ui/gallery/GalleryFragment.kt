package com.yogi.mlintegration.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yogi.imageselectorlibrary.ImageSelector
import com.yogi.mlintegration.databinding.FragmentGalleryBinding

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private val viewModel : GalleryViewModel by viewModels()

    private lateinit var imageSelector: ImageSelector

    private val adapter = GalleryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageSelector = ImageSelector(requireActivity())
        lifecycle.addObserver(imageSelector)
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
}