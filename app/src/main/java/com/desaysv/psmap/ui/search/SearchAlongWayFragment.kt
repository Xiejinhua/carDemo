package com.desaysv.psmap.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.autonavi.auto.skin.NightModeGlobal
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.common.POIFactory
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.view.KeyboardUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.bean.MapPointCardData.PoiCardType
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.data.INaviRepository
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.tracking.EventTrackingUtils
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentSearchAlongWayBinding
import com.desaysv.psmap.model.bean.CommandRequestPOICardBean
import com.desaysv.psmap.model.bean.CommandRequestPOICardBean.Type.Companion.POI_CARD_ROUTE_ADD_VIA
import com.desaysv.psmap.model.bean.CommandRequestSearchCategoryBean
import com.desaysv.psmap.model.bean.CommandRequestSearchCategoryBean.Type.Companion.SEARCH_ALONG_WAY
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.SearchCategoryAdapter
import com.desaysv.psmap.ui.adapter.SearchHistoryAdapter
import com.desaysv.psmap.ui.adapter.SearchSuggestionAdapter
import com.desaysv.psmap.ui.dialog.CustomDialogFragment
import com.desaysv.psmap.ui.search.bean.SearchHistoryBean
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 谢锦华
 * @time 2024/1/8
 * @description
 */
@AndroidEntryPoint
class SearchAlongWayFragment : Fragment() {
    private lateinit var binding: FragmentSearchAlongWayBinding
    private lateinit var searchCategoryAdapter: SearchCategoryAdapter
    private lateinit var searchHistoryAdapter: SearchHistoryAdapter
    private lateinit var searchSuggestionAdapter: SearchSuggestionAdapter
    private val viewModel by viewModels<SearchAlongWayModel>()

    @Inject
    lateinit var mRouteRequestController: RouteRequestController

    @Inject
    lateinit var mINaviRepository: INaviRepository

    @Inject
    lateinit var toast: ToastUtil

    private var lastMidpoi: POI? = null

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var iCarInfoProxy: ICarInfoProxy

    private var deleteSearchHistoryDialog: CustomDialogFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.i("onCreateView()")
        binding = FragmentSearchAlongWayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initView()
        initAdapter()
        initData()
        viewModel.setFollowMode(follow = true,bPreview = false)
    }


    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.isNaving = mINaviRepository.isNavigating()
        binding.searchBox.setNight(NightModeGlobal.isNightMode())
        binding.executePendingBindings()
    }

    private fun initView() {

        binding.searchBox.showCityBtn(false)
        binding.searchBox.setHint(getString(R.string.sv_route_search_for_transit_points))
        binding.searchBox.editOnTouchListener { v, event ->
            if (event?.action == MotionEvent.ACTION_UP) {
                requireActivity().window.setLocalFocus(true, true)
                binding.searchBox.hideKeyboard(false)
            } else if (event?.action == MotionEvent.ACTION_DOWN) {
                requireActivity().window.setLocalFocus(true, true)
            }
            false
        }
        //搜索框文字改变时发起预搜索
        binding.searchBox.editAddTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                Timber.d("s.toString() = ${s.toString()}")
                viewModel.onInputKeywordChanged(s.toString().trim())
            }
        })

        //搜索框返回键
        binding.searchBox.backOnClickListener { findNavController().navigateUp() }
        //搜索按钮
        binding.searchBox.searchOnClickListener {
            toSearchAlongWayResultFragment(binding.searchBox.getTextContent())
        }
        binding.searchBox.setOnEditorActionListener { _, actionId, _ ->
            if (actionId >= EditorInfo.IME_ACTION_GO && actionId <= EditorInfo.IME_ACTION_DONE) {
                toSearchAlongWayResultFragment(binding.searchBox.getTextContent())
                true
            } else {
                false
            }
        }

        //清空搜索历史
        binding.clDeleteHistory.setDebouncedOnClickListener {
            clearDialog("确定清空历史路线？") {
                if (it) {
                    if (viewModel.clearSearchHistory()) {
                        toast.showToast("已清空历史记录", com.desaysv.psmap.model.R.drawable.ic_toast_complete_day)
                        viewModel.refreshHistoryData()
                    } else {
                        toast.showToast("清空历史记录失败了")
                    }
                }
            }
        }
        //清空搜索历史
        binding.tvDeleteHistory.setDebouncedOnClickListener {
            clearDialog("确定清空历史路线？") {
                if (it) {
                    if (viewModel.clearSearchHistory()) {
                        toast.showToast("已清空历史记录", com.desaysv.psmap.model.R.drawable.ic_toast_complete_day)
                        viewModel.refreshHistoryData()
                    } else {
                        toast.showToast("清空历史记录失败了")
                    }
                }
            } //清空历史弹窗
        }


        //日夜模式监听
        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            binding.searchBox.setNight(it == true)
            searchCategoryAdapter.notifyDataSetChanged()
            searchHistoryAdapter.notifyDataSetChanged()
            searchSuggestionAdapter.notifyDataSetChanged()
        }
    }

    private fun initAdapter() {
        val gridLayoutManager = GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)
        binding.searchCommonCategoryListview.layoutManager = gridLayoutManager
        binding.searchCommonCategoryListview.addItemDecoration(
            SearchCategoryAdapter.GridSpacingItemDecoration(
                4,
                resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_48),
                resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_30)
            )
        )
        searchCategoryAdapter = SearchCategoryAdapter().also { adapter ->
            adapter.setOnItemClickListener(object : SearchCategoryAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    when (searchCategoryAdapter.data[position].name) {
                        "美食", "维修站", "加油站", "卫生间", "服务区", "充电站" -> {
                            toSearchAlongWayResultFragment(searchCategoryAdapter.data[position].name)

                            EventTrackingUtils.trackEvent(
                                EventTrackingUtils.EventName.OnthewaySearch_Click,
                                mapOf(Pair(EventTrackingUtils.EventValueName.SearchCategory, searchCategoryAdapter.data[position].name))
                            )
                        }

                        "更多" -> {
                            toSearchCategoryFragment()
                        }

                        "地图选点" -> {
                            val commandRequestPOICardBean = CommandRequestPOICardBean.Builder().setType(POI_CARD_ROUTE_ADD_VIA).build()
                            findNavController().navigate(
                                R.id.to_MapPointDataFragment,
                                commandRequestPOICardBean.toBundle()
                            )
                            viewModel.searchPoiCardInfo(
                                PoiCardType.TYPE_CAR_LOC,
                                POIFactory.createPOI("", GeoPoint(viewModel.getLastLocation().longitude, viewModel.getLastLocation().latitude), "")
                            )
                        }

                        "收藏点" -> {
                            findNavController().navigate(R.id.to_homeFavoriteFragment,
                                Bundle().apply { putInt(Biz.TO_FAVORITE_TYPE, BaseConstant.TO_FAVORITE_VIA_TYPE) })

                        }

                        "收到的点" -> {
                            findNavController().navigate(R.id.to_SearchAimPoiPushFragment)

                        }
                    }
                }
            })
        }
        binding.searchCommonCategoryListview.adapter = searchCategoryAdapter

        searchHistoryAdapter = SearchHistoryAdapter().also { adapter ->
            adapter.setOnItemClickListener(object : SearchHistoryAdapter.OnItemClickListener {
                override fun onItemClick(bean: SearchHistoryBean) {
                    addViaPoi(bean.poi)
                }

                override fun onItemLongClick(bean: SearchHistoryBean): Boolean {
                    return false
                }

                override fun onFavorite(position: Int) {}

                override fun onGoThere(poi: POI?) {}

                override fun onHistoryDelete(bean: SearchHistoryBean) {}
            })
        }
        binding.searchHistoryListview.adapter = searchHistoryAdapter

        searchSuggestionAdapter = SearchSuggestionAdapter().also { adapter ->
            adapter.setOnItemClickListener(object : SearchSuggestionAdapter.OnItemClickListener {
                override fun onItemClick(bean: SearchHistoryBean) {
                    addViaPoi(bean.poi)
                }

                override fun onItemRightClick(bean: SearchHistoryBean) {
                    addViaPoi(bean.poi)
                }

                override fun onChildPoiClick(poi: POI?) {

                }
            })

        }
        binding.searchSuggestionListview.adapter = searchSuggestionAdapter

    }

    private fun initData() {
        viewModel.setToast.unPeek().observe(viewLifecycleOwner) {
            it?.let { nonNullString ->
                if (nonNullString.isNotEmpty()) {
                    toast.showToast(nonNullString)
                }
            }
        }
        if (mINaviRepository.isNavigating()) {
            val list = if (iCarInfoProxy.isT1JFL2ICE()) {
                SearchCategoryConstants.ALONG_WAY_IN_NAVIGATING_CATEGORY_LIST.filter { it.name != "充电站" }
            } else {
                SearchCategoryConstants.ALONG_WAY_IN_NAVIGATING_CATEGORY_LIST
            }
            searchCategoryAdapter.updateData(list)
        } else {
            searchCategoryAdapter.updateData(SearchCategoryConstants.ALONG_WAY_CATEGORY_LIST)
        }

        viewModel.historyPoiListLiveData.observe(viewLifecycleOwner) {
            searchHistoryAdapter.updateData(it)
        }

        viewModel.suggestPoiListLiveData.observe(viewLifecycleOwner) {
            searchSuggestionAdapter.updateData(it)
        }

        viewModel.buttonType.observe(viewLifecycleOwner) { buttonType ->
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

        if (viewModel.historyPoiListLiveData.value.isNullOrEmpty()) {
            viewModel.startSync() //同步获取账号信息
            viewModel.refreshHistoryData()
        }


    }

    //清空历史弹窗
    private fun clearDialog(content: String, onClick: (isOk: Boolean) -> Unit) {
        dismissClearSearchHistoryDialog()
        deleteSearchHistoryDialog = CustomDialogFragment.builder().setCloseButton(true).setContent(content)
            .setOnClickListener(onClick)
            .apply {
                show(this@SearchAlongWayFragment.childFragmentManager, "customDialog")
            }
    }

    private fun dismissClearSearchHistoryDialog() {
        deleteSearchHistoryDialog?.run {
            if (isAdded || isVisible) {
                dismissAllowingStateLoss()
            }
        }
        deleteSearchHistoryDialog = null
    }

    override fun onDestroyView() {
        Timber.i("onDestroyView()")
        super.onDestroyView()
    }

    override fun onPause() {
        super.onPause()
        Timber.i("onPause()")
        KeyboardUtil.hideKeyboard(view)
    }

    override fun onStop() {
        super.onStop()
        Timber.i("onStop()")
        dismissClearSearchHistoryDialog()
    }

    override fun onDestroy() {
        Timber.i("onDestroy()")
        super.onDestroy()
    }

    fun addViaPoi(poi: POI?) {
        lastMidpoi = poi
        if (mINaviRepository.isRealNavi()) {
            poi?.let {
                val carRouteResult = mRouteRequestController.carRouteResult
                carRouteResult?.let {
                    var endPoi = it.toPOI
                    val viaList = arrayListOf<POI>()
                    if (it.hasMidPos()) {
                        if (checkViaPoi(poi, it.midPois)) {
                            viaList.addAll(it.midPois)
                        } else {
                            return@let
                        }
                    }
                    viaList.add(poi)
//                val commandBean = CommandRequestRouteNaviBean.Builder().buildMisPoi(endPoi, viaList)
//                findNavController().navigate(R.id.action_searchAlongWayFragment_to_naviFragment, commandBean.toBundle())
                    viewModel.addWayPoint(poi)
                }

            }
        } else {
            poi?.let {
                Timber.i("btSet newPoi = $poi")
                val carRouteResult = mRouteRequestController.carRouteResult
                carRouteResult?.let {
                    val startPoi = it.fromPOI
                    var endPoi = it.toPOI
                    val viaList = arrayListOf<POI>()
                    if (it.hasMidPos()) {
                        if (checkViaPoi(poi, it.midPois)) {
                            viaList.addAll(it.midPois)
                        } else {
                            return@let
                        }
                    }
                    viaList.add(poi)
//                val commandBean = CommandRequestRouteNaviBean.Builder().build(startPoi, endPoi, viaList)
                    viewModel.addWayPointPlan(poi)
                }
            }
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

    /**
     * 跳转沿途搜结果界面
     */
    private fun toSearchAlongWayResultFragment(keyword: String = "") {
        if (!TextUtils.isEmpty(keyword)) {
            KeyboardUtil.hideKeyboard(view)
            val commandBean =
                CommandRequestSearchCategoryBean.Builder().setKeyword(keyword).setType(SEARCH_ALONG_WAY)
                    .build()
            findNavController().navigate(R.id.to_searchAlongWayResultFragment, commandBean.toBundle())
        } else {
            toast.showToast(R.string.sv_search_please_enter_keyword)
        }
    }

    /**
     * 跳转更多界面
     */
    private fun toSearchCategoryFragment() {
        KeyboardUtil.hideKeyboard(view)
        EventTrackingUtils.trackEvent(
            EventTrackingUtils.EventName.SurroundSearch_Click,
            Pair(EventTrackingUtils.EventValueName.SearchTime, System.currentTimeMillis())
        )
        //使用当前位置进行周边搜索
        val currentPoi: POI = POIFactory.createPOI("我的位置", GeoPoint(viewModel.getLastLocation().longitude, viewModel.getLastLocation().latitude));
        val commandBean = currentPoi.let {
            CommandRequestSearchCategoryBean.Builder()
                .setPoi(it)
                .setType(SEARCH_ALONG_WAY)
                .build()
        }
        findNavController().navigate(R.id.to_searchCategoryFragment, commandBean.toBundle())

    }
}