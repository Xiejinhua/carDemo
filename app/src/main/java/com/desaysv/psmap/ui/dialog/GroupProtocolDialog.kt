package com.desaysv.psmap.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.databinding.DialogGroupProtocolBinding
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 组队出行服务协议dialog
 */
@AndroidEntryPoint
class GroupProtocolDialog : DialogFragment() {
    private lateinit var binding: DialogGroupProtocolBinding

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var settingAccountBusiness: SettingAccountBusiness

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
        binding = DialogGroupProtocolBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initBinding()
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
        initEventOperation()
    }

    private fun initEventOperation() {
        //关闭
        binding.closeTipIv.setDebouncedOnClickListener { dismissAllowingStateLoss() }

        //日夜模式监听
        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            skyBoxBusiness.updateView(binding.root, true)
        }
    }
}