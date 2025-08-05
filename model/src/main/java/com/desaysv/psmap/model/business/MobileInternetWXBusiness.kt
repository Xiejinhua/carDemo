package com.desaysv.psmap.model.business

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.MutableLiveData
import com.autonavi.gbl.aosclient.model.GQRCodeConfirmResponseParam
import com.autonavi.gbl.aosclient.model.GWsPpAutoWeixinQrcodeResponseParam
import com.autonavi.gbl.aosclient.model.GWsPpAutoWeixinStatusResponseParam
import com.autonavi.gbl.aosclient.model.GWsPpAutoWeixinUnbindResponseParam
import com.autosdk.common.storage.MapSharePreference
import com.desaysv.psmap.base.business.UserBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.tracking.EventTrackingUtils
import com.desaysv.psmap.model.R
import com.google.gson.Gson
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


/**
 * 微信互联业务类
 */
@Singleton
class MobileInternetWXBusiness @Inject constructor(
    private val userBusiness: UserBusiness,
    private val sharePreferenceFactory: SharePreferenceFactory,
    private val netWorkManager: NetWorkManager,
    private val gson: Gson, private val application: Application
) {
    val showWeChatLayout: MutableLiveData<Int> = MutableLiveData(5) //微信界面类型 1：显示绑定二维码 2：显示绑定loading界面 3：显示微信用户信息 4：失败界面
    val qrImage = MutableLiveData<Bitmap?>() //绑定二维码
    val qrCodeRefresh = MutableLiveData<Boolean>() //二维码更新
    val unBindFailDialog = MutableLiveData<Boolean>() //高德微信解绑次数限制提醒
    val setToast = MutableLiveData<String>()//toast提示
    val tip = MutableLiveData<String>()//toast提示

    private var requestType = 0 //类型 0： 查询绑定 1. 获取二维码 2. 获取用户微信信息
    private var qrCodeId = ""
    private var qrCodeConfirm = false //true.已扫码 false.未扫码

    private var isUnbind = false //是否点击关闭微信按钮

    private val mapSharePreference = sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.account)

    //获取微信绑定状态
    fun initWXStatusData(isMain: Boolean = false) { //true.地图主图进来的
        if (netWorkManager.isNetworkConnected()) {
            userBusiness.sendReqWsPpAutoWXStatus { gWsPpAutoWeixinStatusResponseParam ->
                paraWeChatStatus(gWsPpAutoWeixinStatusResponseParam, isMain) //高德--微信状态解析
            }
        } else {
            val hasWeChatInfo = mapSharePreference.getBooleanValue(MapSharePreference.SharePreferenceKeyEnum.weChatUserInfo, false)
            if (hasWeChatInfo) {
                tip.postValue(application.resources.getString(R.string.sv_setting_connect_wx_qr_code))
                showWeChatLayout.postValue(3)
                EventTrackingUtils.trackEvent(
                    EventTrackingUtils.EventName.Map_Set,
                    mapOf(
                        Pair(EventTrackingUtils.EventValueName.WeChatStatus, 1)
                    )
                )
            } else {
                failToQuery()
                EventTrackingUtils.trackEvent(
                    EventTrackingUtils.EventName.Map_Set,
                    mapOf(
                        Pair(EventTrackingUtils.EventValueName.WeChatStatus, 0)
                    )
                )
            }
        }
    }

    //点击重试--重新获取二维码
    fun getRetryQrCode() {
        tip.postValue(application.resources.getString(R.string.sv_setting_connect_wx_qr_code))
        showWeChatLayout.postValue(2)
        when (requestType) {
            0 -> { //查询绑定
                userBusiness.sendReqWsPpAutoWXStatus { gWsPpAutoWeixinStatusResponseParam ->
                    paraWeChatStatus(gWsPpAutoWeixinStatusResponseParam)
                }
            }

            1 -> { //获取二维码
                userBusiness.sendReqWsPpAutoWXQrcode { gWsPpAutoWeixinQrcodeResponseParam ->
                    paraWeChatQr(gWsPpAutoWeixinQrcodeResponseParam)
                }
            }
        }
    }

    //点击关闭微信互联
    fun getToUnBind() {
        isUnbind = true
        userBusiness.sendReqWsPpAutoWXUnbind { gWsPpAutoWeixinUnbindResponseParam ->
            paraUnBind(gWsPpAutoWeixinUnbindResponseParam)
        } //微信互联---解绑
    }

    //轮询当前二维码扫描状态
    fun sendReqQRCodeConfirm() {
        if (!qrCodeConfirm) {
            userBusiness.sendReqQRCodeConfirm(qrCodeId) { gqrCodeConfirmResponseParam ->
                paraQRCodeConfirm(gqrCodeConfirmResponseParam)
            }
        }
    }

    //高德--微信状态解析
    private fun paraWeChatStatus(param: GWsPpAutoWeixinStatusResponseParam?, isMain: Boolean = false) {
        if (param != null) {
            when (param.code) {
                1 -> { //成功
                    mapSharePreference.putBooleanValue(MapSharePreference.SharePreferenceKeyEnum.weChatUserInfo, true)
                    tip.postValue(application.resources.getString(R.string.sv_setting_connect_wx_qr_code))
                    showWeChatLayout.postValue(3)
                    if (isMain){
                        EventTrackingUtils.trackEvent(
                            EventTrackingUtils.EventName.Map_Set,
                            mapOf(
                                Pair(EventTrackingUtils.EventValueName.WeChatStatus, 1)
                            )
                        )
                    }
                }

                10061 -> { //10061
                    setToast.postValue(application.resources.getString(R.string.sv_setting_wechat_no_provides_service))
                    failToQuery()
                    mapSharePreference.putBooleanValue(MapSharePreference.SharePreferenceKeyEnum.weChatUserInfo, false)
                    if (isMain){
                        EventTrackingUtils.trackEvent(
                            EventTrackingUtils.EventName.Map_Set,
                            mapOf(
                                Pair(EventTrackingUtils.EventValueName.WeChatStatus, 0)
                            )
                        )
                    }
                }

                10060 -> { //10060.用户没有绑定
                    tip.postValue(application.resources.getString(R.string.sv_setting_connect_wx_qr_code))
                    showWeChatLayout.postValue(2)
                    userBusiness.sendReqWsPpAutoWXQrcode { gWsPpAutoWeixinQrcodeResponseParam ->
                        paraWeChatQr(gWsPpAutoWeixinQrcodeResponseParam)
                    }
                    mapSharePreference.putBooleanValue(MapSharePreference.SharePreferenceKeyEnum.weChatUserInfo, false)
                    if (isMain){
                        EventTrackingUtils.trackEvent(
                            EventTrackingUtils.EventName.Map_Set,
                            mapOf(
                                Pair(EventTrackingUtils.EventValueName.WeChatStatus, 0)
                            )
                        )
                    }
                }

                else -> {
                    val hasWeChatInfo = mapSharePreference.getBooleanValue(MapSharePreference.SharePreferenceKeyEnum.weChatUserInfo, false)
                    if (hasWeChatInfo) {
                        tip.postValue(application.resources.getString(R.string.sv_setting_connect_wx_qr_code))
                        showWeChatLayout.postValue(3)
                        if (isMain){
                            EventTrackingUtils.trackEvent(
                                EventTrackingUtils.EventName.Map_Set,
                                mapOf(
                                    Pair(EventTrackingUtils.EventValueName.WeChatStatus, 1)
                                )
                            )
                        }
                    } else { //未绑定
                        mapSharePreference.putBooleanValue(MapSharePreference.SharePreferenceKeyEnum.weChatUserInfo, false)
                        tip.postValue(application.resources.getString(R.string.sv_setting_connect_wx_qr_code))
                        showWeChatLayout.postValue(2)
                        userBusiness.sendReqWsPpAutoWXQrcode { gWsPpAutoWeixinQrcodeResponseParam ->
                            paraWeChatQr(gWsPpAutoWeixinQrcodeResponseParam)
                        }
                        if (isMain){
                            EventTrackingUtils.trackEvent(
                                EventTrackingUtils.EventName.Map_Set,
                                mapOf(
                                    Pair(EventTrackingUtils.EventValueName.WeChatStatus, 0)
                                )
                            )
                        }
                    }
                }
            }
        } else {
            val hasWeChatInfo = mapSharePreference.getBooleanValue(MapSharePreference.SharePreferenceKeyEnum.weChatUserInfo, false)
            if (hasWeChatInfo) {
                tip.postValue(application.resources.getString(R.string.sv_setting_connect_wx_qr_code))
                showWeChatLayout.postValue(3)
                if (isMain){
                    EventTrackingUtils.trackEvent(
                        EventTrackingUtils.EventName.Map_Set,
                        mapOf(
                            Pair(EventTrackingUtils.EventValueName.WeChatStatus, 1)
                        )
                    )
                }
            } else {
                failToQuery()
                if (isMain){
                    EventTrackingUtils.trackEvent(
                        EventTrackingUtils.EventName.Map_Set,
                        mapOf(
                            Pair(EventTrackingUtils.EventValueName.WeChatStatus, 0)
                        )
                    )
                }
            }
        }
    }

    //显示查询绑定失败界面
    private fun failToQuery() {
        requestType = 0 //类型 0： 查询绑定 1. 获取二维码 2. 获取用户微信信息
        tip.postValue(application.resources.getString(com.desaysv.psmap.base.R.string.sv_common_please_refresh_qr_code))
        showWeChatLayout.postValue(4)
    }

    //显示查询失败界面
    private fun failToWeChatBinding() {
        requestType = 1 //类型 0： 查询绑定 1. 获取二维码 2. 获取用户微信信息
        tip.postValue(application.resources.getString(com.desaysv.psmap.base.R.string.sv_common_please_refresh_qr_code))
        showWeChatLayout.postValue(4)
    }

    //高德--获取微信互联二维码解析
    private fun paraWeChatQr(param: GWsPpAutoWeixinQrcodeResponseParam?) {
        if (param != null && param.code == 1) { //成功
            Timber.d(" paraWeChatQr %s", param.imgStr)
            val bitmap: Bitmap? = base64ToBitmap(param.imgStr)
            if (bitmap != null) {
                qrImage.postValue(bitmap)
                tip.postValue(application.resources.getString(R.string.sv_setting_connect_wx_qr_code))
                showWeChatLayout.postValue(1)
                qrCodeId = param.qrcodeId
                qrCodeRefresh.postValue(true)
                qrCodeConfirm = false //true.已扫码 false.未扫码
            } else {
                failToWeChatBinding() //显示查询微信二维码失败界面
            }
        } else {
            failToWeChatBinding() //显示查询微信二维码失败界面
        }
    }

    //轮询微信是否扫码绑定解析
    private fun paraQRCodeConfirm(param: GQRCodeConfirmResponseParam?) {
        if (param != null && param.code == 1) { //成功
            qrCodeConfirm = true //true.已扫码 false.未扫码
            userBusiness.sendReqWsPpAutoWXStatus { gWsPpAutoWeixinStatusResponseParam ->
                paraWeChatStatus(gWsPpAutoWeixinStatusResponseParam) //高德--微信状态解析
            }
        } else {
            qrCodeConfirm = false //true.已扫码 false.未扫码
            qrCodeRefresh.postValue(true)
            setToast.postValue(application.resources.getString(com.desaysv.psmap.base.R.string.sv_common_please_refresh_qr_code))
            Timber.d(" paraQRCodeConfirm %s", gson.toJson(param))
        }
    }

    //解绑回调解析
    private fun paraUnBind(param: GWsPpAutoWeixinUnbindResponseParam?) {
        if (isUnbind) {
            isUnbind = false
            if (param != null) {
                Timber.d(" paraUnBind： %s", gson.toJson(param))
                if (param.code == 1 || param.code == 10060) { //1.成功 10060.用户没有绑定
                    mapSharePreference.putBooleanValue(MapSharePreference.SharePreferenceKeyEnum.weChatUserInfo, false)
                    tip.postValue(application.resources.getString(R.string.sv_setting_connect_wx_qr_code))
                    showWeChatLayout.postValue(2)
                    userBusiness.sendReqWsPpAutoWXQrcode { gWsPpAutoWeixinQrcodeResponseParam ->
                        paraWeChatQr(gWsPpAutoWeixinQrcodeResponseParam)
                    }
                } else if (param.code == 8) {
                    setToast.postValue(application.resources.getString(com.desaysv.psmap.base.R.string.sv_common_network_anomaly_please_try_again))
                } else {
                    unBindFailDialog.postValue(true)
                }
            } else {
                setToast.postValue(application.resources.getString(com.desaysv.psmap.base.R.string.sv_common_operation_failed_try_again))
            }
        }
    }

    /**
     * base64转为bitmap
     * @param base64Data 数据
     * @return 返回图片
     */
    private fun base64ToBitmap(base64Data: String?): Bitmap? {
        return try {
            val bytes: ByteArray = Base64.decode(base64Data, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            Timber.e(e, "Failed to decode base64 to bitmap")
            null
        }
    }
}