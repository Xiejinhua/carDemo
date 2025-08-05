package com.desaysv.psmap.ui.adapter.vh

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by wangmansheng
 */
class GroupVH<T, B : ViewDataBinding>(val binding: B) : RecyclerView.ViewHolder(binding.root)
