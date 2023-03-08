package com.yogi.mlintegration.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class BaseViewModel : ViewModel() {
    protected val _progress:MutableLiveData<Boolean> = MutableLiveData(false)
    val progress get() = _progress.asLiveData()
    protected val _errorData:MutableLiveData<Throwable> = MutableLiveData()
    val errorData get() = _errorData.asLiveData()
    companion object {
        fun <T> MutableLiveData<T>.asLiveData() = this as LiveData<T>
    }
}