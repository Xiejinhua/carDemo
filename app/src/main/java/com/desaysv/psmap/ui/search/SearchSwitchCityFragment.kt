package com.desaysv.psmap.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.data.model.CityItemInfo
import com.autosdk.view.KeyboardUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.impl.AhaTripImpl
import com.desaysv.psmap.databinding.FragmentSearchSwitchCityBinding
import com.desaysv.psmap.model.bean.CommandRequestSearchBean
import com.desaysv.psmap.model.business.AhaTripBusiness
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.model.utils.CityUtil
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.ui.adapter.SearchCityAdapter
import com.desaysv.psmap.ui.adapter.SearchCityCategoryAdapter
import com.desaysv.psmap.ui.adapter.SearchCityCategoryAdapter.OnCityClickListener
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 张楠
 * @time 2024/2/22
 * @description
 */
@AndroidEntryPoint
class SearchSwitchCityFragment : Fragment() {
    private lateinit var binding: FragmentSearchSwitchCityBinding
    private lateinit var searchCityCategoryAdapter: SearchCityCategoryAdapter
    private lateinit var searchCityAdapter: SearchCityAdapter
    private val viewModel by viewModels<SearchSwitchCityModel>()

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var toast: ToastUtil

    @Inject
    lateinit var ahaTripBusiness: AhaTripBusiness

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.i("onCreateView()")
        binding = FragmentSearchSwitchCityBinding.inflate(inflater, container, false)
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
        binding.searchBox.setHint("请输入省份或城市名称")
        binding.searchBox.editOnTouchListener { v, event ->
            if (event?.action == MotionEvent.ACTION_UP) {
                requireActivity().window.setLocalFocus(true, true)
                binding.searchBox.hideKeyboard(false)
            } else if (event?.action == MotionEvent.ACTION_DOWN) {
                requireActivity().window.setLocalFocus(true, true)
            }
            false
        }
        binding.searchBox.backOnClickListener {
            findNavController().navigateUp()
        }
        binding.searchBox.showCityBtn(false)
        //搜索框文字改变时发起城市搜索
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

        //日夜模式监听
        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            binding.searchBox.setNight(it == true)
            searchCityCategoryAdapter.notifyDataSetChanged()
            searchCityAdapter.notifyDataSetChanged()
        }
    }

    private fun initAdapter() {
        searchCityCategoryAdapter = SearchCityCategoryAdapter().also {
            it.setOnCityClickListener(object : OnCityClickListener {

                override fun OnCityClick(cityItemInfo: CityItemInfo) {
                    if (!CityUtil.isProvince(cityItemInfo.cityAdcode)) {
                        //保存用户选择过的城市
                        viewModel.saveCommonCity(cityItemInfo)
                    }
                    //跳转至离线搜索页
                    val commandBean = CommandRequestSearchBean().also { bean ->
                        bean.city = cityItemInfo
                    }
                    if (viewModel.getSearchBean()?.type == CommandRequestSearchBean.Type.SEARCH_TRIP_CITY){
                        ahaTripBusiness.setCityItemInfo(cityItemInfo)
                        findNavController().navigateUp()
                    } else {
                        toast.showToast("已切换至${cityItemInfo.cityName}", com.desaysv.psmap.model.R.drawable.ic_toast_complete_day)
                        NavHostFragment.findNavController(this@SearchSwitchCityFragment).navigate(R.id.action_searchSwitchCityFragment_to_searchFragment, commandBean.toBundle())
                    }
                }

                override fun OnArrowClick(position: Int) {
                    it.setSelection(position)
                    if (it.selectPos != -1) {
                        binding.searchCityCategoryListview.post {
                            (binding.searchCityCategoryListview.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 0)
                        }
                    }
                }

            })
        }
        binding.searchCityCategoryListview.adapter = searchCityCategoryAdapter

        searchCityAdapter = SearchCityAdapter().also {
            it.setOnCityClickListener(object : SearchCityAdapter.OnItemClickListener {
                override fun onItemClick(cityItemInfo: CityItemInfo) {
                    //先保存用户选择过的城市
                    viewModel.saveCommonCity(cityItemInfo)
                    //跳转至离线搜索页
                    val commandBean = CommandRequestSearchBean().also { bean ->
                        bean.city = cityItemInfo
                    }
                    if (viewModel.getSearchBean()?.type == CommandRequestSearchBean.Type.SEARCH_TRIP_CITY){
                        ahaTripBusiness.setCityItemInfo(cityItemInfo)
                        findNavController().navigateUp()
                    } else {
                        toast.showToast("已切换至${cityItemInfo.cityName}", com.desaysv.psmap.model.R.drawable.ic_toast_complete_day)
                        NavHostFragment.findNavController(this@SearchSwitchCityFragment).navigate(R.id.action_searchSwitchCityFragment_to_searchFragment, commandBean.toBundle())
                    }
                }

            })
        }
        binding.searchCityListview.layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        binding.searchCityListview.adapter = searchCityAdapter

    }

    private fun initData() {
        searchCityCategoryAdapter.setList(viewModel.getAllCityData())
        viewModel.searchCityListLiveData.observe(viewLifecycleOwner) {
            searchCityAdapter.setList(it)
        }
        viewModel.buttonType.observe(viewLifecycleOwner) {
            binding.searchBox.showDeleteBtn(it == 1)
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