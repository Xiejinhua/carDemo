package com.desaysv.psmap.model.business

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.autonavi.gbl.aosclient.model.GFeedbackReportRequestParam
import com.autosdk.bussiness.widget.ui.util.MobileUtil
import com.autosdk.common.storage.MapSharePreference
import com.desaysv.psmap.base.business.AosBusiness
import com.desaysv.psmap.base.business.LocationBusiness
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.MapDataBusiness
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.model.R
import com.desaysv.psmap.model.bean.IssueFeedbackBean
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 错误信息上报模块
 */
@Singleton
class FeedbackReportBusiness @Inject constructor(
    private val settingAccountBusiness: SettingAccountBusiness,
    private val aosBusiness: AosBusiness,
    private val mapDataBusiness: MapDataBusiness,
    private val locationBusiness: LocationBusiness,
    private val mapBusiness: MapBusiness,
    private val sharePreferenceFactory: SharePreferenceFactory,
    @ApplicationContext private val context: Context
) {
    val pageType = MutableLiveData(BaseConstant.TYPE_PAGE_ISSUE) //错误信息页面类型
    val posProblemSelect = MutableLiveData(0) //存在的问题，常用选项 默认选中第一个
    val inputDecStr = MutableLiveData("") //问题描述输入框字符串
    val inputPhoneStr = MutableLiveData("") //手机号码输入框字符串
    val showLoading = MutableLiveData(false) //提交，加载中状态
    val setToast = MutableLiveData<String>() //toast
    private var requestId = 0L //请求id
    var noFeedbackListPhoneEdit = BaseConstant.TYPE_PAGE_ISSUE_POS //非问题列表，手机编辑，问题编辑界面
    private val posProblem = mutableListOf(
        context.getString(R.string.sv_setting_issue_feedback_no_pos),
        context.getString(R.string.sv_setting_issue_feedback_inaccurate_pos),
        context.getString(R.string.sv_setting_issue_feedback_other)
    )
    private val internetProblem = mutableListOf(
        context.getString(R.string.sv_setting_issue_feedback_not_use),
        context.getString(R.string.sv_setting_issue_feedback_un_connect),
        context.getString(R.string.sv_setting_issue_feedback_other)
    )
    private val dataDownloadProblem = mutableListOf(
        context.getString(R.string.sv_setting_issue_feedback_data_download_fail),
        context.getString(R.string.sv_setting_issue_feedback_data_download_lost),
        context.getString(R.string.sv_setting_issue_feedback_other)
    )
    private val broadcastProblem = mutableListOf(
        context.getString(R.string.sv_setting_issue_feedback_navi_silent),
        context.getString(R.string.sv_setting_issue_feedback_broadcast_delay),
        context.getString(R.string.sv_setting_issue_feedback_other)
    )
    private val mapSharePreference = sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.account)
    val commonlyProblem = pageType.switchMap { data -> //存在的问题，常用选项
        MutableLiveData<MutableList<String>>().apply {
            value = when (data) {
                BaseConstant.TYPE_PAGE_ISSUE_POS -> posProblem
                BaseConstant.TYPE_PAGE_ISSUE_INTERNET -> internetProblem
                BaseConstant.TYPE_PAGE_ISSUE_DATA_DOWNLOAD -> dataDownloadProblem
                BaseConstant.TYPE_PAGE_ISSUE_BROADCAST -> broadcastProblem
                else -> posProblem
            }
        }
    }

    val inputDecTip = pageType.switchMap { data -> // 问题描述提示
        MutableLiveData<String>().apply {//TYPE_PAGE_ISSUE
            value = when (data) {
                BaseConstant.TYPE_PAGE_ISSUE_POS -> context.getString(R.string.sv_setting_issue_feedback_input_dec)
                BaseConstant.TYPE_PAGE_ISSUE_INTERNET, BaseConstant.TYPE_PAGE_ISSUE_DATA_DOWNLOAD, BaseConstant.TYPE_PAGE_ISSUE_BROADCAST -> context.getString(
                    R.string.sv_setting_issue_feedback_input_dec_to_resolve
                )

                else -> context.getString(R.string.sv_setting_issue_feedback_input_dec)
            }
        }
    }

    val inputDecStrLength = inputDecStr.switchMap { data -> // 问题描述框输入长度
        MutableLiveData<String>().apply {
            value = "${data.length}/300"
        }
    }

    //问题反馈列表数据
    fun getIssueTypeList(): List<IssueFeedbackBean> = listOf(
        BaseConstant.TYPE_ISSUE_POS to (R.string.sv_setting_issue_feedback_pos to R.string.sv_setting_issue_feedback_pos_dec),
        BaseConstant.TYPE_ISSUE_INTERNET to (R.string.sv_setting_issue_feedback_internet to R.string.sv_setting_issue_feedback_internet_dec),
        BaseConstant.TYPE_ISSUE_DATA_DOWNLOAD to (R.string.sv_setting_issue_feedback_data_download to R.string.sv_setting_issue_feedback_data_download_dec),
        BaseConstant.TYPE_ISSUE_BROADCAST to (R.string.sv_setting_issue_feedback_broadcast to R.string.sv_setting_issue_feedback_broadcast_dec),
        BaseConstant.TYPE_ISSUE_OTHER to (R.string.sv_setting_issue_feedback_other to R.string.sv_setting_issue_feedback_other_dec)
    ).map { (type, pair) ->
        val (titleResId, descResId) = pair
        IssueFeedbackBean(type, context.getString(titleResId), context.getString(descResId))
    }

    //设置页面类型
    fun setPageType(itemType: Int) {
        Timber.i("ErrorReportFragment FeedbackReportBusiness setPageType:$itemType")
        if (itemType > BaseConstant.TYPE_ISSUE && itemType < BaseConstant.TYPE_ISSUE_PHONE) {
            noFeedbackListPhoneEdit = itemType
        }
        when (itemType) {
            BaseConstant.TYPE_ISSUE -> pageType.postValue(BaseConstant.TYPE_PAGE_ISSUE)
            BaseConstant.TYPE_ISSUE_POS -> pageType.postValue(BaseConstant.TYPE_PAGE_ISSUE_POS)
            BaseConstant.TYPE_ISSUE_INTERNET -> pageType.postValue(BaseConstant.TYPE_PAGE_ISSUE_INTERNET)
            BaseConstant.TYPE_ISSUE_DATA_DOWNLOAD -> pageType.postValue(BaseConstant.TYPE_PAGE_ISSUE_DATA_DOWNLOAD)
            BaseConstant.TYPE_ISSUE_BROADCAST -> pageType.postValue(BaseConstant.TYPE_PAGE_ISSUE_BROADCAST)
            BaseConstant.TYPE_ISSUE_OTHER -> pageType.postValue(BaseConstant.TYPE_PAGE_ISSUE_OTHER)
            BaseConstant.TYPE_ISSUE_PHONE -> pageType.postValue(BaseConstant.TYPE_PAGE_ISSUE_PHONE)
            BaseConstant.TYPE_ISSUE_EDIT -> pageType.postValue(BaseConstant.TYPE_PAGE_ISSUE_EDIT_DEC)
            else -> pageType.postValue(BaseConstant.TYPE_PAGE_ISSUE)
        }
    }

    //存在的问题，常用选项 选择操作
    fun setPosProblemSelect(selectPos: Int) {
        posProblemSelect.postValue(selectPos)
    }

    //问题描述输入框字符串操作
    fun setInputDecStr(text: String) {
        inputDecStr.postValue(text)
    }

    //手机号输入框字符串操作
    fun setInputPhoneStr(text: String) {
        inputPhoneStr.postValue(text)
    }

    //重新进入错误界面时，获取账号手机号码
    fun getPhoneStr() {
        if (!settingAccountBusiness.isLogin()) {
            Timber.i("getPhoneStr 高德未登录")
            inputPhoneStr.postValue("")
            return
        }
        val accountProfile = settingAccountBusiness.getAccountProfile()
        val mobile = accountProfile?.mobile
        when {
            !mobile.isNullOrEmpty() -> {
                Timber.i("getPhoneStr 高德账号有手机号码：$mobile")
                inputPhoneStr.postValue(CommonUtils.getFormattedPhone(CommonUtils.phoneNoSpace(mobile)))
            }

            else -> {
                Timber.i("getPhoneStr 高德账号手机号码为空")
                val userLoginPhone = mapSharePreference.getStringValue(MapSharePreference.SharePreferenceKeyEnum.userLoginPhone, "")
                if (!userLoginPhone.isNullOrEmpty()) {
                    Timber.i("getPhoneStr 本地保存有手机号码 userLoginPhone：$userLoginPhone")
                    inputPhoneStr.postValue(CommonUtils.getFormattedPhone(CommonUtils.phoneNoSpace(userLoginPhone)))
                } else {
                    Timber.i("getPhoneStr 本地保存手机号码为空")
                    inputPhoneStr.postValue("")
                }
            }
        }
    }

    //回到反馈列表/或者指定界面
    fun resetDefault(itemType: Int) {
        setPageType(itemType)
        setPosProblemSelect(0) //存在的问题，常用选项 选择操作
        setInputDecStr("") //问题输入框恢复原状，清空数据
        setInputPhoneStr("") //手机号输入框字符串操作，清空数据
        getPhoneStr()  //重新进入错误界面时，获取账号手机号码
    }

    //上传等待状态设置
    fun setShowLoading(show: Boolean) {
        showLoading.postValue(show)
        if (requestId > 0) {
            aosBusiness.abortRequest(requestId)
            requestId = 0
        }
    }

    /**
     * 用户反馈-错误上报
     */
    fun sendReqFeedbackReport() {
        val phone = CommonUtils.phoneNoSpace(inputPhoneStr.value!!)
        if (MobileUtil.checkPhone(phone) || MobileUtil.isValidPhoneNumber(phone)) {
            Timber.i("sendReqFeedbackReport inputPhoneStr: $phone")
            showLoading.postValue(true)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentDate: String = sdf.format(Date())
            val location = locationBusiness.getLastLocation()
            val javaRequest = GFeedbackReportRequestParam()
            javaRequest.adcode = mapDataBusiness.getAdCodeByLonLat(location.longitude, location.latitude).toString()
            javaRequest.aetraffic = "8"
            javaRequest.contact = phone
            javaRequest.description.uDes = inputDecStr.value
            javaRequest.description.editDes.tel = phone
            javaRequest.dibv = currentDate
            javaRequest.error_id = 8021
            javaRequest.extra_info.bgc_status = 0
            javaRequest.extra_info.cpcode = "Amap_Desaysv"
            javaRequest.extra_info.scaleaccuracy = mapBusiness.getZoomLevel().toString()
            javaRequest.extra_info.contribute.place_exist = 0
            javaRequest.extra_info.contribute.bus_lines = null
            javaRequest.latitude = location.latitude
            javaRequest.longitude = location.longitude
            javaRequest.sourcepage = 80
            javaRequest.subtype =
                if (pageType.value != BaseConstant.TYPE_PAGE_ISSUE_OTHER) commonlyProblem.value?.get(posProblemSelect.value!!) else ""
            javaRequest.type = "19001"
            requestId = aosBusiness.sendReqFeedbackReport(javaRequest) {
                showLoading.postValue(false)
                if (it != null && it.code == 1) {
                    setToast.postValue(context.getString(R.string.sv_setting_submission_successful_thank_you_for_your_feedback))
                    resetDefault(BaseConstant.TYPE_PAGE_ISSUE) //回到反馈列表/或者指定界面
                } else {
                    setToast.postValue(context.getString(R.string.sv_setting_submission_failed_please_try_again))
                }
            }
        } else {
            setToast.postValue(context.getString(R.string.sv_setting_phone_error))
            Timber.i("sendReqFeedbackReport TextUtils.isEmpty(phone) || phone.length < 11")
            return
        }
    }
}