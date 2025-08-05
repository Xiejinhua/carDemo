package com.desaysv.psmap.model.business

import android.app.Application
import android.os.Bundle

import com.desaysv.account.sdk.AccountSdk
import com.desaysv.account.sdk.data.AccountHostEnum
import com.desaysv.account.sdk.data.LinkageResult
import com.desaysv.account.sdk.data.LinkageResultEnum
import com.desaysv.psmap.base.utils.CommonUtils
import com.google.gson.Gson
import kotlinx.coroutines.Job
import sv.account.sdk.common.AccountDestination
import sv.account.sdk.common.AccountDto
import sv.account.sdk.common.AccountLifecycle
import sv.account.sdk.common.LinkageDto
import sv.account.sdk.common.LinkageLifecycle
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 账号绑定业务--与个人中心对接
 */
@Singleton
class AccountSdkBusiness @Inject constructor(
    private val application: Application,
    private val gson: Gson
) {
    private var accountStateLinkageEvent: AccountStateLinkageEvent? = null

    /**
     * 个人中心账号绑定初始化
     */
    fun accountSdkInit() {
        try {
            AccountSdk.init(application.applicationContext, AccountHostEnum.JetourT1n)
            Timber.i("accountSdkInt init")
        } catch (e: Exception) {
            Timber.i("accountSdkInt e:${e.message}")
        }
    }

    /**
     * 获取账号信息-- null表示未登录
     */
    fun getAccount(): AccountDto? {
        return if (CommonUtils.isVehicle() && CommonUtils.isUseVehicleAccount()) AccountSdk.accountManager.accountInfo else null
    }

    /**
     * 获取当前是否已登录
     */
    fun isLoggedIn(): Boolean {
        val isLogin =
            if (CommonUtils.isVehicle() && CommonUtils.isUseVehicleAccount()) AccountSdk.accountManager.isLoggedIn() else true
        Timber.i("isLoggedIn:$isLogin")
        return isLogin
    }

    /**
     * 获取当前cp绑定账号的绑定信息
     *  suspend函数，需要在协程中调用
     */
    suspend fun linkageDto(): LinkageDto? {
        val link = if (CommonUtils.isVehicle() && CommonUtils.isUseVehicleAccount()) AccountSdk.linkageManager.getLinkage() else null
        Timber.i("linkageDto:${gson.toJson(link)}")
        return link
    }

    /**
     * 绑定当前登录账号
     * linkageId是当前cp的账号id
     * suspend函数，需要在协程中调用
     * 绑定失败会抛异常IllegalStateException
     */
    suspend fun linkAccount(linkageId: String) {
        try {
            AccountSdk.linkageManager.linkAccount(linkageId)
            if (accountStateLinkageEvent != null) {
                accountStateLinkageEvent?.linkageEventObserver(
                    isSuccessLinkage = true,
                    isBind = true
                )
            }
        } catch (e: IllegalStateException) {
            Timber.i("linkAccount e:${e.message}")
            if (accountStateLinkageEvent != null) {
                accountStateLinkageEvent?.linkageEventObserver(
                    isSuccessLinkage = false,
                    isBind = true
                )
            }
        }
    }

    /**
     * 与当前登录账号解绑
     * linkageId是当前cp的账号id
     * suspend函数，需要在协程中调用
     * 绑定失败会抛异常IllegalStateException
     */
    suspend fun unlinkAccount(linkageId: String) {
        try {
            AccountSdk.linkageManager.unlinkAccount(linkageId)
            if (accountStateLinkageEvent != null) {
                accountStateLinkageEvent?.linkageEventObserver(
                    isSuccessLinkage = true,
                    isBind = false
                )
            }
        } catch (e: Exception) {
            Timber.i("unlinkAccount e:${e.message}")
            if (accountStateLinkageEvent != null) {
                accountStateLinkageEvent?.linkageEventObserver(
                    isSuccessLinkage = false,
                    isBind = false
                )
            }
        }
    }

    /**
     * 启动账号App并打开指定页面
     * AccountDestination是可选，不传的话进入首页。
     */
    fun launchAccountApp() {//进入登录页
        AccountSdk.launchAccountApp(AccountDestination.Login())
    }

    /**
     * 提供CP账号id(注意：CP必要设置)
     * 提供CP账号id（注意：CP必要设置）
     * 提供CP登录账号ID，如果CP已登录，需要返回当前登录账号的id， 如果未登录，返回null或""
     * 需要在Application的oncCeate函数中设置此回调
     *  linkageId是当前CP的账号ID，用于绑定账号操作
     */
    fun setOnGetLinkageIdListener(uid: String) {
        AccountSdk.linkageCallbackManager.setOnGetLinkageIdListener {
            uid
        }
    }

    /**
     * 绑定拦截[可选]
     * 需要在Application的oncreate函数中设置此回调
     * Account App发起绑定后会触发此逻辑。如果在绑定时，CP需要触发特定流程（如绑定时CP需要先绑定CP后台，再绑定Account后台），请实现设置此回调。
     */
    fun setOnLinkListener() {
        AccountSdk.linkageCallbackManager.setOnLinkListener {
            // it.accountId： 账号id
            // it.token: 当前账号的登录token
            Timber.i("onLink: $it")
            // result: 绑定结果（成功或失败）;
            // linkageId: CP账号id;
            // extras: [可选] 需要额外保存到绑定关系中的数据，比如用于同登同退的CP token
            val linkageResult = LinkageResult(
                result = LinkageResultEnum.Success,
                linkageId = "LINKAGE_ID",
                extras = "something like token"
            )
            // 告诉Account app绑定结果
            setResult(linkageResult)
        }
    }

    /**
     * 解绑拦截[可选]
     * 需要在Application的oncreate函数中设置此回调
     * Account App发起绑定后会触发此逻辑。如果在绑定时，CP需要触发特定流程（如绑定时CP需要先绑定CP后台，再绑定Account后台），请实现设置此回调。
     */
    fun setOnUnlinkListener() {
        AccountSdk.linkageCallbackManager.setOnUnlinkListener {
            // it.accountId： 账号id
            // it.token: 当前账号的登录token
            Timber.i("onUnlink: $it")
            // result: 绑定结果（成功或失败）
            // linkageId: CP账号id
            val linkageResult = LinkageResult(LinkageResultEnum.Success)
            // 告诉Account app解绑结果
            setResult(linkageResult)
        }
    }

    /**
     * 获取数据
     */
    suspend fun getState(identifier: String): Bundle? {
        return if (CommonUtils.isVehicle() && CommonUtils.isUseVehicleAccount()) AccountSdk.stateManager.getState(identifier) else null
    }

    /**
     * 保存数据
     * identifier: 设置数据的key
     * content: 要保存的数据，String类型
     * 如果设置失败，会抛IllegalStateException
     */
    suspend fun setState(identifier: String, content: Bundle) {
        try {
            AccountSdk.stateManager.setState(identifier, content)
        } catch (e: IllegalStateException) {
            Timber.i("setState e:${e.message}")
        }
    }

    /**
     * 监听账号状态
     * 内部由StateFlow实现，监听后马上会有当前状态的回调，后续有变化时会回调
     * 回调参数：NotLoggedIn, LoggedIn(AccountDto)
     * 可以通过job取消监听
     */
    fun jobState(): Job {
        Timber.i("jobState isVehicle: ${CommonUtils.isVehicle()}")
        return if (CommonUtils.isVehicle() && CommonUtils.isUseVehicleAccount()) AccountSdk.accountManager.observeState {
            if (accountStateLinkageEvent != null) {
                Timber.i("jobState accountObserver")
                accountStateLinkageEvent?.accountObserver(it)
            }
        } else Job()
    }

    /**
     * 监听账号事件
     * 内部由SharedFlow实现，监听后不会有当前状态的回调，后续有变化时会回调
     * 回调参数：Login(LoginType, AccountDto), Logout(AccountDto), Delete(AccountDto), Update(AccountDto), Register(AccountDto)
     * 可以通过job取消监听
     */
    fun jobEvent(): Job {
        return if (CommonUtils.isVehicle() && CommonUtils.isUseVehicleAccount()) AccountSdk.accountManager.observeEvent {
            Timber.i("onAccountEventChanged: $it")
        } else Job()
    }

    /**
     * 监听账号绑定状态(Account app中发生绑定/解绑操作)
     * 监听账号绑定状态（账号app中发生绑定/解绑操作）
     * 内部由StateFlow实现，监听后马上会有当前状态的回调，后续有变化时会回调
     * 回调参数：NotLinked, Linked(LinkageDto)
     * 可以通过job取消监听
     */
    fun jobLinkageState(): Job {
        return if (CommonUtils.isVehicle() && CommonUtils.isUseVehicleAccount()) AccountSdk.linkageManager.observeState {
            Timber.i("onLinkageStateChanged: $it")
        } else Job()
    }

    /**
     * 监听账号绑定事件(Account app中发生绑定/解绑操作)
     * 监听账号绑定事件（账号app中发生绑定/解绑操作）
     * 内部由SharedFlow实现，监听后不会有当前状态的回调，后续有变化时会回调
     * 回调参数：Link(LinkageDto), Unlink(LinkageDto)
     * 可以通过job取消监听
     */
    fun jobLinkageEvent(): Job {
        return if (CommonUtils.isVehicle() && CommonUtils.isUseVehicleAccount())
            AccountSdk.linkageManager.observeState{
                try {
                    Timber.i("onLinkageEventChanged: $it")
                    if (accountStateLinkageEvent != null ) {
                        if (it is LinkageLifecycle.State.Linked) {
                            accountStateLinkageEvent?.linkageEventObserver(
                                isSuccessLinkage = true,
                                isBind = true
                            )
                        } else if (it is LinkageLifecycle.State.NotLinked) {
                            accountStateLinkageEvent?.linkageEventObserver(
                                isSuccessLinkage = true,
                                isBind = false
                            )
                        }
                    }
                } catch (e: IllegalStateException) {
                    Timber.i("jobLinkageEvent e:${e.message}")
                }
            } else Job()
    }

    /**
     * 监听event事件
     * 内部由SharedFlow实现，监听后不会有当前状态的回调，后续有变化时会回调
     * 可以通过job取消监听
     */
    fun jobPushEvent(messageType: String): Job {
        return if (CommonUtils.isVehicle() && CommonUtils.isUseVehicleAccount()) {
            Timber.i("jobPushEvent messageType:$messageType")
            AccountSdk.eventManager.observeEvent(messageType) {
                Timber.i("jobPushEvent observeEvent")
            }
        } else Job()
    }

    fun setAccountStateLinkageEvent(accountStateLinkageEvent: AccountStateLinkageEvent) {
        this.accountStateLinkageEvent = accountStateLinkageEvent
    }

    interface AccountStateLinkageEvent {
        fun accountObserver(state: AccountLifecycle.State)

        fun linkageEventObserver(isSuccessLinkage: Boolean, isBind: Boolean)//isSuccessLinkage接口调用是否成功 ，isBind true.为成功 false.为失败
    }
}