package com.desaysv.psmap.ui.settings.voicedata

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.autonavi.gbl.data.model.Voice
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.business.SpeechSynthesizeBusiness
import com.desaysv.psmap.base.business.VoiceDataBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 导航语音ViewModel
 */
@HiltViewModel
class VoiceDataViewModel @Inject constructor(
    private val voiceDataBusiness: VoiceDataBusiness,
    private val speechSynthesizeBusiness: SpeechSynthesizeBusiness,
    private val skyBoxBusiness: SkyBoxBusiness
) : ViewModel() {
    val dataListCheckResult = voiceDataBusiness.dataListCheckResult //检测云端语音数据列表是否成功
    val voiceList = voiceDataBusiness.voiceList //voice数据列表--用于界面更新
    val updateImageResult = voiceDataBusiness.updateImageResult //头像更新回调
    val isNight = skyBoxBusiness.themeChange()

    val updateOperated = voiceDataBusiness.updateOperated //更新下载状态
    val updateAllData = voiceDataBusiness.updateAllData //更新数据
    val updatePercent = voiceDataBusiness.updatePercent //更新下载Percent数据
    val enough = voiceDataBusiness.enough //空间提示
    val toDownloadDialog = voiceDataBusiness.toDownloadDialog //是否用流量下载
    val showToast = voiceDataBusiness.showToast //toast提示
    val tipsEnterUnzip = voiceDataBusiness.tipsEnterUnzip //解压
    var tipsVoiceIds = voiceDataBusiness.tipsVoiceIds
    val useVoice = MutableLiveData(getNowUseVoice() ?: Voice()) //正在使用的语音包
    val setUseVoice = voiceDataBusiness.setUseVoice //使用该语音包

    //开始下载，继续下载等操作
    fun downLoadClick(cityAdCode: ArrayList<Int>, operationType: Int) {
        voiceDataBusiness.downLoadClick(cityAdCode, operationType)
    }

    //确定可以流量下载
    fun downLoadConfirmFlow(cityAdCode: ArrayList<Int>, operationType: Int) {
        voiceDataBusiness.downLoadConfirmFlow(cityAdCode, operationType)
    }

    //下载按钮操作
    fun dealWithVoice(voice: Voice) {
        voiceDataBusiness.dealWithVoice(voice)
    }

    //获取正在使用的Voice
    fun getNowUseVoice(): Voice? {
        return speechSynthesizeBusiness.getUseVoice()
    }

    //根据本地保存的角色音VoiceId或者根据新的voiceId设置角色音
    fun todoSetVoice(voice: Voice? = null): Int {
        return speechSynthesizeBusiness.todoSetVoice(voice)
    }

    //设置的导航语音角色音VoiceId
    fun setSpeechVoiceId(voiceId: Int) {
        speechSynthesizeBusiness.setSpeechVoiceId(voiceId)
    }
}