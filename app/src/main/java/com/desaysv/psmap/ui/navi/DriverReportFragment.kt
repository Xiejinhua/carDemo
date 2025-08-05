package com.desaysv.psmap.ui.navi

import android.graphics.Rect
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.autosdk.bussiness.common.POI
import com.autosdk.common.AutoStatus
import com.autosdk.common.utils.AutoGuideLineHelper
import com.autosdk.common.utils.ResUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.AutoStatusAdapter
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.databinding.FragmentDriverReportBinding
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.DriverReportParkingAdapter
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * @author 谢锦华
 * @time 2024/12/25
 * @description 驾驶行为报告 禁行弹框
 */
@AndroidEntryPoint
class DriverReportFragment : Fragment() {
    private lateinit var binding: FragmentDriverReportBinding
    private val viewModel by viewModels<DriverReportViewModel>()

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    private lateinit var driverReportParkingAdapter: DriverReportParkingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDriverReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        AutoStatusAdapter.sendStatus(AutoStatus.DRIVING_REPORT_FRAGMENT_START)
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        viewModel.setRect(getMapPreviewRect())
        initEventOperation()
    }

    private fun initEventOperation() {

        //知道了
        binding.ivGotIt.setDebouncedOnClickListener { findNavController().navigateUp() }
        ViewClickEffectUtils.addClickScale(binding.ivGotIt, CLICKED_SCALE_95)

        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            view?.run { skyBoxBusiness.updateView(this, true) }
        }

        driverReportParkingAdapter = DriverReportParkingAdapter().also { adapter ->
            adapter.setOnItemClickListener(object : DriverReportParkingAdapter.OnItemClickListener {
                override fun onGoThere(poi: POI?) {
                    removeCloseDriverReportTimer()
                    viewModel.setBtTip(getString(R.string.sv_route_know))
                    poi?.let {
                        val commandBean = CommandRequestRouteNaviBean.Builder().build(it)
                        viewModel.planRoute(commandBean)
                    }
                }
            })
        }
        binding.parkingRecyclerView.adapter = driverReportParkingAdapter
        removeCloseDriverReportTimer()
        startCloseDriverReportTimer()

        viewModel.parkingRecommend.observe(viewLifecycleOwner) {

            driverReportParkingAdapter.updateData(it)
        }
        val tripOk = viewModel.getTripReportTrack()//获取行程报告轨迹
        Timber.i("getTripReportTrack is tripOk=$tripOk")
    }

    private fun getMapPreviewRect(): Rect {
        val rect = Rect()
        val margin = ResUtil.getDimension(com.desaysv.psmap.base.R.dimen.sv_dimen_24)
        rect.left =
            AutoGuideLineHelper.getCardWidthByGuideLine(binding.glVertical) + ResUtil.getDimension(com.desaysv.psmap.base.R.dimen.sv_dimen_552)
        rect.top = ResUtil.getDimension(com.desaysv.psmap.base.R.dimen.sv_dimen_152) + margin
        rect.right = ResUtil.getDimension(com.desaysv.psmap.base.R.dimen.sv_dimen_68) + 2 * margin
        rect.bottom = ResUtil.getDimension(com.desaysv.psmap.base.R.dimen.sv_dimen_104) + margin
        return rect
    }

    private val closeDriverReportTimer = object : CountDownTimer(11000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val time: Int = (millisUntilFinished / 1000).toDouble().roundToInt()
            val tip = getString(R.string.sv_route_know)
            viewModel.setBtTip("$tip（${time}S）")
        }

        override fun onFinish() {
            Timber.i(" closeDriverReportTimer onFinish is called ")
            findNavController().navigateUp()
        }
    }

    private fun removeCloseDriverReportTimer() = closeDriverReportTimer.cancel()
    private fun startCloseDriverReportTimer() = closeDriverReportTimer.start()

    override fun onDestroyView() {
        super.onDestroyView()
        removeCloseDriverReportTimer()
        AutoStatusAdapter.sendStatus(AutoStatus.DRIVING_REPORT_FRAGMENT_EXIT)
    }
}