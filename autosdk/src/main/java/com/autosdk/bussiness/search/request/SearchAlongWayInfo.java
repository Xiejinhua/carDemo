package com.autosdk.bussiness.search.request;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.autonavi.gbl.common.model.RectDouble;
import com.autonavi.gbl.common.path.model.RestAreaInfo;
import com.autonavi.gbl.search.model.SearchRoadId;
import com.autosdk.bussiness.common.GeoPoint;
import com.autosdk.bussiness.common.POI;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 沿途搜请求参数
 *
 * <pre>
 *     new SearchAlongWayInfo.Builder()
 *            .setKeyword("充电站") // 必传, 关键字
 *            .setGeolinePointList(geolinePointList) // 必传, 路线抽稀点列表,非空,否则搜索不到信息
 *            .setFilterCondition("") // 可选, 过滤条件
 *            .setRoutePoints(startPoint,endPoint,viaPointList) // 可选, 算路起点/起点终点非空,途经点列表可空
 *            .build(),
 * </pre>
 */
public class SearchAlongWayInfo implements Serializable {
    /**
     * 沿途搜关键字,必填
     */
    private String keyword;

    /**
     * 路线上坐标点(至少两个)使用;分隔，如: 经度;纬度;经度;纬度，在线必传参数 , 抽稀点信息
     */
    @Nullable
    private List<GeoPoint> geolinePointList;

    /**
     * 算路起点位置,通常为车标位置
     */
    private GeoPoint startPoint;

    /**
     * 算路终点位置信息,通常为地图中心点
     */
    private GeoPoint endPoint;

    /**
     * 算路途经点列表
     */
    @Nullable
    private List<GeoPoint> viaPointList;

    /**
     * 离线指定引导路径道路 ，离线搜索条件，离线搜索必填
     */
    private ArrayList<SearchRoadId> guideRoads;

    /**
     * 在线/离线搜索过滤条件 ,其中沿途离线搜索必传参数: specialSearch/offlineCustom/adcode/guideRoads
     * 离线条件, 如: "charge&&charge_cscf:1" , charge 是当前城市所有充电桩的合集，而且cscf等于1（公用属性）
     */
    private String filterCondition;

    /**
     * 是否正在导航中
     * 用于设置请求沿途搜接口的场景,true-行中，false-行前,默认为行中
     */
    private boolean isNaving = true;

    /**
     * 导航类型,1-骑行,4-步行,2-驾车，5-货车，9-摩托车 默认2
     */
    private int naviType = 2;

    private int searchType = 0;

    private String linkid;

    /**
     * 是否需要加油站价格信息
     */
    private boolean isNeedGasprice;

    private String category;

    /**
     * 路线矩形区域
     */
    private RectDouble routeRect;

    /**
     * 沿途服务区信息
     */
    private ArrayList<RestAreaInfo> restAreaInfoList;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getSearchType() {
        return searchType;
    }

    public void setSearchType(int searchType) {
        this.searchType = searchType;
    }

    public RectDouble getRouteRect() {
        return routeRect;
    }

    public void setRouteRect(RectDouble routeRect) {
        this.routeRect = routeRect;
    }

    public ArrayList<RestAreaInfo> getRestAreaInfoList() {
        return restAreaInfoList;
    }

    public void setRestAreaInfoList(ArrayList<RestAreaInfo> restAreaInfoList) {
        this.restAreaInfoList = restAreaInfoList;
    }

    private SearchAlongWayInfo() {
    }

    public static class Builder {
        private final SearchAlongWayInfo alongWayInfo;

        public Builder() {
            alongWayInfo = new SearchAlongWayInfo();
        }

        /**
         * 沿途搜关键字,必填
         */
        public Builder setKeyword(@NonNull String keyword) {
            alongWayInfo.keyword = keyword;
            return this;
        }

        /**
         * 路线上坐标点(至少两个)使用;分隔，如: 经度;纬度;经度;纬度，在线必传参数 , 抽稀点信息
         */
        public Builder setGeolinePointList(@NonNull List<GeoPoint> list) {
            alongWayInfo.geolinePointList = list;
            return this;
        }

        /**
         * 设置算路起点/终点/途经点信息,，在线必填字段， 其中途经点列表可空
         */
        public Builder setRoutePoints(@NonNull GeoPoint startPoint, @NonNull GeoPoint endPoint, @Nullable List<GeoPoint> viaPointList) {
            alongWayInfo.startPoint = startPoint;
            alongWayInfo.endPoint = endPoint;
            alongWayInfo.viaPointList = viaPointList;
            return this;
        }


        /**
         * 设置算路起点/终点/途经点信息,，在线必填字段， 其中途经点列表可空
         */
        public Builder setRoutePointsByPoi(@NonNull POI startPoi, @NonNull POI endPoi, @Nullable List<POI> viaPoiList) {
            alongWayInfo.startPoint = startPoi.getPoint();
            alongWayInfo.endPoint = endPoi.getPoint();

            int size = viaPoiList == null ? 0 : viaPoiList.size();
            if (size == 0) {
                alongWayInfo.viaPointList = null;
                return this;
            }

            ArrayList<GeoPoint> viaList = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                POI poi = viaPoiList.get(i);
                if (poi == null) {
                    continue;
                }
                viaList.add(poi.getPoint());
            }
            alongWayInfo.viaPointList = viaList;
            return this;
        }

        /**
         * 离线指定引导路径道路 ，离线搜索条件，离线搜索必填
         */
        public Builder setGuideRoadsIdList(@Nullable ArrayList<SearchRoadId> guideRoads) {
            alongWayInfo.guideRoads = guideRoads;
            return this;
        }

        /**
         * 在线/离线搜索过滤条件 ,其中沿途离线搜索必传参数: specialSearch/offlineCustom/adcode/guideRoads
         * 离线条件, 如: "charge&&charge_cscf:1" , charge 是当前城市所有充电桩的合集，而且cscf等于1（公用属性）
         */
        public Builder setFilterCondition(String filterCondition) {
            alongWayInfo.filterCondition = filterCondition;
            return this;
        }

        /**
         * 是否处于行驶中,默认为true
         */
        public Builder setNaving(boolean isNaving) {
            alongWayInfo.isNaving = isNaving;
            return this;
        }

        /**
         * 导航类型,1-骑行,4-步行,2-驾车，5-货车，9-摩托车 默认2
         */
        public Builder setNaviType(int naviType) {
            alongWayInfo.naviType = naviType;
            return this;
        }

        /**
         * 设置linkId
         */
        public Builder setLinkId(String linkId) {
            alongWayInfo.linkid = linkId;
            return this;
        }

        /**
         * 是否需要加油站详细信息
         * @param isNeedGasprice
         * @return
         */
        public Builder setIsNeedGasprice(boolean isNeedGasprice) {
            alongWayInfo.isNeedGasprice = isNeedGasprice;
            return this;
        }

        public Builder setRouteRect(RectDouble routeRect) {
            alongWayInfo.routeRect = routeRect;
            return this;
        }

        public Builder setRestAreaInfoList(ArrayList<RestAreaInfo> restAreaInfoList) {
            alongWayInfo.restAreaInfoList = restAreaInfoList;
            return this;
        }

        public SearchAlongWayInfo build() {
            return alongWayInfo;
        }
    }

    public String getKeyword() {
        return keyword;
    }

    @Nullable
    public List<GeoPoint> getGeolinePointList() {
        return geolinePointList;
    }

//    public List<GeoPoint> getRoutePointList() {
//        return routePointList;
//    }

    public ArrayList<SearchRoadId> getGuideRoads() {
        return guideRoads;
    }

    public String getFilterCondition() {
        return filterCondition;
    }

    public boolean isNaving() {
        return isNaving;
    }

    public int getNaviType() {
        return naviType;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setGeolinePointList(@Nullable List<GeoPoint> geolinePointList) {
        this.geolinePointList = geolinePointList;
    }

    public void setGuideRoads(ArrayList<SearchRoadId> guideRoads) {
        this.guideRoads = guideRoads;
    }

    public void setFilterCondition(String filterCondition) {
        this.filterCondition = filterCondition;
    }

    public void setNaving(boolean naving) {
        isNaving = naving;
    }

    public void setNaviType(int naviType) {
        this.naviType = naviType;
    }

    public String getLinkid() {
        return linkid;
    }

    public GeoPoint getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(GeoPoint startPoint) {
        this.startPoint = startPoint;
    }

    public GeoPoint getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(GeoPoint endPoint) {
        this.endPoint = endPoint;
    }

    @Nullable
    public List<GeoPoint> getViaPointList() {
        return viaPointList;
    }

    public void setViaPointList(@Nullable List<GeoPoint> viaPointList) {
        this.viaPointList = viaPointList;
    }

    public boolean getIsNeedGasprice() {
        return this.isNeedGasprice;
    }
}
