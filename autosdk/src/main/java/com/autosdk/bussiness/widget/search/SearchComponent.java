package com.autosdk.bussiness.widget.search;

import android.location.Location;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.autonavi.gbl.aosclient.model.GNavigationEtaqueryAckRouteList;
import com.autonavi.gbl.aosclient.model.GNavigationEtaqueryReqStartEnd;
import com.autonavi.gbl.aosclient.model.GNavigationEtaqueryReqStartPoints;
import com.autonavi.gbl.aosclient.model.GNavigationEtaqueryRequestParam;
import com.autonavi.gbl.aosclient.model.GNavigationEtaqueryResponseParam;
import com.autonavi.gbl.aosclient.observer.ICallBackNavigationEtaquery;
import com.autonavi.gbl.common.model.Coord2DDouble;
import com.autonavi.gbl.layer.model.BizLayerUtil;
import com.autonavi.gbl.search.model.SearchPoiBasicInfo;
import com.autosdk.bussiness.adapter.bean.AdapterCarEnergyInfo;
import com.autosdk.bussiness.aos.AosController;
import com.autosdk.bussiness.common.AlongWaySearchPoi;
import com.autosdk.bussiness.common.POI;
import com.autosdk.bussiness.common.utils.ElectricInfoConverter;
import com.autosdk.bussiness.layer.LayerController;
import com.autosdk.bussiness.layer.SearchLayer;
import com.autosdk.bussiness.manager.SDKManager;
import com.autosdk.bussiness.map.SurfaceViewID;
import com.autosdk.bussiness.search.utils.SearchDataConvertUtils;
import com.autosdk.bussiness.widget.BusinessApplicationUtils;
import com.autosdk.bussiness.widget.setting.SettingComponent;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by AutoSdk on 2021/7/12.
 **/
public class SearchComponent {

    public static SearchComponent getInstance() {
        return SearchComponentHolder.mInstance;
    }

    private SearchComponent() {

    }

    private static class SearchComponentHolder {
        private static SearchComponent mInstance = new SearchComponent();
    }

    /**
     * 对于距离做判断
     * 有距离下发，直接使用
     * 无距离下发，使用本地点到点计算
     */
    public String getPoiBasicInfoDistance(@NonNull SearchPoiBasicInfo basicInfo){
        if (TextUtils.isEmpty(basicInfo.distance)){
            Location location = SDKManager.getInstance().getLocController().getLastLocation();
            Coord2DDouble startPoint = new Coord2DDouble(location.getLongitude(), location.getLatitude());
            double dis = BizLayerUtil.calcDistanceBetweenPoints(startPoint, basicInfo.location);
            return String.valueOf(dis);
        }
        return basicInfo.distance;
    }

    public void showChildesPoi(POI poi){
        SearchLayer searchLayer = LayerController.getInstance().getSearchLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN);
        if(searchLayer!=null){
            // 更新子点
            searchLayer.updateSearchChildPoi(SearchDataConvertUtils.convertPoi2BizSearchChildPoints(poi));
        }
    }

    ICallBackNavigationEtaquery reqVehicleChargeCallback;

    ICallBackNavigationEtaquery reqVehicleChargeForSearchResultCallback;

    ICallBackNavigationEtaquery reqDisAndChargeCallback;

    @Deprecated
    public void reqVehicleCharge(POI poi) {
        if (poi == null) {
            return;
        }
        long oldCurTime = System.currentTimeMillis();
        GNavigationEtaqueryRequestParam etaQueryRequestParam = getEtaQueryRequestParam(poi);
        if (etaQueryRequestParam == null) {
            Timber.e(new NullPointerException(),"GNavigationEtaqueryRequestParam is null , poi.name = %s , poi.id = %s", poi.getName(), poi.getId());
            return;
        }
        etaQueryRequestParam.mTimeOut = 3000;
        if(reqVehicleChargeCallback == null) {
            reqVehicleChargeCallback = new EtaQueryCallback() {
                @Override
                public void onRecvAck(GNavigationEtaqueryResponseParam gNavigationEtaqueryResponseParam) {
                    String vehicleCharge = "";
                    ArrayList<GNavigationEtaqueryAckRouteList> routeList = gNavigationEtaqueryResponseParam.route_list;
                    poi.setChargeLeftPercentage(-1);
                    if (routeList != null && routeList.size() > 0 && routeList.get(0).path != null && routeList.get(0).path.size() > 0) {
                        if (routeList.get(0).path.get(0).charge_left != -1 && poi.getMaxEnergy() != 0) {
                            double e1 = Math.floor(routeList.get(0).path.get(0).charge_left / 100000f);
                            int d = (int) Math.floor(e1 / poi.getMaxEnergy() * 100);
                            vehicleCharge = "到达后电量" + d + "%";
                            poi.setChargeLeftPercentage(d);
                        } else {
                            if (routeList.get(0).path.get(0).distance > 500 * 1000) {
                                vehicleCharge = "";
                            } else {
                                if (etaQueryRequestParam.vehicle.elec.charge > 0) {
                                    vehicleCharge = "当前电量不可达";
                                } else {
                                    vehicleCharge = "";
                                }
                            }
                        }
                    } else {
                        vehicleCharge = "";
                    }
                    long delay = System.currentTimeMillis() - oldCurTime;
                    Timber.d("SearchComponent延迟==" + delay);
                    if (gNavigationEtaqueryResponseParam.mNetworkStatus != 4) {
                        vehicleCharge = "未知";
                    }
                    int configKeyPowerType = SettingComponent.getInstance().getConfigKeyPowerType();
                    if (configKeyPowerType == 0 || configKeyPowerType == 3 || configKeyPowerType == -1) {
                        vehicleCharge = "";
                    }
                    if (poi instanceof AlongWaySearchPoi) {
                        ((AlongWaySearchPoi) poi).setVehiclechargeleft(vehicleCharge);
                    } else {
                        poi.setChargeLeft(vehicleCharge);
                    }
                }
            };
        }
        ((EtaQueryCallback)reqVehicleChargeCallback).updateFiled(poi,etaQueryRequestParam,oldCurTime);
        //暂时注释 AosController.getInstance().sendReqNavigationEtaquery(etaQueryRequestParam, reqVehicleChargeCallback);
    }

    public GNavigationEtaqueryRequestParam getEtaQueryRequestParam(List<POI> pois) {
        AdapterCarEnergyInfo carEnergyInfo = BusinessApplicationUtils.getElectricInfo().carEnergyInfo;
        if (carEnergyInfo != null) {
            for (POI poi : pois) {
                poi.setMaxEnergy(carEnergyInfo.maxBattEnergy);
            }
        }

        Location location = SDKManager.getInstance().getLocController().getLastLocation();
        Coord2DDouble startPoint = new Coord2DDouble(location.getLongitude(), location.getLatitude());

        GNavigationEtaqueryReqStartEnd gNavigationEtaqueryReqStart = new GNavigationEtaqueryReqStartEnd();
        gNavigationEtaqueryReqStart.points = new ArrayList<>();
        gNavigationEtaqueryReqStart.points.add(new GNavigationEtaqueryReqStartPoints(13,2,startPoint.lon,startPoint.lat));

        GNavigationEtaqueryReqStartEnd gNavigationEtaqueryReqEnd = new GNavigationEtaqueryReqStartEnd();
        gNavigationEtaqueryReqEnd.points = new ArrayList<>();
        for (int i = 0;i<pois.size();i++) {
            POI poi = pois.get(i);
            gNavigationEtaqueryReqEnd.points.add(new GNavigationEtaqueryReqStartPoints(i+100, 2,poi.getPoint().getLongitude(),poi.getPoint().getLatitude()));
        }

        GNavigationEtaqueryRequestParam param = ElectricInfoConverter.getNavigationEtaQueryRequestParam(gNavigationEtaqueryReqStart, gNavigationEtaqueryReqEnd, null);
        return param;
    }

    public GNavigationEtaqueryRequestParam getEtaQueryRequestParam(POI poi) {
        AdapterCarEnergyInfo carEnergyInfo = BusinessApplicationUtils.getElectricInfo().carEnergyInfo;
        if (carEnergyInfo != null) {
            poi.setMaxEnergy(carEnergyInfo.maxBattEnergy);
        }

        Location location = SDKManager.getInstance().getLocController().getLastLocation();
        Coord2DDouble startPoint = new Coord2DDouble(location.getLongitude(), location.getLatitude());

        GNavigationEtaqueryReqStartEnd gNavigationEtaqueryReqStart = new GNavigationEtaqueryReqStartEnd();
        gNavigationEtaqueryReqStart.points = new ArrayList<>();
        gNavigationEtaqueryReqStart.points.add(new GNavigationEtaqueryReqStartPoints(13,2,startPoint.lon,startPoint.lat));

        GNavigationEtaqueryReqStartEnd gNavigationEtaqueryReqEnd = new GNavigationEtaqueryReqStartEnd();
        gNavigationEtaqueryReqEnd.points = new ArrayList<>();
        gNavigationEtaqueryReqEnd.points.add(new GNavigationEtaqueryReqStartPoints(143, 2,poi.getPoint().getLongitude(),poi.getPoint().getLatitude()));

        GNavigationEtaqueryRequestParam param = ElectricInfoConverter.getNavigationEtaQueryRequestParam(gNavigationEtaqueryReqStart, gNavigationEtaqueryReqEnd, null);
        return param;
    }

    public String getKeywordSearchFilter(String gasPreference) {
        String filter = "";
        if(!TextUtils.isEmpty(gasPreference)){
            StringBuilder stringBuilder = new StringBuilder("query_type=rqbxy;range=1000;category=011100;custom=brand_charge:");
            if (gasPreference.contains(";")) {
                String[] str = gasPreference.split("\\;");
                for (int i = 0; i < str.length; i++) {
                    stringBuilder.append(str[i]).append("|");
                }
                filter = stringBuilder.substring(0, stringBuilder.length() - 1);
            } else {
                stringBuilder.append(gasPreference);
                filter = stringBuilder.toString();
            }
        }
        return filter;
    }

    public String getFilter(String gasPreference) {
        String filter = "";
        if (TextUtils.isEmpty(gasPreference)) {
            return filter;
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (gasPreference.contains("国家电网")) {
            stringBuilder.append(1);
        }
        if (gasPreference.contains("特来电")) {
            if (!TextUtils.isEmpty(stringBuilder.toString())) {
                stringBuilder.append("|");
            }
            stringBuilder.append(21);
        }
        if (gasPreference.contains("星星充电")) {
            if (!TextUtils.isEmpty(stringBuilder.toString())) {
                stringBuilder.append("|");
            }
            stringBuilder.append(22);
        }
        if (gasPreference.contains("普天新能源")) {
            if (!TextUtils.isEmpty(stringBuilder.toString())) {
                stringBuilder.append("|");
            }
            stringBuilder.append(5);
        }
        if (!stringBuilder.toString().isEmpty()) {
            filter = "brand_code:" + stringBuilder.toString();
        }
        return filter;
    }

    public String getGasCategory(String gasPreference) {
        String filter = "";
        if (TextUtils.isEmpty(gasPreference)) {
            return filter;
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (gasPreference.contains("中石化")) {
            stringBuilder.append("010101");
        }
        if (gasPreference.contains("中石油")) {
            if (!TextUtils.isEmpty(stringBuilder.toString())) {
                stringBuilder.append("|");
            }
            stringBuilder.append("010102");
        }
        if (gasPreference.contains("壳牌")) {
            if (!TextUtils.isEmpty(stringBuilder.toString())) {
                stringBuilder.append("|");
            }
            stringBuilder.append("010103");
        }
        if (gasPreference.contains("美孚")) {
            if (!TextUtils.isEmpty(stringBuilder.toString())) {
                stringBuilder.append("|");
            }
            stringBuilder.append("010104");
        }
        if (!stringBuilder.toString().isEmpty()) {
            filter = stringBuilder.toString();
        }
        return filter;
    }

//    int taskId = -1;
//    public void requestGrandChildPoi(String poiId){
//        if(taskId!=-1){
//            SearchController.getInstance().abort(taskId);
//        }
//        taskId = SearchController.getInstance().naviInfoSearch(new SearchNaviInfoParam(poiId, "305,105,106"), new SearchCallback<SearchNaviInfoResult>(this) {
//            @Override
//            public void onSuccess(SearchNaviInfoResult data) {
//                super.onSuccess(data);
//                List<POI> poiResult = new ArrayList<>();
//                for (SearchNaviInfoBase naviInfoBase : data.naviInfoItem.get(0).childrenItem) {
//                    poiResult.add(SearchDataConvertUtils.convertNaviInfoToPoi(naviInfoBase));
//                }
////                mMvpView.showNaviPoiInfo(poiResult);
//            }
//        });
//    }

    public void unInit() {
        if(reqVehicleChargeCallback != null) {
            reqVehicleChargeCallback = null;
        }
        if(reqVehicleChargeForSearchResultCallback != null) {
            reqVehicleChargeForSearchResultCallback = null;
        }
        if(reqDisAndChargeCallback != null) {
            reqDisAndChargeCallback = null;
        }
    }
}
