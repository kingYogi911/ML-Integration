package com.yogi.mlintegration.features.objectDetection

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.objects.DetectedObject
import com.yogi.mlintegration.R
import com.yogi.mlintegration.databinding.RvLayoutDetectedObjectItemBinding
import com.yogi.mlintegration.databinding.RvLayoutImageLabelItemBinding
import kotlin.coroutines.coroutineContext

class DetectedObjectsAdapter(
    private val onClickCard: (DetectedObject) -> Unit
) : RecyclerView.Adapter<DetectedObjectsAdapter.ViewHolder>() {
    private val dataset: MutableList<DetectedObject> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<DetectedObject>) {
        dataset.clear()
        dataset += data
        notifyDataSetChanged()
    }

    var selected: DetectedObject? = null
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = RvLayoutDetectedObjectItemBinding.bind(itemView)

        init {
            binding.root.setOnClickListener {
                onClickCard(dataset[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.rv_layout_detected_object_item, parent, false)
        )
    }

    override fun getItemCount() = dataset.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataset[position]
        holder.binding.apply {
            root.setCardBackgroundColor(
                ContextCompat.getColor(
                    root.context,
                    if (selected == item) R.color.purple_3 else R.color.white
                )
            )
            tvLabel.text = "Label : ${item.labels.joinToString { it.text }}"
            tvConfidence.text = ""
        }
    }
}