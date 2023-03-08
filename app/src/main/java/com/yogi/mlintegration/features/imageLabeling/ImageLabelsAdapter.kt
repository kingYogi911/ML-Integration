package com.yogi.mlintegration.features.imageLabeling

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.mlkit.vision.label.ImageLabel
import com.yogi.mlintegration.R
import com.yogi.mlintegration.databinding.RvLayoutImageLabelItemBinding

class ImageLabelsAdapter(
) : RecyclerView.Adapter<ImageLabelsAdapter.ViewHolder>() {
    private val dataset: MutableList<ImageLabel> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<ImageLabel>) {
        dataset.clear()
        dataset += data
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = RvLayoutImageLabelItemBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.rv_layout_image_label_item, parent, false)
        )
    }

    override fun getItemCount() = dataset.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataset[position]
        holder.binding.apply {
            tvLabel.text = "Label : ${item.text}"
            tvConfidence.text = "Confidence : ${item.confidence}"
        }
    }
}