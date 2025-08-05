package com.desaysv.psmap.model.business

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.autonavi.gbl.aosclient.model.GAddressPredictRequestParam
import com.autonavi.gbl.aosclient.model.GAddressPredictResponseParam
import com.autonavi.gbl.aosclient.model.GWorkdayListRequestParam
import com.autonavi.gbl.aosclient.model.GWsUserviewFootprintSummaryRequestParam
import com.autonavi.gbl.aosclient.model.GWsUserviewFootprintSummaryResponseParam
import com.autonavi.gbl.servicemanager.ServiceMgr
import com.autonavi.gbl.user.account.model.AccountCheckResult
import com.autonavi.gbl.user.account.model.AccountLogoutResult
import com.autonavi.gbl.user.account.model.AccountProfile
import com.autonavi.gbl.user.account.model.AccountProfileResult
import com.autonavi.gbl.user.account.model.AccountRegisterResult
import com.autonavi.gbl.user.account.model.CarltdBindResult
import com.autonavi.gbl.user.account.model.CarltdLoginResult
import com.autonavi.gbl.user.account.model.CarltdUnBindResult
import com.autonavi.gbl.user.account.model.MobileLoginResult
import com.autonavi.gbl.user.account.model.QRCodeLoginConfirmResult
import com.autonavi.gbl.user.account.model.QRCodeLoginResult
import com.autonavi.gbl.user.account.model.VerificationCodeResult
import com.autonavi.gbl.user.model.UserLoginInfo
import com.autonavi.gbl.user.msgpush.model.AimPoiPushMsg
import com.autonavi.gbl.user.msgpush.model.AimRoutePushMsg
import com.autonavi.gbl.user.msgpush.model.TeamPushMsg
import com.autonavi.gbl.user.msgpush.model.TeamUploadResponseMsg
import com.autonavi.gbl.util.errorcode.common.Service
import com.autonavi.gbl.util.errorcode.common.ThirdParty
import com.autonavi.gbl.util.errorcode.user.Account
import com.autonavi.gbl.util.errorcode.user.Common
import com.autosdk.bussiness.account.AccountController
import com.autosdk.bussiness.account.observer.AccountServiceObserver
import com.autosdk.bussiness.account.utils.ConverUtils
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.common.utils.DeviceIdUtils
import com.autosdk.bussiness.location.LocationController
import com.autosdk.bussiness.push.listener.AimPushMessageListener
import com.autosdk.bussiness.push.listener.TeamMessageListener
import com.autosdk.bussiness.widget.ui.util.MobileUtil
import com.autosdk.common.AutoStatus
import com.autosdk.common.storage.MapSharePreference
import com.desaysv.psmap.base.business.AosBusiness
import com.desaysv.psmap.base.business.AutoStatusAdapter
import com.desaysv.psmap.base.business.MapDataBusiness
import com.desaysv.psmap.base.business.PushMessageBusiness
import com.desaysv.psmap.base.business.UserBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.tracking.EventTrackingUtils
import com.desaysv.psmap.base.utils.AppUtils
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.CustomFileUtils
import com.desaysv.psmap.model.R
import com.desaysv.psmap.model.bean.JetourPushResultBean
import com.desaysv.psmap.model.bean.MoreInfoBean
import com.desaysv.psmap.model.business.AccountSdkBusiness.AccountStateLinkageEvent
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import sv.account.sdk.common.AccountDto
import sv.account.sdk.common.LinkageDto
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton


/**
 * 用户账号登录业务
 * 比如获取账号信息，扫码登录
 */
@Singleton
class SettingAccountBusiness @Inject constructor(
    private val application: Application,
    private val userBusiness: UserBusiness,
    private val pushMessageBusiness: PushMessageBusiness,
    private val linkCarBusiness: LinkCarBusiness,
    private val accountController: AccountController,
    private val gson: Gson,
    private val netWorkManager: NetWorkManager,
    private val sharePreferenceFactory: SharePreferenceFactory,
    private val carInfo: ICarInfoProxy,
    private val mapDataBusiness: MapDataBusiness,
    private val locationController: LocationController,
    private val aosBusiness: AosBusiness,
    private val accountSdkBusiness: AccountSdkBusiness
) {
    private var qrCodeId = ""
    private var mLastQrLoginConfirmReqTaskId = -1
    private var taskId = -1
    val showLoginLayout = MutableLiveData(1)
    val qrTips = MutableLiveData(application.getString(com.desaysv.psmap.base.R.string.sv_common_qr_code_refresh))
    val showQrTip = MutableLiveData(true)
    val failTip = MutableLiveData(application.getString(com.desaysv.psmap.base.R.string.sv_common_qr_code_refresh))
    val loginQrImage = MutableLiveData(BitmapFactory.decodeResource(application.resources, R.drawable.ic_bg_default_qr))
    val loginLoading =
        MutableLiveData<Int>()//状态见BaseConstant中的 【LOGIN_STATE_LOADING 登录中】 【LOGIN_STATE_SUCCESS 登录成功】 【LOGIN_STATE_GUEST 未登录-游客模式】 【LOGIN_STATE_FAILED 登录失败】 【LOGOUT_STATE_LOADING 退出登录中】
    val codeSuccess = MutableLiveData<Boolean>()
    val userName = MutableLiveData(application.getString(R.string.sv_setting_login))
    val avatar = MutableLiveData("")
    val favoritesUpdate = MutableLiveData(true)
    val setToast = MutableLiveData<String>()
    val refreshMessage = MutableLiveData<Boolean>() //消息有更新啦
    val onHiddenChanged = MutableLiveData<Boolean>() //界面Hidden监听
    private val gAddressPredictResponseParam = MutableLiveData<GAddressPredictResponseParam>() //预测用户家/公司的位置 数据回调
    val predictType = MutableLiveData(1) //预测类型 1.家 2.公司
    val predictAddress = MutableLiveData<String>() //预测地址
    val predictPoi = MutableLiveData<POI>() //预测地址转成POI
    val phoneNumber = MutableLiveData("") //输入的手机号码
    val phoneError = MutableLiveData("") //手机号码格式错误提示
    val verificationCode = MutableLiveData("") //输入的验证码
    val verificationCodeTime = MutableLiveData("") //倒计时
    val verificationState = MutableLiveData(false) //获取验证码按钮状态，是否可以点击
    val verificationError = MutableLiveData("") //验证码错误提示
    val loginBtnState = MutableLiveData(false) //登录按钮状态
    val cityNumber = MutableLiveData("") //足迹-城市数量 比如5
    val allCityGe = MutableLiveData("") //足迹-城市 描述【个城市】
    val cityDescription = MutableLiveData("") //足迹-城市描述 比如超过56%的用户
    val guideDis = MutableLiveData("") // 足迹-导航公里数，比如1064
    val guideDisUnit = MutableLiveData("") // 足迹-导航公里数单位，比如公里
    val guideDistanceDescription = MutableLiveData("") // 足迹-导航公里数描述，比如相当于淮河的长度
    val footprintLoading = MutableLiveData(false) //足迹信息加载loading状态
    val syncTrackHistory = MutableLiveData<Boolean>()
    var showMoreInfo = MutableLiveData(MoreInfoBean()) //显示提示文言
    val checkState = MutableLiveData(false) //协议勾选按钮状态
    val jetourAccountState = MutableLiveData(false) //捷途账号登录状态
    private var mIsAccountExist = false
    var isPhoneLogin = false //手机号码登录标志
    private var mFootprintSummaryResponseParam: GWsUserviewFootprintSummaryResponseParam? = null

    private var accountProfile: AccountProfile? = null
    private val accountScope = CoroutineScope(Dispatchers.IO + Job())

    var time: Int = -1
    var mHandler: Handler = Handler()
    var mVerifCodeRunnable: Runnable = object : Runnable {
        override fun run() {
            time++
            if (time <= 60) {
                verificationState.postValue(false)
                verificationCodeTime.postValue((60 - time).toString() + "s")
                mHandler.postDelayed(this, 1000)
            } else {
                verificationCodeTime.postValue("")
                time = -1
                mHandler.removeCallbacks(this)
                verificationState.postValue(true)
            }
        }
    }

    fun toSetPhoneNumber(number: String) {
        val tempPhone = CommonUtils.phoneNoSpace(number)
        if (TextUtils.equals(phoneNumber.value, number)) {
            Timber.i("当前的手机号码就是这个，已经输入了")
        } else {
            phoneNumber.postValue(number)
            if (tempPhone.length == BaseConstant.PHONE_NUMBER_LEN) {
                Timber.i("toSetPhoneNumber number:$tempPhone")
                if (MobileUtil.checkPhone(tempPhone) || MobileUtil.isValidPhoneNumber(tempPhone)) {
                    phoneError.postValue("")
                    verificationError.postValue("")
                    verificationState.postValue(true)
                    verificationCodeTime.postValue("")
                    time = -1
                } else {
                    mHandler.removeCallbacks(mVerifCodeRunnable)
                    phoneError.postValue(application.resources.getString(R.string.sv_setting_phone_error)) //手机格式错误
                    verificationError.postValue("")
                    verificationState.postValue(false)
                    verificationCodeTime.postValue("")
                    time = -1
                }
            } else {
                mHandler.removeCallbacks(mVerifCodeRunnable)
                phoneError.postValue("")
                verificationError.postValue("")
                verificationState.postValue(false)
                verificationCodeTime.postValue("")
                time = -1
            }
        }
        loginBtnState.value =
            tempPhone.isNotEmpty() && tempPhone.length == BaseConstant.PHONE_NUMBER_LEN && !verificationCode.value.isNullOrEmpty() && verificationCode.value!!.length == BaseConstant.PHONE_VERIFY_LEN
    }

    fun toSetVerificationCode(code: String) {
        if (TextUtils.equals(verificationCode.value, code)) {
            Timber.i("当前的验证码就是这个，已经输入了")
        } else {
            verificationError.postValue("")
            verificationCode.postValue(code)
        }

        if (TextUtils.isEmpty(phoneNumber.value)) {
            loginBtnState.value = false
        } else {
            val phone = CommonUtils.phoneNoSpace(phoneNumber.value!!)
            loginBtnState.value =
                code.isNotEmpty() && code.length == BaseConstant.PHONE_VERIFY_LEN && phone.isNotEmpty() && phone.length == BaseConstant.PHONE_NUMBER_LEN
        }
    }

    //退出账号登录界面，验证码按钮文本恢复
    fun defaultVerificationCodeTip() {
        phoneNumber.postValue("")
        verificationState.postValue(false)
        verificationError.postValue("")
        phoneError.postValue("")
        verificationCodeTime.postValue("")
        time = -1
    }

    fun toSetToast(msg: Int) {
        setToast.postValue(application.getString(msg))
    }

    fun setRefreshMessage(isShow: Boolean) {
        refreshMessage.postValue(isShow)
    }

    fun setCheckState(check: Boolean) {
        checkState.postValue(check)
    }

    //设置提示文言
    fun setShowMoreInfo(moreInfo: MoreInfoBean) {
        showMoreInfo.postValue(moreInfo)
    }

    //进行登录
    fun mobileLogin() {
        if (checkState.value == true) {
            if (netWorkManager.isNetworkConnected()) {
                val phone = CommonUtils.phoneNoSpace(phoneNumber.value!!)
                Timber.i("mobileLogin phoneNumber:${phone}")
                if (mIsAccountExist) {
                    userBusiness.mobileLogin(phone, verificationCode.value)
                } else {
                    userBusiness.accountRegist(phone, verificationCode.value)
                }
            } else {
                setToast.postValue(application.resources.getString(com.desaysv.psmap.base.R.string.sv_common_network_anomaly_please_try_again))
            }
        } else {
            setToast.postValue(application.resources.getString(R.string.sv_setting_read_check_agree_terms_conditions))
        }
    }

    //用户是否已经登录
    fun isLogin(): Boolean {
        val accountProfile = getAccountProfile()
        val userName = sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.account)
            .getStringValue(MapSharePreference.SharePreferenceKeyEnum.userName, "")
        return !userName.isNullOrEmpty() || accountProfile != null
    }

    fun getAccountProfile(): AccountProfile? {
        try {
            val accountInfo = userBusiness.accountInfo()
            if (accountProfile == null && accountInfo != null) {
                Timber.d("getAccountProfile() uid:${accountInfo.uid}")
                accountProfile = accountInfo
                BaseConstant.uid = accountInfo.uid
                userBusiness.setLoginInfo(UserLoginInfo(accountInfo.uid))
                userBusiness.syncFrequentData() //同步常去地点(家/公司)到云端
                Timber.d("getAccountProfile() 同步常去地点(家/公司)到云端")
                pushMessageBusiness.stopListener()
                pushMessageBusiness.startListen(accountInfo.uid)
                sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.account)
                    .putStringValue(MapSharePreference.SharePreferenceKeyEnum.accountProfile, gson.toJson(accountProfile))
            } else if (accountProfile == null) {
                val localAccountProfileStr = sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.account)
                    .getStringValue(MapSharePreference.SharePreferenceKeyEnum.accountProfile, "")
                val localAccountProfile = gson.fromJson(localAccountProfileStr, AccountProfile::class.java)
                if (localAccountProfile != null) {
                    Timber.d("getAccountProfile() localAccountProfile:${gson.toJson(localAccountProfile)}")
                    accountProfile = localAccountProfile
                    BaseConstant.uid = localAccountProfile.uid
                    userBusiness.setLoginInfo(UserLoginInfo(localAccountProfile.uid))
                    userBusiness.syncFrequentData() //同步常去地点(家/公司)到云端
                    Timber.d("getAccountProfile() localAccountProfile 同步常去地点(家/公司)到云端")
                    pushMessageBusiness.stopListener()
                    pushMessageBusiness.startListen(localAccountProfile.uid)
                }
            }
            return accountProfile
        } catch (e: Exception) {
            Timber.e("getAccountProfile getAccountProfile:${e.message}")
            return null
        }
    }

    fun getQRImage() {
        if (netWorkManager.isNetworkConnected()) {
            userBusiness.getQRImage()
        } else {
            accountScope.launch {
                delay(300)
                failGetQrCode()
            }
        }
    }

    fun addObserver() {
        accountController.addObserver(observer);
    }

    fun removeObserver() {
        accountController.removeObserver(observer)
    }

    //验证账号是否存在,若存在就获取验证码
    fun requestAccountCheck() {
        if (netWorkManager.isNetworkConnected()) {
            val phone = CommonUtils.phoneNoSpace(phoneNumber.value!!)
            Timber.i("requestAccountCheck phoneNumber:$phone")
            if (MobileUtil.checkPhone(phone) || MobileUtil.isValidPhoneNumber(phone)) {
                userBusiness.requestAccountCheck(phone)
            }
        } else {
            setToast.postValue(application.resources.getString(com.desaysv.psmap.base.R.string.sv_common_network_anomaly_please_try_again))
        }
    }

    /**
     * 获取验证码
     * @param mobile
     * @param isAccountExist
     */
    private fun getVerifyCode(mobile: String?, isAccountExist: Boolean) {
        var oldTime: Long =
            sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.account)
                .getLongValue(MapSharePreference.SharePreferenceKeyEnum.oldTime, 0L)
        val curTime = System.currentTimeMillis()
        val preMobile: String = sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.account)
            .getStringValue(MapSharePreference.SharePreferenceKeyEnum.phoneLoginNumber, "")
        if (preMobile.isNotEmpty()) {
            if (preMobile != mobile) {
                sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.account)
                    .putLongValue(MapSharePreference.SharePreferenceKeyEnum.oldTime, 0)
                oldTime = 0
            }
        }
        Timber.d("oldTime:%s, curTime:%s", oldTime, curTime)
        if (curTime - oldTime < BaseConstant.RESEND_VERIFICATION_CODE_TIME) {
            accountScope.launch {
                setToast.postValue(application.getString(R.string.sv_setting_cannot_resend_within_1_minute))
            }
        } else {
            userBusiness.getVerficationCode(mobile, isAccountExist)
            sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.account)
                .putStringValue(MapSharePreference.SharePreferenceKeyEnum.phoneLoginNumber, mobile)
        }
    }

    private var observer: AccountServiceObserver = object : AccountServiceObserver() {
        override fun notify(errCode: Int, taskId: Int, accountCheckResult: AccountCheckResult?) {
            super.notify(errCode, taskId, accountCheckResult)
            accountCheckResult(errCode, accountCheckResult) //验证账号是否存在
        }

        override fun notify(errCode: Int, taskId: Int, verificationCodeResult: VerificationCodeResult?) {
            super.notify(errCode, taskId, verificationCodeResult)
            verificationCodeResult(errCode, verificationCodeResult)
        }

        override fun notify(errCode: Int, taskId: Int, accountRegisterResult: AccountRegisterResult?) {
            super.notify(errCode, taskId, accountRegisterResult)
            accountRegisterResult(errCode) //账号注册回调
        }

        override fun notify(errCode: Int, taskId: Int, mobileLoginResult: MobileLoginResult?) {
            super.notify(errCode, taskId, mobileLoginResult)
            mobileLoginResult(errCode, mobileLoginResult)
        }

        override fun notify(errCode: Int, taskId: Int, qrCodeLoginResult: QRCodeLoginResult?) {
            super.notify(errCode, taskId, qrCodeLoginResult)
            qrCodeResult(qrCodeLoginResult)
        }

        override fun notify(errCode: Int, taskId: Int, qrCodeLoginConfirmResult: QRCodeLoginConfirmResult?) {
            super.notify(errCode, taskId, qrCodeLoginConfirmResult)
            qrCodeLoginConfirmResult(errCode, qrCodeLoginConfirmResult)
        }

        override fun notify(errCode: Int, taskId: Int, accountLogoutResult: AccountLogoutResult?) {
            super.notify(errCode, taskId, accountLogoutResult)
            accountLogoutResult(errCode, accountLogoutResult)
        }

        override fun notify(errCode: Int, taskId: Int, accountProfileResult: AccountProfileResult?) {
            super.notify(errCode, taskId, accountProfileResult)
            accountProfileResult(errCode, accountProfileResult)
        }

        override fun notify(errCode: Int, taskId: Int, carltdBindResult: CarltdBindResult?) {
            super.notify(errCode, taskId, carltdBindResult)
            carltdBindResult(carltdBindResult)
        }

        override fun notify(errCode: Int, taskId: Int, carltdLoginResult: CarltdLoginResult?) {
            super.notify(errCode, taskId, carltdLoginResult)
            carltdLoginResult(carltdLoginResult)
        }

        override fun notify(errCode: Int, taskId: Int, carltdUnBindResult: CarltdUnBindResult?) {
            super.notify(errCode, taskId, carltdUnBindResult)
            carltdUnBindResult(carltdUnBindResult)
        }
    }

    //验证账号是否存在
    private fun accountCheckResult(errCode: Int, accountCheckResult: AccountCheckResult?) {
        if (errCode == Service.ErrorCodeOK && accountCheckResult!!.code != 10021) {
            mIsAccountExist = false
            accountScope.launch {
                setToast.postValue(application.getString(R.string.sv_setting_phone_not_regist_auto_regist))
                getVerifyCode(CommonUtils.phoneNoSpace(phoneNumber.value!!), false)
            }
        } else { //已注册
            mIsAccountExist = true
            getVerifyCode(CommonUtils.phoneNoSpace(phoneNumber.value!!), true)
        }
    }

    private fun verificationCodeResult(errCode: Int, verificationCodeResult: VerificationCodeResult?) {
        verificationCodeResult?.let {
            Timber.d("VerificationCodeResult errorCode:%s result:%s, code:%s", errCode, it.result, it.code)
            if (errCode == Service.ErrorCodeOK) {
                accountScope.launch {
                    if ("false" == it.result && it.code == 10002) {
                        verificationError.postValue(application.getString(R.string.sv_setting_verification_again_after_one_hour))
                        verificationState.postValue(false)
                        setToast.postValue(application.getString(R.string.sv_setting_verification_again_after_one_hour))
                    } else {
                        sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.account)
                            .putLongValue(MapSharePreference.SharePreferenceKeyEnum.oldTime, System.currentTimeMillis())
                        setToast.postValue(application.getString(R.string.sv_setting_verification_sent_successfully))
                        verificationError.postValue("")
                        mHandler.removeCallbacks(mVerifCodeRunnable)
                        mHandler.post(mVerifCodeRunnable)
                    }
                }
            } else {
                verificationState.postValue(true)
                verificationError.postValue(application.getString(R.string.sv_setting_verification_error))
                setToast.postValue(application.getString(R.string.sv_setting_get_verification_error))
            }
        }
    }

    //账号注册回调
    private fun accountRegisterResult(errCode: Int) {
        Timber.d("accountRegisterResult errorCode:%s", errCode)
        if (errCode == Service.ErrorCodeOK) {
            userBusiness.mobileLogin(CommonUtils.phoneNoSpace(phoneNumber.value!!), verificationCode.value)
        }
    }

    //手机登录结果
    private fun mobileLoginResult(errCode: Int, mobileLoginResult: MobileLoginResult?) {
        mobileLoginResult?.let {
            Timber.d("mobileLoginResult errorCode:%s, code:%s", errCode, it.code)
            if (errCode == Service.ErrorCodeOK && it.code == 1) {
                accountScope.launch {
                    mHandler.removeCallbacks(mVerifCodeRunnable)
                    loginLoading.postValue(BaseConstant.LOGIN_STATE_LOADING)
                    sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.account)
                        .put(MapSharePreference.SharePreferenceKeyEnum.userLoginPhone, CommonUtils.phoneNoSpace(phoneNumber.value!!))
                    if (CommonUtils.isVehicle() && CommonUtils.isUseVehicleAccount()) { //真实车辆
                        setToast.postValue(application.getString(R.string.sv_setting_now_login))
                        isPhoneLogin = true //手机号码登录标志
                        val authId = getAccount()?.id ?: ""//设备ID
                        val res: Int = requestAccountBind(authId, carInfo.sNCode)
                        Timber.d("requestAccountBind res: $res authId:$authId")
                    } else {
                        setToast.postValue(
                            if (mIsAccountExist) application.getString(R.string.sv_setting_login_successful) else application.getString(
                                R.string.sv_setting_registered_logged_successfully
                            )
                        )
                        getUserData() //成功请求用户数据
                        AutoStatusAdapter.sendStatus(AutoStatus.ACCOUNT_LOGIN)
                    }
                }
            } else {
                accountScope.launch {
                    if (netWorkManager.isNetworkConnected()) {
                        verificationError.postValue(application.getString(R.string.sv_setting_verification_error))
                    } else {
                        setToast.postValue(application.getString(R.string.sv_setting_network_not_connected_check_try_again))
                    }
                }
            }
        }
    }

    private fun qrCodeResult(result: QRCodeLoginResult?) {
        Timber.d("qrCodeResult: %s", gson.toJson(result))
        if (result == null) {
            Timber.d(" 获取二维码失败 result == null")
            failGetQrCode()
            return
        }
        result.qrcode?.let {
            val qrCode = it
            val buffer = qrCode.data?.buffer
            if (result.code == 1 && buffer != null) {
                val myBitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.size)
                loginQrImage.postValue(myBitmap)
                showLoginLayout.postValue(2)
                showQrTip.postValue(false)
                qrTips.postValue(application.getString(R.string.sv_setting_scan_code_login))
                qrCodeId = qrCode.id
                sendQrLoginReq()
                startQrImageTimeOutCountDown(it.timeout) //启动定时器--判断二维码是否超时
            } else {
                Timber.d(" 获取二维码失败 result.code: %s", result.code)
                failGetQrCode()
            }
        }
    }


    private fun failGetQrCode() {
        showLoginLayout.postValue(3) //二维码loading 1加载圈 2成功 3失败
        showQrTip.postValue(true)
        qrTips.postValue(application.getString(com.desaysv.psmap.base.R.string.sv_common_fail_qr))
        failTip.postValue(application.getString(com.autosdk.R.string.agroup_invite_friend_text_retry))
    }

    /**
     * 添加定时器--判断二维码是否超时
     */
    @SuppressLint("HandlerLeak")
    private val mQrImageTimeOutHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            Timber.d(" mQrImageTimeOutHandler 二维码超时")
            showLoginLayout.postValue(3) //二维码loading 1加载圈 2成功 3失败
            showQrTip.postValue(true)
            qrTips.postValue(application.getString(com.desaysv.psmap.base.R.string.sv_common_expired_qr))
            failTip.postValue(application.getString(com.autosdk.R.string.agroup_invite_friend_text_retry))
        }
    }

    /**
     * 启动定时器--判断二维码是否超时
     * @param timeOut 单位：秒
     */
    private fun startQrImageTimeOutCountDown(timeOut: Int) {
        if (mQrImageTimeOutHandler != null && timeOut > 0) {
            quitQrImageTimeOutCountDown()
            mQrImageTimeOutHandler.sendEmptyMessageDelayed(0, timeOut * 1000L)
        }
    }

    fun quitQrImageTimeOutCountDown() {
        if (mQrImageTimeOutHandler != null) {
            mQrImageTimeOutHandler.removeCallbacksAndMessages(null)
        }
    }

    private fun sendQrLoginReq(): Int {
        if (qrCodeId.isNullOrEmpty()) {
            return Int.MIN_VALUE
        }
        val reqResult = userBusiness.requestCodeConfirm(qrCodeId)
        if (Service.ErrorCodeOK == reqResult.result) {
            Timber.d(" sendQrLoginReq success ")
            mLastQrLoginConfirmReqTaskId = reqResult.taskId.toInt()
        } else {
            Timber.d(" sendQrLoginReq fail ")
            accountScope.launch {
                delay(30000)
                abortQrLoginConfirmRequest()
                sendQrLoginReq()
            }
        }
        return reqResult.result
    }

    fun abortQrLoginConfirmRequest() {
        if (mLastQrLoginConfirmReqTaskId != -1) {
            userBusiness.accountAbort(mLastQrLoginConfirmReqTaskId)
            mLastQrLoginConfirmReqTaskId = -1
        }
    }

    private fun qrCodeLoginConfirmResult(errCode: Int, result: QRCodeLoginConfirmResult?) {
        Timber.d("qrCodeLoginConfirmResult errCode: $errCode result:${gson.toJson(result)}")
        if (result != null) {
            accountScope.launch {
                when (errCode) {
                    Service.ErrorCodeOK -> {
                        if (result.code == 7) {
                            Timber.d(" 二维码超时失效了...")
                            abortQrLoginConfirmRequest()
//                            getQRImage() //重新获取二维码
                            showLoginLayout.postValue(3) //二维码loading 1加载圈 2成功 3失败
                            showQrTip.postValue(true)
                            failTip.postValue(application.getString(com.autosdk.R.string.login_text1))
                        }
                    }

                    Account.ErrorCodeLoginSuccess -> {
                        Timber.d(" 扫码登录成功 ")
                        setToast.postValue(application.getString(R.string.sv_setting_login_confirm))
                        loginLoading.postValue(BaseConstant.LOGIN_STATE_LOADING)
                        if (CommonUtils.isVehicle() && CommonUtils.isUseVehicleAccount()) { //真实车辆
                            isPhoneLogin = false //手机号码登录标志
                            val authId = getAccount()?.id ?: ""//设备ID
                            val res: Int = requestAccountBind(authId, carInfo.sNCode)
                            Timber.d("requestAccountBind res: $res authId:$authId")
                        } else {
                            getUserData() //成功请求用户数据
                        }
                    }

                    else -> {
                        Timber.d(" 未知错误 ")
                    }
                }
            }
        } else {
            Timber.d(if (errCode == ThirdParty.ErrorCodeNetCancel) "qrCodeLoginConfirmResult 取消长轮询" else "qrCodeLoginConfirmResult 长轮询是否扫码登录回调通知返回null")
            //网络不可用或网络超时重新请求长轮询
            if (errCode == ThirdParty.ErrorCodeNetFailed || errCode == ThirdParty.ErrorCodeNetUnreach) {
                accountScope.launch {
                    delay(60000)//60秒后重新调用长轮询
                    abortQrLoginConfirmRequest()
                    sendQrLoginReq()
                }
            }
        }
    }

    fun getUserData() {
        codeSuccess.postValue(true) //成功关闭登录框
        val requestCode: Int = userBusiness.requestAccountProfile()
        Timber.d(" getUserData requestAccountProfile $requestCode")
        if (requestCode != 0) {
            loginLoading.postValue(BaseConstant.LOGIN_STATE_FAILED)
            deleteUserDataToast(false)
        }
    }

    private fun accountProfileResult(errCode: Int, result: AccountProfileResult?) {
        accountScope.launch {
            Timber.d(" accountProfileResult errCode:%s, result: %s", gson.toJson(result), errCode)
            if (errCode >= ThirdParty.ErrorCodeNetFailed && errCode <= ThirdParty.ErrorCodeNetUnreach) {
                loginLoading.postValue(BaseConstant.LOGIN_STATE_SUCCESS)
                setToast.postValue(application.getString(com.desaysv.psmap.base.R.string.sv_common_network_anomaly_please_try_again))
                saveUserData()
                AutoStatusAdapter.sendStatus(AutoStatus.ACCOUNT_LOGIN)
                return@launch
            }
            if (result != null && result.code == 1) {
                accountProfile = result.profile
                sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.account)
                    .put(MapSharePreference.SharePreferenceKeyEnum.userLoginPhone, result.profile.mobile)
                CustomFileUtils.saveFile(result.profile.mobile, result.profile.uid)
                if (CommonUtils.isVehicle() && CommonUtils.isUseVehicleAccount()) { //真实车辆
                    if (isPhoneLogin) { //手机登录，账号绑定成功提示
                        isPhoneLogin = false
                        setToast.postValue(
                            if (mIsAccountExist) application.getString(R.string.sv_setting_login_successful) else application.getString(
                                R.string.sv_setting_registered_logged_successfully
                            )
                        )
                    } else {
                        setToast.postValue(application.getString(R.string.sv_setting_login_successful))
                    }
                } else {
                    setToast.postValue(application.getString(R.string.sv_setting_login_successful))
                }
                saveUserData()
                AutoStatusAdapter.sendStatus(AutoStatus.ACCOUNT_LOGIN)
            } else {
                loginLoading.postValue(BaseConstant.LOGIN_STATE_FAILED)
                deleteUserDataToast(false)
            }
            if (null != result && result.code != 14) {
                //trackUpdateHeadView.postValue(true)
            } else {
                syncTrackHistory.postValue(true)
            }
        }
    }

    //绑定结果回调
    private fun carltdBindResult(result: CarltdBindResult?) {
        accountScope.launch {
            Timber.d(" carltdBindResult: ${gson.toJson(result)}")
            if (result != null) {
                if (result.code == 1) { //绑定成功
                    val uid = getAccountProfile()?.uid ?: ""
                    Timber.d(" carltdBindResult uid: $uid")
                    EventTrackingUtils.trackEvent(
                        EventTrackingUtils.EventName.Map_Set,
                        mapOf(
                            Pair(EventTrackingUtils.EventValueName.LogonTime, System.currentTimeMillis()),
                            Pair(EventTrackingUtils.EventValueName.LoginType, if (isPhoneLogin) 1 else 0)
                        )
                    )
                    accountSdkBusiness.linkAccount(uid)
                } else { //绑定失败--导致登录失败
                    deleteUserDataToast(true)
                    abortQrLoginConfirmRequest()
                    showLoginLayout.postValue(3) //二维码loading 1加载圈 2成功 3失败
                    showQrTip.postValue(true)
                    failTip.postValue(application.getString(com.autosdk.R.string.login_text1))
                    qrTips.postValue(application.getString(com.autosdk.R.string.login_text_refresh))
                }
            }
        }
    }

    //解绑结果回调
    private fun carltdUnBindResult(result: CarltdUnBindResult?) {
        accountScope.launch {
            Timber.d("carltdUnBindResult: ${gson.toJson(result)}")
            if (result != null) {
                if (result.code == 1) { //绑解成功
                    signOut() //退出账号
                } else { //绑解失败--导致退出失败
                    logoutFail() //退出账号失败
                }
            }
        }
    }

    //绑定失败了，需要重新刷新二维码
    fun bindFail() {
        deleteUserData(true) //删除高德用户账号数据
        setToast.postValue(application.getString(R.string.sv_setting_login_failed_please_try_again))
        abortQrLoginConfirmRequest()
        showLoginLayout.postValue(3) //二维码loading 1加载圈 2成功 3失败
        showQrTip.postValue(true)
        failTip.postValue(application.getString(com.autosdk.R.string.login_text1))
        qrTips.postValue(application.getString(com.autosdk.R.string.login_text_refresh))
    }

    /**
     * 保存用户数据
     */
    private fun saveUserData() {
        accountProfile?.let { profile ->
            userBusiness.saveUserData(profile)
            Timber.d(" 获取用户数据成功了 ${gson.toJson(profile)}")
            sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.account)?.apply {
                put(MapSharePreference.SharePreferenceKeyEnum.userName, profile.nickname)
                put(MapSharePreference.SharePreferenceKeyEnum.userAvatar, profile.avatar)
                putStringValue(MapSharePreference.SharePreferenceKeyEnum.accountProfile, gson.toJson(profile))
            }
            BaseConstant.uid = profile.uid
            Timber.d("saveUserData() uid:${profile.uid}")
            userName.postValue(application.getString(R.string.sv_setting_login_name_hi) + profile.nickname)
            avatar.postValue(profile.avatar)
            userBusiness.setLoginInfo(UserLoginInfo(profile.uid))
            userBusiness.syncFrequentData() //同步常去地点(家/公司)到云端
            Timber.d(" 同步常去地点(家/公司)到云端")
            linkCarBusiness.startLinkPhone()
            pushMessageBusiness.stopListener()
            pushMessageBusiness.startListen(BaseConstant.uid)
            favoritesUpdate.postValue(true)
            if (loginLoading.value != BaseConstant.LOGIN_STATE_SUCCESS) {
                loginLoading.postValue(BaseConstant.LOGIN_STATE_SUCCESS)
            } else {
                Timber.d(" 获取用户数据成功了 loginLoading.value == BaseConstant.LOGIN_STATE_SUCCESS")
            }
            requestFootSummary()
        }
    }

    private fun deleteUserDataToast(isBind: Boolean) {//isBind 是否为绑定操作
        setToast.postValue(application.getString(R.string.sv_setting_login_failed_please_try_again))
        deleteUserData(isBind)
    }

    /**
     * 清空用户数据
     */
    fun deleteUserData(isBind: Boolean) {//isBind 是否为绑定操作
        Timber.d(" deleteUserData in")
        abortQrLoginConfirmRequest()
        linkCarBusiness.stopLinkCar()
        accountProfile = null
        sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.account)?.apply {
            put(MapSharePreference.SharePreferenceKeyEnum.userName, "")
            put(MapSharePreference.SharePreferenceKeyEnum.userAvatar, "")
            put(MapSharePreference.SharePreferenceKeyEnum.userLoginPhone, "")
            put(MapSharePreference.SharePreferenceKeyEnum.teamId, "")
            putStringValue(MapSharePreference.SharePreferenceKeyEnum.accountProfile, "")
        }
        BaseConstant.TEAM_ID = ""
        BaseConstant.uid = ""
        val requestId = userBusiness.deleteAccountInfo() //删除本地数据
        Timber.d(" deleteAccountInfo requestId: $requestId")
        userName.postValue(application.getString(R.string.sv_setting_login))
        avatar.postValue("")
        userBusiness.setLoginInfo(UserLoginInfo())
        pushMessageBusiness.stopListener()
        favoritesUpdate.postValue(true)
        if (isBind) {
            loginLoading.postValue(BaseConstant.LOGIN_STATE_FAILED)
        } else {
            loginLoading.postValue(BaseConstant.LOGIN_STATE_GUEST)
        }
    }

    /**
     * 退出登录
     */
    fun signOut() {
        accountScope.launch {
            Timber.d(" signOut in")
            loginLoading.postValue(BaseConstant.LOGOUT_STATE_LOADING)
            val requestId: Int = userBusiness.requestAccountLogout() //退出登录
            Timber.d(" signOut requestId: $requestId")
            if (requestId != Service.ErrorCodeOK) {
                if (requestId == Common.ErrorCodeNotLogin) {
                    //未登录requestId直接退出账号
                    if (CommonUtils.isVehicle() && CommonUtils.isUseVehicleAccount()) { //真实车辆
                        accountSdkBusiness.unlinkAccount(getAccountProfile()?.uid ?: "")
                    } else {
                        deleteUserData(false)
                    }
                } else {
                    logoutFail() //退出账号失败
                }
            }
        }
    }

    //退出账号失败
    fun logoutFail() {
        loginLoading.postValue(BaseConstant.LOGIN_STATE_SUCCESS)
        if (netWorkManager.isNetworkConnected()) {
            setToast.postValue(application.getString(R.string.sv_setting_logout_failed_please_try_again))
        } else {
            setToast.postValue(application.getString(com.desaysv.psmap.base.R.string.sv_common_network_anomaly_please_try_again))
        }
    }

    /**
     * 退出登录回调
     *
     * @param errCode 返回码
     * @param result  回调信息
     */
    private fun accountLogoutResult(errCode: Int, result: AccountLogoutResult?) {
        Timber.d(" accountLogoutResult  errCode: $errCode result: ${gson.toJson(result)}")
        accountScope.launch {
            if (errCode >= ThirdParty.ErrorCodeNetFailed && errCode <= ThirdParty.ErrorCodeNetUnreach) {
                loginLoading.postValue(BaseConstant.LOGIN_STATE_SUCCESS)
                setToast.postValue(application.getString(com.desaysv.psmap.base.R.string.sv_common_network_anomaly_please_try_again))
                return@launch
            }
            if (result != null && result.code == 1) { //退登成功
                AutoStatusAdapter.sendStatus(AutoStatus.ACCOUNT_EXIT)
                if (CommonUtils.isVehicle() && CommonUtils.isUseVehicleAccount()) { //真实车辆
                    accountSdkBusiness.unlinkAccount(getAccountProfile()?.uid ?: "")
                    EventTrackingUtils.trackEvent(
                        EventTrackingUtils.EventName.Map_Set,
                        mapOf(
                            Pair(EventTrackingUtils.EventValueName.LogoutTime, System.currentTimeMillis())
                        )
                    )
                } else {
                    deleteUserData(false)
                }
            } else {
                //退登失败
                loginLoading.postValue(BaseConstant.LOGIN_STATE_SUCCESS)
                setToast.postValue(application.getString(R.string.sv_setting_logout_failed_please_try_again))
            }
        }
    }

    /**
     * 高德账号快速登录
     */
    fun requestQuickLogin(authId: String, userId: String?) {
        Timber.d(" requestQuickLogin in")
        abortQuickLogin() //取消快速登录
        Timber.d(" requestQuickLogin authId: $authId userId: $userId")
        taskId = userBusiness.requestQuickLogin(authId, userId) //获取登录账号信息
    }

    /**
     * 车企账号绑定
     */
    fun requestAccountBind(authId: String, deviceCode: String): Int =
        userBusiness.requestAccountBind(authId, deviceCode)

    /**
     * 车企账号解绑
     */
    fun requestUnBindAccount(authId: String, deviceCode: String): Int =
        userBusiness.requestUnBindAccount(authId, deviceCode)

    //取消快速登录
    fun abortQuickLogin() {
        if (taskId != -1) {
            userBusiness.accountAbort(taskId)
            taskId = -1
        }
    }

    /**
     * @param result 快速登录结果回调
     */
    private fun carltdLoginResult(result: CarltdLoginResult?) {
        try {
            accountScope.launch {
                Timber.d(" carltdLoginResult: %s", gson.toJson(result))
                if (result != null) {
                    if (result.code == 1) {
                        val userProfile = result.data
                        if (userProfile != null) {
                            Timber.d(" CarltdLoginResult 已快速登录")
                            accountProfile = AccountProfile().apply {
                                avatar = userProfile.avatar
                                uid = userProfile.uid
                                username = userProfile.username
                                nickname = userProfile.nickname
                                mobile = userProfile.mobile
                                email = userProfile.email
                            }
                            val mobile = CustomFileUtils.getFile(userProfile.uid)
                            sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.account)
                                .put(MapSharePreference.SharePreferenceKeyEnum.userLoginPhone, mobile ?: "")
                            saveUserData()
                            AutoStatusAdapter.sendStatus(AutoStatus.ACCOUNT_LOGIN)
                        }
                    } else { //快速登录失败
                        if (AppUtils.mapIsTopAPP(application.applicationContext)) {
                            setToast.postValue(application.getString(R.string.sv_setting_login_invalid))
                            deleteUserData(true) //删除高德用户账号数据
                            AutoStatusAdapter.sendStatus(AutoStatus.ACCOUNT_EXIT)
                        }
                        val linkageDto = linkageDto()
                        if (linkageDto != null) {
                            Timber.i("carltdLoginResult linkageDto: ${linkageDto.linkageId}")
                            accountSdkBusiness.unlinkAccount(linkageDto.linkageId)
                        } else {
                            Timber.i("carltdLoginResult: linkageDto == null")
                        }
                    }
                }
            }
        } catch (e: Exception){
            Timber.i("carltdLoginResult: ${e.message}")
        }
    }

    //消息接收监听器，包含了POI和路线
    private val aimPushMessageListener = object : AimPushMessageListener {
        override fun notifyPoiPushMessage(aimPoiPushMsg: AimPoiPushMsg?) {
            setRefreshMessage(true)
        }

        override fun notifyRoutePushMessage(aimPoiPushMsg: AimRoutePushMsg?) {
            setRefreshMessage(true)
        }

        override fun isSend2Car(): Boolean {
            return true
        }

    }

    //组队消息通知监听器
    private val teamMessageListener = object : TeamMessageListener {
        override fun notifyTeamPushMessage(teamPushMsg: TeamPushMsg?) {
            setRefreshMessage(true)
        }

        override fun notifyTeamUploadResponseMessage(teamUploadResponseMsg: TeamUploadResponseMsg?) {
            if (teamUploadResponseMsg != null && (teamUploadResponseMsg.state == 2 || teamUploadResponseMsg.state == 3)) {//2解散，3移除或退出
                setRefreshMessage(true)
            }
        }

    }

    //注册消息推送
    fun addMessageListener() {
        pushMessageBusiness.addSend2carPushMsgListener(aimPushMessageListener)//注册send2car 推送监听
        pushMessageBusiness.addTeamMessageListener(teamMessageListener)//注册组队消息通知
    }

    //移除消息推送
    fun removeMessageListener() {
        pushMessageBusiness.removeSend2carPushMsgListener(aimPushMessageListener)//移除send2car 推送监听
        pushMessageBusiness.removeTeamMessageListener(teamMessageListener)//移除组队消息通知
    }

    /**
     * 预测用户家/公司的位置
     */
    fun sendReqAddressPredict(label: String): Long {
        if (gAddressPredictResponseParam != null && gAddressPredictResponseParam.value == null) {
            val uid = getAccountProfile()?.uid
            if (!TextUtils.isEmpty(uid)) {
                val javaRequest = GAddressPredictRequestParam()
                javaRequest.uid = uid
                javaRequest.queryType = BaseConstant.REQ_ADDRESS_QUERY_TYPE
                javaRequest.label = label
                return userBusiness.sendReqAddressPredict(javaRequest) {
                    Timber.d("sendReqAddressPredict callBack: ${gson.toJson(it)}")
                    if (it != null && it.code == 1) {
                        gAddressPredictResponseParam.postValue(it)
                    }
                }
            }
        }
        return -1
    }

    //设置 预测用户家/公司的位置 数据
    fun setAddressPredictData(aAddressPredictResponseParam: GAddressPredictResponseParam?) {
        if (aAddressPredictResponseParam != null && aAddressPredictResponseParam.code == 1 && aAddressPredictResponseParam.vctPredictList.size > 0) {
            val gPredictInfo = aAddressPredictResponseParam.vctPredictList[0]
            if (TextUtils.equals(gPredictInfo.label, "home") || TextUtils.equals(gPredictInfo.label, "家")) {
                predictType.postValue(1) //类型 1.家 2.公司
            } else if (TextUtils.equals(gPredictInfo.label, "company") || TextUtils.equals(gPredictInfo.label, "公司")) {
                predictType.postValue(2) //类型 1.家 2.公司
            }
            predictAddress.postValue(gPredictInfo.poi_address)
            predictPoi.postValue(ConverUtils.converPredictInfoToPoi(gPredictInfo))
        }
//        if (aAddressPredictResponseParam != null && aAddressPredictResponseParam.code == 0){
//            val gPredictInfo = GPredictInfo("company", "", "惠州市德赛西威汽车电子有限公司", "惠州市德赛西威汽车电子有限公司", 23.15, 114.56, 23.15, 114.56)
//            predictType.postValue(2) //类型 1.家 2.公司
//            predictAddress.postValue( "惠州市德赛西威汽车电子有限公司")
//            predictPoi.postValue(ConverUtils.converPredictInfoToPoi(gPredictInfo))
//        }
    }

    /**
     * 获取节假日信息
     */
    fun sendReqWorkdayList(): Long {
        val javaRequest = GWorkdayListRequestParam()
        /**< diu    设备唯一号,android--imei, ios--IDFV  例如："1498635cd464d9150b27b7486e436a2f"  */
        javaRequest.diu = carInfo.sNCode
        /**< div  客户端版本号 例如："ANDH070308"   */
        javaRequest.div = ServiceMgr.getVersion()
        return userBusiness.sendReqWorkdayList(javaRequest) {
            Timber.d("sendReqWorkdayList callBack: ${gson.toJson(it)}")
        }
    }

    //退出界面需要重置足迹数据
    fun defaultFootSummary() {
        cityNumber.postValue("") //足迹-城市数量 比如5
        allCityGe.postValue("") //足迹-城市 描述【个城市】
        cityDescription.postValue("") //足迹-城市描述 比如超过56%的用户
        guideDis.postValue("") // 足迹-导航公里数，比如1064
        guideDisUnit.postValue("") // 足迹-导航公里数单位，比如公里
        guideDistanceDescription.postValue("") // 足迹-导航公里数描述，比如相当于淮河的长度
        footprintLoading.postValue(false) //足迹信息加载loading状态
    }

    //进入个人中心判断释放登录和是否有网络，已经登录没有网络显示failShowFootSummary
    fun firstFailShowFootSummary() {
        Timber.i("firstFailShowFootSummary isLogin():${isLogin()}, isNetworkConnected:${netWorkManager.isNetworkConnected()}")
        if (isLogin() && !netWorkManager.isNetworkConnected()) {
            failShowFootSummary()
        }
    }

    //获取足迹信息
    fun requestFootSummary() {
        if (netWorkManager.isNetworkConnected()) {
            footprintLoading.postValue(true)
            val gFootprintSummaryRequestParam = GWsUserviewFootprintSummaryRequestParam()
            gFootprintSummaryRequestParam.tid = DeviceIdUtils.getDeviceId()
            gFootprintSummaryRequestParam.card_version = "v2"
            gFootprintSummaryRequestParam.scene = "3"
            val location = locationController.lastLocation
            gFootprintSummaryRequestParam.adcode = mapDataBusiness.getAdCodeByLonLat(location.longitude, location.latitude).toString()
            accountScope.launch {
                try {
                    Timber.e("sendReqFootprintSummary")
                    withTimeout(TimeUnit.SECONDS.toMillis(30)) {
                        aosBusiness.sendReqFootprintSummary(gFootprintSummaryRequestParam) { pResponse ->
                            mFootprintSummaryResponseParam = pResponse
                            accountScope.launch {
                                setupFootprintSummary(mFootprintSummaryResponseParam)
                            }
                        }
                    }
                } catch (e: TimeoutCancellationException) {
                    Timber.e("Request timed out after 30 seconds")
                    failShowFootSummary()
                } catch (e: Exception) {
                    Timber.e("Error fetching footprint summary: ${e.message}")
                    failShowFootSummary()
                }
            }
        } else {
            Timber.i("requestFootSummary no netWork")
            failShowFootSummary()
        }
    }

    private fun failShowFootSummary() {
        footprintLoading.postValue(false)
        cityNumber.postValue("") //足迹-城市数量 比如5
        allCityGe.postValue("") //足迹-城市 描述【个城市】
        cityDescription.postValue(application.resources.getString(R.string.sv_setting_more_cities_are_waiting_to_be_illuminated)) //足迹-城市描述 比如超过56%的用户
        guideDis.postValue("") // 足迹-导航公里数，比如1064
        guideDisUnit.postValue("") //足迹-导航公里数单位，比如公里
        guideDistanceDescription.postValue(application.resources.getString(R.string.sv_setting_quickly_towards_poetry_distant_lands)) // 足迹-导航公里数描述，比如相当于淮河的长度
    }

    //设置足记信息UI显示
    private fun setupFootprintSummary(pResponse: GWsUserviewFootprintSummaryResponseParam?) {
        footprintLoading.postValue(false)
        Timber.i("setupFootprintSummary: ${gson.toJson(pResponse)}")
        if (isLogin() && null != pResponse) {
            var cityNum = application.resources.getString(R.string.sv_setting_0_city_default)
            var cityDesc = ""
            var guideDistance = "0"
            var guideDistanceUnit = application.resources.getString(R.string.sv_setting_km)
            var guideDistanceDesc = ""

            for (moduleItem in pResponse.data.module) {
                when (moduleItem.name) {
                    "city" -> {
                        cityNum = moduleItem.measure
                        cityDesc = moduleItem.desc.text
                    }

                    "driver" -> {
                        guideDistance = moduleItem.measure
                        guideDistanceUnit = moduleItem.unit
                        guideDistanceDesc = moduleItem.desc.text
                    }

                    else -> {}
                }
            }
            cityNumber.postValue(cityNum) //足迹-城市数量 比如5
            allCityGe.postValue(application.resources.getString(R.string.sv_setting_city_num)) //足迹-城市 描述【个城市】
            cityDescription.postValue(if (TextUtils.isEmpty(cityDesc)) application.resources.getString(R.string.sv_setting_more_cities_are_waiting_to_be_illuminated) else cityDesc) //足迹-城市描述 比如超过56%的用户
            guideDis.postValue(guideDistance) // 足迹-导航公里数，比如1064
            guideDisUnit.postValue(guideDistanceUnit) //足迹-导航公里数单位，比如公里
            guideDistanceDescription.postValue(if (TextUtils.isEmpty(guideDistanceDesc)) application.resources.getString(R.string.sv_setting_quickly_towards_poetry_distant_lands) else guideDistanceDesc) // 足迹-导航公里数描述，比如相当于淮河的长度
        }
    }

    /**
     * 个人中心账号绑定初始化
     */
    fun accountSdkInit() {
        accountSdkBusiness.accountSdkInit()
    }

    /**
     * 监听账号状态
     * 内部由StateFlow实现，监听后马上会有当前状态的回调，后续有变化时会回调
     * 回调参数：NotLoggedIn, LoggedIn(AccountDto)
     * 可以通过job取消监听
     */
    fun jobState(): Job {
        return accountSdkBusiness.jobState()
    }

    /**
     * 监听账号绑定事件(Account app中发生绑定/解绑操作)
     * 监听账号绑定事件（账号app中发生绑定/解绑操作）
     * 内部由SharedFlow实现，监听后不会有当前状态的回调，后续有变化时会回调
     * 回调参数：Link(LinkageDto), Unlink(LinkageDto)
     * 可以通过job取消监听
     */
    fun jobLinkageEvent(): Job {
        return accountSdkBusiness.jobLinkageEvent()
    }

    /**
     * 监听event事件
     * 内部由SharedFlow实现，监听后不会有当前状态的回调，后续有变化时会回调
     * 可以通过job取消监听
     */
    fun jobPushEvent(messageType: String): Job {
        return accountSdkBusiness.jobPushEvent(messageType)
    }

    /**
     * 提供CP账号id(注意：CP必要设置)
     * 提供CP账号id（注意：CP必要设置）
     * 提供CP登录账号ID，如果CP已登录，需要返回当前登录账号的id， 如果未登录，返回null或""
     * 需要在Application的oncCeate函数中设置此回调
     *  linkageId是当前CP的账号ID，用于绑定账号操作
     */
    fun setOnGetLinkageIdListener() {
        accountSdkBusiness.setOnGetLinkageIdListener(getAccountProfile()?.uid ?: "")
    }

    //账号回调监听
    fun setAccountStateLinkageEvent(accountStateLinkageEvent: AccountStateLinkageEvent) {
        accountSdkBusiness.setAccountStateLinkageEvent(accountStateLinkageEvent)
    }

    /**
     * 获取当前cp绑定账号的绑定信息
     *  suspend函数，需要在协程中调用
     */
    suspend fun linkageDto(): LinkageDto? {
        if (CommonUtils.isVehicle() && CommonUtils.isUseVehicleAccount()) {
            return accountSdkBusiness.linkageDto()
        }
        return null
    }

    /**
     * 获取账号信息-- null表示未登录
     */
    fun getAccount(): AccountDto? {
        return accountSdkBusiness.getAccount()
    }

    /**
     * 账号中心--获取当前是否已登录
     */
    fun isLoggedIn(): Boolean {
        return accountSdkBusiness.isLoggedIn()
    }

    /**
     * 启动账号App并打开指定页面
     * AccountDestination是可选，不传的话进入首页。
     */
    fun launchAccountApp() {//进入登录页
        accountSdkBusiness.launchAccountApp()
    }
}