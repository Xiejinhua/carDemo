package com.desaysv.psmap.ui.search.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.autonavi.auto.skin.view.SkinConstraintLayout
import com.desaysv.psmap.databinding.ViewCustomSinglePoiBinding
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.search.bean.SearchResultBean


/**
 * Search bar view
 *
 * 搜索单个结果控件
 */
class CustomSinglePoiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SkinConstraintLayout(context, attrs, defStyleAttr) {
    private var binding: ViewCustomSinglePoiBinding = ViewCustomSinglePoiBinding.inflate(LayoutInflater.from(context), this, true)
    private lateinit var searchResultBean: SearchResultBean

    init {
        binding.ivClose.setDebouncedOnClickListener { listener?.onCloseClick() }
        binding.ivPhoneCall.setDebouncedOnClickListener { listener?.onPhoneCall(searchResultBean.poi?.phone) }
        binding.ivFavorite.setDebouncedOnClickListener { listener?.onFavoriteClick(searchResultBean) }
        binding.ivSearchAround.setDebouncedOnClickListener { listener?.onSearchAround(searchResultBean) }
        binding.ivGoHere.setDebouncedOnClickListener {
            listener?.onGoThere(searchResultBean)
        }
        ViewClickEffectUtils.addClickScale(binding.ivClose, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.ivPhoneCall, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.ivFavorite, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.ivSearchAround, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.ivGoHere, CLICKED_SCALE_95)
    }

    fun updateData(searchResultBean: SearchResultBean) {
        this.searchResultBean = searchResultBean
        binding.item = searchResultBean
    }

    fun getSearchResultBean(): SearchResultBean {
        return searchResultBean
    }

    private var listener: onSinglePoiListener? = null

    fun setOnSinglePoiListener(listener: onSinglePoiListener) {
        this.listener = listener
    }

    interface onSinglePoiListener {
        fun onCloseClick()
        fun onPhoneCall(phone: String?)
        fun onFavoriteClick(searchResultBean: SearchResultBean)
        fun onSearchAround(searchResultBean: SearchResultBean)
        fun onGoThere(searchResultBean: SearchResultBean)
    }

}