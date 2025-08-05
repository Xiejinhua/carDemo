package com.desaysv.psmap.ui.ahatrip

import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.LayoutInflater
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.auto.skin.view.SkinConstraintLayout
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.bussiness.common.POI
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.business.SpeechSynthesizeBusiness
import com.desaysv.psmap.base.impl.ISettingComponent
import com.desaysv.psmap.databinding.LayoutScenicSectorBinding
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.bean.ScenicSectorBean
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 巡航景点推荐
 */
@AndroidEntryPoint
class ScenicSectorView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    SkinConstraintLayout(context, attrs, defStyleAttr) {
    private var binding: LayoutScenicSectorBinding
    private var mScenicSectorListener: ScenicSectorListener? = null
    private var scenicSectorBean: ScenicSectorBean? = null
    private var mDelayTimer: DelayTimer? = null
    private var isLastNight = false

    /**
     * Millis since epoch when alarm should stop.
     * 倒计时时间
     */
    private val millisInFuture: Long = 6000

    /**
     * The interval in millis that the user receives callbacks
     * 时间间隔
     */
    private val countdownInterval: Long = 1000

    @Inject
    lateinit var toastUtil: ToastUtil

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var mRouteBusiness: RouteBusiness

    @Inject
    lateinit var mSpeechSynthesizeBusiness: SpeechSynthesizeBusiness

    @Inject
    lateinit var settingComponent: ISettingComponent

    init {
        binding = LayoutScenicSectorBinding.inflate(LayoutInflater.from(context), this, true)
        skyBoxBusiness.updateView(this, true)
        initEventOperation()
    }

    private fun initEventOperation() {
        //关闭
        binding.closeIv.setDebouncedOnClickListener {
            mScenicSectorListener?.close(0)
        }
        ViewClickEffectUtils.addClickScale(binding.closeIv, CLICKED_SCALE_90)

        //导航
        binding.ivGoHere.setDebouncedOnClickListener {
            mScenicSectorListener?.goto()
            scenicSectorBean?.let {
                var goPoi = POI().apply {
                    name = it.data?.caption
                    addr = it.data?.caption
                    point = GeoPoint(it.data?.geo?.lng ?: 0.0, it.data?.geo?.lat ?: 0.0)
                }
                Timber.i("ivGoHere ${goPoi.name}")
                planRoute(CommandRequestRouteNaviBean.Builder().build(goPoi))
            }
        }
        ViewClickEffectUtils.addClickScale(binding.ivGoHere, CLICKED_SCALE_95)

        //关闭界面
        binding.image.setDebouncedOnClickListener {
            mScenicSectorListener?.close(1)
        }
    }

    //展示数据
    fun show(data: ScenicSectorBean?){
        isLastNight = NightModeGlobal.isNightMode()
        scenicSectorBean = data
        data?.let {
            Glide.with(binding.image.context).asBitmap()
                .load(it.data?.logo)
                .apply(RequestOptions.bitmapTransform(RoundedCornersTransformation(binding.image.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_4), 0)).skipMemoryCache(false))
                .placeholder(if(NightModeGlobal.isNightMode()) R.drawable.ic_seenic_degault_night else R.drawable.ic_seenic_degault_day) //加载中占位图
                .error(if(NightModeGlobal.isNightMode()) R.drawable.ic_seenic_degault_night else R.drawable.ic_seenic_degault_day)//加载错误占位图
                .into(binding.image)
            it.data?.tts?.let { tts ->
                if (settingComponent.getConfigKeyMute() == 1) {
                    Timber.d("静音")
                    return
                } else {
                    mSpeechSynthesizeBusiness.synthesize(tts, false)
                }
                if (mDelayTimer == null) {
                    mDelayTimer = DelayTimer(millisInFuture, countdownInterval)
                }
                mDelayTimer?.start()
            }
        }
    }

    /**
     * 规划路线
     */
    fun planRoute(commandBean: CommandRequestRouteNaviBean?) {
        //路线规划监听初始化
        mRouteBusiness.outsideInit()
        MainScope().launch {
            val start = commandBean?.start
            val end = commandBean?.end
            val midPois = commandBean?.midPois
            if (start != null && end != null) {
                mRouteBusiness.planRoute(start, end, midPois)
            }
        }
    }

    /**
     * 组队邀请点击相应
     */
    fun setOnScenicSectorListener(scenicSectorListener: ScenicSectorListener?) {
        this.mScenicSectorListener = scenicSectorListener
    }

    interface ScenicSectorListener {
        fun close(type: Int) //关闭 0.关闭按钮 1.图片点击 2.到时间关闭
        fun goto() //导航
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mSpeechSynthesizeBusiness.nowStopPlay() //立即停止播报
        if (mDelayTimer != null) {
            mDelayTimer?.cancel()
            mDelayTimer = null
        }
    }

    private inner class DelayTimer(millisInFuture: Long, countDownInterval: Long) :
        CountDownTimer(millisInFuture, countDownInterval) {
        override fun onFinish() {
            Timber.d("DelayTimer onFinish")
            mScenicSectorListener?.close(2)
        }

        override fun onTick(millisUntilFinished: Long) {
            if (isLastNight != NightModeGlobal.isNightMode()){
                Timber.i("onTick isLastNight != NightModeGlobal.isNightMode()")
                isLastNight = NightModeGlobal.isNightMode()
                scenicSectorBean
                scenicSectorBean?.let {
                    Glide.with(binding.image.context).asBitmap()
                        .load(it.data?.logo)
                        .apply(RequestOptions.bitmapTransform(RoundedCornersTransformation(binding.image.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_4), 0)).skipMemoryCache(false))
                        .placeholder(if(NightModeGlobal.isNightMode()) R.drawable.ic_seenic_degault_night else R.drawable.ic_seenic_degault_day) //加载中占位图
                        .error(if(NightModeGlobal.isNightMode()) R.drawable.ic_seenic_degault_night else R.drawable.ic_seenic_degault_day)//加载错误占位图
                        .into(binding.image)
                }
            }
        }
    }
}