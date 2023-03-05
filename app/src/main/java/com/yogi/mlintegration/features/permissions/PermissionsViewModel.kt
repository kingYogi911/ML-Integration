package com.yogi.mlintegration.features.permissions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PermissionsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Permission Request Test"
    }
    val text: LiveData<String> = _text
    fun setMessage(msg:String){
       _text.value = msg
    }
}