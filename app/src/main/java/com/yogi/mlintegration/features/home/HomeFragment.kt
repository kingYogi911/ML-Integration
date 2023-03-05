package com.yogi.mlintegration.features.home

import android.view.View
import androidx.fragment.app.viewModels
import com.yogi.mlintegration.R
import com.yogi.mlintegration.databinding.FragmentHomeBinding
import com.yogi.mlintegration.base.BaseFragment

class HomeFragment : BaseFragment<FragmentHomeBinding>(
    R.layout.fragment_home
) {
    private val viewModel: HomeViewModel by viewModels()

    override fun bindView(view: View) = FragmentHomeBinding.bind(view)

    override fun initViews() {
        viewModel.data.observe(viewLifecycleOwner){
            binding.tv.text = it
        }
    }

}