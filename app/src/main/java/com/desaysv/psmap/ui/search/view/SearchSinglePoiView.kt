package com.desaysv.psmap.ui.search.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.recyclerview.widget.GridLayoutManager
import com.autonavi.auto.skin.view.SkinConstraintLayout
import com.autosdk.bussiness.common.POI
import com.autosdk.common.utils.ResUtil
import com.desaysv.psmap.databinding.ViewSearchSinglePoiBinding
import com.desaysv.psmap.model.bean.CommandRequestSearchBean
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.SearchResultChildAdapter
import com.desaysv.psmap.ui.search.bean.SearchResultBean
import timber.log.Timber


/**
 * Search bar view
 *
 * 搜索单个结果控件
 */
class SearchSinglePoiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SkinConstraintLayout(context, attrs, defStyleAttr) {
    private var binding: ViewSearchSinglePoiBinding = ViewSearchSinglePoiBinding.inflate(LayoutInflater.from(context), this, true)
    private lateinit var searchResultBean: SearchResultBean
    lateinit var searchResultChildAdapter: SearchResultChildAdapter

    init {

        binding.ivClose.setDebouncedOnClickListener { listener?.onCloseClick() }
        binding.ivPhoneCall.setDebouncedOnClickListener { listener?.onPhoneCall(searchResultBean.poi?.phone) }
        binding.ivFavorite.setDebouncedOnClickListener { listener?.onFavoriteClick(searchResultBean) }
        binding.ivSearchAround.setDebouncedOnClickListener { listener?.onSearchAround(searchResultBean) }
        binding.ivGoHere.setDebouncedOnClickListener {
            if (searchResultChildAdapter.selectPosition() != -1) {
                listener?.onGoThere(SearchResultBean(poi = searchResultChildAdapter.data[searchResultChildAdapter.selectPosition()]))
            } else {
                listener?.onGoThere(searchResultBean)
            }
        }
        binding.ivViaGoHere.setDebouncedOnClickListener { listener?.onGoThere(searchResultBean) }
        binding.ivAddVia.setDebouncedOnClickListener { listener?.onAddVia(searchResultBean) }
        binding.ivAddHome.setDebouncedOnClickListener { listener?.onAddHome(searchResultBean) }
        binding.rlSearchChildStation.layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        searchResultChildAdapter = SearchResultChildAdapter().apply {
            this.setOnSearchResultChildListener(object : SearchResultChildAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    listener?.onChildPoiItemClick(
                        if (this@apply.selectPosition() == position) -1 else position,
                        searchResultChildAdapter.data[position]
                    )
                    searchResultChildAdapter.toggleSelectPosition(position)


                }
            })
        }
        binding.rlSearchChildStation.adapter = searchResultChildAdapter

        binding.ivMoreChild.setDebouncedOnClickListener {
            Timber.i("ivMoreChild click")
            binding.ivMoreChild.isSelected = !binding.ivMoreChild.isSelected
            searchResultBean.poi?.childPois?.run {
                if (this.size > 2) {
                    if (binding.ivMoreChild.isSelected) {
                        if ((this.size) > 6) {
                            binding.rlSearchChildStation.layoutParams = binding.rlSearchChildStation.layoutParams.apply {
                                height = ResUtil.getDimension(com.desaysv.psmap.base.R.dimen.sv_dimen_250)
                            }
                        } else {
                            binding.rlSearchChildStation.layoutParams = binding.rlSearchChildStation.layoutParams.apply {
                                height = android.view.WindowManager.LayoutParams.WRAP_CONTENT
                            }
                        }
                        searchResultChildAdapter.updateData(this)
                    } else {
                        binding.rlSearchChildStation.layoutParams = binding.rlSearchChildStation.layoutParams.apply {
                            height = android.view.WindowManager.LayoutParams.WRAP_CONTENT
                        }
                        searchResultChildAdapter.updateData(this.subList(0, 2))
                    }
                } else {
                    Timber.w("childPois < 2 ")
                    searchResultBean.isChildLayout = this.isNotEmpty()
                    searchResultBean.isMoreVisible = false
                    updateData(searchResultBean)
                }
            }
        }
        ViewClickEffectUtils.addClickScale(binding.ivClose, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.ivSearchAround, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.ivFavorite, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.ivPhoneCall, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.ivGoHere, CLICKED_SCALE_95)
        ViewClickEffectUtils.addClickScale(binding.ivAddHome, CLICKED_SCALE_95)
        ViewClickEffectUtils.addClickScale(binding.ivViaGoHere, CLICKED_SCALE_95)
        ViewClickEffectUtils.addClickScale(binding.ivAddVia, CLICKED_SCALE_95)
    }

    fun updateData(searchResultBean: SearchResultBean) {
        this.searchResultBean = searchResultBean
        binding.item = searchResultBean
        //初始化子POI
        searchResultBean.poi?.childPois?.run {
            if (this.isEmpty()) {
                searchResultBean.isChildLayout = false
                searchResultBean.isMoreVisible = false
            } else {
                searchResultBean.isChildLayout = true
                searchResultBean.isMoreVisible = this.size > 2
                if (this.size > 2 && !binding.ivMoreChild.isSelected) {
                    binding.rlSearchChildStation.layoutParams = binding.rlSearchChildStation.layoutParams.apply {
                        height = android.view.WindowManager.LayoutParams.WRAP_CONTENT
                    }
                    searchResultChildAdapter.updateData(this.subList(0, 2))
                } else {
                    if ((this.size ?: 0) > 6) {
                        binding.rlSearchChildStation.layoutParams = binding.rlSearchChildStation.layoutParams.apply {
                            height = ResUtil.getDimension(com.desaysv.psmap.base.R.dimen.sv_dimen_250)
                        }
                    } else {
                        binding.rlSearchChildStation.layoutParams = binding.rlSearchChildStation.layoutParams.apply {
                            height = android.view.WindowManager.LayoutParams.WRAP_CONTENT
                        }
                    }
                    searchResultChildAdapter.updateData(this)
                }
            }
        }
        binding.ivAddHome.text = when(searchResultBean.type){
            CommandRequestSearchBean.Type.SEARCH_HOME -> "设置为家"
            CommandRequestSearchBean.Type.SEARCH_COMPANY -> "设置为公司"
            CommandRequestSearchBean.Type.SEARCH_TEAM_DESTINATION -> "设置为组队出行目的地"
            else -> "设置为家"
        }
        binding.executePendingBindings()
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
        fun onAddVia(searchResultBean: SearchResultBean)
        fun onAddHome(searchResultBean: SearchResultBean)

        /**
         * 选中子POI,默认-1为不选中
         *
         * @param position  选中的父poi的下标
         * @param resultBean 选中的item
         */
        fun onChildPoiItemClick(childPosition: Int, childPoi: POI?)
    }

}