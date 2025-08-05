package com.desaysv.psmap.ui.route

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.autonavi.auto.skin.NightModeGlobal
import com.autosdk.bussiness.location.LocationController
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.bussiness.widget.route.model.NaviStationItemData
import com.desaysv.psmap.R
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentRouteDetailsBinding
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.route.compose.RouteDetailsListComposeView
import com.desaysv.psmap.ui.theme.DsDefaultTheme
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.abs

/**
 * @author 谢锦华
 * @time 2024/10/17
 * @description 路线详情界面
 */

@AndroidEntryPoint
class RouteDetailsFragment : Fragment() {
    private lateinit var binding: FragmentRouteDetailsBinding
    private val viewModel by viewModels<RouteDetailsViewModel>()

    @Inject
    lateinit var mRouteRequestController: RouteRequestController

    @Inject
    lateinit var toast: ToastUtil

    @Inject
    lateinit var mLocationController: LocationController

    @Inject
    lateinit var iCarInfoProxy: ICarInfoProxy

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("RouteDetailsFragment onCreate()")
    }

    @Inject
    lateinit var gson: Gson
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRouteDetailsBinding.inflate(inflater, container, false).apply {
            composeView.setContent {
                composeRouteDetailsView()
            }
        }
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("RouteDetailsFragment onViewCreated()")
        initBinding()
        initEventOperation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
    }

    private fun initEventOperation() {
        viewModel.setToast.unPeek().observe(viewLifecycleOwner) {
            toast.showToast(it)
        }
        //退出路线规划详情界面
        binding.ivBack.setDebouncedOnClickListener {
            viewModel.handleNewRoute()
            findNavController().navigateUp()
        }
        ViewClickEffectUtils.addClickScale(binding.ivBack, CLICKED_SCALE_90)

        binding.tvAvoidRoad.setDebouncedOnClickListener {
            viewModel.goParrySelect(true)
        }
        ViewClickEffectUtils.addClickScale(binding.tvAvoidRoad, CLICKED_SCALE_95)

        binding.tvParry.setDebouncedOnClickListener {
            viewModel.avoidRoute()
        }
        ViewClickEffectUtils.addClickScale(binding.tvParry, CLICKED_SCALE_95)

        binding.tvSimNavi.setDebouncedOnClickListener {
            // 获取最新的定位信息，重新更新以下算路信息
            val speed = abs(iCarInfoProxy.vehicleSpeed)
            Timber.i("tvSimNavi is called speed = $speed")
            if (speed > 0) {
                toast.showToast(R.string.sv_route_the_vehicle_must_be_stationary)
                return@setDebouncedOnClickListener
            }
            val commandBean = CommandRequestRouteNaviBean.Builder().build(mRouteRequestController.carRouteResult)
            NavHostFragment.findNavController(this@RouteDetailsFragment)
                .navigate(R.id.action_routeDetailsFragment_to_simNaviFragment, commandBean.toBundle())
        }
        ViewClickEffectUtils.addClickScale(binding.tvSimNavi, CLICKED_SCALE_95)
    }

    /**
     * 路线详情列表
     */
    @Composable
    private fun composeRouteDetailsView() {
        val naviStationFatalist by viewModel.naviStationFatalist.observeAsState(emptyList())
        val themeChange by viewModel.themeChange.unPeek().observeAsState(NightModeGlobal.isNightMode())
        val parrySelect by viewModel.parryVisibility.observeAsState(false)

        DsDefaultTheme(themeChange) {
            RouteDetailsListComposeView(
                naviStationFatalist,
                parrySelect, onToastClicked = {
                    toast.showToast(R.string.sv_route_navi_avoidance_route_tips)
                },
                onContinueClicked = { naviStationFata, isParrySelect ->
                    viewModel.setParryClick()
                    if (naviStationFata.isSelect || !isParrySelect) {
                        viewModel.updateRouteDodgeLine(naviStationFata)
                    }
                })
        }
    }
}