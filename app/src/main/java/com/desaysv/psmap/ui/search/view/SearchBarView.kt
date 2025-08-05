package com.desaysv.psmap.ui.search.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.autonavi.auto.skin.view.SkinConstraintLayout
import com.desaysv.psmap.databinding.ViewSearchBarBinding
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90


/**
 * Search bar view
 *
 * 不带EditText的顶部搜索栏，只做展示使用，不包含输入关键字输入及搜索按钮的接口
 */
class SearchBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SkinConstraintLayout(context, attrs, defStyleAttr) {
    private var binding: ViewSearchBarBinding

    init {
        binding = ViewSearchBarBinding.inflate(LayoutInflater.from(context), this, true)
        ViewClickEffectUtils.addClickScale(binding.searchBack, CLICKED_SCALE_90)
    }

    //设置标题
    fun setTitle(title: String?) {
        binding.searchAroundTextView.text = title
    }


    //返回键点击操作事件
    fun backOnClickListener(listener: OnClickListener?) {
        binding.searchBack.setOnClickListener(listener)
    }

    //搜索按钮点击操作事件
    fun searchOnClickListener(listener: OnClickListener?) {
        binding.search.setOnClickListener(listener)
    }

    //整条搜索框点击操作事件
    fun searchContentOnClickListener(listener: OnClickListener?) {
        binding.searchContent.setOnClickListener(listener)
    }

    fun hideSearchBtn(hide: Boolean) {
        binding.search.visibility = if (hide) GONE else VISIBLE
    }

}