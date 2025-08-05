package com.desaysv.psmap.ui.settings.offlinedata

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.data.model.AreaType
import com.autonavi.gbl.data.model.CityItemInfo
import com.autonavi.gbl.data.model.DownLoadMode
import com.autonavi.gbl.data.model.OperationType
import com.autonavi.gbl.data.model.TaskStatusCode
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_DOING
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_PAUSE
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_READY
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_SUCCESS
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_WAITING
import com.autosdk.view.KeyboardUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.MapDataBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentOfflineMapBinding
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_93
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.MapDataCityListAdapter
import com.desaysv.psmap.ui.dialog.CustomDialogFragment
import com.desaysv.psmap.ui.dialog.OfflineDataDialogFragment
import com.desaysv.psmap.utils.ExpandableUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 离线地图主界面
 */
@AndroidEntryPoint
class OfflineMapFragment : Fragment() {
    private lateinit var binding: FragmentOfflineMapBinding
    private val viewModel by viewModels<OfflineMapViewModel>()

    private var showFlowDialog: CustomDialogFragment? = null
    private var deleteDialog: OfflineDataDialogFragment? = null
    private var pauseDialog: OfflineDataDialogFragment? = null
    private var continueDialog: OfflineDataDialogFragment? = null
    private var baseDownLoadDialog: OfflineDataDialogFragment? = null
    private var updateDialog: OfflineDataDialogFragment? = null
    private var isFirstOpen = true
    private var isOtherMapDataPage = false //是否在其他离线地图界面

    private var mapDataCityListAdapter: MapDataCityListAdapter? = null
    private var searchDataAdapter: MapDataCityListAdapter? = null

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var mapDataBusiness: MapDataBusiness

    @Inject
    lateinit var toastUtil: ToastUtil

    @Inject
    lateinit var netWorkManager: NetWorkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    //城市列表保存状态
    private fun toSaveInstanceState() {
        val groupCount = mapDataCityListAdapter!!.safeGroupCount
        val collapsedState = BooleanArray(groupCount)
        for (i in 0 until groupCount) {
            collapsedState[i] = mapDataCityListAdapter!!.isCollapsed[i]
        }
        viewModel.savedInstanceState.postValue(collapsedState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOfflineMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initData()
        initEventOperation()
        getCityListAdapterState() //城市列表恢复状态
    }

    //城市列表恢复状态
    private fun getCityListAdapterState() {
        val collapsedState = viewModel.savedInstanceState.value
        collapsedState?.forEachIndexed { index, isCollapsed ->
            if (isCollapsed) {
                mapDataCityListAdapter!!.collapseGroup(index)
            } else {
                mapDataCityListAdapter!!.expandGroup(index)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (!isOtherMapDataPage)
            mapDataBusiness.unregisterMapDataObserver()
    }

    override fun onResume() {
        super.onResume()
        if (isFirstOpen) {
            isFirstOpen = false
        } else {
            mapDataBusiness.registerMapDataObserver(viewModel.mapDataObserverImp)
        }
        lifecycleScope.launch {
            delay(500)
            refreshALLOfflineData() //实时更新离线数据，刷新布局
        }
    }
    override fun onStop() {
        super.onStop()
        Timber.i("onStop()")
        dismissCustomDialog()
        dismissCustomOfflineDialog(deleteDialog) //dismiss 删除对话框
        deleteDialog = null
        dismissCustomOfflineDialog(pauseDialog) //dismiss 暂停对话框
        pauseDialog = null
        dismissCustomOfflineDialog(continueDialog) //dismiss 继续对话框
        continueDialog = null
        dismissCustomOfflineDialog(baseDownLoadDialog) //dismiss 基础包对话框
        baseDownLoadDialog = null
        dismissCustomOfflineDialog(updateDialog) //dismiss 继续对话框
        updateDialog = null
    }


    override fun onDestroy() {
        super.onDestroy()
        mapDataBusiness.unregisterMapDataObserver()
        mapDataBusiness.abortDataListCheck(DownLoadMode.DOWNLOAD_MODE_NET)

        mapDataCityListAdapter = null
        searchDataAdapter = null
        viewModel.showSearch.postValue(false)
        viewModel.showSearchList.postValue(-1) //是否显示搜索列表 -1.空白不显示， 0.列表为空 1.显示搜索列表
        viewModel.setSearchButtonType(0) //搜索框是否显示删除，加载按钮状态 0.不显示删除，加载按钮 1.显示删除 2.显示加载
        findNavController().removeOnDestinationChangedListener(onDestinationChangedListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.i("onDestroyView()")
        if (!isOtherMapDataPage)
            mapDataBusiness.unregisterMapDataObserver()
        toSaveInstanceState() //城市列表保存状态
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            if (!isOtherMapDataPage) {
                mapDataBusiness.unregisterMapDataObserver()
            } else {
                Timber.i("在离线地图其他界面")
            }
        } else {
            mapDataBusiness.registerMapDataObserver(viewModel.mapDataObserverImp)
        }
        if (!hidden) {
            refreshALLOfflineData() //实时更新离线数据，刷新布局
        }
    }

    /**
     * Navigation界面目标Fragment变化回调
     */
    private val onDestinationChangedListener = NavController.OnDestinationChangedListener { _, destination, _ ->
        isOtherMapDataPage = destination.id == R.id.offlineMapDownloadManageFragment || destination.id == R.id.nearbyCityFragment
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        KeyboardUtil.hideKeyboard(view)
        binding.searchBox.setHint("请输入省份或城市名称")
        binding.searchBox.setNight(NightModeGlobal.isNightMode())
        findNavController().addOnDestinationChangedListener(onDestinationChangedListener)
    }

    private fun initData() {
        if (mapDataBusiness.isInitSuccess()) {
            Timber.d(" initData ")
            mapDataBusiness.registerMapDataObserver(viewModel.mapDataObserverImp)
            mapDataBusiness.requestDataListCheck(DownLoadMode.DOWNLOAD_MODE_NET, "")
            viewModel.onRequestDataListCheckResult()
            viewModel.getAllCityData()//获取离线城市列表数据
            operateMapDataCityListAdapter() //离线地图城市列表布局Adapter
            operateSearchDataAdapter() //离线地图搜索Adapter
            viewModel.initData()//获取基础包和城市推荐卡片数据
        } else {
            toastUtil.showToast(requireContext().getString(R.string.sv_setting_service_init_fail))
        }
    }

    private fun initEventOperation() {
        //退出离线地图
        binding.searchBox.backOnClickListener {
            if (viewModel.showSearch.value == true) {
                binding.searchBox.hideKeyboard(true)
                binding.searchBox.setText("")
                viewModel.showSearch.postValue(false)
                viewModel.setSearchButtonType(0) //搜索框是否显示删除，加载按钮状态 0.不显示删除，加载按钮 1.显示删除 2.显示加载
                viewModel.showSearchList.postValue(-1) //是否显示搜索列表 -1.空白不显示， 0.列表为空 1.显示搜索列表
                searchDataAdapter?.updateData(arrayListOf())
            } else {
                findNavController().navigateUp()
            }
        }

        //搜索框文字改变时操作
        binding.searchBox.editAddTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                Timber.d("s.toString() = ${s.toString()}")
                val keyword = s.toString().trim()
                viewModel.onInputKeywordChanged(keyword)
            }
        })

        //搜索框是否显示删除，加载按钮状态 0.不显示删除，加载按钮 1.显示删除 2.显示加载
        viewModel.searchButtonType.observe(viewLifecycleOwner) { buttonType ->
            when (buttonType) {
                0 -> {
                    binding.searchBox.showDeleteBtn(false)
                    binding.searchBox.showLoadingBtn(false)
                }

                1 -> {
                    binding.searchBox.showDeleteBtn(true)
                    binding.searchBox.showLoadingBtn(false)
                }

                2 -> {
                    binding.searchBox.showDeleteBtn(false)
                    binding.searchBox.showLoadingBtn(true)
                }

            }

        }

        //更新搜索列表数据
        viewModel.showSearchList.unPeek().observe(viewLifecycleOwner) {
            if (it == 1) {
                searchDataAdapter?.updateData(viewModel.mSearchList)
            }
        }
        viewModel.refreshSearchList.unPeek().observe(viewLifecycleOwner) {
            searchDataAdapter?.updateData(viewModel.mSearchList)
        }

        //搜索列表Adapter操作
        searchDataAdapter?.setDownLoadClickListener(object : MapDataCityListAdapter.OnMapDataItemClickListener {
            override fun onGroupClick(groupPosition: Int) {
                val count = searchDataAdapter?.getSearchItemCount() ?: 0
                if (groupPosition == 0) {
                    binding.elvSearchList.scrollTo(0, 0)
                } else {
                    lifecycleScope.launch {
                        delay(200)
                        binding.elvSearchList.scrollTo(0, count * resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_125))
                    }
                }
            }

            override fun onAllTaskOperateClick(arCode: Int, operateType: Int) {
                onAllTaskOperate(arCode, operateType)
            }

            override fun onStartClick(groupPosition: Int, childPositon: Int, arcode: Int) {
                onAdapterStartClick(groupPosition, childPositon, arcode)
            }

            override fun onItemClick(groupPosition: Int, childPositon: Int, arCode: Int) {
                onAdapterItemClick(groupPosition, childPositon, arCode)
            }
        })

        //进入下载管理界面
        binding.downloadManage.setDebouncedOnClickListener {
            findNavController().navigate(R.id.to_offlineMapDownloadManageFragment)
        }
        binding.downloadManageMore.setDebouncedOnClickListener {
            findNavController().navigate(R.id.to_offlineMapDownloadManageFragment)
        }
        ViewClickEffectUtils.addClickScale(binding.downloadManageMore, CLICKED_SCALE_90)

        //当前定位城市卡片点击
        binding.currentPositionCl.setDebouncedOnClickListener {
            val cityDownLoadItem = viewModel.getCurrentCityCityDownLoadItem()
            val currentCityInfo = viewModel.getCityItemInfo(viewModel.getCurrentCityAdCode())
            if (cityDownLoadItem != null) {
                when (cityDownLoadItem.taskState) {
                    TASK_STATUS_CODE_SUCCESS -> {
                        viewModel.showDeleteDialog(Bundle().apply {
                            putBoolean(BaseConstant.OFFLINE_IS_BATCH, false)
                            putString(BaseConstant.OFFLINE_CITY_NAME, currentCityInfo?.cityName ?: "")
                            putInt(BaseConstant.OFFLINE_AD_CODE, viewModel.getCurrentCityAdCode())
                            putIntegerArrayList(BaseConstant.OFFLINE_AD_CODES, null)
                        })
                    }

                    TASK_STATUS_CODE_READY -> {
                        if (cityDownLoadItem.bUpdate) {
                            viewModel.showUpdateDialog(Bundle().apply {
                                putBoolean(BaseConstant.OFFLINE_IS_BATCH, false)
                                putInt(BaseConstant.OFFLINE_AD_CODE, viewModel.getCurrentCityAdCode())
                                putIntegerArrayList(BaseConstant.OFFLINE_AD_CODES, null)
                            })
                        } else {
                            viewModel.dealListDownLoad(viewModel.getCurrentCityAdCode())
                        }
                    }

                    TASK_STATUS_CODE_DOING, TASK_STATUS_CODE_WAITING -> {
                        viewModel.showPauseAskDialog(Bundle().apply {
                            putBoolean(BaseConstant.OFFLINE_IS_BATCH, false)
                            putInt(BaseConstant.OFFLINE_AD_CODE, viewModel.getCurrentCityAdCode())
                            putIntegerArrayList(BaseConstant.OFFLINE_AD_CODES, null)
                        })
                    }

                    TASK_STATUS_CODE_PAUSE -> {
                        viewModel.showContinueDialog(Bundle().apply {
                            putBoolean(BaseConstant.OFFLINE_IS_BATCH, false)
                            putInt(BaseConstant.OFFLINE_AD_CODE, viewModel.getCurrentCityAdCode())
                            putIntegerArrayList(BaseConstant.OFFLINE_AD_CODES, null)
                        })
                    }

                    else -> {
                        viewModel.dealListDownLoad(viewModel.getCurrentCityAdCode())
                    }
                }
            }
        }

        //当前城市下载操作
        binding.currentPbLoadSl.setDebouncedOnClickListener {
            viewModel.dealListDownLoad(viewModel.getCurrentCityAdCode())
        }
        ViewClickEffectUtils.addClickScale(binding.currentPbLoadSl, CLICKED_SCALE_95)

        //基础包卡片点击
        binding.baseDataCl.setDebouncedOnClickListener {
            val baseCountry = mapDataBusiness.getAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET, AreaType.AREA_TYPE_COUNTRY)
            val baseCityDownLoadItem = mapDataBusiness.getCityDownLoadItem(DownLoadMode.DOWNLOAD_MODE_NET, baseCountry?.get(0) ?: 0)
            when (baseCityDownLoadItem?.taskState) {
                TASK_STATUS_CODE_SUCCESS -> {
                    viewModel.showDeleteDialog(Bundle().apply {
                        putBoolean(BaseConstant.OFFLINE_IS_BATCH, false)
                        putString(BaseConstant.OFFLINE_CITY_NAME, "基础功能包")
                        putInt(BaseConstant.OFFLINE_AD_CODE, baseCountry?.get(0) ?: 0)
                        putIntegerArrayList(BaseConstant.OFFLINE_AD_CODES, null)
                    })
                }

                TASK_STATUS_CODE_READY -> {
                    if (baseCityDownLoadItem.bUpdate) {
                        viewModel.showUpdateDialog(Bundle().apply {
                            putBoolean(BaseConstant.OFFLINE_IS_BATCH, false)
                            putInt(BaseConstant.OFFLINE_AD_CODE, baseCountry?.get(0) ?: 0)
                            putIntegerArrayList(BaseConstant.OFFLINE_AD_CODES, null)
                        })
                    } else {
                        showBaseDownLoadDialog() //提示下载基础包
                    }
                }

                TASK_STATUS_CODE_DOING, TASK_STATUS_CODE_WAITING -> {
                    viewModel.showPauseAskDialog(Bundle().apply {
                        putBoolean(BaseConstant.OFFLINE_IS_BATCH, false)
                        putInt(BaseConstant.OFFLINE_AD_CODE, baseCountry?.get(0) ?: 0)
                        putIntegerArrayList(BaseConstant.OFFLINE_AD_CODES, null)
                    })
                }

                TASK_STATUS_CODE_PAUSE -> {
                    viewModel.showContinueDialog(Bundle().apply {
                        putBoolean(BaseConstant.OFFLINE_IS_BATCH, false)
                        putInt(BaseConstant.OFFLINE_AD_CODE, baseCountry?.get(0) ?: 0)
                        putIntegerArrayList(BaseConstant.OFFLINE_AD_CODES, null)
                    })
                }

                else -> {
                    showBaseDownLoadDialog() //提示下载基础包
                }
            }
        }

        //基础包下载操作
        binding.baseProgressTxtSl.setDebouncedOnClickListener {
            showBaseDownLoadDialog() //提示下载基础包
        }
        ViewClickEffectUtils.addClickScale(binding.baseProgressTxtSl, CLICKED_SCALE_95)

        //附近推荐全部下载暂停
        binding.sivNearbyDownload.setDebouncedOnClickListener {
            viewModel.downLoadNearCity(BaseConstant.OFFLINE_STATE_T0_DOWNLOAD) //附近城市全部下载或者暂停
        }
        ViewClickEffectUtils.addClickScale(binding.sivNearbyDownload, CLICKED_SCALE_93)

        //跳转附近城市推荐界面
        binding.nearbyRecommendAll.setDebouncedOnClickListener {
            findNavController().navigate(R.id.to_nearbyCityFragment)
        }
        binding.sivRecommendMore.setDebouncedOnClickListener {
            findNavController().navigate(R.id.to_nearbyCityFragment)
        }
        ViewClickEffectUtils.addClickScale(binding.sivRecommendMore, CLICKED_SCALE_90)

        //更新数据
        viewModel.updateAllData.unPeek().observe(viewLifecycleOwner) {
            refreshALLOfflineData() //实时更新离线数据，刷新布局
        }

        //更新下载状态
        viewModel.updateOperated.unPeek().observe(viewLifecycleOwner) {
            Timber.i("updateOperated")
            refreshALLOfflineData() //实时更新离线数据，刷新布局
        }

        //更新下载Percent数据
        viewModel.updatePercent.unPeek().observe(viewLifecycleOwner) {
            refreshALLOfflineData() //实时更新离线数据，刷新布局
        }

        //实时更新城市列表数据
        viewModel.upDateCityListView.observe(viewLifecycleOwner) {
            mapDataCityListAdapter?.updateData(viewModel.getGroupList())
        }

        //全部城市列表Adapter操作
        mapDataCityListAdapter?.setDownLoadClickListener(object : MapDataCityListAdapter.OnMapDataItemClickListener {
            override fun onGroupClick(groupPosition: Int) {
                val defaultDis =
                    if (viewModel.showViewLoadManager.value == true) resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_548)
                    else resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_451)
                if (groupPosition == 0) {
                    binding.mainLayout.smoothScrollTo(0, defaultDis)
                } else {
                    lifecycleScope.launch {
                        delay(200)
                        val adapterPosition: Int = ExpandableUtils.getChildAdapterPosition(mapDataCityListAdapter!!, groupPosition)
                        binding.mainLayout.smoothScrollTo(
                            0,
                            adapterPosition * resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_125) + defaultDis
                        )
                    }
                }
            }

            override fun onAllTaskOperateClick(arCode: Int, operateType: Int) {
                onAllTaskOperate(arCode, operateType)
            }

            override fun onStartClick(groupPosition: Int, childPositon: Int, arcode: Int) {
                onAdapterStartClick(groupPosition, childPositon, arcode)
            }

            override fun onItemClick(groupPosition: Int, childPositon: Int, arCode: Int) {
                onAdapterItemClick(groupPosition, childPositon, arCode)
            }
        })

        //日夜模式监听
        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            view?.run { skyBoxBusiness.updateView(this, true) }

            mapDataCityListAdapter?.run { notifyDataSetChanged() }
            searchDataAdapter?.run { notifyDataSetChanged() }
            binding.searchBox.setNight(it == true)
        }

        //显示toast
        viewModel.showToast.unPeek().observe(viewLifecycleOwner) {
            toastUtil.showToast(it)
        }

        //空间不足提示
        viewModel.enough.unPeek().observe(viewLifecycleOwner) {
            toastUtil.showToast("可用存储空间不足，无法继续下载，请空间清理完成后，手动点击继续下载")
        }

        //弹窗提示是否用流量下载
        viewModel.toDownloadDialog.unPeek().observe(viewLifecycleOwner) { arCodes ->
            dismissCustomDialog()
            showFlowDialog = CustomDialogFragment.builder().setTitle("流量提醒").setContent("当前为非Wi-Fi网络，下载离线数据将产生流量费用，是否继续下载")
                .doubleButton("继续下载", getString(com.desaysv.psmap.base.R.string.sv_common_cancel)).setOnClickListener {
                    if (it) {
                        viewModel.downLoadConfirmFlow(arCodes, OperationType.OPERATION_TYPE_START)
                    }
                }.apply {
                    show(this@OfflineMapFragment.childFragmentManager, "showFlowDialog")
                }
        }

        //显示删除对话框
        viewModel.showDeleteDialog.unPeek().observe(viewLifecycleOwner) { bundle ->
            showDeleteDialog(bundle)
        }

        //显示暂停对话框
        viewModel.showPauseAskDialog.unPeek().observe(viewLifecycleOwner) { bundle ->
            showPauseAskDialog(bundle)
        }

        //显示继续对话框
        viewModel.showContinueDialog.unPeek().observe(viewLifecycleOwner) { bundle ->
            showContinueDialog(bundle)
        }

        //更新对话框
        viewModel.showUpdateDialog.unPeek().observe(viewLifecycleOwner) { bundle ->
            showUpdateDialog(bundle)
        }

        //解压时取消暂停对话框
        viewModel.tipsEnterUnzip.unPeek().observe(viewLifecycleOwner) { arCode -> tipsEnterUnzip(arCode) }
    }

    fun onAllTaskOperate(arCode: Int, operateType: Int) {
        val provinceInfo = mapDataBusiness.getProvinceInfo(arCode)
        val adCodeDiyLst = ArrayList<Int>()
        for (cityItemInfo in provinceInfo!!.cityInfoList) {
            if (viewModel.getCurrentCityAdCode() == cityItemInfo.cityAdcode) {
                adCodeDiyLst.add(0, viewModel.getCurrentCityAdCode())
            } else {
                adCodeDiyLst.add(cityItemInfo.cityAdcode)
            }
        }
        if (!netWorkManager.isNetworkConnected()) {
            toastUtil.showToast(com.autosdk.R.string.record_no_error)
        } else {
            viewModel.downLoadClick(adCodeDiyLst, operateType)
        }
    }

    fun onAdapterStartClick(groupPosition: Int, childPositon: Int, arcode: Int) {
        Timber.d(" onAdapterStartClick ")
        if (childPositon == -100) { //省份一栏操作
            val cityAdCode = ArrayList<Int>()
            val cityItemInfoList: ArrayList<CityItemInfo> =
                if (viewModel.showSearch.value == true) viewModel.mSearchList[groupPosition].cityItemInfos else viewModel.getGroupList()[groupPosition].cityItemInfos
            for (i in cityItemInfoList.indices) {
                cityAdCode.add(cityItemInfoList[i].cityAdcode)
            }
            when (arcode) {
                BaseConstant.OFFLINE_STATE_UPDATE, BaseConstant.OFFLINE_STATE_T0_DOWNLOAD, BaseConstant.OFFLINE_STATE_PAUSE -> {
                    mapDataBusiness.startAllTask(DownLoadMode.DOWNLOAD_MODE_NET, cityAdCode)
                }

                BaseConstant.OFFLINE_STATE_DOWNLOAD -> {
                    mapDataBusiness.pauseAllTask(DownLoadMode.DOWNLOAD_MODE_NET, cityAdCode)
                }

                else -> {
                    toastUtil.showToast("该省份已全部下载")
                }
            }
        } else {
            viewModel.dealListDownLoad(arcode)
            Timber.d(" onStartClick other")
        }
    }

    fun onAdapterItemClick(groupPosition: Int, childPositon: Int, arCode: Int) {
        val cityDownLoadItem = mapDataBusiness.getCityDownLoadItem(DownLoadMode.DOWNLOAD_MODE_NET, arCode) ?: return
        if (cityDownLoadItem.taskState == TASK_STATUS_CODE_SUCCESS) {
            val group = if (viewModel.showSearch.value == true) viewModel.mSearchList[groupPosition] else viewModel.getGroupList()[groupPosition]
            val cityName = if (group.cityItemInfos.isNotEmpty()) group.cityItemInfos[childPositon].cityName else group.cityItemInfos[0].cityName
            viewModel.showDeleteDialog(Bundle().apply {
                putBoolean(BaseConstant.OFFLINE_IS_BATCH, false)
                putString(BaseConstant.OFFLINE_CITY_NAME, cityName)
                putInt(BaseConstant.OFFLINE_AD_CODE, arCode)
                putIntegerArrayList(BaseConstant.OFFLINE_AD_CODES, null)
            })
        } else if (cityDownLoadItem.taskState == TASK_STATUS_CODE_READY) {
            if (cityDownLoadItem.bUpdate) {
                viewModel.showUpdateDialog(Bundle().apply {
                    putBoolean(BaseConstant.OFFLINE_IS_BATCH, false)
                    putInt(BaseConstant.OFFLINE_AD_CODE, arCode)
                    putIntegerArrayList(BaseConstant.OFFLINE_AD_CODES, null)
                })
            } else {
                viewModel.dealListDownLoad(arCode)
            }
        } else if (cityDownLoadItem.taskState == TASK_STATUS_CODE_DOING || cityDownLoadItem.taskState == TASK_STATUS_CODE_WAITING) {
            viewModel.showPauseAskDialog(Bundle().apply {
                putBoolean(BaseConstant.OFFLINE_IS_BATCH, false)
                putInt(BaseConstant.OFFLINE_AD_CODE, arCode)
                putIntegerArrayList(BaseConstant.OFFLINE_AD_CODES, null)
            })
        } else if (cityDownLoadItem.taskState == TASK_STATUS_CODE_PAUSE) {
            viewModel.showContinueDialog(Bundle().apply {
                putBoolean(BaseConstant.OFFLINE_IS_BATCH, false)
                putInt(BaseConstant.OFFLINE_AD_CODE, arCode)
                putIntegerArrayList(BaseConstant.OFFLINE_AD_CODES, null)
            })
        } else {
            viewModel.dealListDownLoad(arCode)
        }
    }

    //实时更新离线数据，刷新布局
    private fun refreshALLOfflineData() {
        viewModel.refreshOfflineData() //刷新离线数据，更新UI
    }

    //离线地图城市列表布局Adapter
    private fun operateMapDataCityListAdapter() {
        mapDataCityListAdapter = MapDataCityListAdapter(mapDataBusiness, viewModel.getGroupList()).also { binding.elvDataList.adapter = it }
        mapDataCityListAdapter!!.collapseAllGroup()
    }

    //离线地图搜索Adapter
    private fun operateSearchDataAdapter() {
        searchDataAdapter = MapDataCityListAdapter(mapDataBusiness, arrayListOf()).also { binding.elvSearchList.adapter = it }
        searchDataAdapter!!.collapseAllGroup()
    }

    //删除对话框
    private fun showDeleteDialog(bundle: Bundle) {//isBatch是否批量
        val isBatch = bundle.getBoolean(BaseConstant.OFFLINE_IS_BATCH, false)
        val cityName = bundle.getString(BaseConstant.OFFLINE_CITY_NAME, "")
        val adCode = bundle.getInt(BaseConstant.OFFLINE_AD_CODE, 0)
        val adCodes = bundle.getIntegerArrayList(BaseConstant.OFFLINE_AD_CODES)
        var content = "" //弹框content
        var confirm = getString(R.string.sv_common_app_confirm)
        var cityAdCode = ArrayList<Int>()
        if (isBatch) {
            cityAdCode = adCodes ?: arrayListOf()
            content = "删除" + cityName + "城市数据？"
        } else {
            if ("全省地图" == cityName) {
                val provinceInfo = mapDataBusiness.getProvinceInfo(adCode)
                if (provinceInfo != null) {
                    content = "删除" + provinceInfo.provName + "全省地图" + "地图数据吗？"
                    for (cityItemInfo in provinceInfo.cityInfoList) {
                        cityAdCode.add(cityItemInfo.cityAdcode)
                    }
                }
            } else {
                cityAdCode.add(adCode)
                val baseCountry = mapDataBusiness.getAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET, AreaType.AREA_TYPE_COUNTRY)
                if (adCode == baseCountry?.get(0)) {//基础包删除
                    content = "删除基础功能包数据后，将不\n能离线跨城导航"
                    confirm = getString(com.desaysv.psmap.base.R.string.sv_common_confirm)
                } else {
                    content = "删除" + cityName + "城市数据？"
                }
            }
        }
        viewModel.tipsAdCodes = cityAdCode
        dismissCustomOfflineDialog(deleteDialog) //dismiss 删除对话框
        deleteDialog = null
        deleteDialog = OfflineDataDialogFragment.builder().setTitle("").setContent(content)
            .doubleButton(confirm, getString(com.desaysv.psmap.base.R.string.sv_common_cancel)).setOnClickListener {
                if (it == BaseConstant.OFFLINE_DIALOG_STATE_CONFIRM) {
                    viewModel.dataDeleteOperation(cityAdCode)
                }
                viewModel.tipsAdCodes = arrayListOf()
            }.apply {
                show(this@OfflineMapFragment.childFragmentManager, "deleteDialog")
            }
    }

    //暂停对话框
    private fun showPauseAskDialog(bundle: Bundle) {
        val isBatch = bundle.getBoolean(BaseConstant.OFFLINE_IS_BATCH, false)
        val adCode = bundle.getInt(BaseConstant.OFFLINE_AD_CODE, 0)
        val adCodes = bundle.getIntegerArrayList(BaseConstant.OFFLINE_AD_CODES)
        var cityAdCode = ArrayList<Int>()
        if (isBatch) {
            cityAdCode = adCodes ?: arrayListOf()
        } else {
            cityAdCode.add(adCode)
        }
        viewModel.tipsAdCodes = cityAdCode
        dismissCustomOfflineDialog(pauseDialog) //dismiss 暂停对话框
        pauseDialog = null
        pauseDialog = OfflineDataDialogFragment.builder().setTitle("").setContent("暂停下载提醒")
            .doubleButton("暂停下载", "取消下载").setOnClickListener {
                if (it == BaseConstant.OFFLINE_DIALOG_STATE_CANCEL) {//200.取消并关闭dialog 100.确定并关闭dialog 300.关闭dialog
                    viewModel.downLoadClick(cityAdCode, OperationType.OPERATION_TYPE_CANCEL)
                } else if (it == BaseConstant.OFFLINE_DIALOG_STATE_CONFIRM) {
                    viewModel.downLoadClick(cityAdCode, OperationType.OPERATION_TYPE_PAUSE)
                }
                viewModel.tipsAdCodes = arrayListOf()
            }.apply {
                show(this@OfflineMapFragment.childFragmentManager, "pauseDialog")
            }
    }

    //继续对话框
    private fun showContinueDialog(bundle: Bundle) {
        val isBatch = bundle.getBoolean(BaseConstant.OFFLINE_IS_BATCH, false)
        val adCode = bundle.getInt(BaseConstant.OFFLINE_AD_CODE, 0)
        val adCodes = bundle.getIntegerArrayList(BaseConstant.OFFLINE_AD_CODES)
        var cityAdCode = ArrayList<Int>()
        if (isBatch) {
            cityAdCode = adCodes ?: arrayListOf()
        } else {
            cityAdCode.add(adCode)
        }
        viewModel.tipsAdCodes = cityAdCode
        dismissCustomOfflineDialog(continueDialog) //dismiss 继续对话框
        continueDialog = null
        continueDialog = OfflineDataDialogFragment.builder().setTitle("").setContent("继续下载提醒")
            .doubleButton("继续下载", "取消下载").setOnClickListener {
                if (it == BaseConstant.OFFLINE_DIALOG_STATE_CANCEL) {//200.取消并关闭dialog 100.确定并关闭dialog 300.关闭dialog
                    viewModel.downLoadClick(cityAdCode, OperationType.OPERATION_TYPE_CANCEL)
                } else if (it == BaseConstant.OFFLINE_DIALOG_STATE_CONFIRM) {
                    viewModel.downLoadClick(cityAdCode, OperationType.OPERATION_TYPE_START)
                }
                viewModel.tipsAdCodes = arrayListOf()
            }.apply {
                show(this@OfflineMapFragment.childFragmentManager, "continueDialog")
            }
    }

    //提示下载基础包
    private fun showBaseDownLoadDialog() {
        dismissCustomOfflineDialog(baseDownLoadDialog) //dismiss 继续对话框
        baseDownLoadDialog = null
        var baseDownLoad = false
        val baseCountry = mapDataBusiness.getAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET, AreaType.AREA_TYPE_COUNTRY)
        val baseCityDownLoadItem = mapDataBusiness.getCityDownLoadItem(DownLoadMode.DOWNLOAD_MODE_NET, baseCountry?.get(0) ?: 0)
        val cityAdCode = ArrayList<Int>()
        cityAdCode.add(baseCountry?.get(0) ?: 0)
        viewModel.tipsAdCodes = cityAdCode
        if (baseCityDownLoadItem != null) {
            if (baseCityDownLoadItem.taskState != TaskStatusCode.TASK_STATUS_CODE_READY) {
                baseDownLoad = true
            }
        }
        if (!baseDownLoad) {
            baseDownLoadDialog = OfflineDataDialogFragment.builder().setTitle("").setContent("基础功能包为离线跨城导航必备数据，推荐下载")
                .doubleButton("下载", "暂不下载").setOnClickListener {
                    if (it == BaseConstant.OFFLINE_DIALOG_STATE_CANCEL) {//200.取消并关闭dialog 100.确定并关闭dialog 300.关闭dialog
                        viewModel.downLoadClick(cityAdCode, OperationType.OPERATION_TYPE_CANCEL)
                    } else if (it == BaseConstant.OFFLINE_DIALOG_STATE_CONFIRM) {
                        viewModel.downLoadClick(cityAdCode, OperationType.OPERATION_TYPE_START)
                    }
                    viewModel.tipsAdCodes = arrayListOf()
                }.apply {
                    show(this@OfflineMapFragment.childFragmentManager, "baseDownLoadDialog")
                }
        } else {
            viewModel.downLoadBase()
        }
    }

    //更新对话框
    private fun showUpdateDialog(bundle: Bundle) {
        val isBatch = bundle.getBoolean(BaseConstant.OFFLINE_IS_BATCH, false)
        val adCode = bundle.getInt(BaseConstant.OFFLINE_AD_CODE, 0)
        val adCodes = bundle.getIntegerArrayList(BaseConstant.OFFLINE_AD_CODES)
        var cityAdCode = ArrayList<Int>()
        if (isBatch) {
            cityAdCode = adCodes ?: arrayListOf()
        } else {
            cityAdCode.add(adCode)
        }
        viewModel.tipsAdCodes = cityAdCode
        dismissCustomOfflineDialog(updateDialog) //dismiss 继续对话框
        updateDialog = null
        updateDialog = OfflineDataDialogFragment.builder().setTitle("").setContent("更新提醒")
            .doubleButton("更新", "删除数据").setOnClickListener {
                if (it == BaseConstant.OFFLINE_DIALOG_STATE_CANCEL) {//200.取消并关闭dialog 100.确定并关闭dialog 300.关闭dialog
                    showDeleteDialog(bundle)
                } else if (it == BaseConstant.OFFLINE_DIALOG_STATE_CONFIRM) {
                    viewModel.downLoadClick(cityAdCode, OperationType.OPERATION_TYPE_START)
                }
                viewModel.tipsAdCodes = arrayListOf()
            }.apply {
                show(this@OfflineMapFragment.childFragmentManager, "updateDialog")
            }
    }

    //dialog dismiss
    private fun dismissCustomOfflineDialog(dialog: OfflineDataDialogFragment?) {
        dialog?.run {
            if (isAdded || isVisible) {
                dismissAllowingStateLoss()
            }
        }
    }

    //解压时取消暂停对话框
    private fun tipsEnterUnzip(adCode: Int) {
        if (viewModel.tipsAdCodes.size == 1) {
            viewModel.tipsAdCodes.forEach {
                if (TextUtils.equals(it.toString(), adCode.toString())) {
                    dismissCustomOfflineDialog(pauseDialog) //dismiss 暂停对话框
                }
            }
        }
    }

    private fun dismissCustomDialog() {
        showFlowDialog?.run {
            if (isAdded || isVisible) {
                dismissAllowingStateLoss()
            }
        }
        showFlowDialog = null
    }
}