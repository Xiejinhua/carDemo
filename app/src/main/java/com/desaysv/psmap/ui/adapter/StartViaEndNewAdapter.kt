package com.desaysv.psmap.ui.adapter

import com.autosdk.bussiness.common.POI
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemStartViaEndBinding
import com.google.gson.Gson
import timber.log.Timber

/**
 * @author 谢锦华
 * @time 2024/1/16
 * @description
 */

class StartViaEndNewAdapter :
    BaseQuickAdapter<POI, BaseDataBindingHolder<ItemStartViaEndBinding>>(R.layout.item_start_via_end) {

    fun updateData(poiList: List<POI>?) {
        poiList?.let { setList(it) }
        notifyDataSetChanged()
    }

    override fun convert(holder: BaseDataBindingHolder<ItemStartViaEndBinding>, item: POI) {
        Timber.i("binding item = ${Gson().toJson(item)},index = ${holder.layoutPosition}")
        val binding: ItemStartViaEndBinding? = holder.dataBinding
        binding?.viewModel = item
        binding?.isShow = data.size > 1
        binding?.executePendingBindings()
        binding?.ivDeleteVia?.setOnClickListener {
            callBack?.let {
                it.onDeleteVia(item, holder.layoutPosition)
            }
        }
        binding?.etPoi?.setOnClickListener {
            callBack?.let {
                it.onEditPoi(
                    item,
                    if (data.size - 1 == holder.layoutPosition) 3 else 2,
                    holder.layoutPosition
                )
            }
        }
    }

    private var callBack: DataCallBack? = null
    fun setOnCallBack(callBack: DataCallBack) {
        this.callBack = callBack
    }

    interface DataCallBack {
        //点击修改途经点和终点
        fun onEditPoi(item: POI, type: Int, index: Int)

        //剩下的途经点
        fun onDeleteVia(item: POI, index: Int)
    }


}