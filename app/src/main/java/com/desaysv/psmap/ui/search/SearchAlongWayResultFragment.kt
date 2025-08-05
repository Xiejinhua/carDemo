package com.desaysv.psmap.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.autonavi.gbl.layer.SearchAlongWayLayerItem
import com.autonavi.gbl.layer.model.BizSearchType
import com.autonavi.gbl.map.layer.BaseLayer
import com.autonavi.gbl.map.layer.LayerItem
import com.autonavi.gbl.map.layer.model.ClickViewIdInfo
import com.autonavi.gbl.map.layer.observer.ILayerClickObserver
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.bussiness.widget.route.utils.AutoRouteUtil
import com.autosdk.common.AutoStatus
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.AutoStatusAdapter
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.data.INaviRepository
import com.desaysv.psmap.base.tracking.EventTrackingUtils
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentSearchAlongWayResultBinding
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.bean.CommandRequestSearchCategoryBean
import com.desaysv.psmap.model.bean.MapCommandType
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.ui.adapter.SearchAlongWayResultAdapter
import com.desaysv.psmap.ui.search.bean.SearchResultBean
import com.desaysv.psmap.ui.search.view.SearchSinglePoiView
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 张楠
 * @time 2024/2/27
 * @description
 */

@AndroidEntryPoint
class SearchAlongWayResultFragment : Fragment() {
    private lateinit var binding: FragmentSearchAlongWayResultBinding
    private lateinit var searchAlongWayResultAdapter: SearchAlongWayResultAdapter
    private val viewModel by viewModels<SearchAlongWayResultModel>()

    @Inject
    lateinit var mRouteRequestController: RouteRequestController

    @Inject
    lateinit var mINaviRepository: INaviRepository

    @Inject
    lateinit var mNaviBusiness: NaviBusiness

    @Inject
    lateinit var toast: ToastUtil

    private var lastMidpoi: POI? = null

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchAlongWayResultBinding.inflate(inflater, container, false)
        val commandRequestSearchCategoryBean = requireArguments().getParcelable<CommandRequestSearchCategoryBean>(Biz.KEY_BIZ_SEARCH_CATEGORY_LIST)
        viewModel.setSearchBean(commandRequestSearchCategoryBean)
        Timber.i("commandRequestSearchCategoryBean = ${commandRequestSearchCategoryBean?.keyword}")
        AutoStatusAdapter.sendStatus(AutoStatus.SEARCH_RESULT_FRAGMENT_START)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initView()
        initAdapter()
        initData()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        viewModel.setSearchLayer(hidden)
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
    }

    private fun initView() {
        binding.searchBar.backOnClickListener {
            findNavController().navigateUp()
        }
        binding.searchBar.setTitle(viewModel.getSearchBean()?.keyword)
        //搜索按钮
        binding.searchBar.hideSearchBtn(false)
        binding.searchBar.searchContentOnClickListener {
            EventTrackingUtils.trackEvent(
                EventTrackingUtils.EventName.OnthewaySearch_Click,
                mapOf(Pair(EventTrackingUtils.EventValueName.SearchTime, System.currentTimeMillis()))
            )
            findNavController().navigate(R.id.to_searchAlongWayFragment)
        }

        viewModel.setSearchLayer(false)
        binding.singlePoiView.setOnSinglePoiListener(object : SearchSinglePoiView.onSinglePoiListener {
            override fun onCloseClick() {
                viewModel.showSinglePoiView(null)
                AutoStatusAdapter.sendStatus(AutoStatus.SEARCH_RESULT_SINGLE_POI_FRAGMENT_EXIT)
            }

            override fun onPhoneCall(phone: String?) {}

            override fun onFavoriteClick(searchResultBean: SearchResultBean) {}

            override fun onSearchAround(bean: SearchResultBean) {}

            override fun onGoThere(searchResultBean: SearchResultBean) {
                searchResultBean.poi?.let { poi ->
                    val commandBean = CommandRequestRouteNaviBean.Builder().build(poi)
                    viewModel.planRoute(commandBean)
                }
            }

            override fun onAddVia(searchResultBean: SearchResultBean) {
                addViaPoi(searchResultBean.poi)
            }

            override fun onAddHome(searchResultBean: SearchResultBean) {

            }

            override fun onChildPoiItemClick(childPosition: Int, childPoi: POI?) {
                TODO("Not yet implemented")
            }
        })
        binding.searchResultListview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    // 判断是否处于列表顶部
                    val isAtTop = layoutManager.findFirstVisibleItemPosition() == 0
                    viewModel.isListTop.postValue(isAtTop)

                    // 判断是否处于列表底部
                    val isAtBottom = layoutManager.findLastVisibleItemPosition() == layoutManager.itemCount - 1
                    viewModel.isListBottom.postValue(isAtBottom)
                }
            }
        })
    }

    private fun initAdapter() {
        searchAlongWayResultAdapter = SearchAlongWayResultAdapter().also { adapter ->
            adapter.setOnSearchResultChildListener(object : SearchAlongWayResultAdapter.OnItemClickListener {
                override fun onItemClick(position: Int, resultBean: SearchResultBean) {
                    if (position != -1) {
                        resultBean.poi?.let {
                            viewModel.setParentPoiSelect(it, true, position)
                        }
                        lifecycleScope.launch(Dispatchers.IO) {
                            viewModel.getDeepInfo(resultBean)
                            if (viewModel.singleResult.value != null) {
                                viewModel.showSinglePoiView(resultBean)
                            }
                        }
                        viewModel.protocolAlongWaySearchResultListChangeExecute(choice = position)
                        EventTrackingUtils.trackEvent(
                            EventTrackingUtils.EventName.Search_Click,
                            mapOf(
                                Pair(EventTrackingUtils.EventValueName.SearchType, if (viewModel.getSearchBean()?.isVoiceSearch == true) 0 else 1)
                            )
                        )
                        viewModel.clickEnable.postValue(true)
                    } else {
                        lifecycleScope.launch(Dispatchers.IO) {
                            viewModel.showSinglePoiView(resultBean)
                        }
                    }
                }

                override fun onItemRightClick(bean: SearchResultBean) {
                    addViaPoi(bean.poi)
                }

            })
        }
        binding.searchResultListview.adapter = searchAlongWayResultAdapter
    }

    private fun initData() {
        val commandRequestSearchBean = viewModel.getSearchBean()
        commandRequestSearchBean?.keyword?.let { searchCategory ->
            when (commandRequestSearchBean.type) {
                CommandRequestSearchCategoryBean.Type.SEARCH_ALONG_WAY -> {
                    viewModel.startAlongWaySearch(searchCategory)
                }
            }
        }
        viewModel.searchResultListLiveData.observe(viewLifecycleOwner) { list ->
            list?.forEach { bean ->
                bean.type = 1
                bean.distance = viewModel.getDistance(bean.poi)
                bean.poi?.let { poi ->
                    bean.isFavorite = viewModel.isFavorited(poi)
                }
            }
            searchAlongWayResultAdapter.reSetSelect()
            searchAlongWayResultAdapter.updateData(list)
            viewModel.showSearchResultListData()
            viewModel.setSearchLayerClickObserver(layerClickObserver, false)
            viewModel.showPreview()
            viewModel.protocolAlongWaySearchResultListChangeExecute()
        }
        viewModel.setToast.unPeek().observe(viewLifecycleOwner) {
            it?.let { nonNullString ->
                if (nonNullString.isNotEmpty()) {
                    toast.showToast(nonNullString)
                }
            }
        }
        viewModel.singleResult.observe(viewLifecycleOwner) {
            it?.let {
                AutoStatusAdapter.sendStatus(AutoStatus.SEARCH_RESULT_SINGLE_POI_FRAGMENT_START)
                binding.singlePoiView.updateData(it)
                viewModel.updateMapCenter(it.poi)
            }
            if (it == null) {
                viewModel.clickEnable.postValue(false)
            } else {
                viewModel.clickEnable.postValue(true)
            }
        }


        //日夜模式监听
        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            searchAlongWayResultAdapter.notifyDataSetChanged()
        }
        viewModel.searchListOperaLiveData.unPeek().observe(viewLifecycleOwner) { searchListOperaData ->
            Timber.i("searchListOperaData = ${gson.toJson(searchListOperaData)}")
            searchListOperaData?.let {
                searchListOperaData.operateType?.let { operateType ->
                    searchListOperaData.poiIndex?.let { poiIndex ->
                        when (operateType) { //操作类型：0查看poi；1去这⾥（路线规划）
                            0 -> {
                                if (searchAlongWayResultAdapter.getSelection() != poiIndex) {
                                    searchAlongWayResultAdapter.setSelection(poiIndex)
                                }
                            }

                            1 -> {
                                if (searchListOperaData.poiIndex in searchAlongWayResultAdapter.data.indices) {
                                    searchAlongWayResultAdapter.data[poiIndex].poi?.let {
                                        val commandBean = CommandRequestRouteNaviBean.Builder().build(it)
                                        viewModel.planRoute(commandBean)
                                    }
                                }
                            }
                        }
                    }
                }
//                searchListOperaData.pageTurning?.let { pageTurning ->
//                    when (pageTurning) {
//                        0 -> { //上一页
//                            viewModel.onListRefresh()
//                        }
//
//                        1 -> { //下一页
//                            viewModel.onListLoadMore()
//                        }
//                    }
//                }
                searchListOperaData.screenTurning?.let { screenTurning ->
                    when (screenTurning) {
                        0 -> { //上翻屏
                            binding.searchResultListview.smoothScrollBy(0, -binding.searchResultListview.height)
                        }

                        1 -> { //下翻屏
                            binding.searchResultListview.smoothScrollBy(0, binding.searchResultListview.height)
                        }

                    }
                }
            }
        }
        viewModel.isListTop.observe(viewLifecycleOwner) {
            viewModel.protocolAlongWaySearchResultListChangeExecute()
        }
        viewModel.isListBottom.observe(viewLifecycleOwner) {
            viewModel.protocolAlongWaySearchResultListChangeExecute()
        }

        viewModel.mapCommand.unPeek().observe(viewLifecycleOwner) { mapCommandBean ->
            when (mapCommandBean.mapCommandType) {
                MapCommandType.PosRank -> {
                    Timber.i("PosRank rank = ${mapCommandBean.pair} ")
                    val offset = mapCommandBean.pair?.second!!
                    if (searchAlongWayResultAdapter.data.size < offset) {
                        viewModel.notifyPosRankCommandResult(false, "当前只有${searchAlongWayResultAdapter.data.size}个搜索结果，请换个试试")
                    } else {
                        searchAlongWayResultAdapter.data[offset - 1].poi?.let { poi ->
                            if (AutoRouteUtil.isSamePoi(poi, mRouteRequestController.carRouteResult.toPOI)
                                || AutoRouteUtil.isSamePoi(poi, mRouteRequestController.carRouteResult.fromPOI)
                            ) {
                                //该途经点与起点/终点重复，请换个试试
                                viewModel.notifyPosRankCommandResult(false, "该途经点与起点或终点重复，请换个试试")
                            } else {
                                if (addViaPoi(poi)) {
                                    //已将{[location]POI}设置为途经点
                                    viewModel.notifyPosRankCommandResult(true, "已将${poi.name}设置为途经点")
                                } else {
                                    //该途经点已添加，请换个试试
                                    viewModel.notifyPosRankCommandResult(false, "该途经点已添加，请换个试试")
                                }
                            }
                        }
                    }
                }

                MapCommandType.PageRank -> {
                    Timber.i("PageRank rank = ${mapCommandBean.pair} ")
                    when (mapCommandBean.pair?.first) {
                        //下一页
                        "++" -> {
                            binding.searchResultListview.smoothScrollBy(0, binding.searchResultListview.height)
                            viewModel.notifyPageRankCommandResult(true, "已翻到下一页")
                        }

                        //上一页
                        "--" -> {
                            binding.searchResultListview.smoothScrollBy(0, -binding.searchResultListview.height)
                            viewModel.notifyPageRankCommandResult(true, "已翻到上一页")
                        }
                    }
                }

                MapCommandType.Confirm -> {
                    Timber.i("MapCommandType.Confirm")
                    //在只有一个搜索结果时候处理
                    if (viewModel.isSingleResult.value == true) {
                        viewModel.singleResult.value?.poi?.let { poi ->
                            if (mRouteRequestController.carRouteResult?.toPOI?.id == poi.id || mRouteRequestController.carRouteResult?.fromPOI?.id == poi
                                    .id
                            ) {
                                //该途经点与起点/终点重复，请换个试试
                                viewModel.notifyConfirmCommandResult(false, "该途经点与起点/终点重复，请换个试试")
                            } else {
                                if (addViaPoi(poi)) {
                                    //已将{[location]POI}设置为途经点
                                    viewModel.notifyConfirmCommandResult(true, "已将${poi.name}设置为途经点")
                                } else {
                                    //该途经点已添加，请换个试试
                                    viewModel.notifyConfirmCommandResult(false, "该途经点已添加，请换个试试")
                                }
                            }
                        }
                    } else {
                        viewModel.backToNavi()
                    }
                }

                else -> {}
            }
        }
        viewModel.setFollowMode(follow = false, bPreview = true)

        viewModel.setAutoZoom(false)
    }

    override fun onDestroyView() {
        Timber.i("onDestroyView()")
        super.onDestroyView()
        viewModel.setSearchLayerClickObserver(layerClickObserver, true)
        viewModel.exitPreview()
        viewModel.clearAllSearchItems();
        AutoStatusAdapter.sendStatus(AutoStatus.SEARCH_RESULT_FRAGMENT_EXIT)
    }

    override fun onDestroy() {
        Timber.i("onDestroy()")
        super.onDestroy()
    }

    private val layerClickObserver: ILayerClickObserver = object : ILayerClickObserver {
        override fun onBeforeNotifyClick(baseLayer: BaseLayer?, layerItem: LayerItem?, clickViewIdInfo: ClickViewIdInfo?) {}
        override fun onNotifyClick(layer: BaseLayer?, layerItem: LayerItem?, clickViewIds: ClickViewIdInfo?) {

            when (layerItem?.businessType) {
                BizSearchType.BizSearchTypePoiAlongRoute -> {
                    val item = layerItem as SearchAlongWayLayerItem
                    val mIndex = item.id.toInt()
                    Timber.i("onNotifyClick BizSearchTypePoiAlongRoute mIndex=$mIndex")
                    lifecycleScope.launch(Dispatchers.Main) {
                        val isSingleResult = viewModel.isSingleResult.value
                        isSingleResult?.let {
                            //单个结果时
                            if (it) {

                            } else {
                                val curSelection: Int = searchAlongWayResultAdapter.getSelection()
                                if (curSelection != mIndex) {
                                    searchAlongWayResultAdapter.setSelection(mIndex)
                                    binding.searchResultListview.scrollToPosition(mIndex)
                                } else {
                                    searchAlongWayResultAdapter.setSelection(mIndex)
                                }
                            }
                        }
                    }
                }

                else -> {}
            }


        }

        override fun onAfterNotifyClick(baseLayer: BaseLayer?, layerItem: LayerItem?, clickViewIdInfo: ClickViewIdInfo?) {}
    }


    fun addViaPoi(poi: POI?): Boolean {
        lastMidpoi = poi
        if (mINaviRepository.isRealNavi()) {
            return poi?.let {
                val carRouteResult = mRouteRequestController.carRouteResult
                val endPoi = carRouteResult.toPOI
                val viaList = arrayListOf<POI>()
                if (carRouteResult?.hasMidPos() == true) {
                    if (checkViaPoi(poi, carRouteResult.midPois)) {
                        viaList.addAll(carRouteResult.midPois)
                    } else {
                        return false
                    }
                }
                viaList.add(poi)
//                val commandBean = CommandRequestRouteNaviBean.Builder().buildMisPoi(endPoi, viaList)
//                findNavController().navigate(R.id.action_searchAlongWayResultFragment_to_naviFragment, commandBean.toBundle())
                viewModel.addWayPoint(poi)
                return true
            } ?: false
        } else {
            return poi?.let {
                Timber.i("btSet newPoi = $poi")
                val carRouteResult = mRouteRequestController.carRouteResult
                val startPoi = carRouteResult.fromPOI
                val endPoi = carRouteResult.toPOI
                val viaList = arrayListOf<POI>()
                if (carRouteResult?.hasMidPos() == true) {
                    if (checkViaPoi(poi, carRouteResult.midPois)) {
                        viaList.addAll(carRouteResult.midPois)
                    } else {
                        return false
                    }
                }
                viaList.add(poi)
//                val commandBean = CommandRequestRouteNaviBean.Builder().build(startPoi, endPoi, viaList)
                viewModel.addWayPointPlan(poi)
                return true
            } ?: false
        }
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