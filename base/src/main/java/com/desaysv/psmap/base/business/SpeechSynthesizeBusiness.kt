package com.desaysv.psmap.base.business

import android.app.Application
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.autonavi.gbl.data.model.DownLoadMode.DOWNLOAD_MODE_NET
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_SUCCESS
import com.autonavi.gbl.data.model.Voice
import com.autonavi.gbl.speech.model.TTSParam
import com.autonavi.gbl.speech.model.TTSParam.TTSParam1
import com.autonavi.gbl.util.model.BinaryStream
import com.autosdk.bussiness.speech.SpeechSynthesizeController
import com.autosdk.bussiness.speech.observer.SpeechObserverImp
import com.autosdk.common.AutoConstant
import com.autosdk.common.storage.MapSharePreference
import com.desaysv.psmap.base.R
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.impl.AudioFocusManager
import com.desaysv.psmap.base.impl.OnFocusChangedToPlayerOperation
import com.desaysv.psmap.base.utils.BaseConstant
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import java.util.Collections
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 语音合成业务
 */
@Singleton
class SpeechSynthesizeBusiness @Inject constructor(
    private val speechSynthesizeController: SpeechSynthesizeController,
    private val voiceDataBusiness: VoiceDataBusiness,
    private val sharePreferenceFactory: SharePreferenceFactory,
    private val gson: Gson,
    private val application: Application,
    private val audioFocusManager: AudioFocusManager
) {
    private val mapSharePreference = sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.userSetting)
    private val initResult = MutableLiveData<Boolean>()
    private val voiceList = voiceDataBusiness.voiceList //voice数据列表--用于界面更新
    val voiceDataSpeechSynthesizeReady = MediatorLiveData<Pair<MutableList<Voice>, Boolean>>().apply {//语音数据list和合成语音已经准备好
        addSource(voiceList) { list ->
            val initSpeech = initResult.value ?: false
            value = Pair(list, initSpeech)
        }
        addSource(initResult) { isShowAgreement ->
            val voiceDataList = voiceList.value ?: arrayListOf()
            value = Pair(voiceDataList, isShowAgreement)
        }
    }
    private val unreportedTexts = Collections.synchronizedList(ArrayList<String>())
    private var requestId = 0 //请求id，观察者可通过此id判断当前回调是哪个请求 由外部自行保证唯一性, 若已存在相同id在队列中等待执行，后面一条会返回失败
    private val pcmDataArray = ArrayList<ByteArray>()
    private var durationTime = 0L//播放时长
    private var audioTrack: AudioTrack? = null
    private var isPlaying: Boolean = false
    private var isStart: Boolean = false
    private var scope = CoroutineScope(Dispatchers.IO + Job())
    private val sampleRate = 16000 // 根据实际情况设置 合成的语音格式固定为16000采样率、S16-LE、单声道的PCM
    private val channelConfig = AudioFormat.CHANNEL_OUT_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private fun setAudioTrack() {
        audioTrack = AudioTrack(
            BaseConstant.SOURCE_NAVIGATION,
            sampleRate,
            channelConfig,
            audioFormat,
            AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat),
            AudioTrack.MODE_STREAM
        )
    }

    fun isPlaying(): Boolean {
        return isPlaying
    }

    //服务初始化
    fun initSpeechSynthesize() {
        speechSynthesizeController.initSpeechSynthesize()
        initResult.postValue(isInitSuccess())
        registerSpeechObserver()
        addFocusChangedToPlayerOperation()
        setAudioTrack()
    }

    /**
     * 是否初始化成功
     */
    fun isInitSuccess(): Boolean {
        return speechSynthesizeController.isInitSuccess
    }

    fun unInit() {
        try {
            audioFocusManager.abandonAudioFocus()
            speechSynthesizeController.unInit()
            unregisterSpeechObserver()
            removeFocusChangedToPlayerOperation()
            scope.cancel() // 取消协程作用域
            audioTrack?.release()
            audioTrack = null
            pcmDataArray.clear()
            unreportedTexts.clear()
        } catch (e: Exception) {
            Timber.i("unInit Exception:${e.message}")
        }
    }

    // ============================ 观察者 ================================
    private fun registerSpeechObserver() {
        speechSynthesizeController.registerSpeechObserver(speechObserverImp)
    }

    private fun unregisterSpeechObserver() {
        speechSynthesizeController.unregisterSpeechObserver()
    }

    //静音停止播放
    fun muteToStopPlay() {
        Timber.i(" muteToStopPlay ")
        if (isPlaying) {
            scope.launch {
                stopPlay()
            }
        }
    }

    /**
     * 设置合成参数，调用Start前设置
     * @param text 待合成的文本
     * @param isSync false-异步调用，立即返回 true-同步调用，合成结束后返回
     * @return
     */
    @Synchronized
    fun synthesize(text: String?, isSync: Boolean) {
        scope.launch {
            Timber.i("getSynthesize() isPlaying:$isPlaying  pcmDataArray.size:${pcmDataArray.size}  isStart:$isStart")
            if (isPlaying || pcmDataArray.size > 0 || isStart) {
                Timber.i("getSynthesize() isPlaying unreportedTexts")
                unreportedTexts.clear()
                unreportedTexts.add(text ?: "")
            } else {
                Timber.i("getSynthesize() isPlaying false synthesize")
                if (requestId > 10000) {
                    requestId = 0
                }
                requestId++
                unreportedTexts.clear()
                speechSynthesizeController.stopAll()
                speechSynthesizeController.synthesize(text, isSync, requestId)
            }
        }
    }


    /**
     * 设置合成参数，调用Start前设置
     * @param param
     * @param value
     * @return
     */
    fun setParam(@TTSParam1 param: Int, value: Int): Int {
        return speechSynthesizeController.setParam(param, value)
    }

    /**
     * 设置语音包，若正在合成会先停止当前合成进程
     * @param irfPath
     */
    private fun setVoice(irfPath: String): Int {
        return speechSynthesizeController.setVoice(irfPath)
    }

    //获取设置的导航语音角色音VoiceId
    private fun getSpeechVoiceId(): Int {
        return mapSharePreference.getIntValue(MapSharePreference.SharePreferenceKeyEnum.speechVoiceId, -1)
    }

    //设置的导航语音角色音VoiceId
    fun setSpeechVoiceId(voiceId: Int) {
        mapSharePreference.putIntValue(MapSharePreference.SharePreferenceKeyEnum.speechVoiceId, voiceId)
    }

    //根据本地保存的角色音VoiceId或者根据新的voiceId设置角色音
    fun todoSetVoice(voice: Voice? = null): Int {
        if (voice == null) {
            val voiceId = getSpeechVoiceId() //获取设置的导航语音角色音VoiceId
            Timber.i("todoSetVoice  voiceId:$voiceId")
            if (voiceId == -1) {
                return setVoice(AutoConstant.VOICE_DEFAULT_IRF)
            }
            val index = voiceList.value?.indexOfFirst { it.id == voiceId } ?: -1
            Timber.i("todoSetVoice index:$index")
            return if (index != -1) {
                setVoice(voiceList.value!![index].filePath)
            } else {
                setVoice(AutoConstant.VOICE_DEFAULT_IRF)
            }
        } else if (voice.id == -1) {
            return setVoice(AutoConstant.VOICE_DEFAULT_IRF)
        } else {
            return setVoice(voice.filePath)
        }
    }

    //获取正在使用的Voice
    fun getUseVoice(): Voice? {
        val voiceId = getSpeechVoiceId() //获取设置的导航语音角色音VoiceId
        Timber.i("todoSetVoice  voiceId:$voiceId")
        if (voiceId == -1) {
            return Voice().apply {
                this.id = -1
                this.name = application.getString(R.string.sv_setting_standard_female_voice)
            }
        }
        val index = voiceList.value?.indexOfFirst { it.id == voiceId } ?: -1
        Timber.i("todoSetVoice index:$index")
        return if (index != -1) {
            val voice = voiceDataBusiness.getVoice(DOWNLOAD_MODE_NET, voiceList.value!![index].id)
            if (voice?.taskState == TASK_STATUS_CODE_SUCCESS) {
                return voice
            }

            setSpeechVoiceId(-1)
            Voice().apply {
                this.id = -1
                this.name = application.getString(R.string.sv_setting_standard_female_voice)
            }
        } else {
            setSpeechVoiceId(-1)
            Voice().apply {
                this.id = -1
                this.name = application.getString(R.string.sv_setting_standard_female_voice)
            }
        }
    }

    //退出导航，模拟导航，巡航主动结束播报
    fun otherSetStop(isSimulationNavi: Boolean = false) {
        if (isPlaying) {
            scope.launch {
                if (isSimulationNavi) {
                    delay(2000)
                    if (isPlaying) {
                        delay(1500)
                    }
                    if (isPlaying) {
                        delay(1000)
                    }
                    if (isPlaying) {
                        delay(500)
                    }
                } else {
                    delay(1500)
                }
                stopPlay()
            }
        }
    }

    //立即停止播报
    fun nowStopPlay(){
        if (isPlaying) {
            scope.launch {
                stopPlay()
            }
        }
    }

    private fun stopPlay() {
        Timber.i("stopPlay")
        speechSynthesizeController.stopAll()
        audioFocusManager.abandonAudioFocus()
        audioFocusManager.resetAudioFocusState()
        isPlaying = false
        pcmDataArray.clear()
        unreportedTexts.clear()

        synchronized(this) {
            audioTrack?.let { track ->
                if (track.state != AudioTrack.STATE_UNINITIALIZED) {
                    try {
                        track.stop()
                    } catch (e: IllegalStateException) {
                        Timber.i("stopPlay IllegalStateException e:${e.message}")
                    }
                    track.release()
                }
            }
            audioTrack = null
        }
    }

    //pcm音频播放
    @Synchronized
    private fun playPcmData() {
        scope.launch {
            if (pcmDataArray.size > 0) {
                Timber.i("playPcmData isPlaying:$isPlaying durationTime：$durationTime")
                if (audioTrack == null) {
                    setAudioTrack()
                }
                if (!audioFocusManager.audioFocusStateDelayed()) {
                    if (audioTrack != null && audioTrack?.state == AudioTrack.STATE_INITIALIZED) {
                        Timber.i("playPcmData !audioFocusManager.audioFocusStateDelayed() && audioTrack != null && audioTrack?.state == AudioTrack.STATE_INITIALIZED")
                        synchronized(audioTrack!!) {
                            if (pcmDataArray.size > 0 && audioTrack != null && audioTrack?.state == AudioTrack.STATE_INITIALIZED) {
                                scope.launch {
                                    try {
                                        isPlaying = true
                                        audioTrack?.play()
                                        withTimeout(TimeUnit.SECONDS.toMillis(durationTime)) {
                                            for (data in pcmDataArray) {
                                                if (audioTrack?.state == AudioTrack.STATE_INITIALIZED) {
                                                    audioTrack?.write(data, 0, data.size)
                                                } else {
                                                    Timber.i("playPcmData audioTrack state not initialized")
                                                    finishPlay() //播放完毕释放资源
                                                    break
                                                }
                                            }
                                        }
                                    } catch (e: TimeoutCancellationException) {
                                        Timber.e("playPcmData timed out after $durationTime")
                                        finishPlay() //播放完毕释放资源
                                    } catch (e: Exception) {
                                        Timber.e("playPcmData Exception: ${e.message}")
                                        finishPlay() //播放完毕释放资源
                                    } finally {
                                        Timber.i("playPcmData finally")
                                        finishPlay() //播放完毕释放资源
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Timber.i("playPcmData audioFocusManager.audioFocusStateDelayed()")
                    isPlaying = false
                    pcmDataArray.clear()
                    audioFocusManager.abandonAudioFocus()
                    synchronized(this) {
                        audioTrack?.let { track ->
                            if (track.state != AudioTrack.STATE_UNINITIALIZED) {
                                try {
                                    track.stop()
                                } catch (e: IllegalStateException) {
                                    Timber.i("stopPlay IllegalStateException e:${e.message}")
                                }
                                track.release()
                            }
                        }
                        audioTrack = null
                    }
                }
            }
        }
    }

    //播放完毕释放资源
    private fun finishPlay() {
        isPlaying = false
        pcmDataArray.clear()
        audioFocusManager.abandonAudioFocus()
        synchronized(this) {
            audioTrack?.let { track ->
                if (track.state != AudioTrack.STATE_UNINITIALIZED) {
                    try {
                        track.stop()
                    } catch (e: IllegalStateException) {
                        Timber.i("stopPlay IllegalStateException e:${e.message}")
                    }
                    track.release()
                }
            }
            audioTrack = null
        }
        if (unreportedTexts.size > 0) {
            Timber.i("finishPlay 未完成的文本继续请求合成语音")
            synthesize(unreportedTexts[0], false) //未完成的文本继续请求合成语音
        }
    }

    private fun addFocusChangedToPlayerOperation() {
        audioFocusManager.addFocusChangedToPlayerOperation(object : OnFocusChangedToPlayerOperation {
            override fun isPlaying(): Boolean {
                Timber.i("addFocusChangedToPlayerOperation isPlaying")
                return isPlaying
            }

            override fun onPlay() {
                Timber.i("addFocusChangedToPlayerOperation onPlay")
                if (pcmDataArray.size > 0) {
                    isStart = false
                    Timber.i("addFocusChangedToPlayerOperation onPlay hasFocus:${audioFocusManager.hasFocus()} audioFocusStateDelayed:${audioFocusManager.audioFocusStateDelayed()}")
                    if (!audioFocusManager.hasFocus()) {
                        val requestResult = audioFocusManager.requestAudioFocus()
                        if (requestResult) {
                            Timber.i("addFocusChangedToPlayerOperation onPlay playPcmData")
                            playPcmData() //pcm音频播放
                        } else {
                            Timber.i("addFocusChangedToPlayerOperation onPlay audioFocusManager.audioFocusStateDelayed()")
                            pcmDataArray.clear()
                            isPlaying = false
                            audioFocusManager.abandonAudioFocus()
                        }
                    } else {
                        Timber.i("addFocusChangedToPlayerOperation audioFocusManager.hasFocus()")
                        playPcmData() //pcm音频播放
                    }
                }
            }

            override fun onPause() {
                Timber.i("addFocusChangedToPlayerOperation onPause")
                isPlaying = false
                audioFocusManager.abandonAudioFocus()
                // 确保audioTrack已初始化
                if (audioTrack != null && audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    audioTrack?.pause()
                }
            }
        })
    }

    private fun removeFocusChangedToPlayerOperation() {
        audioFocusManager.removeFocusChangedToPlayerOperation()
    }

    private val speechObserverImp = object : SpeechObserverImp() {
        override fun onSampleRateChange(sampleRate: Int, pcmLen: IntArray?) {
            Timber.i("onSampleRateChange sampleRate:$sampleRate pcmLen:${gson.toJson(pcmLen)}")
            // 根据sampleRate更新播放器参数
            // 重新设置分片大小（可选）
            if (sampleRate == 16000) {
                pcmLen!![0] = 1280
                setParam(TTSParam.TTS_PARAM_PER_PCM_SIZE, pcmLen[0]) //设置合成参数，调用Start前设置
            }
        }

        override fun onStart(requestId: Int) {
            Timber.i("onStart requestId:$requestId")
            isStart = true
            durationTime = 0L
        }

        override fun onGetData(requestId: Int, pcmData: BinaryStream?, duration: Long) {
            Timber.i("onGetData requestId:$requestId pcmData:${pcmData == null} duration:$duration")
            durationTime += duration
            pcmData?.buffer.let {
                if (it != null) {
                    pcmDataArray.add(it)
                }
            }
        }

        override fun onError(requestId: Int, errCode: Int) {
            Timber.i("onError requestId:$requestId errCode:$errCode")
            isStart = false
            isPlaying = false
            audioFocusManager.abandonAudioFocus()
        }

        override fun onFinish(requestId: Int) {
            Timber.i("onFinish requestId:$requestId hasFocus:${audioFocusManager.hasFocus()} audioFocusStateDelayed:${audioFocusManager.audioFocusStateDelayed()}")
            if (pcmDataArray.size > 0) {
                Timber.i("onFinish playPcmData")
                isStart = false
                if (!audioFocusManager.hasFocus()) {
                    val requestResult = audioFocusManager.requestAudioFocus()
                    if (requestResult) {
                        Timber.i("onFinish to playPcmData")
                        playPcmData() //pcm音频播放
                    } else {
                        Timber.i("onFinish audioFocusManager.audioFocusStateDelayed()")
                        pcmDataArray.clear()
                        isPlaying = false
                        audioFocusManager.abandonAudioFocus()
                    }
                } else {
                    Timber.i("onFinish audioFocusManager.hasFocus() pcm音频播放")
                    playPcmData() //pcm音频播放
                }
            } else {
                Timber.i("onFinish playPcmData pcmDataArray.size <= 0")
                isStart = false
            }
        }

        override fun onStop(requestId: Int) {
            Timber.i("onStop requestId:$requestId")
            isStart = false
            isPlaying = false
            audioFocusManager.abandonAudioFocus()
        }
    }
}