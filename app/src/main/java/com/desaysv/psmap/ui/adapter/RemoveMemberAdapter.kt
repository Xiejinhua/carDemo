package com.desaysv.psmap.ui.adapter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.widget.ImageView
import com.autonavi.auto.skin.NightModeGlobal
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.databinding.ItemAgroupRemoveFriendBinding
import com.txzing.sdk.bean.UserInfo
import timber.log.Timber


/**
 * 组队出行-移除组员Adapter
 */
class RemoveMemberAdapter :
    BaseQuickAdapter<UserInfo?, BaseDataBindingHolder<ItemAgroupRemoveFriendBinding>>(com.desaysv.psmap.R.layout.item_agroup_remove_friend) {

    private val selectedMembers = mutableSetOf<Int>()
    private var onSelectionChangedListener: OnSelectionChangedListener? = null

    @SuppressLint("CheckResult")
    override fun convert(
        holder: BaseDataBindingHolder<ItemAgroupRemoveFriendBinding>,
        item: UserInfo?
    ) {
        // 获取 Binding
        val binding: ItemAgroupRemoveFriendBinding = holder.dataBinding ?: return
        if (item != null) {
            binding.userInfo = item
            if (item.head_img != null) {
                if (binding.sivAddfriend.tag == null || !binding.sivAddfriend.tag.equals(item.head_img)) {
                    loadImage(binding.sivAddfriend, item.head_img, item.user_id.toString())
                }
            }
            binding.cbSelect.isChecked = false
            binding.executePendingBindings()

            binding.cbSelect.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedMembers.add(item.user_id)
                } else {
                    selectedMembers.remove(item.user_id)
                }
                onSelectionChangedListener?.onSelectionChanged(selectedMembers.isEmpty())
            }

        }
    }

    private fun loadImage(
        imageView: ImageView,
        imgUrl: String?,
        uid: String,
    ) {
        Glide.with(imageView.context).asBitmap()
            .load(imgUrl).apply(RequestOptions.circleCropTransform().skipMemoryCache(false))
            .placeholder(if (NightModeGlobal.isNightMode()) com.desaysv.psmap.base.R.drawable.ic_default_avatar_night else com.desaysv.psmap.base.R.drawable.ic_default_avatar_day) //加载中占位图
            .error(if (NightModeGlobal.isNightMode()) com.desaysv.psmap.base.R.drawable.ic_default_avatar_night else com.desaysv.psmap.base.R.drawable.ic_default_avatar_day)//加载错误占位图
            .addListener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Timber.d("onLoadFailed")
                    imageView.tag = uid
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    Timber.d("onResourceReady")
                    imageView.tag = imgUrl
                    return false
                }
            }).into(imageView)
    }

    fun updateData(poiList: List<UserInfo>) {
        val size = poiList.size
        if (size > 0) {
            selectedMembers.clear()
            setList(poiList)
        }
        notifyDataSetChanged()
    }

    fun getSelectedMembers(): List<Int> {
        return selectedMembers.toList()
    }

    interface OnSelectionChangedListener {
        fun onSelectionChanged(isEmpty: Boolean)
    }

    fun setOnSelectionChangedListener(listener: OnSelectionChangedListener) {
        onSelectionChangedListener = listener
    }
}