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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.autonavi.auto.skin.NightModeGlobal
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.common.POIFactory
import com.autosdk.view.KeyboardUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.tracking.EventTrackingUtils
import com.desaysv.psmap.databinding.FragmentSearchBinding
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.bean.CommandRequestSearchBean
import com.desaysv.psmap.model.bean.CommandRequestSearchBean.Type.Companion.SEARCH_JETOURPOI_SCHEMA
import com.desaysv.psmap.model.bean.CommandRequestSearchBean.Type.Companion.SEARCH_KEYWORD
import com.desaysv.psmap.model.bean.CommandRequestSearchCategoryBean
import com.desaysv.psmap.model.business.ByteAutoBusiness
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_93
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.SearchHistoryAdapter
import com.desaysv.psmap.ui.adapter.SearchSuggestionAdapter
import com.desaysv.psmap.ui.dialog.CustomDialogFragment
import com.desaysv.psmap.ui.search.bean.SearchHistoryBean
import com.desaysv.psmap.ui.search.bean.SearchResultBean
import com.desaysv.psmap.ui.search.view.SearchSinglePoiView
import com.desaysv.psmap.utils.LoadingUtil
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 谢锦华
 * @time 2024/1/8
 * @description
 */
@AndroidEntryPoint
class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var searchHistoryAdapter: SearchHistoryAdapter
    private lateinit var searchSuggestionAdapter: SearchSuggestionAdapter
    private val viewModel by viewModels<SearchViewModel>()


    @Inject
    lateinit var toast: ToastUtil

    @Inject
    lateinit var loadingUtil: LoadingUtil

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var byteAutoBusiness: ByteAutoBusiness

    private var deleteSearchHistoryDialog: CustomDialogFragment? = null

    @Inject
    lateinit var netWorkManager: NetWorkManager

    @Inject
    lateinit var gson: Gson

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.i("onCreateView()")
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        val commandRequestSearchBean = requireArguments().getParcelable<CommandRequestSearchBean>(Biz.KEY_BIZ_SEARCH_REQUEST)
        viewModel.setSearchBean(commandRequestSearchBean)
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
        binding.searchBox.setNight(NightModeGlobal.isNightMode())
    }

    private fun initView() {
        binding.searchBox.showCityBtn(!viewModel.isNetworkConnected())
        binding.searchBox.editOnTouchListener { v, event ->
            if (event?.action == MotionEvent.ACTION_UP) {
                requireActivity().window.setLocalFocus(true, true)
                binding.searchBox.hideKeyboard(false)
            } else if (event?.action == MotionEvent.ACTION_DOWN) {
                requireActivity().window.setLocalFocus(true, true)
            }
            false
        }
        viewModel.getSearchBean().let { searchBean ->
            //设置离线城市
            binding.searchBox.setCityName(searchBean?.city?.cityName)
        }
        binding.clRoot.setDebouncedOnClickListener {
            findNavController().navigateUp()
        }
        binding.searchBox.setHint(getString(R.string.sv_search_enter_destination))
        //搜索框文字改变时发起预搜索
        binding.searchBox.editAddTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                Timber.d("s.toString() = ${s.toString()}")
                searchSuggestionAdapter.keyword = s.toString().trim()
                viewModel.onInputKeywordChanged(s.toString().trim())
            }
        })

        //搜索框返回键
        binding.searchBox.backOnClickListener { findNavController().navigateUp() }
        //搜索按钮
        binding.searchBox.searchOnClickListener {
            doSearch(binding.searchBox.getTextContent())
        }
        //离线城市选择按钮
        binding.searchBox.switchCityOnClickListener {
            val commandBean = viewModel.getSearchBean()
            NavHostFragment.findNavController(this@SearchFragment).navigate(R.id.to_searchSwitchCityFragment, commandBean?.toBundle())
        }
        //显示键盘
//        binding.searchBox.hideKeyboard(false)
        binding.searchBox.setOnEditorActionListener { _, actionId, _ ->
            if (actionId >= EditorInfo.IME_ACTION_GO && actionId <= EditorInfo.IME_ACTION_DONE) {
                doSearch(binding.searchBox.getTextContent())
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

        //周边搜索加油站
        binding.clSearchCategoryGasStation.setDebouncedOnClickListener {
            doSearch("加油站")
        }

        //捷途探趣
        binding.clSearchCategoryDouyin.setDebouncedOnClickListener {
            if (viewModel.getDouYinConfirm()) { //已经同意协议
                if (netWorkManager.isNetworkConnected()) {
                    val job = lifecycleScope.launch(Dispatchers.Main) {
                        val list = byteAutoBusiness.fefreshJetourPoiListDistance()
                        if (list.isNotEmpty()) {
                            val schema = byteAutoBusiness.getByteAutoVideoSchemaByKeyword(list.first().venue_name)
                            loadingUtil.cancelLoading()
                            if (schema.isNullOrEmpty()) {
                                toast.showToast("网络异常，请检查网络后再试")
                            } else {
                                val commandBean =
                                    CommandRequestSearchBean.Builder()
                                        .setKeyword(schema)
                                        .setType(SEARCH_JETOURPOI_SCHEMA)
                                        .build()
                                findNavController().navigate(R.id.to_jetourPoiListFragment, commandBean.toBundle())
                            }
                        } else {
                            //走不到这里
                            loadingUtil.cancelLoading()
                            toast.showToast("未找到数据")
                        }
                    }
                    loadingUtil.showLoading("正在搜索", onItemClick = {
                        job.cancel()
                    })
                } else {
                    toast.showToast("网络异常，请检查网络后再试")
                }
            } else { //未同意协议
                findNavController().navigate(R.id.to_searchDouYinConfirmFragment)
            }

            EventTrackingUtils.trackEvent(
                EventTrackingUtils.EventName.IntSearch_Click,
                mapOf(
                    Pair(EventTrackingUtils.EventValueName.JetourInterestClick, System.currentTimeMillis())
                )
            )

        }
        //周边搜索停车场
        binding.clSearchCategoryPark.setDebouncedOnClickListener {
            doSearch("停车场")
        }

        binding.clSearchCategoryMore.setDebouncedOnClickListener {
            EventTrackingUtils.trackEvent(
                EventTrackingUtils.EventName.SurroundSearch_Click,
                Pair(EventTrackingUtils.EventValueName.SearchTime, System.currentTimeMillis())
            )
            //使用当前位置进行周边搜索
            val currentPoi = POIFactory.createPOI("我的位置", GeoPoint(viewModel.getLastLocation().longitude, viewModel.getLastLocation().latitude));
            val commandBean = currentPoi?.let {
                CommandRequestSearchCategoryBean.Builder()
                    .setPoi(it)
                    .setCity(viewModel.getSearchBean()?.city)
                    .setType(CommandRequestSearchCategoryBean.Type.Companion.SEARCH_AROUND)
                    .build()
            }
            NavHostFragment.findNavController(this@SearchFragment)
                .navigate(R.id.action_searchFragment_to_searchCategoryFragment, commandBean?.toBundle())
        }

        //日夜模式监听
        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            binding.searchBox.setNight(it == true)
            searchHistoryAdapter.notifyDataSetChanged()
            searchSuggestionAdapter.notifyDataSetChanged()
        }

        binding.singlePoiView.setOnSinglePoiListener(object : SearchSinglePoiView.onSinglePoiListener {
            override fun onCloseClick() {
                if ((viewModel.isSuggestionLiveData.value == true)) {
                    viewModel.showSinglePoiView(null)
                } else {
                    findNavController().navigateUp()
                }
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
                            .setCity(viewModel.getSearchBean()?.city)
                            .setType(CommandRequestSearchCategoryBean.Type.Companion.SEARCH_AROUND)
                            .build()
                    }
                    NavHostFragment.findNavController(this@SearchFragment)
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

        ViewClickEffectUtils.addClickScale(binding.clSearchCategoryDouyin, CLICKED_SCALE_93)
        ViewClickEffectUtils.addClickScale(binding.clSearchCategoryGasStation, CLICKED_SCALE_93)
        ViewClickEffectUtils.addClickScale(binding.clSearchCategoryPark, CLICKED_SCALE_93)
        ViewClickEffectUtils.addClickScale(binding.tvDeleteHistory, CLICKED_SCALE_90)

        ViewClickEffectUtils.addClickScale(binding.clSearchCategoryMore, CLICKED_SCALE_93)
    }

    private fun initAdapter() {

        //历史搜索及导航记录Adapter
        searchHistoryAdapter = SearchHistoryAdapter().also {
            it.setOnItemClickListener(object : SearchHistoryAdapter.OnItemClickListener {

                override fun onItemClick(bean: SearchHistoryBean) {
                    when (bean.type) {
                        1 -> {
                            doRoute(bean.poi)
                        }

                        2 -> {
                            doSearch(bean.poi?.name)
                        }
                    }
                }

                override fun onItemLongClick(bean: SearchHistoryBean): Boolean {
                    when (bean.type) {
                        1 -> {
                            clearDialog("确定删除此条路线？") {
                                if (it) {
                                    if (viewModel.delHistoryRoute(bean)) {
                                        binding.searchHistoryListview.closeMenu()
                                        toast.showToast("已删除此条路线", com.desaysv.psmap.model.R.drawable.ic_toast_complete_day)
                                        viewModel.refreshHistoryData()
                                    } else {
                                        toast.showToast("删除此条路线失败")
                                    }
                                }
                            } //清除某条路线数据
                        }

                        2 -> {
                            clearDialog("确定删除此条记录？") {
                                if (it) {
                                    if (viewModel.delSearchHistory(bean)) {
                                        binding.searchHistoryListview.closeMenu()
                                        toast.showToast("已删除此条记录", com.desaysv.psmap.model.R.drawable.ic_toast_complete_day)
                                        viewModel.refreshHistoryData()
                                    } else {
                                        toast.showToast("删除记录失败")
                                    }
                                }
                            } //清除某条路线数据
                        }
                    }
                    return true
                }

                override fun onFavorite(position: Int) {
                    it.data[position].poi?.let { poi ->
                        if (it.data[position].isFavorite) {
                            if (viewModel.delFavorite(poi)) {
                                toast.showToast("已取消收藏", com.desaysv.psmap.model.R.drawable.ic_toast_complete_day)
                                it.data[position].isFavorite = false
                            } else {
                                toast.showToast("取消收藏失败")
                            }
                        } else {
                            if (viewModel.addFavorite(poi)) {
                                toast.showToast("已收藏", com.desaysv.psmap.model.R.drawable.ic_toast_complete_day)
                                it.data[position].isFavorite = true
                            } else {
                                toast.showToast("收藏失败")
                            }
                        }
                        it.notifyItemChanged(position)
                    }
                }

                override fun onGoThere(poi: POI?) {
                    doRoute(poi)
                }

                override fun onHistoryDelete(bean: SearchHistoryBean) {
                    when (bean.type) {
                        1 -> {
                            clearDialog("确定删除此条路线？") {
                                if (it) {
                                    if (viewModel.delHistoryRoute(bean)) {
                                        binding.searchHistoryListview.closeMenu()
                                        toast.showToast("已删除此条路线", com.desaysv.psmap.model.R.drawable.ic_toast_complete_day)
                                        viewModel.refreshHistoryData()
                                    } else {
                                        toast.showToast("删除此条路线失败")
                                    }
                                }
                            } //清除某条路线数据
                        }

                        2 -> {
                            clearDialog("确定删除此条记录？") {
                                if (it) {
                                    if (viewModel.delSearchHistory(bean)) {
                                        binding.searchHistoryListview.closeMenu()
                                        toast.showToast("已删除此条记录", com.desaysv.psmap.model.R.drawable.ic_toast_complete_day)
                                        viewModel.refreshHistoryData()
                                    } else {
                                        toast.showToast("删除记录失败")
                                    }
                                }
                            } //清除某条路线数据
                        }
                    }
                }
            })

            it.setOnItemLongClickListener { adapter, view, position ->
                Timber.i("onItemLongClick() called with: adapter = $adapter, view = $view, position = $position")
                val bean: SearchHistoryBean = it.data[position]
                when (bean.type) {
                    1 -> {
                        clearDialog("确定删除此条路线？") {
                            if (it) {
                                if (viewModel.delHistoryRoute(bean)) {
                                    binding.searchHistoryListview.closeMenu()
                                    toast.showToast("已删除此条路线", com.desaysv.psmap.model.R.drawable.ic_toast_complete_day)
                                    viewModel.refreshHistoryData()
                                } else {
                                    toast.showToast("删除此条路线失败")
                                }
                            }
                        } //清除某条路线数据
                    }

                    2 -> {
                        clearDialog("确定删除此条记录？") {
                            if (it) {
                                if (viewModel.delSearchHistory(bean)) {
                                    binding.searchHistoryListview.closeMenu()
                                    toast.showToast("已删除此条记录", com.desaysv.psmap.model.R.drawable.ic_toast_complete_day)
                                    viewModel.refreshHistoryData()
                                } else {
                                    toast.showToast("删除记录失败")
                                }
                            }
                        } //清除某条路线数据
                    }
                }
                true
            }
        }
        binding.searchHistoryListview.adapter = searchHistoryAdapter

        //预搜索Adapter
        searchSuggestionAdapter = SearchSuggestionAdapter().also {
            it.setOnItemClickListener(object : SearchSuggestionAdapter.OnItemClickListener {
                override fun onItemClick(bean: SearchHistoryBean) {
                    when (bean.type) {
                        3 -> {
                            goPOIDetail(bean.poi)
                        }

                        else -> {
                            Timber.i("searchSuggestionAdapter onItemClick error")
                        }
                    }
                }

                override fun onItemRightClick(bean: SearchHistoryBean) {
                    binding.searchBox.setText(bean.poi?.name)
                }

                override fun onChildPoiClick(poi: POI?) {
                    goPOIDetail(poi)
                }

            })
        }
        binding.searchSuggestionListview.adapter = searchSuggestionAdapter

    }

    private fun initData() {
        viewModel.init()

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

        viewModel.noHistoryVisibilityLiveData.observe(viewLifecycleOwner) {
            Timber.i("noHistoryVisibilityLiveData = $it")
        }
        viewModel.singleResult.observe(viewLifecycleOwner) {
            Timber.i("singleResult = ${gson.toJson(it)}")

            it?.let { bean ->
                bean.poi?.let { poi ->
                    val type = viewModel.getSearchBean()?.type.let {
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
                binding.singlePoiView.updateData(bean)
                viewModel.addSearchHistory(poi = bean.poi)
            }
        }
        if (viewModel.historyPoiListLiveData.value.isNullOrEmpty()) {
            viewModel.startSync() //同步获取账号信息
            viewModel.refreshHistoryData()
        }
        if (viewModel.getSearchBean()?.shouldShowKeyboard == true) {
            binding.searchBox.hideKeyboard(false)
        }
        viewModel.setFollowMode(false)
    }

    //清空历史弹窗
    private fun clearDialog(content: String, onClick: (isOk: Boolean) -> Unit) {
        dismissClearSearchHistoryDialog()
        deleteSearchHistoryDialog = CustomDialogFragment.builder().setCloseButton(true).setContent(content)
            .setOnClickListener(onClick)
            .apply {
                show(this@SearchFragment.childFragmentManager, "customDialog")
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

    private fun doSearch(keyword: String?) {
        if (!TextUtils.isEmpty(keyword)) {
            KeyboardUtil.hideKeyboard(view)
            Timber.i("doSearch commandRequestSearchBean:${viewModel.getSearchBean()}")
            val commandBean = viewModel.getSearchBean()?.apply {
                this.keyword = keyword
                this.type = SEARCH_KEYWORD
            } ?: CommandRequestSearchBean.Builder().setKeyword(keyword).setType(SEARCH_KEYWORD).build()
            NavHostFragment.findNavController(this@SearchFragment).navigate(R.id.to_searchResultFragment, commandBean.toBundle())
        } else {
            toast.showToast(R.string.sv_search_please_enter_keyword)
        }
    }

    private fun doRoute(poi: POI?) {
        KeyboardUtil.hideKeyboard(view)
        val commandBean = poi?.let { poi -> CommandRequestRouteNaviBean.Builder().build(poi) }
        viewModel.planRoute(commandBean)
    }

    //去poi详情页，待实现
    private fun goPOIDetail(poi: POI?) {
        poi?.let {
            KeyboardUtil.hideKeyboard(view)
            lifecycleScope.launch(Dispatchers.IO) {
                val resultBean = SearchResultBean(poi = poi)
                viewModel.getDeepInfo(resultBean)
                viewModel.showSinglePoiView(resultBean)
            }
        }

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
        Timber.i("onStop()")
        super.onStop()
        dismissClearSearchHistoryDialog()
        loadingUtil.cancelLoading()
    }
}