package com.desaysv.psmap.ui.adapter

import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.data.model.Voice
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemVoiceDataBinding
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener

/**
 * 语音数据Adapter
 */
class VoiceDataAdapter :
    BaseQuickAdapter<Voice, BaseDataBindingHolder<ItemVoiceDataBinding>>(R.layout.item_voice_data) {
    private var voiceDataItemClickListener: OnVoiceDataItemClickListener? = null
    private var useVoice: Voice? = null

    fun onRefreshData(list: List<Voice>?, useVoice: Voice?) {
        val size = list?.size ?: 0
        if (size > 0) {
            setList(list)
        }
        this.useVoice = useVoice
        notifyDataSetChanged()
    }

    override fun convert(holder: BaseDataBindingHolder<ItemVoiceDataBinding>, item: Voice) {
        // 获取 Binding
        val binding: ItemVoiceDataBinding = holder.dataBinding ?: return
        binding.isNight = NightModeGlobal.isNightMode()
        binding.item = item
        binding.showTitle = showTitle(holder.layoutPosition, data) //是否显示title
        binding.defaultVoceName =
            binding.root.resources.getString(R.string.sv_setting_system_default_voice)
        binding.isUseVoice = (useVoice?.id ?: -1) == item.id //判断使用
        binding.executePendingBindings()

        //整行点击
        binding.itemPlaceholder.setDebouncedOnClickListener {
            voiceDataItemClickListener?.onItemClick(item)
        }

        //下载按钮操作
        binding.pbLoadSl.setDebouncedOnClickListener {
            voiceDataItemClickListener?.onDownLoadClick(item)
        }
        ViewClickEffectUtils.addClickScale(binding.pbLoadSl, CLICKED_SCALE_95)
    }

    //是否显示title
    private fun showTitle(position: Int, dataList: MutableList<Voice>): Boolean {
        if (dataList == null || dataList.size == 0) return false
        if (position == 0) return true
        if (dataList[position - 1].isRecommended != dataList[position].isRecommended) return true
        return false
    }

    fun setVoiceDataItemClickListener(voiceDataItemClickListener: OnVoiceDataItemClickListener?) {
        this.voiceDataItemClickListener = voiceDataItemClickListener
    }

    interface OnVoiceDataItemClickListener {
        fun onItemClick(voice: Voice) //整行点击

        fun onDownLoadClick(voice: Voice) //下载按钮操作
    }
}