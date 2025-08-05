package com.desaysv.psmap.ui.home

import android.annotation.SuppressLint
import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.user.behavior.model.FavoriteBaseItem
import com.autonavi.gbl.user.behavior.model.FavoriteType
import com.autonavi.gbl.user.behavior.model.SimpleFavoriteItem
import com.autonavi.gbl.user.syncsdk.model.SyncEventType.SyncSdkEventBackupEnd
import com.autonavi.gbl.user.syncsdk.model.SyncEventType.SyncSdkEventSyncEnd
import com.autonavi.gbl.user.syncsdk.model.SyncMode
import com.autonavi.gbl.user.syncsdk.model.SyncRet
import com.autonavi.gbl.user.syncsdk.model.SyncRet.SyncRetSuccess
import com.autonavi.gbl.user.syncsdk.observer.ISyncSDKServiceObserver
import com.autonavi.gbl.util.errorcode.common.Service
import com.autosdk.bussiness.account.observer.BehaviorServiceObserver
import com.autosdk.bussiness.account.utils.ConverUtils
import com.autosdk.bussiness.common.POI
import com.autosdk.common.storage.MapSharePreference
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.business.SearchBusiness
import com.desaysv.psmap.base.business.UserBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.bean.MapCommandType
import com.desaysv.psmap.model.business.JsonStandardProtocolManager
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.di.ProvidesHiltModule
import com.desaysv.psmap.model.impl.IMapCommand
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject


/**
 * @author 王漫生
 * @description 收藏夹ViewModel
 */
@HiltViewModel
class HomeFavoriteViewModel @Inject constructor(
    private val userBusiness: UserBusiness,
    private val searchBusiness: SearchBusiness,
    private val settingAccountBusiness: SettingAccountBusiness,
    private val sharePreferenceFactory: SharePreferenceFactory,
    private val mRouteBusiness: RouteBusiness,
    private val mNaviBusiness: NaviBusiness,
    private val netWorkManager: NetWorkManager,
    private var jsonStandardProtocolManager: JsonStandardProtocolManager,
    private val application: Application,
    @ProvidesHiltModule.DefaultMapCommand private val mapCommand: IMapCommand
) : ViewModel() {
    val hasCollection = MutableLiveData(-1) //是否有收藏点 -1默认态 0.没有收藏点 1.有收藏点
    val isRefresh = MutableLiveData(false) //是否在更新中
    val isLoading = MutableLiveData(true) //是否加载中
    val isNight = MutableLiveData(NightModeGlobal.isNightMode())
    val setToast = MutableLiveData<String>() //toast
    val onRefreshData = MutableLiveData<Int>() //-3:家刷新-2:公司部刷新-1:全部刷新，其他是对应position刷新
    val showChangeLayout = MutableLiveData(false) //true.显示重命名布局
    val inputNameStr = MutableLiveData("") //重命名输入框输入
    val updateFavorite = MutableLiveData(-1) //重命名成功刷新对应的item

    var favoriteItems = ArrayList<SimpleFavoriteItem>() //普通收藏点List
    val routeErrorMessage: LiveData<String> = mRouteBusiness.routeErrorMessage
    private val syncSharePreference = sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.sycn)
    val loginLoading = settingAccountBusiness.loginLoading
    private var syncBtnClick = false //是否点击同步按钮
    var changePosition = -1 //重命名所对应的位置

    val naviErrorMessage: LiveData<String> = mNaviBusiness.naviErrorMessage
    val setNaviToast: LiveData<String> = mNaviBusiness.setToast

    val mapCommandBean = mapCommand.getMapCommand()

    override fun onCleared() {
        super.onCleared()
        userBusiness.unregisterISyncSDKServiceObserver(observer)
        userBusiness.unregisterBehaviorServiceObserver(iBehaviorServiceObserver)
        favoriteItems.clear()
    }

    //名称输入
    fun toSetChangeName(name: String) {
        if (TextUtils.equals(inputNameStr.value, name)) {
            Timber.i("当前的名称就是这个，已经输入了")
        } else {
            inputNameStr.postValue(name)
        }
    }

    //用户是否已经登录
    fun isLogin(): Boolean {
        return settingAccountBusiness.isLogin()
    }

    fun initData(isSync: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            // 切换到主线程更新 UI
            isLoading.postValue(true)
            if (isLogin() && netWorkManager.isNetworkConnected() && userBusiness.isSyncing() != 0 && isSync) {
                syncBtnClick = false
                isRefresh.postValue(true)
                userBusiness.startSync(true)
            }
            if (isSync) {
                userBusiness.registerISyncSDKServiceObserver(observer)
                userBusiness.registerBehaviorServiceObserver(iBehaviorServiceObserver)
            }
            Timber.d(" initData refreshFavoriteList isSync:$isSync")
            refreshFavoriteList()
            isLoading.postValue(false)
        }
    }

    private var iBehaviorServiceObserver: BehaviorServiceObserver = object : BehaviorServiceObserver {
        override fun notifyFavorite(baseItem: FavoriteBaseItem, isDelete: Boolean) {
            //不作处理
        }

        override fun notify(eventType: Int, exCode: Int) {
            Timber.d(" iBehaviorServiceObserver eventType:$eventType exCode:$exCode")
            if (eventType == SyncSdkEventBackupEnd || eventType == SyncSdkEventSyncEnd) {
                if (exCode == SyncRet.SyncRetNetworkError) {
                    if (!netWorkManager.isNetworkConnected()) { //没有网络
                        setToast.postValue(application.getString(com.desaysv.psmap.base.R.string.sv_common_network_anomaly_please_try_again))
                        isRefresh.postValue(false)
                    }
                } else if (exCode == SyncRet.SyncRetSuccess) {
                    viewModelScope.launch(Dispatchers.IO) {
                        Timber.d(" iBehaviorServiceObserver refreshFavoriteList")
                        refreshFavoriteList()
                    }
                } else {
                    isRefresh.postValue(false)
                }
            }
        }

        override fun notify(i: Int, arrayList: ArrayList<SimpleFavoriteItem>, b: Boolean) {
            //不作处理
        }
    }
    private var observer = ISyncSDKServiceObserver { eventType: Int, exCode: Int ->
        Timber.d(" observer refreshFavoriteList ISyncSDKServiceObserver eventType:$eventType exCode:$exCode")
        if (eventType == SyncSdkEventSyncEnd && exCode == SyncRetSuccess) {
            viewModelScope.launch(Dispatchers.IO) {
                Timber.d(" observer SyncSdkEventSyncEnd refreshFavoriteList")
                updateSyncTime()
                refreshFavoriteList()
                isRefresh.postValue(false)
                if (syncBtnClick) {
                    syncBtnClick = false
                    setToast.postValue("同步完成")
                }
            }
        }
    }

    //保存同步时间
    @SuppressLint("SimpleDateFormat")
    fun updateSyncTime() {
        syncSharePreference.putStringValue(
            MapSharePreference.SharePreferenceKeyEnum.sycnTime,
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        )
    }

    fun refreshFavoriteList() {
        viewModelScope.launch {
            // 切换到主线程更新 UI
            favoriteItems.clear()
            val poi = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypePoi, true)
            favoriteItems.addAll(poi ?: ArrayList())
            hasCollection.postValue(if (favoriteItems.isNotEmpty()) 1 else 0)
            Timber.d("refreshFavoriteList favoriteItems size:%s", favoriteItems.size)
            withContext(Dispatchers.Main) {
                onRefreshData.postValue(-1)
            }
            mapCommand.notifyMapCommandResult(MapCommandType.NaviToFavorite, poi)
        }
    }

    //取消收藏
    fun cancelFavorite(simpleFavoriteItem: SimpleFavoriteItem?, position: Int, type: Int) { //1:家 2:公司 3.普通收藏点
        val favoriteBaseItem = FavoriteBaseItem().apply {
            item_id = simpleFavoriteItem?.item_id ?: ""
        }
        val favoriteItem = userBusiness.getFavorite(favoriteBaseItem)
        if (null != favoriteItem) {
            favoriteBaseItem.poiid = favoriteItem.poiid
            val del = userBusiness.delFavorite(favoriteBaseItem, SyncMode.SyncModeNow)
            if (del == 0) {
                setToast.postValue("已取消收藏")
                when (type) { //-3:家刷新-2:公司部刷新-1:全部刷新，其他是对应position刷新
                    else -> {
                        onRefreshData.postValue(position)
                    }
                }
            } else {
                setToast.postValue("取消收藏失败")
            }
        } else {
            setToast.postValue("取消收藏失败")
        }
    }

    /**
     * 显示POI详情
     */
    fun showPoiCard(simpleFavoriteItem: SimpleFavoriteItem) {
        Timber.i("showPoiCard")
        mapCommand.showPoiDetail(ConverUtils.converSimpleFavoriteToPoi(simpleFavoriteItem))
    }

    /**
     * 置顶收藏点操作
     */
    fun topFavorite(simpleFavoriteItem: SimpleFavoriteItem, top: Boolean): Int {
        return userBusiness.topFavorite(simpleFavoriteItem, top, SyncMode.SyncModeNow)
    }

    //重命名提交
    fun submitFavoriteName() {
        if (changePosition != -1 && favoriteItems.size > 0 && changePosition < favoriteItems.size) {
            val favoriteBaseItem = FavoriteBaseItem().apply {
                item_id = favoriteItems[changePosition].item_id ?: ""
            }
            val favoriteItem = userBusiness.getFavorite(favoriteBaseItem)
            if (null != favoriteItem) {
                favoriteItem.custom_name = inputNameStr.value
                val result = userBusiness.updateFavorite(favoriteItem, SyncMode.SyncModeNow)
                if (result == Service.ErrorCodeOK) {
                    favoriteItems[changePosition].custom_name = inputNameStr.value
                    updateFavorite.postValue(changePosition)
                    showChangeLayout.postValue(false)
                    setToast.postValue(application.getString(R.string.sv_setting_change_favorite_name_success))
                } else {
                    setToast.postValue(application.getString(R.string.sv_setting_change_favorite_name_fail))
                }
            } else {
                setToast.postValue(application.getString(R.string.sv_setting_change_favorite_name_fail))
                Timber.i(" favoriteItem == null ")
            }
        } else {
            setToast.postValue(application.getString(R.string.sv_setting_change_favorite_name_fail))
            Timber.i(" position size error changePosition:$changePosition size:${favoriteItems.size}")
        }
    }

    /**
     * 规划路线
     */
    fun planRoute(commandBean: CommandRequestRouteNaviBean?) {
        Timber.i("planRoute")
        viewModelScope.launch {
            val start = commandBean?.start
            val end = commandBean?.end
            val midPois = commandBean?.midPois
            if (start != null && end != null) {
                mRouteBusiness.planRoute(start, end, midPois)
            }
        }
    }

    /**
     * 添加途经点
     */
    fun addWayPointPlan(poi: POI?) = mRouteBusiness.addWayPoint(poi)

    /**
     * 添加途经点
     */
    fun addWayPoint(poi: POI?) = mNaviBusiness.addWayPoint(poi)


    fun notifyPosRankCommandResult(result: Boolean, tips: String) {
        Timber.i("notifyPosRankCommandResult result = $result,tips = $tips")
        mapCommand.notifyMapCommandResult(MapCommandType.PosRank, Pair(result, tips))
    }

    fun notifyPageRankCommandResult(result: Boolean, tips: String) {
        Timber.i("notifyPageRankCommandResult result = $result,tips = $tips")
        mapCommand.notifyMapCommandResult(MapCommandType.PageRank, Pair(result, tips))
    }
}