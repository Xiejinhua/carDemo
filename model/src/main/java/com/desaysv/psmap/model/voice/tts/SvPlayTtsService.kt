package com.desaysv.psmap.model.voice.tts

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.RemoteException
import android.util.SparseIntArray
import com.autosdk.common.tts.IAutoPlayer
import com.autosdk.common.tts.ITTSListener
import com.desay_svautomotive.voicemanager.SdkManager
import com.desay_svautomotive.voicemanager.VrNaviManager
import com.desay_svautomotive.voicemanager.VrTtsManager
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.model.R
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger

/**
 * IOV语音TTS播报
 */
class SvPlayTtsService(@ApplicationContext context: Context) : IAutoPlayer, VrTtsManager.ITtsClient {
    private val mContext = context
    private val listeners: MutableSet<ITTSListener> = mutableSetOf()
    private val ttsManager: VrTtsManager by lazy {
        VrTtsManager.getInstance()
    }

    private val soundPool: SoundPool by lazy {
        SoundPool.Builder().setMaxStreams(10).setAudioAttributes(
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
        ).build()
    }

    private val soundMap = SparseIntArray()

    private val ttsChecksum = AtomicInteger(10000)

    @Volatile
    private var isSpeaking = false

    private var initting = false
    private var inited = false

    private var isMute = false

    override fun init() {
        Timber.i("init start")
        initting = true
        SdkManager.getInstance().init(mContext)
        ttsManager.setITelClient(this)
        soundMap.put(1, soundPool.load(mContext, R.raw.autoreroute, 1))
        val dingId = soundPool.load(mContext, R.raw.edog_dingdong, 1)
        soundMap.put(100, dingId)
        soundMap.put(101, dingId)
        soundMap.put(102, dingId)
        soundMap.put(103, soundPool.load(mContext, R.raw.navi_warning, 1))
        soundMap.put(108, soundPool.load(mContext, R.raw.camera, 1))
        initting = false
        inited = true
        Timber.i("init finish")
    }

    override fun release() {
        Timber.i("unInit")
        inited = false
        soundMap.clear()
        soundPool.release()
    }

    override fun playText(text: String?) {
        Timber.i("playText text=$text")
        try {
            if (!isTtsServerConnected()) {
                Timber.w("playText, TtsServer not Connected")
                return
            }
            if (isSpeaking) {
                Timber.w("playText isSpeaking")
                return
            }
            if (isMute) {
                Timber.w("playText isMute")
                return
            }
            isSpeaking = true
            ttsManager.requestTtsPlay(text, true, ttsChecksum.incrementAndGet())
            Timber.i("playText called with: ttsChecksum = ${ttsChecksum.get()}")
        } catch (e: RemoteException) {
            Timber.w(e)
            isSpeaking = false
        }
    }

    override fun playText(text: String?, volumPercent: Int) {
        Timber.i("playText text=$text volumPercent=$volumPercent")
        //todo 语音TTS接口暂不支持音量调节
        throw RuntimeException("语音TTS接口暂不支持音量调节")
    }

    override fun playTextNavi(text: String?) {
        Timber.i("playTextNavi text=$text")
        try {
            if (!isTtsServerConnected()) {
                Timber.w("playTextNavi, TtsServer not Connected")
                return
            }
            if (isMute) {
                Timber.w("playText isMute")
                return
            }
            isSpeaking = true
            ttsManager.requestTtsPlay(text, true, ttsChecksum.incrementAndGet())
            Timber.i("playTextNavi called with: ttsChecksum = ${ttsChecksum.get()}")
        } catch (e: RemoteException) {
            Timber.w(e)
            isSpeaking = false
        }
    }

    override fun playTextByVoiceControl(text: String?, type: Int) {
        Timber.i("playTextByVoiceControl text=$text type=$type")
        VrNaviManager.getInstance().updateNavigationJson(type, CommonUtils.updateNavigationJson(text))
    }

    override fun setVolume(value: Int) {
        isMute = value == 0
    }

    override fun getStreamType(): Int {
        TODO("Not yet implemented")
    }

    override fun setStreamType(streamType: Int) {
        TODO("Not yet implemented")
    }

    override fun stop(isSimulationNavi: Boolean) {
        Timber.i("stop")
    }

    override fun isPlaying(): Boolean {
        return isSpeaking
    }

    override fun playNaviWarningSound(type: Int) {
        Timber.i("playNaviWarningSound type=$type")
        if (isMute) {
            Timber.i("playNaviWarningSound isMute")
            return
        }
        soundMap[type].let {
            if (it > 0) soundPool.play(it, 1f, 1f, 1, 0, 1f)
        }
    }

    override fun playArSound(type: Int) {
        TODO("Not yet implemented")
    }

    override fun playGroupSound(type: Int) {
        TODO("Not yet implemented")
    }

    override fun registerITTSListener(listener: ITTSListener?) {
        listener?.let {
            listeners.add(listener)
        }
    }

    override fun unregisterITTSListener(listener: ITTSListener?) {
        listener?.let {
            listeners.remove(listener)
        }
    }

    private fun isTtsServerConnected(): Boolean {
        val sdkManager = SdkManager.getInstance()
        val clazz: Class<*> = sdkManager.javaClass
        try {
            val field = clazz.getDeclaredField("mRegisterVR")
            field.isAccessible = true
            return field[sdkManager] != null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /*========================VrTtsManager.ITtsClient==================================*/
    override fun ttsPlayBegin(checksum: Int) {
        Timber.i("ttsPlayBegin $checksum")
        if (checksum == ttsChecksum.get()) {
            for (listener in listeners) {
                listener.onTTSPlayBegin()
            }
        }
    }

    override fun ttsPlayComplete(checksum: Int) {
        Timber.i("ttsPlayComplete $checksum")
        isSpeaking = false
        if (checksum == ttsChecksum.get()) {
            for (listener in listeners) {
                listener.onTTSPlayComplete()
            }
        }
    }

    override fun ttsPlayInterrupted(checksum: Int) {
        Timber.i("ttsPlayInterrupted $checksum")
        isSpeaking = false
        if (checksum == ttsChecksum.get()) {
            for (listener in listeners) {
                listener.onTTSPlayInterrupted()
            }
        }
    }

    override fun ttsPlayError(checksum: Int) {
        Timber.i("ttsPlayError $checksum")
        isSpeaking = false
        if (checksum == ttsChecksum.get()) {
            for (listener in listeners) {
                listener.onTTSPlayError()
            }
        }
    }

    override fun ttsIsPlaying(checksum: Int, p1: Boolean) {
        Timber.i("ttsIsPlaying $checksum $p1")
        isSpeaking = true
        if (checksum == ttsChecksum.get()) {
            for (listener in listeners) {
                listener.onTTSIsPlaying()
            }
        }
    }
}