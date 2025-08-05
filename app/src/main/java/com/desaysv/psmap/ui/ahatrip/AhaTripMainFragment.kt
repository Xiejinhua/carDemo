package com.desaysv.psmap.ui.ahatrip

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.RadioButton
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.autonavi.auto.skin.NightModeGlobal
import com.autosdk.view.KeyboardUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.tracking.EventTrackingUtils
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentAhaTripMainBinding
import com.desaysv.psmap.model.bean.CommandRequestSearchBean
import com.desaysv.psmap.model.bean.CommandRequestSearchBean.Type.Companion.SEARCH_CUSTOM_AHA_TRIP
import com.desaysv.psmap.model.business.AhaTripBusiness
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.AhaTripHomeAdapter
import com.example.aha_api_sdkd01.manger.models.LineListModel
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 路书首页
 */
@AndroidEntryPoint
class AhaTripMainFragment : Fragment() {
    private lateinit var binding: FragmentAhaTripMainBinding
    private val viewModel: AhaTripMainViewModel by viewModels()
    private var lastTargetX = 0
    private var isFirst = true
    private var adapter: AhaTripHomeAdapter? = null
    private var commandRequestSearchBean: CommandRequestSearchBean? = null

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var ahaTripBusiness: AhaTripBusiness

    @Inject
    lateinit var toast: ToastUtil

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAhaTripMainBinding.inflate(inflater, container, false)
        commandRequestSearchBean = requireArguments().getParcelable(Biz.KEY_BIZ_SEARCH_REQUEST)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initEventOperation()
        initData()
    }

    private fun initData() {
        commandRequestSearchBean?.let { commandRequestSearchBean ->
            if (commandRequestSearchBean.type == SEARCH_CUSTOM_AHA_TRIP) {
                if ((commandRequestSearchBean.keyword.equals("行程列表") || commandRequestSearchBean.keyword.equals("收藏"))) {
                    if (viewModel.isLogin()) {
                        val bundle =
                            CommandRequestSearchBean.Builder().setKeyword(commandRequestSearchBean.keyword).setType(commandRequestSearchBean.type)
                                .build()
                                .toBundle()
                        findNavController().navigate(R.id.to_myAhaMainFragment, bundle)
                    } else {
                        viewModel.registerLogin()
                    }
                } else {
                    commandRequestSearchBean.city?.let {
                        ahaTripBusiness.setCityItemInfo(it)
                    }
                }
            }
        }
        //将关键字设置为空，防止第二次进入时，又重新跳转
        commandRequestSearchBean?.keyword = ""
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter = null
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.searchBox.setNight(NightModeGlobal.isNightMode())
        binding.searchBox.setHint(getString(R.string.sv_custom_trip_point_name))
        binding.searchBox.showTripCityBtn(true)
        adapter = AhaTripHomeAdapter().also { binding.tripList.adapter = it }
        binding.refreshLayout.setEnableRefresh(false)
        binding.refreshLayout.setEnableAutoLoadMore(true)
        binding.refreshLayout.setDisableContentWhenLoading(true)
        binding.refreshLayout.setIsTrip(2)
        comeInRequestLineList() //进入界面加载详情数据
        KeyboardUtil.hideKeyboard(view)
    }

    //进入界面加载详情数据
    private fun comeInRequestLineList() {
        Timber.d(" initBinding selectTab:%s", viewModel.selectTab.value)
        if (viewModel.selectTab.value == true) {
            binding.layoutTab.check(R.id.rb_day)
            if (isFirst) {
                isFirst = false
                viewModel.getCityItemInfo()?.let {
                    binding.refreshLayout.resetNoMoreData()
                    viewModel.lineCurrentPage = 1
                    viewModel.lineAllList.postValue(arrayListOf())
                    binding.tripList.scrollToPosition(0)
                    viewModel.scrollToPosition.postValue(0)
                    viewModel.requestLineList(
                        viewModel.selectType.value ?: BaseConstant.AHA_TRIP_SORT_DAY,
                        it,
                        commandRequestSearchBean?.keyword ?: "",
                        commandRequestSearchBean?.day ?: ""
                    )
                    commandRequestSearchBean?.keyword?.let { keyword ->
                        binding.searchBox.setText(keyword)
                    }
                } //请求路书列表
            } else if (!TextUtils.equals(viewModel.getTripCityName(), viewModel.getCityItemInfo()?.cityName)) {
                viewModel.getCityItemInfo()?.let {
                    binding.refreshLayout.resetNoMoreData()
                    viewModel.lineCurrentPage = 1
                    viewModel.lineAllList.postValue(arrayListOf())
                    binding.tripList.scrollToPosition(0)
                    viewModel.scrollToPosition.postValue(0)
                    viewModel.requestLineList(
                        viewModel.selectType.value ?: BaseConstant.AHA_TRIP_SORT_DAY,
                        it,
                        viewModel.inputKeyWord.value ?: "",
                        ""
                    ) //请求路书列表
                } //请求路书列表
            } else {
                adapter?.onRefreshData(viewModel.lineAllList.value, viewModel.selectTab.value ?: true, viewModel.inputKeyWord.value ?: "", viewModel.lineCurrentPage == (viewModel.lineMaxPage.value ?: 1))
                binding.tripList.scrollToPosition( viewModel.scrollToPosition.value ?: 0)
            }
        } else if (!TextUtils.equals(viewModel.getTripCityName(), viewModel.getCityItemInfo()?.cityName)) {
            viewModel.getCityItemInfo()?.let {
                binding.refreshLayout.resetNoMoreData()
                viewModel.lineCurrentPage = 1
                viewModel.lineAllList.postValue(arrayListOf())
                binding.tripList.scrollToPosition(0)
                viewModel.scrollToPosition.postValue(0)
                viewModel.requestLineList(
                    viewModel.selectType.value ?: BaseConstant.AHA_TRIP_SORT_DAY,
                    it,
                    viewModel.inputKeyWord.value ?: "",
                    ""
                ) //请求路书列表
            } //请求路书列表
        } else {
            adapter?.onRefreshData(viewModel.lineAllList.value, viewModel.selectTab.value ?: true, viewModel.inputKeyWord.value ?: "", viewModel.lineCurrentPage == (viewModel.lineMaxPage.value ?: 1))
            binding.tripList.scrollToPosition( viewModel.scrollToPosition.value ?: 0)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initEventOperation() {
        //点击返回按钮，退出该界面
        binding.back.setDebouncedOnClickListener {
            findNavController().navigateUp()
        }
        ViewClickEffectUtils.addClickScale(binding.back, CLICKED_SCALE_90)

        binding.searchBox.editOnTouchListener { _, event ->
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
                val keyword = s.toString().trim()
                Timber.d("s.toString() = $keyword isLastShowSearchBoxEmpty:${viewModel.getLastShowSearchBoxEmpty()}")
                viewModel.onInputKeywordChanged(keyword)
                if (TextUtils.isEmpty(keyword) && !viewModel.getLastShowSearchBoxEmpty()) {
                    viewModel.getCityItemInfo()?.let {
                        binding.refreshLayout.resetNoMoreData()
                        viewModel.lineCurrentPage = 1
                        viewModel.lineAllList.postValue(arrayListOf())
                        binding.tripList.scrollToPosition(0)
                        viewModel.scrollToPosition.postValue(0)
                        viewModel.requestLineList(viewModel.selectType.value ?: BaseConstant.AHA_TRIP_SORT_DAY, it, "","")
                    } //请求路书列表
                }
            }
        })

        //搜索按钮
        binding.searchBox.searchOnClickListener {
            doSearchTrip(viewModel.inputKeyWord.value) //路书搜索
        }

        //离线城市选择按钮
        binding.searchBox.switchTripCityOnClickListener {
            val commandBean = CommandRequestSearchBean()
            commandBean.city = viewModel.getCityItemInfo()
            commandBean.type = CommandRequestSearchBean.Type.SEARCH_TRIP_CITY
            NavHostFragment.findNavController(this@AhaTripMainFragment).navigate(R.id.to_searchSwitchCityFragment, commandBean.toBundle())
        }

        binding.searchBox.setOnEditorActionListener { _, actionId, _ ->
            if (actionId >= EditorInfo.IME_ACTION_GO && actionId <= EditorInfo.IME_ACTION_DONE) {
                Timber.i("setOnEditorActionListener")
                doSearchTrip(viewModel.inputKeyWord.value) //路书搜索
                true
            } else {
                false
            }
        }

        //用户头像点击
        binding.userImage.setDebouncedOnClickListener {
            if (viewModel.isLogin()) {
                findNavController().navigate(R.id.to_myAhaMainFragment, commandRequestSearchBean?.toBundle())
            } else {
                viewModel.registerLogin()
            }
        }
        ViewClickEffectUtils.addClickScale(binding.userImage, CLICKED_SCALE_90)

        binding.layoutTab.setOnCheckedChangeListener { group, checkedId ->
            lifecycleScope.launch {
                val checkedButton: RadioButton = group.findViewById(checkedId)
                // 计算指示条应该移动到的位置
                val targetX: Int = checkedButton.left + (checkedButton.width - binding.indicator.width) / 2
                Timber.i("layoutTab checkedId:$checkedId targetX:$targetX lastTargetX:$lastTargetX")
                // 创建平移动画
                if (targetX == 0 && checkedId != R.id.rb_day) {
                    binding.indicator.animate()
                        .x(lastTargetX.toFloat())
                        .setDuration(0)
                        .setInterpolator(FastOutSlowInInterpolator())
                        .start()
                } else {
                    binding.indicator.animate()
                        .x(targetX.toFloat())
                        .setDuration(200)
                        .setInterpolator(FastOutSlowInInterpolator())
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                lastTargetX = targetX
                                when (checkedId) {
                                    R.id.rb_day -> {
                                        viewModel.selectTab.postValue(true)
                                        viewModel.getCityItemInfo()?.let {
                                            binding.refreshLayout.resetNoMoreData()
                                            viewModel.lineCurrentPage = 1
                                            viewModel.lineAllList.postValue(arrayListOf())
                                            binding.tripList.scrollToPosition(0)
                                            viewModel.scrollToPosition.postValue(0)
                                            viewModel.requestLineList(BaseConstant.AHA_TRIP_SORT_DAY, it, viewModel.inputKeyWord.value ?: "","")
                                        } //请求路书列表
                                    }

                                    R.id.rb_score -> {
                                        viewModel.selectTab.postValue(false)
                                        viewModel.getCityItemInfo()?.let {
                                            binding.refreshLayout.resetNoMoreData()
                                            viewModel.lineCurrentPage = 1
                                            viewModel.lineAllList.postValue(arrayListOf())
                                            binding.tripList.scrollToPosition(0)
                                            viewModel.scrollToPosition.postValue(0)
                                            viewModel.requestLineList(BaseConstant.AHA_TRIP_SORT_SCORE, it, viewModel.inputKeyWord.value ?: "","")
                                        } //请求路书列表
                                    }
                                }
                            }
                        })
                        .start()
                }
            }
        }

        adapter?.setOnTripClickListener(object : AhaTripHomeAdapter.OnTripClickListener {
            override fun onItemClick(item: LineListModel.DataDTO.ListDTO?) {//路书详情
                viewModel.setIsLineFav(false)
                findNavController().navigate(R.id.to_ahaTripDetailFragment, Bundle().apply {
                    putInt("id", item?.id ?: 0)
                    putInt("totalDay", item?.totalDay ?: 0)
                    putInt("position", 0)
                    putBoolean("isMineFav", false)
                })
            }

            override fun onOpenAhaClick() {
                viewModel.goAhaHome() //跳转到路书APP
            }
        })

        //列表加载更多
        binding.refreshLayout.setOnRefreshLoadMoreListener(
            object : OnRefreshLoadMoreListener {
                override fun onRefresh(refreshLayout: RefreshLayout) {
                    //不作处理
                }

                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    if (viewModel.lineCurrentPage < (viewModel.lineMaxPage.value ?: 1)) {
                        viewModel.lineCurrentPage += 1
                        viewModel.getCityItemInfo()?.let {
                            viewModel.requestLineList(viewModel.selectType.value ?: BaseConstant.AHA_TRIP_SORT_DAY, it, viewModel.inputKeyWord.value ?: "", commandRequestSearchBean?.day ?: "") //请求路书列表
                        } //请求路书列表
                    } else { //最后一页了
                        binding.refreshLayout.finishLoadMore()//结束加载
                    }
                }
            })

        viewModel.getCityItemInfo().let { cityItemInfo ->
            //设置离线城市
            viewModel.setTripCityName(cityItemInfo?.cityName ?: "")
            binding.searchBox.setTripCityName(cityItemInfo?.cityName ?: "")
        }

        viewModel.buttonType.observe(viewLifecycleOwner) {
            binding.searchBox.showLoadingBtn(false)
            binding.searchBox.showDeleteBtn(it == 1)
        }

        viewModel.setToast.unPeek().observe(viewLifecycleOwner) {
            toast.showToast(it)
        }

        //更新路书列表数据
        viewModel.lineList.unPeek().observe(viewLifecycleOwner) {
            Timber.i("lineList:${it.size} lineCurrentPage:${viewModel.lineCurrentPage} lineMaxPage:${viewModel.lineMaxPage.value}")
            if (viewModel.lineCurrentPage == 1){
                adapter?.onRefreshData(it, viewModel.selectTab.value ?: true, viewModel.inputKeyWord.value ?: "", viewModel.lineCurrentPage == (viewModel.lineMaxPage.value ?: 1))
                if (it.isNotEmpty()){
                    binding.tripList.scrollToPosition(0)
                    viewModel.scrollToPosition.postValue(0)
                    viewModel.lineAllList.postValue(it)
                }else {
                    viewModel.lineAllList.postValue(arrayListOf())
                }
            } else {
                if (it.isNotEmpty()){
                    val lastSize = adapter?.data?.size ?: 0
                    val data = adapter?.data ?: arrayListOf()
                    data.addAll(it)
                    adapter?.onRefreshData(data as List<LineListModel.DataDTO.ListDTO>, viewModel.selectTab.value ?: true, viewModel.inputKeyWord.value ?: "", viewModel.lineCurrentPage == (viewModel.lineMaxPage.value ?: 1))
                    binding.tripList.scrollToPosition(lastSize)
                    viewModel.scrollToPosition.postValue(lastSize)
                    viewModel.lineAllList.postValue(data as List<LineListModel.DataDTO.ListDTO>)
                } else {
                    viewModel.lineCurrentPage -= 1
                    val size = adapter?.data?.size ?: 0
                    val lastSize = if(size > 0 ) size - 1 else 0
                    binding.tripList.scrollToPosition(lastSize)
                    viewModel.scrollToPosition.postValue(lastSize)
                    viewModel.lineAllList.postValue(viewModel.lineAllList.value)
                }
            }
            if (viewModel.lineCurrentPage < (viewModel.lineMaxPage.value ?: 1)) {
                binding.refreshLayout.finishLoadMore()
            } else { //最后一页了
                binding.refreshLayout.finishLoadMore()
                binding.refreshLayout.finishLoadMoreWithNoMoreData()
                binding.refreshLayout.setIsTrip(2)
            }
        }

        //日夜模式监听
        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            binding.searchBox.setNight(it == true)
            adapter?.notifyDataSetChanged()
            binding.refreshLayout.setNight(it == true)
        }
    }

    //路书搜索
    private fun doSearchTrip(keyword: String?) {
        if (!TextUtils.isEmpty(keyword)) {
            KeyboardUtil.hideKeyboard(view)
            viewModel.getCityItemInfo()?.let {
                binding.refreshLayout.resetNoMoreData()
                viewModel.lineCurrentPage = 1
                viewModel.lineAllList.postValue(arrayListOf())
                binding.tripList.scrollToPosition(0)
                viewModel.scrollToPosition.postValue(0)
                viewModel.requestLineList(viewModel.selectType.value ?: BaseConstant.AHA_TRIP_SORT_DAY, it, keyword!!,"")
            } //请求路书列表
            EventTrackingUtils.trackEvent(
                EventTrackingUtils.EventName.RoadBookSearch_Click,
                mapOf(
                    Pair(EventTrackingUtils.EventValueName.SearchTime, System.currentTimeMillis())
                )
            )
        } else {
            viewModel.getCityItemInfo()?.let {
                binding.refreshLayout.resetNoMoreData()
                viewModel.lineCurrentPage = 1
                viewModel.lineAllList.postValue(arrayListOf())
                binding.tripList.scrollToPosition(0)
                viewModel.scrollToPosition.postValue(0)
                viewModel.requestLineList(viewModel.selectType.value ?: BaseConstant.AHA_TRIP_SORT_DAY, it, "","")
            } //请求路书列表
        }
    }
}