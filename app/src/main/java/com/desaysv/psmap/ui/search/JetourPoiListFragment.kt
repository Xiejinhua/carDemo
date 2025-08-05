package com.desaysv.psmap.ui.search

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.autonavi.gbl.layer.model.BizCustomTypePoint
import com.autonavi.gbl.map.layer.BaseLayer
import com.autonavi.gbl.map.layer.LayerItem
import com.autonavi.gbl.map.layer.model.ClickViewIdInfo
import com.autonavi.gbl.map.layer.observer.ILayerClickObserver
import com.autosdk.bussiness.common.POI
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.tracking.EventTrackingUtils
import com.desaysv.psmap.databinding.FragmentJetourPoiListBinding
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.bean.CommandRequestSearchBean
import com.desaysv.psmap.model.bean.CommandRequestSearchCategoryBean
import com.desaysv.psmap.model.bean.JetourPoi
import com.desaysv.psmap.model.business.ByteAutoBusiness
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.search.bean.SearchResultBean
import com.desaysv.psmap.ui.search.view.SearchSinglePoiView
import com.desaysv.psmap.utils.LoadingUtil
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 张楠
 * @time 2024/2/22
 * @description
 */
@AndroidEntryPoint
class JetourPoiListFragment : Fragment() {
    private lateinit var binding: FragmentJetourPoiListBinding
    private val viewModel by viewModels<JetourPoiListModel>()

    @Inject
    lateinit var toast: ToastUtil

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var byteAutoBusiness: ByteAutoBusiness

    private lateinit var commandRequestSearchBean: CommandRequestSearchBean

    private var mCurrentJetourPoi: JetourPoi? = null

    @Inject
    lateinit var loadingUtil: LoadingUtil

    @Inject
    lateinit var gson: Gson

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.i("onCreateView()")
        binding = FragmentJetourPoiListBinding.inflate(inflater, container, false)
        requireArguments().getParcelable<CommandRequestSearchBean>(Biz.KEY_BIZ_SEARCH_REQUEST)?.let {
            commandRequestSearchBean = it
            Timber.i("commandRequestSearchBean.type = ${commandRequestSearchBean.type}")
            Timber.i("commandRequestSearchBean = ${commandRequestSearchBean.keyword}")
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
    }

    private fun initView() {

        binding.searchCategoryBack.setDebouncedOnClickListener {
            findNavController().navigateUp()
        }

        binding.singlePoiView.setOnSinglePoiListener(object : SearchSinglePoiView.onSinglePoiListener {
            override fun onCloseClick() {
                viewModel.showSinglePoiView(null)
                byteAutoBusiness.showPreview()
                mCurrentJetourPoi?.schema?.let { schema ->
                    startDouyinActivityBySchema(schema)
                }
            }

            override fun onPhoneCall(phone: String?) {
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
                    NavHostFragment.findNavController(this@JetourPoiListFragment)
                        .navigate(R.id.to_searchCategoryFragment, commandBean.toBundle())
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

            }

            override fun onChildPoiItemClick(childPosition: Int, childPoi: POI?) {

            }
        })
        binding.clRoot.setDebouncedOnClickListener {
            findNavController().navigateUp()
        }
    }

    var job: Job? = null

    private fun initData() {
        viewModel.init()
        viewModel.isByteDanceForeground.observe(viewLifecycleOwner) { isForeground ->
            Timber.i("isByteDanceForeground isForeground:$isForeground")
            Timber.i("isByteDanceForeground isLoadingLiveData:${viewModel.isLoadingLiveData.value}")
            if (isForeground) {
                if (viewModel.isLoadingLiveData.value == true) {
                    viewModel.isLoadingLiveData.postValue(false)
                }
            }
        }
        viewModel.jetourPoiListLiveData.observe(viewLifecycleOwner) { list ->
            Timber.i("jetourPoiList.size = ${list.size}")
            viewModel.updateJetourPoiList(list)
            byteAutoBusiness.showPreview()
            mCurrentJetourPoi = list.firstOrNull()
            byteAutoBusiness.setFocus(BizCustomTypePoint.BizCustomTypePoint5, mCurrentJetourPoi?.id, true)
            commandRequestSearchBean.keyword?.let { schema ->
                mCurrentJetourPoi?.schema = schema
                startDouyinActivityBySchema(schema)
            }
        }
        byteAutoBusiness.setLayerClickObserver(layerClickObserver, false)

        viewModel.singleResult.observe(viewLifecycleOwner) { searchResultBean ->
            Timber.i("singleResult = ${gson.toJson(searchResultBean)}")
            Timber.i("isLoadingLiveData:${viewModel.isLoadingLiveData.value}")
            if (searchResultBean!=null) {
                searchResultBean.let { bean ->
                    bean.poi?.let { poi ->
                        val type = commandRequestSearchBean.type.let {
                            if (it in 3 until 6) {
                                it
                            } else {
                                0
                            }

                        }
                        bean.type = type
                        bean.isFavorite = viewModel.isFavorited(poi)
                    }
                    binding.singlePoiView.updateData(bean)
                    viewModel.updateMapCenter(bean.poi)
                }
            }else {
                Timber.i("singleResult is null")
                byteAutoBusiness.showPreview()
            }
        }
        viewModel.isLoadingLiveData.observe(viewLifecycleOwner) { isLoading ->
            Timber.i("isLoadingLiveData: $isLoading")
            if (isLoading) {
                loadingUtil.showLoading("正在搜索", onItemClick = {
                    job?.cancel()
                    if (viewModel.isLoadingLiveData.value== true) {
                        Timber.i("isLoadingLiveData cancel job")
                        viewModel.isLoadingLiveData.postValue(false)
                    }
                })
            } else {
                loadingUtil.cancelLoading()
            }
        }
    }

    private val layerClickObserver: ILayerClickObserver = object : ILayerClickObserver {
        override fun onBeforeNotifyClick(baseLayer: BaseLayer?, layerItem: LayerItem?, clickViewIdInfo: ClickViewIdInfo?) {}
        override fun onNotifyClick(layer: BaseLayer?, layerItem: LayerItem?, clickViewIds: ClickViewIdInfo?) {
            when (layerItem?.businessType) {
                BizCustomTypePoint.BizCustomTypePoint5 -> {
                    val id = layerItem.id
                    Timber.i("onNotifyClick() id=$id")
                    val jetourPoi = getJetourPitById(id)
                    jetourPoi?.let {
                        if (jetourPoi.id == mCurrentJetourPoi?.id) {
                            //当前已经选中，跳转至地点详情页
                            Timber.i("onNotifyClick() 当前已经选中，跳转至地点详情页")
                            closeDouyinActivity()
                            lifecycleScope.launch {
                                val poi = viewModel.getSearchBeanByJetourPoi(jetourPoi)
                                val searchBean = SearchResultBean(poi)
                                if (searchBean.disAndTime == null) {
                                    searchBean.disAndTime = viewModel.getDisTime(poi)
                                }
                                viewModel.showSinglePoiView(searchBean)
                            }
                            return
                        } else {
                            viewModel.showSinglePoiView(null)
                            mCurrentJetourPoi = jetourPoi
                            jetourPoi.let {
                                job = lifecycleScope.launch(Dispatchers.IO) {
                                    viewModel.isLoadingLiveData.postValue(true)
                                    if (jetourPoi.schema.isNullOrEmpty()) {
                                        jetourPoi.schema = byteAutoBusiness.getByteAutoVideoSchemaByKeyword(jetourPoi.venue_name)
                                    }
                                    mCurrentJetourPoi?.schema = jetourPoi.schema
                                    Timber.i("onNotifyClick() schema${mCurrentJetourPoi?.schema}")
                                    viewModel.isLoadingLiveData.postValue(false)
                                    if (mCurrentJetourPoi?.schema.isNullOrEmpty()) {
                                        withContext(Dispatchers.Main) {
                                            toast.showToast("网络异常，请检查网络后再试")
                                            Timber.i("onNotifyClick findNavController().navigateUp()")
                                        }
                                        return@launch
                                    } else {
                                        mCurrentJetourPoi?.schema?.let { schema ->
                                            startDouyinActivityBySchema(schema)
                                        }
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


    fun getJetourPitById(id: String): JetourPoi? {
        Timber.i("getJetourPitById() id=$id")
        viewModel.jetourPoiListLiveData.value?.let { list ->
            Timber.i("getJetourPitById() list.size = ${list.size}")
            return list.firstOrNull { it.id == id }
        }
        return null
    }

    override fun onDestroyView() {
        Timber.i("onDestroyView()")
        super.onDestroyView()
        byteAutoBusiness.setLayerClickObserver(layerClickObserver, true)
        byteAutoBusiness.clearAllItem()
    }

    override fun onPause() {
        super.onPause()
        Timber.i("onPause()")
    }

    override fun onDestroy() {
        Timber.i("onDestroy()")
        super.onDestroy()
    }

    private fun startDouyinActivityBySchema(value: String) {
        Timber.i("startDouyinActivityBySchema")
        if (isAdded.not()) {
            Timber.w("Fragment is not added, cannot start Douyin activity")
            return
        }
        val schema = "$value&card_attr_style=screen_1"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(schema)).apply {
            setPackage("com.bytedance.byteautoservice3")
            //设置启动Flags，清空并创建新Task
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun closeDouyinActivity() {
        Timber.i("closeDouyinActivity")
        context?.sendBroadcast(Intent("com.bytedance.byteautoservice3.action.close_app").apply {
            setPackage("com.bytedance.byteautoservice3")
        })
    }


}

