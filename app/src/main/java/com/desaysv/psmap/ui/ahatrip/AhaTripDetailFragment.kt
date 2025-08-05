package com.desaysv.psmap.ui.ahatrip

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.auto.skin.view.SkinRadioButton
import com.autonavi.gbl.layer.model.BizCustomTypeLine
import com.autonavi.gbl.layer.model.BizCustomTypePoint
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.common.POIFactory
import com.autosdk.view.KeyboardUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.business.SpeechSynthesizeBusiness
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentAhaTripDetailBinding
import com.desaysv.psmap.model.banner.adapter.BannerImageAdapter
import com.desaysv.psmap.model.banner.holder.BannerImageHolder
import com.desaysv.psmap.model.banner.indicator.CircleIndicator
import com.desaysv.psmap.model.bean.AhaTripDetailTabBean
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.AhaLineDetailDayAdapter
import com.example.aha_api_sdkd01.manger.models.LineDetailModel
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.abs

/**
 * 路书详情
 */
@AndroidEntryPoint
class AhaTripDetailFragment : Fragment() {
    private lateinit var binding: FragmentAhaTripDetailBinding
    private val viewModel: AhaTripDetailViewModel by viewModels()

    private var lastCheckedId: Int? = null // 记录上一个选中的 RadioButton ID
    private var lastTargetX = 0
    private var isAnimationRunning = false
    private val tabItems = ArrayList<AhaTripDetailTabBean>()
    private var ahaLineDetailDayAdapter: AhaLineDetailDayAdapter? = null
    private var position = 0 //对应的位置
    private var isMineFav = false //是否是路书收藏进来的

    @Inject
    lateinit var mSpeechSynthesizeBusiness: SpeechSynthesizeBusiness

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var toast: ToastUtil

    override fun onResume() {
        super.onResume()
        if (viewModel.selectTab.value == 0){
            viewModel.setLinePointLine() //简介图层扎标和画线
        } else if (viewModel.selectTab.value!! > 0){
            viewModel.setLineDayNodePointLine(viewModel.selectTab.value!!) //DAY几图层扎标和画线
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.removeAllLayerItems(BizCustomTypeLine.BizCustomTypeLine3.toLong())
        viewModel.removeAllLayerItems(BizCustomTypePoint.BizCustomTypePoint3.toLong())
        viewModel.removeAllLayerItems(BizCustomTypePoint.BizCustomTypePoint2.toLong())
        viewModel.exitPreview()
    }

    override fun onDestroy() {
        super.onDestroy()
        ahaLineDetailDayAdapter = null
        viewModel.lineDetail.postValue(null)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAhaTripDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initEventOperation()
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        viewModel.setLineId(arguments?.getInt("id", 0).toString())
        viewModel.totalDay.postValue((arguments?.getInt("totalDay", 0) ?: 0).toString() + "天")
        isMineFav = arguments?.getBoolean("isMineFav", false) ?: false
        position = arguments?.getInt("position", 0) ?: 0
        if (viewModel.lineDetail.value == null){
            viewModel.requestLineDetail() //单条路书详情
        } else {
            setRadioGroupLayout(viewModel.lineDetail.value) //设置RadioGroup布局，实现简介，dayXX的切换
            Timber.d(" initBinding selectTab:%s", viewModel.selectTab.value )
            binding.layoutTab.check(tabItems[viewModel.selectTab.value!!].viewId)
            binding.indicator.animate()
                .x(lastTargetX.toFloat())
                .setDuration(0)
                .setInterpolator(FastOutSlowInInterpolator())
                .start()

            if (viewModel.lineDetail.value != null) {
                setCarousel(viewModel.lineDetail.value!!.swiper) //设置轮播图
            }
        }
        binding.banner.addBannerLifecycleObserver(this)//添加生命周期观察者
            .setIndicator(CircleIndicator(requireContext()), false)
        ahaLineDetailDayAdapter = AhaLineDetailDayAdapter().also { binding.nodeList.adapter = it }
        KeyboardUtil.hideKeyboard(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initEventOperation() {
        //退出该界面
        binding.back.setDebouncedOnClickListener {
            mSpeechSynthesizeBusiness.nowStopPlay() //立即停止播报
            findNavController().navigateUp()
        }
        ViewClickEffectUtils.addClickScale(binding.back, CLICKED_SCALE_90)

        //播报
        binding.sound.setDebouncedOnClickListener {
            viewModel.lineDetail.value?.description?.let {
                if (!mSpeechSynthesizeBusiness.isPlaying()){
                    mSpeechSynthesizeBusiness.synthesize(it, false)
                }
            }
        }
        ViewClickEffectUtils.addClickScale(binding.sound, CLICKED_SCALE_90)

        binding.layoutTab.setOnCheckedChangeListener { group, checkedId ->
            // 如果动画正在运行且目标 Tab 不同，则取消当前动画
            if (isAnimationRunning && lastCheckedId != checkedId) {
                binding.indicator.animate().setListener(null).cancel()
                isAnimationRunning = false
            }
            isAnimationRunning = true

            lifecycleScope.launch {
                val checkedButton: RadioButton = group.findViewById(checkedId) ?: return@launch
                // 计算指示条应该移动到的位置
                val targetX: Int = checkedButton.left + (checkedButton.width - binding.indicator.width) / 2
                Timber.i("layoutTab checkedId:$checkedId targetX:$targetX lastTargetX:$lastTargetX")
                // 判断动画持续时间
                val duration = if (lastCheckedId != null && areAdjacent(lastCheckedId!!, checkedId)) {
                    200 // 相邻的 RadioButton
                } else {
                    300 // 非相邻的 RadioButton
                }
                // 创建平移动画
                if (targetX == 0 && checkedId != tabItems[0].viewId){
                    binding.indicator.animate()
                        .x(lastTargetX.toFloat())
                        .setDuration(0)
                        .setInterpolator(FastOutSlowInInterpolator())
                        .start()
                    viewModel.selectTab.postValue(viewModel.selectTab.value ?: 0)
                } else {
                    binding.indicator.animate()
                        .x(targetX.toFloat())
                        .setDuration(duration.toLong())
                        .setInterpolator(FastOutSlowInInterpolator())
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                lastTargetX = targetX
                                lastCheckedId = checkedId // 更新上一个选中的 ID
                                tabItems.forEach { item ->
                                    if (item.viewId == checkedId){
                                        viewModel.selectTab.postValue(item.index)
                                        return@forEach // 提前退出 forEach 循环
                                    }
                                }
                                isAnimationRunning = false // 动画结束标志
                                scrollToSelectedTab(checkedId)
                                viewModel.setSelectCheckedId(checkedId)
                            }
                        })
                        .start()
                }
            }
        }

        // 确保在布局完成后获取每个 RadioButton 的位置
        binding.tab.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.tab.viewTreeObserver.removeOnGlobalLayoutListener(this)
                scrollToSelectedTab(viewModel.selectCheckedId.value!!)
            }
        })

        //路书收藏操作
        binding.favorite.setDebouncedOnClickListener {
            mSpeechSynthesizeBusiness.nowStopPlay() //立即停止播报
            if (viewModel.isLogin()){
                viewModel.requestFavorite(viewModel.lineDetail.value?.id.toString(), isMineFav) //路书请求收藏/取消收藏
            } else {
                viewModel.registerLogin()
            }
        }
        ViewClickEffectUtils.addClickScale(binding.favorite, CLICKED_SCALE_95)

        //全天导航操作
        binding.allDayNavi.setDebouncedOnClickListener {
            try {
                mSpeechSynthesizeBusiness.nowStopPlay() // 立即停止播报
                val selectTab = viewModel.selectTab.value ?: 0
                if (selectTab <= 0) return@setDebouncedOnClickListener

                val lineDetail = viewModel.lineDetail.value?.lineData?.get(selectTab - 1)?.node ?: return@setDebouncedOnClickListener
                val nodeSize = lineDetail.size
                if (nodeSize == 0) return@setDebouncedOnClickListener

                val endPoi = POIFactory.createPOI().apply {
                    name = lineDetail[if (nodeSize > 15) 15 else nodeSize - 1].caption
                    addr = this.name
                    point = GeoPoint(
                        lineDetail[if (nodeSize > 15) 15 else nodeSize - 1].lng,
                        lineDetail[if (nodeSize > 15) 15 else nodeSize - 1].lat
                    )
                }

                var midPois: ArrayList<POI>? = null
                if (nodeSize > 1){
                    midPois = lineDetail.take(if (nodeSize > 15) 15 else nodeSize - 1)
                        .map { item -> POIFactory.createPOI(item.caption, GeoPoint(item.lng, item.lat)) }
                        .toCollection(ArrayList())
                }
                val commandBean = CommandRequestRouteNaviBean.Builder().build(endPoi, midPois)
                viewModel.planRoute(commandBean)
            } catch (e: Exception){
                Timber.i("allDayNavi Exception:${e.message}")
            }
        }
        ViewClickEffectUtils.addClickScale(binding.favorite, CLICKED_SCALE_95)

        //进去景点详情
        ahaLineDetailDayAdapter?.setOnItemClickListener { _, _, position ->
            val poiDetail = ahaLineDetailDayAdapter?.data?.get(position)?.poiDetail
            Timber.i("setOnItemClickListener id:${poiDetail?.id}")
            if (poiDetail != null && poiDetail.id != null){
                mSpeechSynthesizeBusiness.nowStopPlay() //立即停止播报
                findNavController().navigate(R.id.to_ahaScenicDetailFragment, Bundle().apply {
                    putInt("id", poiDetail.id)
                })
            } else {
                toast.showToast(R.string.sv_custom_aha_no_point_detail)
            }
        }

        //进入景点详情页
        viewModel.gotoAhaScenicDetail.unPeek().observe(viewLifecycleOwner) {
            try {
                Timber.i("gotoAhaScenicDetail id:${it}")
                if (it != null){
                    mSpeechSynthesizeBusiness.nowStopPlay() //立即停止播报
                    findNavController().navigate(R.id.to_ahaScenicDetailFragment, Bundle().apply {
                        putInt("id", it.toInt())
                    })
                } else {
                    toast.showToast(R.string.sv_custom_aha_no_point_detail)
                }
            }catch (e: Exception){
                Timber.i("gotoAhaScenicDetail Exception:${e.message}")
                toast.showToast(R.string.sv_custom_aha_no_point_detail)
            }
        }

        viewModel.lineDetail.unPeek().observe(viewLifecycleOwner) {
            if (it != null) {
                setRadioGroupLayout(it) //设置RadioGroup布局，实现简介，dayXX的切换
                viewModel.setDescriptionData(it)
                setCarousel(it.swiper) //设置轮播图
                viewModel.setLinePointLine() //简介图层扎标和画线
            }
        }

        viewModel.setToast.unPeek().observe(viewLifecycleOwner) {
            toast.showToast(it)
        }

        viewModel.selectTab.observe(viewLifecycleOwner) {
            if (it > 0){
                val node = viewModel.lineDetail.value?.lineData?.get(it - 1)?.node
                if (node != null && node.size > 0){
                    ahaLineDetailDayAdapter?.onRefreshData(viewModel.lineDetail.value?.lineData?.get(it - 1)?.node)
                }
                viewModel.hasNode.postValue(node != null && node.size > 0)
                binding.banner.stop()
                viewModel.setLineDayNodePointLine(it) //DAY几图层扎标和画线
            } else{
                binding.banner.start()
                viewModel.setLinePointLine() //简介图层扎标和画线
            }
        }

        //进入对应天详情
        viewModel.gotoAhaDayDetail.unPeek().observe(viewLifecycleOwner) {
            viewModel.selectTab.postValue(it + 1)
            binding.layoutTab.check(tabItems[it + 1].viewId)
            val node = viewModel.lineDetail.value?.lineData?.get(it)?.node
            if (node != null && node.size > 0){
                ahaLineDetailDayAdapter?.onRefreshData(viewModel.lineDetail.value?.lineData?.get(it)?.node)
            }
            viewModel.hasNode.postValue(node != null && node.size > 0)
            binding.banner.stop()
            viewModel.setLineDayNodePointLine(it + 1) //DAY几图层扎标和画线
        }

        viewModel.isLineFavChange.unPeek().observe(viewLifecycleOwner) {
            try {
                if (isMineFav){
                    viewModel.lineCollectList.value?.get(position)?.isFav  = it
                    if (!it){
                        viewModel.deleteLineCollectResult.postValue(viewModel.lineCollectList.value?.get(position)?.id.toString())
                    }else {
                        viewModel.deleteLineCollectResult.postValue("")
                    }
                }
            }catch (e: Exception){
                Timber.i("isGuideFavChange Exception:${e.message}")
            }
        }

        //日夜模式监听
        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            view?.run { skyBoxBusiness.updateView(this, true) }
            if (viewModel.lineDetail.value != null) {
                setCarousel(viewModel.lineDetail.value!!.swiper) //设置轮播图
            }
            ahaLineDetailDayAdapter?.notifyDataSetChanged()
        }
    }

    //设置RadioGroup布局，实现简介，dayXX的切换
    private fun setRadioGroupLayout(data: LineDetailModel.DataDTO?){
        if (data != null){
            binding.layoutTab.removeAllViews() // 清空 RadioGroup 中的子视图
            tabItems.clear()
            tabItems.add(AhaTripDetailTabBean(0, 0, resources.getString(R.string.sv_custom_aha_description)))
            if (!data.lineData.isNullOrEmpty()){
                data.lineData.forEachIndexed { index, lineDataDTO ->
                    tabItems.add(AhaTripDetailTabBean(index + 1, index + 1, "第${lineDataDTO.dayId}天"))
                }
            }
            tabItems.forEachIndexed { index, item ->
                val radioButton = SkinRadioButton(requireContext()).apply {
                    skyBoxBusiness.updateView(this, true)
                    id = View.generateViewId() // 自动生成唯一的 ID
                    layoutParams = RadioGroup.LayoutParams(
                        resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_140),
                        resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_60)
                    )
                    gravity = Gravity.CENTER
                    buttonDrawable = null // 移除默认的 RadioButton 样式
                    isChecked = item.index == viewModel.selectTab.value // 根据 ViewModel 设置选中状态
                    text = item.content // 设置文本
                    textSize = resources.getDimension(com.desaysv.psmap.base.R.dimen.sv_dimen_30)
                    setTextColor(R.color.color_setting_rb_day, R.color.color_setting_rb_night)
                    this.setCompoundDrawablesWithIntrinsicBounds(null, null, null, ContextCompat.getDrawable(requireContext(), R.drawable.selector_bg_rb_transparent))
                    this.compoundDrawablePadding = resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_11)
                    if (index > 0) {
                        // 获取当前的 LayoutParams
                        val layoutParams = this.layoutParams as? RadioGroup.LayoutParams
                        // 修改 leftMargin
                        layoutParams?.leftMargin = resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_16)
                        // 重新应用修改后的 LayoutParams
                        this.layoutParams = layoutParams
                    } else {
                        typeface = if (item.index == viewModel.selectTab.value) {
                            Typeface.DEFAULT_BOLD
                        } else {
                            Typeface.DEFAULT
                        }
                    }
                    // 动态设置粗体
                    setOnCheckedChangeListener { _, isChecked ->
                        typeface = if (isChecked) {
                            Typeface.DEFAULT_BOLD
                        } else {
                            Typeface.DEFAULT
                        }
                    }
                }
                tabItems[index].viewId = radioButton.id
                view?.run { skyBoxBusiness.updateView(radioButton, true) }
                // 将 RadioButton 添加到 RadioGroup
                binding.layoutTab.addView(radioButton)
                // 设置监听器以更新 ViewModel 的 selectTab
                radioButton.setOnClickListener {
                    viewModel.selectTab.postValue(index)
                    binding.layoutTab.check(radioButton.id) // 手动触发选中状态
                }
            }
        }
    }

    // 判断两个 RadioButton 是否相邻
    private fun areAdjacent(lastId: Int, currentId: Int): Boolean {
        var lastIndex = 0
        var currentIndex = 0
        tabItems.forEach { item ->
            if (item.viewId == lastId){
                lastIndex = item.index
                return@forEach // 提前退出 forEach 循环
            }
        }
        tabItems.forEach { item ->
            if (item.viewId == currentId){
                currentIndex = item.index
                return@forEach // 提前退出 forEach 循环
            }
        }
        return abs(currentIndex - lastIndex) > 1
    }

    private fun scrollToSelectedTab(checkedId: Int) {
        try {
            if (!isAnimationRunning){
                val selectedView = view?.findViewById<View>(checkedId)
                if (selectedView != null) {
                    val left = selectedView.left
                    val width = selectedView.width
                    val targetScrollX = left - (binding.tab.width - width) / 2
                    binding.tab.smoothScrollTo(targetScrollX, 0)
                }
            }
        }catch (e: Exception){
            Timber.e("scrollToSelectedTab Exception:${e.message}")
        }
    }

    //设置轮播图
    private fun setCarousel(swiperDTO: List<LineDetailModel.DataDTO.SwiperDTO>?){
        val data = if (swiperDTO.isNullOrEmpty()){
            arrayListOf(LineDetailModel.DataDTO.SwiperDTO().apply { url = "" })
        } else {
            swiperDTO
        }

        binding.banner.setAdapter(object: BannerImageAdapter<LineDetailModel.DataDTO.SwiperDTO>(data) {
            override fun onBindView(
                holder: BannerImageHolder,
                data: LineDetailModel.DataDTO.SwiperDTO?,
                position: Int,
                size: Int) {
                Glide.with(holder.itemView).asBitmap()
                    .load(data?.url)
                    .apply(RequestOptions.bitmapTransform(RoundedCornersTransformation(binding.banner.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_12), 0)).skipMemoryCache(false))
                    .placeholder(if(NightModeGlobal.isNightMode()) R.drawable.ic_aha_banner_default_night else R.drawable.ic_aha_banner_default_day) //加载中占位图
                    .error(if(NightModeGlobal.isNightMode()) R.drawable.ic_aha_banner_default_night else R.drawable.ic_aha_banner_default_day)//加载错误占位图
                    .into(holder.imageView)
            }
        })
    }
}