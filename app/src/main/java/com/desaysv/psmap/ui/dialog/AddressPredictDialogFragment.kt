package com.desaysv.psmap.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.autonavi.gbl.aosclient.model.GAddressPredictResponseParam
import com.autosdk.bussiness.common.POI
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.LayoutAddressPredictBinding
import com.desaysv.psmap.model.bean.CommandRequestSearchBean
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * 家/公司预测位置DialogFragment
 */
@AndroidEntryPoint
class AddressPredictDialogFragment : DialogFragment() {
    private lateinit var binding: LayoutAddressPredictBinding
    private val viewModel by viewModels<AddressPredictViewModel>()
    private var aAddressPredictResponseParam: GAddressPredictResponseParam? = null
    private var mListener: OnDialogListener? = null // 自定义接口OnDialogListener

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    fun newInstance(aAddressPredictResponseParam: GAddressPredictResponseParam?): AddressPredictDialogFragment {
        val fragment = AddressPredictDialogFragment()
        val args = Bundle()
        args.putSerializable(BaseConstant.ADDRESS_PREDICT_PUSH_MSG, aAddressPredictResponseParam)
        fragment.arguments = args
        return fragment
    }

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
        setStyle(STYLE_NO_FRAME, com.desaysv.psmap.model.R.style.MobileDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutAddressPredictBinding.inflate(inflater, container, false)
        skyBoxBusiness.updateView(binding.root, true)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        setAttributes()
        initBinding()
        initEventOperation()
    }

    override fun dismiss() {
        dismissAllowingStateLoss()
    }

    override fun dismissAllowingStateLoss() {
        if (isAdded) super.dismissAllowingStateLoss()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun initBinding() {
        dialog?.setCanceledOnTouchOutside(false) //空白处不能取消动画
        isCancelable = false //返回键不能取消

        aAddressPredictResponseParam = arguments?.getSerializable(BaseConstant.ADDRESS_PREDICT_PUSH_MSG) as GAddressPredictResponseParam?
        viewModel.initData(aAddressPredictResponseParam)
    }

    private fun setAttributes() {
        val dialogWindow: Window? = dialog?.window
        if (dialogWindow != null) {
            dialogWindow.decorView.setPadding(0, 0, 0, 0)
            dialogWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val lp: WindowManager.LayoutParams = dialogWindow.attributes
            lp.width = resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_556)
            lp.height = resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_319)
            lp.y = resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_280)
            lp.x = resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_561)
            lp.gravity = Gravity.TOP or Gravity.LEFT
            lp.dimAmount = 0f
            lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_FULLSCREEN
            dialogWindow.attributes = lp
            val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN
            dialogWindow.decorView.systemUiVisibility = uiOptions
        }
    }

    private fun initEventOperation() {
        //关闭
        binding.ivClose.setDebouncedOnClickListener {
            dismissAllowingStateLoss()
        }

        //確定
        binding.ok.setDebouncedOnClickListener {
            viewModel.predictType.value?.let { it1 -> viewModel.predictPoi.value?.let { it2 -> mListener?.setOnClick(it1, it2) } }
        }

        //修改
        binding.change.setDebouncedOnClickListener {
            dismissAllowingStateLoss()
            mListener?.setOnChangeClick(if (viewModel.predictType.value == 1) CommandRequestSearchBean.Type.SEARCH_HOME else CommandRequestSearchBean.Type.SEARCH_COMPANY)
        }

        viewModel.predictAddress.unPeek().observe(viewLifecycleOwner) {
            binding.content.text = it
        }
    }

    fun setOnClickListener(listener: OnDialogListener) {
        mListener = listener
    }


    interface OnDialogListener {
        fun setOnClick(predictType: Int, predictPoi: POI) // 确定按钮点击

        fun setOnChangeClick(type: Int) // 修改按钮点击
    }
}
