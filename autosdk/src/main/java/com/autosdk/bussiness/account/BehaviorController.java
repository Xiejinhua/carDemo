package com.autosdk.bussiness.account;

import android.text.TextUtils;

import com.autonavi.gbl.servicemanager.ServiceMgr;
import com.autonavi.gbl.user.behavior.BehaviorService;
import com.autonavi.gbl.user.behavior.model.BehaviorServiceParam;
import com.autonavi.gbl.user.behavior.model.ConfigKey;
import com.autonavi.gbl.user.behavior.model.ConfigValue;
import com.autonavi.gbl.user.behavior.model.FavoriteBaseItem;
import com.autonavi.gbl.user.behavior.model.FavoriteItem;
import com.autonavi.gbl.user.behavior.model.FavoriteType;
import com.autonavi.gbl.user.behavior.model.SimpleFavoriteItem;
import com.autonavi.gbl.user.behavior.model.VehicleInfo;
import com.autonavi.gbl.user.behavior.observer.IBehaviorServiceObserver;
import com.autonavi.gbl.user.model.UserLoginInfo;
import com.autonavi.gbl.user.syncsdk.model.SyncEventType;
import com.autonavi.gbl.user.syncsdk.model.SyncMode;
import com.autonavi.gbl.user.syncsdk.model.SyncRet;
import com.autonavi.gbl.util.errorcode.common.Service;
import com.autonavi.gbl.util.model.ServiceInitStatus;
import com.autonavi.gbl.util.model.SingleServiceID;
import com.autosdk.bussiness.account.observer.BehaviorServiceObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import timber.log.Timber;

/**
 * 收藏夹M层
 *
 */
public class BehaviorController {
    private BehaviorService mBehaviorService;

    private static class BehaviorManagerHolder {
        private static BehaviorController mInstance = new BehaviorController();
    }

    private BehaviorController() {
    }

    public static BehaviorController getInstance() {
        return BehaviorManagerHolder.mInstance;
    }

    public BehaviorService getBehaviorService() {
        return mBehaviorService;
    }

    /**
     * 初始化收藏夹服务
     */
    public boolean init() {
        initBehaviorService();
        return isBehaviorSuccess();
    }

    private void initBehaviorService() {
        mBehaviorService = (BehaviorService) ServiceMgr.getServiceMgrInstance().getBLService(SingleServiceID.BehaviorSingleServiceID);
        BehaviorServiceParam param = new BehaviorServiceParam();
        //初始化参数待配置
        int behavior = mBehaviorService.init(param);
        mBehaviorService.addObserver(behaviorServiceObserver);
        Timber.i("initBehaviorService: init= " + behavior);
    }

    BehaviorServiceObserver behaviorServiceObserver = new BehaviorServiceObserver() {
        @Override
        public void notifyFavorite(FavoriteBaseItem baseItem, boolean isDelete) {
        }

        //+++++++++++===============行为数据服务观察者 ================

        /**
         * @return void
         * @brief 获取行为数据回调通知
         * @param[in] eventType        同步SDK回调事件类型
         * @param[in] exCode           同步SDK返回值
         */
        @Override
        public void notify(@SyncEventType.SyncEventType1 int eventType, @SyncRet.SyncRet1 int exCode) {
            for (BehaviorServiceObserver observer : behaviorServiceObservers) {
                observer.notify(eventType, exCode);
            }
        }

        /**
         * 异步获取收藏点回调
         *
         * @param type   收藏点类型
         * @param data   收藏点列表
         * @param sorted 是否排序
         */
        @Override
        public void notify(@FavoriteType.FavoriteType1 int type, ArrayList<SimpleFavoriteItem> data, boolean sorted) {
            for (BehaviorServiceObserver observer : behaviorServiceObservers) {
                observer.notify(type, data, sorted);
            }
        }
    };

    public boolean isBehaviorSuccess() {
        return mBehaviorService != null
                && mBehaviorService.isInit() == ServiceInitStatus.ServiceInitDone;
    }

    /**
     * 异步获取收藏点列表
     */
    public void getFavoriteListAsync(@FavoriteType.FavoriteType1 int type, boolean sorted) {
        mBehaviorService.getFavoriteListAsync(type, sorted);
    }

    public void uninit() {
        //观察者销毁
        behaviorServiceObserver = null;
        destroyBeahvior();
    }

    private void destroyBeahvior() {
        if (mBehaviorService != null) {
            mBehaviorService = null;
        }
    }

    // ============================ 观察者 ================================

    private static List<BehaviorServiceObserver> behaviorServiceObservers = new CopyOnWriteArrayList<>();
    public void registerBehaviorServiceObserver(BehaviorServiceObserver behaviorServiceObserver) {
        behaviorServiceObservers.add(behaviorServiceObserver);
    }

    public void unregisterBehaviorServiceObserver(BehaviorServiceObserver behaviorServiceObserver) {
        behaviorServiceObservers.remove(behaviorServiceObserver);
    }

    public IBehaviorServiceObserver getBehaviorServiceObserver() {
        return behaviorServiceObserver;
    }


    // ============================ BehaviorService 服务方法 ================================

    /**
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * @brief 设置用户登录信息
     * @param[in] param:           用户登录信息,为空时为游客模式
     * @note 登录账号后调此接口设置用户模式，非登录状态调此接口设置游客模式\n
     * 在调用获取收藏点接口或历史记录前需先调用此接口。
     * @note thread:multi
     */
    public int setLoginInfo(UserLoginInfo param) {
        if (mBehaviorService != null) {
            return mBehaviorService.setLoginInfo(param);
        }
        return -2;
    }

    /**
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * @brief 设置配置信息
     * @param[in] key              配置项Key
     * @param[in] value            配置项内容
     * @param[in] mode             同步方式
     * @note 设置的配置信息在同步后可以在手机、车机多端同步。
     * @note thread:multi
     */
    public int setConfig(@ConfigKey.ConfigKey1 int key, ConfigValue value, @SyncMode.SyncMode1 int mode) {
        if (mBehaviorService != null) {
            return mBehaviorService.setConfig(key, value, mode);
        }
        return Service.AUTO_UNKNOWN_ERROR;
    }

    /**
     * @return 配置项内容
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * @brief 获取配置信息
     * @param[in] key              配置项Key
     * @note thread:multi
     */
    public ConfigValue getConfig(int key) {
        if (mBehaviorService != null) {
            return mBehaviorService.getConfig(key);
        }
        return new ConfigValue();
    }

    /**
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * @brief 添加收藏点
     * @param[in] item             收藏点信息
     * @param[in] mode             同步方式
     * @note thread:multi
     */
    public int addFavorite(FavoriteItem item, int mode) {
        int result = -2;
        if (mBehaviorService != null) {
            String resultAdd = mBehaviorService.addFavorite(item, mode);
            for (BehaviorServiceObserver observer : behaviorServiceObservers) {
                if (observer != null && !TextUtils.isEmpty(resultAdd)) {
                    FavoriteBaseItem favoriteBaseItem = new FavoriteBaseItem();
                    favoriteBaseItem.item_id = resultAdd;
                    observer.notifyFavorite(favoriteBaseItem, false);
                }
            }
//            if (!TextUtils.isEmpty(resultAdd)) {
//                Timber.i("addFavorite !TextUtils.isEmpty(resultAdd)");
//                if (item.common_name == FavoriteType.FavoriteTypeHome || item.common_name == FavoriteType.FavoriteTypeCompany){
//                    syncFrequentData();
//                }
//            }
            return TextUtils.isEmpty(resultAdd) ? Service.AUTO_UNKNOWN_ERROR : Service.ErrorCodeOK;
        }
        return result;
    }

    /**
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * @brief 删除所有收藏点
     * @param[in] mode             同步方式
     * @note thread:multi
     */
    public int clearFavorite(@SyncMode.SyncMode1 int mode) {
        if (mBehaviorService != null) {
            return mBehaviorService.clearFavorite(mode);
        }
        return -2;
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
    public ArrayList<SimpleFavoriteItem> getSimpleFavoriteList(int type, boolean sorted) {
        if (mBehaviorService != null) {
            return mBehaviorService.getSimpleFavoriteList(type, sorted);
        }
        return null;
    }

    public int[] getSimpleFavoriteIds() {
        if (mBehaviorService != null) {
            return mBehaviorService.getSimpleFavoriteIds();
        }
        return null;
    }

    /**
     * @return 收藏点信息
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * @brief 通过基础信息获取收藏点
     * @param[in] base             基础信息
     * @note thread:multi
     */
    public FavoriteItem getFavorite(FavoriteBaseItem base) {
        if (mBehaviorService != null) {
            return mBehaviorService.getFavorite(base);
        }
        return null;
    }

    /**
     * @return 精简信息收藏点
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * @brief 通过id获取精简信息收藏点
     * @param[in] id               收藏点id
     * @note thread:multi
     */
    public SimpleFavoriteItem getSimpleFavoriteById(int id) {
        if (mBehaviorService != null) {
            return mBehaviorService.getSimpleFavoriteById(id);
        }
        return null;
    }

    /**
     * @return Poi唯一码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * @brief 获取ItemId(Poi唯一码)
     * @param[in] x                经度
     * @param[in] y                纬度
     * @param[in] name             名称
     * @note thread:multi
     */
    public String getItemId(int x, int y, String name) {
        if (mBehaviorService != null) {
            return mBehaviorService.getItemId(x, y, name);
        }
        return null;
    }

    /**
     * @return 车辆信息
     * - ErrorCodeOK  成功
     * - 其他 （参考ErrorCode定义）
     * @brief 取得车辆具体信息
     * @note thread:mutil
     */
    public VehicleInfo getCar(String plateNum) {
        if (mBehaviorService != null) {
            return mBehaviorService.getCar(plateNum);
        }
        return null;
    }

    /**
     * @return const dice::String16  版本号
     * @brief 获取版本号
     * @note thread:multi
     */
    public String getVersion() {
        if (mBehaviorService != null) {
            return mBehaviorService.getVersion();
        }
        return null;
    }

    /**
     * @return void
     * @brief 日志开关
     * @param[in] nLevel           日志层级
     * @note thread:multi
     */
    public void logSwitch(int nLevel) {
        if (mBehaviorService != null) {
            mBehaviorService.logSwitch(nLevel);
        }
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
    public int delFavorite(FavoriteBaseItem base, @SyncMode.SyncMode1 int mode) {
        int result = -2;
        if (mBehaviorService != null) {
            FavoriteItem favoriteItem = getFavorite(base);
            if (favoriteItem != null) {
                base.item_id = favoriteItem.item_id;
            }
            String resultDel = mBehaviorService.delFavorite(base, mode);
            for (BehaviorServiceObserver observer : behaviorServiceObservers) {
                observer.notifyFavorite(base, true);
            }
//            if (!TextUtils.isEmpty(resultDel) && favoriteItem != null && (favoriteItem.common_name == FavoriteType.FavoriteTypeHome || favoriteItem.common_name == FavoriteType.FavoriteTypeCompany)){
//                syncFrequentData();
//            }
            return TextUtils.isEmpty(resultDel) ? Service.AUTO_UNKNOWN_ERROR : Service.ErrorCodeOK;
        }
        return result;
    }

    /**
     * 移除家 ， 或者公司 ， 使得家或公司始终为1个
     *
     * @param removeitems
     */
    public void removeFavorites(ArrayList<SimpleFavoriteItem> removeitems) {
        if (removeitems != null && !removeitems.isEmpty()) {
            SimpleFavoriteItem simpleFavoriteItem = removeitems.get(0);
            FavoriteBaseItem favoriteBaseItem = new FavoriteBaseItem();
            favoriteBaseItem.item_id = simpleFavoriteItem.item_id;
            FavoriteItem favoriteItem = getFavorite(favoriteBaseItem);
            if (favoriteItem != null) {
                favoriteBaseItem.poiid = favoriteItem.poiid;
                int result = delFavorite(favoriteBaseItem, SyncMode.SyncModeNow);
                if (result != Service.ErrorCodeOK) {
                    Timber.d("delFavorite failed , item_id = %s , result = %s", favoriteItem.item_id, result);
                }
            }
        }
    }

    /**
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * @brief 更新收藏点
     * @param[in] item             收藏点信息
     * @param[in] mode             同步方式
     * @note thread:multi
     */
    public int updateFavorite(FavoriteItem item, @SyncMode.SyncMode1 int mode) {
        if (mBehaviorService != null) {
            String resuleUpdate = mBehaviorService.updateFavorite(item, mode);
//            if (!TextUtils.isEmpty(resuleUpdate) && (item.common_name == FavoriteType.FavoriteTypeHome || item.common_name == FavoriteType.FavoriteTypeCompany)){
//                syncFrequentData();
//            }
            return TextUtils.isEmpty(resuleUpdate) ? Service.AUTO_UNKNOWN_ERROR : Service.ErrorCodeOK;
        }
        return -2;
    }

    /**
     * 同步常去点信息(650之后需要使用此接口同步家/公司)
     * @return
     */
    public int syncFrequentData() {
        if (mBehaviorService != null) {
            ConfigValue configValue = new ConfigValue();
            configValue.intValue = 1;
            BehaviorController.getInstance().setConfig(ConfigKey.ConfigKeyOftenArrived,configValue, SyncMode.SyncModeNow);
            int ret = mBehaviorService.syncFrequentData();
            Timber.i("syncFrequentData ret:%s", ret);
            return  ret;
        }
        return -1;
    }

    /**
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * @brief 置顶收藏点
     * @param[in] base             基础信息
     * @param[in] top              是否置顶
     * @param[in] mode             同步方式
     * @note thread:multi
     */
    public int topFavorite(FavoriteBaseItem base, boolean top, @SyncMode.SyncMode1 int mode) {
        if (mBehaviorService != null) {
            String topResult = mBehaviorService.topFavorite(base, top, mode);
            return TextUtils.isEmpty(topResult) ? Service.AUTO_UNKNOWN_ERROR : Service.ErrorCodeOK;
        }
        return -2;
    }

    /**
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * @brief 通过基础信息检查是否已收藏
     * @param[in] base             基础信息
     * @note thread:multi
     */
    public int isFavorited(FavoriteBaseItem base) {
        if (mBehaviorService != null) {
            String isFavoriteResult = mBehaviorService.isFavorited(base);
            return TextUtils.isEmpty(isFavoriteResult) ? Service.AUTO_UNKNOWN_ERROR : Service.ErrorCodeOK;
        }
        return -2;
    }


    /**
     * 获取所有收藏记录集合
     *
     * @return 收藏记录
     */
    public ArrayList<SimpleFavoriteItem> getAllFavoriteItem() {
        ArrayList<SimpleFavoriteItem> arrayList = new ArrayList<>();
        ArrayList<SimpleFavoriteItem> poi = getSimpleFavoriteList(FavoriteType.FavoriteTypePoi, false);
        ArrayList<SimpleFavoriteItem> home = getSimpleFavoriteList(FavoriteType.FavoriteTypeHome, false);
        ArrayList<SimpleFavoriteItem> company = getSimpleFavoriteList(FavoriteType.FavoriteTypeCompany, false);
        if (null != poi) {
            arrayList.addAll(poi);
        }
        if (null != home) {
            arrayList.addAll(home);
        }
        if (null != company) {
            arrayList.addAll(company);
        }
        return arrayList;
    }
}