package com.desaysv.psmap.model.utils

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import com.desaysv.psmap.base.impl.AudioFocusManager
import com.desaysv.psmap.base.impl.OnFocusChangedToPlayerOperation
import com.desaysv.psmap.base.utils.BaseConstant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 默认的音源管理类
 */
@Singleton
class NaviFocusManager @Inject constructor(private val application: Application) : AudioFocusManager {

    /**
     * 音源焦点变化透出媒体播放器操作接口
     * =============================================================================================
     */
    private var operation: OnFocusChangedToPlayerOperation? = null

    override fun addFocusChangedToPlayerOperation(onFocusChangedToPlayerOperation: OnFocusChangedToPlayerOperation) {
        this.operation = onFocusChangedToPlayerOperation
    }

    override fun removeFocusChangedToPlayerOperation() {
        operation = null
    }

    /**
     * 音源焦点申请逻辑
     * =============================================================================================
     */

    //是否获取到焦点
    private var mHasFocus = false

    //当前获取到焦点的状态
    private var audioFocusState = AudioManager.AUDIOFOCUS_REQUEST_GRANTED

    //是否音源焦点导致的暂停播放
    private var mIsStopAudioByFocus = false

    //音源焦点相关
    private val mAudioManager: AudioManager by lazy { application.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    private lateinit var mFocusRequest: AudioFocusRequest

    /**
     * 焦点改变监听
     */
    private val mAudioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focus ->
        when (focus) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                //长时间持有焦点，完全获得焦点，恢复播放
                Timber.i(" onAudioFocusChange() AUDIOFOCUS_GAIN ${operation?.isPlaying()}, $mIsStopAudioByFocus")
                //请求焦点延迟 且 重新恢复焦点 之后，语音或其他强制修改原播放状态的情况：
                //正常情况，根据失去焦点之前的播放的状态，恢复播放或暂停（不处理）
                if (mIsStopAudioByFocus) {
                    operation?.onPlay()
                    mIsStopAudioByFocus = false
                }
                mHasFocus = true
                audioFocusState = AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            }

            AudioManager.AUDIOFOCUS_LOSS -> {
                //如果当前已经是因为AUDIOFOCUS_LOSS_TRANSIENT而停止，则isPlaying一定为false，会导致标志被覆盖
                if (!mIsStopAudioByFocus) mIsStopAudioByFocus = operation?.isPlaying() ?: true
                Timber.w(" onAudioFocusChange() AUDIOFOCUS_LOSS ${operation?.isPlaying()}, $mIsStopAudioByFocus")
                // 长时间失去焦点，停止播放
                operation?.onPause()
                mHasFocus = false
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // 短暂丢失焦点，如果当前已经是因为AUDIOFOCUS_LOSS而停止，则isPlaying一定为false，会导致标志被覆盖
                if (!mIsStopAudioByFocus) mIsStopAudioByFocus = operation?.isPlaying() ?: true
                Timber.w(" onAudioFocusChange() AUDIOFOCUS_LOSS_TRANSIENT ${operation?.isPlaying()}, $mIsStopAudioByFocus")
                operation?.onPause()
                mHasFocus = false
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // 短暂失去焦点，但是可以跟新使用者共用焦点，降低音量即可
                Timber.w(" onAudioFocusChange() AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK")
            }
        }
    }

    /**
     * 请求焦点
     */
    override fun requestAudioFocus(): Boolean {
        audioFocusState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Timber.i(" requestAudioFocus() Build.VERSION.SDK_INT >= Build.VERSION_CODES.O")
            //多个音源，需要重新赋值
            mFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK).run {
                setAudioAttributes(AudioAttributes.Builder().run {
                    setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                    setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    setLegacyStreamType(BaseConstant.SOURCE_NAVIGATION)
                    build()
                })
                setWillPauseWhenDucked(false)//设置为true，AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK会暂停，系统不会压低声音。设置为false，系统接管自动降音。
                setAcceptsDelayedFocusGain(true)//设置为true，则表示应用可以接受延迟获取焦点，即在其他应用释放焦点后才能获取焦点。
                setOnAudioFocusChangeListener(mAudioFocusChangeListener)
                build()
            }
            //请求焦点
            mAudioManager.requestAudioFocus(mFocusRequest)
        } else {
            Timber.i(" requestAudioFocus() Android8.0版本以下 请求焦点")
            //Android8.0版本以下 请求焦点
            mAudioManager.requestAudioFocus(
                mAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        Timber.i(" requestAudioFocus() $audioFocusState")

        //焦点申请成功
        mHasFocus = audioFocusState == AudioManager.AUDIOFOCUS_REQUEST_GRANTED //通知Player更新和存储媒体类型

        return mHasFocus
    }

    /**
     * 释放焦点
     */
    override fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mAudioManager.abandonAudioFocusRequest(mFocusRequest)
            Timber.i(" abandonAudioFocus abandonAudioFocusRequest")
        } else {
            mAudioManager.abandonAudioFocus(mAudioFocusChangeListener)
        }
        mHasFocus = false
    }

    override fun hasFocus(): Boolean {
        Timber.i(" hasFocus value:$mHasFocus")
        return mHasFocus
    }

    fun audioFocusState() = audioFocusState

    override fun resetAudioFocusState() {
        audioFocusState = AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    override fun audioFocusStateDelayed(): Boolean {
        Timber.i(" audioFocusStateDelayed audioFocusState:$audioFocusState")
        return audioFocusState == AudioManager.AUDIOFOCUS_REQUEST_DELAYED
    }
}