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
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.autonavi.auto.skin.NightModeGlobal
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.common.POIFactory
import com.autosdk.view.KeyboardUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.bean.MapPointCardData.PoiCardType
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentSearchAddTeamDestinationBinding
import com.desaysv.psmap.model.bean.CommandRequestPOICardBean
import com.desaysv.psmap.model.bean.CommandRequestPOICardBean.Type.Companion.POI_CARD_SEARCH_TEAM_DESTINATION
import com.desaysv.psmap.model.bean.CommandRequestSearchBean
import com.desaysv.psmap.model.bean.CommandRequestSearchBean.Type.Companion.SEARCH_TEAM_DESTINATION
import com.desaysv.psmap.model.bean.CommandRequestSearchCategoryBean
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.ui.adapter.SearchCategoryAdapter
import com.desaysv.psmap.ui.adapter.SearchHistoryAdapter
import com.desaysv.psmap.ui.adapter.SearchSuggestionAdapter
import com.desaysv.psmap.ui.dialog.CustomDialogFragment
import com.desaysv.psmap.ui.search.bean.SearchHistoryBean
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 张楠
 * @time 2024/1/8
 * @description
 */
@AndroidEntryPoint
class SearchAddTeamDestinationFragment : Fragment() {
    private lateinit var binding: FragmentSearchAddTeamDestinationBinding
    private lateinit var searchCategoryAdapter: SearchCategoryAdapter
    private lateinit var searchHistoryAdapter: SearchHistoryAdapter
    private lateinit var searchSuggestionAdapter: SearchSuggestionAdapter
    private val viewModel by viewModels<SearchAddTeamDestinationModel>()

    @Inject
    lateinit var toast: ToastUtil

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    private var deleteSearchHistoryDialog: CustomDialogFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.i("onCreateView()")
        binding = FragmentSearchAddTeamDestinationBinding.inflate(inflater, container, false)
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
        binding.executePendingBindings()
    }

    private fun initView() {

        binding.searchBox.showCityBtn(false)
        binding.searchBox.setHint(getString(R.string.sv_custom_trip_point_name))
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
            doSearch(binding.searchBox.getTextContent())
        }
        binding.searchBox.setOnEditorActionListener { _, actionId, _ ->
            if (actionId >= EditorInfo.IME_ACTION_GO && actionId <= EditorInfo.IME_ACTION_DONE) {
                doSearch(binding.searchBox.getTextContent())
                true
            } else {
                false
            }
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
                            doSearch(searchCategoryAdapter.data[position].name)
                        }

                        "更多" -> {
                            toSearchCategoryFragment()
                        }

                        "地图选点" -> {
                            val commandRequestPOICardBean = CommandRequestPOICardBean.Builder().setType(POI_CARD_SEARCH_TEAM_DESTINATION).build()
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
                            findNavController().navigate(R.id.to_favoriteFragment,
                                Bundle().apply { putInt(Biz.TO_FAVORITE_TYPE, BaseConstant.TO_FAVORITE_TEAM_DESTINATION) })
                        }

                        "收到的点" -> {
                            findNavController().navigate(R.id.to_SearchAimPoiPushFragment,
                                Bundle().apply { putBoolean(Biz.TO_AIM_POI_PUSH_TYPE, true) })
                        }
                    }
                }
            })
        }
        binding.searchCommonCategoryListview.adapter = searchCategoryAdapter

        searchHistoryAdapter = SearchHistoryAdapter().also { adapter ->
            adapter.setOnItemClickListener(object : SearchHistoryAdapter.OnItemClickListener {
                override fun onItemClick(bean: SearchHistoryBean) {
                    addDestination(bean.poi)
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
                    addDestination(bean.poi)
                }

                override fun onItemRightClick(bean: SearchHistoryBean) {
                    addDestination(bean.poi)
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

        searchCategoryAdapter.updateData(SearchCategoryConstants.ALONG_WAY_CATEGORY_LIST)

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
                show(this@SearchAddTeamDestinationFragment.childFragmentManager, "customDialog")
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

    fun addDestination(poi: POI?) {
        poi?.let {
            viewModel.addDestination(poi)
            findNavController().navigateUp()
        }
    }

    private fun doSearch(keyword: String?) {
        if (!TextUtils.isEmpty(keyword)) {
            KeyboardUtil.hideKeyboard(view)
            val commandBean = CommandRequestSearchBean.Builder().setKeyword(keyword).setType(SEARCH_TEAM_DESTINATION).build()
            NavHostFragment.findNavController(this@SearchAddTeamDestinationFragment).navigate(R.id.to_searchResultFragment, commandBean.toBundle())
        } else {
            toast.showToast(R.string.sv_search_please_enter_keyword)
        }
    }

    /**
     * 跳转更多界面
     */
    private fun toSearchCategoryFragment() {
        KeyboardUtil.hideKeyboard(view)
        //使用当前位置进行周边搜索
        val currentPoi: POI = POIFactory.createPOI("我的位置", GeoPoint(viewModel.getLastLocation().longitude, viewModel.getLastLocation().latitude));
        val commandBean = currentPoi.let {
            CommandRequestSearchCategoryBean.Builder()
                .setPoi(it)
                .setType(SEARCH_TEAM_DESTINATION)
                .build()
        }
        findNavController().navigate(R.id.to_searchCategoryFragment, commandBean.toBundle())

    }
}