package com.autosdk.bussiness.common;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.autonavi.gbl.search.model.ChargingStationInfo;
import com.autonavi.gbl.search.model.SearchDriveInfo;
import com.autonavi.gbl.search.model.SearchLabelInfo;
import com.autonavi.gbl.search.model.SearchPicGallery;
import com.autonavi.gbl.search.model.SearchPoiGasInfo;
import com.autonavi.gbl.search.model.SearchPoiGasStationInfo;
import com.autonavi.gbl.search.model.SearchPoiNearbyInfo;
import com.autonavi.gbl.search.model.SearchPoiParkingInfo;
import com.autonavi.gbl.search.model.SearchPoiPhoto;
import com.autonavi.gbl.search.model.SearchPoiRankInfo;
import com.autonavi.gbl.search.model.SearchProductInfo;
import com.autonavi.gbl.search.model.SearchRankInfoBase;
import com.autosdk.bussiness.search.bean.PoiDetailProductBean;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 功能说明:
 * <p>一、背景说明:</p>
 * 为了解决多模块业务共用POI,且需要进行intent数据传递,定义了POI类，实现序列化，且支持扩展。
 * <p>二、使用方式:</p>
 * <strong>1.如何创建对象？</strong><br/>
 * 当你需要一个POI对象时，可通过POIFactory来创建。<br/>
 * <strong>POI poi = POIFactory.createPOI("poiname", new GeoPoint());</strong>
 * <p/>
 */
public class POI implements Serializable, Parcelable {

    private String id = "";
    // 父POI的Id
    private String pid = "";
    // POI起终点位置类型,默认值为0 当前GPS位置, 1 手动选择，2 POI方式
    private int type = 0;
    private String industry = "";
    // 名称
    private String name = "";
    // 电话
    private String phone = "";
    // 城市名称
    private String cityName = "";
    // 城市区号
    private String cityCode = "";
    // 地址
    private String addr = "";
    private String category = "";
    // 城市代码
    private String adCode = "";
    // 与我的位置之间的距离
    private String distance = ""; //数字距离，不包含单位
    private String parent = "0";
    //终点的父POI与子POI的关系类型
    private int childType;
    //终点的楼层
    private String floorNo;
    //String类型的距离，由搜索模板返回
    private String dis; //距离，包含单位
    //提供搜索历史使用
    private int historyType;
    //区县名称
    private String district;
    //区县行政区编号
    private int districtadcode;
    // POI搜索类型，例如加油站，停车场等
    private String typeCode = "";
    //poi类型
    private String poiTag = "";
    private String shortname = "";
    //剩余电量
    private String chargeLeft;
    //新能源到达距离和时间
    private String etaDis;
    //是否沿途搜转周边搜
    private boolean isAlongWaySearch;
    /* 电池最大负载电量 */
    private double maxEnergy;
    private int chargeLeftPercentage;
    /* 营业时间 */
    private String deepinfo;
    /* 选择率 */
    private double ratio;
    /* 营业状 态 */
    private String openStatus;
    /*poi个性化标签，适用于美食、洗车*/
    private int hisMark;
    /*poi个性化标签，适用于景区;*/
    private String scenicMark;
    /* 停车场信息 */
    private SearchPoiParkingInfo parkingInfo;
    /**
     * 人均价格
     */
    private int averageCost;
    /**
     * 常规星级评分
     */
    private String rating;

    /**
     * 评价人数
     */
    private int reviewTotal;
    /**
     * poi图片信息
     */
    private String imageUrl;
    /**
     * 货架信息
     */
    private List<SearchProductInfo> searchProductInfoList;

    private SearchPoiRankInfo rankInfo;

    private SearchPoiPhoto photoInfo;
    /*特色标签*/
    public ArrayList<String> featuredLabel;

    private SearchDriveInfo mSearchDriveInfo;

    private SearchPoiGasStationInfo mSearchPoiGasStationInfo;

    private ArrayList<SearchLabelInfo> mSearchLabelInfo;
    /**
     * 是否最快达到
     */
    private boolean isFastest;

    private boolean isClosest;
    /**
     * 本月导航过的数量
     */
    public long naviMonthUv;

    private String alongSearchDistance = "";

    private String alongSearchTravelTime = "";

    //路线还原信息
    private String mPathRestorationInfo = "";
    //子行业类型, 如西式快餐
    private String subIndustry = "";
    //poi热度信息，例如：1小时前有人导航
    private String hotInfo = "";
    //POI附近信息
    private SearchPoiNearbyInfo nearbyInfo;
    /**
     * 是否是家或公司
     */
    private boolean isHomeCompany;
    //收藏点itemid
    private String favItemId;

    /* poi类型  2：途经点  3：终点*/
    int poiType;

    String arriveTimes = "";

    String remindBatteryValue = "";

    int childIndex = -1;//子POI index

    public String getArriveTimes() {
        return arriveTimes;
    }

    public void setArriveTimes(String arriveTimes) {
        this.arriveTimes = arriveTimes;
    }

    public String getRemindBatteryValue() {
        return remindBatteryValue;
    }

    public void setRemindBatteryValue(String remindBatteryValue) {
        this.remindBatteryValue = remindBatteryValue;
    }

    public int getChildIndex() {
        return childIndex;
    }

    public void setChildIndex(int childIndex) {
        this.childIndex = childIndex;
    }

    public int getPoiType() {
        return poiType;
    }

    public void setPoiType(int poiType) {
        this.poiType = poiType;
    }

    public SearchPoiNearbyInfo getNearbyInfo() {
        return nearbyInfo;
    }

    public void setNearbyInfo(SearchPoiNearbyInfo nearbyInfo) {
        this.nearbyInfo = nearbyInfo;
    }

    public String getSubIndustry() {
        return subIndustry;
    }

    public void setSubIndustry(String subIndustry) {
        this.subIndustry = subIndustry;
    }

    public String getHotInfo() {
        return hotInfo;
    }

    public void setHotInfo(String hotInfo) {
        this.hotInfo = hotInfo;
    }

    /**
     * 顺路搜驾车信息专用
     */
    public void setSearchDriveInfo(SearchDriveInfo searchDriveInfo) {
        mSearchDriveInfo = searchDriveInfo;
    }

    /**
     * 顺路搜驾车信息专用
     */
    public SearchDriveInfo getSearchDriveInfo() {
        return mSearchDriveInfo;
    }

    /**
     * 顺路搜加油专用
     */
    public SearchPoiGasStationInfo getSearchPoiGasStationInfo() {
        return mSearchPoiGasStationInfo;
    }

    /**
     * 顺路搜加油专用
     */
    public void setSearchPoiGasStationInfo(SearchPoiGasStationInfo searchPoiGasStationInfo) {
        mSearchPoiGasStationInfo = searchPoiGasStationInfo;
    }

    /**
     * 顺路搜标签专用
     */
    public ArrayList<SearchLabelInfo> getSearchLabelInfos() {
        return mSearchLabelInfo;
    }

    /**
     * 顺路搜标签专用
     */
    public void setSearchLabelInfos(ArrayList<SearchLabelInfo> searchLabelInfos) {
        mSearchLabelInfo = searchLabelInfos;
    }

    public void setPathRestorationInfo(String pathRestorationInfo) {
        mPathRestorationInfo = pathRestorationInfo;
    }

    public String getPathRestorationInfo() {
        return mPathRestorationInfo;
    }

    public String getAlongSearchDistance() {
        return alongSearchDistance;
    }

    public void setAlongSearchDistance(String alongSearchDistance) {
        this.alongSearchDistance = alongSearchDistance;
    }

    public String getAlongSearchTravelTime() {
        return alongSearchTravelTime;
    }

    public void setAlongSearchTravelTime(String alongSearchTravelTime) {
        this.alongSearchTravelTime = alongSearchTravelTime;
    }

    public List<SearchProductInfo> getSearchProductInfoList() {
        return searchProductInfoList;
    }

    public void setSearchProductInfoList(List<SearchProductInfo> searchProductInfoList) {
        this.searchProductInfoList = searchProductInfoList;
    }

    public void setFeaturedLabel(ArrayList<String> label) {
        this.featuredLabel = label;
    }

    public ArrayList<String> getFeaturedLabel() {
        return this.featuredLabel;
    }

    /**
     * 设置榜单信息
     *
     * @param info
     */
    public void setPoiRankInfo(SearchPoiRankInfo info) {
        this.rankInfo = info;
    }

    /**
     * 获取榜单信息
     *
     * @return
     */
    public SearchPoiRankInfo getRankInfo() {
        return this.rankInfo;
    }

    /**
     * 设置POI的图片信息
     *
     * @param info
     */
    public void setPoiPhoto(SearchPoiPhoto info) {
        this.photoInfo = info;
    }

    public SearchPoiPhoto getPhotoInfo() {
        return this.photoInfo;
    }

    /**
     * 判断是否为最快达到
     *
     * @return
     */
    public boolean isFastestArrival() {
        return isFastest;
    }

    /**
     * 设置服务器返回的数据
     *
     * @param isFastest
     */
    public void setFastestArrivalState(boolean isFastest) {
        this.isFastest = isFastest;
    }

    public void setIsClosest(boolean visible) {
        isClosest = visible;
    }

    public boolean getIsClosest() {
        return isClosest;
    }

    public int getAverageCost() {
        return averageCost;
    }

    public void setAverageCost(int averageCost) {
        this.averageCost = averageCost;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public void setReviewTotal(int num) {
        reviewTotal = num;
    }

    public int getReviewTotal() {
        return reviewTotal;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String url) {
        this.imageUrl = url;
    }

    /**
     * 图片墙信息
     */
    private SearchPicGallery galleryInfo;

    public SearchPicGallery getGalleryInfo() {
        return galleryInfo;
    }

    public void setGalleryInfo(SearchPicGallery galleryInfo) {
        this.galleryInfo = galleryInfo;
    }

    /**
     * poi详情代金券列表
     */
    private List<PoiDetailProductBean> voucherList;

    public List<PoiDetailProductBean> getVoucherList() {
        return voucherList;
    }

    public void setVoucherList(List<PoiDetailProductBean> voucherList) {
        this.voucherList = voucherList;
    }

    /**
     * poi团购券列表
     */
    private List<PoiDetailProductBean> productList;

    public List<PoiDetailProductBean> getProductList() {
        return productList;
    }

    public void setProductList(List<PoiDetailProductBean> productList) {
        this.productList = productList;
    }

    /**
     * 高德榜单
     */
    private List<SearchRankInfoBase> rankBarInfoList;

    public List<SearchRankInfoBase> getRankBarInfoList() {
        return rankBarInfoList;
    }

    public void setRankBarInfo(List<SearchRankInfoBase> rankBarInfoList) {
        this.rankBarInfoList = rankBarInfoList;
    }

    /**
     * 美食类别
     */
    private List<String> foodCategory;

    public List<String> getFoodCategory() {
        return foodCategory;
    }

    public void setFoodCategory(List<String> foodCategory) {
        this.foodCategory = foodCategory;
    }

    public void setOpenStatus(String openStatus) {
        this.openStatus = openStatus;
    }

    public String getOpenStatus() {
        return openStatus;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    public double getRatio() {
        return ratio;
    }

    public void setDeepinfo(String deepinfo) {
        this.deepinfo = deepinfo;
    }

    public String getDeepinfo() {
        return deepinfo;
    }

    public double getMaxEnergy() {
        return maxEnergy;
    }

    public void setMaxEnergy(double maxEnergy) {
        this.maxEnergy = maxEnergy;
    }

    public boolean isAlongWaySearch() {
        return isAlongWaySearch;
    }

    public void setAlongWaySearch(boolean alongWaySearch) {
        isAlongWaySearch = alongWaySearch;
    }

    public String getPoiTag() {
        return poiTag;
    }

    public void setPoiTag(String poiTag) {
        this.poiTag = poiTag;
    }

    public int getHistoryType() {
        return historyType;
    }

    public void setHistoryType(int historyType) {
        this.historyType = historyType;
    }

    private GeoPoint point = new GeoPoint(); // 坐标
    private HashMap<String, ArrayList<GeoPoint>> poiExtra = new HashMap<String, ArrayList<GeoPoint>>();
    private transient HashMap<Class<?>, POI> typeMap = new HashMap<Class<?>, POI>();
    private static HashMap<Class<?>, Field[]> deepCopyMap = new HashMap<Class<?>, Field[]>();
    private ArrayList<POI> childPois = new ArrayList<>();

    private ArrayList<ArrayList<GeoPoint>> poiPolygonBounds;
    private ArrayList<ArrayList<GeoPoint>> poiRoadaoiBounds;
    private ArrayList<GeoPoint> parkInfos;
    private ArrayList<POI> extraChildSearchPOI = new ArrayList<>();
    private ArrayList<ArrayList<GeoPoint>> extPoiPolygonBounds;

    public ArrayList<GeoPoint> getParkInfos() {
        return parkInfos;
    }

    public SearchPoiGasInfo gasInfo;//油价信息

    public void setParkInfos(ArrayList<GeoPoint> parkInfos) {
        this.parkInfos = parkInfos;
    }

    public ArrayList<ArrayList<GeoPoint>> getPoiRoadaoiBounds() {
        return poiRoadaoiBounds;
    }

    public void setPoiRoadaoiBounds(ArrayList<ArrayList<GeoPoint>> poiRoadaoiBounds) {
        this.poiRoadaoiBounds = poiRoadaoiBounds;
    }

    private ChargingStationInfo chargeStationInfo;  //充电站信息

    public ChargingStationInfo getChargeStationInfo() {
        return chargeStationInfo;
    }

    public void setChargeStationInfo(ChargingStationInfo chargeStationInfo) {
        this.chargeStationInfo = chargeStationInfo;
    }

    public SearchPoiGasInfo getGasInfo() {
        return gasInfo;
    }

    public void setGasInfo(SearchPoiGasInfo gasInfo) {
        this.gasInfo = gasInfo;
    }

    /**
     * POI id（高德标准）
     */
    public String getId() {
        return id;
    }

    /**
     * 设置POI id
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * POI 类型（高德 标准 ）
     */
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    /**
     * 获取POI的商业类型
     *
     * @return
     */
    public String getIndustry() {
        return industry;
    }

    /**
     * 设置POI的商业类型
     *
     * @param type
     */
    public void setIndustry(String type) {
        this.industry = type;
    }

    /**
     * 地理位置信息 对象永远不能为空
     */
    public GeoPoint getPoint() {
        return point;
    }

    /**
     * 设置地理位置信息
     *
     * @param point
     */
    public void setPoint(GeoPoint point) {
        this.point = point;
    }

    /**
     * 获取poi name
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * 设置poi name
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * f_nona 终点的楼层信息
     */
    public String getFloorNo() {
        return floorNo;
    }

    /**
     * 设置终点楼层信息
     *
     * @param floorNo
     */
    public void setFloorNo(String floorNo) {
        this.floorNo = floorNo;
    }

    /**
     * 获取phone
     *
     * @return
     */
    public String getPhone() {
        return phone;
    }

    /**
     * 设置phone
     *
     * @param phone
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * 获取城市名称
     *
     * @return
     */
    public String getCityName() {
        return cityName;
    }

    /**
     * 设置城市名称
     *
     * @param cityName
     */
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    /**
     * 城市区号
     */
    public String getCityCode() {
        return cityCode;
    }

    /**
     * 设置城市区号
     *
     * @param cityCode
     */
    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    /**
     * 获取地址
     *
     * @return
     */
    public String getAddr() {
        return addr;
    }

    /**
     * 设置地址
     *
     * @param addr
     */
    public void setAddr(String addr) {
        this.addr = addr;
    }

    /**
     * 城市编码（高德标准）
     */
    public String getAdCode() {
        return adCode;
    }

    /**
     * 设置城市编码 （高德标准）
     *
     * @param addrCode
     */
    public void setAdCode(String addrCode) {
        this.adCode = addrCode;
    }

    /**
     * 距离，由模板数据返回
     */
    public String getDis() {
        return dis;
    }

    /**
     * 获取距离
     *
     * @param dis
     */
    public void setDis(String dis) {
        this.dis = dis;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getDistance() {
        return distance;
    }

    /**
     * 设置本月导航过的数据
     *
     * @param monthUv
     */
    public void setNaviMonthUv(long monthUv) {
        naviMonthUv = monthUv;
    }

    /**
     * 获取本月导航过的数量
     *
     * @return
     */
    public long getNaviMonthUv() {
        return naviMonthUv;
    }

    /**
     * 取到达点坐标（入口）
     *
     * @return
     */
    public ArrayList<GeoPoint> getEntranceList() {
        return toGeoList("entranceList");
    }

    /**
     * 设置到达点坐标(入口)
     *
     * @param inList
     */
    public void setEntranceList(ArrayList<GeoPoint> inList) {
        this.poiExtra.put("entranceList", inList);
    }

    /**
     * 取到达点坐标（出口）
     *
     * @return
     */
    public ArrayList<GeoPoint> getExitList() {
        return toGeoList("exitList");
    }

    /**
     * 设置到达点坐标（出口）
     *
     * @param exitList
     */
    public void setExitList(ArrayList<GeoPoint> exitList) {
        this.poiExtra.put("exitList", exitList);
    }

    /**
     * 取poi扩展信息
     *
     * @return
     */
    public HashMap<String, ArrayList<GeoPoint>> getPoiExtra() {
        return poiExtra;
    }

    /**
     * 获取poi 类别
     *
     * @return
     */
    public String getCategory() {
        return category;
    }

    /**
     * 设置Poi 类别
     *
     * @param str
     */
    public void setCategory(String str) {
        this.category = str;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getParent() {
        return parent;
    }

    public void setChildType(int childtype) {
        this.childType = childtype;
    }

    public int getChildType() {
        return childType;
    }

    /**
     * 子POI信息
     *
     * @param childPois
     */
    public void setChildPois(ArrayList<POI> childPois) {
        this.childPois = childPois;
    }

    public ArrayList<POI> getChildPois() {
        return childPois;
    }

    public ArrayList<ArrayList<GeoPoint>> getPoiPolygonBounds() {
        return poiPolygonBounds;
    }

    public void setPoiPolygonBounds(ArrayList<ArrayList<GeoPoint>> poiPolygonBounds) {
        this.poiPolygonBounds = poiPolygonBounds;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ArrayList<GeoPoint> toGeoList(String key) {
        ArrayList list = this.poiExtra.get(key);
        if (list != null) {
            int i = list.size();
            while (i-- > 0) {
                Object item = list.get(i);
                if (item instanceof Map) {
                    Map map = (Map) item;
                    list.set(i, new GeoPoint(((Number) map.get("x")).intValue(), ((Number) map.get("y")).intValue()));
                }
            }

        }
        return list;
    }


    public ArrayList<POI> getExtraChildSearchPoi() {
        return extraChildSearchPOI;
    }

    public void setExtraChildSearchPoi(ArrayList<POI> extraChildPoi) {
        this.extraChildSearchPOI = extraChildPoi;
    }

    public ArrayList<ArrayList<GeoPoint>> getExtPoiPolygonBounds() {
        return extPoiPolygonBounds;
    }

    public void setExtPoiPolygonBounds(ArrayList<ArrayList<GeoPoint>> extPoiPolygonBounds) {
        this.extPoiPolygonBounds = extPoiPolygonBounds;
    }

    /**
     * 获取区县名称
     *
     * @return
     */
    public String getDistrict() {
        return district;
    }

    /**
     * 设置区县名称
     *
     * @param district
     */
    public void setDistrict(String district) {
        this.district = district;
    }

    /**
     * 获取区县行政编号
     *
     * @return
     */
    public int getDistrictadcode() {
        return districtadcode;
    }

    /**
     * 获取景区个性化标签
     *
     * @return
     */
    public String getScenicMark() {
        return scenicMark;
    }

    public void setScenicMark(String mark) {
        this.scenicMark = mark;
    }

    /**
     * 获取个性化标签
     *
     * @return
     */
    public int getHisMark() {
        return hisMark;
    }

    /**
     * 设置个性化标签
     *
     * @param hisMark
     */
    public void setHisMark(int hisMark) {
        this.hisMark = hisMark;
    }

    /**
     * 设置区县行政编号
     *
     * @return
     */
    public void setDistrictadcode(int districtadcode) {
        this.districtadcode = districtadcode;
    }

    /**
     * POI搜索类型，例如加油站，停车场等
     */
    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    /**
     * 获取剩余电量
     *
     * @return
     */
    public String getChargeLeft() {
        return chargeLeft;
    }

    public void setChargeLeft(String chargeLeft) {
        this.chargeLeft = chargeLeft;
    }

    public int getChargeLeftPercentage() {
        return chargeLeftPercentage;
    }

    public void setChargeLeftPercentage(int chargeLeftPercentage) {
        this.chargeLeftPercentage = chargeLeftPercentage;
    }

    public String getEtaDis() {
        return etaDis;
    }

    public void setEtaDis(String etaDis) {
        this.etaDis = etaDis;
    }

    public SearchPoiParkingInfo getParkingInfo() {
        return parkingInfo;
    }

    public void setParkingInfo(SearchPoiParkingInfo parkingInfo) {
        this.parkingInfo = parkingInfo;
    }

    public boolean isHomeCompany() {
        return isHomeCompany;
    }

    public void setHomeCompany(boolean homeCompany) {
        isHomeCompany = homeCompany;
    }

    public void setFavItemId(String itemId) {
        favItemId = itemId;
    }

    public String getFavItemId() {
        return favItemId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.pid);
        dest.writeInt(this.type);
        dest.writeString(this.industry);
        dest.writeString(this.name);
        dest.writeString(this.phone);
        dest.writeString(this.cityName);
        dest.writeString(this.cityCode);
        dest.writeString(this.addr);
        dest.writeString(this.category);
        dest.writeString(this.adCode);
        dest.writeString(this.distance);
        dest.writeString(this.parent);
        dest.writeInt(this.childType);
        dest.writeString(this.floorNo);
        dest.writeString(this.dis);
        dest.writeInt(this.historyType);
        dest.writeString(this.district);
        dest.writeInt(this.districtadcode);
        dest.writeString(this.typeCode);
        dest.writeString(this.poiTag);
        dest.writeString(this.shortname);
        dest.writeString(this.chargeLeft);
        dest.writeString(this.etaDis);
        dest.writeByte(this.isAlongWaySearch ? (byte) 1 : (byte) 0);
        dest.writeDouble(this.maxEnergy);
        dest.writeInt(this.chargeLeftPercentage);
        dest.writeString(this.deepinfo);
        dest.writeDouble(this.ratio);
        dest.writeString(this.openStatus);
        dest.writeInt(this.hisMark);
        dest.writeString(this.scenicMark);
        dest.writeSerializable(this.parkingInfo);
        dest.writeInt(this.averageCost);
        dest.writeString(this.rating);
        dest.writeInt(this.reviewTotal);
        dest.writeString(this.imageUrl);
        dest.writeList(this.searchProductInfoList);
        dest.writeSerializable(this.rankInfo);
        dest.writeSerializable(this.photoInfo);
        dest.writeStringList(this.featuredLabel);
        dest.writeSerializable(this.mSearchDriveInfo);
        dest.writeSerializable(this.mSearchPoiGasStationInfo);
        dest.writeList(this.mSearchLabelInfo);
        dest.writeByte(this.isFastest ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isClosest ? (byte) 1 : (byte) 0);
        dest.writeLong(this.naviMonthUv);
        dest.writeString(this.alongSearchDistance);
        dest.writeString(this.alongSearchTravelTime);
        dest.writeString(this.mPathRestorationInfo);
        dest.writeString(this.subIndustry);
        dest.writeString(this.hotInfo);
        dest.writeSerializable(this.nearbyInfo);
        dest.writeByte(this.isHomeCompany ? (byte) 1 : (byte) 0);
        dest.writeString(this.favItemId);
        dest.writeInt(this.poiType);
        dest.writeString(this.arriveTimes);
        dest.writeString(this.remindBatteryValue);
        dest.writeInt(this.childIndex);
        dest.writeSerializable(this.galleryInfo);
        dest.writeList(this.voucherList);
        dest.writeList(this.productList);
        dest.writeList(this.rankBarInfoList);
        dest.writeStringList(this.foodCategory);
        dest.writeSerializable(this.point);
        dest.writeSerializable(this.poiExtra);
        dest.writeList(this.childPois);
        dest.writeList(this.poiPolygonBounds);
        dest.writeList(this.poiRoadaoiBounds);
        dest.writeList(this.parkInfos);
        dest.writeList(this.extraChildSearchPOI);
        dest.writeList(this.extPoiPolygonBounds);
        dest.writeSerializable(this.gasInfo);
        dest.writeSerializable(this.chargeStationInfo);
    }

    public void readFromParcel(Parcel source) {
        this.id = source.readString();
        this.pid = source.readString();
        this.type = source.readInt();
        this.industry = source.readString();
        this.name = source.readString();
        this.phone = source.readString();
        this.cityName = source.readString();
        this.cityCode = source.readString();
        this.addr = source.readString();
        this.category = source.readString();
        this.adCode = source.readString();
        this.distance = source.readString();
        this.parent = source.readString();
        this.childType = source.readInt();
        this.floorNo = source.readString();
        this.dis = source.readString();
        this.historyType = source.readInt();
        this.district = source.readString();
        this.districtadcode = source.readInt();
        this.typeCode = source.readString();
        this.poiTag = source.readString();
        this.shortname = source.readString();
        this.chargeLeft = source.readString();
        this.etaDis = source.readString();
        this.isAlongWaySearch = source.readByte() != 0;
        this.maxEnergy = source.readDouble();
        this.chargeLeftPercentage = source.readInt();
        this.deepinfo = source.readString();
        this.ratio = source.readDouble();
        this.openStatus = source.readString();
        this.hisMark = source.readInt();
        this.scenicMark = source.readString();
        this.parkingInfo = (SearchPoiParkingInfo) source.readSerializable();
        this.averageCost = source.readInt();
        this.rating = source.readString();
        this.reviewTotal = source.readInt();
        this.imageUrl = source.readString();
        this.searchProductInfoList = new ArrayList<SearchProductInfo>();
        source.readList(this.searchProductInfoList, SearchProductInfo.class.getClassLoader());
        this.rankInfo = (SearchPoiRankInfo) source.readSerializable();
        this.photoInfo = (SearchPoiPhoto) source.readSerializable();
        this.featuredLabel = source.createStringArrayList();
        this.mSearchDriveInfo = (SearchDriveInfo) source.readSerializable();
        this.mSearchPoiGasStationInfo = (SearchPoiGasStationInfo) source.readSerializable();
        this.mSearchLabelInfo = new ArrayList<SearchLabelInfo>();
        source.readList(this.mSearchLabelInfo, SearchLabelInfo.class.getClassLoader());
        this.isFastest = source.readByte() != 0;
        this.isClosest = source.readByte() != 0;
        this.naviMonthUv = source.readLong();
        this.alongSearchDistance = source.readString();
        this.alongSearchTravelTime = source.readString();
        this.mPathRestorationInfo = source.readString();
        this.subIndustry = source.readString();
        this.hotInfo = source.readString();
        this.nearbyInfo = (SearchPoiNearbyInfo) source.readSerializable();
        this.isHomeCompany = source.readByte() != 0;
        this.favItemId = source.readString();
        this.poiType = source.readInt();
        this.arriveTimes = source.readString();
        this.remindBatteryValue = source.readString();
        this.childIndex = source.readInt();
        this.galleryInfo = (SearchPicGallery) source.readSerializable();
        this.voucherList = new ArrayList<PoiDetailProductBean>();
        source.readList(this.voucherList, PoiDetailProductBean.class.getClassLoader());
        this.productList = new ArrayList<PoiDetailProductBean>();
        source.readList(this.productList, PoiDetailProductBean.class.getClassLoader());
        this.rankBarInfoList = new ArrayList<SearchRankInfoBase>();
        source.readList(this.rankBarInfoList, SearchRankInfoBase.class.getClassLoader());
        this.foodCategory = source.createStringArrayList();
        this.point = (GeoPoint) source.readSerializable();
        this.poiExtra = (HashMap<String, ArrayList<GeoPoint>>) source.readSerializable();
        this.childPois = new ArrayList<POI>();
        source.readList(this.childPois, POI.class.getClassLoader());
        this.poiPolygonBounds = new ArrayList<ArrayList<GeoPoint>>();
        source.readList(this.poiPolygonBounds, ArrayList.class.getClassLoader());
        this.poiRoadaoiBounds = new ArrayList<ArrayList<GeoPoint>>();
        source.readList(this.poiRoadaoiBounds, ArrayList.class.getClassLoader());
        this.parkInfos = new ArrayList<GeoPoint>();
        source.readList(this.parkInfos, GeoPoint.class.getClassLoader());
        this.extraChildSearchPOI = new ArrayList<POI>();
        source.readList(this.extraChildSearchPOI, POI.class.getClassLoader());
        this.extPoiPolygonBounds = new ArrayList<ArrayList<GeoPoint>>();
        source.readList(this.extPoiPolygonBounds, ArrayList.class.getClassLoader());
        this.gasInfo = (SearchPoiGasInfo) source.readSerializable();
        this.chargeStationInfo = (ChargingStationInfo) source.readSerializable();
    }

    public POI() {
    }

    protected POI(Parcel in) {
        this.id = in.readString();
        this.pid = in.readString();
        this.type = in.readInt();
        this.industry = in.readString();
        this.name = in.readString();
        this.phone = in.readString();
        this.cityName = in.readString();
        this.cityCode = in.readString();
        this.addr = in.readString();
        this.category = in.readString();
        this.adCode = in.readString();
        this.distance = in.readString();
        this.parent = in.readString();
        this.childType = in.readInt();
        this.floorNo = in.readString();
        this.dis = in.readString();
        this.historyType = in.readInt();
        this.district = in.readString();
        this.districtadcode = in.readInt();
        this.typeCode = in.readString();
        this.poiTag = in.readString();
        this.shortname = in.readString();
        this.chargeLeft = in.readString();
        this.etaDis = in.readString();
        this.isAlongWaySearch = in.readByte() != 0;
        this.maxEnergy = in.readDouble();
        this.chargeLeftPercentage = in.readInt();
        this.deepinfo = in.readString();
        this.ratio = in.readDouble();
        this.openStatus = in.readString();
        this.hisMark = in.readInt();
        this.scenicMark = in.readString();
        this.parkingInfo = (SearchPoiParkingInfo) in.readSerializable();
        this.averageCost = in.readInt();
        this.rating = in.readString();
        this.reviewTotal = in.readInt();
        this.imageUrl = in.readString();
        this.searchProductInfoList = new ArrayList<SearchProductInfo>();
        in.readList(this.searchProductInfoList, SearchProductInfo.class.getClassLoader());
        this.rankInfo = (SearchPoiRankInfo) in.readSerializable();
        this.photoInfo = (SearchPoiPhoto) in.readSerializable();
        this.featuredLabel = in.createStringArrayList();
        this.mSearchDriveInfo = (SearchDriveInfo) in.readSerializable();
        this.mSearchPoiGasStationInfo = (SearchPoiGasStationInfo) in.readSerializable();
        this.mSearchLabelInfo = new ArrayList<SearchLabelInfo>();
        in.readList(this.mSearchLabelInfo, SearchLabelInfo.class.getClassLoader());
        this.isFastest = in.readByte() != 0;
        this.isClosest = in.readByte() != 0;
        this.naviMonthUv = in.readLong();
        this.alongSearchDistance = in.readString();
        this.alongSearchTravelTime = in.readString();
        this.mPathRestorationInfo = in.readString();
        this.subIndustry = in.readString();
        this.hotInfo = in.readString();
        this.nearbyInfo = (SearchPoiNearbyInfo) in.readSerializable();
        this.isHomeCompany = in.readByte() != 0;
        this.favItemId = in.readString();
        this.poiType = in.readInt();
        this.arriveTimes = in.readString();
        this.remindBatteryValue = in.readString();
        this.childIndex = in.readInt();
        this.galleryInfo = (SearchPicGallery) in.readSerializable();
        this.voucherList = new ArrayList<PoiDetailProductBean>();
        in.readList(this.voucherList, PoiDetailProductBean.class.getClassLoader());
        this.productList = new ArrayList<PoiDetailProductBean>();
        in.readList(this.productList, PoiDetailProductBean.class.getClassLoader());
        this.rankBarInfoList = new ArrayList<SearchRankInfoBase>();
        in.readList(this.rankBarInfoList, SearchRankInfoBase.class.getClassLoader());
        this.foodCategory = in.createStringArrayList();
        this.point = (GeoPoint) in.readSerializable();
        this.poiExtra = (HashMap<String, ArrayList<GeoPoint>>) in.readSerializable();
        this.childPois = new ArrayList<POI>();
        in.readList(this.childPois, POI.class.getClassLoader());
        this.poiPolygonBounds = new ArrayList<ArrayList<GeoPoint>>();
        in.readList(this.poiPolygonBounds, ArrayList.class.getClassLoader());
        this.poiRoadaoiBounds = new ArrayList<ArrayList<GeoPoint>>();
        in.readList(this.poiRoadaoiBounds, ArrayList.class.getClassLoader());
        this.parkInfos = new ArrayList<GeoPoint>();
        in.readList(this.parkInfos, GeoPoint.class.getClassLoader());
        this.extraChildSearchPOI = new ArrayList<POI>();
        in.readList(this.extraChildSearchPOI, POI.class.getClassLoader());
        this.extPoiPolygonBounds = new ArrayList<ArrayList<GeoPoint>>();
        in.readList(this.extPoiPolygonBounds, ArrayList.class.getClassLoader());
        this.gasInfo = (SearchPoiGasInfo) in.readSerializable();
        this.chargeStationInfo = (ChargingStationInfo) in.readSerializable();
    }

    public static final Parcelable.Creator<POI> CREATOR = new Parcelable.Creator<POI>() {
        @Override
        public POI createFromParcel(Parcel source) {
            return new POI(source);
        }

        @Override
        public POI[] newArray(int size) {
            return new POI[size];
        }
    };
}