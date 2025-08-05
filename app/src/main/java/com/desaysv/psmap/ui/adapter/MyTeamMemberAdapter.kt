package com.desaysv.psmap.ui.adapter

import android.graphics.Bitmap
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.autonavi.auto.skin.NightModeGlobal
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.databinding.ItemAgroupMemberBinding
import com.desaysv.psmap.databinding.PopupGroupSettingMenuBinding
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.txzing.sdk.bean.UserInfo
import timber.log.Timber


/**
 * 组队出行-我的队伍Adapter
 */
class MyTeamMemberAdapter :
    BaseQuickAdapter<UserInfo?, BaseDataBindingHolder<ItemAgroupMemberBinding>>(com.desaysv.psmap.R.layout.item_agroup_member) {
    private var onItemListener: OnItemListener? = null
    private var mUserId: String? = null
    private var leaderId: String? = null
    private var mMembersFocusList = ArrayList<Boolean>()


    override fun convert(
        holder: BaseDataBindingHolder<ItemAgroupMemberBinding>,
        item: UserInfo?
    ) {
        // 获取 Binding
        val binding: ItemAgroupMemberBinding = holder.dataBinding ?: return
        if (item != null) {
            binding.isLeader = leaderId == item.user_id.toString()
            binding.isCurLeader = leaderId == mUserId
            binding.isPersonMe = mUserId == item.user_id.toString()
            binding.isAddfriendOne = "999" == item.user_id.toString()
            binding.isMembersFocus = mMembersFocusList.getOrNull(holder.layoutPosition) ?: false
            binding.userInfo = item
//            binding.sivAddfriend.alpha = if (item.online) 1.0f else 0.4f
            if (item.user_id.toString() != "999" && item.head_img != null) {
                if (binding.sivAddfriend.tag == null || !binding.sivAddfriend.tag.equals(item.head_img)) {
                    loadImage(binding.sivAddfriend, item.head_img, item.user_id.toString(), holder)
                }
            }
            binding.executePendingBindings()

            //item点击
            binding.sivAddfriendOne.setDebouncedOnClickListener {
                onItemListener?.onItemClick(holder.layoutPosition)
            }
            ViewClickEffectUtils.addClickScale(binding.sivAddfriendOne, CLICKED_SCALE_95)
            binding.sivAddfriend.setDebouncedOnClickListener {
                onItemListener?.onItemClick(holder.layoutPosition)
            }
            ViewClickEffectUtils.addClickScale(binding.sivAddfriend, CLICKED_SCALE_95)
            binding.sivMemberSetting.setDebouncedOnClickListener {
                binding.sivMemberSettingSelect.visibility = View.VISIBLE
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
                val lastVisibleItemPosition = layoutManager?.findLastVisibleItemPosition()
                val isLastPosition = lastVisibleItemPosition == holder.layoutPosition && lastVisibleItemPosition > 3
                Timber.i("lastVisibleItemPosition = $lastVisibleItemPosition layoutPosition = ${holder.layoutPosition} isLastPosition = $isLastPosition")
                showPopupWindow(binding.sivMemberSetting, binding, item, isLastPosition, NightModeGlobal.isNightMode())

            }
            ViewClickEffectUtils.addClickScale(binding.sivMemberSetting, CLICKED_SCALE_95)
        }
    }

    @Synchronized
    private fun loadImage(
        imageView: ImageView,
        imgUrl: String?,
        uid: String,
        holder: BaseDataBindingHolder<ItemAgroupMemberBinding>
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
                    onItemListener?.onRefreshHead(holder.layoutPosition)
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
                    onItemListener?.onRefreshHead(holder.layoutPosition)
                    return false
                }
            }).into(imageView)
    }

    @Synchronized
    fun updateData(
        poiList: List<UserInfo>?,
        mUserId: String?,
        leaderId: String?,
        mMembersFocusList: ArrayList<Boolean>
    ) {
        Timber.d("updateData 1")
        this.mUserId = mUserId
        this.leaderId = leaderId
        this.mMembersFocusList.clear()
        this.mMembersFocusList.addAll(mMembersFocusList)
        poiList?.let {
            setList(it)
        }
        notifyDataSetChanged()
    }

    @Synchronized
    fun updateData(
        position: Int,
        mUserId: String?,
        leaderId: String?,
        mMembersFocusList: ArrayList<Boolean>
    ) {
        Timber.d("updateData 2")
        this.mUserId = mUserId
        this.leaderId = leaderId
        this.mMembersFocusList.clear()
        this.mMembersFocusList.addAll(mMembersFocusList)
        notifyItemChanged(position)
    }

    /**
     * 设置item点击事件
     */
    fun setOnItemClickListener(onItemListener: OnItemListener?) {
        this.onItemListener = onItemListener
    }

    /**
     * 列表事件
     */
    interface OnItemListener {
        /**
         * 列表第几项
         */
        fun onItemClick(position: Int)

        fun onTransferCaptainClick(userInfo: UserInfo)

        fun onRemoveMemberClick(userInfo: UserInfo)

        fun onNickNameClick()

        fun onRefreshHead(position: Int)
    }

    private fun showPopupWindow(
        anchorView: View,
        itemAgroupMemberBinding: ItemAgroupMemberBinding,
        item: UserInfo?,
        isLastPosition: Boolean = false,
        isNight: Boolean = false
    ) {
        // 加载气泡框布局
        val binding: PopupGroupSettingMenuBinding = PopupGroupSettingMenuBinding.inflate(LayoutInflater.from(context))
        val popupView: View = binding.root

        // 创建 PopupWindow
        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        binding.isCurLeader = leaderId == mUserId
        binding.isPersonMe = mUserId == item?.user_id.toString()

        // 设置点击事件
        binding.btnTransferCaptain.setDebouncedOnClickListener { v: View? ->
            // 处理队长转让逻辑
            onItemListener?.onTransferCaptainClick(item ?: UserInfo())
            popupWindow.dismiss()
        }

        binding.btnRemoveMember.setDebouncedOnClickListener { v: View? ->
            // 处理移除队伍逻辑
            onItemListener?.onRemoveMemberClick(item ?: UserInfo())
            popupWindow.dismiss()
        }

        binding.btnEditMember.setDebouncedOnClickListener { v: View? ->
            // 处理修改昵称逻辑
            onItemListener?.onNickNameClick()
            popupWindow.dismiss()
        }

        // 监听 PopupWindow 消失事件
        popupWindow.setOnDismissListener {
            itemAgroupMemberBinding.sivMemberSettingSelect.visibility = View.GONE
        }

        // 手动测量视图
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        popupView.measure(widthMeasureSpec, heightMeasureSpec)
        val popupWidth = popupView.measuredWidth
        val popupHeight = popupView.measuredHeight
        Timber.d("showPopupWindow popupWidth:$popupWidth popupHeight:$popupHeight")
        // 显示气泡框
        Timber.d("showPopupWindow popupView.height:${popupView.height}")
        val location = IntArray(2)
        Timber.d("showPopupWindow location x:${location[0]} y:${location[1]}")
        anchorView.getLocationOnScreen(location)
        val x = location[0] + anchorView.width - popupWidth + anchorView.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_20)
        val y = if (isLastPosition) {
            binding.clRoot.setBackgroundResource(
                if (isNight) com.desaysv.psmap.R.drawable.popup_group_setting_menu_last_bg_night
                else com.desaysv.psmap.R.drawable.popup_group_setting_menu_last_bg
            )
            // 获取布局参数
            val layoutParams = binding.llContent.layoutParams as? ConstraintLayout.LayoutParams
            if (layoutParams != null) {
                layoutParams.topMargin = anchorView.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_18)
                layoutParams.bottomMargin = anchorView.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_35)
                binding.llContent.layoutParams = layoutParams
            }
            if (mUserId == item?.user_id.toString()) location[1] - anchorView.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_145)
            else location[1] - anchorView.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_245)
        } else {
            anchorView.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_90) + location[1]
        }
        popupWindow.showAtLocation(anchorView.rootView, Gravity.NO_GRAVITY, x, y)

    }

}