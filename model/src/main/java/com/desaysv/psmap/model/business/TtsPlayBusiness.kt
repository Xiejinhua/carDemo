package com.desaysv.psmap.model.business

import android.text.TextUtils
import com.autonavi.gbl.guide.model.PlayRingType
import com.autonavi.gbl.guide.model.SoundInfo
import com.autonavi.gbl.guide.observer.ISoundPlayObserver
import com.autosdk.bussiness.navi.NaviController
import com.autosdk.common.tts.IAutoPlayer
import com.desaysv.psmap.base.business.CruiseBusiness
import com.desaysv.psmap.base.impl.ISettingComponent
import com.desaysv.psmap.base.utils.BaseConstant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 导航播报业务类
 */
@Singleton
class TtsPlayBusiness @Inject constructor(
    private val ttsPlayer: IAutoPlayer,
    private val naviController: NaviController,
    private val settingComponent: ISettingComponent,
    private val mCruiseBusiness: CruiseBusiness
) : ISoundPlayObserver {
    fun init() {
        Timber.i("init")
        ttsPlayer.init()
        naviController.registerTbtSoundPlayObserver(this)
    }

    fun unInit() {
        Timber.i("unInit")
        ttsPlayer.release()
        naviController.unregisterTbtSoundPlayObserver(this)
    }

    /**
     * @param isForce 是否强制播报,打断现在播报内容
     */
    fun playText(text: String, isForce: Boolean = false) {
        Timber.i("playText text=$text isForce=$isForce")
        if (isForce) ttsPlayer.playTextNavi(text) else ttsPlayer.playText(text)
    }

    /**
     * 查询前方路况
     */
    fun playTrafficStatus(): Boolean {
        val playTRManualExt = naviController.guideService.playTRManualExt(BaseConstant.PLAY_TRAFFIC_STATUS_ID)
        Timber.i("playTrafficStatus $playTRManualExt")
        return playTRManualExt
    }

    override fun onPlayRing(@PlayRingType.PlayRingType1 type: Int) {
        Timber.i("onPlayRing type=$type")
        if (settingComponent.getConfigKeyMute() == 1) {
            Timber.d("onPlayRing 静音")
            return
        }
        if (mCruiseBusiness.cruiseStatus.value == true) {
            Timber.d(" onPlayRing settingComponent.getCruiseBroadcastSwitch() = ${settingComponent.getCruiseBroadcastSwitch()}")
            if (settingComponent.getCruiseBroadcastSwitch()) {
                ttsPlayer.playNaviWarningSound(type)
            }
        } else {
            ttsPlayer.playNaviWarningSound(type)
        }
    }

    override fun onPlayTTS(pInfo: SoundInfo) {
        try {
            if (settingComponent.getConfigKeyMute() == 1) {
                Timber.d("settingComponent.getConfigKeyMute() = %s", settingComponent.getConfigKeyMute())
                if (pInfo.isManualPlay.toInt() == 1 && pInfo.manualRequestID == BaseConstant.PLAY_TRAFFIC_STATUS_ID) { //手动路况播报
                    Timber.i("toPlayText onPlayTTS playTextByVoiceControl ${pInfo.text}")
                    Timber.i("toPlayText TR_TTS_BROADCAST is ${BaseConstant.TR_TTS_BROADCAST}")
                    if (BaseConstant.TR_TTS_BROADCAST == 1) { //查询前⽅路况 tts播报⽅(由谁播报) 0：auto 1：系统
                        BaseConstant.TR_TTS_BROADCAST = 0
                        if (!TextUtils.isEmpty(pInfo.text)) {
                            mCruiseBusiness.tRTtsBroadcast.postValue(pInfo.text)
                        } else {
                            mCruiseBusiness.tRTtsBroadcast.postValue("未获取到前方路况，请稍后再试试哦")
                        }
                    } else {
                        Timber.d("onPlayTTS settingComponent.getConfigKeyMute() = Mute has text")
                        mCruiseBusiness.tRTtsBroadcast.postValue("")
                    }
                } else {
                    Timber.d("onPlayTTS settingComponent.getConfigKeyMute() = Mute")
                }
                return
            }
            if (mCruiseBusiness.cruiseStatus.value == true) {
                Timber.d(" onPlayTTS  settingComponent.getCruiseBroadcastSwitch() = ${settingComponent.getCruiseBroadcastSwitch()}")
                if (settingComponent.getCruiseBroadcastSwitch()) {
                    toPlayText(pInfo)
                }
            } else {
                toPlayText(pInfo)
            }
        } catch (e: Exception) {
            Timber.e(e, "onPlayTTS")
        }
    }

    private fun toPlayText(pInfo: SoundInfo) {
        if (settingComponent.getConfigKeyMute() == 1) {
            Timber.d("toPlayText 静音")
            return
        }

        if (pInfo.isManualPlay.toInt() == 1 && pInfo.manualRequestID == BaseConstant.PLAY_TRAFFIC_STATUS_ID) { //手动路况播报
            Timber.i("toPlayText onPlayTTS playTextByVoiceControl ${pInfo.text}")
            Timber.i("toPlayText TR_TTS_BROADCAST is ${BaseConstant.TR_TTS_BROADCAST}")
            if (BaseConstant.TR_TTS_BROADCAST == 1) { //查询前⽅路况 tts播报⽅(由谁播报) 0：auto 1：系统
                BaseConstant.TR_TTS_BROADCAST = 0
                if (!TextUtils.isEmpty(pInfo.text)) {
                    mCruiseBusiness.tRTtsBroadcast.postValue(pInfo.text)
                } else {
                    mCruiseBusiness.tRTtsBroadcast.postValue("未获取到前方路况，请稍后再试试哦")
                }
            } else {
                mCruiseBusiness.tRTtsBroadcast.postValue("")
                ttsPlayer.playTextByVoiceControl(pInfo.text, 10109)
            }
        } else {
            Timber.i("toPlayText onPlayTTS playTextNavi ${pInfo.text}")
            if (!TextUtils.isEmpty(pInfo.text)) {
                ttsPlayer.playTextNavi(pInfo.text)
            } else {
                Timber.i("tts_onPlayTTS:pInfo.text isEmpty")
            }
        }

    }

    override fun isPlaying(): Boolean {
//        Timber.i("isPlaying ${ttsPlayer.isPlaying}")
        return ttsPlayer.isPlaying
    }

    fun playTextNavi(tts: String) {
        ttsPlayer.playTextNavi(tts)
    }

    fun playNaviManual(): Boolean {
        return naviController.guideService.playNaviManual().also {
            Timber.i("playNaviManual $it")
        }
    }

}