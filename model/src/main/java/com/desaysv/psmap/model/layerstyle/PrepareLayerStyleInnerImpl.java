package com.desaysv.psmap.model.layerstyle;

import static com.autonavi.gbl.map.layer.model.LayerIconType.LayerIconTypeBMP;

import android.annotation.SuppressLint;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import com.autonavi.auto.skin.NightModeGlobal;
import com.autonavi.gbl.layer.CustomPointLayerItem;
import com.autonavi.gbl.layer.GuideLabelLayerItem;
import com.autonavi.gbl.layer.model.BizAGroupType;
import com.autonavi.gbl.layer.model.BizCarType;
import com.autonavi.gbl.layer.model.BizCustomTypePlane;
import com.autonavi.gbl.layer.model.BizCustomTypePoint;
import com.autonavi.gbl.layer.model.BizRoadCrossType;
import com.autonavi.gbl.layer.model.BizRouteType;
import com.autonavi.gbl.layer.model.BizSearchType;
import com.autonavi.gbl.layer.model.BizUserType;
import com.autonavi.gbl.layer.model.InnerStyleParam;
import com.autonavi.gbl.layer.observer.PrepareLayerStyleInner;
import com.autonavi.gbl.map.MapView;
import com.autonavi.gbl.map.layer.BaseLayer;
import com.autonavi.gbl.map.layer.LayerItem;
import com.autonavi.gbl.map.layer.PointLayerItem;
import com.autonavi.gbl.map.layer.RouteLayerItem;
import com.autonavi.gbl.map.layer.model.ItemStyleInfo;
import com.autonavi.gbl.map.layer.model.LayerIconAnchor;
import com.autonavi.gbl.map.layer.model.LayerItemType;
import com.autonavi.gbl.map.layer.model.LayerTexture;
import com.autonavi.gbl.map.layer.model.RouteLayerDrawParam;
import com.autonavi.gbl.map.layer.model.RouteLayerParam;
import com.autonavi.gbl.map.layer.model.RouteLayerStyle;
import com.autonavi.gbl.map.layer.model.RouteLayerStyleType;
import com.autonavi.gbl.util.model.BinaryStream;
import com.autosdk.bussiness.common.utils.AssetUtils;
import com.autosdk.bussiness.map.SurfaceViewID;
import com.autosdk.common.utils.CommonUtil;
import com.autosdk.common.utils.StringUtils;
import com.desaysv.psmap.base.auto.layerstyle.bean.MarkerInfoBean;
import com.desaysv.psmap.base.auto.layerstyle.bean.RasterImageBean;
import com.desaysv.psmap.base.auto.layerstyle.bean.VectorCrossBean;
import com.desaysv.psmap.base.auto.layerstyle.utils.StyleJsonAnalysisUtil;
import com.desaysv.psmap.base.component.UserComponent;
import com.desaysv.psmap.base.utils.EnlargeInfo;
import com.google.gson.Gson;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * style文件可参考LayerAsset目录下.json文件
 * 自定义样式纹理可参看
 * {@link PrepareLayerStyleImpl}
 */
public class PrepareLayerStyleInnerImpl extends PrepareLayerStyleInner {
    private Application mApplication;
    private StyleJsonAnalysisUtil mStyleJsonAnalysisUtil;
    private static final int DEFAULT_ERR_NUM = -9527;
    private Long mGroupLayerId = -1L;
    private int mDynamicMarkerId = 0;
    private final Map<String, Integer> mGroupDynamicIds = new HashMap<>(4);
    private final Map<Integer, ArrayList<DynamicItemsId>> mLayerDynamicIds = new HashMap<>();
    private final List<String> mImageNameList = new ArrayList<>();

    private PrepareLayerStyleImpl myPrepareLayerStyle;

    protected PrepareLayerStyleInnerImpl(long cPtr, boolean cMemoryOwn) {
        super(cPtr, cMemoryOwn);
    }

    private int mSurfaceViewId = SurfaceViewID.SURFACE_VIEW_ID_MAIN;

    public PrepareLayerStyleInnerImpl(Application application, MapView mapView, InnerStyleParam param) {
        super(mapView, new PrepareLayerParamInnerImpl(mapView.getEngineId(), SurfaceViewID.transform2SurfaceViewID(mapView.getEngineId())), param);
        mApplication = application;
        byte[] assetFileContent = AssetUtils.getAssetFileContent(application.getApplicationContext(), "hmi_style_1.json");
        mStyleJsonAnalysisUtil = new StyleJsonAnalysisUtil(new String(assetFileContent));
        myPrepareLayerStyle = new PrepareLayerStyleImpl(application, SurfaceViewID.transform2SurfaceViewID(mapView.getEngineId()));
        myPrepareLayerStyle.setNightMode(NightModeGlobal.isNightMode());
        mSurfaceViewId = SurfaceViewID.transform2SurfaceViewID(mapView.getEngineId());
    }

    public PrepareLayerStyleInnerImpl(com.autonavi.gbl.layer.observer.impl.PrepareLayerStyleInnerImpl service) {
        super(service);
    }

    class DynamicItemsId {
        DynamicItemsId(String itemId, int dynamicId) {
            this.itemId = itemId;
            this.dynamicId = dynamicId;
        }

        String itemId;
        int dynamicId;
    }

    public int getDynamicMarkerId() {
        if (mDynamicMarkerId >= 0x60000) {
            mDynamicMarkerId = 0;
        }

        return mDynamicMarkerId++;
    }

    /**
     * @return markrid
     * @brief 获取静态markrid
     */
    public int getStaticMarkerId(int index) {
        return 0x60000 + index;
    }

    @Override
    public int getMarkerId(BaseLayer pLayer, LayerItem pItem, ItemStyleInfo itemStyleInfo) {
        int markerId = -1;
        if (pItem == null || pLayer == null || itemStyleInfo == null) {
            return markerId;
        }
        int itemType = pItem.getItemType();
        int businessType = pItem.getBusinessType();
        if (isCustomizationLayerItem(businessType, itemType)) {
            return myPrepareLayerStyle.getMarkerId(pLayer, pItem, itemStyleInfo);
        }
        itemStyleInfo.markerId = itemStyleInfo.markerId.trim();
        if (businessType == BizAGroupType.BizAGroupTypeAGroup || businessType == BizCustomTypePoint.BizCustomTypePoint3) {
            if (!StringUtils.isInteger(itemStyleInfo.markerId)) {
                int dynamicMarderId = isDynamicMarker(pLayer, pItem, itemStyleInfo.markerId, itemStyleInfo.markerInfo);
                if (dynamicMarderId >= 0) {
                    return dynamicMarderId;
                }
            }
        } else if (businessType == BizAGroupType.BizAGroupTypeEndPoint) {
            return getEndViaPointMarkerId(pLayer, pItem, itemStyleInfo);
        } else if (businessType == BizRouteType.BizRouteTypeViaPoint) {
            /*if (MultiDisplayManager.isMultiSDK()) {
                //多屏一致性包，则途经点图层走自定义纹理
                return getEndViaPointMarkerId(pLayer, pItem, itemStyleInfo);
            }*/
        }

        if (mSurfaceViewId == SurfaceViewID.SURFACE_VIEW_ID_EX1) {
            switch (itemType) {
                case LayerItemType.LayerItemVectorCrossType://矢量路口
                    if (businessType == BizRoadCrossType.BizRoadCrossTypeVector) {
                        return myPrepareLayerStyle.getMarkerId(pLayer, pItem, itemStyleInfo);
                    }
                    break;
                case LayerItemType.LayerItemRasterImageType:
                    if (businessType == BizRoadCrossType.BizRoadCrossTypeRasterImage) {//栅格图图层
                        return myPrepareLayerStyle.getMarkerId(pLayer, pItem, itemStyleInfo);
                    }
                    break;
                default:
                    break;
            }
        }

//        Timber.d("getMarkerId markerId = " + markerId);
        return super.getMarkerId(pLayer, pItem, itemStyleInfo);
    }


    @Override
    public int get3DModelId(BaseLayer pLayer, LayerItem item, String str3DModelId) {
        if (pLayer == null || item == null || str3DModelId == null) {
            return -1;
        }
        if (isCustomizationLayerItem(item.getBusinessType(), item.getItemType())) {
            return myPrepareLayerStyle.get3DModelId(pLayer, item, str3DModelId);
        }
        return super.get3DModelId(pLayer, item, str3DModelId);
    }

    @Override
    public String getLayerStyle(BaseLayer pLayer, LayerItem layerItem, boolean forJava) {
        if (pLayer == null || layerItem == null) {
            return "EMPTY";
        }
        int itemType = layerItem.getItemType();
        int businessType = layerItem.getBusinessType();
        if (isCustomizationLayerItem(businessType, itemType)) {
            return myPrepareLayerStyle.getLayerStyle(pLayer, layerItem, forJava);
        }
        String strStyleJson = null;
        switch (itemType) {
            case LayerItemType.LayerItemVectorCrossType://矢量路口
                if (businessType == BizRoadCrossType.BizRoadCrossTypeVector) {//根据屏幕适配路口大图宽高
                    VectorCrossBean vectorCrossBean = mStyleJsonAnalysisUtil.getVectorCrossBeanFromJson(mStyleJsonAnalysisUtil.getStyleBeanJson("vector_cross_style"));
                    if (vectorCrossBean != null) {
                        VectorCrossBean.VectorCrossLayerStyleBean.VectorCrossAttrBean.RectBean rectBean = vectorCrossBean.getVector_cross_layer_style().getVector_cross_attr().getRect();
                        Rect rect = mSurfaceViewId == SurfaceViewID.SURFACE_VIEW_ID_EX1 ? EnlargeInfo.getInstance().getEx1Rect() :
                                EnlargeInfo.getInstance().getRect();
                        rectBean.setX_min(rect.left);
                        rectBean.setY_min(rect.top);
                        rectBean.setX_max(rect.right);
                        rectBean.setY_max(rect.bottom);
                        if (mSurfaceViewId == SurfaceViewID.SURFACE_VIEW_ID_EX1)
                            vectorCrossBean.getVector_cross_layer_style().getVector_cross_marker().setBg_marker_id("global_image_cross_background_day");
                        Gson gson = new Gson();
                        strStyleJson = gson.toJson(vectorCrossBean);
                        Timber.d("LayerItemVectorCrossType  rect: " + rect + "strStyleJson = " + strStyleJson);
                    } else {
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("vector_cross_style");
                    }
                }
                break;
            case LayerItemType.LayerItemRasterImageType:
                if (businessType == BizRoadCrossType.BizRoadCrossTypeRasterImage) {//栅格图图层
                    //根据屏幕适配栅格大图宽高
                    RasterImageBean rasterImageBean = mStyleJsonAnalysisUtil.getRasterImageBeanFromJson(mStyleJsonAnalysisUtil.getStyleBeanJson("raster_image_style"));
                    if (rasterImageBean != null) {
                        RasterImageBean.RasterImageLayerItemStyleBean rasterImageLayerItemStyleBean = rasterImageBean.getRaster_image_layer_item_style();
                        Rect rect = mSurfaceViewId == SurfaceViewID.SURFACE_VIEW_ID_EX1 ? EnlargeInfo.getInstance().getEx1Rect() :
                                EnlargeInfo.getInstance().getRect();
                        rasterImageLayerItemStyleBean.setWinx(rect.left);
                        rasterImageLayerItemStyleBean.setWiny(rect.top);
                        rasterImageLayerItemStyleBean.setWidth(rect.width());
                        rasterImageLayerItemStyleBean.setHeight(rect.height());
                        if (mSurfaceViewId == SurfaceViewID.SURFACE_VIEW_ID_EX1)
                            rasterImageBean.getRaster_image_layer_item_style().setBg_marker_id("global_image_cross_background_day");
                        Gson gson = new Gson();
                        strStyleJson = gson.toJson(rasterImageBean);
                        Timber.d("LayerItemRasterImageType  rect: %s", rect);
                    } else {
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("raster_image_style");
                    }
                }
                break;
            default:
                break;
        }
        if (strStyleJson != null) {
            return strStyleJson;
        }
        return super.getLayerStyle(pLayer, layerItem, forJava);
    }

    @Override
    public com.autonavi.gbl.layer.observer.impl.PrepareLayerStyleInnerImpl getService() {
        return super.getService();
    }

    @Override
    public boolean getRouteLayerStyle(BaseLayer layer, LayerItem item, RouteLayerStyle style) {
        super.getRouteLayerStyle(layer, item, style);
        RouteLayerItem routeLayerItem = (RouteLayerItem) item;
        RouteLayerDrawParam routeDrawParam = routeLayerItem.getRouteDrawParam();
        // 是否为小地图
        boolean isEaglLine = false;
        int type = routeDrawParam.mRouteStyleType;
        if (type == RouteLayerStyleType.EaglEye_Normal
                || type == RouteLayerStyleType.EaglEye_HightLight
                || type == RouteLayerStyleType.EaglEye_Offline
                || type == RouteLayerStyleType.EaglEye_Offline_HightLight) {
            isEaglLine = true;
        }

        // 是否为离线style
        boolean isOfflineStyle = false;
        if (type == RouteLayerStyleType.Main_Offline || type == RouteLayerStyleType.Main_Offline_HightLight
                || type == RouteLayerStyleType.EaglEye_Offline
                || type == RouteLayerStyleType.EaglEye_Offline_HightLight) {
            isOfflineStyle = true;
        }
        Timber.i("isOfflineStyle %s", isOfflineStyle);
        // 非高亮路线(包括动态导航的更优路线，和备选路线）
        boolean isHighLight = true;
        //是否为备选路线;
        boolean isAlternative = false;
        if (type == RouteLayerStyleType.Main_Normal || type == RouteLayerStyleType.EaglEye_Normal ||
                type == RouteLayerStyleType.Main_Offline || type == RouteLayerStyleType.EaglEye_Offline) {
            isHighLight = false;
            isAlternative = true;
        }

        //控制中控屏路线大小
        if (mSurfaceViewId == SurfaceViewID.SURFACE_VIEW_ID_MAIN) {
            if (style.vecParam != null && !style.vecParam.isEmpty()) {
                for (RouteLayerParam overlayParam : style.vecParam) {
                    if (!isEaglLine) {
                        overlayParam.lineWidth = 40;
                        overlayParam.borderLineWidth = 34;
                    }
                }

            }
        } else if (mSurfaceViewId == SurfaceViewID.SURFACE_VIEW_ID_EX1) {
            for (RouteLayerParam overlayParam : style.vecParam) {
                if (!isEaglLine) {
                    overlayParam.lineWidth = 24;
                    overlayParam.borderLineWidth = 22;
                }
            }
        }
        return true;
    }

    @SuppressLint("WrongConstant")
    public synchronized int isDynamicMarker(BaseLayer pLayer, LayerItem layerItem, String strMarkerId, String strMarkerInfo) {
        int dynamicId = -1;
        if (pLayer == null || layerItem == null || strMarkerId == null || strMarkerId.isEmpty()) {
            return dynamicId;
        }

        switch (layerItem.getBusinessType()) {
            case BizAGroupType.BizAGroupTypeAGroup:
                if (layerItem instanceof PointLayerItem) {
                    PointLayerItem conLayerItem = (PointLayerItem) layerItem;
                    mGroupLayerId = pLayer.getLayerID();
                    if ("id_dynamic".equals(strMarkerId)) {
                        dynamicId = DynamicStyleUtil.addGroupMarker(mApplication, pLayer, conLayerItem.getID(), strMarkerInfo, mGroupDynamicIds, mDynamicMarkerId, mStyleJsonAnalysisUtil);
                    } else if ("id_dynamic_focus".equals(strMarkerId)) {
                        dynamicId = DynamicStyleUtil.addGroupFocusMarker(mApplication, pLayer, conLayerItem.getID(), strMarkerInfo, mGroupDynamicIds, mDynamicMarkerId, mStyleJsonAnalysisUtil);
                    }
                    mDynamicMarkerId = dynamicId;
                }
                break;
            //目前默认纹理副屏不支持多备选图层 暂时先使用自定义的方式实现
           /* case BizRouteType.BizRouteTypeGuideLabel:
                GuideLabelLayerItem labelItem = (GuideLabelLayerItem) layerItem;
                dynamicId = addGuideLabelMarker(pLayer,labelItem, strMarkerId, strMarkerInfo);
                break;*/
            case BizCustomTypePoint.BizCustomTypePoint3:
                //if (layerItem instanceof QuadrantLayerItem) {
                CustomPointLayerItem quadrantLayerItem = (CustomPointLayerItem) layerItem;
                dynamicId = addParkMarker(pLayer, quadrantLayerItem, strMarkerId, strMarkerInfo);
                //}
                break;
            default:
                break;
        }

        addDynamicIds(pLayer, layerItem, dynamicId, mLayerDynamicIds);

        return dynamicId;
    }

    private void addDynamicIds(BaseLayer pLayer, LayerItem layerItem, int dynamicId, Map<Integer, ArrayList<DynamicItemsId>> layerDynamicIds) {
        if (dynamicId != -1) {
            Integer layerId = Integer.valueOf((int) pLayer.getLayerID());
            ArrayList<DynamicItemsId> dynamicIds = new ArrayList<>();
            if (layerDynamicIds.containsKey(layerId)) {
                dynamicIds = layerDynamicIds.get(layerId);
            }

            DynamicItemsId itemsId = new DynamicItemsId(layerItem.getID(), dynamicId);
            dynamicIds.add(itemsId);

            layerDynamicIds.put(layerId, dynamicIds);
        }
    }

    /**
     * @return markerID
     * @brief 对于不存在的纹理通过添加并生成id
     * @param[in] pLayer         图元所在图层
     * @param[in] pItem          需要更新样式的具体图元，通过GetBusinessType返回值判断具体图层
     * @param[in] imageName
     * @param[in] strMarkerInfo  marker对应纹理的数据参数配置名
     * @note
     */
    public synchronized int addStaticMarker(BaseLayer pLayer, LayerItem pItem, String imageName, String strMarkerInfo) {
        int markerId = -1;
        if (pLayer == null || imageName == null || null == strMarkerInfo || imageName.isEmpty()) {
            return markerId;
        }

        Timber.d("AddStaticMarker 创建纹理: strMarkerInfo = " + strMarkerInfo + " imageName = " + imageName);

        if (strMarkerInfo.isEmpty()) {
            strMarkerInfo = imageName;
        }
        LayerTexture layerTexture = new LayerTexture();
        layerTexture.name = imageName;
        setLayerTexture(strMarkerInfo, layerTexture);//设置锚点
        Timber.d("AddStaticMarker: markerInfoBean为null：imageName = " + imageName);

        int resID = mApplication.getResources().getIdentifier(imageName, "drawable"
                , mApplication.getApplicationInfo().packageName);
        if (resID == 0) {
            return markerId;
        }
        Bitmap bitmap = null;
        bitmap = BitmapFactory.decodeResource(mApplication.getResources(), resID);
        if (bitmap == null) {
            Timber.d(pLayer.getName() + ",资源图片缺失，创建新图片纹理" + imageName + "失败");
            return markerId;
        }

        ByteBuffer dataBuffer = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(dataBuffer);
        layerTexture.dataBuff = new BinaryStream(dataBuffer.array());
        layerTexture.width = bitmap.getWidth();
        layerTexture.height = bitmap.getHeight();
        layerTexture.iconType = LayerIconTypeBMP;
        markerId = getStaticMarkerId(mImageNameList.size());
        layerTexture.resID = markerId;

        boolean isAddSuccess = pLayer.getMapView().addLayerTexture(layerTexture);
        mImageNameList.add(imageName);
        Timber.d("AddStaticMarker: 创建纹理 = %s, isAddSuccess = %s, width = %s, height = %s",
                imageName, isAddSuccess, layerTexture.width, layerTexture.height);

        return markerId;
    }

    /**
     * 设置锚点
     *
     * @param strMarkerInfo
     * @param layerTexture
     */
    @SuppressLint("WrongConstant")
    private void setLayerTexture(String strMarkerInfo, LayerTexture layerTexture) {
        MarkerInfoBean markerInfoBean = mStyleJsonAnalysisUtil.getMarkerInfoFromJson(strMarkerInfo);
        Timber.d("====setLayerTexture strMarkerInfo = %s, markerInfoBean = %s", strMarkerInfo, markerInfoBean);
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

    /**
     * 合成导航多备选标签路牌view
     *
     * @param pLayer
     * @param strMarkerId
     * @param strMarkerInfo
     * @return
     */
    public synchronized int addGuideLabelMarker(BaseLayer pLayer, GuideLabelLayerItem item, String strMarkerId, String strMarkerInfo) {
        int dynamicId = -1;
        if (pLayer == null || strMarkerId == null || strMarkerId.isEmpty()) {
            return dynamicId;
        }
        int resID = mApplication.getResources().getIdentifier(strMarkerId, "drawable"
                , mApplication.getApplicationInfo().packageName);
        Bitmap bitmap = BitmapFactory.decodeResource(mApplication.getResources(), resID);
        if (bitmap == null) {
            Timber.e(new NullPointerException(), "创建新图片纹理" + strMarkerId + "失败");
            return dynamicId;
        }
        String time;
        int travelTimeDiff = item.getMTravelTimeDiff();
        if (travelTimeDiff > 0) {
            time = "慢";
        } else {
            time = "快";
        }
        time += CommonUtil.switchFromSecond(Math.abs(travelTimeDiff));

        String distance;
        int distanceDiff = item.getMDistanceDiff();
        if (distanceDiff > 0) {
            distance = "多";
        } else {
            distance = "少";
        }
        distance += CommonUtil.distanceUnitTransform(Math.abs(distanceDiff));

        String trafficLight;
        int trafficLightDiff = item.getMTrafficLightDiff();
        if (trafficLightDiff > 0) {
            trafficLight = "多%d个";
        } else {
            trafficLight = "少%d个";
        }
        trafficLight = String.format(trafficLight, Math.abs(trafficLightDiff));

        String cost;
        int costDiff = item.getMCostDiff();
        if (costDiff > 0) {
            cost = "多%d元";
        } else {
            cost = "少%d元";
        }

        // 导航多备选标签布局
        /*todo 缺少资源从demo找、
        View view = View.inflate(mApplication, R.layout.layout_guide_label, null);
        view.setBackgroundResource(resID);
        SkinTextView labelText=view.findViewById(R.id.label_text);
        if (TextUtils.isEmpty(item.getMLabelContent())){
            labelText.setVisibility(View.GONE);
        }else{
            labelText.setText(item.getMLabelContent());
        }

        SkinTextView timeDiffText=view.findViewById(R.id.time_diff_text);
        timeDiffText.setText(time);
        SkinTextView costText=view.findViewById(R.id.cost_text);
        if (item.getMPathCost()<=0){
            view.findViewById(R.id.cost_image).setVisibility(View.GONE);
            costText.setVisibility(View.GONE);

        }else{
            costText.setText(item.getMPathCost() + "元");
        }

        SkinTextView distanceDiffText=view.findViewById(R.id.distance_diff_text);
        distanceDiffText.setText(distance);
        SkinTextView trafficLightText=view.findViewById(R.id.traffic_light_text);
        trafficLightText.setText(trafficLight);

        Bitmap dynamicBitmap = BitmapUtils.createBitmapFromView(view);
        ByteBuffer dataBuffer = ByteBuffer.allocate(dynamicBitmap.getByteCount());
        dynamicBitmap.copyPixelsToBuffer(dataBuffer);
        LayerTexture layerTexture = new LayerTexture();
        layerTexture.dataBuff = new BinaryStream(dataBuffer.array());
        layerTexture.width = dynamicBitmap.getWidth();
        layerTexture.height = dynamicBitmap.getHeight();
        layerTexture.iconType = LayerIconTypeBMP;
        setLayerTexture(strMarkerInfo, layerTexture);
        dynamicId = getDynamicMarkerId();
        layerTexture.resID = dynamicId;

        boolean isAddSuccess = pLayer.getMapView().addLayerTexture(layerTexture);
        Timber.d("AddDynamicMarder: 创建纹理  isAddSuccess = " + isAddSuccess);*/

        return dynamicId;
    }


    /**
     * 自定义找停车场图层
     *
     * @param pLayer
     * @param strMarkerId
     * @param strMarkerInfo
     * @return
     */
    public synchronized int addParkMarker(BaseLayer pLayer, CustomPointLayerItem item, String strMarkerId, String strMarkerInfo) {
        int dynamicId = -1;
        if (pLayer == null || strMarkerId == null || strMarkerId.isEmpty()) {
            return dynamicId;
        }
        /*todo 缺少资源从demo找、
        View view = View.inflate(mApplication, R.layout.layout_park_label, null);
        Bitmap dynamicBitmap = BitmapUtils.createBitmapFromView(view);
        ByteBuffer dataBuffer = ByteBuffer.allocate(dynamicBitmap.getByteCount());
        dynamicBitmap.copyPixelsToBuffer(dataBuffer);
        LayerTexture layerTexture = new LayerTexture();
        layerTexture.dataBuff = new BinaryStream(dataBuffer.array());
        layerTexture.width = dynamicBitmap.getWidth();
        layerTexture.height = dynamicBitmap.getHeight();
        layerTexture.iconType = LayerIconTypeBMP;
        layerTexture.anchorType = LayerIconAnchor.LayerIconAnchorCenterRight;
        layerTexture.isRepeat = false;
        layerTexture.xRatio = 0;
        layerTexture.yRatio =0;
        layerTexture.isGenMipmaps = false;
        layerTexture.isPreMulAlpha = true;
        dynamicId = getDynamicMarkerId();
        layerTexture.resID = dynamicId;
        boolean isAddSuccess = pLayer.getMapView().addLayerTexture(layerTexture);
        Timber.d("AddDynamicMarder: 创建纹理  isAddSuccess = " + isAddSuccess);*/

        return dynamicId;
    }

    /**
     * <
     *
     * @brief 清除图层所有item时，通知HMI的接口，上层根据该接口删除动态纹理或者静态纹理
     * @param[in] pLayer        图层对象
     * @note thread：main       HMI或BL内部调用ClearAllItems时触发该回调
     */
    @Override
    public synchronized void clearLayerItems(BaseLayer pLayer) {
        if (pLayer == null) {
            return;
        }
        if (mGroupLayerId == pLayer.getLayerID()) {
            UserComponent.Companion.getInstance().clearGroupBitmap();
            mGroupDynamicIds.clear();
        }

        Integer layerId = Integer.valueOf((int) pLayer.getLayerID());
        if (mLayerDynamicIds.containsKey(layerId)) {
            ArrayList<DynamicItemsId> dynamicIds = mLayerDynamicIds.get(layerId);

            for (int i = 0; i < dynamicIds.size(); i++) {
                DynamicItemsId itemsId = dynamicIds.get(i);
                pLayer.getMapView().destroyTexture(itemsId.dynamicId);
            }

            mLayerDynamicIds.remove(layerId);
        }
        super.clearLayerItems(pLayer);
    }

    private int getEndViaPointMarkerId(BaseLayer pLayer, LayerItem pItem, ItemStyleInfo itemStyleInfo) {
        int markerId = -1;
        int index = mImageNameList.indexOf(itemStyleInfo.markerId);
        if (index >= 0) {
            markerId = getStaticMarkerId(index);
//            Timber.d("getMarkerId markerId = " + markerId);
            return markerId;
        }
        markerId = addStaticMarker(pLayer, pItem, itemStyleInfo.markerId, itemStyleInfo.markerInfo);
        return markerId;
    }

    /**
     * 是否是定制化图层
     *
     * @param businessType  业务类型
     * @param layerItemType 具体图元类型
     * @return 是否自定义实现
     */
    private boolean isCustomizationLayerItem(int businessType, int layerItemType) {
        boolean isCustomization = false;
        if (mCustomLayers.size() == 0) {
            addCustomLayers();
        }

        //搜索图层
        if (businessType >= BizSearchType.BizSearchTypeLine && businessType <= BizSearchType.BizSearchTypeMax) {
            isCustomization = true;
        }

        //自定义图层
        if (businessType >= BizCustomTypePoint.BizCustomTypePointInvalid && businessType <= BizCustomTypePlane.BizCustomTypePlaneMax) {
            isCustomization = true;
        }
        //用户图层
        if (businessType >= BizUserType.BizUserTypeInvalid && businessType <= BizUserType.BizUserTypeMax) {
            isCustomization = true;
        }

        //组队图层
        if (businessType >= BizAGroupType.BizAGroupTypeInvalid && businessType <= BizAGroupType.BizAGroupTypeMax) {
            isCustomization = true;
        }

        /*
        //路线红绿灯
        if (businessType == BizRouteType.BizRouteTypeTrafficLight && layerItemType == LayerItemType.LayerItemPointType) {
            isCustomization = true;
        }*/

        //车标图层
        if (businessType >= BizCarType.BizCarTypeInvalid && businessType <= BizCarType.BizCarTypeMax) {
            isCustomization = true;
        }

        //起点终点图层
        if (businessType >= BizRouteType.BizRouteTypeStartPoint && businessType <= BizRouteType.BizRouteTypeViaPoint) {
            isCustomization = true;
        }

         /*//路线红绿灯
        if (businessType == BizRouteType.BizRouteTypeTrafficLight && layerItemType == LayerItemType.LayerItemPointType) {
            isCustomization = true;
        }*/

        if (mCustomLayers.contains(businessType)) {
            Timber.i(" mCustomLayers %s", businessType);
            return true;
        }

        return isCustomization;
    }

    private final ArrayList<Integer> mCustomLayers = new ArrayList<>();

    private void addCustomLayers() {
    /*    mCustomLayers.add(BizRouteType.BizRouteTypeJamPoint);
        mCustomLayers.add(BizRouteType.BizRouteTypeTrafficBlock);
        mCustomLayers.add(BizRouteType.BizRouteTypeTrafficBlockOuter);
        mCustomLayers.add(BizRoadFacilityType.BizRoadFacilityTypeCruiseFacility);
        mCustomLayers.add(BizRoadFacilityType.BizRoadFacilityTypeGuideFacility);
        mCustomLayers.add(BizRoadFacilityType.BizRoadFacilityTypeGuideTrafficEvent);
        mCustomLayers.add(BizRouteType.BizRouteTypeGuideEtaEvent);
        mCustomLayers.add(BizRouteType.BizRouteTypeEndPoint);
        mCustomLayers.add(BizRouteType.BizRouteTypeGuideLabel);
        mCustomLayers.add(BizRouteType.BizRouteTypeViaRoad);*/
        mCustomLayers.add(BizRouteType.BizRouteTypeRestArea);
        mCustomLayers.add(BizRouteType.BizRouteTypeWeather);
    }

}
