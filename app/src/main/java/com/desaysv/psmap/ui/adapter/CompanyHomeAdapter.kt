package com.desaysv.psmap.ui.adapter

import android.text.TextUtils
import com.autonavi.gbl.user.behavior.model.SimpleFavoriteItem
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemCompanyHomeBinding
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener

/**
 * 收藏夹家和公司Adapter
 */
class CompanyHomeAdapter :
    BaseQuickAdapter<SimpleFavoriteItem, BaseDataBindingHolder<ItemCompanyHomeBinding>>(R.layout.item_company_home) {
    private var itemClickListener: OnItemClickListener? = null
    private var type: Int = -1

    fun onRefreshData(list: List<SimpleFavoriteItem>?, type: Int) {
        val size = list?.size ?: 0
        if (size > 0) {
            setList(list)
        } else {
            setList(arrayListOf(SimpleFavoriteItem()))
        }
        this.type = type
        notifyDataSetChanged()
    }

    override fun convert(
        holder: BaseDataBindingHolder<ItemCompanyHomeBinding>,
        item: SimpleFavoriteItem
    ) {
        // 获取 Binding
        val binding: ItemCompanyHomeBinding = holder.dataBinding ?: return
        binding.bean = item
        binding.type = type
        binding.executePendingBindings()

        //整行点击
        binding.item.setDebouncedOnClickListener {
            itemClickListener?.onItemClick(item)
        }
        ViewClickEffectUtils.addClickScale(binding.item, CLICKED_SCALE_95)

        //删除按钮操作
        binding.delete.setDebouncedOnClickListener {
            itemClickListener?.onDeleteClick(item)
        }
        ViewClickEffectUtils.addClickScale(binding.delete, CLICKED_SCALE_95)
        //修改地址按钮操作
        binding.change.setDebouncedOnClickListener {
            itemClickListener?.onChangeClick()
        }
        ViewClickEffectUtils.addClickScale(binding.change, CLICKED_SCALE_95)

        //手动打开左滑菜单
        binding.more.setDebouncedOnClickListener {
            if (TextUtils.isEmpty(item.item_id)) {
                itemClickListener?.onItemClick(item)
            } else {
                itemClickListener?.onOpenMenuClick()
            }
        }
        ViewClickEffectUtils.addClickScale(binding.more, CLICKED_SCALE_90)
    }

    fun setItemClickListener(itemClickListener: OnItemClickListener?) {
        this.itemClickListener = itemClickListener
    }

    interface OnItemClickListener {
        fun onItemClick(favoriteItem: SimpleFavoriteItem) //整行点击
        fun onDeleteClick(favoriteItem: SimpleFavoriteItem) //删除按钮操作
        fun onChangeClick() //修改地址操作
        fun onOpenMenuClick() //手动打开左滑菜单
    }
}