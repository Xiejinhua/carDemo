package com.desaysv.psmap.ui.adapter

import android.annotation.SuppressLint
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemIssueFeedbackBinding
import com.desaysv.psmap.model.bean.IssueFeedbackBean
import com.desaysv.psmap.model.utils.ViewClickEffectUtils

/**
 * 问题反馈Adapter
 */
class IssueFeedbackAdapter :
    BaseQuickAdapter<IssueFeedbackBean?, BaseDataBindingHolder<ItemIssueFeedbackBinding>>(
        R.layout.item_issue_feedback
    ) {

    fun onRefreshData(list: List<IssueFeedbackBean>?) {
        val size = list?.size ?: 0
        if (size > 0) {
            setList(list)
        }
        notifyDataSetChanged()
    }

    @SuppressLint("CheckResult")
    override fun convert(
        holder: BaseDataBindingHolder<ItemIssueFeedbackBinding>,
        item: IssueFeedbackBean?
    ) {
        // 获取 Binding
        val binding = holder.dataBinding
        if (binding != null) {
            binding.item = data[holder.layoutPosition]
            binding.executePendingBindings()
            ViewClickEffectUtils.addClickScale(binding.itemSl)
        }
    }
}