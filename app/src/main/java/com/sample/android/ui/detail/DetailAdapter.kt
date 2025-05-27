package com.sample.android.ui.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.sample.android.databinding.HolderDetailBinding
import com.sample.android.ui.data.UserUiData

interface DetailProperty {
    val requestManager: RequestManager
}

class DetailAdapter(private val property: DetailProperty) :
    ListAdapter<UserUiData, DetailAdapter.ViewHolder>(
        object : DiffUtil.ItemCallback<UserUiData>() {
            override fun areItemsTheSame(oldItem: UserUiData, newItem: UserUiData): Boolean {
                return oldItem.data == newItem.data
            }

            override fun areContentsTheSame(oldItem: UserUiData, newItem: UserUiData): Boolean {
                return true
            }
        }
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            HolderDetailBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < 0) {
            return
        }
        val item = getItem(position)
        holder.binding.apply {
            requestManager = property.requestManager
            thumbnail = item.data.thumbnail
        }
    }

    inner class ViewHolder(val binding: HolderDetailBinding) : RecyclerView.ViewHolder(binding.root)
}