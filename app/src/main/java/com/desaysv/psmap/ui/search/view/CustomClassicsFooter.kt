package com.desaysv.psmap.ui.search.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.autosdk.common.utils.CommonUtil
import com.autosdk.common.utils.ResUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ViewSearchListFooterBinding
import com.scwang.smart.refresh.layout.api.RefreshFooter
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.constant.RefreshState
import com.scwang.smart.refresh.layout.constant.SpinnerStyle
import com.scwang.smart.refresh.layout.simple.SimpleComponent
import timber.log.Timber

/**
 * @author 张楠
 * @time 2024/12/02
 * @description
 */
class CustomClassicsFooter @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    SimpleComponent(context, attrs, defStyle),
    RefreshFooter {
    private var binding: ViewSearchListFooterBinding = ViewSearchListFooterBinding.inflate(LayoutInflater.from(context), this, true)
    private var isTrip = 0; //0：默认 1：我的行程 2.路书
    var page = 1
        set(value) {
            field = value
            when (isTrip) {
                1 -> {
                    binding.ivFooterTitle.text = ResUtil.getString(R.string.sv_search_list_footer_all_load_data)
                }
                2 -> {
                    binding.ivFooterTitle.text = ""
                }
                else -> {
                    binding.ivFooterTitle.text = ResUtil.getString(R.string.sv_search_list_header_pulldown, CommonUtil.numberToChinese(field))
                }
            }
        }

    @SuppressLint("SetTextI18n")
    override fun onStateChanged(refreshLayout: RefreshLayout, oldState: RefreshState, newState: RefreshState) {
        Timber.i("CustomClassicsFooter onStateChanged isTrip:$isTrip")
        when (newState) {
            RefreshState.PullUpToLoad -> {
                when (isTrip) {
                    1 -> {
                        binding.ivFooterTitle.text = ResUtil.getString(R.string.sv_search_list_footer_all_load_data)
                        binding.ivFooterArrow.visibility = GONE
                        binding.ivFooterArrow.animate().rotation(0F)
                    }
                    2 -> {
                        binding.ivFooterTitle.text = ""
                        binding.ivFooterArrow.visibility = GONE
                        binding.ivFooterArrow.animate().rotation(0F)
                    }
                    else -> {
                        binding.ivFooterTitle.text = ResUtil.getString(R.string.sv_search_list_header_pulldown, CommonUtil.numberToChinese(page))
                        binding.ivFooterArrow.visibility = VISIBLE
                        binding.ivFooterArrow.animate().rotation(0F)
                    }
                }
            }

            RefreshState.ReleaseToLoad -> {
                when (isTrip) {
                    1 -> {
                        binding.ivFooterTitle.text = ResUtil.getString(R.string.sv_search_list_footer_all_load_data)
                        binding.ivFooterArrow.visibility = GONE
                        binding.ivFooterArrow.animate().rotation(0F)
                    }
                    2 -> {
                        binding.ivFooterTitle.text = ResUtil.getString(R.string.sv_search_list_footer_release_to_load)
                        binding.ivFooterArrow.animate().rotation(180f)
                    }
                    else -> {
                        binding.ivFooterTitle.text = ResUtil.getString(R.string.sv_search_list_header_pulldown, page) + ResUtil.getString(R.string.sv_search_list_footer_release_to_load)
                        binding.ivFooterArrow.animate().rotation(180f)
                    }
                }
            }

            RefreshState.Loading,
            RefreshState.LoadReleased -> {
                if (isTrip == 1){
                    binding.ivFooterTitle.text = ResUtil.getString(R.string.sv_search_list_footer_all_load_data)
                    binding.ivFooterArrow.visibility = GONE
                    binding.ivFooterArrow.animate().rotation(0F)
                }else {
                    binding.ivFooterTitle.setText(R.string.sv_search_list_header_loading)
                    binding.ivFooterArrow.visibility = GONE
                    binding.ivFooterLoading.visibility = visibility
                }
            }

            RefreshState.LoadFinish -> {
                when (isTrip) {
                    1 -> {
                        binding.ivFooterTitle.text = ResUtil.getString(R.string.sv_search_list_footer_all_load_data)
                        binding.ivFooterArrow.visibility = GONE
                        binding.ivFooterArrow.animate().rotation(0F)
                    }
                    2 -> {
                        binding.ivFooterArrow.animate().rotation(0F)
                        binding.ivFooterLoading.visibility = GONE
                        binding.ivFooterTitle.text = ""
                    }
                    else -> {
                        binding.ivFooterArrow.animate().rotation(0F)
                        binding.ivFooterLoading.visibility = GONE
                        binding.ivFooterTitle.text = ResUtil.getString(R.string.sv_search_list_header_pulldown, CommonUtil.numberToChinese(page))
                    }
                }
            }

            else -> {
                if (isTrip == 1){
                    binding.ivFooterTitle.text = ResUtil.getString(R.string.sv_search_list_footer_all_load_data)
                    binding.ivFooterArrow.visibility = GONE
                    binding.ivFooterArrow.animate().rotation(0F)
                } else {
                    Timber.i("Footer else 默认")
                }
            }
        }

    }

    override fun getSpinnerStyle(): SpinnerStyle {
        return SpinnerStyle.FixedBehind
    }

    //是否是我的行程界面
    fun setIsTrip(isTrip: Int = 0){
        this.isTrip = isTrip
    }

    //设置日夜模式
    fun setNight(isNight: Boolean) {
        val drawable = if (isNight) binding.ivFooterLoading.context?.getDrawable(com.desaysv.psmap.model.R.drawable.rotate_loading_view_night) else
            binding.ivFooterLoading.context?.getDrawable(com.desaysv.psmap.model.R.drawable.rotate_loading_view_day)
        if (drawable != null) {
            drawable.setBounds(
                0, 0,
                binding.ivFooterLoading.resources?.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_48) ?: 0,
                binding.ivFooterLoading.resources?.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_48) ?: 0
            ) //这个四参数指的是drawable将在被绘制在canvas的哪个矩形区域内
            binding.ivFooterLoading.indeterminateDrawable = drawable
        }
    }
}