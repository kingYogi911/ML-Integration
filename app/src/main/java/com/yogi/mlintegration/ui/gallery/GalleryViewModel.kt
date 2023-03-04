package com.yogi.mlintegration.ui.gallery

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GalleryViewModel : ViewModel() {

    private val _uris = MutableLiveData<List<Uri>>(emptyList())
    val uris: LiveData<List<Uri>> = _uris

    fun addImages(imageUris:List<Uri>){
        _uris.value = _uris.value!! + imageUris
    }
}