package com.desaysv.psmap.ui.settings.errorreport

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.model.business.FeedbackReportBusiness
import com.desaysv.psmap.model.business.SettingAccountBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 错误信息上报ViewModel
 */
@HiltViewModel
class ErrorReportViewModel @Inject constructor(
    private val feedbackReportBusiness: FeedbackReportBusiness,
    private val settingAccountBusiness: SettingAccountBusiness,
    private val skyBoxBusiness: SkyBoxBusiness
) : ViewModel() {
    val pageType = feedbackReportBusiness.pageType //错误信息页面类型
    val posProblemSelect = feedbackReportBusiness.posProblemSelect //存在的问题，常用选项 默认选中第一个
    val issueTypeList = feedbackReportBusiness.getIssueTypeList() //问题反馈列表数据
    val commonlyProblem = feedbackReportBusiness.commonlyProblem //存在的问题，常用选项
    val inputDecTip = feedbackReportBusiness.inputDecTip // 问题描述提示
    val inputDecStr = feedbackReportBusiness.inputDecStr //问题描述输入框字符串
    val inputDecStrLength = feedbackReportBusiness.inputDecStrLength // 问题描述框输入长度
    val inputPhoneStr = feedbackReportBusiness.inputPhoneStr //手机号码输入框字符串
    val showLoading = feedbackReportBusiness.showLoading //提交，加载中状态
    val setToast = feedbackReportBusiness.setToast //toast
    val loginLoading = settingAccountBusiness.loginLoading
    val isNight = skyBoxBusiness.themeChange()

    val hasInputDecStr = inputDecStr.switchMap { data -> // 问题描述框是否有输入
        MutableLiveData<Boolean>().apply {
            value = !TextUtils.isEmpty(data)
        }
    }

    //设置页面类型
    fun setPageType(itemType: Int) {
        feedbackReportBusiness.setPageType(itemType)
    }

    fun getNoFeedbackListPhoneEdit(): Int {//非问题列表，手机编辑，问题编辑界面
        return feedbackReportBusiness.noFeedbackListPhoneEdit
    }

    //存在的问题，常用选项 选择操作
    fun setPosProblemSelect(selectPos: Int) {
        feedbackReportBusiness.setPosProblemSelect(selectPos)
    }

    //问题描述输入框字符串操作
    fun setInputDecStr(text: String) {
        feedbackReportBusiness.setInputDecStr(text)
    }

    //手机号输入框字符串操作
    fun setInputPhoneStr(text: String) {
        feedbackReportBusiness.setInputPhoneStr(text)
    }

    //重新进入错误界面时，获取账号手机号码
    fun getPhoneStr() {
        feedbackReportBusiness.getPhoneStr()
    }

    //回到反馈列表/或者指定界面
    fun resetDefault(itemType: Int) {
        feedbackReportBusiness.resetDefault(itemType)
    }

    //上传等待状态设置
    fun setShowLoading(show: Boolean) {
        feedbackReportBusiness.setShowLoading(show)
    }

    /**
     * 用户反馈-错误上报
     */
    fun sendReqFeedbackReport() {
        feedbackReportBusiness.sendReqFeedbackReport()
    }

    //手机编辑界面退出时，判断手机号码显示
    fun judeShowPhone(){
        if (pageType.value == BaseConstant.TYPE_PAGE_ISSUE_PHONE && !TextUtils.isEmpty(inputPhoneStr.value) && inputPhoneStr.value!!.length < 13){
            getPhoneStr()  //重新进入错误界面时，获取账号手机号码
        }
    }
}