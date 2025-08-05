package com.desaysv.psmap.ui.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemWxSendBinding

/**
 * @author 王漫生
 * @description 微信互联图片Adapter
 */
class WxSendAdapter :
    BaseQuickAdapter<Int, BaseDataBindingHolder<ItemWxSendBinding>>(R.layout.item_wx_send) {
    fun setData() {
        //设置列表数据
        val list: MutableList<Int> = ArrayList()
        list.add(0)
        list.add(1)
        list.add(2)
        list.add(3)
        list.add(4)
        setList(list)
    }

    override fun convert(holder: BaseDataBindingHolder<ItemWxSendBinding>, item: Int) {
        val binding: ItemWxSendBinding? = holder.dataBinding
        if (binding != null) {
            binding.item = item
        }
    }
}
