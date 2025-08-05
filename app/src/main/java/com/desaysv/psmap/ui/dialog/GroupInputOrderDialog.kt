package com.desaysv.psmap.ui.dialog

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.autosdk.common.AutoConstant
import com.autosdk.view.KeyboardUtil
import com.desaysv.psmap.base.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.databinding.DialogGroupInputOrderBinding
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 组队加入队伍dialog
 */
@AndroidEntryPoint
class GroupInputOrderDialog : DialogFragment() {
    private lateinit var binding: DialogGroupInputOrderBinding
    private var params: ConstraintLayout.LayoutParams? = null
    private var mListener: OnDialogListener? = null // 自定义接口OnDialogListener
    private var dialogWindow: Window? = null

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var settingAccountBusiness: SettingAccountBusiness

    @Inject
    lateinit var toastUtil: ToastUtil

    override fun show(manager: FragmentManager, tag: String?) {
        manager.beginTransaction().remove(this).commitAllowingStateLoss()
        if (!isAdded) {
            val ft = manager.beginTransaction()
            ft.add(this, tag)
            ft.commitAllowingStateLoss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, com.desaysv.psmap.model.R.style.CustomDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogGroupInputOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initBinding()
        initEventOperation()
    }

    override fun onDestroy() {
        super.onDestroy()
        dialogWindow?.let {
            KeyboardUtil.unregisterSoftInputChangedListener(it)
        }
    }

    override fun onPause() {
        super.onPause()
        dismissAllowingStateLoss()
    }

    override fun dismiss() {
        dismissAllowingStateLoss()
    }

    override fun dismissAllowingStateLoss() {
        if (isAdded) super.dismissAllowingStateLoss()
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner

        dialog?.setCanceledOnTouchOutside(false) //空白处不能取消动画
        isCancelable = false //返回键不能取消

        binding.setSearchTeam.isFocusable = true;
        binding.setSearchTeam.isFocusableInTouchMode = true;
        binding.setSearchTeam.requestFocus();
        dialogWindow = requireActivity().window
        params = binding.dialog.layoutParams as ConstraintLayout.LayoutParams?
        paramsTopMargin(false) //设置params的topMargin
        KeyboardUtil.registerSoftInputChangedListener(dialogWindow!!, softInputChangedListener)
    }

    private fun initEventOperation() {
        //取消按钮
        binding.cancel.setDebouncedOnClickListener {
            setShowInputClickResult(false, "") //判断是否确定输入口令加入队伍
        }

        //确定按钮
        binding.ok.setDebouncedOnClickListener {
            if (!TextUtils.isEmpty(binding.setSearchTeam.text.toString())) {
                setShowInputClickResult(true, binding.setSearchTeam.getText().toString().trim()) //判断是否确定输入口令加入队伍
            } else {
                toastUtil.showToast("口令为空，请输入口令")
            }
        }

        //日夜模式监听
        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            skyBoxBusiness.updateView(binding.root, true)
        }
    }

    private val softInputChangedListener: KeyboardUtil.OnSoftInputChangedListener =
        KeyboardUtil.OnSoftInputChangedListener { isSoftInputVisible ->
            paramsTopMargin(isSoftInputVisible > 0) //设置params的topMargin
        }

    //设置params的topMargin
    private fun paramsTopMargin(isSoftInputVisible: Boolean) {
        params!!.topMargin = if (isSoftInputVisible) resources
            .getDimensionPixelSize(R.dimen.sv_dimen_88) else (AutoConstant.mScreenHeight - resources
            .getDimensionPixelSize(R.dimen.sv_dimen_352)) / 2
        binding.dialog.layoutParams = params
    }

    //判断是否确定输入口令加入队伍
    private fun setShowInputClickResult(isSureJoin: Boolean, mContent: String) {
        KeyboardUtil.hideKeyboard(binding.root)
        binding.setSearchTeam.setText("")
        dismissAllowingStateLoss()
        mListener?.setOnClick(isSureJoin, mContent);//false:退出账号-取消 true:退出账号-确定
    }

    fun setOnClickListener(listener: OnDialogListener) { // 为确定按钮和取消按钮设置监听器
        mListener = listener
    }


    fun interface OnDialogListener {
        // 自定义接口，确定/取消按钮的点击事件
        fun setOnClick(isOk: Boolean, mContent: String?) // 确定按钮点击
    }

}