package com.yogi.imageselectorlibrary

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.yogi.imageselectorlibrary.R
import java.io.File

class MyFileProvider : FileProvider(R.xml.file_paths){
    companion object {
        val authorityName get() = "com.yogi.imageselectorlibrary"
        fun getUriForFile(context: Context,file: File):Uri{
            return getUriForFile(context, authorityName,file)
        }
    }
}