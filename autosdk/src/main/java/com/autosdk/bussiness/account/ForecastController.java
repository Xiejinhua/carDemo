package com.autosdk.bussiness.account;

import com.autonavi.gbl.servicemanager.ServiceMgr;
import com.autonavi.gbl.user.forcast.ForcastService;
import com.autonavi.gbl.user.forcast.model.ArrivedType;
import com.autonavi.gbl.user.forcast.model.ForcastArrivedData;
import com.autonavi.gbl.user.forcast.model.ForcastArrivedParam;
import com.autonavi.gbl.user.forcast.model.ForcastInitParam;
import com.autonavi.gbl.user.forcast.model.OftenArrivedItem;
import com.autonavi.gbl.user.forcast.observer.IForcastServiceObserver;
import com.autonavi.gbl.user.model.UserLoginInfo;
import com.autonavi.gbl.util.TimeUtil;
import com.autonavi.gbl.util.errorcode.common.Service;
import com.autonavi.gbl.util.model.SingleServiceID;
import com.autosdk.bussiness.common.utils.FileUtils;

import java.util.ArrayList;


public class ForecastController implements IForcastServiceObserver {
    private ForcastService mForecastService;
    private IForcastServiceObserver forecastServiceObserver;
    private IForcastServiceObserver onForcastArrivedData;

    private static class ForecastManagerHolder {
        private static ForecastController mInstance = new ForecastController();
    }

    public static ForecastController getInstance() {
        return ForecastManagerHolder.mInstance;
    }

    public ForcastService getForecastService() {
        return mForecastService;
    }

    public void addForecastObserver(IForcastServiceObserver observer) {
        forecastServiceObserver = observer;
    }

    public void unregisterForecastObserver() {
        forecastServiceObserver = null;
    }

    public boolean init(String path) {
        mForecastService = (ForcastService) ServiceMgr.getServiceMgrInstance().getBLService(SingleServiceID.ForcastSingleServiceID);
        if (mForecastService == null) {
            return false;
        }
        // 保证传入目录存在
        FileUtils.createDir(path);
        ForcastInitParam param = new ForcastInitParam();
        param.stCurTime = TimeUtil.getLocalTime2(); // 当前时间 com.autonavi.gbl.util.model.DateTime
        param.dbPath = path; // 预测数据库文件保存目录路径
        param.nMaxEnergyMileage = 50; // 能源消耗保存最大公里数单位(KM)
        param.nTopArrivedMaxCnt = 8; // 常去地点列表最大个数, 也决定了获取常去地点接口返回的最大数据量
        mForecastService.addObserver(this);
        //初始化参数待配置
        return mForecastService.init(param) == Service.ErrorCodeOK;
    }

    public void unInit() {
        if (mForecastService != null) {
            mForecastService.unInit();
        }
    }

    public ArrayList<OftenArrivedItem> getArrivedDataList(@ArrivedType.ArrivedType1 int type) {
        if (mForecastService != null) {
            ArrayList<OftenArrivedItem> ret = mForecastService.getArrivedDataList(type);
            return ret;
        } else {
            return null;
        }
    }

    public boolean getOnlineForecastArrivedData(ForcastArrivedParam param, IForcastServiceObserver observer) {
        if (mForecastService != null) {
            onForcastArrivedData = observer;
            mForecastService.getOnlineForcastArrivedData(param);
        }
        return false;
    }

    public boolean addLocalArrivedData(@ArrivedType.ArrivedType1 int type, OftenArrivedItem item) {
        if (mForecastService != null) {
            mForecastService.addLocalArrivedData(type, item);
        }
        return false;
    }

    public boolean setLogin(UserLoginInfo userInfo) {
        if (mForecastService != null) {
            mForecastService.setLoginInfo(userInfo);
        }
        return false;
    }

    public boolean delLocalArrivedData(@ArrivedType.ArrivedType1 int type, String name) {
        if (mForecastService != null) {
            mForecastService.delLocalArrivedData(type, name);
        }
        return false;
    }

    @Override
    public void onInit(int result) {
        if (forecastServiceObserver != null) {
            forecastServiceObserver.onInit(result);
        }
    }

    @Override
    public void onSetLoginInfo(int result) {
        if (forecastServiceObserver != null) {
            forecastServiceObserver.onSetLoginInfo(result);
        }

    }

    @Override
    public void onForcastArrivedData(ForcastArrivedData data) {
        if (onForcastArrivedData != null) {
            onForcastArrivedData.onForcastArrivedData(data);
        }
    }
}
