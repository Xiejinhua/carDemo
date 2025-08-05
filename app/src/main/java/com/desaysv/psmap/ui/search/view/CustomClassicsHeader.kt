package com.desaysv.psmap.ui.search.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.autonavi.auto.skin.NightModeGlobal
import com.autosdk.common.utils.CommonUtil
import com.autosdk.common.utils.ResUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ViewSearchListHeaderBinding
import com.scwang.smart.refresh.layout.api.RefreshHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.constant.RefreshState
import com.scwang.smart.refresh.layout.simple.SimpleComponent
import timber.log.Timber

/**
 * @author 张楠
 * @time 2024/12/02
 * @description
 */
class CustomClassicsHeader @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    SimpleComponent(context, attrs, defStyle),
    RefreshHeader {
    private var binding: ViewSearchListHeaderBinding = ViewSearchListHeaderBinding.inflate(LayoutInflater.from(context), this, true)

    private var isTrip = 0; //0：默认 1：我的行程 2.路书

    var page = 1
        set(value) {
            field = value
            if (isTrip == 1){
                binding.ivHeaderTitle.text = ResUtil.getString(R.string.sv_search_list_header_sync_data)
                binding.ivHeaderArrow.visibility = GONE
                binding.ivHeaderArrow.animate().rotation(0f)
            } else {
                if (field == 1) {
                    binding.ivHeaderTitle.text = ResUtil.getString(R.string.sv_search_list_header_no_refresh)
                    binding.ivHeaderArrow.visibility = GONE
                    binding.ivHeaderTitle.setTextColor(
                        if (NightModeGlobal.isNightMode())
                            resources.getColor(com.desaysv.psmap.model.R.color.onSecondaryNight, null)
                        else
                            resources.getColor(com.desaysv.psmap.model.R.color.onSecondaryDay, null)
                    )
                    binding.ivHeaderTitle.setTextColor(com.desaysv.psmap.model.R.color.onSecondaryDay, com.desaysv.psmap.model.R.color.onSecondaryNight)
                }else{
                    binding.ivHeaderTitle.text = ResUtil.getString(R.string.sv_search_list_header_pulldown, CommonUtil.numberToChinese(field))
                    binding.ivHeaderArrow.visibility = VISIBLE
                    binding.ivHeaderTitle.setTextColor(
                        if (NightModeGlobal.isNightMode())
                            resources.getColor(com.desaysv.psmap.model.R.color.onPrimaryNight, null)
                        else
                            resources.getColor(com.desaysv.psmap.model.R.color.onPrimaryDay, null)
                    )
                    binding.ivHeaderTitle.setTextColor(com.desaysv.psmap.model.R.color.onPrimaryDay, com.desaysv.psmap.model.R.color.onPrimaryNight)
                }
            }
        }

    override fun onStateChanged(refreshLayout: RefreshLayout, oldState: RefreshState, newState: RefreshState) {
        Timber.i("CustomClassicsHeader onStateChanged isTrip:$isTrip")
        when (newState) {
            RefreshState.PullDownToRefresh -> {
                if (isTrip == 1){
                    binding.ivHeaderTitle.text = ResUtil.getString(R.string.sv_search_list_header_sync_data)
                    binding.ivHeaderArrow.visibility = GONE
                    binding.ivHeaderArrow.animate().rotation(0f)
                } else {
                    if (page == 1) {
                        binding.ivHeaderTitle.text = ResUtil.getString(R.string.sv_search_list_header_no_refresh)
                        binding.ivHeaderArrow.visibility = GONE
                        binding.ivHeaderArrow.animate().rotation(0F)
                        binding.ivHeaderTitle.setTextColor(
                            if (NightModeGlobal.isNightMode())
                                resources.getColor(com.desaysv.psmap.model.R.color.onSecondaryNight, null)
                            else
                                resources.getColor(com.desaysv.psmap.model.R.color.onSecondaryDay, null)
                        )
                        binding.ivHeaderTitle.setTextColor(com.desaysv.psmap.model.R.color.onSecondaryDay, com.desaysv.psmap.model.R.color.onSecondaryNight)
                    }else{
                        binding.ivHeaderTitle.text = ResUtil.getString(R.string.sv_search_list_header_pulldown, CommonUtil.numberToChinese(page))
                        binding.ivHeaderArrow.visibility = VISIBLE
                        binding.ivHeaderArrow.animate().rotation(0F)

                        binding.ivHeaderTitle.setTextColor(
                            if (NightModeGlobal.isNightMode())
                                resources.getColor(com.desaysv.psmap.model.R.color.onPrimaryNight, null)
                            else
                                resources.getColor(com.desaysv.psmap.model.R.color.onPrimaryDay, null)
                        )
                        binding.ivHeaderTitle.setTextColor(com.desaysv.psmap.model.R.color.onPrimaryDay, com.desaysv.psmap.model.R.color.onPrimaryNight)
                    }
                }
            }

            RefreshState.ReleaseToRefresh -> {
                if (isTrip == 1){
                    binding.ivHeaderTitle.text = ResUtil.getString(R.string.sv_search_list_header_sync_data)
                    binding.ivHeaderArrow.visibility = GONE
                    binding.ivHeaderArrow.animate().rotation(0f)
                } else {
                    binding.ivHeaderTitle.text = ResUtil.getString(R.string.sv_search_list_header_pulldown, page) + ResUtil.getString(R.string.sv_search_list_header_release_to_refresh)
                    binding.ivHeaderArrow.visibility = VISIBLE
                    binding.ivHeaderArrow.animate().rotation(180f)
                }
            }

            RefreshState.Refreshing,
            RefreshState.RefreshReleased -> {
                if (isTrip == 1){
                    binding.ivHeaderTitle.text = ResUtil.getString(R.string.sv_search_list_header_sync_data)
                    binding.ivHeaderArrow.visibility = GONE
                    binding.ivHeaderArrow.animate().rotation(0f)
                } else {
                    binding.ivHeaderTitle.setText(R.string.sv_search_list_header_refreshing)
                    binding.ivHeaderArrow.visibility = GONE
                    binding.ivHeaderLoading.visibility = visibility
                }
            }

            RefreshState.RefreshFinish -> {
                if (isTrip == 1){
                    binding.ivHeaderTitle.text = ResUtil.getString(R.string.sv_search_list_header_sync_data)
                    binding.ivHeaderArrow.visibility = GONE
                    binding.ivHeaderArrow.animate().rotation(0f)
                } else {
                    binding.ivHeaderArrow.animate().rotation(0F)
                    binding.ivHeaderLoading.visibility = GONE
                    binding.ivHeaderTitle.text = ResUtil.getString(R.string.sv_search_list_header_pulldown, CommonUtil.numberToChinese(page))
                }
            }

            else -> {
                if (isTrip == 1){
                    binding.ivHeaderTitle.text = ResUtil.getString(R.string.sv_search_list_header_sync_data)
                    binding.ivHeaderArrow.visibility = GONE
                    binding.ivHeaderArrow.animate().rotation(0f)
                } else {
                    Timber.i("Header else 默认")
                }
            }
        }

    }

    //是否是我的行程界面
    fun setIsTrip(isTrip: Int = 0){
        this.isTrip = isTrip
    }

    //设置日夜模式
    fun setNight(isNight: Boolean) {
        val drawable = if (isNight) binding.ivHeaderLoading.context?.getDrawable(com.desaysv.psmap.model.R.drawable.rotate_loading_view_night) else
            binding.ivHeaderLoading.context?.getDrawable(com.desaysv.psmap.model.R.drawable.rotate_loading_view_day)
        if (drawable != null) {
            drawable.setBounds(
                0, 0,
                binding.ivHeaderLoading.resources?.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_48) ?: 0,
                binding.ivHeaderLoading.resources?.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_48) ?: 0
            ) //这个四参数指的是drawable将在被绘制在canvas的哪个矩形区域内
            binding.ivHeaderLoading.indeterminateDrawable = drawable
        }
    }
}