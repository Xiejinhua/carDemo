package com.desaysv.psmap.ui.search

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.autonavi.gbl.layer.SearchChildLayerItem
import com.autonavi.gbl.layer.SearchParentLayerItem
import com.autonavi.gbl.layer.model.BizSearchType
import com.autonavi.gbl.map.layer.BaseLayer
import com.autonavi.gbl.map.layer.LayerItem
import com.autonavi.gbl.map.layer.model.ClickViewIdInfo
import com.autonavi.gbl.map.layer.observer.ILayerClickObserver
import com.autonavi.gbl.search.model.SearchClassifyInfo
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.common.utils.GsonManager
import com.autosdk.common.AutoStatus
import com.autosdk.common.utils.ResUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.AutoStatusAdapter
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.tracking.EventTrackingUtils
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentSearchResultBinding
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.bean.CommandRequestSearchBean
import com.desaysv.psmap.model.bean.CommandRequestSearchCategoryBean
import com.desaysv.psmap.model.bean.MapCommandType
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.SearchResultAdapter
import com.desaysv.psmap.ui.dialog.CustomDialogFragment
import com.desaysv.psmap.ui.search.bean.SearchResultBean
import com.desaysv.psmap.ui.search.view.ClassifyFragment
import com.desaysv.psmap.ui.search.view.SearchSinglePoiView
import com.desaysv.psmap.utils.LoadingUtil
import com.google.gson.Gson
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
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
class SearchResultFragment : Fragment() {
    private lateinit var binding: FragmentSearchResultBinding
    private lateinit var searchResultAdapter: SearchResultAdapter
    private val viewModel by viewModels<SearchResultModel>()

    @Inject
    lateinit var toast: ToastUtil

    @Inject
    lateinit var loadingUtil: LoadingUtil

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var gson: Gson

    private lateinit var commandRequestSearchBean: CommandRequestSearchBean

    private val classifyDialog by lazy { ClassifyFragment.builder() }

    private var favoriteDialog: CustomDialogFragment? = null

    private var lastCheckId = 0
    private var lastTargetX = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        requireArguments().getParcelable<CommandRequestSearchBean>(Biz.KEY_BIZ_SEARCH_REQUEST)?.let {
            commandRequestSearchBean = it
            viewModel.setSearchBean(commandRequestSearchBean)
            Timber.i("commandRequestSearchBean = $commandRequestSearchBean")
            Timber.i("commandRequestSearchBean.type = ${commandRequestSearchBean.type}")
        }
        Timber.i("commandRequestSearchBean = ${commandRequestSearchBean.keyword}")
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
        Timber.i("onHiddenChanged()")
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
    }

    private fun initView() {
        binding.searchBar.searchContentOnClickListener {
            findNavController().navigateUp()
        }
        binding.back.setDebouncedOnClickListener {
            findNavController().navigateUp()
        }
        binding.searchBar.backOnClickListener {
            findNavController().navigateUp()
        }
        binding.searchBar.setTitle(
            if (commandRequestSearchBean.type == CommandRequestSearchBean.Type.SEARCH_CUSTOM_POI)
                getString(com.desaysv.psmap.base.R.string.sv_custom_poi_category_5)
            else commandRequestSearchBean.keyword
        )

        binding.singlePoiView.setOnSinglePoiListener(object : SearchSinglePoiView.onSinglePoiListener {
            override fun onCloseClick() {
                if (viewModel.isSingleResult.value!!) {
                    findNavController().navigateUp()
                    return
                }
                if ((!viewModel.searchResultListLiveData.value.isNullOrEmpty() && viewModel.searchResultListLiveData.value!!.size > 1) || viewModel.currentPage.value!! != 1) {
                    viewModel.showSinglePoiView(null)
                } else {
                    findNavController().navigateUp()
                }
                AutoStatusAdapter.sendStatus(AutoStatus.SEARCH_RESULT_SINGLE_POI_FRAGMENT_EXIT)
            }

            override fun onPhoneCall(phone: String?) {
                phone?.let {
                    var num: String = phone
                    val index: Int = phone.indexOf(";")
                    if (index > -1) {
                        val phoneList: Array<String> = phone.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        if (phoneList.size > 0) {
                            num = phoneList[0].trim { it <= ' ' }
                        }
                    }
                    viewModel.onPhoneCall(num)
                }
            }

            override fun onFavoriteClick(searchResultBean: SearchResultBean) {
                searchResultBean.poi?.let { poi ->
                    if (searchResultBean.isFavorite) {//当前该poi点已收藏，应取消收藏
                        if (viewModel.delFavorite(poi)) {//取消收藏成功
                            toast.showToast("已取消收藏", com.desaysv.psmap.model.R.drawable.ic_toast_complete_day)
                            searchResultBean.isFavorite = false
                        } else {//取消收藏失败
                            searchResultBean.isFavorite = true
                            toast.showToast(R.string.sv_search_cancel_favorite_error)
                        }
                    } else {//当前该poi点未收藏，应收藏
                        if (viewModel.addFavorite(poi)) {//收藏成功
                            toast.showToast("已收藏", com.desaysv.psmap.model.R.drawable.ic_toast_complete_day)
                            searchResultBean.isFavorite = true
                        } else {//收藏失败
                            searchResultBean.isFavorite = false
                            toast.showToast(R.string.sv_search_favorite_error)
                        }
                    }
                    binding.singlePoiView.updateData(searchResultBean)
                }
            }

            override fun onSearchAround(bean: SearchResultBean) {
                bean.poi?.let { poi ->
                    EventTrackingUtils.trackEvent(
                        EventTrackingUtils.EventName.SurroundSearch_Click,
                        Pair(EventTrackingUtils.EventValueName.SearchTime, System.currentTimeMillis())
                    )
                    val commandBean = poi.let {
                        CommandRequestSearchCategoryBean.Builder()
                            .setPoi(it)
                            .setCity(commandRequestSearchBean.city)
                            .setType(CommandRequestSearchCategoryBean.Type.Companion.SEARCH_AROUND)
                            .build()
                    }
                    NavHostFragment.findNavController(this@SearchResultFragment)
                        .navigate(R.id.action_searchResultFragment_to_searchCategoryFragment, commandBean.toBundle())
                }
            }

            override fun onGoThere(searchResultBean: SearchResultBean) {
                searchResultBean.poi?.let { poi ->
                    val commandBean = CommandRequestRouteNaviBean.Builder().build(poi)
                    viewModel.planRoute(commandBean)
                }

            }

            override fun onAddVia(searchResultBean: SearchResultBean) {
            }

            override fun onAddHome(searchResultBean: SearchResultBean) {
                searchResultBean.poi?.let { poi ->
                    doCollection(commandRequestSearchBean.type, poi)
                }
            }

            override fun onChildPoiItemClick(childPosition: Int, childPoi: POI?) {
                if (childPosition != -1) {
                    childPoi?.let { childPoi ->
                        viewModel.setChildPoiSelect(childPoi, true)
                    }
                } else {
                    childPoi?.let {
                        viewModel.setChildPoiSelect(childPoi, false)
                        //子poi取消选中时，中心回到父poi上
                        viewModel.updateMapCenter(binding.singlePoiView.getSearchResultBean().poi)
                    }
                }
            }
        })

        binding.tvNoCityDataRetry.setDebouncedOnClickListener {
            viewModel.doSearch(viewModel.getSearchBean())
        }

        binding.refreshLayout.setOnRefreshLoadMoreListener(
            object : OnRefreshLoadMoreListener {
                override fun onRefresh(refreshLayout: RefreshLayout) {
                    viewModel.onListRefresh();
                }

                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    viewModel.onListLoadMore()
                }
            })

        binding.layoutTab.check(if (viewModel.getChargeKeyword() == "加油站") R.id.rb_gasstation else R.id.rb_charging)
        binding.layoutTab.setOnCheckedChangeListener { group, checkedId ->
            lifecycleScope.launch {
                viewModel.tabSelect.postValue(if (checkedId == R.id.rb_gasstation) 0 else 1)
                val checkedButton: RadioButton = group.findViewById(checkedId)
                // 计算指示条应该移动到的位置
                val targetX: Int = checkedButton.left + (checkedButton.width - binding.indicator.width) / 2
                Timber.i("setting layoutTab checkedId:$checkedId targetX:$targetX lastTargetX:$lastTargetX")
                var duration = 200 // 相邻的 RadioButton
                // 创建平移动画
                if (targetX == 0 && checkedId != R.id.rb_gasstation) {
                    binding.indicator.animate()
                        .x(lastTargetX.toFloat())
                        .setDuration(0)
                        .setInterpolator(FastOutSlowInInterpolator())
                        .start()
                } else {
                    binding.indicator.animate()
                        .x(targetX.toFloat())
                        .setDuration(duration.toLong())
                        .setInterpolator(FastOutSlowInInterpolator())
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                lastTargetX = targetX
                                lastCheckId = checkedId // 更新上一个选中的 ID
                                when (checkedId) {
                                    R.id.rb_gasstation -> {
                                        viewModel.doSearch(viewModel.getSearchBean(), chargeKeyword = "加油站")
                                    }

                                    R.id.rb_charging -> {
                                        viewModel.doSearch(viewModel.getSearchBean(), chargeKeyword = "充电站")
                                    }
                                }
                            }
                        })
                        .start()
                }
            }
        }

        viewModel.tabSelect.postValue(
            if (viewModel.getChargeKeyword() == "加油站") 0 //加油站
            else 1 //充电站
        )
        if (viewModel.getChargeKeyword() == "加油站") {

        } else {
            binding.indicator.animate()
                .x(240f)
                .setDuration(0)
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
            lastTargetX = 240
            lastCheckId = R.id.rb_charging
        }


        viewModel.setSearchLayer(false)
        classifyDialog.setOnClassifyClickListener(object : ClassifyFragment.OnClassifyClickListener {
            override fun onClassifyClick(value: String, retainState: String, searchClassifyInfo: SearchClassifyInfo) {
                viewModel.onClassifySearch(value, retainState, searchClassifyInfo)
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
        searchResultAdapter = SearchResultAdapter().also { adapter ->
            adapter.setOnItemClickListener(object : SearchResultAdapter.OnSearchResultItemClickListener {
                override fun onItemClick(position: Int, resultBean: SearchResultBean) {
                    if (position != -1) {
                        resultBean.poi?.let {
                            viewModel.setParentPoiSelect(it, true)
                        }
                        lifecycleScope.launch(Dispatchers.IO) {
                            viewModel.getDeepInfo(resultBean)
                            if (viewModel.singleResult.value != null) {
                                viewModel.showSinglePoiView(resultBean)
                            }
                        }
                        viewModel.protocolSearchResultListChangeExecute(choice = position)
                        EventTrackingUtils.trackEvent(
                            EventTrackingUtils.EventName.Search_Click,
                            Pair(EventTrackingUtils.EventValueName.SearchType, if (commandRequestSearchBean.isVoiceSearch) 0 else 1)
                        )
                    } else {
                        lifecycleScope.launch(Dispatchers.IO) {
                            viewModel.showSinglePoiView(resultBean)
                        }
                    }
                }

                override fun onFavorite(position: Int) {
                    val poi = searchResultAdapter.getChildSelectionPoi() ?: searchResultAdapter.getSelectionPoi()
                    poi?.let {
                        if (viewModel.isFavorited(it)) {//当前该poi点已收藏，应取消收藏
                            if (viewModel.delFavorite(it)) {//取消收藏成功
                                toast.showToast("已取消收藏", com.desaysv.psmap.model.R.drawable.ic_toast_complete_day)
                                searchResultAdapter.data[position].isFavorite = false
                            } else {//取消收藏失败
                                searchResultAdapter.data[position].isFavorite = true
                                toast.showToast(R.string.sv_search_cancel_favorite_error)
                            }
                        } else {//当前该poi点未收藏，应收藏
                            if (viewModel.addFavorite(poi)) {//收藏成功
                                toast.showToast("已收藏", com.desaysv.psmap.model.R.drawable.ic_toast_complete_day)
                                searchResultAdapter.data[position].isFavorite = true
                            } else {//收藏失败
                                searchResultAdapter.data[position].isFavorite = false
                                toast.showToast(R.string.sv_search_favorite_error)
                            }
                            searchResultAdapter.data[position].isFavorite = viewModel.addFavorite(poi)
                        }
                        searchResultAdapter.notifyItemChanged(position)
                    }
                }

                override fun onGoThere(position: Int) {
                    val item = searchResultAdapter.data[position].poi
                    val childSelectionPoi = searchResultAdapter.getChildSelectionPoi()
                    val poi: POI?
                    if (childSelectionPoi == null) {
                        poi = item
                    } else {
                        poi = childSelectionPoi
                        if (!poi.name.contains("(")) {
                            poi.name = item?.name + String.format(
                                ResUtil.getString(R.string.sv_search_child_name),
                                childSelectionPoi.name
                            )
                        }
                    }
                    if (commandRequestSearchBean.type == CommandRequestSearchBean.Type.SEARCH_KEYWORD_COLLECT || commandRequestSearchBean.type in
                        CommandRequestSearchBean.Type.SEARCH_HOME..CommandRequestSearchBean.Type.SEARCH_TEAM_DESTINATION
                    ) {
                        poi?.let {
                            doCollection(commandRequestSearchBean.type, poi)
                        }
                    } else {
                        val commandBean = poi?.let { CommandRequestRouteNaviBean.Builder().build(it) }
                        viewModel.planRoute(commandBean)
                        viewModel.protocolSearchResultListChangeExecute(isPlanRoute = true, choice = position)
                    }
                }

                override fun toAround(poi: POI?) {
                    EventTrackingUtils.trackEvent(
                        EventTrackingUtils.EventName.SurroundSearch_Click,
                        Pair(EventTrackingUtils.EventValueName.SearchTime, System.currentTimeMillis())
                    )
                    val commandBean = poi?.let {
                        CommandRequestSearchCategoryBean.Builder()
                            .setPoi(it)
                            .setCity(commandRequestSearchBean.city)
                            .setType(CommandRequestSearchCategoryBean.Type.Companion.SEARCH_AROUND)
                            .build()
                    }
                    NavHostFragment.findNavController(this@SearchResultFragment)
                        .navigate(R.id.action_searchResultFragment_to_searchCategoryFragment, commandBean?.toBundle())
                }

                override fun onChildPoiItemClick(position: Int, childPosition: Int, childPoi: POI?) {
                    Timber.i("onChildPoiItemClick() called with: position = $position, childPosition = $childPosition, childPoi = $childPoi")
                    if (childPosition != -1) {
                        childPoi?.let { childPoi ->
                            viewModel.setChildPoiSelect(childPoi, true)
                            searchResultAdapter.data[position].isFavorite = viewModel.isFavorited(childPoi)
                        }
                    } else {
                        childPoi?.let {
                            viewModel.setChildPoiSelect(childPoi, false)
                            //子poi取消选中时，中心回到父poi上
                            viewModel.updateMapCenter(adapter.data[position].poi)
                            adapter.data[position].poi?.let { parentPoi ->
                                searchResultAdapter.data[position].isFavorite = viewModel.isFavorited(parentPoi)
                            }
                        }
                    }
                    //去除，防止item闪烁
//                    searchResultAdapter.notifyItemChanged(position)
                }

            })
        }
        binding.searchResultListview.adapter = searchResultAdapter
    }

    private fun initData() {
        val commandRequestSearchBean = viewModel.getSearchBean()
        //存在结果不需要重新搜索，新传进来的才需要重新规划
        if (viewModel.searchResultListLiveData.value.isNullOrEmpty()) {
            viewModel.doSearch(commandRequestSearchBean)
        }
        viewModel.searchResultListLiveData.observe(viewLifecycleOwner) {
            for (bean in it) {
                bean.poi?.let { poi ->
                    val type = commandRequestSearchBean?.type.let {
                        if (it in 3 until 6 || it == 9) {
                            it
                        } else {
                            0
                        }
                    } ?: 0
                    bean.type = type
                    bean.isFavorite = viewModel.isFavorited(poi)
                }
            }
            searchResultAdapter.reSetSelect()
            searchResultAdapter.updateData(it)
            viewModel.showSearchResultListData()
            viewModel.setSearchLayerClickObserver(layerClickObserver, false)
            viewModel.showPreview()
            lifecycleScope.launch {
                delay(200)
                if (!it.isNullOrEmpty() && viewModel.singleResult.value == null) {
                    searchResultAdapter.reSetSelect()
                    binding.searchResultListview.scrollToPosition(0)
                    searchResultAdapter.setSelection(0)
                }
                delay(200)
                val layoutManager = binding.searchResultListview.layoutManager as LinearLayoutManager
                // 判断是否处于列表顶部
                val isAtTop = layoutManager.findFirstVisibleItemPosition() == 0
                viewModel.isListTop.postValue(isAtTop)

                // 判断是否处于列表底部
                val isAtBottom = layoutManager.findLastVisibleItemPosition() == layoutManager.itemCount - 1
                Timber.i("layoutManager.findLastVisibleItemPosition() = ${layoutManager.findLastVisibleItemPosition()}, layoutManager.itemCount = ${layoutManager.itemCount}")
                viewModel.isListBottom.postValue(isAtBottom)
            }
        }
        viewModel.singleResult.observe(viewLifecycleOwner) {
            Timber.i("singleResult = ${gson.toJson(it)}")

            it?.let { bean ->
                bean.poi?.let { poi ->
                    val type = commandRequestSearchBean?.type.let {
                        it
                        if (it in 3 until 6) {
                            it
                        } else {
                            0
                        }

                    } ?: 0
                    bean.type = type
                    bean.isFavorite = viewModel.isFavorited(poi)
                }
                AutoStatusAdapter.sendStatus(AutoStatus.SEARCH_RESULT_SINGLE_POI_FRAGMENT_START)
                binding.singlePoiView.updateData(bean)
                viewModel.updateMapCenter(bean.poi)
                viewModel.addSearchHistory(poi = bean.poi)
            }
            if (viewModel.isSingleResult.value!!) {
                viewModel.showSearchSingleResultData()
            }
        }
        viewModel.isSearchSuccess.observe(viewLifecycleOwner) {
            if (viewModel.isRefresh == 0) {
                binding.refreshLayout.finishRefresh()
            } else if (viewModel.isRefresh == 1) {
                binding.refreshLayout.finishLoadMore()
            }
        }
        viewModel.totalPage.observe(viewLifecycleOwner) { totalPage ->
            binding.refreshLayout.setTotalPage(totalPage)
            viewModel.protocolSearchResultListChangeExecute()
        }
        viewModel.currentPage.observe(viewLifecycleOwner) { currentPage ->
            binding.refreshLayout.setCurrentPage(currentPage)
        }
        viewModel.isListTop.observe(viewLifecycleOwner) {
            Timber.i("isListTop = $it")
            viewModel.protocolSearchResultListChangeExecute()
        }
        viewModel.isListBottom.observe(viewLifecycleOwner) {
            Timber.i("isListBottom = $it")
            viewModel.protocolSearchResultListChangeExecute()
        }
        viewModel.isLoadingLiveData.unPeek().observe(viewLifecycleOwner) { isLoading ->
            Timber.i("isLoading = $isLoading")
            if (viewModel.isFirst.value == true) {
                // 只在第一次加载完成时更新 isFirst 标志
                if (!isLoading) {
                    viewModel.isFirst.postValue(false)
                }
                when {
                    isLoading -> loadingUtil.showLoading("正在搜索中", onItemClick = { findNavController().navigateUp() })
                    else -> loadingUtil.cancelLoading()
                }

            } else {
                if (viewModel.isListLoadOrRefreash.value == true) {
                    when {
                        isLoading ->loadingUtil.showLoading("正在搜索...", onItemClick = { viewModel.abortAll() })

                        else -> {
                            loadingUtil.cancelLoading()
                            viewModel.isListLoadOrRefreash.value = false
                        }
                    }

                } else {
                    when {
                        isLoading -> loadingUtil.showLoading("正在搜索中", onItemClick = { viewModel.abortAll() })
                        else -> loadingUtil.cancelLoading()
                    }
                }
            }
        }

        skyBoxBusiness.themeChange().unPeek().observe(viewLifecycleOwner) {
            binding.refreshLayout.setNight(it == true)
            searchResultAdapter.notifyDataSetChanged()
        }

        //筛选
        viewModel.classifyInfo.observe(viewLifecycleOwner) { searchClassifyInfo ->
            updateFilterData(searchClassifyInfo)
        }

        //筛选
        viewModel.showClassifyInfo.observe(viewLifecycleOwner) { showClassifyInfo ->
            Timber.i("showClassifyInfo = $showClassifyInfo")
        }
        binding.layoutFilter0.root.setOnClickListener { view ->
            viewModel.classifyInfo.value?.let { classifyInfo ->
                classifyDialog.showClassifyDialog(childFragmentManager, classifyInfo, 0)
            }
        }
        binding.layoutFilter1.root.setOnClickListener { view ->
            viewModel.classifyInfo.value?.let { classifyInfo ->
                classifyDialog.showClassifyDialog(childFragmentManager, classifyInfo, 1)
            }
        }
        binding.layoutFilter2.root.setOnClickListener { view ->
            viewModel.classifyInfo.value?.let { classifyInfo ->
                classifyDialog.showClassifyDialog(childFragmentManager, classifyInfo, 2)
            }
        }
        binding.layoutFilter3.root.setOnClickListener { view ->
            viewModel.classifyInfo.value?.let { classifyInfo ->
                classifyDialog.showClassifyDialog(childFragmentManager, classifyInfo, 3)
            }
        }
        viewModel.searchListOperaLiveData.unPeek().observe(viewLifecycleOwner) { searchListOperaData ->
            Timber.i("searchListOperaData = ${gson.toJson(searchListOperaData)}")
            searchListOperaData?.let {
                searchListOperaData.operateType?.let { operateType ->
                    searchListOperaData.poiIndex?.let { poiIndex ->
                        when (operateType) { //操作类型：0查看poi；1去这⾥（路线规划）
                            0 -> {
                                if (searchResultAdapter.getSelection() != poiIndex) {
                                    searchResultAdapter.setSelection(poiIndex)
                                }
                            }

                            1 -> {
                                if (searchListOperaData.poiIndex in searchResultAdapter.data.indices) {
                                    searchResultAdapter.data[poiIndex].poi?.let {
                                        val commandBean = CommandRequestRouteNaviBean.Builder().build(it)
                                        viewModel.planRoute(commandBean)
                                    }
                                }
                            }
                        }
                    }
                }
                searchListOperaData.pageTurning?.let { pageTurning ->
                    when (pageTurning) {
                        0 -> { //上一页
                            viewModel.onListRefresh()
                        }

                        1 -> { //下一页
                            viewModel.onListLoadMore()
                        }
                    }
                }
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
        viewModel.setFollowMode(follow = false, bPreview = true)
        viewModel.setAutoZoom(false)

        viewModel.mapCommand.unPeek().observe(viewLifecycleOwner) { mapCommandBean ->
            when (mapCommandBean.mapCommandType) {
                MapCommandType.PosRank -> {
                    Timber.i("PosRank rank = ${mapCommandBean.pair} ")
                    val offset = mapCommandBean.pair?.second!!
                    if (searchResultAdapter.data.size < offset) {
                        viewModel.notifyPosRankCommandResult(
                            MapCommandType.PosRank,
                            false,
                            "当前只有${searchResultAdapter.data.size}个搜索结果，请换个试试"
                        )
                    } else {
                        searchResultAdapter.data[offset - 1].poi?.let {
                            iflytekVoicePOIChooseConfirm(it)
                        }
                    }
                }

                MapCommandType.PageRank -> {
                    Timber.i("PageRank rank = ${mapCommandBean.pair} ")
                    if (mapCommandBean.pair == null)
                        return@observe
                    if (viewModel.totalPage.value!! <= 1) {
                        viewModel.notifyPosRankCommandResult(
                            MapCommandType.PageRank,
                            false,
                            "当前只有一页搜索结果哦"
                        )
                        return@observe
                    }
                    when (mapCommandBean.pair?.first) {
                        //下一页
                        "++" -> {
                            if (viewModel.currentPage.value!! < viewModel.totalPage.value!!) {
                                viewModel.onListLoadMore()
                            } else {
                                viewModel.notifyPosRankCommandResult(
                                    MapCommandType.PageRank,
                                    true,
                                    "当前已经是最后一页了"
                                )
                            }
                        }

                        //上一页
                        "--" -> {
                            if (viewModel.currentPage.value!! > 1) {
                                viewModel.onListRefresh()
                            } else {
                                viewModel.notifyPosRankCommandResult(
                                    MapCommandType.PageRank,
                                    true,
                                    "当前已经是第一页了"
                                )
                            }
                        }

                        "+" -> {
                            //第一页
                            when (mapCommandBean.pair?.second) {
                                1 -> {
                                    if (viewModel.currentPage.value!! == 1) {
                                        viewModel.notifyPosRankCommandResult(
                                            MapCommandType.PageRank,
                                            true,
                                            "当前已经是第一页了"
                                        )
                                    } else {
                                        viewModel.doSearchByPage(1)
                                    }
                                }

                                in 2..viewModel.totalPage.value!! -> {
                                    viewModel.doSearchByPage(mapCommandBean.pair?.second!!)
                                }

                                else -> {
                                    viewModel.notifyPosRankCommandResult(
                                        MapCommandType.PageRank,
                                        true,
                                        "还不支持此操作哦"
                                    )
                                }
                            }
                        }

                        "-" -> {
                            when (mapCommandBean.pair?.second) {
                                //最后一页
                                1 -> {
                                    if (viewModel.currentPage.value!! == viewModel.totalPage.value!!) {
                                        viewModel.notifyPosRankCommandResult(
                                            MapCommandType.PageRank,
                                            true,
                                            "当前已经是最后一页了"
                                        )
                                    } else {
                                        viewModel.doSearchByPage(viewModel.totalPage.value!!)
                                    }
                                }

                                in 2 until viewModel.totalPage.value!! -> {
                                    viewModel.doSearchByPage(viewModel.totalPage.value!! - mapCommandBean.pair?.second!! + 1)
                                }

                                else -> {
                                    viewModel.notifyPosRankCommandResult(
                                        MapCommandType.PageRank,
                                        true,
                                        "还不支持此操作哦"
                                    )
                                }
                            }
                        }
                    }
                }

                MapCommandType.Confirm -> {
                    Timber.i("Confirm")
                    if (viewModel.isSingleResult.value == true) {
                        viewModel.singleResult.value?.poi?.let {
                            iflytekVoicePOIChooseConfirm(it)
                        }
                    }
                }

                else -> {}
            }
        }
    }

    private fun iflytekVoicePOIChooseConfirm(poi: POI) {
        Timber.i("iflytekVoicePOIChooseConfirm type = ${commandRequestSearchBean.type}")
        when (commandRequestSearchBean.type) {
            CommandRequestSearchBean.Type.SEARCH_COMPANY -> {
                Timber.i("PosRank addCompany")
                val isSuccess = viewModel.addCompany(poi, true)
                if (isSuccess) {
                    toast.showToast(
                        R.string.sv_search_add_company_success,
                        com.desaysv.psmap.model.R.drawable.ic_toast_complete_day
                    )
                    findNavController().navigateUp()
                    viewModel.notifyPosRankCommandResult(
                        MapCommandType.PosRank,
                        true,
                        "已将公司设置为${poi.name}"
                    )
                } else {
                    toast.showToast(R.string.sv_search_add_company_error)
                    viewModel.notifyPosRankCommandResult(
                        MapCommandType.PosRank,
                        false,
                        "设置公司失败"
                    )
                }
            }

            CommandRequestSearchBean.Type.SEARCH_HOME -> {
                Timber.i("PosRank addHome")
                val isSuccess = viewModel.addHome(poi, true)
                if (isSuccess) {
                    toast.showToast(R.string.sv_search_add_home_success, com.desaysv.psmap.model.R.drawable.ic_toast_complete_day)
                    findNavController().navigateUp()
                    viewModel.notifyPosRankCommandResult(
                        MapCommandType.PosRank,
                        true,
                        "已将家设置为${poi.name}"
                    )
                } else {
                    toast.showToast(R.string.sv_search_add_home_error)
                    viewModel.notifyPosRankCommandResult(
                        MapCommandType.PosRank,
                        false,
                        "设置家失败"
                    )
                }
            }

            CommandRequestSearchBean.Type.SEARCH_KEYWORD_COLLECT -> {
                val isSuccess = viewModel.addFavorite(poi)
                if (isSuccess) {
                    toast.showToast("收藏成功", com.desaysv.psmap.model.R.drawable.ic_toast_complete_day)
                    findNavController().navigateUp()
                    viewModel.notifyPosRankCommandResult(
                        MapCommandType.PosRank,
                        true,
                        "已为您收藏至收藏夹"
                    )
                } else {
                    toast.showToast("收藏失败")
                    viewModel.notifyPosRankCommandResult(
                        MapCommandType.PosRank,
                        false,
                        "收藏失败"
                    )
                }
            }

            else -> {
                Timber.i("PosRank planRoute")
                viewModel.notifyPosRankCommandResult(
                    MapCommandType.PosRank,
                    true,
                    poi
                )
                /*val commandBean = CommandRequestRouteNaviBean.Builder().build(poi)
                viewModel.planRoute(commandBean)*/
            }
        }
    }


    override fun onDestroyView() {
        Timber.i("onDestroyView()")
        super.onDestroyView()
        viewModel.setSearchLayerClickObserver(layerClickObserver, true)
        viewModel.exitPreview()
        viewModel.clearAllSearchItems()
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
                BizSearchType.BizSearchTypePoiParentPoint -> {
                    val searchParentLayerItem = layerItem as SearchParentLayerItem
                    val mIndex = searchParentLayerItem.mIndex
                    Timber.i("onNotifyClick BizSearchTypePoiParentPoint mIndex=$mIndex")
                    lifecycleScope.launch(Dispatchers.Main) {
                        val isSingleResult = viewModel.isSingleResult.value
                        isSingleResult?.let {
                            //单个结果时
                            if (it) {

                            } else {
                                val curSelection: Int = searchResultAdapter.getSelection()
                                if (curSelection != mIndex) {
                                    searchResultAdapter.setSelection(mIndex)
                                    binding.searchResultListview.scrollToPosition(mIndex)
                                }
                            }
                        }
                    }
                }

                BizSearchType.BizSearchTypePoiChildPoint -> {
                    val searchChildLayerItem = layerItem as SearchChildLayerItem
                    val clickChildPoiId = searchChildLayerItem.id
                    Timber.i("onNotifyClick   searchChildLayerItem.id = ${searchChildLayerItem.id}")
                    lifecycleScope.launch(Dispatchers.Main) {
                        //当前用户在图层中点击中的子poi点就是当前列表中选中的子poi点，取消焦点态
                        var isSelected = false
                        searchResultAdapter.getChildSelectionPoi()?.let { childPoi ->
                            if (clickChildPoiId.equals(childPoi.id)) {
                                isSelected = true
                                viewModel.setChildPoiSelect(childPoi, false)
                                //子poi取消选中时，中心回到父poi上
                                viewModel.updateMapCenter(searchResultAdapter.getSelectionPoi())
                            }
                        }
                        //更新子poi的列表选中状态UI
                        searchResultAdapter.getSelectionPoi()?.childPois?.let { childPois ->
                            for (index in childPois.indices) {
                                if (clickChildPoiId.equals(childPois[index].id)) {
                                    searchResultAdapter.setChildSelection(searchResultAdapter.getSelection(), index)
                                    //未选中时
                                    if (!isSelected) {
                                        viewModel.setChildPoiSelect(childPois[index], true)
                                    }
                                }
                            }
                        }
                        viewModel.singleResult.value?.let {
                            for (index in binding.singlePoiView.searchResultChildAdapter.data.indices) {
                                if (clickChildPoiId.equals(binding.singlePoiView.searchResultChildAdapter.data[index].id)) {
                                    if (binding.singlePoiView.searchResultChildAdapter.selectPosition() == index) {
                                        binding.singlePoiView.searchResultChildAdapter.setSelectPosition(-1)
                                        //子poi取消选中时，中心回到父poi上
                                        viewModel.updateMapCenter(binding.singlePoiView.getSearchResultBean().poi)
                                    } else {
                                        binding.singlePoiView.searchResultChildAdapter.setSelectPosition(index)
                                    }
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

    private fun doCollection(@CommandRequestSearchBean.Type type: Int, poi: POI) {
        val isFavorite = viewModel.isFavorited(poi)
        when (type) {
            CommandRequestSearchBean.Type.SEARCH_HOME -> {
                if (isFavorite) {
                    dismissCustomDialog()
                    favoriteDialog = CustomDialogFragment.builder().setCloseButton(true).setContent(getString(R.string.sv_search_has_favorite) + "家")
                        .setOnClickListener {
                            if (it) {
                                val isSuccess = viewModel.addHome(poi, true);
                                if (isSuccess) {
                                    toast.showToast(R.string.sv_search_add_home_success, com.desaysv.psmap.model.R.drawable.ic_toast_complete_day)
                                    findNavController().popBackStack(R.id.searchAddHomeFragment, true)
                                } else {
                                    toast.showToast(R.string.sv_search_add_home_error)
                                }
                            }
                        }.apply {
                            show(this@SearchResultFragment.childFragmentManager, "favoriteDialog")
                        }
                } else {
                    val isSuccess = viewModel.addHome(poi, false);
                    if (isSuccess) {
                        toast.showToast(R.string.sv_search_add_home_success, com.desaysv.psmap.model.R.drawable.ic_toast_complete_day)
                        findNavController().popBackStack(R.id.searchAddHomeFragment, true)
                    } else {
                        toast.showToast(R.string.sv_search_add_home_error)
                    }
                }
            }

            CommandRequestSearchBean.Type.SEARCH_COMPANY -> {
                if (isFavorite) {
                    dismissCustomDialog()
                    favoriteDialog =
                        CustomDialogFragment.builder().setCloseButton(true).setContent(getString(R.string.sv_search_has_favorite) + "公司")
                            .setOnClickListener {
                                if (it) {
                                    val isSuccess = viewModel.addCompany(poi, true);
                                    if (isSuccess) {
                                        toast.showToast(
                                            R.string.sv_search_add_company_success,
                                            com.desaysv.psmap.model.R.drawable.ic_toast_complete_day
                                        )
                                        findNavController().popBackStack(R.id.searchAddHomeFragment, true)
                                    } else {
                                        toast.showToast(R.string.sv_search_add_company_error)
                                    }
                                }
                            }
                            .apply {
                                show(this@SearchResultFragment.childFragmentManager, "favoriteDialog")
                            }
                } else {
                    val isSuccess = viewModel.addCompany(poi, false);
                    if (isSuccess) {
                        toast.showToast(R.string.sv_search_add_company_success, com.desaysv.psmap.model.R.drawable.ic_toast_complete_day)
                        findNavController().popBackStack(R.id.searchAddHomeFragment, true)
                    } else {
                        toast.showToast(R.string.sv_search_add_company_error)
                    }
                }
            }

            CommandRequestSearchBean.Type.SEARCH_TEAM_DESTINATION -> {
                viewModel.addDestination(poi)
                findNavController().popBackStack(R.id.searchAddTeamDestinationFragment, true)
            }

            //语音打开手动点击
            CommandRequestSearchBean.Type.SEARCH_KEYWORD_COLLECT -> {
                val isSuccess = viewModel.addFavorite(poi)
                if (isSuccess) {
                    toast.showToast("收藏成功", com.desaysv.psmap.model.R.drawable.ic_toast_complete_day)
                    findNavController().navigateUp()
                } else {
                    toast.showToast("收藏失败")
                }
            }

            else -> {}
        }
    }

    private fun dismissCustomDialog() {
        favoriteDialog?.run {
            if (isAdded || isVisible) {
                dismissAllowingStateLoss()
            }
        }
        favoriteDialog = null
    }

    override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
        super.onInflate(context, attrs, savedInstanceState)
    }

    override fun onStop() {
        super.onStop()
        Timber.i("onStop")
        loadingUtil.cancelLoading()
        dismissCustomDialog()
        classifyDialog.dismissAllowingStateLoss()
    }

    private fun updateFilterData(searchClassifyInfo: SearchClassifyInfo?) {
        Timber.i("updateFilterData() called with: searchClassifyInfo = ${GsonManager.getInstance().toJson(searchClassifyInfo)}")
        if (searchClassifyInfo?.classifyItemInfo?.categoryInfoList?.isNotEmpty() == true) {
            for (i in searchClassifyInfo.classifyItemInfo.categoryInfoList.indices) {
                val searchCategoryInfo = searchClassifyInfo.classifyItemInfo.categoryInfoList[i]
                when (i) {
                    0 -> {
                        binding.layoutFilter0.stvText.text = searchCategoryInfo.baseInfo.name
                    }

                    1 -> {
                        binding.layoutFilter1.stvText.text = searchCategoryInfo.baseInfo.name
                    }

                    2 -> {
                        binding.layoutFilter2.stvText.text = searchCategoryInfo.baseInfo.name
                    }

                    3 -> {
                        binding.layoutFilter3.stvText.text = "更多筛选"
                    }
                }
            }
            updateFilterView(searchClassifyInfo)
        }
    }

    private fun updateFilterView(searchClassifyInfo: SearchClassifyInfo?) {
        if (searchClassifyInfo?.classifyItemInfo?.categoryInfoList?.isNotEmpty() == true) {
            Timber.i("updateFilterView() called with: size = ${searchClassifyInfo.classifyItemInfo.categoryInfoList.size}")
            when (searchClassifyInfo.classifyItemInfo.categoryInfoList.size) {
                1 -> {
                    binding.layoutFilter0.root.visibility = View.VISIBLE
                    binding.layoutFilter1.root.visibility = View.GONE
                    binding.layoutFilter2.root.visibility = View.GONE
                    binding.layoutFilter3.root.visibility = View.GONE
                }

                2 -> {
                    binding.layoutFilter0.root.visibility = View.VISIBLE
                    binding.layoutFilter1.root.visibility = View.VISIBLE
                    binding.layoutFilter2.root.visibility = View.GONE
                    binding.layoutFilter3.root.visibility = View.GONE
                }

                3 -> {
                    binding.layoutFilter0.root.visibility = View.VISIBLE
                    binding.layoutFilter1.root.visibility = View.VISIBLE
                    binding.layoutFilter2.root.visibility = View.VISIBLE
                    binding.layoutFilter3.root.visibility = View.GONE
                }

                4 -> {
                    binding.layoutFilter0.root.visibility = View.VISIBLE
                    binding.layoutFilter1.root.visibility = View.VISIBLE
                    binding.layoutFilter2.root.visibility = View.VISIBLE
                    binding.layoutFilter3.root.visibility = View.VISIBLE
                }
            }
        }
    }

}