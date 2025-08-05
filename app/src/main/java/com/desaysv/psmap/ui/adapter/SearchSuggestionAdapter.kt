package com.desaysv.psmap.ui.adapter

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import com.autonavi.auto.skin.NightModeGlobal
import com.autosdk.bussiness.common.POI
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemSearchSuggestionBinding
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.search.bean.SearchHistoryBean


class SearchSuggestionAdapter :
    BaseQuickAdapter<SearchHistoryBean, BaseDataBindingHolder<ItemSearchSuggestionBinding>>(R.layout.item_search_suggestion) {
    var keyword: String? = null

    override fun convert(
        holder: BaseDataBindingHolder<ItemSearchSuggestionBinding>,
        item: SearchHistoryBean
    ) {
        // 获取 Binding
        holder.dataBinding?.run {
            this.item = item
            this.isNight = NightModeGlobal.isNightMode()
            this.position = holder.layoutPosition
            executePendingBindings()
            top.setDebouncedOnClickListener {
                onItemClickListener?.onItemClick(getItem(holder.layoutPosition))
            }
            ivSuggestionGotoThere.setDebouncedOnClickListener {
                onItemClickListener?.onItemRightClick(getItem(holder.layoutPosition))
            }
            llSuggestionChild1.setDebouncedOnClickListener {
                onItemClickListener?.onChildPoiClick(item.poi?.childPois?.get(0))
            }
            llSuggestionChild2.setDebouncedOnClickListener {
                onItemClickListener?.onChildPoiClick(item.poi?.childPois?.get(1))
            }
            item.poi?.childPois?.let { childPois ->
                if (childPois.isNotEmpty()) {
                    tvSuggestionChild1Name.text = childPois[0].shortname
                    if (childPois[0].ratio != 0.toDouble()) {
                        tvSuggestionChild1Ratio.text = String.format("%.0f%%", childPois[0].ratio)
                        tvSuggestionChild1Ratio.visibility = View.VISIBLE
                    } else {
                        tvSuggestionChild1Ratio.visibility = View.GONE
                    }
                    if (childPois.size > 1) {
                        tvSuggestionChild2Name.text = childPois[1].shortname
                        if (childPois[1].ratio != 0.toDouble()) {
                            tvSuggestionChild2Ratio.text =
                                String.format("%.0f%%", childPois[1].ratio)
                            tvSuggestionChild2Ratio.visibility = View.VISIBLE
                        } else {
                            tvSuggestionChild2Ratio.visibility = View.GONE
                        }
                    }
                }
            }
            keyword?.let { keyword ->
                if (keyword.isNotEmpty()) {
                    val colorRes = if (NightModeGlobal.isNightMode())
                        com.desaysv.psmap.model.R.color.onTertiaryContainerNight
                    else
                        com.desaysv.psmap.model.R.color.onTertiaryContainerDay
                    val color = ContextCompat.getColor(tvSuggestionName.context, colorRes)
                    tvSuggestionName.text = getKeywordSpannableString(tvSuggestionName.text.toString(), keyword, color)
                    tvSuggestionNameMax.text = getKeywordSpannableString(tvSuggestionNameMax.text.toString(), keyword, color)
                }
            }

            ViewClickEffectUtils.addClickScale(ivSuggestionGotoThere, CLICKED_SCALE_90)
            ViewClickEffectUtils.addClickScale(llSuggestionChild1, CLICKED_SCALE_95)
            ViewClickEffectUtils.addClickScale(llSuggestionChild2, CLICKED_SCALE_95)
        }
    }

    //设置文本中关键字的颜色
    private fun getKeywordSpannableString(
        text: String,
        keyword: String,
        color: Int
    ): SpannableString {
        val spannableString = SpannableString(text)
        var position: Int = text.indexOf(keyword)
        while (position != -1) {
            // 设置关键字的颜色
            spannableString.setSpan(
                ForegroundColorSpan(color),
                position,
                position + keyword.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            position = text.indexOf(keyword, position + keyword.length)
        }
        return spannableString
    }


    fun updateData(poiList: List<SearchHistoryBean>?) {
        poiList?.let { setList(it) }
        notifyDataSetChanged()
    }

    private var onItemClickListener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    interface OnItemClickListener {

        /**
         * 点击item
         *
         * @param SearchHistoryBean  选中的item
         */
        fun onItemClick(bean: SearchHistoryBean)

        /**
         * 点击item的右侧图标
         *
         * @param SearchHistoryBean  选中的item
         */
        fun onItemRightClick(bean: SearchHistoryBean)

        /**
         * 点击子Poi
         *
         * @param Poi  选中的子POI
         */
        fun onChildPoiClick(poi: POI?)

    }

}