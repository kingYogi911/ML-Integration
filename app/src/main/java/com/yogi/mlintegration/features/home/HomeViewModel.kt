package com.yogi.mlintegration.features.home

import androidx.lifecycle.MutableLiveData
import com.yogi.mlintegration.base.BaseViewModel

class HomeViewModel : BaseViewModel() {
    private val _data = MutableLiveData("Home Page")
    val data get() = _data.asLiveData()
}