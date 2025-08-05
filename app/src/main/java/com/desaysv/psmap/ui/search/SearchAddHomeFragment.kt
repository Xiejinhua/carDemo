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
import com.desaysv.psmap.base.bean.MapPointCardData
import com.desaysv.psmap.base.bean.MapPointCardData.PoiCardType
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.databinding.FragmentSearchAddHomeBinding
import com.desaysv.psmap.model.bean.CommandRequestPOICardBean
import com.desaysv.psmap.model.bean.CommandRequestPOICardBean.Type.Companion.POI_CARD_SEARCH_COMPANY
import com.desaysv.psmap.model.bean.CommandRequestPOICardBean.Type.Companion.POI_CARD_SEARCH_HOME
import com.desaysv.psmap.model.bean.CommandRequestPOICardBean.Type.Companion.POI_CARD_SEARCH_TEAM_DESTINATION
import com.desaysv.psmap.model.bean.CommandRequestSearchBean
import com.desaysv.psmap.model.bean.CommandRequestSearchBean.Type.Companion.SEARCH_COMPANY
import com.desaysv.psmap.model.bean.CommandRequestSearchBean.Type.Companion.SEARCH_HOME
import com.desaysv.psmap.model.bean.CommandRequestSearchBean.Type.Companion.SEARCH_TEAM_DESTINATION
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.SearchAddHomeAdapter
import com.desaysv.psmap.ui.adapter.SearchHistoryAdapter
import com.desaysv.psmap.ui.dialog.CustomDialogFragment
import com.desaysv.psmap.ui.search.bean.SearchHistoryBean
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
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
class SearchAddHomeFragment : Fragment() {
    private lateinit var binding: FragmentSearchAddHomeBinding
    private lateinit var searchAddHomeAdapter: SearchAddHomeAdapter
    private lateinit var searchHistoryAdapter: SearchHistoryAdapter
    private val viewModel by viewModels<SearchAddHomeModel>()
    private lateinit var commandRequestSearchBean: CommandRequestSearchBean

    @Inject
    lateinit var toast: ToastUtil

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    private var favoriteDialog: CustomDialogFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.i("onCreateView()")
        binding = FragmentSearchAddHomeBinding.inflate(inflater, container, false)
        requireArguments().getParcelable<CommandRequestSearchBean>(Biz.KEY_BIZ_SEARCH_REQUEST)?.let {
            commandRequestSearchBean = it
            Timber.i("commandRequestSearchBean = $commandRequestSearchBean")
            Timber.i("commandRequestSearchBean.type = ${commandRequestSearchBean.type}")
        }
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
        //根据传入的type设置提示文字
        showTips()
        //搜索框文字改变时发起预搜索
        binding.searchBox.editAddTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val str = s.toString().trim()
                if (str.isEmpty()) {
                    showTips()
                }
                viewModel.onInputKeywordChanged(str)
            }
        })
        //搜索框返回键
        binding.searchBox.backOnClickListener { findNavController().navigateUp() }

        binding.searchBox.editOnTouchListener { v, event ->
            if (event?.action == MotionEvent.ACTION_UP) {
                requireActivity().window.setLocalFocus(true, true)
                binding.searchBox.hideKeyboard(false)
            } else if (event?.action == MotionEvent.ACTION_DOWN) {
                requireActivity().window.setLocalFocus(true, true)
            }
            false
        }
        //搜索按钮
        binding.searchBox.searchOnClickListener {
            doSearch(binding.searchBox.getTextContent())
        }
        //监听键盘确认键
        binding.searchBox.setOnEditorActionListener { _, actionId, _ ->
            if (actionId >= EditorInfo.IME_ACTION_GO && actionId <= EditorInfo.IME_ACTION_DONE) {
                doSearch(binding.searchBox.getTextContent())
                true
            } else {
                false
            }
        }
        binding.clMyPosition.setDebouncedOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    if(viewModel.gpsState.value == false) {
                        toast.showToast(R.string.sv_search_get_address)
                    }
                }
                val poi = viewModel.getLastLocationPoi()
                withContext(Dispatchers.Main) {
                    if (poi == null) {
                        toast.showToast(R.string.sv_search_get_address_fail)
                    } else {
                        doCollection(commandRequestSearchBean.type, poi)
                    }
                }
            }
        }
        binding.clMapPosition.setDebouncedOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.searchPoiCardInfo(
                    PoiCardType.TYPE_CAR_LOC,
                    POIFactory.createPOI("", GeoPoint(viewModel.getLastLocation().longitude, viewModel.getLastLocation().latitude), "")
                )
                withContext(Dispatchers.Main) {
                    toPoiDetailFragment(commandRequestSearchBean.type, null)
                }
            }
        }
        binding.tvNoCityDataRetry.setDebouncedOnClickListener {
            viewModel.inputKeyWord.value?.let { keyword ->
                viewModel.onInputKeywordChanged(keyword)
            }
        }

        //日夜模式监听
        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            binding.searchBox.setNight(it == true)
            searchAddHomeAdapter.notifyDataSetChanged()
            searchHistoryAdapter.notifyDataSetChanged()
        }

        ViewClickEffectUtils.addClickScale(binding.clMyPosition, CLICKED_SCALE_95)
        ViewClickEffectUtils.addClickScale(binding.clMapPosition, CLICKED_SCALE_95)
        ViewClickEffectUtils.addClickScale(binding.tvNoCityDataRetry, CLICKED_SCALE_95)
    }

    private fun initAdapter() {
        //预搜索Adapter
        searchAddHomeAdapter = SearchAddHomeAdapter().also { adapter ->
            adapter.setOnItemClickListener(object : SearchAddHomeAdapter.OnItemClickListener {
                override fun onItemClick(bean: SearchHistoryBean) {
                    toPoiDetailFragment(commandRequestSearchBean.type, bean.poi)
                }

                override fun onItemRightClick(bean: SearchHistoryBean) {
                    bean.poi?.let { poi ->
                        doCollection(commandRequestSearchBean.type, poi)
                    }
                }

            })
        }
        binding.searchSuggestionListview.adapter = searchAddHomeAdapter

        //历史Adapter
        searchHistoryAdapter = SearchHistoryAdapter().also { adapter ->
            adapter.setOnItemClickListener(object : SearchHistoryAdapter.OnItemClickListener {
                override fun onItemClick(bean: SearchHistoryBean) {
                    if (bean.type == 2) {
                        commandRequestSearchBean.poi = bean.poi
                        commandRequestSearchBean.keyword = bean.poi?.name
                        NavHostFragment.findNavController(this@SearchAddHomeFragment).navigate(R.id.to_searchResultFragment, commandRequestSearchBean.toBundle())
                    } else if (bean.type == 6) {
                        toPoiDetailFragment(commandRequestSearchBean.type, bean.poi)
                    }
                }

                override fun onItemLongClick(bean: SearchHistoryBean): Boolean {
                    return false
                }

                override fun onGoThere(poi: POI?) {
                    poi?.let {
                        doCollection(commandRequestSearchBean.type, poi)
                    }
                }

                override fun onFavorite(position: Int) {}
                override fun onHistoryDelete(bean: SearchHistoryBean) {}
            })
        }
        binding.searchHistoryListview.adapter = searchHistoryAdapter
    }

    private fun initData() {

        viewModel.historyPoiListLiveData.observe(viewLifecycleOwner) {
            searchHistoryAdapter.updateData(it)
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

        viewModel.suggestPoiListLiveData.observe(viewLifecycleOwner) {
            searchAddHomeAdapter.updateData(it)
        }
        if (viewModel.historyPoiListLiveData.value.isNullOrEmpty()) {
            viewModel.startSync() //同步获取账号信息
            viewModel.refreshHistoryData()
        }
        if ( commandRequestSearchBean.shouldShowKeyboard==true) {
            binding.searchBox.hideKeyboard(false)
        }
    }

    private fun doSearch(keyword: String?) {
        if (!TextUtils.isEmpty(keyword)) {
            KeyboardUtil.hideKeyboard(view)
            //commandRequestSearchBean在传过来时已经带了type，所以这里只设置keyword就行
            val commandBean = commandRequestSearchBean.also {
                it.keyword = keyword
            }
            NavHostFragment.findNavController(this@SearchAddHomeFragment).navigate(R.id.to_searchResultFragment, commandBean.toBundle())
        } else {
            when (commandRequestSearchBean.type) {
                SEARCH_HOME -> {
                    toast.showToast(R.string.sv_search_please_enter_home)
                }

                SEARCH_COMPANY -> {
                    toast.showToast(R.string.sv_search_please_enter_company)
                }
            }
        }
    }

    private fun doCollection(@CommandRequestSearchBean.Type type: Int, poi: POI) {
        val isFavorite = viewModel.isFavorited(poi)
        when (type) {
            SEARCH_HOME -> {
                toast.showToast(R.string.sv_search_adding_home)
                if (isFavorite) {
                    dismissCustomDialog()
                    favoriteDialog = CustomDialogFragment.builder().setCloseButton(true).setContent(getString(R.string.sv_search_has_favorite) + "家")
                        .setOnClickListener {
                            if (it) {
                                val isSuccess = viewModel.addHome(poi, true);
                                if (isSuccess) {
                                    toast.showToast(R.string.sv_search_add_home_success, com.desaysv.psmap.model.R.drawable.ic_toast_complete_day)
                                    findNavController().navigateUp()
                                } else {
                                    toast.showToast(R.string.sv_search_add_home_error)
                                }
                            }
                        }.apply {
                            this@SearchAddHomeFragment.childFragmentManager
                                .beginTransaction()
                                .add(this, "favoriteDialog")
                                .commitAllowingStateLoss()
                        }
                } else {
                    val isSuccess = viewModel.addHome(poi, false);
                    if (isSuccess) {
                        toast.showToast(R.string.sv_search_add_home_success, com.desaysv.psmap.model.R.drawable.ic_toast_complete_day)
                        findNavController().navigateUp()
                    } else {
                        toast.showToast(R.string.sv_search_add_home_error)
                    }
                }
            }

            SEARCH_COMPANY -> {
                toast.showToast(R.string.sv_search_adding_company)
                if (isFavorite) {
                    dismissCustomDialog()
                    favoriteDialog = CustomDialogFragment.builder().setCloseButton(true).setContent(getString(R.string.sv_search_has_favorite) + "公司")
                        .setOnClickListener {
                            if (it) {
                                val isSuccess = viewModel.addCompany(poi, true);
                                if (isSuccess) {
                                    toast.showToast(R.string.sv_search_add_company_success, com.desaysv.psmap.model.R.drawable.ic_toast_complete_day)
                                    findNavController().navigateUp()
                                } else {
                                    toast.showToast(R.string.sv_search_add_company_error)
                                }
                            }
                        }.apply {
                            this@SearchAddHomeFragment.childFragmentManager
                                .beginTransaction()
                                .add(this, "favoriteDialog")
                                .commitAllowingStateLoss()
                        }
                } else {
                    val isSuccess = viewModel.addCompany(poi, false);
                    if (isSuccess) {
                        toast.showToast(R.string.sv_search_add_company_success, com.desaysv.psmap.model.R.drawable.ic_toast_complete_day)
                        findNavController().navigateUp()
                    } else {
                        toast.showToast(R.string.sv_search_add_company_error)
                    }
                }

            }

            SEARCH_TEAM_DESTINATION -> {
                viewModel.addDestination(poi)
                findNavController().navigateUp()
            }
        }
    }

    //根据传入的type设置提示文字
    private fun showTips() {
        commandRequestSearchBean.let {
            Timber.i("commandRequestSearchBean.type = ${commandRequestSearchBean.type}")
            when (it.type) {
                SEARCH_HOME -> {
                    Timber.i("commandRequestSearchBean 家")
                    binding.searchBox.setHint(getString(R.string.sv_search_please_enter_home))
                }

                SEARCH_COMPANY -> {
                    Timber.i("commandRequestSearchBean 公司")
                    binding.searchBox.setHint(getString(R.string.sv_search_please_enter_company))
                }

                SEARCH_TEAM_DESTINATION -> {
                    Timber.i("commandRequestSearchBean 组队")
                    binding.searchBox.setHint(getString(R.string.sv_search_please_enter_team_destination))
                }
            }
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

    override fun onPause() {
        super.onPause()
        Timber.i("onPause()")
        KeyboardUtil.hideKeyboard(view)
    }

    override fun onStop() {
        super.onStop()
        Timber.i("onStop()")
        dismissCustomDialog()
    }


    override fun onDestroyView() {
        Timber.i("onDestroyView()")
        super.onDestroyView()
    }

    override fun onDestroy() {
        Timber.i("onDestroy()")
        super.onDestroy()
    }

    fun toPoiDetailFragment(@CommandRequestSearchBean.Type type: Int, poi: POI?) {
        lifecycleScope.launch {
            poi?.let {
                viewModel.getDisTime(poi)
                viewModel.resetShowPoi(MapPointCardData(PoiCardType.TYPE_POI, poi))
            }
            val poiCardBeanType = when (type) {
                SEARCH_HOME -> POI_CARD_SEARCH_HOME

                SEARCH_COMPANY -> POI_CARD_SEARCH_COMPANY

                SEARCH_TEAM_DESTINATION -> POI_CARD_SEARCH_TEAM_DESTINATION

                else -> POI_CARD_SEARCH_HOME
            }
            val commandRequestPOICardBean = CommandRequestPOICardBean.Builder().setType(poiCardBeanType).build()
            findNavController().navigate(R.id.to_MapPointDataFragment, commandRequestPOICardBean.toBundle())
        }
    }
}