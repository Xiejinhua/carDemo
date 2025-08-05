package com.desaysv.psmap.ui.route.restarea

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.databinding.FragmentRestareaDetailsBinding
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
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
 * @time 2024/12/25
 * @description 沿途服务区详情卡片
 */
@AndroidEntryPoint
class RestAreaDetailsFragment : Fragment() {
    private lateinit var binding: FragmentRestareaDetailsBinding
    private val viewModel by viewModels<RestAreaViewModel>()

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var mRouteRequestController: RouteRequestController

    @Inject
    lateinit var toast: ToastUtil

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRestareaDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        initEventOperation()
    }

    private fun initEventOperation() {

        //关闭按钮
        binding.ivClose.setDebouncedOnClickListener { findNavController().navigateUp() }
        ViewClickEffectUtils.addClickScale(binding.ivClose, CLICKED_SCALE_90)

        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            view?.run { skyBoxBusiness.updateView(this, true) }
        }

        viewModel.alongWayPoiDeepInfo.observe(viewLifecycleOwner) {
            it?.let {
                viewModel.handleAlongWayPoiDeepInfo(it.lineChildPois)
            }
        }

        binding.ivViaGoHere.setDebouncedOnClickListener {
            viewModel.alongWayPoiDeepInfo.value?.let {
                val commandBean = CommandRequestRouteNaviBean.Builder().build(it)
                viewModel.planRoute(commandBean)
            }
        }
        ViewClickEffectUtils.addClickScale(binding.ivViaGoHere, CLICKED_SCALE_95)

        binding.ivAddVia.setDebouncedOnClickListener {
            viewModel.alongWayPoiDeepInfo.value?.let {
                val carRouteResult = mRouteRequestController.carRouteResult
                val endPoi = carRouteResult.toPOI
                val viaList = arrayListOf<POI>()
                if (carRouteResult?.hasMidPos() == true) {
                    if (checkViaPoi(it, carRouteResult.midPois)) {
                        viaList.addAll(carRouteResult.midPois)
                    } else {
                        return@let
                    }
                }
                viaList.add(it)
                val commandBean = CommandRequestRouteNaviBean.Builder().buildMisPoi(endPoi, viaList)
                viewModel.planRoute(commandBean)
            }
        }
        ViewClickEffectUtils.addClickScale(binding.ivAddVia, CLICKED_SCALE_95)
    }

    private fun checkViaPoi(addPOI: POI, mMidPois: ArrayList<POI>): Boolean {
        Timber.i("checkViaPoi() called with: addPOI = $addPOI, mMidPois = $mMidPois")
        var result: Boolean = true
        if (!mMidPois.isNullOrEmpty()) {
            if (mMidPois.size >= 15) {
                toast.showToast(com.desaysv.psmap.base.R.string.sv_route_result_addmid_has_15)
                result = false
            } else {
                for (poi in mMidPois) {
                    Timber.i("checkViaPoi() called with: addPOI.id = ${addPOI?.id}, mMidPois = ${poi.id}")
                    if (addPOI.id != null && addPOI.id.equals(poi.id)) {
                        toast.showToast(com.desaysv.psmap.base.R.string.sv_route_via_poi_add_fail)
                        result = false
                    }
                }
            }
        } else {
            result = true
        }
        return result
    }
}