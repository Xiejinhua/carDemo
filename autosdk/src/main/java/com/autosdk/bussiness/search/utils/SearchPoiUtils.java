package com.autosdk.bussiness.search.utils;

import com.autosdk.bussiness.common.POI;

import java.util.Objects;


public class SearchPoiUtils {
    private static final String OPEN_STATUS_TYPE1 = "正常状态";

    private static final String OPEN_STATUS_TYPE2 = "营业中";
    /**
     * 定义POI类型
     */
    public enum ClassifyPOI{
        /*无效*/
        INVALID,
        /*美食*/
        FOOD,
        /*商场*/
        SHOPPING,
        /*景区*/
        SCENIC,
        /*停车场*/
        PARKING,
        /*充电站*/
        CHARGE_STATION,
        /*卫生间、休息室*/
        RESTROOM,
        /*洗车*/
        CAR_WASHING,
        /*加油站*/
        GAS_STATION,
        /*美食-咖啡*/
        FOOD_COFFEE,
        /*美食-甜品*/
        FOOD_DESSERT_SHOP,
        /*美食-火锅*/
        FOOD_HOT_POT,
        /*美食-中餐*/
        FOOD_CHINESE_FOOD,
        /*景区-动物园*/
        SCENIC_ZOO,
        /*景区-植物园*/
        SCENIC_BOTANICAL_GARDEN,
        /*景区-公园*/
        SCENIC_PARK,
        /*景区-风景名胜*/
        SCENIC_TOURIST_ATTRACTION,
        /*休闲场所*/
        SCENIC_RELAXATION_PLACE,
        /*服务区*/
        SERVICE_AREA
    }
    public static int getTypeCode(String typeCode){
        if( typeCode == null || typeCode.isEmpty()) {return 0;}
        String[] code = typeCode.split("\\|");
        try{
            return Integer.parseInt(code[0]);
        }catch (NumberFormatException ignore){}
        return 0;
    }
    public static boolean isFoodPoiType(final POI data) {
        return isFoodPoi(getTypeCode(data.getTypeCode())) || ((Objects.equals(data.getIndustry(), "dining") && !Objects.equals(data.getPoiTag(), "儿童乐园") ));
    }

    public static boolean isMallPoiType(final POI data) {
        return IsShoppingMallPoi(getTypeCode(data.getTypeCode()));
    }

    public static boolean isCarWashingPoiType(final POI data) {
        return IsCarWashingPoi(getTypeCode(data.getTypeCode()));
    }

    public static boolean IsServiceAreaPoiType(final POI data) {
        return IsServiceAreaPoi(getTypeCode(data.getTypeCode()));
    }

    public static boolean IsGasStationPoiType(final POI data) {
        return IsGasStationPoi(getTypeCode(data.getTypeCode()));
    }

    public static boolean isScenicPoiType(final POI data) {
        return IsScenicPoi(getTypeCode(data.getTypeCode()));
    }

    public static boolean isRelaxationPoiType(final POI data) {
        return isRelaxationPlace(getTypeCode(data.getTypeCode()), data.getPoiTag());
    }

    public static boolean isParkingPoiType(final POI data) {
        return IsParkingPoi(getTypeCode(data.getTypeCode()));
    }

    /**
     * POI类型是否是充电站
     * @param data     poi点
     * @return boolean
     */
    public static boolean IsChargingStation(final POI data) {
        return IsChargingStation(data.getTypeCode());
    }

    public static boolean IsChargingStation(String typeCode) {
        int code = getTypeCode(typeCode) / 100;
        // 参照搜索模块，011100为充电站
        return 111 == code;
    }

    /**
     * POI类型是厕所相关
     * @param data poi点
     * @return boolean
     */
    public static boolean isRestRoom(final POI data){
        int code =getTypeCode(data.getTypeCode()) / 1000;
        if (200 == code) {
           code =getTypeCode(data.getTypeCode()) % 1000;
            return code >= 300 && code <= 399;
        }
        return false;
    }

    public static boolean isRestRoom(String typeCode) {
        int code =getTypeCode(typeCode) / 1000;
        if (200 == code) {
            code =getTypeCode(typeCode) % 1000;
            return code >= 300 && code <= 399;
        }
        return false;
    }

    public static boolean IsScenicPoi(int typeCode) {
        int tmp = typeCode / 1000;

        if (tmp == 110) {
            return true;
        }
        // 140000 140100 140200 140300 140400 140600 140700 140800
        if (tmp == 140) {
            return true;
        }
        // 休闲类别
        return typeCode == 80501;
    }

    /**
     * 休闲场所
     * @param typeCode 080501
     * @return boolean
     */
    public static boolean isRelaxationPlace(int typeCode,String tag) {
        int tmp = typeCode / 1000;
        int suffix = typeCode % 1000;
        if ((tmp == 80) && (suffix >= 500 && suffix < 600) ) {
            return true;
        }
        /*061205 书店*/
        if ((tmp == 61) && suffix == 205 ) {
            return true;
        }

        if ((tmp == 170) && suffix == 201 ) {
            return Objects.equals("儿童乐园", tag);
        }
        /*游泳官*/
        return 72001 == typeCode;
    }

    public static boolean isFoodPoi(int typeCode) {
        int tmp = typeCode / 10000;
        return 5 == tmp;
    }

    /**
     * 判断POI是否为咖啡类型
     * @param data poi点
     * @return
     * 050500 050501 050502 050503 050504 咖啡类型区间
     */
     public static boolean isFoodCoffeeType(final POI data) {
        int typeCode= getTypeCode(data.getTypeCode())/100;
         return typeCode == 505;
     }

    /**
     * 判断POI类别是否为甜品类型
     * @param data poi点
     * @return boolean
     * 050900 甜品类型
     */
    public static boolean isFoodDessertShop(final POI data) {
        int typeCode= getTypeCode(data.getTypeCode())/100;
        return typeCode == 509;
    }
    /**
     * 判断POI类别是否为火锅
     * @param data poi点
     * @return
     * 050117 火锅类型
     */
    public static boolean isFoodHotPot(final POI data) {
        int typeCode= getTypeCode(data.getTypeCode());
        return typeCode == 50117;
    }

    /**
     * 判断POI类别是否为中餐厅
     * @param data poi
     * @return
     * 050100
     */
    public static boolean isFoodChineseFood(final POI data) {
        int typeCode= getTypeCode(data.getTypeCode());
        /*火锅包含在中餐厅里。要先排除一下*/
        if(typeCode != 50117){
            typeCode = typeCode /100;
            return typeCode != 501;
        }
        return false;
    }

    /**
     * 判断POI类别是否为动物园
     * @param data poi
     * @return boolean
     */
    public static boolean isScenicZoo(final POI data) {
        int typeCode= getTypeCode(data.getTypeCode());
        return typeCode == 110102;
    }
    /**
     * 判断POI类别是否为植物园
     * @param data poi
     * @return boolean
     */
    public static boolean isScenicBotanicalGarden(final POI data) {
        int typeCode= getTypeCode(data.getTypeCode());
        return typeCode == 110103;
    }

    /**
     * 判断POI类型是否为公园
     * @param data poi
     * @return boolean
     */
    public static boolean isScenicPark(final POI data) {
        int typeCode= getTypeCode(data.getTypeCode());
        return typeCode == 110101;
    }

    /**
     * 判断POI类型是否为风景名胜
     * @param data poi
     * @return boolean
     */
    public static boolean isScenicTouristAttraction(final POI data) {
        int typeCode= getTypeCode(data.getTypeCode());
        return typeCode >= 110200 && typeCode <= 110210;
    }

    /**< 停车场编码。参照【附录E CMS_POI分类与编码（中英文）20161115】
     150900  交通设施服务  停车场   停车场相关
     150903  交通设施服务  停车场   换乘停车场
     150904  交通设施服务  停车场   公共停车场
     150905  交通设施服务  停车场   专用停车场
     150906  交通设施服务  停车场   路边停车场
     150907  交通设施服务  停车场   停车场入口
     150908  交通设施服务  停车场   停车场出口
     150909  交通设施服务  停车场   停车场出入口
     */
    public static boolean IsParkingPoi(int typeCode) {
        int tmp = typeCode / 100;

        return 1509 == tmp;
    }

    public static boolean IsShoppingMallPoi(int typeCode) {
        int tmp = typeCode / 1000;
        return 60 == tmp || 61 == tmp;
    }

    public static boolean IsServiceAreaPoi(int typeCode) {
        return (180300 == typeCode) || (180301 == typeCode) || (180302 == typeCode);
    }

    public static boolean IsCarWashingPoi(int typeCode) {
        int tmp = typeCode / 100;
        return (104 == tmp)
                || (105 == tmp)
                || (106 == tmp)
                || (300 == tmp)
                || (301 == tmp)
                || (100 == tmp)
                || (10 == tmp)
                || (108 == tmp);
    }

    /**< 加油站编码。参照【附录E CMS_POI分类与编码（中英文）20161115】
     010100  汽车服务    加油站   加油站
     010101  汽车服务    加油站   中国石化
     010102  汽车服务    加油站   中国石油
     010103  汽车服务    加油站   壳牌
     010104  汽车服务    加油站   美孚
     010105  汽车服务    加油站   加德士
     010107  汽车服务    加油站   东方
     010108  汽车服务    加油站   中石油碧辟
     010109  汽车服务    加油站   中石化碧辟
     010110  汽车服务    加油站   道达尔
     010111  汽车服务    加油站   埃索
     010112  汽车服务    加油站   中化道达尔
     */
    public static boolean IsGasStationPoi(int typeCode) {
        int tmp = typeCode / 100;
        return 101 == tmp;
    }

    public static boolean IsGasStationPoi(String typeCode) {
        return IsGasStationPoi(getTypeCode(typeCode));
    }

    /**
     * 获取poi大类别里的子类别
     * @param parent poi大类别
     * @param poi poi数据
     * @return 返回相应的子品类，如果没找到，返回相应的父品类
     */
    public static ClassifyPOI getPoiSubClassify(ClassifyPOI parent, final POI poi){
        if( ClassifyPOI.FOOD == parent){
            if(isFoodCoffeeType(poi)){
                return ClassifyPOI.FOOD_COFFEE;
            }
            if(isFoodChineseFood(poi)){
                return ClassifyPOI.FOOD_CHINESE_FOOD;
            }
            if(isFoodHotPot(poi)){
                return ClassifyPOI.FOOD_HOT_POT;
            }
            if(isFoodDessertShop(poi)){
                return ClassifyPOI.FOOD_DESSERT_SHOP;
            }
        }

        if( ClassifyPOI.SCENIC == parent ){
            if(isScenicZoo(poi)){
                return ClassifyPOI.SCENIC_ZOO;
            }
            if(isScenicPark(poi)){
                return ClassifyPOI.SCENIC_PARK;
            }
            if(isScenicBotanicalGarden(poi)){
                return ClassifyPOI.SCENIC_BOTANICAL_GARDEN;
            }
            if(isScenicTouristAttraction(poi)){
                return ClassifyPOI.SCENIC_TOURIST_ATTRACTION;
            }
        }

        return parent;
    }
    /**
     * 获取POI分类
     * @param poi poi点
     * @return  ClassifyPOI
     */
    public static ClassifyPOI getPoiClassify(POI poi){
        if(SearchPoiUtils.isFoodPoiType(poi)){
            return ClassifyPOI.FOOD;
        }
        if(SearchPoiUtils.isMallPoiType(poi)){
            return ClassifyPOI.SHOPPING;
        }
        if(SearchPoiUtils.isScenicPoiType(poi) || SearchPoiUtils.isRelaxationPoiType(poi)){
            return ClassifyPOI.SCENIC;
        }
        if(SearchPoiUtils.isParkingPoiType(poi)){
            return ClassifyPOI.PARKING;
        }
        if(SearchPoiUtils.IsChargingStation(poi)){
            return ClassifyPOI.CHARGE_STATION;
        }
        if(SearchPoiUtils.isRestRoom(poi)){
            return ClassifyPOI.RESTROOM;
        }
        if(SearchPoiUtils.isCarWashingPoiType(poi)){
            return ClassifyPOI.CAR_WASHING;
        }
        if (SearchPoiUtils.IsGasStationPoiType(poi)) {
            return ClassifyPOI.GAS_STATION;
        }
        if (SearchPoiUtils.IsServiceAreaPoiType(poi)) {
            return ClassifyPOI.SERVICE_AREA;
        }
        return ClassifyPOI.INVALID;
    }

    public static ClassifyPOI getPoiClassify(String type) {
        return getPoiClassify(type, "");
    }

    public static ClassifyPOI getPoiClassify(String type, String tag) {
        if(isFoodPoi(getTypeCode(type))){
            return ClassifyPOI.FOOD;
        }
        if(IsShoppingMallPoi(getTypeCode(type))){
            return ClassifyPOI.SHOPPING;
        }
        if(IsScenicPoi(getTypeCode(type)) || isRelaxationPlace(getTypeCode(type), tag)) {
            return ClassifyPOI.SCENIC;
        }
        if(IsParkingPoi(getTypeCode(type))){
            return ClassifyPOI.PARKING;
        }
        if(IsChargingStation(type)) {
            return ClassifyPOI.CHARGE_STATION;
        }
        if(isRestRoom(type)){
            return ClassifyPOI.RESTROOM;
        }
        if(IsCarWashingPoi(getTypeCode(type))){
            return ClassifyPOI.CAR_WASHING;
        }
        if (IsGasStationPoi(getTypeCode(type))) {
            return ClassifyPOI.GAS_STATION;
        }
        if (IsServiceAreaPoi(getTypeCode(type))) {
            return ClassifyPOI.SERVICE_AREA;
        }
        return ClassifyPOI.INVALID;
    }

    public static String getPoiKeyword(POI poi){
        ClassifyPOI type = getPoiClassify(poi);
        String keyword = "";
        switch (type){
            case FOOD:
                keyword = "美食";
                break;
            case SHOPPING:
                keyword = "商场";
                break;
            case SCENIC:
                keyword = "景区";
                break;
            case PARKING:
                keyword = "停车场";
                break;
            case CHARGE_STATION:
                keyword = "充电站";
                break;
            case RESTROOM:
                keyword = "卫生间";
                break;
            default:
                break;
        }
        return keyword;
    }

    /**
     * 判断当前状态是否正在营业中
     * @param openStatus openStatus
     * @return boolean
     */
    public static boolean isOpen(String openStatus){
        return OPEN_STATUS_TYPE1.equals(openStatus) || OPEN_STATUS_TYPE2.equals(openStatus) || openStatus.isEmpty();
    }

    /**
     * 是否为行政区域
     * @param data
     * @return
     */
    public static boolean isRegionPoi(POI data) {
        int typeCode = getTypeCode(data.getTypeCode());
        return typeCode >= 190101 && typeCode <= 190109;
    }

}
