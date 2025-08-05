package com.desaysv.psmap.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.autonavi.gbl.common.model.Coord3DDouble
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.view.KeyboardUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.bean.MapPointCardData
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.data.INaviRepository
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentMapPointDataBinding
import com.desaysv.psmap.model.bean.CommandRequestPOICardBean
import com.desaysv.psmap.model.bean.CommandRequestPOICardBean.Type.Companion.POI_CARD_SEARCH_COMPANY
import com.desaysv.psmap.model.bean.CommandRequestPOICardBean.Type.Companion.POI_CARD_SEARCH_HOME
import com.desaysv.psmap.model.bean.CommandRequestPOICardBean.Type.Companion.POI_CARD_SEARCH_TEAM_DESTINATION
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.SearchResultChildAdapter
import com.desaysv.psmap.ui.dialog.CustomDialogFragment
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 张楠
 * @time 2024/12/30
 * @description 地图选点-实现点击图面显示的功能
 */
@AndroidEntryPoint
class MapPointDataFragment : Fragment() {
    private lateinit var binding: FragmentMapPointDataBinding
    private val viewModel by viewModels<MapPointDataViewModel>()
    private lateinit var commandRequestPOICardBean: CommandRequestPOICardBean

    private lateinit var searchResultChildAdapter: SearchResultChildAdapter

    @Inject
    lateinit var toast: ToastUtil

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var mRouteRequestController: RouteRequestController

    @Inject
    lateinit var mINaviRepository: INaviRepository

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    private var lastMidpoi: POI? = null

    private var favoriteDialog: CustomDialogFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.i("onCreateView()")
        binding = FragmentMapPointDataBinding.inflate(inflater, container, false)
        requireArguments().getParcelable<CommandRequestPOICardBean>(Biz.KEY_BIZ_SHOW_POI_CARD)?.let {
            commandRequestPOICardBean = it
            Timber.i("commandRequestPOICardBean = $commandRequestPOICardBean")
            Timber.i("commandRequestPOICardBean.type = ${commandRequestPOICardBean.type}")
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initView()
        initData()
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.type = commandRequestPOICardBean.type
        binding.executePendingBindings()


        binding.rlSearchChildStation.layoutManager =
            GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        searchResultChildAdapter = SearchResultChildAdapter().apply {
            this.setOnSearchResultChildListener(object : SearchResultChildAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    searchResultChildAdapter.toggleSelectPosition(position)
                    viewModel.updatePointCardChildPoiIndex(selectPosition())
                }
            })
        }
        binding.rlSearchChildStation.adapter = searchResultChildAdapter

    }

    private fun initView() {
        initSetButton(commandRequestPOICardBean.type)
        //返回键
        binding.ivClose1.setDebouncedOnClickListener { findNavController().navigateUp() }

        binding.ivClose2.setDebouncedOnClickListener { findNavController().navigateUp() }

        binding.ivMoreChild.setDebouncedOnClickListener {
            Timber.i("ivMoreChild click")
            binding.ivMoreChild.isSelected = !binding.ivMoreChild.isSelected
            viewModel.showViaPoi.value?.poi?.childPois?.run {
                if (this.size > 2) {
                    if (binding.ivMoreChild.isSelected) {
                        searchResultChildAdapter.updateData(this)
                    } else {
                        searchResultChildAdapter.updateData(this.subList(0, 2))
                    }
                } else {
                    Timber.w("childPois < 2 ")
                    viewModel.showChild(this.isNotEmpty(), false)
                }
            }
        }

        //初始化子POI
        viewModel.showViaPoi.value?.poi?.childPois?.run {
            if (this.isEmpty()) {
                viewModel.showChild(false, false)
            } else {
                viewModel.showChild(true, this.size > 2)
                if (this.size > 2 && !binding.ivMoreChild.isSelected) {
                    searchResultChildAdapter.updateData(this.subList(0, 2))
                } else {
                    searchResultChildAdapter.updateData(this)
                }
            }
        }
        binding.ivAddVia.setDebouncedOnClickListener {
            addViaPoi(viewModel.showViaPoi.value?.poi)
        }
        binding.ivViaGoHere.setDebouncedOnClickListener {
            viewModel.showViaPoi.value?.poi?.run {
                var goPoi = this
                if (!this.childPois.isNullOrEmpty() && viewModel.showViaPoi.value?.poi?.childIndex != -1) {
                    goPoi = this.childPois[viewModel.showViaPoi.value!!.poi.childIndex]
                }
                Timber.i("clGoHere ${goPoi.name}")
                val commandBean = CommandRequestRouteNaviBean.Builder().build(goPoi)
                viewModel.planRoute(commandBean)
            }
        }
        binding.ivAddHome.setDebouncedOnClickListener {
            viewModel.showViaPoi.value?.poi?.let { poi ->
                doCollection(commandRequestPOICardBean.type, poi)
            }
        }
        ViewClickEffectUtils.addClickScale(binding.ivClose1, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.ivClose2, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.ivSearchAround, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.ivFavorite, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.ivPhoneCall, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.ivGoHere, CLICKED_SCALE_95)
        ViewClickEffectUtils.addClickScale(binding.ivAddHome, CLICKED_SCALE_95)
        ViewClickEffectUtils.addClickScale(binding.ivViaGoHere, CLICKED_SCALE_95)
        ViewClickEffectUtils.addClickScale(binding.ivAddVia, CLICKED_SCALE_95)
        ViewClickEffectUtils.addClickScale(binding.tvReTry, CLICKED_SCALE_95)
    }

    private fun initData() {
        viewModel.showViaPoi.observe(viewLifecycleOwner) { showViaPoi ->
            showViaPoi?.let {
                Timber.i(" SearchPoiDetailFragment is called showViaPoi = ${gson.toJson(it)}")
                it.poi.run {
                    if (this.childPois.isNullOrEmpty()) {
                        viewModel.showChild(false, false)
                    } else {
                        viewModel.showChild(true, this.childPois.size > 2)
                        if (this.childPois.size > 2 && !binding.ivMoreChild.isSelected) {
                            searchResultChildAdapter.updateData(this.childPois.subList(0, 2))
                        } else {
                            searchResultChildAdapter.updateData(this.childPois)
                        }
                    }

                }
                updatePOI(showViaPoi.poi)
            }
        }
        viewModel.setToast.unPeek().observe(viewLifecycleOwner) {
            it?.let { nonNullString ->
                if (nonNullString.isNotEmpty()) {
                    toast.showToast(nonNullString)
                }
            }
        }

        skyBoxBusiness.themeChange().unPeek().observe(viewLifecycleOwner) {
            searchResultChildAdapter.notifyDataSetChanged()
        }
    }

    private fun updatePOI(poi: POI) {
        viewModel.updateMapCenter(poi)
        viewModel.setFollowMode(false)
        //自车位置不显示扎点
        if (viewModel.showViaPoi.value?.cardType != MapPointCardData.PoiCardType.TYPE_CAR_LOC) {
            viewModel.showCustomTypePoint1(Coord3DDouble(poi.point.longitude, poi.point.latitude, 0.0))
        }
    }

    private fun doCollection(@CommandRequestPOICardBean.Type type: Int, poi: POI) {
        Timber.i("doCollection() called with: type = $type, poi = $poi")
        val isFavorite = viewModel.isFavorited(poi)
        when (type) {
            POI_CARD_SEARCH_HOME -> {
                if (isFavorite) {
                    dismissDialog()
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
                            show(this@MapPointDataFragment.childFragmentManager, "favoriteDialog")
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

            POI_CARD_SEARCH_COMPANY -> {
                if (isFavorite) {
                    dismissDialog()
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
                            }.apply {
                                show(this@MapPointDataFragment.childFragmentManager, "favoriteDialog")
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

            POI_CARD_SEARCH_TEAM_DESTINATION -> {
                viewModel.addDestination(poi)
                findNavController().popBackStack(R.id.searchAddTeamDestinationFragment, true)
            }
        }
    }

    fun initSetButton(@CommandRequestPOICardBean.Type type: Int) {
        when (type) {
            POI_CARD_SEARCH_HOME -> {
                binding.ivAddHome.text = "设置为家"
            }

            POI_CARD_SEARCH_COMPANY -> {
                binding.ivAddHome.text = "设置为公司"
            }

            POI_CARD_SEARCH_TEAM_DESTINATION -> {
                binding.ivAddHome.text = "设置为组队出行目的地"
            }
        }
    }

    private fun dismissDialog() {
        favoriteDialog?.run {
            if (this.isAdded || this.isVisible)
                dismissAllowingStateLoss()
        }
        favoriteDialog = null
    }


    override fun onPause() {
        super.onPause()
        Timber.i("onPause()")
        KeyboardUtil.hideKeyboard(view)
    }

    override fun onStop() {
        super.onStop()
        Timber.i("onStop()")
        dismissDialog()
    }


    override fun onDestroyView() {
        viewModel.hideCustomTypePoint1()
        super.onDestroyView()
    }

    override fun onDestroy() {
        Timber.i("onDestroy()")
        super.onDestroy()
    }

    private fun addViaPoi(poi: POI?) {
        lastMidpoi = poi
        mRouteRequestController.carRouteResult?.let { carRouteResult ->
            if (mINaviRepository.isRealNavi()) {
                poi?.let {
                    val endPoi = carRouteResult.toPOI
                    val viaList = arrayListOf<POI>()
                    if (carRouteResult.hasMidPos()) {
                        if (checkViaPoi(poi, carRouteResult.midPois)) {
                            viaList.addAll(carRouteResult.midPois)
                        } else {
                            return@let
                        }
                    }
                    viaList.add(poi)
                    viewModel.addWayPoint(poi)
                }
            } else {
                poi?.let {
                    Timber.i("btSet newPoi = $poi")
                    val startPoi = carRouteResult.fromPOI
                    val endPoi = carRouteResult.toPOI
                    val viaList = arrayListOf<POI>()
                    if (carRouteResult.hasMidPos()) {
                        if (checkViaPoi(poi, carRouteResult.midPois)) {
                            viaList.addAll(carRouteResult.midPois)
                        } else {
                            return@let
                        }
                    }
                    viaList.add(poi)
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
}