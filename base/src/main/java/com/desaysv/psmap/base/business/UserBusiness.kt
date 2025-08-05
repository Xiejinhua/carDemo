package com.desaysv.psmap.base.business

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.autonavi.gbl.aosclient.model.GAddressPredictRequestParam
import com.autonavi.gbl.aosclient.model.GQRCodeConfirmRequestParam
import com.autonavi.gbl.aosclient.model.GRangeSpiderRequestParam
import com.autonavi.gbl.aosclient.model.GSendToPhoneRequestParam
import com.autonavi.gbl.aosclient.model.GTrafficEventDetailRequestParam
import com.autonavi.gbl.aosclient.model.GTrafficEventDetailResponseParam
import com.autonavi.gbl.aosclient.model.GWorkdayListRequestParam
import com.autonavi.gbl.aosclient.model.GWsPpAutoWeixinQrcodeRequestParam
import com.autonavi.gbl.aosclient.model.GWsPpAutoWeixinStatusRequestParam
import com.autonavi.gbl.aosclient.model.GWsPpAutoWeixinUnbindRequestParam
import com.autonavi.gbl.aosclient.model.GWsTserviceInternalLinkCarReportRequestParam
import com.autonavi.gbl.aosclient.observer.ICallBackAddressPredict
import com.autonavi.gbl.aosclient.observer.ICallBackQRCodeConfirm
import com.autonavi.gbl.aosclient.observer.ICallBackRangeSpider
import com.autonavi.gbl.aosclient.observer.ICallBackSendToPhone
import com.autonavi.gbl.aosclient.observer.ICallBackTrafficEventDetail
import com.autonavi.gbl.aosclient.observer.ICallBackWorkdayList
import com.autonavi.gbl.aosclient.observer.ICallBackWsPpAutoWeixinQrcode
import com.autonavi.gbl.aosclient.observer.ICallBackWsPpAutoWeixinStatus
import com.autonavi.gbl.aosclient.observer.ICallBackWsPpAutoWeixinUnbind
import com.autonavi.gbl.aosclient.observer.ICallBackWsTserviceInternalLinkCarReport
import com.autonavi.gbl.user.account.model.AccountProfile
import com.autonavi.gbl.user.behavior.model.FavoriteBaseItem
import com.autonavi.gbl.user.behavior.model.FavoriteItem
import com.autonavi.gbl.user.behavior.model.FavoriteType
import com.autonavi.gbl.user.behavior.model.FavoriteType.FavoriteType1
import com.autonavi.gbl.user.behavior.model.SimpleFavoriteItem
import com.autonavi.gbl.user.model.BehaviorDataType.BehaviorDataType1
import com.autonavi.gbl.user.model.UserLoginInfo
import com.autonavi.gbl.user.syncsdk.model.SyncMode
import com.autonavi.gbl.user.syncsdk.model.SyncMode.SyncMode1
import com.autonavi.gbl.user.syncsdk.observer.ISyncSDKServiceObserver
import com.autonavi.gbl.user.usertrack.model.HistoryRouteItem
import com.autonavi.gbl.user.usertrack.model.SearchHistoryItem
import com.autonavi.gbl.user.usertrack.observer.IGpsInfoGetter
import com.autonavi.gbl.util.errorcode.common.Service
import com.autosdk.bussiness.account.AccountController
import com.autosdk.bussiness.account.BehaviorController
import com.autosdk.bussiness.account.LinkCarController
import com.autosdk.bussiness.account.SyncSdkController
import com.autosdk.bussiness.account.UserGroupController
import com.autosdk.bussiness.account.UserTrackController
import com.autosdk.bussiness.account.observer.BehaviorServiceObserver
import com.autosdk.bussiness.account.utils.ConverUtils
import com.autosdk.bussiness.aos.AosController
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.common.POIFactory
import com.autosdk.bussiness.layer.LayerController
import com.autosdk.bussiness.layer.UserBehaviorLayer
import com.autosdk.bussiness.map.SurfaceViewID
import com.autosdk.bussiness.search.request.SearchQueryType
import com.autosdk.bussiness.search.utils.SearchDataConvertUtils
import com.autosdk.common.AutoConstant
import com.autosdk.common.AutoStatus
import com.autosdk.common.storage.MapSharePreference
import com.desaysv.psmap.base.R
import com.desaysv.psmap.base.bean.HomeFavoriteItem
import com.desaysv.psmap.base.bean.HomeFavoriteItemType
import com.desaysv.psmap.base.bean.SearchHistoryType
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine


/**
 * 用户数据管理
 * 比如账号信息，用户行为，收藏夹等
 */
@Singleton
class UserBusiness @Inject constructor(
    @ApplicationContext private val context: Context,
    private val accountController: AccountController,
    private val behaviorController: BehaviorController,
    private val linkCarController: LinkCarController,
    private val syncSdkController: SyncSdkController,
    private val userGroupController: UserGroupController,
    private val userTrackController: UserTrackController,
    private val aosController: AosController,
    private val sharePreferenceFactory: SharePreferenceFactory,
    private val layerController: LayerController
) {

    val addFavoriteResultVoice = MutableLiveData<Map<Int, POI>>()
    val delFavoriteResultVoice = MutableLiveData<Map<Int, POI>>()
    val updateFavoriteResultVoice = MutableLiveData<Map<Int, POI>>()
    val openUserFavorite = MutableLiveData<Boolean>() //打开收藏夹

    private val userBehaviorLayer: UserBehaviorLayer by lazy {
        layerController.getUserBehaviorLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }


    /**
     * 账号服务初始化
     */
    fun initAccount() {
        accountController.initService(AutoConstant.ACCOUNT_DIR)
    }

    /**
     * 用户行为初始化
     */
    fun initBehavior() {
        behaviorController.init()
    }

    /**
     * 同步服务初始化
     */
    fun initSyncSdk() {
        syncSdkController.init(AutoConstant.SYNC_DIR)
    }

    /**
     * 组队服务初始化
     */
    fun initUserGroup() {
        userGroupController.initService()
    }

    /**
     * 轨迹服务初始化
     */
    fun initUserTrack() {
        userTrackController.initService()
    }

    /**
     * Aos服务初始化
     */
    fun initAos() {
        aosController.initAosService()
    }

    // ============================ AccountController管理类相关 ================================

    fun accountInfo(): AccountProfile? = accountController.accountInfo

    /**
     * 获取二维码
     */
    fun getQRImage() {
        accountController.requestQrCodeLogin()
    }

    /**
     * 长轮询二维码是否被登录
     */
    fun requestCodeConfirm(qrCodeId: String): AccountController.AccountResult =
        accountController.requestCodeConfirm(qrCodeId)

    /**
     * 获取登录账号信息
     */
    fun requestAccountProfile(): Int = accountController.requestAccountProfile()

    /**
     * 车企账号绑定
     */
    fun requestAccountBind(authId: String, deviceCode: String): Int =
        accountController.requestAccountBind(BaseConstant.SOURCE_ID, authId, deviceCode)

    /**
     * 车企账号解绑
     */
    fun requestUnBindAccount(authId: String, deviceCode: String): Int =
        accountController.requestUnBindAccount(BaseConstant.SOURCE_ID, authId, deviceCode)

    /**
     * 保存用户信息
     */
    fun saveUserData(userData: AccountProfile?) {
        accountController.saveUserData(userData)
    }

    /**
     * 删除用户数据
     */
    fun deleteAccountInfo(): Int {
        return accountController.deleteAccountInfo()
    }

    /**
     * 退出登录账号
     */
    fun requestAccountLogout() = accountController.requestAccountLogout()

    /**
     * 验证账号是否存在
     */
    fun requestAccountCheck(mobile: String?) {
        accountController.requestAccountCheck(mobile)
    }

    /**
     * 获取验证码
     * @param mobile 登录手机号
     */
    fun getVerficationCode(mobile: String?, isAccountExist: Boolean) {
        accountController.getVerficationCode(mobile, isAccountExist)
    }

    /**
     * 账号验证码注册
     * @param mobile 手机账号
     * @param code   验证码
     */
    fun accountRegist(mobile: String?, code: String?) {
        accountController.accountRegist(mobile, code)
    }

    /**
     * 手机验证码登录
     * @param mobile
     * @param code
     */
    fun mobileLogin(mobile: String?, code: String?) {
        accountController.mobileLogin(mobile, code)
    }

    /**
     * 车企账号快速登录
     */
    fun requestQuickLogin(authId: String, userId: String?): Int =
        accountController.requestQuickLogin(BaseConstant.SOURCE_ID, authId, userId)

    fun accountAbort(taskId: Int) {
        accountController.abort(taskId.toLong())
    }


    // ============================ BehaviorController管理类相关 ================================
    /**
     * 设置用户登录信息
     */
    fun setLoginInfo(param: UserLoginInfo?): Int = behaviorController.setLoginInfo(param)

    fun registerBehaviorServiceObserver(behaviorServiceObserver: BehaviorServiceObserver) {
        behaviorController.registerBehaviorServiceObserver(behaviorServiceObserver)
    }

    fun unregisterBehaviorServiceObserver(behaviorServiceObserver: BehaviorServiceObserver) {
        behaviorController.unregisterBehaviorServiceObserver(behaviorServiceObserver)
    }

    /**
     * @return 精简信息收藏点列表
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * @brief 获取精简信息收藏点列表
     * @param[in] type             收藏类别
     * @param[in] sorted           是否排序
     * @note thread:multi
     */
    fun getSimpleFavoriteList(type: Int, sorted: Boolean): ArrayList<SimpleFavoriteItem>? {
        return behaviorController.getSimpleFavoriteList(type, sorted)
    }

    /**
     * 异步获取收藏点列表
     */
    fun getFavoriteListAsync(@FavoriteType1 type: Int, sorted: Boolean) {
        behaviorController.getFavoriteListAsync(type, sorted)
    }

    /**
     * 移除家 ， 或者公司 ， 使得家或公司始终为1个
     *
     * @param removeItems
     */
    fun removeFavorites(removeItems: ArrayList<SimpleFavoriteItem>) =
        behaviorController.removeFavorites(removeItems)

    suspend fun getHomeFavorites(): List<HomeFavoriteItem> {
        val favorites = mutableListOf<HomeFavoriteItem>()
        /*favorites.add(0, HomeFavoriteItem(HomeFavoriteItemType.NULL_HOME, POIFactory.createPOI().apply {
            id = "null_home"
            name = context.getString(R.string.sv_common_home)
            addr = context.getString(R.string.sv_common_home_tips)
        }))
        favorites.add(1, HomeFavoriteItem(HomeFavoriteItemType.NULL_COMPANY, POIFactory.createPOI().apply {
            id = "null_Company"
            name = context.getString(R.string.sv_common_company)
            addr = context.getString(R.string.sv_common_company_tips)
        }))*/

        behaviorController.getSimpleFavoriteList(FavoriteType.FavoriteTypeHome, true)?.let {
            if (it.isNotEmpty())
                favorites.add(
                    HomeFavoriteItem(
                        HomeFavoriteItemType.HOME,
                        ConverUtils.converSimpleFavoriteItemToPoi(it[0])
                    )
                )
        }

        behaviorController.getSimpleFavoriteList(FavoriteType.FavoriteTypeCompany, true)?.let {
            if (it.isNotEmpty())
                favorites.add(
                    HomeFavoriteItem(
                        HomeFavoriteItemType.COMPANY,
                        ConverUtils.converSimpleFavoriteItemToPoi(it[0])
                    )
                )
        }

        behaviorController.getSimpleFavoriteList(FavoriteType.FavoriteTypePoi, true)?.let {
            it.map {
                favorites.add(
                    HomeFavoriteItem(
                        HomeFavoriteItemType.FAVORITE,
                        ConverUtils.converSimpleFavoriteItemToPoi(it)
                    )
                )
            }
        }

        if (favorites.isEmpty()) {
            favorites.add(
                HomeFavoriteItem(
                    HomeFavoriteItemType.NULL_FAVORITE,
                    POIFactory.createPOI().apply {
                        id = "null_favorite"
                        name = context.getString(R.string.sv_common_favorites_empty)
                        addr = context.getString(R.string.sv_common_favorites_empty)
                    })
            )
        }

        return favorites
    }

    fun getHomePoi(): POI? {
        return behaviorController.getSimpleFavoriteList(FavoriteType.FavoriteTypeHome, true)?.run {
            ConverUtils.converSimpleFavoriteItemToPoi(this[0])
        }
    }

    fun getCompanyPoi(): POI? {
        return behaviorController.getSimpleFavoriteList(FavoriteType.FavoriteTypeCompany, true)?.run {
            ConverUtils.converSimpleFavoriteItemToPoi(this[0])
        }
    }

    /**
     * @return 收藏点信息
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * @brief 通过基础信息获取收藏点
     * @param[in] base             基础信息
     * @note thread:multi
     */
    fun getFavorite(base: FavoriteBaseItem): FavoriteItem? {
        return behaviorController.getFavorite(base)
    }

    /**
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * @brief 删除指定收藏点
     * @param[in] base             基础信息
     * @param[in] mode             同步方式
     * @note thread:multi
     */
    fun delFavorite(base: FavoriteBaseItem, @SyncMode.SyncMode1 mode: Int): Int {
        val favoriteItem = getFavorite(base)
        val result = behaviorController.delFavorite(base, mode)
        if (result == Service.ErrorCodeOK && favoriteItem != null && (favoriteItem.common_name == FavoriteType.FavoriteTypeHome || favoriteItem.common_name == FavoriteType.FavoriteTypeCompany)) {
            delFavoriteResultVoice.postValue(mapOf(favoriteItem.common_name to ConverUtils.converFavoriteItemToPOI(favoriteItem)))
        }
        return result
    }

    /**
     * 清空所有收藏点，谨慎使用
     */
    fun clearAllFavorites(@SyncMode.SyncMode1 mode: Int = SyncMode.SyncModeNow): Int {
        return behaviorController.clearFavorite(mode)
    }

    fun isFavorited(poi: POI): Boolean {
        return behaviorController.isFavorited(ConverUtils.converPOIToFavoriteBaseItem(poi)) == Service.ErrorCodeOK
    }

    fun addFavorite(
        poi: POI,
        @SyncMode.SyncMode1 syncMode: Int = SyncMode.SyncModeNow,
        @FavoriteType.FavoriteType1 type: Int = FavoriteType.FavoriteTypePoi
    ): Boolean {
        when (type) {
            FavoriteType.FavoriteTypeHome -> AutoStatusAdapter.sendStatus(AutoStatus.HOME_ADDR_CHANGED)
            FavoriteType.FavoriteTypeCompany -> AutoStatusAdapter.sendStatus(AutoStatus.COMPANY_ADDR_CHANGED)
            FavoriteType.FavoriteTypePoi -> AutoStatusAdapter.sendStatus(AutoStatus.FAVORITE_ADDR_CHANGED)
            else -> {}
        }
        val result = behaviorController.addFavorite(
            ConverUtils.converPOIToFavoriteItem(poi, type),
            syncMode
        )
        if (result == Service.ErrorCodeOK) {
            addFavoriteResultVoice.postValue(mapOf(type to poi))
        }

        return result == Service.ErrorCodeOK
    }

    fun delFavorite(poi: POI, @SyncMode.SyncMode1 syncMode: Int = SyncMode.SyncModeNow): Boolean {
        when (poi.id) {
            getHomePoi()?.id -> {
                AutoStatusAdapter.sendStatus(AutoStatus.HOME_ADDR_CHANGED)
            }

            getCompanyPoi()?.id -> {
                AutoStatusAdapter.sendStatus(AutoStatus.COMPANY_ADDR_CHANGED)
            }

            else -> {
                AutoStatusAdapter.sendStatus(AutoStatus.FAVORITE_ADDR_CHANGED)
            }
        }

        val favoriteBaseItem = ConverUtils.converPOIToFavoriteBaseItem(poi)
        val favoriteItem = getFavorite(favoriteBaseItem)
        val result = behaviorController.delFavorite(
            favoriteBaseItem,
            syncMode
        )
        if (result == Service.ErrorCodeOK && favoriteItem != null && (favoriteItem.common_name == FavoriteType.FavoriteTypeHome || favoriteItem.common_name == FavoriteType.FavoriteTypeCompany)) {
            delFavoriteResultVoice.postValue(mapOf(favoriteItem.common_name to poi))
        }
        return result == Service.ErrorCodeOK
    }

    /**
     * 是否显示所有收藏扎点
     */
    fun showAllFavoritesItem(isShow: Boolean) {
        Timber.i("showAllFavoritesItem $isShow")
        behaviorController.allFavoriteItem?.let {
            if (isShow) userBehaviorLayer.updateFavoriteMainByFavoriteItem(it)
            else userBehaviorLayer.clearAllItems()
        }
    }

    /**
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * @brief 置顶收藏点
     * @param base             基础信息
     * @param top              是否置顶
     * @param mode             同步方式
     * @note thread:multi
     */
    fun topFavorite(simpleFavoriteItem: SimpleFavoriteItem, top: Boolean, @SyncMode1 mode: Int): Int {
        val favoriteBaseItem = FavoriteBaseItem()
        favoriteBaseItem.item_id = simpleFavoriteItem.item_id
        return behaviorController.topFavorite(favoriteBaseItem, top, mode)
    }

    /**
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * @brief 更新收藏点
     * @param item             收藏点信息
     * @param mode             同步方式
     * @note thread:multi
     */
    fun updateFavorite(item: FavoriteItem?, @SyncMode1 mode: Int): Int {
        Timber.i("updateFavorite item.name:${item?.name}, item.poiid:${item?.poiid}, item.address:${item?.address}, item.point_x:${item?.point_x}, item.point_y:${item?.point_y}, item.common_name:${item?.common_name}")
        val poiList = getSimpleFavoriteList(FavoriteType.FavoriteTypePoi, true)
        val homeList = getSimpleFavoriteList(FavoriteType.FavoriteTypeHome, true)
        val companyList = getSimpleFavoriteList(FavoriteType.FavoriteTypeCompany, true)
        var isUpdate = true
        if (!poiList.isNullOrEmpty()) {
            poiList.forEach { simpleFavoriteItem ->
                val favoriteBaseItem = FavoriteBaseItem()
                favoriteBaseItem.item_id = simpleFavoriteItem.item_id
                val favoriteItem = getFavorite(favoriteBaseItem)
                if (favoriteItem?.poiid == item?.poiid) {
                    behaviorController.delFavorite(favoriteBaseItem, mode)
                    isUpdate = false
                }
            }
        }
        if (!homeList.isNullOrEmpty()) {
            homeList.forEach { simpleFavoriteItem ->
                val favoriteBaseItem = FavoriteBaseItem()
                favoriteBaseItem.item_id = simpleFavoriteItem.item_id
                val favoriteItem = getFavorite(favoriteBaseItem)
                if (favoriteItem?.poiid == item?.poiid) {
                    behaviorController.delFavorite(favoriteBaseItem, mode)
                    isUpdate = false
                }
            }
        }
        if (!companyList.isNullOrEmpty()) {
            companyList.forEach { simpleFavoriteItem ->
                val favoriteBaseItem = FavoriteBaseItem()
                favoriteBaseItem.item_id = simpleFavoriteItem.item_id
                val favoriteItem = getFavorite(favoriteBaseItem)
                if (favoriteItem?.poiid == item?.poiid) {
                    behaviorController.delFavorite(favoriteBaseItem, mode)
                    isUpdate = false
                }
            }
        }
        var result: Int = if (isUpdate) {
            behaviorController.updateFavorite(item, mode)
        } else {
            behaviorController.addFavorite(item, mode)
        }
        if (result == Service.ErrorCodeOK && item != null && (item.common_name == FavoriteType.FavoriteTypeHome || item.common_name == FavoriteType.FavoriteTypeCompany)) {
            updateFavoriteResultVoice.postValue(mapOf(item.common_name to ConverUtils.converFavoriteItemToPOI(item)))
        }
        return result
    }

    /**
     * 同步常去地点(家/公司)到云端
     */
    fun syncFrequentData(): Int {
        return behaviorController.syncFrequentData()
    }


    // ============================ LinkCarController管理类相关 ================================
    /**
     * 手车互联--获取手机登录状态
     */
    fun getLinkPhoneStatus() {
        linkCarController.getLinkPhoneStatus {
            Timber.e("手车互联:第二步发送车机的当前定位给手机")
        }
    }

    /**
     * 手车互联--间隔60s上报车机状态
     */
    fun startLinkCarReport(
        pAosRequest: GWsTserviceInternalLinkCarReportRequestParam?,
        pAosCallbackRef: ICallBackWsTserviceInternalLinkCarReport
    ) {
//        linkCarController.startLinkCarReport(pAosRequest, pAosCallbackRef)
    }

    // ============================ SyncSdkController管理类相关 ================================
    /**
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * @brief 添加轨迹服务观察者
     * @param[in] ob:              轨迹服务观察者
     * @note thread:mutil
     */
    fun registerISyncSDKServiceObserver(ob: ISyncSDKServiceObserver) {
        syncSdkController.registerISyncSDKServiceObserver(ob)
    }

    /**
     * @return void
     * @brief 移除轨迹服务观察者
     * @param[in] ob:              轨迹服务观察者
     * @note thread:mutil
     */
    fun unregisterISyncSDKServiceObserver(ob: ISyncSDKServiceObserver) {
        syncSdkController.unregisterISyncSDKServiceObserver(ob)
    }

    /**
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * @brief 是否正在同步
     * @note thread:multi
     */
    fun isSyncing(): Int {
        return syncSdkController.isSyncing()
    }

    /**
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * @brief 手动开始同步
     * @note thread:multi
     */
    fun startSync(isSyncFrequentData: Boolean = false): Int {
        if (isSyncFrequentData) {
            syncFrequentData() //同步常去地点(家/公司)到云端
        }
        return syncSdkController.startSync()
    }

    // ============================ UserTrackController管理类相关 ================================
    fun registerGpsInfoGetter(gpsInfoGetter: IGpsInfoGetter?) {
        userTrackController.registerGpsInfoGetter(gpsInfoGetter)
    }

    fun unregisterGpsInfoGetter(gpsInfoGetter: IGpsInfoGetter?) {
        userTrackController.unregisterGpsInfoGetter(gpsInfoGetter)
    }

    /**
     * 添加搜索记录
     *
     * @param keyword 关键字
     * @param poi 当搜索结果只有一个POI地点时，不保存搜索关键字，改为保存该POI
     * @param searchQuery 搜索为周边搜还是普通搜索
     */
    fun addSearchHistory(
        keyword: String? = null,
        poi: POI? = null,
        @SearchQueryType searchQuery: String = SearchQueryType.NORMAL
    ) {
        Timber.d("addSearchHistory() called with: keyword = $keyword, poi = $poi")
        keyword?.let {
            val searchHistoryItem = SearchHistoryItem()
            searchHistoryItem.name = keyword
            searchHistoryItem.history_type = SearchHistoryType.NORMOL.value
            searchHistoryItem.search_query = searchQuery
            userTrackController.addSearchHistory(searchHistoryItem, SyncMode.SyncModeNow)
        }
        poi?.let {
            userTrackController.addSearchHistory(
                SearchDataConvertUtils.convertPoiToSearchHistoryItem(
                    poi,
                    poi.name,
                    SearchHistoryType.DETAIL.value
                ),
                SyncMode.SyncModeNow
            )
        }
    }

    /**
     * 添加导航记录
     *
     * @param item 历史路线信息
     */
    fun addRouteHistory(item: HistoryRouteItem) =
        userTrackController.addHistoryRoute(item, SyncMode.SyncModeNow)

    /**
     * 删除搜索历史及导航历史
     *
     */
    fun clearSearchHistory(): Boolean {
        val result = userTrackController.clearSearchHistory(SyncMode.SyncModeNow)
        val resultRoute = userTrackController.clearHistoryRoute(SyncMode.SyncModeNow)
        return (result == 0 && resultRoute == 0)
    }

    /**
     * 获取搜索历史
     *
     */
    fun getSearchHistory(): ArrayList<SearchHistoryItem>? = userTrackController.searchHistory

    /**
     * 获取导航历史
     *
     */
    fun getHistoryRouteItem(): ArrayList<HistoryRouteItem>? = userTrackController.historyRoute

    /**
     * 删除搜索历史
     *
     */
    fun delSearchHistory(item: SearchHistoryItem, @SyncMode.SyncMode1 mode: Int): Int =
        userTrackController.delSearchHistory(item, mode)

    /**
     * 删除导航历史
     *
     */
    fun delHistoryRoute(item: HistoryRouteItem, @SyncMode.SyncMode1 mode: Int): Int =
        userTrackController.delHistoryRoute(item, mode)

    /**
     * 处理未完成的轨迹并开始轨迹
     *
     * 删除180天文件－－－处理未完成－－－开始轨迹
     */
    fun startTrackAndhandleUnfinishTrace(fileName: String) =
        userTrackController.startTrackAndhandleUnfinishTrace(
            AutoConstant.SYNC_DIR + "403",
            fileName
        )

    /**
     * 关闭 GPS 跟踪
     */
    fun closeGpsTrack() = userTrackController.closeGpsTrack(AutoConstant.SYNC_DIR + "403")

    /**
     * @param type 行为数据类型
     * @param id   行为数据id
     * @return dice::String16   行为数据
     * 通过id获取行为数据
     * thread:mutil
     */
    fun getBehaviorDataById(@BehaviorDataType1 type: Int, id: Int): String? {
        return userTrackController.getBehaviorDataById(type, id)
    }

    /**
     * @param type 行为数据类型
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * 获取行为数据id列表
     * @param[out] ids              行为数据id列表
     * thread:mutil
     */
    fun getBehaviorDataIds(@BehaviorDataType1 type: Int): IntArray? {
        return userTrackController.getBehaviorDataIds(type)
    }

    /**
     * @param type 行为数据类型
     * @param mode 同步方式
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * 删除行为数据
     * thread:mutil
     */
    fun clearBehaviorData(@BehaviorDataType1 type: Int, @SyncMode1 mode: Int): Int {
        return userTrackController.clearBehaviorData(type, mode)
    }

    /**
     * 处理未完成的轨迹
     *
     * 删除180天文件－－－处理未完成
     */
    fun handleUnfinishTrace() {
        userTrackController.handleUnfinishTrace(AutoConstant.SYNC_DIR + "403")
    }

    fun getCurrentFileName(): String {
        return userTrackController.getmCurrentFileName()
    }


    // ============================ AosController管理类相关 ================================
    /**
     * 可达范围
     */
    fun sendReqRangeSpider(
        pAosRequest: GRangeSpiderRequestParam?,
        callBackRangeSpider: ICallBackRangeSpider
    ): Long {
        return aosController.sendReqRangeSpider(pAosRequest, callBackRangeSpider)
    }

    /**
     * 交通事件详情
     */
    fun sendReqTrafficEventDetail(
        pAosRequest: GTrafficEventDetailRequestParam?,
        callBackTrafficEventDetail: ICallBackTrafficEventDetail
    ): Long {
        return aosController.sendReqTrafficEventDetail(pAosRequest, callBackTrafficEventDetail)
    }

    /**
     * 交通事件详情
     */
    suspend fun sendReqTrafficEventDetail(trafficEventID: String): Result<GTrafficEventDetailResponseParam> {
        val requestParam = GTrafficEventDetailRequestParam()
        requestParam.eventid = trafficEventID
        return withTimeoutOrNull(8000) {
            suspendCancellableCoroutine { continuation ->
                aosController.sendReqTrafficEventDetail(
                    requestParam
                ) { gTrafficEventDetailRequestParam ->
                    continuation.resume(Result.success(gTrafficEventDetailRequestParam))
                }
            }
        } ?: Result.error("Time Out!")
    }

    /**
     * 微信状态
     */
    fun sendReqWsPpAutoWXStatus(pAosCallbackRef: ICallBackWsPpAutoWeixinStatus): Long {
        return aosController.sendReqWsPpAutoWeixinStatus(GWsPpAutoWeixinStatusRequestParam().apply {
            product = 1
        }, pAosCallbackRef)
    }

    /**
     * 微信互联--获取微信互联二维码
     */
    fun sendReqWsPpAutoWXQrcode(pAosCallbackRef: ICallBackWsPpAutoWeixinQrcode): Long {
        return aosController.sendReqWsPpAutoWeixinQrcode(GWsPpAutoWeixinQrcodeRequestParam().apply {
            product = 1
        }, pAosCallbackRef)
    }

    /**
     * 微信解绑
     */
    fun sendReqWsPpAutoWXUnbind(pAosCallbackRef: ICallBackWsPpAutoWeixinUnbind): Long {
        return aosController.sendReqWsPpAutoWeixinUnbind(GWsPpAutoWeixinUnbindRequestParam().apply {
            product = 1
        }, pAosCallbackRef)
    }

    /**
     * 网络请求,内存由HMI管理,sns,轮询当前二维码扫描状态
     */
    fun sendReqQRCodeConfirm(qrCodeId: String?, pAosCallbackRef: ICallBackQRCodeConfirm?): Long {
        return aosController.sendReqQRCodeConfirm(GQRCodeConfirmRequestParam().apply {
            QRCodeId = qrCodeId
            TypeId = 2
        }, pAosCallbackRef)
    }

    /**
     * 导航最后一公里推送手机
     */
    fun sendReqSendToPhone(param: GSendToPhoneRequestParam, callback: ICallBackSendToPhone): Long {
        return aosController.sendReqSendToPhone(param, callback)
    }

    /**
     * 预测用户家/公司的位置
     */
    fun sendReqAddressPredict(
        pAosRequest: GAddressPredictRequestParam?,
        iCallBackAddressPredict: ICallBackAddressPredict
    ): Long {
        return aosController.sendReqAddressPredict(pAosRequest, iCallBackAddressPredict)
    }

    /**
     * 获取节假日信息
     */
    fun sendReqWorkdayList(
        pAosRequest: GWorkdayListRequestParam?,
        iCallBackWorkdayList: ICallBackWorkdayList
    ): Long {
        return aosController.sendReqWorkdayList(pAosRequest, iCallBackWorkdayList)
    }

    //用户是否已经登录
    fun isLogin(): Boolean {
        val accountProfile = accountInfo()
        val userName = sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.account)
            .getStringValue(MapSharePreference.SharePreferenceKeyEnum.userName, "")
        return !userName.isNullOrEmpty() || accountProfile != null
    }


}