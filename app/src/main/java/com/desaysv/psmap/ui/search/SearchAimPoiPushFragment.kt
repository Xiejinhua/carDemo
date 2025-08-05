package com.desaysv.psmap.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.autonavi.gbl.layer.SearchAlongWayLayerItem
import com.autonavi.gbl.layer.model.BizSearchType
import com.autonavi.gbl.map.layer.BaseLayer
import com.autonavi.gbl.map.layer.LayerItem
import com.autonavi.gbl.map.layer.model.ClickViewIdInfo
import com.autonavi.gbl.map.layer.observer.ILayerClickObserver
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.view.KeyboardUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.data.INaviRepository
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentSearchAimPoiPushBinding
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.SearchAimPoiPushAdapter
import com.desaysv.psmap.ui.search.bean.SearchResultBean
import com.desaysv.psmap.ui.search.view.SearchSinglePoiView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 张楠
 * @time 2024/2/22
 * @description
 */
@AndroidEntryPoint
class SearchAimPoiPushFragment : Fragment() {
    private lateinit var binding: FragmentSearchAimPoiPushBinding
    private lateinit var searchAimPoiPushAdapter: SearchAimPoiPushAdapter
    private val viewModel by viewModels<SearchAimPoiPushModel>()

    @Inject
    lateinit var mRouteRequestController: RouteRequestController

    @Inject
    lateinit var mINaviRepository: INaviRepository

    @Inject
    lateinit var toast: ToastUtil

    @Inject
    lateinit var settingAccountBusiness: SettingAccountBusiness

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    var isTeamDestination = false //是否添加组队出行目的地界面

    private var lastMidpoi: POI? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.i("onCreateView()")
        binding = FragmentSearchAimPoiPushBinding.inflate(inflater, container, false)
        isTeamDestination = arguments?.getBoolean(Biz.TO_AIM_POI_PUSH_TYPE, false) ?: false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initView()
        initAdapter()
        initData()
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
    }

    private fun initView() {

        binding.searchCategoryBack.setDebouncedOnClickListener {
            findNavController().navigateUp()
        }
        //刷新消息
        settingAccountBusiness.refreshMessage.unPeek().observe(viewLifecycleOwner) {
            if (!viewModel.isLogin()) { //未登录
                Timber.d(" refreshMessage 未登录")
            } else {
                viewModel.initMessageData()
            }
        }
        binding.singlePoiView.setOnSinglePoiListener(object : SearchSinglePoiView.onSinglePoiListener {
            override fun onCloseClick() {
                viewModel.showSinglePoiView(null)
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
                if (isTeamDestination) {
                    searchResultBean.poi?.let { poi ->
                        viewModel.addDestination(poi)
                        findNavController().popBackStack(R.id.searchAddTeamDestinationFragment, true)
                    }
                }
            }

            override fun onChildPoiItemClick(childPosition: Int, childPoi: POI?) {

            }
        })
        ViewClickEffectUtils.addClickScale(binding.searchCategoryBack, CLICKED_SCALE_90)
    }

    private fun initAdapter() {

        searchAimPoiPushAdapter = SearchAimPoiPushAdapter().also { adapter ->
            adapter.setOnSearchResultChildListener(object : SearchAimPoiPushAdapter.OnItemClickListener {
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
                    } else {
                        lifecycleScope.launch(Dispatchers.IO) {
                            viewModel.showSinglePoiView(resultBean)
                        }
                    }
                }

                override fun onItemRightClick(bean: SearchResultBean) {
                    if (isTeamDestination) {
                        bean.poi?.let { poi ->
                            viewModel.addDestination(poi)
                            findNavController().popBackStack(R.id.searchAddTeamDestinationFragment, true)
                        }
                    } else {
                        addViaPoi(bean.poi)
                    }
                }

            })
        }
        binding.messageListview.adapter = searchAimPoiPushAdapter

    }

    private fun initData() {

        //日夜模式监听
        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            searchAimPoiPushAdapter.notifyDataSetChanged()
        }
        viewModel.initMessageData()


        viewModel.singleResult.observe(viewLifecycleOwner) {
            it?.let {
                binding.singlePoiView.updateData(it)
                viewModel.updateMapCenter(it.poi)
            }
        }
        viewModel.searchResultListLiveData.observe(viewLifecycleOwner) { list ->
            Timber.i("searchResultListLiveData.observe = $list")
            list?.forEach { bean ->
                bean.type = if (isTeamDestination) 5 else 1
                bean.poi?.let { poi ->
                    bean.isFavorite = viewModel.isFavorited(poi)
                }
            }
            searchAimPoiPushAdapter.setList(list)
            viewModel.showSearchResultListData()
            viewModel.setSearchLayerClickObserver(layerClickObserver, false)
        }
    }

    override fun onDestroyView() {
        Timber.i("onDestroyView()")
        super.onDestroyView()
        viewModel.setSearchLayerClickObserver(layerClickObserver, true)
        viewModel.clearAllSearchItems();
    }

    override fun onPause() {
        super.onPause()
        Timber.i("onPause()")
        KeyboardUtil.hideKeyboard(view)
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
                val endPoi = carRouteResult.toPOI
                val viaList = arrayListOf<POI>()
                if (carRouteResult?.hasMidPos() == true) {
                    if (checkViaPoi(poi, carRouteResult.midPois)) {
                        viaList.addAll(carRouteResult.midPois)
                    } else {
                        return@let
                    }
                }
                viaList.add(poi)
//                val commandBean = CommandRequestRouteNaviBean.Builder().buildMisPoi(endPoi, viaList)
//                findNavController().navigate(R.id.action_searchAlongWayResultFragment_to_naviFragment, commandBean.toBundle())
                viewModel.addWayPoint(poi)
            }
        } else {
            poi?.let {
                Timber.i("btSet newPoi = $poi")
                val carRouteResult = mRouteRequestController.carRouteResult
                val startPoi = carRouteResult.fromPOI
                val endPoi = carRouteResult.toPOI
                val viaList = arrayListOf<POI>()
                if (carRouteResult?.hasMidPos() == true) {
                    if (checkViaPoi(poi, carRouteResult.midPois)) {
                        viaList.addAll(carRouteResult.midPois)
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

    private val layerClickObserver: ILayerClickObserver = object : ILayerClickObserver {
        override fun onBeforeNotifyClick(baseLayer: BaseLayer?, layerItem: LayerItem?, clickViewIdInfo: ClickViewIdInfo?) {}
        override fun onNotifyClick(layer: BaseLayer?, layerItem: LayerItem?, clickViewIds: ClickViewIdInfo?) {
            lifecycleScope.launch(Dispatchers.Main) {
                when (layerItem?.businessType) {
                    BizSearchType.BizSearchTypePoiAlongRoute -> {
                        val item =
                            layerItem as SearchAlongWayLayerItem
                        val mIndex = item.id.toInt()
                        Timber.i("onNotifyClick BizSearchTypePoiAlongRoute mIndex=$mIndex")
                        val curSelection: Int = searchAimPoiPushAdapter.getSelection()
                        if (curSelection != mIndex) {
                            searchAimPoiPushAdapter.setSelection(mIndex)
                            binding.messageListview.scrollToPosition(mIndex)
                        } else {
                            searchAimPoiPushAdapter.setSelection(mIndex)
                        }
                    }

                    else -> {}
                }
            }

        }

        override fun onAfterNotifyClick(baseLayer: BaseLayer?, layerItem: LayerItem?, clickViewIdInfo: ClickViewIdInfo?) {}
    }
}

