package com.desaysv.psmap.ui.adapter

import android.widget.ImageView
import coil.load
import coil.transform.CircleCropTransformation
import com.autonavi.auto.skin.NightModeGlobal
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.databinding.ItemAgroupItemInviteFriendsBinding
import com.desaysv.psmap.model.bean.CustomGroupMember
import com.txzing.sdk.bean.HistoryFriendResponse
import com.txzing.sdk.bean.UserInfo
import timber.log.Timber

/**
 * 组队出行-邀请好友Adapter
 */
class InviteJoinAdapter :
    BaseQuickAdapter<CustomGroupMember?, BaseDataBindingHolder<ItemAgroupItemInviteFriendsBinding>>(com.desaysv.psmap.R.layout.item_agroup_item_invite_friends) {
    private val selectedMembers = mutableSetOf<Int>()
    private var onSelectionChangedListener: OnSelectionChangedListener? = null

    override fun convert(
        holder: BaseDataBindingHolder<ItemAgroupItemInviteFriendsBinding>,
        item: CustomGroupMember?
    ) {
        Timber.d("InviteJoinAdapter convert:${item?.user_id} item:${item?.nick_name}")
        // 获取 Binding
        val binding: ItemAgroupItemInviteFriendsBinding = holder.dataBinding ?: return
        if (item != null) {
            binding.item = item
            binding.executePendingBindings()

            loadImage(binding.sivAddfriend, item)
            binding.cbInviteSelectBg.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedMembers.add(item.user_id)
                } else {
                    selectedMembers.remove(item.user_id)
                }
                onSelectionChangedListener?.onSelectionChanged(selectedMembers.isEmpty())
            }
        }
    }

    private fun loadImage(imageView: ImageView, item: CustomGroupMember?) {
        imageView.load(item?.head_img) {
            crossfade(true) //渐进进出
            placeholder(if (NightModeGlobal.isNightMode()) com.desaysv.psmap.base.R.drawable.ic_default_avatar_night else com.desaysv.psmap.base.R.drawable.ic_default_avatar_day)//加载中占位图
            error(if (NightModeGlobal.isNightMode()) com.desaysv.psmap.base.R.drawable.ic_default_avatar_night else com.desaysv.psmap.base.R.drawable.ic_default_avatar_day)//加载错误占位图
            transformations(CircleCropTransformation())  //圆形图
        }
    }

    fun updateData(poiList: List<CustomGroupMember?>) {
        Timber.d("InviteJoinAdapter updateData poiList:${poiList.size}")
        if (poiList.isNotEmpty()) {
            selectedMembers.clear()
            setList(poiList)
        }
        notifyDataSetChanged()
    }

    fun getSelectedMembers(): List<Int> {
        return selectedMembers.toList()
    }

    fun updateDataList(
        position: Int,
        members: List<UserInfo>?
    ) {
        notifyItemChanged(position)
    }

    interface OnSelectionChangedListener {
        fun onSelectionChanged(isEmpty: Boolean)
    }

    fun setOnSelectionChangedListener(listener: OnSelectionChangedListener) {
        onSelectionChangedListener = listener
    }
}
