package com.desaysv.psmap.model.layerstyle.parser;

import android.content.res.AssetManager;
import android.location.Location;
import android.location.LocationManager;

import com.autonavi.gbl.layer.BizControlService;
import com.autonavi.gbl.layer.SpeedCarLayerItem;
import com.autonavi.gbl.layer.model.BizCarType;
import com.autonavi.gbl.map.layer.BaseLayer;
import com.autonavi.gbl.map.layer.LayerItem;
import com.autonavi.gbl.map.layer.model.CarMode;
import com.autonavi.gbl.map.layer.model.Layer3DModel;
import com.autonavi.gbl.util.model.BinaryStream;
import com.autosdk.bussiness.location.LocationController;
import com.autosdk.bussiness.location.constant.SdkLocStatus;
import com.autosdk.bussiness.manager.SDKManager;
import com.desaysv.psmap.base.auto.layerstyle.utils.StyleJsonAnalysisUtil;

import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;


public class CarLayerStyleParser {
    private final int THREE_D_CAR_STYLE_RES_ID = 0x10002;
    private String m3DModelName;

    public @interface CarLocationType {
        /**
         * 无效值
         */
        int CAR_INVALID_LOCATION_TYPE = 0;

        /**
         * 从未有过GPS定位 当前是网络定位方式
         */
        int CAR_NET_LOCATION_TYPE = 1;

        /**
         * 无网络无GPS定位方式
         */
        int CAR_NO_GPS_LOCATION_TYPE = 2;

        /**
         * GPS定位过的方式 只要有过gps定位,就使用此种方式
         */
        int CAR_GPS_LOCATION_TYPE = 3;
    }

    public @CarLocationType int getCarLocationType() {
        @CarLocationType int carLocationType = CarLocationType.CAR_NET_LOCATION_TYPE;

        Location location = SDKManager.getInstance().getLocController().getLastLocation();

        if (null != location) {

            if (LocationManager.GPS_PROVIDER.equals(location.getProvider())) {
                carLocationType = CarLocationType.CAR_GPS_LOCATION_TYPE;
            } else if (LocationManager.NETWORK_PROVIDER.equals(location.getProvider())) {
                carLocationType = CarLocationType.CAR_NET_LOCATION_TYPE;
            } else {
                carLocationType = CarLocationType.CAR_NO_GPS_LOCATION_TYPE;
            }
        }

        return carLocationType;
    }

    public boolean getGPSValid() {

        boolean valid = false;
        Location location = SDKManager.getInstance().getLocController().getLastLocation();

        if (null != location) {

            //经度最大是180° 最小是0°
            double longitude = location.getLongitude();
            if (0.0 > longitude || 180.0 < longitude) {
                return valid;
            }

            //纬度最大是90° 最小是0°
            double latitude = location.getLatitude();
            if (0.0 > latitude || 90.0 < latitude) {
                return valid;
            }

            valid = true;

        }

        return valid;
    }

    public String getCarLayerPointStyle(LayerItem layerItem, StyleJsonAnalysisUtil jsonAnalysis) {
        String strStyleJson = "EMPTY";
        if (null != jsonAnalysis && null != layerItem) {
            SpeedCarLayerItem speedCarItem = (SpeedCarLayerItem) layerItem;
            if (speedCarItem.getBBackGround()) {
                strStyleJson = jsonAnalysis.getStyleBeanJson("point_speed_car_base");
            } else {
                strStyleJson = jsonAnalysis.getStyleBeanJson("point_speed_car");
            }
        }

        return strStyleJson;
    }

    public String getCarLayer3DModelStyle(@BizCarType.BizCarType1 int carType, StyleJsonAnalysisUtil jsonAnalysis) {
        String strStyleJson = "EMPTY";
        if (null != jsonAnalysis) {
            switch (carType) {
                case BizCarType.BizCarTypeCruise:
                case BizCarType.BizCarTypeGuide:
                case BizCarType.BizCarTypeSearch:
                case BizCarType.BizCarTypeFamiliar:
                    strStyleJson = jsonAnalysis.getStyleBeanJson("3dcar_style");
                    break;
                default:
                    break;
            }
        }
        return strStyleJson;
    }


    public int addLayer3DModel(AssetManager assetManager, BaseLayer pLayer, String str3DModelId) {
        int retValue = -1;

        if (null != assetManager && null != pLayer && null != str3DModelId) {
            InputStream input = null;
            try {
                String str3DModelFile;
                SdkLocStatus locationStatus = LocationController.getInstance().getLocationStatus();
                if (locationStatus == SdkLocStatus.ON_LOCATION_GPS_FAIl || locationStatus == SdkLocStatus.ON_LOCATION_FAIL) {
                    str3DModelFile = "car_3d_logo/1/carLogoLow.dat";//获取定位失败,车标置灰
                } else {
                    str3DModelFile = "car_3d_logo/1/carLogo.dat";
                }

                input = assetManager.open(str3DModelFile);
                if (null != input) {
                    int len = input.available();
                    byte[] buffer = new byte[len];
                    int read = input.read(buffer);
                    Timber.d("addLayer3DModel len=%s read=%s", len, read);
                    Layer3DModel modelParam = new Layer3DModel();
                    modelParam.resourceID = THREE_D_CAR_STYLE_RES_ID;
                    modelParam.dataBuff = new BinaryStream(buffer);

                    if (null == m3DModelName || m3DModelName.isEmpty()) {
                        pLayer.getMapView().addLayer3DModel(modelParam);
                    } else if (!m3DModelName.equals(str3DModelFile)) {
                        pLayer.getMapView().updateLayer3DModel(modelParam);
                    }

                    m3DModelName = str3DModelFile;
                    retValue = THREE_D_CAR_STYLE_RES_ID;
                }
            } catch (IOException e) {
                Timber.e("addLayer3DModel Exception:%s", e.getMessage());
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        Timber.e("addLayer3DModel finally Exception:%s", e.getMessage());
                    }
                }
            }
        }

        Timber.d("get3DModelId str3DModelId=" + str3DModelId + ", retValue=" + retValue);
        return retValue;
    }

    public String getCarLayerStyle(final BaseLayer pLayer, LayerItem layerItem, boolean bNightMode, @BizCarType.BizCarType1 int carType, StyleJsonAnalysisUtil jsonAnalysis) {
        String strStyleJson = "EMPTY";

        if (null == pLayer || null == layerItem || null == jsonAnalysis) {
            return strStyleJson;
        }

        BizControlService bizService = SDKManager.getInstance().getLayerController().getBizControlService();

        if (null == bizService || BizCarType.BizCarTypeInvalid == carType) {
            return strStyleJson;
        }

//        @CarMode.CarMode1 int carMode = bizService.getBizCarControl(pLayer.getMapView()).getCarMode();
        String info = layerItem.getInfo();
        int carMode = Integer.parseInt(info);

        if (BizCarType.BizCarTypeRoute == carType) {
            //strStyleJson = "car_route_normal";
            strStyleJson = "car_other_2d";
            strStyleJson += (bNightMode ? "_night" : "_day");

        } else if (BizCarType.BizCarTypeEagleEye == carType) {
            strStyleJson = bNightMode ? "car_eagle_normal_night" : "car_eagle_normal";

        } else {
            /*@CarLocationType int carLocationType = getCarLocationType();
            if ((BizCarType.BizCarTypeCruise == carType) && CarLocationType.CAR_NET_LOCATION_TYPE == carLocationType) {

                strStyleJson = "car_cruise_net_loc";
            }*/

            int scale = pLayer.getMapView().getOperatorScale().getCurrentScale();

            Timber.d("getCarLayerStyle scale:%s carType:%s", scale, carType);

            if (scale >= 1000) {
                /*SdkLocStatus locationStatus = LocationController.getInstance().getLocationStatus();
                if (locationStatus == SdkLocStatus.ON_LOCATION_GPS_FAIl || locationStatus == SdkLocStatus.ON_LOCATION_FAIL) {
                    strStyleJson = bNightMode ? "car_other_nogps_night" : "car_other_nogps_day";
                } else {
                    strStyleJson = bNightMode ? "car_other_gps_night" : "car_other_gps_day";
                }*/
                strStyleJson = "car_other_2d";
                strStyleJson += (bNightMode ? "_night" : "_day");
            } else {
                if (CarMode.CarMode3D == carMode || CarMode.CarModeSkeleton == carMode) {
                    strStyleJson = "car_other_3d";
                    strStyleJson += (bNightMode ? "_night" : "_day");
                } else {
                    strStyleJson = "car_other_2d";
                    strStyleJson += (bNightMode ? "_night" : "_day");
                    /*if (CarMode.CarMode2D == carMode) {
                        // 与网络定位方式使用相同车标
                        strStyleJson = "car_cruise_net_loc";
                    } else {
                        return strStyleJson;
                    }*/
                }

                /*if (BizCarType.BizCarTypeSearch == carType) {
                 *//**< 搜索结果界面使用不带罗盘的3D或车速车标 *//*
                 *//**< 3D车标显示底盘, Skeleton车标不显示底盘 *//*
                    if (CarMode.CarModeSkeleton != carMode) {
                        strStyleJson += "_search";
                    } else {
                        strStyleJson = "car_other_skeleton_search";
                    }
                }*/
            }
        }

        Timber.d("strStyleJson:" + strStyleJson);

        return jsonAnalysis.getStyleBeanJson(strStyleJson);
    }
}
