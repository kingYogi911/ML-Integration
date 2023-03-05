package com.yogi.mlintegration.base

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<T : ViewBinding>(
    private val layoutId: Int
) : Fragment(layoutId) {
    private var _binding: T? = null
    val binding get() = _binding!!

    abstract fun bindView(view: View): T

    abstract fun initViews()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = bindView(view)
        initViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}