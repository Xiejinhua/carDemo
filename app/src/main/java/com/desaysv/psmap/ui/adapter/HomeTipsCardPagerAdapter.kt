package com.desaysv.psmap.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.auto.skin.SkinManager
import com.desaysv.psmap.R
import com.desaysv.psmap.base.bean.HomeCardTipsData
import com.desaysv.psmap.base.bean.HomeCardTipsType
import com.desaysv.psmap.databinding.ItemHomeTipsCardBinding
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import timber.log.Timber


class HomeTipsCardPagerAdapter(
    private var cardData: ArrayList<HomeCardTipsData>,
    private var onActionClick: (type: HomeCardTipsData) -> Unit,
    private var onCloseClick: (type: HomeCardTipsType) -> Unit
) :
    RecyclerView.Adapter<HomeTipsCardPagerAdapter.CardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val dataBinding: ItemHomeTipsCardBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), R.layout.item_home_tips_card,
            parent, false
        )
        return CardViewHolder(dataBinding)
    }

    override fun getItemCount(): Int {
        return cardData.size
    }

    fun updateData(data: List<HomeCardTipsData>) {
        cardData.clear()
        cardData.addAll(data)
        notifyDataSetChanged()
        Timber.i("updateData")
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.dataBinding.isNight = NightModeGlobal.isNightMode()
        holder.dataBinding.cardData = cardData[position]
        holder.dataBinding.ivClose.setDebouncedOnClickListener {
            val type = cardData[position].type
            removeCard(position)
            onCloseClick.invoke(type)
        }

        holder.dataBinding.sdwAction.setDebouncedOnClickListener {
            val data = cardData[position]
            removeCard(position)
            onActionClick.invoke(data)
        }
        SkinManager.getInstance().updateView(holder.itemView, true)
        ViewClickEffectUtils.addClickScale(holder.dataBinding.ivClose, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(holder.dataBinding.sdwAction, CLICKED_SCALE_95)
    }

    // 新增移除卡片的方法
    private fun removeCard(position: Int) {
        Timber.i("removeCard position=$position cardData.size=${cardData.size}")
        if (position >= 0 && position < cardData.size) {
            cardData.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, cardData.size)
        }
    }

    class CardViewHolder internal constructor(val dataBinding: ItemHomeTipsCardBinding) :
        RecyclerView.ViewHolder
            (dataBinding.root) {
    }
}