package com.desaysv.psmap.ui.adapter

import com.autonavi.gbl.user.msgpush.model.AimPushMsg
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemAimPushMsgBinding
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener

/**
 * 我的消息Adapter
 */
class MyMessageAdapter :
    BaseQuickAdapter<AimPushMsg, BaseDataBindingHolder<ItemAimPushMsgBinding>>(R.layout.item_aim_push_msg) {
    private var itemClickListener: OnItemClickListener? = null

    fun onRefreshData(list: List<AimPushMsg>?) {
        val size = list?.size ?: 0
        if (size > 0) {
            setList(list)
        }
        notifyDataSetChanged()
    }

    override fun convert(holder: BaseDataBindingHolder<ItemAimPushMsgBinding>, item: AimPushMsg) {
        // 获取 Binding
        val binding: ItemAimPushMsgBinding = holder.dataBinding ?: return
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

    fun setItemClickListener(itemClickListener: OnItemClickListener?) {
        this.itemClickListener = itemClickListener
    }

    interface OnItemClickListener {
        fun onItemClick(aimPushMsg: AimPushMsg) //整行点击
        fun onDeleteClick(aimPushMsg: AimPushMsg) //删除按钮操作
        fun onCancelClick() //取消按钮操作
    }
}