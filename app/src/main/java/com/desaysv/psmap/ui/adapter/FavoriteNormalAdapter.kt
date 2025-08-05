package com.desaysv.psmap.ui.adapter

import android.annotation.SuppressLint
import android.view.View
import com.autonavi.gbl.user.behavior.model.SimpleFavoriteItem
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemNormalFavoriteBinding
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener

/**
 * @author 王漫生
 * @project：收藏夹普通收藏点adapter
 */
class FavoriteNormalAdapter :
    BaseQuickAdapter<SimpleFavoriteItem?, BaseDataBindingHolder<ItemNormalFavoriteBinding>>(R.layout.item_normal_favorite) {
    private var favoriteItemImpl: FavoriteItemImpl? = null
    private var isVia = false

    private var funGetDistance: ((SimpleFavoriteItem?) -> String)? = null

    fun onRefreshData(list: List<SimpleFavoriteItem>?) {
        val size = list?.size ?: 0
        if (size > 0) {
            setList(list)
        }
        notifyDataSetChanged()
    }

    @SuppressLint("CheckResult")
    override fun convert(
        holder: BaseDataBindingHolder<ItemNormalFavoriteBinding>,
        item: SimpleFavoriteItem?
    ) {
        // 获取 Binding
        val binding = holder.dataBinding
        if (binding != null) {
            binding.favoriteItem = data[holder.layoutPosition]
            binding.position = (holder.layoutPosition + 1).toString()
            binding.isTop = item?.top_time?.toInt() != 0

            binding.addVia.visibility = if (isVia) {
                View.VISIBLE
            } else View.GONE
            binding.stvDistance.visibility = if (isVia) {
                View.VISIBLE
            } else View.GONE

            binding.stvDistance.text = funGetDistance?.invoke(item)
            binding.executePendingBindings()

            //item点击操作--查看POI详情
            binding.item.setDebouncedOnClickListener {
                if (holder.layoutPosition != -1 && favoriteItemImpl != null) {
                    favoriteItemImpl!!.onItemClick(holder.layoutPosition)
                }
            }

            //删除操作
            binding.delete.setDebouncedOnClickListener {
                if (holder.layoutPosition != -1 && holder.layoutPosition < data.size && favoriteItemImpl != null) {
                    favoriteItemImpl!!.onItemDeleteClick(holder.layoutPosition)
                }
            }
            ViewClickEffectUtils.addClickScale(binding.delete, CLICKED_SCALE_95)

            //置顶操作
            binding.favoriteTop.setDebouncedOnClickListener {
                if (holder.layoutPosition != -1 && holder.layoutPosition < data.size && favoriteItemImpl != null) {
                    favoriteItemImpl!!.onItemTopClick(
                        holder.layoutPosition,
                        item?.top_time?.toInt() == 0
                    )
                }
            }
            ViewClickEffectUtils.addClickScale(binding.favoriteTop, CLICKED_SCALE_95)

            //重命名操作
            binding.changeName.setDebouncedOnClickListener {
                if (holder.layoutPosition != -1 && holder.layoutPosition < data.size && favoriteItemImpl != null) {
                    favoriteItemImpl!!.onItemChangeClick(holder.layoutPosition)
                }
            }
            ViewClickEffectUtils.addClickScale(binding.changeName, CLICKED_SCALE_95)

            //添加途经点操作
            binding.addVia.setDebouncedOnClickListener {
                if (holder.layoutPosition != -1 && holder.layoutPosition < data.size && favoriteItemImpl != null) {
                    favoriteItemImpl!!.onItemAddViaClick(holder.layoutPosition)
                }
            }
            ViewClickEffectUtils.addClickScale(binding.addVia, CLICKED_SCALE_90)
        }
    }

    /**
     * 设置item点击事件
     */
    fun setOnFavoriteItemListener(favoriteItemImpl: FavoriteItemImpl?) {
        this.favoriteItemImpl = favoriteItemImpl
    }

    /**
     * 设置是否显示为途经点添加布局
     */
    fun setIsVia(isVia: Boolean) {
        this.isVia = isVia
        notifyDataSetChanged()
    }

    /**
     * 设置获取距离的函数
     */
    fun setFunGetDistance(value: ((SimpleFavoriteItem?) -> String)?) {
        this.funGetDistance = value
    }

    interface FavoriteItemImpl {
        /**
         * 列表第几项
         */
        fun onItemClick(position: Int)

        /**
         * 删除第几项
         */
        fun onItemDeleteClick(position: Int)

        /**
         * 置顶操作
         * toTop true.置顶 false.取消置顶
         */
        fun onItemTopClick(position: Int, toTop: Boolean)

        /**
         * 重命名第几项
         */
        fun onItemChangeClick(position: Int)

        /**
         * 添加途经点
         */
        fun onItemAddViaClick(position: Int)
    }
}
