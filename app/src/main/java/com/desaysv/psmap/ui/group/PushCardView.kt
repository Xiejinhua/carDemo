package com.desaysv.psmap.ui.group

import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.lifecycle.Observer
import com.autonavi.auto.skin.view.SkinConstraintLayout
import com.autonavi.gbl.user.msgpush.model.MsgPushType
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.PushMessageBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.LayoutPushCardBinding
import com.desaysv.psmap.model.bean.TeamPushBean
import com.desaysv.psmap.model.bean.TeamPushData
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject


/**
 * 组队推送弹条
 */
@AndroidEntryPoint
class PushCardView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    SkinConstraintLayout(context, attrs, defStyleAttr) {
    private var mTeamPushData: TeamPushData? = null
    private var mDelayTimer: DelayTimer? = null

    /**
     * Millis since epoch when alarm should stop.
     * 倒计时时间
     */
    private val millisInFuture: Long = 10000

    /**
     * The interval in millis that the user receives callbacks
     * 时间间隔
     */
    private val countdownInterval: Long = 1000
    private var binding: LayoutPushCardBinding

    private var mPushCardViewListener: PushCardViewListener? = null

    @Inject
    lateinit var pushMessageBusiness: PushMessageBusiness

    @Inject
    lateinit var toastUtil: ToastUtil

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    init {
        binding = LayoutPushCardBinding.inflate(LayoutInflater.from(context), this, true)
        skyBoxBusiness.updateView(this, true)
        mDelayTimer = DelayTimer(millisInFuture, countdownInterval)
        initEventOperation()
    }

    private fun initEventOperation() {
        //关闭
        binding.ivClose.setDebouncedOnClickListener {

            closeView() //关闭卡片
        }
        ViewClickEffectUtils.addClickScale(binding.ivClose, CLICKED_SCALE_90)

        //按钮点击加入组队/导航
        binding.stvTextGo.setDebouncedOnClickListener {

            closeView() //关闭卡片
        }
        ViewClickEffectUtils.addClickScale(binding.stvTextGo, CLICKED_SCALE_95)

        skyBoxBusiness.themeChange().unPeek().observeForever(observeForever)
    }

    private val observeForever = Observer<Boolean> {
        binding.isNight = it
    }

    fun show(teamPushData: TeamPushData) {
        mTeamPushData = teamPushData

        binding.title.text = teamPushData.title
        binding.content.text = teamPushData.content
        binding.stvTextGo.text = teamPushData.btnText

        /*if (mDelayTimer == null) {
            mDelayTimer = DelayTimer(millisInFuture, countdownInterval)
        }
        mDelayTimer?.start()*/
    }

    //关闭卡片
    fun closeView() {
        /*if (mDelayTimer != null) {
            mDelayTimer?.cancel()
            mDelayTimer = null
        }*/
        //true 进入组队出行/导航到目的地
        mPushCardViewListener?.onclick(mTeamPushData)

        skyBoxBusiness.themeChange().unPeek().removeObserver(observeForever)
    }

    private inner class DelayTimer(millisInFuture: Long, countDownInterval: Long) :
        CountDownTimer(millisInFuture, countDownInterval) {
        override fun onFinish() {
            Timber.d("DelayTimer onFinish")
            closeView() //关闭卡片
        }

        override fun onTick(millisUntilFinished: Long) {}
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pushMessageBusiness.scopeCancel() // 在 View 销毁时取消协程
    }

    /**
     * 组队邀请点击相应
     */
    fun setPushCardViewListener(pushCardViewListener: PushCardViewListener?) {
        mPushCardViewListener = pushCardViewListener
    }

    interface PushCardViewListener {
        fun onclick(teamPushData: TeamPushData?)
    }
}
