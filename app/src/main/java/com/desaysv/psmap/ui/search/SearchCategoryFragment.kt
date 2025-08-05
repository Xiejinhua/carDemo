package com.desaysv.psmap.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.autosdk.view.KeyboardUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.tracking.EventTrackingUtils
import com.desaysv.psmap.databinding.FragmentSearchCategoryBinding
import com.desaysv.psmap.model.bean.CommandRequestSearchBean
import com.desaysv.psmap.model.bean.CommandRequestSearchBean.Type.Companion.SEARCH_TEAM_DESTINATION
import com.desaysv.psmap.model.bean.CommandRequestSearchCategoryBean
import com.desaysv.psmap.model.bean.CommandRequestSearchCategoryBean.Type.Companion.SEARCH_ALONG_WAY
import com.desaysv.psmap.model.bean.CommandRequestSearchCategoryBean.Type.Companion.SEARCH_AROUND
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.SearchCategoryAdapter
import com.desaysv.psmap.ui.search.view.SearchCategoryView.OnItemClickListener
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 张楠
 * @time 2024/2/22
 * @description
 */
@AndroidEntryPoint
class SearchCategoryFragment : Fragment() {
    private lateinit var binding: FragmentSearchCategoryBinding
    private lateinit var searchCategoryAdapter: SearchCategoryAdapter
    private var commandRequestSearchBean: CommandRequestSearchCategoryBean? = null

    @Inject
    lateinit var toast: ToastUtil

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var carInfoProxy: ICarInfoProxy
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.i("onCreateView()")
        binding = FragmentSearchCategoryBinding.inflate(inflater, container, false)
        commandRequestSearchBean = requireArguments().getParcelable(Biz.KEY_BIZ_SEARCH_CATEGORY_LIST)
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
    }

    private fun initView() {

        binding.searchCategoryBack.setDebouncedOnClickListener {
            findNavController().navigateUp()
        }
        binding.searchTripCategory.init("出游", SearchCategoryConstants.ALONG_WAY_CATEGORY_TRIP_LIST, object : OnItemClickListener {
            override fun onItemClick(name: String) {
                commandRequestSearchBean?.let {
                    when (it.type) {
                        SEARCH_AROUND -> {
                            val commandBean = CommandRequestSearchBean.Builder()
                                .setKeyword(name)
                                .setCity(commandRequestSearchBean?.city)
                                .setType(CommandRequestSearchBean.Type.SEARCH_AROUND)
                                .setPoi(commandRequestSearchBean?.poi)
                                .build()
                            findNavController().navigate(R.id.to_searchResultFragment, commandBean.toBundle())
                            EventTrackingUtils.trackEvent(
                                EventTrackingUtils.EventName.SurroundSearch_Click,
                                Pair(EventTrackingUtils.EventValueName.SearchCategory, name)
                            )
                        }

                        SEARCH_ALONG_WAY -> {
                            it.keyword = name
                            NavHostFragment.findNavController(this@SearchCategoryFragment)
                                .navigate(R.id.to_searchAlongWayResultFragment, it.toBundle())
                            EventTrackingUtils.trackEvent(
                                EventTrackingUtils.EventName.SurroundSearch_Click,
                                Pair(EventTrackingUtils.EventValueName.SearchCategory, name)
                            )
                        }

                        SEARCH_TEAM_DESTINATION -> {
                            val commandBean = CommandRequestSearchBean.Builder()
                                .setKeyword(name)
                                .setType(SEARCH_TEAM_DESTINATION)
                                .build()
                            findNavController().navigate(R.id.to_searchResultFragment, commandBean.toBundle())
                        }
                    }
                }
            }

        })
        binding.searchFoodCategory.init("餐饮", SearchCategoryConstants.AROUND_CATEGORY_FOOD_LIST, object : OnItemClickListener {
            override fun onItemClick(name: String) {
                commandRequestSearchBean?.let {
                    when (it.type) {
                        SEARCH_AROUND -> {
                            val commandBean = CommandRequestSearchBean.Builder()
                                .setKeyword(name)
                                .setCity(commandRequestSearchBean?.city)
                                .setType(CommandRequestSearchBean.Type.SEARCH_AROUND)
                                .setPoi(commandRequestSearchBean?.poi)
                                .build()
                            findNavController().navigate(R.id.to_searchResultFragment, commandBean.toBundle())
                            EventTrackingUtils.trackEvent(
                                EventTrackingUtils.EventName.SurroundSearch_Click,
                                Pair(EventTrackingUtils.EventValueName.SearchCategory, name)
                            )
                        }

                        SEARCH_ALONG_WAY -> {
                            it.keyword = name
                            NavHostFragment.findNavController(this@SearchCategoryFragment)
                                .navigate(R.id.to_searchAlongWayResultFragment, it.toBundle())
                            EventTrackingUtils.trackEvent(
                                EventTrackingUtils.EventName.SurroundSearch_Click,
                                Pair(EventTrackingUtils.EventValueName.SearchCategory, name)
                            )
                        }

                        SEARCH_TEAM_DESTINATION -> {
                            val commandBean = CommandRequestSearchBean.Builder()
                                .setKeyword(name)
                                .setType(SEARCH_TEAM_DESTINATION)
                                .build()
                            findNavController().navigate(R.id.to_searchResultFragment, commandBean.toBundle())
                        }
                    }
                }
            }

        })
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
        //周边分类搜索Adapter
        searchCategoryAdapter = SearchCategoryAdapter().also { adapter ->
            adapter.setOnItemClickListener(object : SearchCategoryAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    commandRequestSearchBean?.let {
                        when (it.type) {
                            SEARCH_AROUND -> {
                                val commandBean = CommandRequestSearchBean.Builder()
                                    .setKeyword(adapter.data[position].name)
                                    .setCity(commandRequestSearchBean?.city)
                                    .setType(CommandRequestSearchBean.Type.SEARCH_AROUND)
                                    .setPoi(commandRequestSearchBean?.poi)
                                    .build()
                                findNavController().navigate(R.id.to_searchResultFragment, commandBean.toBundle())
                                EventTrackingUtils.trackEvent(
                                    EventTrackingUtils.EventName.SurroundSearch_Click,
                                    Pair(EventTrackingUtils.EventValueName.SearchCategory, adapter.data[position].name)
                                )
                            }

                            SEARCH_ALONG_WAY -> {
                                it.keyword = adapter.data[position].name
                                NavHostFragment.findNavController(this@SearchCategoryFragment)
                                    .navigate(R.id.to_searchAlongWayResultFragment, it.toBundle())
                                EventTrackingUtils.trackEvent(
                                    EventTrackingUtils.EventName.SurroundSearch_Click,
                                    Pair(EventTrackingUtils.EventValueName.SearchCategory, adapter.data[position].name)
                                )
                            }

                            SEARCH_TEAM_DESTINATION -> {
                                val commandBean = CommandRequestSearchBean.Builder()
                                    .setKeyword(adapter.data[position].name)
                                    .setType(SEARCH_TEAM_DESTINATION)
                                    .build()
                                findNavController().navigate(R.id.to_searchResultFragment, commandBean.toBundle())
                            }
                        }
                    }
                }
            })
        }
        binding.searchCommonCategoryListview.adapter = searchCategoryAdapter

    }

    private fun initData() {
        val list = if (carInfoProxy.isT1JFL2ICE()) {
            SearchCategoryConstants.AROUND_CATEGORY_LIST.filter { it.name != "充电站" }
        } else {
            SearchCategoryConstants.AROUND_CATEGORY_LIST
        }
        commandRequestSearchBean?.let {
            when (it.type) {
                SEARCH_AROUND -> searchCategoryAdapter.updateData(list)
                SEARCH_ALONG_WAY -> searchCategoryAdapter.updateData(list)
                SEARCH_TEAM_DESTINATION -> searchCategoryAdapter.updateData(list)
            }
        }

        //日夜模式监听
        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            searchCategoryAdapter.notifyDataSetChanged()
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

    override fun onDestroy() {
        Timber.i("onDestroy()")
        super.onDestroy()
    }
}

