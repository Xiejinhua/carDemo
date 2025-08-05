package com.desaysv.psmap.model.layerstyle;

import static com.autonavi.gbl.map.layer.model.LayerIconType.LayerIconTypeBMP;

import android.annotation.SuppressLint;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.autonavi.gbl.map.layer.BaseLayer;
import com.autonavi.gbl.map.layer.LayerItem;
import com.autonavi.gbl.map.layer.model.CustomTextureParam;
import com.autonavi.gbl.map.layer.model.CustomUpdateParam;
import com.autonavi.gbl.map.layer.model.ItemStyleInfo;
import com.autonavi.gbl.map.layer.model.LayerIconAnchor;
import com.autonavi.gbl.map.layer.model.LayerItemType;
import com.autonavi.gbl.map.layer.model.LayerTexture;
import com.autonavi.gbl.map.layer.model.RouteLayerStyle;
import com.autonavi.gbl.map.layer.observer.IPrepareLayerStyle;
import com.autonavi.gbl.util.model.BinaryStream;
import com.autosdk.bussiness.common.utils.FileUtils;
import com.autosdk.bussiness.layer.ChargeStationLayerItem;
import com.autosdk.bussiness.layer.ElectricBusinessTypePoint;
import com.autosdk.common.utils.BitmapUtils;
import com.autosdk.common.utils.ResUtil;
import com.desaysv.psmap.base.auto.layerstyle.bean.MarkerInfoBean;
import com.desaysv.psmap.base.auto.layerstyle.utils.StyleJsonAnalysisUtil;
import com.desaysv.psmap.model.R;

import java.nio.ByteBuffer;

public class CustomPrepareStyleImpl implements IPrepareLayerStyle {

    private final Application mApplication;
    private final StyleJsonAnalysisUtil mStyleJsonAnalysisUtil;

    public CustomPrepareStyleImpl(Application application) {
        this.mApplication = application;
        String fileStringFromAssets = FileUtils.getFileStringFromAssets(mApplication, "style.json");
        this.mStyleJsonAnalysisUtil = new StyleJsonAnalysisUtil(fileStringFromAssets);
    }

    @Override
    public int getMarkerId(BaseLayer baseLayer, LayerItem layerItem, ItemStyleInfo itemStyleInfo) {
        switch (layerItem.getBusinessType()) {
            case ElectricBusinessTypePoint.CHARGE_STATION:
                return addChargeStationMarker(baseLayer, layerItem, itemStyleInfo.markerId, itemStyleInfo.markerInfo);
            case ElectricBusinessTypePoint.CHARGE_STATION_DETAIL:
                return addChargeStationDetailMarker(baseLayer, layerItem, itemStyleInfo.markerInfo);
            default:
                break;
        }
        return 0;
    }

    @Override
    public boolean getCustomTexture(BaseLayer baseLayer, LayerItem layerItem, ItemStyleInfo itemStyleInfo, CustomTextureParam customTextureParam) {
        return false;
    }

    @Override
    public boolean updateCustomTexture(BaseLayer baseLayer, LayerItem layerItem, ItemStyleInfo itemStyleInfo, CustomUpdateParam customUpdateParam) {
        return false;
    }

    public synchronized int addChargeStationDetailMarker(BaseLayer baseLayer, LayerItem item, String markerInfo) {
        ChargeStationLayerItem stationLayerItem = (ChargeStationLayerItem) item;
        View view = View.inflate(mApplication, R.layout.global_texture_charge_station_alert, null);
        TextView chargeStationInfoTv = view.findViewById(R.id.charge_station_info);
        chargeStationInfoTv.setText(stationLayerItem.getStationInfo().name + " 充电" + (int) (stationLayerItem.getStationInfo().chargeTime / 60) + "分钟");
        Bitmap dynamicBitmap = BitmapUtils.createBitmapFromView(view);
        ByteBuffer dataBuffer = ByteBuffer.allocate(dynamicBitmap.getByteCount());
        dynamicBitmap.copyPixelsToBuffer(dataBuffer);
        LayerTexture layerTexture = new LayerTexture();
        layerTexture.dataBuff = new BinaryStream(dataBuffer.array());
        layerTexture.width = dynamicBitmap.getWidth();
        layerTexture.height = dynamicBitmap.getHeight();
        layerTexture.iconType = LayerIconTypeBMP;
        setLayerTexture(markerInfo, layerTexture);
        layerTexture.anchorType = LayerIconAnchor.LayerIconAnchorRightTop;
        int dynamicMarkerId = getDynamicMarkerId();
        layerTexture.resID = dynamicMarkerId;
        baseLayer.getMapView().addLayerTexture(layerTexture);
        return dynamicMarkerId;
    }

    private int mDynamicMarkerId = 0x90000;

    public int getDynamicMarkerId() {
        return mDynamicMarkerId++;
    }


    public synchronized int addChargeStationMarker(BaseLayer baseLayer, LayerItem layerItem, String markerId, String markerInfo) {
        int identifier = mApplication.getResources().getIdentifier(markerId, "drawable", mApplication.getPackageName());
        if (identifier == 0) {
            return -1;
        }

        Bitmap bitmap = BitmapFactory.decodeResource(mApplication.getResources(), identifier);
        ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(byteBuffer);
        LayerTexture layerTexture = new LayerTexture();

        //设置锚点
        if (TextUtils.isEmpty(markerInfo)) {
            markerInfo = markerId;
        }
        setLayerTexture(markerInfo, layerTexture);
        layerTexture.isPreMulAlpha = true;
        layerTexture.dataBuff = new BinaryStream(byteBuffer.array());
        layerTexture.width = bitmap.getWidth();
        layerTexture.height = bitmap.getHeight();
        layerTexture.name = markerId;
        layerTexture.resID = identifier;
        layerTexture.iconType = LayerIconTypeBMP;
        baseLayer.getMapView().addLayerTexture(layerTexture);
        return identifier;
    }

    @SuppressLint("WrongConstant")
    private void setLayerTexture(String strMarkerInfo, LayerTexture layerTexture) {
        MarkerInfoBean markerInfoBean = mStyleJsonAnalysisUtil.getMarkerInfoFromJson(strMarkerInfo);
        if (markerInfoBean != null) {
            layerTexture.anchorType = markerInfoBean.getAnchor();
            layerTexture.xRatio = markerInfoBean.getX_ratio();
            layerTexture.yRatio = markerInfoBean.getY_ratio();
            layerTexture.isRepeat = markerInfoBean.getRepeat() == 1;
            layerTexture.isGenMipmaps = markerInfoBean.getGen_mipmaps() == 1;
            layerTexture.isPreMulAlpha = true;//纹理是否预乘透明通道,1：预乘；0：未预乘  bitmap Image are loaded with the {@link Bitmap.Config#ARGB_8888} config by default
        } else {
            layerTexture.anchorType = LayerIconAnchor.LayerIconAnchorCenter;
            layerTexture.isRepeat = false;
            layerTexture.xRatio = 0;
            layerTexture.yRatio = 0;
            layerTexture.isGenMipmaps = false;
            layerTexture.isPreMulAlpha = true;
        }
    }


    @Override
    public int get3DModelId(BaseLayer baseLayer, LayerItem layerItem, String s) {
        return 0;
    }

    @Override
    public String getLayerStyle(BaseLayer baseLayer, LayerItem layerItem, boolean b) {
        int itemType = layerItem.getItemType();
        int businessType = layerItem.getBusinessType();
        String strStyleJson = null;

        if (itemType == LayerItemType.LayerItemPointType) {
            switch (businessType) {
                case ElectricBusinessTypePoint.CHARGE_STATION:
                    ChargeStationLayerItem chargeStationLayerItem = (ChargeStationLayerItem) layerItem;
                    String chargeStationName = chargeStationLayerItem.getStationInfo().name;
                    if (ResUtil.getString(com.autosdk.R.string.navi_gas_preference_telai).equals(chargeStationName)) {
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_charge_station_telaidian_day");
                    } else if (ResUtil.getString(com.autosdk.R.string.navi_gas_preference_sgcc).equals(chargeStationName)) {
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_charge_station_guojiadianwang_day");
                    } else if (ResUtil.getString(com.autosdk.R.string.navi_gas_preference_xingxing).equals(chargeStationName)) {
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_charge_station_xingxing_day");
                    } else if (ResUtil.getString(com.autosdk.R.string.navi_gas_preference_putian).equals(chargeStationName)) {
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_charge_station_putian_day");
                    } else {
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_charge_station_define_day");
                    }
                    break;
                case ElectricBusinessTypePoint.CHARGE_STATION_DETAIL:
                    strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_charge_station_detail");
                    break;
                default:
                    break;
            }
        }

        return strStyleJson;
    }

    @Override
    public boolean getRouteLayerStyle(BaseLayer baseLayer, LayerItem layerItem, RouteLayerStyle routeLayerStyle) {
        return false;
    }

    @Override
    public boolean isRouteCacheStyleEnabled() {
        return false;
    }

    @Override
    public boolean isRouteStyleNightMode() {
        return false;
    }

    @Override
    public void clearLayerItems(BaseLayer baseLayer) {

    }

    @Override
    public void clearLayerItem(BaseLayer baseLayer, LayerItem layerItem) {

    }

    @Override
    public void clearLayerStyle(BaseLayer baseLayer, LayerItem layerItem) {

    }

    @Override
    public String getCommonInfo(String s) {
        return null;
    }

    @Override
    public boolean switchStyle(int i) {
        return false;
    }
}
