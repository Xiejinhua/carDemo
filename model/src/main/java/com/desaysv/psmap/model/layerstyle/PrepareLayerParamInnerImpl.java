package com.desaysv.psmap.model.layerstyle;

import com.autonavi.auto.skin.NightModeGlobal;
import com.autonavi.gbl.layer.model.BizAreaType;
import com.autonavi.gbl.layer.model.BizCarType;
import com.autonavi.gbl.layer.model.BizFlyLineType;
import com.autonavi.gbl.layer.model.BizRouteType;
import com.autonavi.gbl.layer.model.PrepareLayerCarParam;
import com.autonavi.gbl.layer.model.PrepareLayerEndAreaParentPointParam;
import com.autonavi.gbl.layer.model.PrepareLayerEnergyEmptyParam;
import com.autonavi.gbl.layer.model.PrepareLayerEnergyKeyPointParam;
import com.autonavi.gbl.layer.model.PrepareLayerEnergyRemainParam;
import com.autonavi.gbl.layer.model.PrepareLayerMarkerParam;
import com.autonavi.gbl.layer.model.PrepareViaETAPointParam;
import com.autonavi.gbl.layer.observer.PrepareLayerParamInner;
import com.autonavi.gbl.map.layer.LayerItem;
import com.autonavi.gbl.map.layer.model.CarMode;
import com.autonavi.gbl.map.layer.model.LayerItemType;
import com.autonavi.gbl.map.model.MapEngineID;
import com.autonavi.gbl.pos.model.LocInfo;
import com.autosdk.bussiness.location.LocationController;
import com.autosdk.bussiness.location.constant.SdkLocStatus;
import com.autosdk.bussiness.manager.SDKManager;
import com.autosdk.bussiness.map.SurfaceViewID;
import com.autosdk.bussiness.navi.NaviController;

import timber.log.Timber;

public class PrepareLayerParamInnerImpl extends PrepareLayerParamInner {
    private @SurfaceViewID.SurfaceViewID1
    int surfaceViewId;
    private int engineID;

    protected PrepareLayerParamInnerImpl(long cPtr, boolean cMemoryOwn) {
        super(cPtr, cMemoryOwn);
    }

    public PrepareLayerParamInnerImpl(int engineID, @SurfaceViewID.SurfaceViewID1 int surfaceViewId) {
        super(engineID);
        this.engineID = engineID;
        this.surfaceViewId = surfaceViewId;
    }

    @Override
    public boolean getPrepareLayerParam(LayerItem item, PrepareLayerMarkerParam param) {
        int itemType = item.getItemType();
        int businessType = item.getBusinessType();
        switch (itemType) {
            case LayerItemType.LayerItemPointType:
                switch (businessType) {
                    case BizCarType.BizCarTypeCruise:
                    case BizCarType.BizCarTypeGuide:
                        int carMode = SDKManager.getInstance().getLayerController().getMapLayer(surfaceViewId).getCarControl().getCarMode();
                        if (CarMode.CarModeSpeed == carMode || CarMode.CarMode2D == carMode) {
                            PrepareLayerCarParam param1 = (PrepareLayerCarParam) param;
                            if (param1 != null) {
                                param1.carMode = carMode;
                                param1.showMiniCar = false;
                                LocInfo locInfo = LocationController.getInstance().getLocInfo();
                                SdkLocStatus locationStatus = LocationController.getInstance().getLocationStatus();
                                if (locationStatus == SdkLocStatus.ON_LOCATION_GPS_FAIl || locationStatus == SdkLocStatus.ON_LOCATION_FAIL) {
                                    param1.isGPSValid = false;
                                    param1.isOverSpeed = false;
                                } else {
                                    param1.isGPSValid = true;
                                    param1.isOverSpeed = NaviController.getInstance().getIsOverSpeed();
                                }


                                if (CarMode.CarModeSpeed == carMode) {
                                    if (locInfo != null) {
                                        if (!param1.isGPSValid) {
                                            param1.speed = -1;
                                        } else {
                                            param1.speed = (int) locInfo.speed;
                                        }
                                    }
                                }
                            }
                            return true;
                        }
                        break;
                    case BizFlyLineType.BizFlyLineTypePoint:
                        /*PrepareLayerFlylineParam flyLineParam = (PrepareLayerFlylineParam) param;
                        if (flyLineParam != null) {
                            int drawEndMode = FlylineDrawMode.FLYLINE_NONE_END;
                            BizControlService bizService = SDKManager.getInstance().getLayerController().getBizControlService();
                            if (null != bizService) {
                                drawEndMode = bizService.getBizFlyLineControl(MapController.getInstance().getMapView(surfaceViewId)).getDrawMode();
                            }
                            flyLineParam.drawMode = drawEndMode;
                        }*/
                        break;
                    case BizRouteType.BizRouteTypeEnergyRemainPoint:
                        Timber.i("BizRouteTypeEnergyRemainPoint");
                        /*PrepareLayerEnergyRemainParam remainParam = (PrepareLayerEnergyRemainParam) param;
                        if (remainParam != null && ElectricInfoConverter.isElectric()) {
                            float maxVechicleCharge = (float) BusinessApplicationUtils.getElectricInfo().carEnergyInfo.maxBattEnergy;
                            RoutePathPointItem pathPointItem = (RoutePathPointItem) item;
                            remainParam.showEnergyRemain = true;
                            remainParam.energyRemainPercent = (int) ((pathPointItem.getMLeftEnergy() * 0.01 / 1000 / maxVechicleCharge) * 100);
                            return true;
                        }
                        break;*/
                        PrepareLayerEnergyRemainParam energyRemainParam = (PrepareLayerEnergyRemainParam) param;
                        if (energyRemainParam != null) {
                            energyRemainParam.showEnergyRemain = false;
                            return true;
                        }
                        break;
                    case BizRouteType.BizRouteTypeEnergyEmptyPoint:
                        Timber.d("BizRouteTypeEnergyEmptyPoint");
                        PrepareLayerEnergyEmptyParam energyEmptyParam = (PrepareLayerEnergyEmptyParam) param;
                        if (energyEmptyParam != null) {
                            energyEmptyParam.showEnergyEmpty = false;
                            return true;
                        }
                        break;
                    case BizRouteType.BizRouteTypeViaETA:
                        /*PrepareViaETAPointParam pointParam = (PrepareViaETAPointParam) param;
                        if (ElectricInfoConverter.isElectric() && pointParam != null) {
                            pointParam.showEnergy = true;
                            ViaETALayerItem pathPointItem = (ViaETALayerItem) item;
                            float maxVechicleCharge = (float) BusinessApplicationUtils.getElectricInfo().carEnergyInfo.maxBattEnergy;
                            pointParam.energyLeftPercent = (int) ((pathPointItem.getMLeftEnergy() * 0.01 / 1000 / maxVechicleCharge) * 100);
                            return true;
                        }
                        break;*/
                        Timber.d("BizRouteTypeViaETA");
                        PrepareViaETAPointParam pointParam = (PrepareViaETAPointParam) param;
                        if (pointParam != null) {
                            pointParam.showEnergy = false;
                            pointParam.showETA = !NaviController.getInstance().isNaving();
                            return true;
                        }
                        break;
                    case BizRouteType.BizRouteTypeViaChargeStationPoint:
                        Timber.i("BizRouteTypeViaChargeStationPoint");
                        /*PrepareViaETAPointParam etaPointParam = (PrepareViaETAPointParam) param;
                        if (ElectricInfoConverter.isElectric() && etaPointParam != null) {
                            etaPointParam.showEnergy = true;
                            ViaETALayerItem pathPointItem = (ViaETALayerItem) item;
                            float maxVechicleCharge = (float) BusinessApplicationUtils.getElectricInfo().carEnergyInfo.maxBattEnergy;
                            etaPointParam.energyLeftPercent = (int) ((pathPointItem.getMLeftEnergy() * 0.01 / 1000 / maxVechicleCharge) * 100);
                            return true;
                        }
                        break;*/
                        PrepareViaETAPointParam etaPointParam = (PrepareViaETAPointParam) param;
                        if (etaPointParam != null) {
                            etaPointParam.showEnergy = false;
                            etaPointParam.showETA = !NaviController.getInstance().isNaving();
                            return true;
                        }
                        break;
                    case BizRouteType.BizRouteTypeEnergyKeyPoint:
                        Timber.d("BizRouteTypeEnergyKeyPoint");
                        PrepareLayerEnergyKeyPointParam keyPointParam = (PrepareLayerEnergyKeyPointParam) param;
                        if (keyPointParam != null) {
                            return true;
                        }
                        break;
                    case BizAreaType.BizAreaTypeEndAreaParentPoint:
                        Timber.d("BizAreaTypeEndAreaParentPoint");
                        /*PrepareLayerEndAreaParentPointParam endAreaParentPointParam = (PrepareLayerEndAreaParentPointParam) param;
                        if (ElectricInfoConverter.isElectric() && endAreaParentPointParam != null) {
                            endAreaParentPointParam.showEnergy = true;
                            EndAreaParentLayerItem endAreaPointLayerItem = (EndAreaParentLayerItem) item;
                            float maxVechicleCharge = (float) BusinessApplicationUtils.getElectricInfo().carEnergyInfo.maxBattEnergy;
                            endAreaParentPointParam.energyLeftPercent = (int) ((endAreaPointLayerItem.getMLeftEnergy() * 0.01 / 1000 / maxVechicleCharge) * 100);
                        }
                        break;*/
                        PrepareLayerEndAreaParentPointParam endAreaParentPointParam = (PrepareLayerEndAreaParentPointParam) param;
                        if (endAreaParentPointParam != null) {
                            endAreaParentPointParam.showEnergy = false;
                            endAreaParentPointParam.showETA = !NaviController.getInstance().isNaving();
                            return true;
                        }
                        break;
                    /*case BizRouteType.BizRouteTypeEndPoint:
                        PrepareLayerEndPointParam endPointParam = (PrepareLayerEndPointParam) param;
                        endPointParam.endMode = FamiliarEndMode.FamiliarEndModeNormal;
                        break;*/
                    default:
                        break;
                }
                break;
            case LayerItemType.LayerItemNaviCarType:
                if (param instanceof PrepareLayerCarParam) {
                    PrepareLayerCarParam param1 = (PrepareLayerCarParam) param;
                    int carMode = SDKManager.getInstance().getLayerController().getMapLayer(surfaceViewId).getCarControl().getCarMode();
                    param1.carMode = carMode;
                    Timber.d("itemType" + param1.carMode);
                    SdkLocStatus locationStatus = LocationController.getInstance().getLocationStatus();
                    if (locationStatus == SdkLocStatus.ON_LOCATION_GPS_FAIl || locationStatus == SdkLocStatus.ON_LOCATION_FAIL) {
                        param1.isGPSValid = false;
                        param1.isOverSpeed = false;
                    } else {
                        param1.isGPSValid = true;
                        param1.isOverSpeed = NaviController.getInstance().getIsOverSpeed();
                    }
                    Timber.d("param1.isGPSValid:" + param1.isGPSValid);
                    int scale = SDKManager.getInstance().getMapController().getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN).getOperatorScale().getCurrentScale();
                    if (SurfaceViewID.transform2EngineID(engineID) == MapEngineID.MapEngineIdMainEagleEye) {
                        param1.showMiniCar = false;
                    } else {
                        if (CarMode.CarModeSpeed == carMode) {
                            param1.showMiniCar = false;
                        } else {
                            if (scale >= 1000) {
                                param1.showMiniCar = true;
                            } else {
                                param1.showMiniCar = false;
                            }
                        }
                    }

                }
                break;
            default:
                break;
        }

        return super.getPrepareLayerParam(item, param);
    }

    @Override
    public boolean isNightMode() {
        return NightModeGlobal.isNightMode();
    }


    @Override
    public boolean isRouteStyleNightMode() {
        return NightModeGlobal.isNightMode();
    }

    @Override
    public float getPointMarkerScaleFactor() {
        return 1.0f;
        //return DisplayInfoManager.getInstance().getMarkerScaleRatio(surfaceViewId);
    }
}
