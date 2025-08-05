package com.desaysv.psmap.model.voice.tts

import android.content.Context
import com.autosdk.common.tts.IAutoPlayer
import com.autosdk.common.tts.ITTSListener
import com.desaysv.psmap.base.business.SpeechSynthesizeBusiness
import com.desaysv.psmap.base.impl.ISettingComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber

/**
 * T1N用高德语音合成功能
 */
class T1NAutoPlayTtsService(
    @ApplicationContext context: Context,
    speechSynthesizeBusiness: SpeechSynthesizeBusiness,
    settingComponent: ISettingComponent
) : IAutoPlayer {
    private val mContext = context
    private val mSpeechSynthesizeBusiness = speechSynthesizeBusiness
    private val mSettingComponent = settingComponent

    override fun init() {
        Timber.i("init")
        mSpeechSynthesizeBusiness.initSpeechSynthesize()
    }

    override fun release() {
        Timber.i("release")
        mSpeechSynthesizeBusiness.unInit()
    }

    override fun playText(text: String?) {
        if (mSettingComponent.getConfigKeyMute() == 1) {
            Timber.d("playText 静音")
            return
        }
        Timber.i("playText text=$text")
        mSpeechSynthesizeBusiness.synthesize(text, false)
    }

    override fun playText(text: String?, volumPercent: Int) {
        Timber.i("playText text=$text volumPercent=$volumPercent")
        //todo 语音TTS接口暂不支持音量调节
        throw RuntimeException("语音TTS接口暂不支持音量调节")
    }

    override fun playTextNavi(text: String?) {
        if (mSettingComponent.getConfigKeyMute() == 1) {
            Timber.d("playTextNavi 静音")
            return
        }
        Timber.i("playTextNavi text=$text")
        mSpeechSynthesizeBusiness.synthesize(text, false)
    }

    override fun playTextByVoiceControl(text: String?, type: Int) {
        if (mSettingComponent.getConfigKeyMute() == 1) {
            Timber.d("playTextByVoiceControl 静音")
            return
        }
        Timber.i("playTextByVoiceControl text=$text type=$type")
        mSpeechSynthesizeBusiness.synthesize(text, false)
    }

    override fun setVolume(value: Int) {
        TODO("Not yet implemented")
    }

    override fun getStreamType(): Int {
        TODO("Not yet implemented")
    }

    override fun setStreamType(streamType: Int) {
        TODO("Not yet implemented")
    }

    override fun stop(isSimulationNavi: Boolean) {
        Timber.i("stop")
        mSpeechSynthesizeBusiness.otherSetStop(isSimulationNavi)
    }

    override fun isPlaying(): Boolean {
        return mSpeechSynthesizeBusiness.isPlaying()
    }

    override fun playNaviWarningSound(type: Int) {
        //todo 叮咚音
    }

    override fun playArSound(type: Int) {
        TODO("Not yet implemented")
    }

    override fun playGroupSound(type: Int) {
        TODO("Not yet implemented")
    }

    override fun registerITTSListener(listener: ITTSListener?) {
        //TODO 通知外部播报状态
    }

    override fun unregisterITTSListener(listener: ITTSListener?) {
        //TODO
    }

}