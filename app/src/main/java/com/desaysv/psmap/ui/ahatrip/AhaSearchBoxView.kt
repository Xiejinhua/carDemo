package com.desaysv.psmap.ui.ahatrip

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import com.autonavi.auto.skin.view.SkinConstraintLayout
import com.autosdk.view.KeyboardUtil
import com.desaysv.psmap.databinding.ViewAhaSearchBoxBinding
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95

/**
 * 路书 Search box view
 *
 * 带EditText的顶部搜索栏
 */
class AhaSearchBoxView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SkinConstraintLayout(context, attrs, defStyleAttr) {
    private var binding: ViewAhaSearchBoxBinding

    init {
        binding = ViewAhaSearchBoxBinding.inflate(LayoutInflater.from(context), this, true)

        //删除按钮删除EditText中的内容
        binding.sivSearchDelete.setOnClickListener { binding.searchAroundEditText.text.clear() }
        ViewClickEffectUtils.addClickScale(binding.sivSearchDelete, CLICKED_SCALE_90)
    }

    //设置日夜模式
    fun setNight(isNight: Boolean) {
        binding.isNight = isNight
    }

    //设置标题
    fun setText(title: String?) {
        binding.searchAroundEditText.setText(title)
    }

    //设置标题提示
    fun setHint(title: String?) {
        binding.searchAroundEditText.hint = title
    }

    // 避免键盘全屏显示
    fun setImeOptions() {
        binding.searchAroundEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI)
    }

    //键盘显示
    fun hideKeyboard(hide: Boolean) {
        if (hide) {
            KeyboardUtil.hideKeyboard(binding.searchAroundEditText)
        } else {
            KeyboardUtil.showKeyboard(binding.searchAroundEditText)
        }
    }

    //setSelection
    fun setSelection(length: Int) {
        binding.searchAroundEditText.setSelection(length)
    }

    //输入框文字监听事件
    fun editAddTextChangedListener(listener: TextWatcher?) {
        binding.searchAroundEditText.addTextChangedListener(listener)
    }

    //输入框是否能输入
    fun enableEditText(enable: Boolean) {
        binding.searchAroundEditText.isEnabled = enable
    }

    //输入框点击事件
    @SuppressLint("ClickableViewAccessibility")
    fun editOnTouchListener(listener: OnTouchListener?) {
        binding.searchAroundEditText.setOnTouchListener(listener)
    }

    //清空按钮点击操作事件
    fun deleteOnClickListener(listener: OnClickListener?) {
        binding.sivSearchDelete.setOnClickListener(listener)
        ViewClickEffectUtils.addClickScale(binding.sivSearchDelete, CLICKED_SCALE_90)
    }

    //搜索按钮点击操作事件
    fun searchOnClickListener(listener: OnClickListener?) {
        binding.search.setOnClickListener(listener)
        ViewClickEffectUtils.addClickScale(binding.search, CLICKED_SCALE_90)
    }

    //键盘确定键监听
    fun setOnEditorActionListener(listener: OnEditorActionListener?) {
        binding.searchAroundEditText.setOnEditorActionListener(listener)
    }

    fun getTextContent() = binding.searchAroundEditText.text.toString()

    fun showDeleteBtn(show: Boolean) {
        binding.sivSearchDelete.visibility = if (show) VISIBLE else GONE
    }

    //是否隐藏loading按钮
    fun showLoadingBtn(show: Boolean) {
        binding.clsLoading.visibility = if (show) VISIBLE else GONE
    }

    //路书城市按钮点击操作事件
    fun switchTripCityOnClickListener(listener: OnClickListener?) {
        binding.clTripCity.setOnClickListener(listener)
        ViewClickEffectUtils.addClickScale(binding.clTripCity, CLICKED_SCALE_95)
    }

    fun showTripCityBtn(show: Boolean) {
        binding.clTripCity.visibility = if (show) VISIBLE else GONE
    }

    //设置路书搜索城市名称
    fun setTripCityName(title: String?) {
        binding.tvTripCityTitle.text = title
    }

    fun getTripCityName(): String{
        return binding.tvTripCityTitle.text.toString()
    }
}