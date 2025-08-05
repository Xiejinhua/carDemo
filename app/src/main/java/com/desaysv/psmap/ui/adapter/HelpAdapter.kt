package com.desaysv.psmap.ui.adapter

import android.annotation.SuppressLint
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemHelpBinding
import com.desaysv.psmap.model.bean.HelpBean

/**
 * 帮助Adapter
 */
class HelpAdapter : BaseQuickAdapter<HelpBean?, BaseDataBindingHolder<ItemHelpBinding>>(
    R.layout.item_help
) {

    fun onRefreshData(list: List<HelpBean>?) {
        val size = list?.size ?: 0
        if (size > 0) {
            setList(list)
        }
        notifyDataSetChanged()
    }

    @SuppressLint("CheckResult")
    override fun convert(holder: BaseDataBindingHolder<ItemHelpBinding>, item: HelpBean?) {
        // 获取 Binding
        val binding = holder.dataBinding
        if (binding != null) {
            binding.item = data[holder.layoutPosition]
            binding.executePendingBindings()
        }
    }
}