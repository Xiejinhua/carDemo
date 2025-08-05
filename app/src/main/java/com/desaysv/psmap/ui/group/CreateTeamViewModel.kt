package com.desaysv.psmap.ui.group

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.model.business.CustomTeamBusiness
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.business.UserGroupBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 创建队伍ViewModel
 */
@HiltViewModel
class CreateTeamViewModel @Inject constructor(
    private val settingAccountBusiness: SettingAccountBusiness,
    private val userGroupBusiness: UserGroupBusiness,
    private val skyBoxBusiness: SkyBoxBusiness,
    private val customTeamBusiness: CustomTeamBusiness
) : ViewModel() {
    val loginLoading = settingAccountBusiness.loginLoading //1:登录中 2:登录成功 3:登录失败
    val onHiddenChanged = settingAccountBusiness.onHiddenChanged //界面Hidden监听
    val responseStatusHasGroup =
        userGroupBusiness.responseStatusHasGroup //0.创建或加入队伍界面--获取组队状态--判断已经在队里了 1.创建或加入队伍界面--创建队伍--判断已经在队里了 2.创建或加入队伍界面--加入队伍--判断已经在队里了
    val showServiceOrInputOrder = MutableLiveData(0) //0.默认页面（创建或者加入） 1.组队协议而界面 2.输入口令界面
    val joinTeamResult = customTeamBusiness.joinTeamResult //加入队伍结果
    val createTeamResult = customTeamBusiness.createTeamResult //创建队伍结果
    val isNight = skyBoxBusiness.themeChange()
    val setToast = MutableLiveData<String>() //toast显示
    val inputOrder = MutableLiveData("") //口令输入字符串
    private var isAddObserver = false //是否已经添加监听
    val isFirst = MutableLiveData(true) //用于第一次进入界面判断
    val teamInfo = customTeamBusiness.teamInfo //组队信息


    //判断是否登录，未登录提示，只需要进来提示一次
    fun loginTip() {
        if (!settingAccountBusiness.isLoggedIn()) {
            setToast.postValue("登录企业账号才能使用组队出行服务")
        }
    }

    //账号中心--获取当前是否已登录
    fun isLoggedIn(): Boolean {
        return settingAccountBusiness.isLoggedIn()
    }

    fun initData() {
        if (!isAddObserver) {
            isAddObserver = true //是否已经添加监听
            userGroupBusiness.addGroupObserverWithType(BaseConstant.TYPE_GROUP_CREATE)
        }
    }

    override fun onCleared() {
        super.onCleared()
        setClickType("")//"0" 创建队伍,非""和"0"是加入队伍
        isAddObserver = false //是否已经添加监听
        userGroupBusiness.removeGroupObserverWithType(BaseConstant.TYPE_GROUP_CREATE)  //移除组队监听
    }

    /**
     * 获取组队状态
     */
    fun reqStatus() {
        userGroupBusiness.setTaskId(userGroupBusiness.reqStatus().toLong())
    }

    //"0" 创建队伍,非""和"0"是加入队伍
    fun setClickType(clickType: String) {
        userGroupBusiness.setClickType(clickType)
    }

    //创建或加入队伍发现已经再队伍中啦--后续进入组队界面
    fun createJoinButHasGroup() {
        userGroupBusiness.createJoinButHasGroup()
    }

    /**
     * 取消请求
     */
    fun abortRequest() {
        userGroupBusiness.abortRequest()
    }

    /**
     * 触发创建队伍的操作。
     * 此方法调用 [CustomTeamBusiness] 中的 `createTeam` 方法，
     * 用于实际执行创建队伍的业务逻辑。
     */
    fun createTeam() {
        customTeamBusiness.createTeam()
    }

    /**
     * 尝试使用给定的组队口令加入队伍。
     * 此方法调用 [CustomTeamBusiness] 中的 `joinTeam` 方法，
     * 将传入的组队口令传递给业务层进行实际的加入队伍操作。
     *
     * @param code 用于加入队伍的组队口令。
     */
    fun joinTeam(code: String) {
        customTeamBusiness.joinTeam(code)
    }
}