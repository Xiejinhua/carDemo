package com.desaysv.psmap.ui.adapter

import com.autonavi.gbl.user.msgpush.model.TeamPushMsg
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemTeamPushMsgBinding
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener

/**
 * 广播消息Adapter
 */
class BroadcastMessageAdapter :
    BaseQuickAdapter<TeamPushMsg, BaseDataBindingHolder<ItemTeamPushMsgBinding>>(R.layout.item_team_push_msg) {
    private var itemClickListener: OnBroadcastItemClickListener? = null

    fun onRefreshData(list: List<TeamPushMsg>?) {
        val size = list?.size ?: 0
        if (size > 0) {
            setList(list)
        }
        notifyDataSetChanged()
    }

    override fun convert(holder: BaseDataBindingHolder<ItemTeamPushMsgBinding>, item: TeamPushMsg) {
        // 获取 Binding
        val binding: ItemTeamPushMsgBinding = holder.dataBinding ?: return
        binding.bean = item
        binding.executePendingBindings()

        //整行点击
        binding.item.setDebouncedOnClickListener {
            itemClickListener?.onItemClick(item)
        }

        //删除按钮操作
        binding.delete.setDebouncedOnClickListener {
            itemClickListener?.onDeleteClick(item)
        }
        ViewClickEffectUtils.addClickScale(binding.delete, CLICKED_SCALE_95)
    }

    fun setItemClickListener(itemClickListener: OnBroadcastItemClickListener?) {
        this.itemClickListener = itemClickListener
    }

    interface OnBroadcastItemClickListener {
        fun onItemClick(teamPushMsg: TeamPushMsg) //整行点击
        fun onDeleteClick(teamPushMsg: TeamPushMsg) //删除按钮操作
        fun onCancelClick() //取消按钮操作
    }
}