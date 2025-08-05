package com.desaysv.psmap.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.autonavi.auto.skin.NightModeGlobal
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.bussiness.navi.route.utils.RouteLifecycleMonitor
import com.desaysv.psmap.R
import com.desaysv.psmap.base.bean.HomeCardTipsType
import com.desaysv.psmap.base.data.INaviRepository
import com.desaysv.psmap.base.tracking.EventTrackingUtils
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.LastRouteUtils
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentHomeBinding
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.bean.CommandRequestSearchBean
import com.desaysv.psmap.model.bean.MapCommandType
import com.desaysv.psmap.model.utils.NaviLaneUtil
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.HomeTipsCardPagerAdapter
import com.desaysv.psmap.ui.home.compose.CruiseLaneComposeView
import com.desaysv.psmap.ui.settings.AccountAndSettingTab
import com.desaysv.psmap.ui.theme.DsDefaultTheme
import com.desaysv.psmap.utils.LoadingUtil
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


/**
 * @author 谢锦华
 * @time 2024/1/8
 * @description
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel by viewModels<HomeViewModel>()

    @Inject
    lateinit var mLastRouteUtils: LastRouteUtils

    @Inject
    lateinit var mRouteLifecycleMonitor: RouteLifecycleMonitor

    @Inject
    lateinit var mRouteRequestController: RouteRequestController

    @Inject
    lateinit var mINaviRepository: INaviRepository

    @Inject
    lateinit var toast: ToastUtil

    @Inject
    lateinit var loadingUtil: LoadingUtil

    @Inject
    lateinit var gson: Gson

    //Fragment数据共享方式一：通过同一个MainViewModel做数据共享
//    private val mainViewModel by lazy { ViewModelProvider(requireActivity())[MainViewModel::class.java] }

    var mCardPagerAdapter: HomeTipsCardPagerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false).apply {
            cvLaneInfo.setContent {
                CruiseLaneView()
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initEventOperation()
        initTask()
    }

    override fun onResume() {
        super.onResume()
        Timber.i("onResume")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("HomeFragment onDestroy()")
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        //viewModel.homeTipsCardVisibility.value = false

//        viewModel.sendReqAddressPredict() // 预测用户家/公司的位置

        mCardPagerAdapter = HomeTipsCardPagerAdapter(
            arrayListOf(),
            { cardData ->
                Timber.i("action click cardType ${cardData.type} ")
                when (cardData.type) {
                    HomeCardTipsType.CONTINUE_NAVI -> {
                        mLastRouteUtils.getLastRouteCarResultData()?.let {
                            mRouteLifecycleMonitor.setPathResult(it.second)
                            mRouteRequestController.carRouteResult = it.first
                            val commandBean = CommandRequestRouteNaviBean.Builder().buildByFile(it.first)
                            viewModel.startNavi(commandBean)
                        }
                    }

                    HomeCardTipsType.OPEN_TRAFFIC_RESTRICTION -> {
                        findNavController().navigate(
                            R.id.to_accountAndSettingFragment,
                            Bundle().also {
                                it.putInt(
                                    BaseConstant.ACCOUNT_SETTING_TAB,
                                    AccountAndSettingTab.SETTING
                                )
                            })
                    }

                    HomeCardTipsType.TRAFFIC_RESTRICTION -> {
                        Timber.i("TRAFFIC_RESTRICTION is called ${gson.toJson(viewModel.restrictInfoDetails.value)}")
                        viewModel.restrictInfoDetails.value?.let {
                            NavHostFragment.findNavController(this@HomeFragment).navigate(R.id.action_homeFragment_to_restrictFragment)
                        }
                    }

                    HomeCardTipsType.DOWNLOAD_OFFLINE_DATA, HomeCardTipsType.LONG_TERM_OFFLINE -> {
                        //离线地图
                        findNavController().navigate(R.id.to_offlineMapFragment)
                    }

                    HomeCardTipsType.LOW_FUEL -> {
                        val commandBean = CommandRequestSearchBean.Builder().setKeyword("加油站")
                            .setType(CommandRequestSearchBean.Type.SEARCH_KEYWORD).build()
                        NavHostFragment.findNavController(this@HomeFragment).navigate(
                            R.id.action_homeFragment_to_searchResultFragment,
                            commandBean.toBundle()
                        )
                    }

                    HomeCardTipsType.SEND_TO_CAR_POI -> {
                        Timber.i("SEND_TO_CAR_POI ${cardData.poi?.name}")
                        cardData.poi?.run {
                            viewModel.showPoiCard(this)
                        }
                    }

                    HomeCardTipsType.SEND_TO_CAR_ROUTE -> {
                        Timber.i("SEND_TO_CAR_ROUTE ${cardData.poi?.name}")
                        cardData.poi?.run {
                            val commandBean = CommandRequestRouteNaviBean.Builder().build(this)
                            viewModel.planRoute(commandBean)
                        }
                    }

                    HomeCardTipsType.FORECAST_POI -> {
                        cardData.poi?.run {
                            val commandBean = CommandRequestRouteNaviBean.Builder().build(this)
                            viewModel.planRoute(commandBean)
                        }
                    }

                    HomeCardTipsType.TRAVEL_RECOMMEND_POI -> {
                        cardData.poi?.run {
                            val commandBean = CommandRequestRouteNaviBean.Builder().build(this)
                            viewModel.planRoute(commandBean)
                        }
                    }

                    HomeCardTipsType.PREDICT_HOME -> {
                        cardData.poi?.run {
                            if (!viewModel.addHome(this))
                                toast.showToast(R.string.sv_search_add_home_error)
                        }
                    }

                    HomeCardTipsType.PREDICT_COMPANY -> {
                        cardData.poi?.run {
                            if (!viewModel.addCompany(this))
                                toast.showToast(R.string.sv_search_add_company_error)
                        }
                    }
                }
                viewModel.removeHomeCardTipsData(cardData.type)
            }, { cardType ->
                Timber.i("close click cardType $cardType ")
                when (cardType) {
                    HomeCardTipsType.CONTINUE_NAVI -> {
                        if (!mINaviRepository.isNavigating()) {
                            //删除存储的导航文件
                            mLastRouteUtils.deleteRouteFile()
                        }
                    }

                    HomeCardTipsType.OPEN_TRAFFIC_RESTRICTION -> {}
                    HomeCardTipsType.TRAFFIC_RESTRICTION -> {
                        viewModel.removeRestrictInfoDetails()
                    }

                    HomeCardTipsType.DOWNLOAD_OFFLINE_DATA -> {}
                    HomeCardTipsType.LONG_TERM_OFFLINE -> {}
                    HomeCardTipsType.LOW_FUEL -> {}
                    HomeCardTipsType.SEND_TO_CAR_POI -> {}
                    HomeCardTipsType.SEND_TO_CAR_ROUTE -> {}
                    HomeCardTipsType.FORECAST_POI -> {}
                    HomeCardTipsType.TRAVEL_RECOMMEND_POI -> {}
                    HomeCardTipsType.PREDICT_HOME, HomeCardTipsType.PREDICT_COMPANY -> {
                    }
                }
                if (mCardPagerAdapter?.itemCount == 1) {
                    viewModel.tipsCardTabVisibility.value = false
                } else {
                    viewModel.tipsCardTabVisibility.value = true
                }
                viewModel.removeHomeCardTipsData(cardType)
            }).also {
            binding.tipsCardPager.adapter = it
            TabLayoutMediator(
                binding.tipsCardTabLayout, binding.tipsCardPager
            ) { tab, position ->
                tab.setCustomView(R.layout.view_tab_home_tips)
            }.attach()
        }

        val updateTipsCardPagerHeight = fun(position: Int) {
            binding.tipsCardPager.getChildAt(0)?.let {
                it as RecyclerView
                it.findViewHolderForAdapterPosition(position)?.run {
                    itemView.post {
                        // 测量页面内容高度
                        itemView.measure(
                            View.MeasureSpec.makeMeasureSpec(binding.tipsCardPager.width, View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.UNSPECIFIED
                        )
                        val newHeight = itemView.measuredHeight
                        // 设置 ViewPager2 高度
                        val params = binding.tipsCardPager.layoutParams
                        params.height = newHeight
                        binding.tipsCardPager.layoutParams = params
                        Timber.i("binding.tipsCardPager.height=$newHeight")
                    }

                }
            }
        }

        mCardPagerAdapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                Timber.d("ViewPager2 onChanged")
                lifecycleScope.launch {
                    delay(50)
                    updateTipsCardPagerHeight(0)
                }
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                Timber.d("ViewPager2 onItemRangeRemoved")
                lifecycleScope.launch {
                    delay(50)
                    updateTipsCardPagerHeight(0)
                }
            }
        })

        // 监听viewpager页面变化
        binding.tipsCardPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Timber.i("onPageSelected position=$position mCardPagerAdapter?.itemCount=${mCardPagerAdapter?.itemCount}")
                if (mCardPagerAdapter?.itemCount == 0) {
                    viewModel.homeTipsCardVisibility.value = false
                }

                if (mCardPagerAdapter?.itemCount == 1) {
                    viewModel.tipsCardTabVisibility.value = false
                } else {
                    viewModel.tipsCardTabVisibility.value = true
                }
                updateTipsCardPagerHeight(position)
                Timber.i("binding.homeTipsCard.height=${binding.homeTipsCard.height}")
            }
        })

        viewModel.themeChange.observe(this.viewLifecycleOwner) { isNight ->
            Timber.i("themeChange isNight=$isNight")
            themeChange(isNight)
        }
    }

    private fun themeChange(isNight: Boolean) {
        val tabLayout = binding.tipsCardTabLayout
        // 遍历每个 Tab 并设置背景颜色和间隔
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            tab?.let {
                tab.setCustomView(R.layout.view_tab_home_tips)
            }
        }

        tabLayout.setSelectedTabIndicatorColor(
            tabLayout.resources.getColor(
                if (isNight) com.desaysv
                    .psmap.model.R.color.primaryContainerSecondaryNight else com.desaysv
                    .psmap.model.R.color.primaryContainerSecondaryDay
            )
        )

        mCardPagerAdapter?.notifyDataSetChanged()
    }

    private fun initEventOperation() {
        binding.tvSearch.setDebouncedOnClickListener {
            findNavController().navigate(R.id.searchFragment, CommandRequestSearchBean().toBundle())
        }
        binding.ivSearch.setDebouncedOnClickListener {
            findNavController().navigate(R.id.searchFragment, CommandRequestSearchBean().toBundle())
        }
        binding.btFavorite.setDebouncedOnClickListener {
            findNavController().navigate(R.id.to_homeFavoriteFragment)
            EventTrackingUtils.trackEvent(
                EventTrackingUtils.EventName.Nav_Start,
                mapOf(Pair(EventTrackingUtils.EventValueName.FavoritesClick, System.currentTimeMillis()))
            )
        }

        binding.btJietuLogo.setDebouncedOnClickListener {
            NavHostFragment.findNavController(this@HomeFragment).navigate(R.id.to_ahaTripMainFragment, CommandRequestSearchBean().toBundle())
            EventTrackingUtils.trackEvent(
                EventTrackingUtils.EventName.JetourOnly_Click,
                mapOf(
                    Pair(EventTrackingUtils.EventValueName.JetourOnlyClick, System.currentTimeMillis())
                )
            )
        }

        //登录状态回调
        viewModel.loginLoading.unPeek().observe(viewLifecycleOwner) { integer ->
            if (integer == BaseConstant.LOGIN_STATE_SUCCESS || integer == BaseConstant.LOGOUT_STATE_LOADING) {
                Timber.d("loginLoading 处于登录状态")
            }
        }

        viewModel.setToast.unPeek().observe(viewLifecycleOwner) {
            toast.showToast(it)
        }

        viewModel.tipsCardList.observe(viewLifecycleOwner) {
            Timber.i("tipsCardList=${it.isNullOrEmpty()}")
            if (it.isNullOrEmpty()) {
                viewModel.homeTipsCardVisibility.value = false
            } else {
                viewModel.homeTipsCardVisibility.value = true
                mCardPagerAdapter?.updateData(it)
                if (it.size == 1) {
                    viewModel.tipsCardTabVisibility.value = false
                } else {
                    viewModel.tipsCardTabVisibility.value = true
                }
            }
        }

        binding.btGoHome.setDebouncedOnClickListener {
            Timber.i("btGoHome")
            if (viewModel.getHomePoi() == null) {
                val commandBean =
                    CommandRequestSearchBean.Builder().setType(CommandRequestSearchBean.Type.SEARCH_HOME).build()
                findNavController().navigate(R.id.to_searchAddHomeFragment, commandBean.toBundle())
            } else {
                viewModel.getHomePoi()?.let {
                    val commandBean = CommandRequestRouteNaviBean.Builder().build(it)
                    viewModel.planRoute(commandBean)
                }
            }
            EventTrackingUtils.trackEvent(
                EventTrackingUtils.EventName.Nav_Start,
                mapOf(Pair(EventTrackingUtils.EventValueName.HomeClick, System.currentTimeMillis()))
            )
        }

        binding.btGoCompany.setDebouncedOnClickListener {
            Timber.i("btGoCompany")
            if (viewModel.getCompanyPoi() == null) {
                val commandBean =
                    CommandRequestSearchBean.Builder().setType(CommandRequestSearchBean.Type.SEARCH_COMPANY).build()
                findNavController().navigate(R.id.to_searchAddHomeFragment, commandBean.toBundle())
            } else {
                viewModel.getCompanyPoi()?.let {
                    val commandBean = CommandRequestRouteNaviBean.Builder().build(it)
                    viewModel.planRoute(commandBean)
                }
            }
            EventTrackingUtils.trackEvent(
                EventTrackingUtils.EventName.Nav_Start,
                mapOf(Pair(EventTrackingUtils.EventValueName.CompanyClick, System.currentTimeMillis()))
            )
        }

        viewModel.commutingScenariosData.observe(viewLifecycleOwner) { data ->
            data?.run { binding.viewCommutingScenarios.updateData(data) }
        }

        binding.viewCommutingScenarios.clickListener({
            viewModel.hideCommutingScenariosCard()
        }, { data ->
            mRouteRequestController.carRouteResult?.run {
                val commandBean = CommandRequestRouteNaviBean.Builder().build(this)
                viewModel.planRoute(commandBean)
            }
            viewModel.hideCommutingScenariosCard()
        })

        ViewClickEffectUtils.addClickScale(binding.ivSearch, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.btGoHome, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.btGoCompany, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.btFavorite, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.btJietuLogo, CLICKED_SCALE_90)
    }

    private fun initTask() {
        if (viewModel.continueSapaNavi()) {
            mLastRouteUtils.getLastRouteCarResultData()?.let {
                Timber.i("continueSapaNavi")
                mRouteLifecycleMonitor.setPathResult(it.second)
                mRouteRequestController.carRouteResult = it.first
                val commandBean = CommandRequestRouteNaviBean.Builder().buildByFile(it.first)
                viewModel.startNavi(commandBean)
            }
            viewModel.clearContinueSapaNavi()
        }

        viewModel.mapCommand.unPeek().observe(viewLifecycleOwner) { command ->
            if (command.mapCommandType == MapCommandType.Confirm) {
                Timber.i("ConfirmNavi1")
                if (viewModel.tipsCardList.value?.isNotEmpty() == true && viewModel.tipsCardList.value?.first()?.type == HomeCardTipsType.CONTINUE_NAVI) {
                    Timber.i("ConfirmNavi2")
                    mLastRouteUtils.getLastRouteCarResultData()?.let {
                        mRouteLifecycleMonitor.setPathResult(it.second)
                        mRouteRequestController.carRouteResult = it.first
                        val commandBean = CommandRequestRouteNaviBean.Builder().buildByFile(it.first)
                        viewModel.startNavi(commandBean)
                        viewModel.confirmNavi(command.mapCommandType)
                    }
                }

            }
        }
    }

    /**
     * 车道线列表
     */
    @Composable
    private fun CruiseLaneView() {
        val themeChange by viewModel.themeChange.unPeek()
            .observeAsState(NightModeGlobal.isNightMode())
        val naviLaneList by viewModel.cruiseLaneList.observeAsState(emptyList())
        NaviLaneUtil.initLaneResId()
        DsDefaultTheme(themeChange) {
            Timber.i("CruiseLaneComposeView themeChange = $themeChange")
            CruiseLaneComposeView(laneList = naviLaneList)
        }
    }
}