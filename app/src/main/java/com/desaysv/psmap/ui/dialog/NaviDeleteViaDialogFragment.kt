package com.desaysv.psmap.ui.dialog

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.DialogNaviDeleteviaBinding
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 谢锦华
 * @time 2024/1/5
 * @description 删除途经点弹框
 */
@AndroidEntryPoint
class NaviDeleteViaDialogFragment : Fragment() {
    private lateinit var binding: DialogNaviDeleteviaBinding
    private val viewModel by viewModels<NaviDeleteViaDialogViewModel>()

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var mRouteRequestController: RouteRequestController

    @Inject
    lateinit var mNaviBusiness: NaviBusiness
    private var onListener: OnListener? = null

    @Inject
    lateinit var toast: ToastUtil


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogNaviDeleteviaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        initEventOperation()
    }

    private fun initEventOperation() {
        viewModel.setNavToast.unPeek().observe(viewLifecycleOwner) {
            it?.let { nonNullString ->
                if (nonNullString.isNotEmpty()) {
                    toast.showToast(nonNullString)
                }
            }
        }
        viewModel.setRouteToast.unPeek().observe(viewLifecycleOwner) {
            it?.let { nonNullString ->
                if (nonNullString.isNotEmpty()) {
                    toast.showToast(nonNullString)
                }
            }
        }
        //确定按钮
        binding.tvConfirm.setDebouncedOnClickListener {
            viewModel.showViaNaviViaData.value?.let { it1 ->
                viewModel.delMisPoi(it1)
            }
        }
        ViewClickEffectUtils.addClickScale(binding.tvConfirm, CLICKED_SCALE_95)

        //关闭按钮
        binding.closeIv.setDebouncedOnClickListener { findNavController().navigateUp() }
        ViewClickEffectUtils.addClickScale(binding.closeIv, CLICKED_SCALE_90)

        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            view?.run { skyBoxBusiness.updateView(this, true) }
        }

//        viewModel.showViaNaviViaData.observe(viewLifecycleOwner) {
//            val builder = StringBuilder()
//            if (!TextUtils.isEmpty(it.name)) {
//                builder.append(it.name)
//                builder.append("\n")
//            }
//            if (!TextUtils.isEmpty(it.distance)) {
//                builder.append(it.distance)
//            }
//            if (!TextUtils.isEmpty(it.time)) {
//                builder.append(" | ")
//                builder.append(it.time)
//            }
//            binding.tvDistanceTime.text = builder.toString()
//        }
    }

    fun setOnListener(onListener: OnListener): NaviDeleteViaDialogFragment {
        this.onListener = onListener
        return this
    }

    fun interface OnListener {
        fun onSure()
    }

}