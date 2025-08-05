package com.desaysv.psmap.ui.ahatrip

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.layer.model.BizCustomTypePoint
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.bussiness.common.POI
import com.autosdk.view.KeyboardUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.tracking.EventTrackingUtils
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentAhaScenicDetailBinding
import com.desaysv.psmap.model.banner.adapter.BannerImageAdapter
import com.desaysv.psmap.model.banner.holder.BannerImageHolder
import com.desaysv.psmap.model.banner.indicator.CircleIndicator
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.bean.CommandRequestSearchCategoryBean
import com.desaysv.psmap.model.bean.ScenicDetailBean
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import timber.log.Timber
import javax.inject.Inject

/**
 * 景点详情
 */
@AndroidEntryPoint
class AhaScenicDetailFragment : Fragment() {
    private lateinit var binding: FragmentAhaScenicDetailBinding
    private val viewModel: AhaScenicDetailViewModel by viewModels()

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var toast: ToastUtil

    override fun onPause() {
        super.onPause()
        viewModel.removeAllLayerItems(BizCustomTypePoint.BizCustomTypePoint2.toLong())
    }

    override fun onResume() {
        super.onResume()
        viewModel.setScenicDetailPoint()//景点详情扎点
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.scenicDetail.postValue(null)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAhaScenicDetailBinding.inflate(inflater, container, false)
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

        viewModel.setScenicId(arguments?.getInt("id", 0).toString())
        if (viewModel.scenicDetail.value == null){
            viewModel.requestScenicDetail() //景区详情接口
        }
        binding.banner.addBannerLifecycleObserver(this)//添加生命周期观察者
            .setIndicator(CircleIndicator(requireContext()), false)
        KeyboardUtil.hideKeyboard(view)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initEventOperation() {
        //退出该界面
        binding.ivClose.setDebouncedOnClickListener {
            findNavController().navigateUp()
        }
        ViewClickEffectUtils.addClickScale(binding.ivClose, CLICKED_SCALE_90)

        binding.ivSearchAround.setDebouncedOnClickListener {
            viewModel.scenicDetail.value?.let { data ->
                Timber.i("ivSearchAround")
                EventTrackingUtils.trackEvent(
                    EventTrackingUtils.EventName.SurroundSearch_Click,
                    Pair(EventTrackingUtils.EventValueName.SearchTime, System.currentTimeMillis())
                )
                val commandBean = CommandRequestSearchCategoryBean.Builder()
                    .setPoi(POI().apply {
                        name = data.caption
                        addr = data.address
                        point = GeoPoint(data.geo?.lng ?: 0.0, data.geo?.lat ?: 0.0)
                    })
                    .setType(CommandRequestSearchCategoryBean.Type.SEARCH_AROUND)
                    .build()
                findNavController().navigate(
                    R.id.to_searchCategoryFragment,
                    commandBean.toBundle()
                )
            }
        }
        ViewClickEffectUtils.addClickScale(binding.ivSearchAround, CLICKED_SCALE_90)

        binding.ivSearchAroundOnly.setDebouncedOnClickListener {
            viewModel.scenicDetail.value?.let { data ->
                Timber.i("ivSearchAround")
                EventTrackingUtils.trackEvent(
                    EventTrackingUtils.EventName.SurroundSearch_Click,
                    Pair(EventTrackingUtils.EventValueName.SearchTime, System.currentTimeMillis())
                )
                val commandBean = CommandRequestSearchCategoryBean.Builder()
                    .setPoi(POI().apply {
                        name = data.caption
                        addr = data.address
                        point = GeoPoint(data.geo?.lng ?: 0.0, data.geo?.lat ?: 0.0)
                    })
                    .setType(CommandRequestSearchCategoryBean.Type.SEARCH_AROUND)
                    .build()
                findNavController().navigate(
                    R.id.to_searchCategoryFragment,
                    commandBean.toBundle()
                )
            }
        }
        ViewClickEffectUtils.addClickScale(binding.ivSearchAroundOnly, CLICKED_SCALE_90)

        binding.ivPhoneCall.setDebouncedOnClickListener {
            viewModel.scenicDetail.value?.phone?.let { phone ->
                var num: String = phone
                val index: Int = phone.indexOf("；")
                if (index > -1) {
                    val phoneList: Array<String> = phone.split("；".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (phoneList.isNotEmpty()) {
                        num = phoneList[0].trim { it <= ' ' }
                    }
                }
                viewModel.onPhoneCall(num)
            }
        }
        ViewClickEffectUtils.addClickScale(binding.ivPhoneCall, CLICKED_SCALE_90)

        binding.ivGoHere.setDebouncedOnClickListener {
            Timber.i("clGoHere click")
            viewModel.scenicDetail.value?.let { data ->
                var goPoi = POI().apply {
                    name = data.caption
                    addr = data.address
                    point = GeoPoint(data.geo?.lng ?: 0.0, data.geo?.lat ?: 0.0)
                }

                Timber.i("clGoHere ${goPoi.name}")
                viewModel.planRoute(CommandRequestRouteNaviBean.Builder().build(goPoi))
            }
        }
        ViewClickEffectUtils.addClickScale(binding.ivGoHere, CLICKED_SCALE_95)

        viewModel.scenicDetail.observe(viewLifecycleOwner){
            setBannerImage(it)
            viewModel.setScenicDetailPoint()//景点详情扎点
        }

        viewModel.scenicDetail.unPeek().observe(viewLifecycleOwner){
            if (it != null && it.id == -100000){
                toast.showToast(R.string.sv_custom_aha_no_point_detail)
            }
        }

        //日夜模式监听
        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            view?.run { skyBoxBusiness.updateView(this, true) }
            if (viewModel.scenicDetail.value != null) {
                setBannerImage(viewModel.scenicDetail.value)
            }
        }
    }

    private fun setBannerImage(bean: ScenicDetailBean?){
        if (bean?.images != null && bean.images!!.isNotEmpty()){
            binding.banner.setAdapter(object: BannerImageAdapter<String>(bean.images) {
                override fun onBindView(
                    holder: BannerImageHolder,
                    data: String?,
                    position: Int,
                    size: Int) {
                    Glide.with(holder.itemView).asBitmap()
                        .load(data)
                        .apply(RequestOptions.bitmapTransform(RoundedCornersTransformation(binding.banner.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_12), 0)).skipMemoryCache(false))
                        .placeholder(if(NightModeGlobal.isNightMode()) R.drawable.ic_aha_banner_default_night else R.drawable.ic_aha_banner_default_day) //加载中占位图
                        .error(if(NightModeGlobal.isNightMode()) R.drawable.ic_aha_banner_default_night else R.drawable.ic_aha_banner_default_day)//加载错误占位图
                        .into(holder.imageView)
                }
            })
        }
    }
}