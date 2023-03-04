package com.yogi.mlintegration.ui.gallery

import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yogi.mlintegration.R
import com.yogi.mlintegration.databinding.RvGalleryImageItemBinding

class GalleryAdapter : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    private val dataset: MutableList<Uri> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<Uri>) {
        dataset.clear()
        dataset += data
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = RvGalleryImageItemBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_gallery_image_item, parent, false)
            .let { ViewHolder(it) }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.iv.setImageURI(dataset[position])
    }

    override fun getItemCount() = dataset.size
}