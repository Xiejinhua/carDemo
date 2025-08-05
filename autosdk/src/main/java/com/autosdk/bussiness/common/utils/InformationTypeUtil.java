package com.autosdk.bussiness.common.utils;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.autosdk.bussiness.widget.ui.util.StringUtils;

public class InformationTypeUtil {
    public static final int INFORMATION_TYPE_UNKONWN = 0;
    public static final int INFORMATION_TYPE_GAS = 1;
    public static final int INFORMATION_TYPE_CHARGE = 2;
    public static final int INFORMATION_TYPE_CAR_WASHING = 3;
    public static final int INFORMATION_TYPE_FOOD = 4;
    public static final int INFORMATION_TYPE_PARKING = 5;
    public static final int INFORMATION_TYPE_SCENIC = 6;

    @IntDef({INFORMATION_TYPE_UNKONWN,
            INFORMATION_TYPE_GAS,
            INFORMATION_TYPE_CHARGE,
            INFORMATION_TYPE_CAR_WASHING,
            INFORMATION_TYPE_FOOD,
            INFORMATION_TYPE_PARKING,
            INFORMATION_TYPE_SCENIC})
    @Retention(RetentionPolicy.SOURCE)
    public @interface INFORMATION_POI_TYPE {
    }

    @INFORMATION_POI_TYPE
    public static int getInformationTypeSplit(String typecode) {
        int splitIndex = typecode.indexOf("|");
        int informationType = INFORMATION_TYPE_UNKONWN;
        if (splitIndex > 0) {
            informationType = InformationTypeUtil.getInformationType(typecode.substring(0, splitIndex));
            String subStr = typecode.substring(splitIndex + 1);
            while (informationType == InformationTypeUtil.INFORMATION_TYPE_UNKONWN) {
                splitIndex = subStr.indexOf("|");
                if (splitIndex > 0) {
                    informationType = InformationTypeUtil.getInformationType(subStr.substring(0, splitIndex));
                    subStr = subStr.substring(splitIndex + 1);
                } else {
                    informationType = InformationTypeUtil.getInformationType(subStr);
                    break;
                }
            }
        } else {
            informationType = InformationTypeUtil.getInformationType(typecode);
        }
        return informationType;
    }

    public static int getInformationType(String typecode) {
        return getInformationType(StringUtils.str2Int(typecode, 10, 0));
    }

    @INFORMATION_POI_TYPE
    public static int getInformationType(int typecode) {
        if (isGasStationPoi(typecode)) {
            return INFORMATION_TYPE_GAS;
        }

        if (isChargeStationPoi(typecode)) {
            return INFORMATION_TYPE_CHARGE;
        }

        if (isCarWashingPoi(typecode)) {
            return INFORMATION_TYPE_CAR_WASHING;
        }

        if (isFoodPoi(typecode)) {
            return INFORMATION_TYPE_FOOD;
        }

        if (isParkingPoi(typecode)) {
            return INFORMATION_TYPE_PARKING;
        }

        if (isScenicPoi(typecode)) {
            return INFORMATION_TYPE_SCENIC;
        }

        return INFORMATION_TYPE_UNKONWN;

    }

    public static boolean isGasStationPoi(int typecode) {
        int tmp = typecode / 100;

        return 101 == tmp;
    }

    public static boolean isChargeStationPoi(int typecode) {
        int tmp = typecode / 100;

        return 111 == tmp;
    }

    public static boolean isCarWashingPoi(int typecode) {
        int tmp = typecode / 100;

        return (104 == tmp)
                || (105 == tmp)
                || (106 == tmp)
                || (300 == tmp)
                || (100 == tmp)
                || (108 == tmp)
                || (301 == tmp);
    }

    public static boolean isFoodPoi(int typecode) {
        int tmp = typecode / 10000;

        return 5 == tmp;
    }

    public static boolean isParkingPoi(int typecode) {
        int tmp = typecode / 100;

        return 1509 == tmp;
    }

    public static boolean isScenicPoi(int typecode) {

        int tmp = typecode / 1000;

        if (tmp == 110) {
            return true;
        }

        return 140000 == typecode
                || 140100 == typecode
                || 140200 == typecode
                || 140300 == typecode
                || 140400 == typecode
                || 140600 == typecode
                || 140700 == typecode
                || 140800 == typecode
                || 80501 == typecode;
    }

    /**
     * < 汽车维修编码。参照【附录E CMS_POI分类与编码（中英文）20161115】
     * 030000  汽车维修    汽车维修    汽车维修
     * 030100  汽车维修    汽车综合维修  汽车综合维修
     * 030200  汽车维修    大众特约维修  大众维修
     * 030201  汽车维修    大众特约维修  上海大众维修
     * 030202  汽车维修    大众特约维修  一汽-大众维修
     * 030203  汽车维修    大众特约维修  斯柯达维修
     * ...
     * ...
     * ...
     * 036000  汽车维修    梅赛德斯-奔驰卡车维修 梅赛德斯-奔驰卡车维修
     * 036100  汽车维修    德国曼恩维修  德国曼恩维修
     * 036200  汽车维修    斯堪尼亚维修  斯堪尼亚维修
     * 036300  汽车维修    沃尔沃卡车维修   沃尔沃卡车维修
     * 039900  汽车维修    观致维修    观致维修
     */
    public static boolean isCarRepairPoi(int typecode) {
        int tmp = typecode / 100;

        if ((tmp >= 300) && (tmp <= 399)) {
            return (true);
        }

        return (false);
    }

    /**
     * < 宾馆酒店编码。参照【附录E CMS_POI分类与编码（中英文）20161115】
     * 100100  住宿服务    宾馆酒店    宾馆酒店
     * 100101  住宿服务    宾馆酒店    奢华酒店
     * 100102  住宿服务    宾馆酒店    五星级宾馆
     * 100103  住宿服务    宾馆酒店    四星级宾馆
     * 100104  住宿服务    宾馆酒店    三星级宾馆
     * 100105  住宿服务    宾馆酒店    经济型连锁酒店
     * 100200  住宿服务    旅馆招待所  旅馆招待所
     * 100201  住宿服务    旅馆招待所  青年旅舍
     */
    public static boolean isHotelPoi(int typecode) {
        if ((100000 == typecode)
                || (100100 == typecode)
                || (100101 == typecode)
                || (100102 == typecode)
                || (100103 == typecode)
                || (100104 == typecode)
                || (100105 == typecode)
                || (100200 == typecode)
                || (100201 == typecode)) {
            return (true);
        }

        return (false);
    }

    public static boolean isWashroomPoi(int typecode) {
        int tmp = typecode / 100;

        if (2003 == tmp) {
            return (true);
        }

        return false;
    }

    public static boolean isCngStationPoi(int typecode) {

        if (10300 == typecode) {
            return (true);
        }

        return false;
    }

    @INFORMATION_POI_TYPE
    public static int getInformationType(String bizType, String keyword) {

        if ("gas_station".equals(bizType) && isGasStationKeyword(keyword)) {
            return INFORMATION_TYPE_GAS;
        }

        if ("charging".equals(bizType)) {
            return INFORMATION_TYPE_CHARGE;
        }

        if ("dining".equals(bizType)) {
            return INFORMATION_TYPE_FOOD;
        }

        if ("scenic".equals(bizType)) {
            return INFORMATION_TYPE_SCENIC;
        }

        if ("car_service".equals(bizType)) {
            return INFORMATION_TYPE_CAR_WASHING;
        }

        if (isParkingKeyword(keyword)) {
            return INFORMATION_TYPE_PARKING;
        }

        return INFORMATION_TYPE_UNKONWN;

    }

    public static boolean isGasStationKeyword(String keyword) {

        //全部关键字：jy/jyz/jiay/jiayo/jiayou/jiayouz/jiayouzh/jiayouzha/jiayouzhan/加油/加油站

        if ("加油".equals(keyword) || "加油站".equals(keyword)) {
            return true;
        }

        keyword = keyword.toLowerCase();
        if ("jy".equals(keyword)
                || "jyz".equals(keyword)
                || "jiay".equals(keyword)
                || "jiayo".equals(keyword)
                || "jiayou".equals(keyword)
                || "jiayouz".equals(keyword)
                || "jiayouzh".equals(keyword)
                || "jiayouzha".equals(keyword)
                || "jiayouzhan".equals(keyword)) {
            return true;
        }

        return false;
    }

    public static boolean isParkingKeyword(String keyword) {

        if ("停车".equals(keyword) || "停车场".equals(keyword)) {
            return true;
        }

        return false;
    }

}
