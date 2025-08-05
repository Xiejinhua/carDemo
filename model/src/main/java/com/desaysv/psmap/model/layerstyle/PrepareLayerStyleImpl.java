package com.desaysv.psmap.model.layerstyle;

import static com.autonavi.gbl.map.layer.model.LayerIconType.LayerIconTypeBMP;
import static com.autosdk.bussiness.common.utils.FileUtils.getFileStringFromAssets;
import static com.autosdk.bussiness.layer.MapLayer.FLYLINE_SCENE_TYPE_RESULT_DETAIL;
import static com.autosdk.bussiness.layer.MapLayer.FLYLINE_SCENE_TYPE_RESULT_LIST;
import static com.autosdk.bussiness.layer.MapLayer.FLYLINE_SCENE_TYPE_SELECT_POI;

import android.annotation.SuppressLint;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.autonavi.auto.skin.NightModeGlobal;
import com.autonavi.auto.skin.view.SkinConstraintLayout;
import com.autonavi.auto.skin.view.SkinImageView;
import com.autonavi.auto.skin.view.SkinTextView;
import com.autonavi.gbl.common.model.Coord3DDouble;
import com.autonavi.gbl.common.path.model.CameraType;
import com.autonavi.gbl.common.path.model.FacilityType;
import com.autonavi.gbl.common.path.model.SubCameraExtType;
import com.autonavi.gbl.common.path.option.RouteType;
import com.autonavi.gbl.guide.model.CruiseFacilityType;
import com.autonavi.gbl.layer.BizControlService;
import com.autonavi.gbl.layer.CruiseCongestionItem;
import com.autonavi.gbl.layer.CruiseFacilityLayerItem;
import com.autonavi.gbl.layer.CustomPointLayerItem;
import com.autonavi.gbl.layer.EndAreaParentLayerItem;
import com.autonavi.gbl.layer.EndAreaPointLayerItem;
import com.autonavi.gbl.layer.FavoritePointLayerItem;
import com.autonavi.gbl.layer.GpsTrackPointLayerItem;
import com.autonavi.gbl.layer.GuideCameraLayerItem;
import com.autonavi.gbl.layer.GuideCongestionLayerItem;
import com.autonavi.gbl.layer.GuideETAEventLayerItem;
import com.autonavi.gbl.layer.GuideFacilityLayerItem;
import com.autonavi.gbl.layer.GuideLabelLayerItem;
import com.autonavi.gbl.layer.GuideMixForkLayerItem;
import com.autonavi.gbl.layer.GuideTrafficEventLayerItem;
import com.autonavi.gbl.layer.PathBoardLayerItem;
import com.autonavi.gbl.layer.RouteBlockLayerItem;
import com.autonavi.gbl.layer.RouteCompareTipsLayerItem;
import com.autonavi.gbl.layer.RoutePathPointItem;
import com.autonavi.gbl.layer.RouteTrafficEventTipsLayerItem;
import com.autonavi.gbl.layer.RouteViaRoadLayerItem;
import com.autonavi.gbl.layer.RouteWeatherLayerItem;
import com.autonavi.gbl.layer.SearchAlongWayLayerItem;
import com.autonavi.gbl.layer.SearchBeginEndLayerItem;
import com.autonavi.gbl.layer.SearchChildLayerItem;
import com.autonavi.gbl.layer.SearchExitEntranceLayerItem;
import com.autonavi.gbl.layer.SearchParentLayerItem;
import com.autonavi.gbl.layer.model.BizAGroupType;
import com.autonavi.gbl.layer.model.BizAreaType;
import com.autonavi.gbl.layer.model.BizCarType;
import com.autonavi.gbl.layer.model.BizCustomTypeArrow;
import com.autonavi.gbl.layer.model.BizCustomTypeCircle;
import com.autonavi.gbl.layer.model.BizCustomTypeLine;
import com.autonavi.gbl.layer.model.BizCustomTypePoint;
import com.autonavi.gbl.layer.model.BizCustomTypePolygon;
import com.autonavi.gbl.layer.model.BizDirectionStyle;
import com.autonavi.gbl.layer.model.BizFlyLineType;
import com.autonavi.gbl.layer.model.BizGpsPointType;
import com.autonavi.gbl.layer.model.BizLabelType;
import com.autonavi.gbl.layer.model.BizRoadCrossType;
import com.autonavi.gbl.layer.model.BizRoadFacilityType;
import com.autonavi.gbl.layer.model.BizRouteType;
import com.autonavi.gbl.layer.model.BizSearchType;
import com.autonavi.gbl.layer.model.BizUserType;
import com.autonavi.gbl.layer.model.FlylineDrawMode;
import com.autonavi.gbl.layer.model.RouteTrafficEventType;
import com.autonavi.gbl.layer.model.SearchAlongwayType;
import com.autonavi.gbl.map.layer.BaseLayer;
import com.autonavi.gbl.map.layer.LayerItem;
import com.autonavi.gbl.map.layer.PointLayerItem;
import com.autonavi.gbl.map.layer.RouteLayerItem;
import com.autonavi.gbl.map.layer.model.CustomTextureParam;
import com.autonavi.gbl.map.layer.model.CustomUpdatePair;
import com.autonavi.gbl.map.layer.model.CustomUpdateParam;
import com.autonavi.gbl.map.layer.model.ItemStyleInfo;
import com.autonavi.gbl.map.layer.model.LayerIconAnchor;
import com.autonavi.gbl.map.layer.model.LayerItemType;
import com.autonavi.gbl.map.layer.model.LayerTexture;
import com.autonavi.gbl.map.layer.model.RouteLayerDrawParam;
import com.autonavi.gbl.map.layer.model.RouteLayerStyle;
import com.autonavi.gbl.map.layer.observer.IPrepareLayerStyle;
import com.autonavi.gbl.pos.model.LocInfo;
import com.autonavi.gbl.route.model.WeatherLabelItem;
import com.autonavi.gbl.user.behavior.model.FavoriteType;
import com.autonavi.gbl.util.model.BinaryStream;
import com.autosdk.bussiness.common.AlongWaySearchPoi;
import com.autosdk.bussiness.common.POI;
import com.autosdk.bussiness.common.utils.FileUtils;
import com.autosdk.bussiness.layer.CardController;
import com.autosdk.bussiness.layer.ElectricBusinessTypePoint;
import com.autosdk.bussiness.layer.LayerController;
import com.autosdk.bussiness.location.LocationController;
import com.autosdk.bussiness.manager.SDKManager;
import com.autosdk.bussiness.map.SurfaceViewID;
import com.autosdk.bussiness.widget.route.utils.AutoRouteUtil;
import com.desaysv.psmap.base.component.UserComponent;
import com.autosdk.common.SdkApplicationUtils;
import com.autosdk.common.utils.BitmapUtils;
import com.autosdk.common.utils.CommonUtil;
import com.desaysv.psmap.base.utils.EnlargeInfo;
import com.autosdk.common.utils.StringUtils;
import com.desaysv.psmap.base.auto.layerstyle.bean.CarTypeBean;
import com.desaysv.psmap.base.auto.layerstyle.bean.MarkerInfoBean;
import com.desaysv.psmap.base.auto.layerstyle.bean.RasterImageBean;
import com.desaysv.psmap.base.auto.layerstyle.bean.VectorCrossBean;
import com.desaysv.psmap.base.auto.layerstyle.utils.StyleJsonAnalysisUtil;
import com.desaysv.psmap.base.utils.BaseConstant;
import com.desaysv.psmap.model.R;
import com.desaysv.psmap.model.layerstyle.parser.CarLayerStyleParser;
import com.desaysv.psmap.model.layerstyle.parser.NaviRouteLayerParser;
import com.google.gson.Gson;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class PrepareLayerStyleImpl implements IPrepareLayerStyle {
    private static final String TAG = "MapPrepareLayerStyle";
    private static final int DEFAULT_ERR_NUM = -9527;

    private Application mApplication;
    @SurfaceViewID.SurfaceViewID1
    int mSurfaceViewID;
    private StyleJsonAnalysisUtil mStyleJsonAnalysisUtil;

    private CarLayerStyleParser mCarLayerStyleParser;

    private NaviRouteLayerParser mNaviRouteLayerParser;

    private boolean mIsNightMode = false;
    private double mScaleFactor = 1.5;
    private int mDynamicMarkerId = 0;
    private Long mGroupLayerId = -1L;
    private final Map<String, Integer> mGroupDynamicIds = new HashMap<>(4);
    private int speedCarDynamicId = -1;//车速车标纹理id
    private int guideCameraDynamicId = -1;//电子眼纹理id
    private final List<String> mImageNameList = new ArrayList<>();
    private final Map<Integer, ArrayList<DynamicItemsId>> mLayerDynamicIds = new HashMap<Integer, ArrayList<DynamicItemsId>>();
    private final Map<Integer, ArrayList<DynamicItemsId>> mCardLayerDynamicIds = new HashMap<Integer, ArrayList<DynamicItemsId>>();
    private int drawableDirectionWidth;//车标光圈宽度
    private final Map<String, String> htmlMap = new HashMap();
    private int markerKey = 0x1234;

    class DynamicItemsId {
        DynamicItemsId(String itemId, int dynamicId) {
            this.itemId = itemId;
            this.dynamicId = dynamicId;
        }

        String itemId;
        int dynamicId;
    }

    public int getFlyLineType() {
        int flyLineType = 0;

        flyLineType = LayerController.getInstance().getMapLayer(mSurfaceViewID).getFlyLineScenceType();

        return flyLineType;
    }

    public PrepareLayerStyleImpl(Application application, @SurfaceViewID.SurfaceViewID1 int surfaceViewID) {
        mApplication = application;
        mSurfaceViewID = surfaceViewID;
        String styleJsonFilePath = getFileStringFromAssets(SdkApplicationUtils.getApplication(), "style.json");
        mStyleJsonAnalysisUtil = new StyleJsonAnalysisUtil(styleJsonFilePath);
        mCarLayerStyleParser = new CarLayerStyleParser();
        mNaviRouteLayerParser = new NaviRouteLayerParser();
        Drawable drawableDirection = mApplication.getResources().getDrawable(R.drawable.global_image_navi_direction_day);
        Drawable drawableWord = mApplication.getResources().getDrawable(R.drawable.global_image_navi_direction_east_day);
        drawableDirectionWidth = drawableDirection.getIntrinsicWidth() / 2 - drawableWord.getIntrinsicWidth() / 2;
    }


    public void init(Application application, BizControlService bizService) {

    }

    public void setScaleFactor(double scaleFactor) {
        this.mScaleFactor = scaleFactor;
    }

    public void setNightMode(boolean nightMode) {
        this.mIsNightMode = nightMode;
    }

    /**
     * @return markrid
     * @brief 获取静态markrid
     */
    public int getStaticMarkerId(int index) {
        return 0x60000 + index;
    }

    public int getDynamicMarkerId() {
        if (mDynamicMarkerId >= 0x60000) {
            mDynamicMarkerId = 0;
        }

        return mDynamicMarkerId++;
    }

    public synchronized int addChargeStationMarker(BaseLayer baseLayer, LayerItem layerItem, String markerId, String markerInfo) {
        int identifier = mApplication.getResources().getIdentifier(markerId, "drawable", mApplication.getPackageName());
        if (identifier == 0) {
            return -1;
        }
        Bitmap bitmap = BitmapFactory.decodeResource(mApplication.getResources(), identifier);
//        int w = bitmap.getWidth();
//        int h = bitmap.getHeight();
//        Matrix matrix = new Matrix();
//        float scale = 0.5f;
//        matrix.postScale(scale, scale);
//        Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
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
        Timber.d("AddStaticMarker imageName = %s, resID = %s", imageName, resID);
        if (resID == 0) {
            return markerId;
        }
        Bitmap bitmap = null;
        if (pItem.getBusinessType() == BizRoadCrossType.BizRoadCrossTypeVector || pItem.getBusinessType() == BizRoadCrossType.BizRoadCrossTypeRasterImage ||
                pItem.getBusinessType() == BizSearchType.BizSearchTypePoiAlongRoute || pItem.getBusinessType() == BizSearchType.BizSearchTypePoiChildPoint ||
                "map_traffic_platenum_restrict_light".equals(imageName) || "map_traffic_platenum_restrict_hl".equals(imageName)
                || pItem.getBusinessType() == BizAreaType.BizAreaTypeEndAreaPolyline) {//路口大图、限行区域、终点区域线不缩放
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;//矢量路口大图及栅格图背景显示原始大小，限行图层图片不缩放,不缩放
            bitmap = BitmapFactory.decodeResource(mApplication.getResources(), resID, options);
        } else {
            bitmap = BitmapFactory.decodeResource(mApplication.getResources(), resID);
        }
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
     * 添加途经点扎点
     *
     * @param pLayer
     * @param layerItem
     * @param strMarkerId
     * @param strMarkerInfo
     * @param isFocus
     * @return
     */
    public synchronized int AddViaDynamicMarker(BaseLayer pLayer, LayerItem layerItem, String strMarkerId, String strMarkerInfo, boolean isFocus) {
        int dynamicId = -1;
        if (pLayer == null || strMarkerId == null || strMarkerId.isEmpty()) {
            return dynamicId;
        }
        Bitmap bitmap = createViaOperationView(layerItem);
        if (bitmap == null) {
            Timber.e("创建新图片纹理" + strMarkerId + "失败 " + new NullPointerException());
            return dynamicId;
        } else {
            Timber.i("%s创建bitmap成功", bitmap.toString());
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap dynamicBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(dynamicBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#ff0000"));
        Rect src = new Rect(0, 0, width, height);
        Rect dst = new Rect(0, 0, dynamicBitmap.getWidth(), dynamicBitmap.getHeight());
        canvas.drawBitmap(bitmap, src, dst, paint);

        ByteBuffer dataBuffer = ByteBuffer.allocate(dynamicBitmap.getByteCount());
        dynamicBitmap.copyPixelsToBuffer(dataBuffer);
        LayerTexture layerTexture = new LayerTexture();
        layerTexture.dataBuff = new BinaryStream(dataBuffer.array());
        layerTexture.width = dynamicBitmap.getWidth();
        layerTexture.height = dynamicBitmap.getHeight();
        layerTexture.iconType = LayerIconTypeBMP;
        layerTexture.anchorType = LayerIconAnchor.LayerIconAnchorRandomPosition;
        layerTexture.xRatio = 1.0f;
        layerTexture.yRatio = 0.5f;
        dynamicId = getDynamicMarkerId();
        layerTexture.resID = dynamicId;

        boolean isAddSuccess = pLayer.getMapView().addLayerTexture(layerTexture);
        Timber.i("AddDynamicMarder: 创建纹理 isAddSuccess = %s", isAddSuccess);

        return dynamicId;
    }

    /**
     * 途经点选点操作
     *
     * @param layerItem
     * @return
     */
    private View mMiddView = null;// 中间点途经点操作view

    private Bitmap createViaOperationView(LayerItem layerItem) {
        CustomPointLayerItem customPointItem = (CustomPointLayerItem) layerItem;
        int leftResId = -1;
        int type = customPointItem.getMType();
        String value = customPointItem.getMValue();
        Timber.i("type:" + type + ",value:" + value);
        boolean isNight = NightModeGlobal.isNightMode();
        if (customPointItem.getMType() == 1) {
            // 增加: type == 1
//            leftResId = isNight ? R.drawable.auto_point_add_night : R.drawable.auto_point_add;
            value = value.length() <= 6 ? value : value.substring(0, 5) + "...";
        } else {
            // 删除：type == 2
//            leftResId = isNight ? R.drawable.auto_point_del_night : R.drawable.auto_point_del;
            //  索引需要注意异常:修正了字符串异常导致索引越界问题
//            value = value.length() <= 1 ? value : value.substring(1, value.length());
        }
        if (mMiddView == null) {
            mMiddView = View.inflate(mApplication, R.layout.global_layout_texture_text_add_del_sel, null);
        }
        mMiddView.findViewById(R.id.ll_bg).setBackgroundResource(isNight ? R.drawable.shape_map_via_icon_day : R.drawable.shape_map_via_icon_night);
        Timber.i("222 type:" + type + ",value:" + value);
        ((TextView) mMiddView.findViewById(R.id.tx_texture_id)).setText(value);
//        ((ImageView) mMiddView.findViewById(R.id.iv_add_del)).setImageResource(leftResId);
        return BitmapUtils.createBitmapFromView(mMiddView);
    }

    /**
     * 路书DAY扎点
     *
     * @param pLayer
     * @param layerItem
     * @param strMarkerId
     * @param strMarkerInfo
     * @param isFocus
     * @return
     */
    public synchronized int AddLineDayDynamicMarker(BaseLayer pLayer, LayerItem layerItem, String strMarkerId) {
        int dynamicId = -1;
        if (pLayer == null || strMarkerId == null || strMarkerId.isEmpty()) {
            return dynamicId;
        }
        Bitmap bitmap = createLineDayOperationView(layerItem);
        if (bitmap == null) {
            Timber.e("创建新图片纹理" + strMarkerId + "失败 " + new NullPointerException());
            return dynamicId;
        } else {
            Timber.i("%s创建bitmap成功", bitmap.toString());
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap dynamicBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(dynamicBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#ff0000"));
        Rect src = new Rect(0, 0, width, height);
        Rect dst = new Rect(0, 0, dynamicBitmap.getWidth(), dynamicBitmap.getHeight());
        canvas.drawBitmap(bitmap, src, dst, paint);

        ByteBuffer dataBuffer = ByteBuffer.allocate(dynamicBitmap.getByteCount());
        dynamicBitmap.copyPixelsToBuffer(dataBuffer);
        LayerTexture layerTexture = new LayerTexture();
        layerTexture.dataBuff = new BinaryStream(dataBuffer.array());
        layerTexture.width = dynamicBitmap.getWidth();
        layerTexture.height = dynamicBitmap.getHeight();
        layerTexture.iconType = LayerIconTypeBMP;
        layerTexture.anchorType = LayerIconAnchor.LayerIconAnchorRandomPosition;
        layerTexture.xRatio = 0.5f;
        layerTexture.yRatio = 0.5f;
        dynamicId = getDynamicMarkerId();
        layerTexture.resID = dynamicId;

        boolean isAddSuccess = pLayer.getMapView().addLayerTexture(layerTexture);
        Timber.i("AddLineDayDynamicMarker: 创建纹理 isAddSuccess = %s", isAddSuccess);

        return dynamicId;
    }

    /**
     * 路书DAY操作
     *
     * @param layerItem
     * @return
     */
    private View mLineDayView = null;// 中间点途经点操作view

    @SuppressLint("SetTextI18n")
    private Bitmap createLineDayOperationView(LayerItem layerItem) {
        CustomPointLayerItem customPointItem = (CustomPointLayerItem) layerItem;
        int type = customPointItem.getMType();
        String value = customPointItem.getMValue();
        Timber.i("type:" + type + ",value:" + value);
        if (mLineDayView == null) {
            mLineDayView = View.inflate(mApplication, R.layout.aha_line_day, null);
        }
        SkinTextView day = mLineDayView.findViewById(R.id.day);
        if (customPointItem.getMType() < 10){
            day.setText("0" + value);
        } else {
            day.setText(value);
        }
        return BitmapUtils.createBitmapFromView(mLineDayView);
    }

    /**
     * 路书DAY-node扎点
     *
     * @param pLayer
     * @param layerItem
     * @param strMarkerId
     * @param strMarkerInfo
     * @param isFocus
     * @return
     */
    public synchronized int AddLineDayNodeDynamicMarker(BaseLayer pLayer, LayerItem layerItem, String strMarkerId) {
        int dynamicId = -1;
        if (pLayer == null || strMarkerId == null || strMarkerId.isEmpty()) {
            return dynamicId;
        }
        Bitmap bitmap = createLineDayNodeOperationView(layerItem);
        if (bitmap == null) {
            Timber.e("创建新图片纹理" + strMarkerId + "失败 " + new NullPointerException());
            return dynamicId;
        } else {
            Timber.i("%s创建bitmap成功", bitmap.toString());
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap dynamicBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(dynamicBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#ff0000"));
        Rect src = new Rect(0, 0, width, height);
        Rect dst = new Rect(0, 0, dynamicBitmap.getWidth(), dynamicBitmap.getHeight());
        canvas.drawBitmap(bitmap, src, dst, paint);

        ByteBuffer dataBuffer = ByteBuffer.allocate(dynamicBitmap.getByteCount());
        dynamicBitmap.copyPixelsToBuffer(dataBuffer);
        LayerTexture layerTexture = new LayerTexture();
        layerTexture.dataBuff = new BinaryStream(dataBuffer.array());
        layerTexture.width = dynamicBitmap.getWidth();
        layerTexture.height = dynamicBitmap.getHeight();
        layerTexture.iconType = LayerIconTypeBMP;
        layerTexture.anchorType = LayerIconAnchor.LayerIconAnchorRandomPosition;
        layerTexture.xRatio = 0.5f;
        layerTexture.yRatio = 1.0f;
        dynamicId = getDynamicMarkerId();
        layerTexture.resID = dynamicId;

        boolean isAddSuccess = pLayer.getMapView().addLayerTexture(layerTexture);
        Timber.i("AddLineDayNodeDynamicMarker: 创建纹理 isAddSuccess = %s", isAddSuccess);

        return dynamicId;
    }

    /**
     * 路书DAY操作
     *
     * @param layerItem
     * @return
     */
    private View mLineDayNodeView = null;// 中间点途经点操作view

    private Bitmap createLineDayNodeOperationView(LayerItem layerItem) {
        CustomPointLayerItem customPointItem = (CustomPointLayerItem) layerItem;
        String value = customPointItem.getMValue();
        Timber.i("value:" + value);
        if (mLineDayNodeView == null) {
            mLineDayNodeView = View.inflate(mApplication, R.layout.layout_aha_secenic, null);
        }
        SkinConstraintLayout top = mLineDayNodeView.findViewById(R.id.top);
        SkinTextView name = mLineDayNodeView.findViewById(R.id.name);
        SkinImageView day = mLineDayNodeView.findViewById(R.id.day);
        name.setText(value);
        if (NightModeGlobal.isNightMode()){
            top.setBackgroundResource(R.drawable.ic_aha_scenic_name_bg_night);
            name.setTextColor(name.getResources().getColor(R.color.onPrimaryNight));
            day.setBackgroundResource(R.drawable.ic_aha_scenic_bg_night);
        } else {
            top.setBackgroundResource(R.drawable.ic_aha_scenic_name_bg_day);
            name.setTextColor(name.getResources().getColor(R.color.customAhaSecenicPointTextDay));
            day.setBackgroundResource(R.drawable.ic_aha_scenic_bg_day);
        }
        return BitmapUtils.createBitmapFromView(mLineDayNodeView);
    }

    /**
     * 捷途探趣POI扎点
     *
     * @param pLayer
     * @param layerItem
     * @param strMarkerId
     * @return
     */
    public synchronized int AddJetourPoiDynamicMarker(BaseLayer pLayer, LayerItem layerItem, String strMarkerId, String strMarkerInfo, boolean isFocus) {
        Timber.i("AddJetourPoiDynamicMarker: pLayer = %s, layerItem = %s, strMarkerId = %s, strMarkerInfo = %s, isFocus = %s",
                pLayer, layerItem, strMarkerId, strMarkerInfo, isFocus);
        int dynamicId = -1;
        if (pLayer == null || strMarkerId == null || strMarkerId.isEmpty()) {
            return dynamicId;
        }
        View view;
        if (isFocus) {
            view = View.inflate(mApplication, R.layout.global_texture_jetour_poi_parent_focus_point, null);
            TextView name =  view.findViewById(R.id.name);
            TextView distance = view.findViewById(R.id.distance);

            CustomPointLayerItem customPointItem = (CustomPointLayerItem) layerItem;
            String value = customPointItem.getMValue();
            String[] str= value.split("\\|");
            name.setText(str[0]);
            distance.setText(str[1]);
        } else {
            view = View.inflate(mApplication, R.layout.global_texture_jetour_poi_parent_normal_point, null);
        }

        Bitmap dynamicBitmap = BitmapUtils.createBitmapFromView(view);
        ByteBuffer dataBuffer = ByteBuffer.allocate(dynamicBitmap.getByteCount());
        dynamicBitmap.copyPixelsToBuffer(dataBuffer);
        final LayerTexture layerTexture = new LayerTexture();
        layerTexture.dataBuff = new BinaryStream(dataBuffer.array());
        layerTexture.width = dynamicBitmap.getWidth();
        layerTexture.height = dynamicBitmap.getHeight();
        layerTexture.iconType = LayerIconTypeBMP;
        setLayerTexture(strMarkerInfo, layerTexture);
        dynamicId = getDynamicMarkerId();
        layerTexture.resID = dynamicId;

        boolean isAddSuccess = pLayer.getMapView().addLayerTexture(layerTexture);
        return dynamicId;
    }

    /**
     * 多备选 卡片实现
     *
     * @param pLayer
     * @param item
     * @param strMarkerId
     * @param strMarkerInfo
     * @return
     */
    private boolean addCardGuideLabelMarder(BaseLayer pLayer, GuideLabelLayerItem item, String strMarkerId, String strMarkerInfo, CustomTextureParam customParam) {
        String html = getHtmlString(strMarkerId);
        MarkerInfoBean markerInfoBean = mStyleJsonAnalysisUtil.getMarkerInfoFromJson(strMarkerInfo);
        if (markerInfoBean == null) {
            throw new NullPointerException("markerInfoBean is null");
        }
        customParam.markerKey.markerKey = BigInteger.valueOf(markerKey++);/**< 自定义纹理标识，该参数不同会重新生成纹理数据*/
        customParam.markerKey.customXmlStr = html;

        customParam.attrs.isNightForAsvg = false;
        customParam.attrs.scaleFactor = 1.0f;
        customParam.attrs.anchorType = markerInfoBean.getAnchor();
        customParam.attrs.xOffset = -markerInfoBean.getX_offset();
        customParam.attrs.yOffset = markerInfoBean.getY_offset();
        customParam.attrs.isRepeat = true;
        customParam.attrs.isGenMipmaps = false;
        customParam.attrs.isPreMulAlpha = false;

        String time;
        int travelTimeDiff = item.getMTravelTimeDiff();
        if (travelTimeDiff > 0) {
            time = "慢";
        } else {
            time = "快";
        }
        time += CommonUtil.formatTimeBySecond(Math.abs(travelTimeDiff));

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
        cost = String.format(cost, Math.abs(costDiff));

        customParam.updateList.add(createUpdatePair("time_diff_text", time));
        customParam.updateList.add(createUpdatePair("cost_text", item.getMPathCost() + "元"));
        customParam.updateList.add(createUpdatePair("distance_diff_text", distance));
        customParam.updateList.add(createUpdatePair("traffic_light_text", trafficLight));
        return true;
    }

    /**
     * 获取Html目录下的xml文件内容
     *
     * @param strMarkerId
     * @return
     */
    private String getHtmlString(String strMarkerId) {
        String html;
        if (htmlMap.containsKey(strMarkerId)) {
            html = htmlMap.get(strMarkerId);
        } else {
            html = FileUtils.getFileStringFromAssets(SdkApplicationUtils.getApplication(), "html/" + strMarkerId);
            htmlMap.put(strMarkerId, html);
        }
        return html;
    }

    public CustomUpdatePair createUpdatePair(String id, String value) {
        CustomUpdatePair updatePair = new CustomUpdatePair();
        updatePair.idStr = id;
        updatePair.newValue = value;
        return updatePair;
    }

    /**
     * 预警电量
     *
     * @param pLayer
     * @param strMarkerId
     * @param strMarkerInfo
     * @return
     */
    public synchronized int addRangeExhaustedMarker(BaseLayer pLayer, String strMarkerId, String strMarkerInfo) {
        int dynamicId = -1;
        if (pLayer == null || strMarkerId == null || strMarkerId.isEmpty()) {
            return dynamicId;
        }

        /*todo 缺少的资源从demo找
        View view = View.inflate(mApplication, R.layout.exhausted_central_alert, null);
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
        Timber.d("AddRangeExhaustedMarker: 创建纹理 isAddSuccess = " + isAddSuccess);*/

        return dynamicId;
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


    public synchronized int addDynamicMarder(BaseLayer pLayer, String text, String strMarkerId, String strMarkerInfo) {
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

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap dynamicBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(dynamicBitmap);
        Paint paint = new Paint();
        paint.setTextSize(45);
        paint.setColor(Color.parseColor("#ff0000"));
        Rect src = new Rect(0, 0, width, height);
        Rect dst = new Rect(0, 0, dynamicBitmap.getWidth(), dynamicBitmap.getHeight());
        canvas.drawBitmap(bitmap, src, dst, paint);
        canvas.drawText(text, 40, dynamicBitmap.getHeight() / 2, paint);

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
        Timber.d("AddDynamicMarder: 创建纹理" + text + " isAddSuccess = " + isAddSuccess);

        return dynamicId;
    }

    /**
     * 导航电子眼交通设施图层业务，激活态view
     *
     * @param pLayer
     * @param text
     * @param strMarkerId
     * @param strMarkerInfo
     * @return
     */
    public synchronized int addGuideCameraActiveMarder(BaseLayer pLayer, String text, String strMarkerId, String strMarkerInfo, int poiMarkerId) {
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

        // 导航电子眼交通激活态布局
        /*todo 缺少资源从demo找
        View view = View.inflate(mApplication, R.layout.active_camera_layout, null);
        LinearLayout activeCameraInfo = (LinearLayout) view.findViewById(R.id.active_camera_info_img);
        activeCameraInfo.setBackgroundResource(resID);
        TextView activeCameraSpeed = (TextView) view.findViewById(R.id.active_camera_speed);
        activeCameraSpeed.setText(text);
        Bitmap dynamicBitmap = BitmapUtils.createBitmapFromView(view);
        ByteBuffer dataBuffer = ByteBuffer.allocate(dynamicBitmap.getByteCount());
        dynamicBitmap.copyPixelsToBuffer(dataBuffer);
        LayerTexture layerTexture = new LayerTexture();
        layerTexture.dataBuff = new BinaryStream(dataBuffer.array());
        layerTexture.width = dynamicBitmap.getWidth();
        layerTexture.height = dynamicBitmap.getHeight();
        layerTexture.iconType = LayerIconTypeBMP;
        setLayerTexture(strMarkerInfo, layerTexture);
        if (poiMarkerId == -1) { //如果是新的item,累加dynamicId，否则用缓存
            dynamicId = getDynamicMarkerId();
            guideCameraDynamicId = dynamicId;
        }
        layerTexture.resID = guideCameraDynamicId;

        boolean isAddSuccess = pLayer.getMapView().addLayerTexture(layerTexture);
        Timber.d("AddDynamicMarder: 创建纹理" + text + " isAddSuccess = " + isAddSuccess);*/

        return guideCameraDynamicId;
    }

    /**
     * 合成区间测速电子眼view
     *
     * @param pLayer
     * @param text
     * @param strMarkerId
     * @param strMarkerInfo
     * @return
     */
    public synchronized int addGuideIntervalCameraMarder(BaseLayer pLayer, String text, String strMarkerId, String strMarkerInfo) {
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

        // 区间测速布局
        /*todo 缺少资源从demo找
        View view = View.inflate(mApplication, R.layout.interval_camera_layout, null);
        LinearLayout intervalCameraInfo = (LinearLayout) view.findViewById(R.id.interval_camera_info_img);
        intervalCameraInfo.setBackgroundResource(resID);
        TextView intervalCameraSpeed = (TextView) view.findViewById(R.id.interval_camera_speed);
        intervalCameraSpeed.setText(text);
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
        Timber.d("AddDynamicMarder: 创建纹理" + text + " isAddSuccess = " + isAddSuccess);*/

        return dynamicId;
    }

    /**
     * 合成限速电子眼view
     *
     * @param pLayer
     * @param text
     * @param strMarkerId
     * @param strMarkerInfo
     * @return
     */
    public synchronized int addGuideCameraMarder(BaseLayer pLayer, String text, String strMarkerId, String strMarkerInfo) {
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

        // 限速电子眼布局
        /*todo 缺少资源从demo找、
        View view = View.inflate(mApplication, R.layout.navigation_edog_speed_limits_textview, null);
        LinearLayout edogSpeedInfo = (LinearLayout) view.findViewById(R.id.edog_speed_info_img);
        edogSpeedInfo.setBackgroundResource(resID);
        TextView tvEdogSpeedInfo = (TextView) view.findViewById(R.id.edog_speed_info);
        tvEdogSpeedInfo.setText(text);
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
        Timber.d("AddDynamicMarder: 创建纹理" + text + " isAddSuccess = " + isAddSuccess);*/

        return dynamicId;
    }

    /**
     * 合成路牌view
     *
     * @param pLayer
     * @param text
     * @param strMarkerId
     * @param strMarkerInfo
     * @return
     */
    public synchronized int addRouteBoardMarder(BaseLayer pLayer, String text, String strMarkerId, String strMarkerInfo) {
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

        // 路牌名称布局
        /*todo 缺少资源从demo找、
        View view = View.inflate(mApplication, R.layout.route_board_layout, null);
        SkinTextView routeName = (SkinTextView) view.findViewById(R.id.tv_route_board);
        view.setBackgroundResource(resID);
        routeName.setText(text);
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
        Timber.d("AddDynamicMarder: 创建纹理" + text + " isAddSuccess = " + isAddSuccess);*/

        return dynamicId;
    }

    /**
     * 合成导航多备选标签路牌view
     *
     * @param pLayer
     * @param text
     * @param strMarkerId
     * @param strMarkerInfo
     * @return
     */
    public synchronized int addGuideLabelMarder(BaseLayer pLayer, String text, int time, String strMarkerId, String strMarkerInfo) {
        int dynamicId = -1;
        if (pLayer == null || strMarkerId == null || TextUtils.isEmpty(text) || strMarkerId.isEmpty()) {
            return dynamicId;
        }
        int resID = mApplication.getResources().getIdentifier(strMarkerId, "drawable"
                , mApplication.getApplicationInfo().packageName);
        Bitmap bitmap = BitmapFactory.decodeResource(mApplication.getResources(), resID);
        if (bitmap == null) {
            Timber.e(new NullPointerException(), "创建新图片纹理" + strMarkerId + "失败");
            return dynamicId;
        }
        String labelText = "";
        if (time > 0) {
            labelText = "慢";
        } else {
            labelText = "快";
        }
        labelText += CommonUtil.switchFromSecond(Math.abs(time));

        String roadName = "";
        roadName = "经" + text;

        // 导航多备选标签布局
        /*todo 缺少资源从demo找、
        View view = View.inflate(mApplication, R.layout.guide_label_layout, null);
        SkinTextView guideLabel = (SkinTextView) view.findViewById(R.id.guide_label);
        SkinTextView guideText = (SkinTextView) view.findViewById(R.id.guide_text);
        view.setBackgroundResource(resID);
        guideLabel.setText(labelText);
        guideText.setText(roadName);
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
        Timber.d("AddDynamicMarder: 创建纹理" + text + " isAddSuccess = " + isAddSuccess);*/

        return dynamicId;
    }

    /**
     * 合成拥堵路况等路牌view
     *
     * @param pLayer
     * @param text
     * @param strMarkerId
     * @param strMarkerInfo
     * @return
     */
    public synchronized int addGuideCongestionMarder(BaseLayer pLayer, String text, String strMarkerId, String strMarkerInfo) {
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

        // 拥堵路况布局
        /*todo 缺少资源从demo找、
        View view = View.inflate(mApplication, R.layout.congestion_board_layout, null);
        SkinTextView routeName = (SkinTextView) view.findViewById(R.id.conges_time);
        view.setBackgroundResource(resID);
        routeName.setText(text);
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
        Timber.d("AddDynamicMarder: 创建纹理" + text + " isAddSuccess = " + isAddSuccess);*/

        return dynamicId;
    }

    /**
     * 合成路线页面途径路牌
     *
     * @param pLayer
     * @param strMarkerId
     * @param strMarkerInfo
     * @param routeViaRoadLayerItem
     * @param isFocus
     * @return
     */
    public synchronized int addRouteViaRoadMarder(BaseLayer pLayer, String strMarkerId, String strMarkerInfo, RouteViaRoadLayerItem routeViaRoadLayerItem, boolean isFocus) {
        int dynamicId = -1;
        if (pLayer == null || strMarkerId == null || strMarkerId.isEmpty()) {
            return dynamicId;
        }
        /*todo 缺少资源从demo找、
        View view = View.inflate(mApplication, R.layout.route_via_road_layout, null);
        TextView roadName = (TextView) view.findViewById(R.id.road_name);
        TextView roadContent = (TextView) view.findViewById(R.id.road_content);
        ViaRoadInfo viaRoadInfo = routeViaRoadLayerItem.getMViaRoadInfo();
        roadName.setText(viaRoadInfo.roadName);
        if (isFocus) {
            StringBuilder sb = new StringBuilder();
            sb.append(viaRoadInfo.minLaneNum);
            sb.append("-");
            sb.append(viaRoadInfo.maxLaneNum);
            sb.append(" ");
            sb.append(viaRoadInfo.minLimitSpeed);
            sb.append("-");
            sb.append(viaRoadInfo.maxLimitSpeed);
            sb.append("km/h");
            roadContent.setText(sb.toString());
            roadContent.setVisibility(View.VISIBLE);
        } else {
            roadContent.setVisibility(View.GONE);
        }

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
        Timber.d("AddRouteViaRoadMarder: 创建纹理" + viaRoadInfo.roadName + " isAddSuccess = " + isAddSuccess);*/
        return dynamicId;
    }

    /**
     * 终点区域父节点扎标view
     *
     * @param pLayer
     * @param poiName
     * @param travelTime
     * @return
     */
    public synchronized int addEndAreaParentPointsMarder(BaseLayer pLayer, String poiName, int mDirection, long travelTime) {
        int dynamicId = -1;
        //去除poiName判断
        if (pLayer == null) {
            return dynamicId;
        }

        /*todo 缺少资源从demo找、
        View view;
        // 终点区域父节点扎标view
        view = View.inflate(mApplication, R.layout.end_area_parent_poi, null);
        StrokeTextView tvPoiName = view.findViewById(R.id.poi_name);
//        预留eta 到达时间 显示
        TextView travelTimeTv = view.findViewById(R.id.travel_time);
        tvPoiName.setText(poiName, 8);
        String travelTimeStr = AutoRouteUtil.getScheduledTime(SdkApplicationUtils.getApplication().getApplicationContext(), travelTime, false);
        if (SdkNetworkUtil.isNetworkConnected()) {
            travelTimeTv.setText(travelTimeStr);
            travelTimeTv.setVisibility(View.VISIBLE);
        } else {
            travelTimeTv.setVisibility(View.GONE);
        }
        Bitmap dynamicBitmap = BitmapUtils.createBitmapFromView(view);
        ByteBuffer dataBuffer = ByteBuffer.allocate(dynamicBitmap.getByteCount());
        dynamicBitmap.copyPixelsToBuffer(dataBuffer);
        LayerTexture layerTexture = new LayerTexture();
        layerTexture.dataBuff = new BinaryStream(dataBuffer.array());
        layerTexture.width = dynamicBitmap.getWidth();
        layerTexture.height = dynamicBitmap.getHeight();
        layerTexture.iconType = LayerIconTypeBMP;
        switch (mDirection) {
            case RouteEndAreaDirectionLeft://左侧
                layerTexture.anchorType = LayerIconAnchor.LayerIconAnchorCenterRight;
                break;
            case RouteEndAreaDirectionRight://右侧
                layerTexture.anchorType = LayerIconAnchor.LayerIconAnchorCenterLeft;
                break;
            case RouteEndAreaDirectionBottom://下方
                layerTexture.anchorType = LayerIconAnchor.LayerIconAnchorCenterTop;
                break;
            default:
                break;
        }

        dynamicId = getDynamicMarkerId();
        layerTexture.resID = dynamicId;

        boolean isAddSuccess = pLayer.getMapView().addLayerTexture(layerTexture);
        Timber.d("AddDynamicMarder: 创建纹理" + poiName + " isAddSuccess = " + isAddSuccess);*/

        return dynamicId;
    }

    /**
     * 终点区域子节点扎标view
     *
     * @param pLayer
     * @param poiName
     * @param poiType
     * @return
     */
    public synchronized int addEndAreaChildPointsMarder(BaseLayer pLayer, String poiName, int poiType, String strMarkerInfo) {
        int dynamicId = -1;
        String strMarkerId = "";
        switch (poiType) {
            case 31://门
            case 101:
                strMarkerId = "global_image_child_door_day";
                break;
            case 45://进站
            case 103:
                strMarkerId = "global_image_child_in_day";
                break;
            case 46://出站
            case 104:
                strMarkerId = "global_image_child_exist_day";
                break;
            case 43://出发
            case 105:
                strMarkerId = "global_image_child_fly_day";
                break;
            case 44://到达
            case 106:
                strMarkerId = "global_image_child_arrive_day";
                break;
            case 107://地铁出入口
                strMarkerId = "global_image_child_subway_day";
                break;
            case 34://航站楼/候机楼
            case 303:
                strMarkerId = "global_image_child_airport_day";
                break;
            case 35://火车站/机场/汽车站的候机楼/候车室
            case 304:
                strMarkerId = "global_image_child_more_day";
                break;
            case 41://停车场
            case 305:
                strMarkerId = "global_image_child_park_day";
                break;
            case 42://售票处
            case 306:
                strMarkerId = "global_image_child_ticket_day";
                break;
            default://默认门
                strMarkerId = "global_image_child_door_day";
                break;
        }
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

        // 终点区域子节点扎标view
        /*todo 缺少资源从demo找、
        View view = View.inflate(mApplication, R.layout.end_area_child_poi, null);
        SkinImageView poiImg = (SkinImageView) view.findViewById(R.id.poi_img);
        poiImg.setBackgroundResource(resID);
        StrokeTextView tvPoiName = view.findViewById(R.id.poi_name);
        if (poiName != null) {
            tvPoiName.setStrokeWidth(6);
            tvPoiName.setText(poiName);
        }
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
        Timber.d("AddDynamicMarder: 创建纹理" + poiName + " isAddSuccess = " + isAddSuccess);*/

        return dynamicId;
    }


    /**
     * 车速车标view
     *
     * @param pLayer
     * @param strMarkerId
     * @param strMarkerInfo
     * @return
     */
    public synchronized int addSpeedCarMarder(final BaseLayer pLayer, String strMarkerId, String strMarkerInfo) {
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
        LocInfo locInfo = LocationController.getInstance().getLocInfo();
        String speedText = "0";
        if (locInfo != null) {
            speedText = (int) locInfo.speed + "";
        }
        // 车速车标布局
        /*todo 缺少资源从demo找、
        View view = View.inflate(mApplication, R.layout.speed_car_layout, null);
        view.setBackgroundResource(resID);
        SkinTextView speed = (SkinTextView) view.findViewById(R.id.speed);
        view.setBackgroundResource(resID);
        speed.setText(speedText);
        Bitmap dynamicBitmap = BitmapUtils.createBitmapFromView(view);
        ByteBuffer dataBuffer = ByteBuffer.allocate(dynamicBitmap.getByteCount());
        dynamicBitmap.copyPixelsToBuffer(dataBuffer);
        final LayerTexture layerTexture = new LayerTexture();
        layerTexture.dataBuff = new BinaryStream(dataBuffer.array());
        layerTexture.width = dynamicBitmap.getWidth();
        layerTexture.height = dynamicBitmap.getHeight();
        layerTexture.iconType = LayerIconTypeBMP;
        setLayerTexture(strMarkerInfo, layerTexture);
        if (speedCarDynamicId == -1) {
            dynamicId = getDynamicMarkerId();
            speedCarDynamicId = dynamicId;
        }
        layerTexture.resID = speedCarDynamicId;

        boolean isAddSuccess = pLayer.getMapView().addLayerTexture(layerTexture);

        Timber.d("AddDynamicMarder: 创建纹理" + speedText + " isAddSuccess = " + isAddSuccess);*/

        return speedCarDynamicId;
    }

    /**
     * 合成搜索结果父节点扎标view
     *
     * @param pLayer
     * @param strMarkerId
     * @param strMarkerInfo
     * @return
     */
    public synchronized int AddPoiParentPointMarder(final BaseLayer pLayer, String strMarkerId, String strMarkerInfo, SearchParentLayerItem searchParentLayerItem, boolean isFocus) {
        int dynamicId = -1;
        if (pLayer == null || strMarkerId == null || strMarkerId.isEmpty()) {
            return dynamicId;
        }
        View view;
        if (isFocus) {
            view = View.inflate(mApplication, R.layout.global_texture_poi_parent_focus_point, null);
        } else {
            view = View.inflate(mApplication, R.layout.global_texture_poi_parent_normal_point, null);
        }
        TextView index = (TextView) view.findViewById(R.id.poi_position_tv);

        ConstraintLayout constraintLayout = view.findViewById(R.id.root);
        if (NightModeGlobal.isNightMode()) {
            constraintLayout.setBackgroundResource(isFocus ? R.drawable.global_image_icon_list_active : R.drawable.global_image_icon_list_normal);
        } else {
            constraintLayout.setBackgroundResource(isFocus ? R.drawable.global_image_icon_list_active_day : R.drawable.global_image_icon_list_normal_day);
        }

        index.setText(String.valueOf(searchParentLayerItem.getMIndex() + 1));

        Bitmap dynamicBitmap = BitmapUtils.createBitmapFromView(view);
        ByteBuffer dataBuffer = ByteBuffer.allocate(dynamicBitmap.getByteCount());
        dynamicBitmap.copyPixelsToBuffer(dataBuffer);
        final LayerTexture layerTexture = new LayerTexture();
        layerTexture.dataBuff = new BinaryStream(dataBuffer.array());
        layerTexture.width = dynamicBitmap.getWidth();
        layerTexture.height = dynamicBitmap.getHeight();
        layerTexture.iconType = LayerIconTypeBMP;
        setLayerTexture(strMarkerInfo, layerTexture);
        dynamicId = searchParentLayerItem.getMIndex() + (isFocus ? 99999 : 88888);
        layerTexture.resID = dynamicId;

        boolean isAddSuccess = pLayer.getMapView().addLayerTexture(layerTexture);
        return dynamicId;
    }

    public Bitmap createBitmapFromView(View v) {
        //测量使得view指定大小
        v.measure(0, 0);
        //调用layout方法布局后，可以得到view的尺寸大小
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        Bitmap bmp = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        v.draw(c);
        return bmp;
    }

    @SuppressLint("WrongConstant")
    public synchronized int isDynamicMarker(BaseLayer pLayer, LayerItem layerItem, String strMarkerId, String strMarkerInfo) {
        int dynamicId = -1;
        if (pLayer == null || layerItem == null || strMarkerId == null || strMarkerId.isEmpty()) {
            return dynamicId;
        }

        int itemType = layerItem.getItemType();
        int businessType = layerItem.getBusinessType();
        Timber.d("itemType=" + itemType + ", businessType=" + businessType);

        switch (businessType) {
            case BizSearchType.BizSearchTypePoiAlongRoute:
                break;
            case BizSearchType.BizSearchTypePoiParentPoint:
                SearchParentLayerItem item = (SearchParentLayerItem) layerItem;
                Timber.d("BizSearchTypePoiParentPoint=" + item.getPoiType() + ", isFocus=" + item.getFocus());
//                if (item.getPoiType() != PoiParentType.PoiParentTypeGas && item.getPoiType() != PoiParentType.PoiParentTypeGasDiscounts) {
                if (strMarkerId.equals("global_image_b_poi_normal")) {
                    dynamicId = AddPoiParentPointMarder(pLayer, strMarkerId, strMarkerInfo, item, false);
                } else if (strMarkerId.equals("global_image_b_poi_1_focus")) {
                    dynamicId = AddPoiParentPointMarder(pLayer, strMarkerId, strMarkerInfo, item, true);
                }
//                }
                break;
            case BizCustomTypePoint.BizCustomTypePoint2:
                dynamicId = AddLineDayNodeDynamicMarker(pLayer, layerItem, strMarkerId);
                break;
            case BizCustomTypePoint.BizCustomTypePoint3:
                dynamicId = AddLineDayDynamicMarker(pLayer, layerItem, strMarkerId);
                break;
            case BizCustomTypePoint.BizCustomTypePoint5:
                if (strMarkerId.equals("id_dynamic")) {
                    dynamicId = AddJetourPoiDynamicMarker(pLayer, layerItem, strMarkerId,strMarkerInfo,false);
                } else if (strMarkerId.equals("id_dynamic_focus")) {
                    dynamicId = AddJetourPoiDynamicMarker(pLayer, layerItem, strMarkerId,strMarkerInfo,true);
                }
                break;

            case 0x4000:
                dynamicId = addChargeStationMarker(pLayer, layerItem, strMarkerId, strMarkerInfo);
                break;
            case BizAreaType.BizAreaTypePolygonExhaustedPoint:
                if ("1".equals(layerItem.getID().trim())) {
                    dynamicId = addRangeExhaustedMarker(pLayer, strMarkerId, strMarkerInfo);
                }
                break;
            case BizRoadFacilityType.BizRoadFacilityTypeGuideCameraActive: {
                GuideCameraLayerItem guideCameraLayerItem = (GuideCameraLayerItem) layerItem;
                Timber.d("BizRoadFacilityTypeGuideCameraActive=" + guideCameraLayerItem.getMCameraSpeed() + "");
                if (CameraType.CameraTypeSpeed != guideCameraLayerItem.getMCameraExtType()) {
                    break;
                }
                if (guideCameraLayerItem.getMCameraSpeed() <= 0) {
                    return -2;
                }
                dynamicId = addGuideCameraActiveMarder(pLayer, String.valueOf(guideCameraLayerItem.getMCameraSpeed()), strMarkerId, strMarkerInfo, guideCameraLayerItem.getNormalStyle().poiMarkerId);
                break;
            }
            case BizRoadFacilityType.BizRoadFacilityTypeGuideCameraNormal: {
                GuideCameraLayerItem mGuideCameraLayerItem = (GuideCameraLayerItem) layerItem;
                Timber.d("BizRoadFacilityTypeGuideCameraNormal=" + mGuideCameraLayerItem.getMCameraSpeed() + "");
                if (CameraType.CameraTypeSpeed != mGuideCameraLayerItem.getMCameraExtType()) {
                    break;
                }
                if (mGuideCameraLayerItem.getMCameraSpeed() <= 0) {
                    return -2;
                }
                dynamicId = addGuideCameraMarder(pLayer, String.valueOf(mGuideCameraLayerItem.getMCameraSpeed()), strMarkerId, strMarkerInfo);
                break;
            }
            /*case BizRoadFacilityType.BizRoadFacilityTypeGuideIntervalCamera: {
                GuideIntervalCameraLayerItem mGuideIntervalCameraLayerItem = (GuideIntervalCameraLayerItem) layerItem;
                Timber.d("BizRoadFacilityTypeGuideIntervalCamera=" +mGuideIntervalCameraLayerItem.getMSpeed()+"");
                if (CameraType.CameraTypeIntervalvelocitystart != mGuideIntervalCameraLayerItem.getMCameraType()&&CameraType.CameraTypeIntervalvelocityend != mGuideIntervalCameraLayerItem.getMCameraType()) {
                    break;
                }
                if(mGuideIntervalCameraLayerItem.getMSpeed() <= 0){
                    return -2;
                }
                dynamicId = AddGuideIntervalCameraMarder(pLayer, String.valueOf(mGuideIntervalCameraLayerItem.getMSpeed()), strMarkerId, strMarkerInfo);
                break;
            }*/
            case BizRouteType.BizRouteTypeGuidePathBoard: {
                PathBoardLayerItem pathBoardItem = (PathBoardLayerItem) (layerItem);
                String roadName = pathBoardItem.getMBoardName();
                if (roadName == null || roadName.isEmpty()) {
                    roadName = "无名道路";
                }
                Timber.d("BizRouteTypeGuidePathBoard=" + roadName);
                dynamicId = addRouteBoardMarder(pLayer, roadName, strMarkerId, strMarkerInfo);
                break;
            }
            /*case BizRouteType.BizRouteTypeGuideLabel: {
                GuideLabelLayerItem labelItem = (GuideLabelLayerItem) layerItem;
                if (TextUtils.isEmpty(labelItem.getMRoadName())) {
                    return -2;
                }
                if (BaseConstant.USE_CARD_TEXTURE) {
                    dynamicId = addCardGuideLabelMarder(pLayer, labelItem, strMarkerId, strMarkerInfo);
                    isCard = true;
                } else {
                    dynamicId = addGuideLabelMarder(pLayer, labelItem.getMRoadName(), labelItem.getMTravelTimeDiff(), strMarkerId, strMarkerInfo);
                }
                break;
            }*/
            case BizRouteType.BizRouteTypeGuideCongestion: {
                GuideCongestionLayerItem conLayerItem = (GuideCongestionLayerItem) layerItem;
                if (conLayerItem.getMTimeInfo() == null || TextUtils.isEmpty(conLayerItem.getMTimeInfo())) {
                    return -2;
                }
                String text = conLayerItem.getMTimeInfo() + " " + CommonUtil.distanceUnitTransform(conLayerItem.getMRemainDist());
                dynamicId = addGuideCongestionMarder(pLayer, text, strMarkerId, strMarkerInfo);
                break;
            }
            case BizAreaType.BizAreaTypeEndAreaParentPoint: {
                EndAreaParentLayerItem conLayerItem = (EndAreaParentLayerItem) layerItem;
                dynamicId = DynamicStyleUtil.handleEndAreaParentPointsMarkedId(mApplication, pLayer, conLayerItem.getMPoiName(), conLayerItem.getMDirection(), conLayerItem.getMTravelTime(), getDynamicMarkerId());
                break;
            }
            case BizAreaType.BizAreaTypeEndAreaChildPoint: {
                EndAreaPointLayerItem conLayerItem = (EndAreaPointLayerItem) layerItem;
                dynamicId = addEndAreaChildPointsMarder(pLayer, conLayerItem.getMPoiName(), conLayerItem.getMPoiType(), strMarkerInfo);
                break;
            }
            case BizAGroupType.BizAGroupTypeAGroup: {
                PointLayerItem conLayerItem = (PointLayerItem) layerItem;
                mGroupLayerId = pLayer.getLayerID();
                if ("id_dynamic".equals(strMarkerId)) {
                    dynamicId = DynamicStyleUtil.addGroupMarker(mApplication, pLayer, conLayerItem.getID(), strMarkerInfo, mGroupDynamicIds, mDynamicMarkerId, mStyleJsonAnalysisUtil);
                } else if ("id_dynamic_focus".equals(strMarkerId)) {
                    dynamicId = DynamicStyleUtil.addGroupFocusMarker(mApplication, pLayer, conLayerItem.getID(), strMarkerInfo, mGroupDynamicIds, mDynamicMarkerId, mStyleJsonAnalysisUtil);
                }
                mDynamicMarkerId = dynamicId;
                break;
            }
            case BizCarType.BizCarTypeCruise:
            case BizCarType.BizCarTypeGuide:
            case BizCarType.BizCarTypeSearch:
            case BizCarType.BizCarTypeFamiliar: {
                if (itemType == LayerItemType.LayerItemPointType) {//车速车标显示
                    dynamicId = addSpeedCarMarder(pLayer, strMarkerId, strMarkerInfo);
                }

                break;
            }
            case BizRouteType.BizRouteTypeViaRoad:
                //路线页面途径路
                RouteViaRoadLayerItem routeViaRoadLayerItem = (RouteViaRoadLayerItem) layerItem;
                Timber.d("BizRouteTypeViaRoad strMarkerId = %s, strMarkerInfo = %s", strMarkerId, strMarkerInfo);
                if ("id_via_road".equals(strMarkerId)) {
                    dynamicId = addRouteViaRoadMarder(pLayer, strMarkerId, strMarkerInfo, routeViaRoadLayerItem, false);
                } else if ("id_via_road_focus".equals(strMarkerId)) {
                    dynamicId = addRouteViaRoadMarder(pLayer, strMarkerId, strMarkerInfo, routeViaRoadLayerItem, true);
                }
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

//    /**
//     * @brief                       图元样式中通过marker  图片文件名获取对应的marker id回调接口
//     * @param[in] pLayer            图元所在图层
//     * @param[in] pItem             需要更新样式的具体图元，通过GetBusinessType返回值判断具体图层
//     * @param[in] strMarkerInfo     marker对应纹理的数据参数配置名
//     * @return int32_t              返回图片的marker id
//     * @note thread：main           HMI根据图片文件名添加纹理并且返回相应的marker id，如果是marker id，则转换成int32_t后返回
//     */
//    @Override
//    public synchronized int getMarkerId(BaseLayer pLayer, LayerItem pItem, String strMarkerId, String strMarkerInfo)  {
//
//         if (null != strMarkerId && strMarkerId.indexOf("global_image_car_mini_day") >= 0)
//         {
//             Timber.d("getMarkerId: 最初markerID: " + strMarkerId + " , " + strMarkerInfo + ", " + Thread.currentThread());
//         }
//
//        int markerId = -1;
//        strMarkerId = strMarkerId.trim();
//        if (TextUtils.isEmpty(strMarkerId)) {
//             Timber.d("getMarkerId: strMarkerId为空字符" + strMarkerInfo);
//            return markerId;
//        }
//
//        if ( StringUtils.isInteger(strMarkerId)) {
//            markerId = StringUtils.str2Int(strMarkerId, 10, DEFAULT_ERR_NUM);
//        } else {
//            int dynamicMarderId = isDynamicMarker(pLayer, pItem, strMarkerId, strMarkerInfo);
//            if (dynamicMarderId >=0) {
//                return dynamicMarderId;
//            }
//
//            int index = mImageNameList.indexOf(strMarkerId);
//            if (index >= 0) {
//                markerId = getStaticMarkerId(index);
//                 Timber.d("getMarkerId markerId = " + markerId);
//                return markerId;
//            }
//
//            if(dynamicMarderId!=-2&&dynamicMarderId!=0){//速度值或字符串为空时不显示overlay
//                markerId = AddStaticMarker(pLayer, pItem, strMarkerId, strMarkerInfo);
//            }
//        }
//
//         Timber.d("getMarkerId markerId = " + markerId);
//        return markerId;
//    }

    @Override
    public boolean isRouteCacheStyleEnabled() {
        return false;
    }

    @Override
    public boolean isRouteStyleNightMode() {
        return false;
    }


    /**
     * @return int32_t           返回模型3Dmodel id
     * @brief 图元样式中通过3Dmodel 模型文件名获取对应的3Dmodel id回调接口
     * @param[in] pLayer         图元所在图层
     * @param[in] pItem          需要更新样式的具体图元，通过GetBusinessType返回值判断具体图层
     * @note thread：main        HMI根据模型文件名添加纹理并且返回相应的3Dmodel id，如果是3Dmodel id，则转换成int32_t后返回
     */
    @Override
    public int get3DModelId(BaseLayer pLayer, LayerItem pItem, String str3DModelId) {
        int retValue = -1;
        synchronized (mApplication) {
            if (null != mApplication && null != mCarLayerStyleParser) {
                retValue = mCarLayerStyleParser.addLayer3DModel(mApplication.getAssets(), pLayer, str3DModelId);
            }

            Timber.d("get3DModelId str3DModelId=" + str3DModelId + ", retValue=" + retValue);
        }

        return retValue;
    }

    /**
     * @return String            返回的样式JSON内容字符串，由客户端构造
     * @brief 图元样式JSON串回调接口
     * @param[in] pLayer         图元所在图层
     * @param[in] layerItem      需要更新样式的具体图元，通过GetBusinessType返回值判断具体图层
     * @param[in] forJava        参数暂时无用，只是标示当前接口用于支持Java端，传递Stirng而非cJSON*
     * @note thread：main
     */
    @Override
    public String getLayerStyle(final BaseLayer pLayer, LayerItem layerItem, boolean forJava) {
        String strStyleJson = "EMPTY";
        if (pLayer == null || layerItem == null) {
            return strStyleJson;
        }

        int itemType = layerItem.getItemType();
        int businessType = layerItem.getBusinessType();
        Timber.d("itemType=" + itemType + ", businessType=" + businessType);

        mIsNightMode = NightModeGlobal.isNightMode();

        switch (itemType) {
            case LayerItemType.LayerItemPointType:
                switch (businessType) {
                    case BizCustomTypePoint.BizCustomTypePoint2:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_custom_aha_line_day_node_style");
                        break;
                    case BizCustomTypePoint.BizCustomTypePoint3:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_custom_aha_line_day_style");
                        break;
                    case BizCustomTypePoint.BizCustomTypePoint4:
//                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_custom_park_map");// 终点停车场推荐
                        CustomPointLayerItem parkItem = (CustomPointLayerItem) layerItem;
                        int parkIndex = parkItem.getMType() + 1;//下标从0开始，但纹理json从1开始，所以默认+1
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_custom_park_map" + parkIndex);
                        Timber.i("parkIndex =" + parkIndex + "strStyleJson = " + strStyleJson);
                        break;
                    case BizCustomTypePoint.BizCustomTypePoint5:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_custom_jetour_poi_day_style");
                        break;
                    case ElectricBusinessTypePoint.CHARGE_STATION:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_alongroute_charge_11101");
                        break;
                    case BizCustomTypePoint.BizCustomTypePoint1:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("point_custom_style", mIsNightMode);
                        break;
                    case BizCustomTypePoint.BizCustomTypePoint10:
                        CustomPointLayerItem gpsTrackItem = (CustomPointLayerItem) layerItem;
                        switch (gpsTrackItem.getMType()) {
                            case BizGpsPointType.GPS_POINT_START:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson(mIsNightMode ? "point_route_start_night" : "point_route_start"); //GPS轨迹起点
                                break;
                            case BizGpsPointType.GPS_POINT_END:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson(mIsNightMode ? "point_route_end_night" : "point_route_end");
                                //GPS轨迹终点
                                break;
                        }
                        break;
                    case BizCarType.BizCarTypeCruise:
                    case BizCarType.BizCarTypeGuide:
                    case BizCarType.BizCarTypeSearch:
                    case BizCarType.BizCarTypeFamiliar:
                        if (null != mCarLayerStyleParser) {
                            strStyleJson = mCarLayerStyleParser.getCarLayerPointStyle(layerItem, mStyleJsonAnalysisUtil);
                        }
                        break;

                    case BizRouteType.BizRouteTypeLittleCamera:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_little_camera");
                        break;

                    case BizRouteType.BizRouteTypeEnergyEmptyPoint:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_route_energy_empty");
                        break;

                    case BizRouteType.BizRouteTypeEnergyRemainPoint:
                        RoutePathPointItem routePathPointItem = (RoutePathPointItem) layerItem;
                        Timber.d("getLayerStyle: " + routePathPointItem.getMLeftEnergy());
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_route_energy_remain_day");
                        break;

                    case BizRouteType.BizRouteTypeTrafficLight:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson(mIsNightMode ? "point_traffic_light_night" : "point_traffic_light");
                        break;

                    case BizRouteType.BizRouteTypeCompareTip:
                        RouteCompareTipsLayerItem routeCompareTipsLayerItem = (RouteCompareTipsLayerItem) layerItem;
                        if (routeCompareTipsLayerItem.getMIsFaster()) {
                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_compare_tip_new");
                        } else {
                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_compare_tip_old");
                        }
                        break;

                    case BizUserType.BizUserTypeSendToCar:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_sendtocar");
                        break;

                    case BizUserType.BizUserTypeFavoriteMain:
                    case BizUserType.BizUserTypeFavoritePoi:
                        if (layerItem instanceof FavoritePointLayerItem) {
                            FavoritePointLayerItem favoriteItem = (FavoritePointLayerItem) layerItem;
                            switch (favoriteItem.getMFavoriteType()) {
                                case FavoriteType.FavoriteTypePoi:
                                    strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("user_favorite", mIsNightMode);
                                    break;
                                case FavoriteType.FavoriteTypeHome:
                                    strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("user_home", mIsNightMode);
                                    break;
                                case FavoriteType.FavoriteTypeCompany:
                                    strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("user_company", mIsNightMode);
                                    break;
                                default:
                                    break;
                            }
                        }
                        break;
                    case BizRoadFacilityType.BizRoadFacilityTypeCruiseFacility:
                        CruiseFacilityLayerItem cruiseItem = (CruiseFacilityLayerItem) layerItem;
                        switch (cruiseItem.getMType()) {
                            case CruiseFacilityType.CruiseCarIntersectLeft:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_left_in");
                                break;
                            case CruiseFacilityType.CruiseCarIntersectRight:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_right_in");
                                break;
                            case CruiseFacilityType.CruiseSharpTurnLeft:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_bends");
                                break;
                            case CruiseFacilityType.CruiseWindArea:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_the_wind");
                                break;
                            case CruiseFacilityType.CruiseContinueDetour:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_continuous_curve");
                                break;
                            case CruiseFacilityType.CruiseAccidentProne:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_accident_prone_sections");
                                break;
                            case CruiseFacilityType.CruiseRockFallLeft:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_left_falling_rocks");
                                break;
                            case CruiseFacilityType.CruiseRailwayCrossing:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_railway_crossing");
                                break;
                            case CruiseFacilityType.CruiseGroundSlippery:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_easy_slip_road");
                                break;
                            case CruiseFacilityType.CruiseThroughVillage:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_village");
                                break;
                            case CruiseFacilityType.CruiseNarrowLeftSide:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_left_lane_narrowing");
                                break;
                            case CruiseFacilityType.CruiseNarrowRightSide:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_right_lane_narrowing");
                                break;
                            case CruiseFacilityType.CruiseBothSidesNarrow:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_lane_narrowing_on_both_sides");
                                break;
                            case CruiseFacilityType.CruiseRockFallRight:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_right_falling_rocks");
                                break;
                            case CruiseFacilityType.CruiseThroughSchool:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_school");
                                break;
                            case CruiseFacilityType.CruiseKeeperRailwayCrossing:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_railway_crossing");
                                break;
                            case CruiseFacilityType.CruiseNokeeperRailwayCrossing:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_railway_crossing");
                                break;
                            case CruiseFacilityType.CruiseSharpTurnRight:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_sharp_turn_right");
                                break;
                            case CruiseFacilityType.CruiseReverseDetour:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_detour_left");
                                break;
                            case CruiseFacilityType.CruiseNoPassing:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_no_overtaking");
                                break;
                            case CruiseFacilityType.CruiseNarrowBridge:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_narrow_bridge");
                                break;
                            case CruiseFacilityType.CruiseAround:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_detour_left_right");
                                break;
                            case CruiseFacilityType.CruiseAroundLeft:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_detour_left");
                                break;
                            case CruiseFacilityType.CruiseAroundRight:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_detour_right");
                                break;
                            case CruiseFacilityType.CruiseMountainDangerLeft:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_mountain_left");
                                break;
                            case CruiseFacilityType.CruiseMountainDangerRight:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_mountain_right");
                                break;
                            case CruiseFacilityType.CruiseUpwardSlope:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_upper_slope");
                                break;
                            case CruiseFacilityType.CruiseDonwardSlope:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_down_slope");
                                break;
                            case CruiseFacilityType.CruiseWaterRoad:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_through_water");
                                break;
                            case CruiseFacilityType.CruiseRoughRoad:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_bumpy_road");
                                break;
                            case CruiseFacilityType.CruiseSlowDown:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_slow_down");
                                break;
                            case CruiseFacilityType.CruiseDanger:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_accident_prone_sections");
                                break;
                            case CruiseFacilityType.CruiseTunnel:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_tunnel");
                                break;
                            case CruiseFacilityType.CruiseFerry:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_ferry");
                                break;
                            default:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_facilities_tunnel");
                                break;
                        }
                        break;
                    case BizRoadFacilityType.BizRoadFacilityTypeCruiseCamera:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson(mIsNightMode ? "cruise_camera_night" : "cruise_camera");
                        break;
                    case BizRouteType.BizRouteTypeGuideCongestion:
                        GuideCongestionLayerItem gConLayerItem = (GuideCongestionLayerItem) layerItem;
                        Timber.d("getLayerStyle: 拥堵气泡guideCongestionLayerItem"
                                + "mCongestionStatus = " + gConLayerItem.getMCongestionStatus()
                                + ",mDirectionStyle = " + gConLayerItem.getMDirectionStyle()
                                + ",mTimeInfo.mTimeInfo = " + gConLayerItem.getMTimeInfo()
                                + ",mRemainDist = " + gConLayerItem.getMRemainDist()
                                + ",mTotalTimeOfSeconds" + gConLayerItem.getMTotalTimeOfSeconds()
                                + ",mTotalRemainDist = " + gConLayerItem.getMTotalRemainDist());
                        if (gConLayerItem.getMDirectionStyle() == BizDirectionStyle.DIRECTION_STYLE_LEFTUP ||
                                gConLayerItem.getMDirectionStyle() == BizDirectionStyle.DIRECTION_STYLE_LEFTDOWN) {
                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_congestion_right");
                        } else if (gConLayerItem.getMDirectionStyle() == BizDirectionStyle.DIRECTION_STYLE_RIGHTUP ||
                                gConLayerItem.getMDirectionStyle() == BizDirectionStyle.DIRECTION_STYLE_RIGHTDOWN) {
                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_congestion_left");
                        }
                        break;
                    case BizRoadFacilityType.BizRoadFacilityTypeCruiseCongestion:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_cruise_congestion_event");
                        break;
                    case BizLabelType.BizLabelTypeCruiseLane:
                        //strStyleJson =  mStyleJsonAnalysis.getStyleBeanJson("point_cruise_lane");
                        break;
                    case BizLabelType.BizLabelTypeRoutePopAddViaPoint:
                    case BizLabelType.BizLabelTypeRoutePopRemoveViaPoint:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_route_popup_alongway");
                        break;
                    case BizLabelType.BizLabelTypeRoutePopSearchPoint:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_route_via");
                        break;
                    case BizLabelType.BizLabelTypeRoutePopEndArea:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_end_area_pop_point");
                        break;

                    case BizUserType.BizUserTypeGpsTrack:
                        GpsTrackPointLayerItem layerItemType = (GpsTrackPointLayerItem) layerItem;
                        switch (layerItemType.getMGpsPointType()) {
                            case BizGpsPointType.GPS_POINT_START:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson(mIsNightMode ? "point_gps_track_start_night" : "point_gps_track_start");
                                break;
                            case BizGpsPointType.GPS_POINT_END:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson(mIsNightMode ? "point_gps_track_end_night" : "point_gps_track_end");
                                break;
                            case BizGpsPointType.GPS_POINT_FASTEST:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson(mIsNightMode ? "point_gps_track_fastest_night" : "point_gps_track_fastest");
                                break;
                            default:
                                break;
                        }
                        break;
                    case BizAreaType.BizAreaTypeEndAreaParentPoint:
                        EndAreaParentLayerItem pItem = (EndAreaParentLayerItem) layerItem;
                        String parentMsg = "BizAreaTypeEndAreaParentPoint:"
                                + "\nmPoiName:" + pItem.getMPoiName()
                                + "\nmTravelTime:" + pItem.getMTravelTime()
                                + "\nmLeftEnergy:" + pItem.getMLeftEnergy()
                                + "\nmDirection:" + pItem.getMDirection();
                        Timber.d(parentMsg);
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_end_area_parent_point_ex");//TODO
                        break;
                    case BizAreaType.BizAreaTypeEndAreaChildPoint:
                        EndAreaPointLayerItem cItem = (EndAreaPointLayerItem) layerItem;
                        String childMsg = "BizAreaTypeEndAreaChildPoint:"
                                + "\nmPoiName:" + cItem.getMPoiName()
                                + "\nmPoiType:" + cItem.getMPoiType();
                        Timber.d(childMsg);

                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_end_area_child_point");//TODO
                        break;
                    case BizAGroupType.BizAGroupTypeAGroup:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_agroup");
                        break;

                    case BizAGroupType.BizAGroupTypeEndPoint:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_agroup_end");
                        break;
                    case BizRouteType.BizRouteTypeViaRoad:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("via_road");
                        break;
                    case BizRouteType.BizRouteTypeStartPoint:
                        RoutePathPointItem routePointItem = (RoutePathPointItem) layerItem;
                        if (!routePointItem.getMIsNavi()) {
                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson(mIsNightMode ? "point_route_start_night" : "point_route_start");
                        }
                        break;
                    case BizRouteType.BizRouteTypeEndPoint:
                        RoutePathPointItem routeItem = (RoutePathPointItem) layerItem;
                        if (!routeItem.getMIsNavi()) {
                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson(mIsNightMode ? "point_route_end_night" : "point_route_end");
                        } else {
                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_end");
                        }
                        break;
                    case BizRouteType.BizRouteTypeEndPathPoint:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_route_end_path");
                        break;
                    case BizRouteType.BizRouteTypeEagleStartPoint:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_eagle_start");
                        break;
                    case BizRouteType.BizRouteTypeEagleViaPoint:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_eagle_via");
                        break;
                    case BizRouteType.BizRouteTypeEagleEndPoint:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_eagle_end");
                        break;

                    case BizRouteType.BizRouteTypeViaPoint:
                        final RoutePathPointItem viaRoutePointItem = (RoutePathPointItem) layerItem;
                        String jsonName = "point_route_via";
                        if (viaRoutePointItem.getMTotalCount() > 1) {
                            jsonName = "point_route_via" + viaRoutePointItem.getID();
                        }
                        if (viaRoutePointItem.getPassed()) {
                            Timber.i("viaRoutePointItem isPassed is called");
                            jsonName = "point_route_via_pass";
                        }
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode(jsonName, mIsNightMode);
                        break;
                    case BizSearchType.BizSearchTypePoiParentPoint:
                        SearchParentLayerItem parentItem = (SearchParentLayerItem) layerItem;
                        switch (parentItem.getPoiType()) {
//                            case PoiParentType.PoiParentTypeGas:
//                                switch (parentItem.getTypeCode()) {
//                                    case 10101:
//                                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_alongroute_gas_10101");
//                                        break;
//                                    case 10102:
//                                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_alongroute_gas_10102");
//                                        break;
//                                    case 10103:
//                                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_alongroute_gas_10103");
//                                        break;
//                                    default:
//                                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_alongroute_gas_default");
//                                        break;
//                                }
//                                break;
//                            case PoiParentType.PoiParentTypeGasDiscounts:
//                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_alongroute_gas_discounts");
//                                break;
                            default:
//                                int index = parentItem.getMIndex();
//                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_parent" + index);
                                //改为动态纹理
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_parent");
                                break;
                        }
                        break;
                    case BizRouteType.BizRouteTypeJamPoint:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_route_jam_point");
                        break;
                    case BizRouteType.BizRouteTypeTrafficBlock:
                    case BizRouteType.BizRouteTypeTrafficBlockOuter:
                        RouteBlockLayerItem blockItem = (RouteBlockLayerItem) layerItem;

                        switch ((int) blockItem.getMEventCloud().pointDetail.pointControl.pTType) {
                            case 11057: /**< 大雾&道路关闭 */
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("route_traffic_block_fog");
                                break; /**< 大雪&道路关闭 */
                            case 11059: /**< 大雪&道路关闭 */
                            case 11063: /**< 路面积雪&道路关闭 */
                            case 11064: /**< 路面薄冰&道路关闭 */
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("route_traffic_block_snow");
                                break;
                            case 11062: /**< 积水&道路关闭 */
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("route_traffic_block_water");
                                break;
                            default:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("route_traffic_block_normal");
                                break;
                        }
                        break;

                    case BizRoadFacilityType.BizRoadFacilityTypeGuideTrafficEvent:
                        GuideTrafficEventLayerItem guideTrafficEventLayerItem = (GuideTrafficEventLayerItem) layerItem;
                        Timber.d("BizRoadFacilityTypeGuideTrafficEvent: getPathId" + guideTrafficEventLayerItem.getPathId());

                        switch (guideTrafficEventLayerItem.getMLayerTag()) {
                            case 11011: //accident
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("guide_traffic_event_accident");
                                break;
                            case 11010: //malfunction
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("guide_traffic_event_malfunction");
                                break;
                            case 11040: //project
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("guide_traffic_event_project");
                                break;
                            case 11100: //water
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("guide_traffic_event_water");
                                break;
                            default:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("guide_traffic_event_block");
                                break;
                        }

                        break;

                    case BizRouteType.BizRouteTypeTrafficEventTip:
                        RouteTrafficEventTipsLayerItem eventTipsItem = (RouteTrafficEventTipsLayerItem) layerItem;
                        Coord3DDouble pos = eventTipsItem.getPosition();
                        Timber.d("BizRouteTypeTrafficEventTip lat = " + pos.lat +
                                ", lon =" + pos + ",lon z = " + pos.z);

                        switch ((int) eventTipsItem.getMTrafficEventTipsInfo().mTrafficIncident.eventType) {
                            case RouteTrafficEventType.ROUTE_NORMAL_TRAFFIC_EVENT:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("route_traffic_normal");
                                break;

                            case RouteTrafficEventType.ROUTE_SERIOUS_TRAFFIC_EVENT:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("route_traffic_serious");
                                break;

                            case RouteTrafficEventType.ROUTE_SUSPECTED_TRAFFIC_EVENT:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("route_traffic_suspected");
                                break;

                            case RouteTrafficEventType.ROUTE_CONSTRUCTION:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("route_traffic_construction");
                                break;

                            case RouteTrafficEventType.ROUTE_CONSTRUCTION_IMPACT_TRAVEL:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("route_traffic_impact_travel");
                                break;

                            case RouteTrafficEventType.ROUTE_CONSTRUCTION_NOT_RECOMMENDED_TRAVEL:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("route_traffic_notrecommend");
                                break;

                            case RouteTrafficEventType.ROUTE_ROAD_WATER:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("route_traffic_water");
                                break;

                            case RouteTrafficEventType.ROUTE_ROAD_OBSTACLE:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("route_traffic_obstacle");
                                break;

                            default:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("route_traffic_normal");
                                break;
                        }
                        break;

                    case BizRoadFacilityType.BizRoadFacilityTypeGuideFacility:
                        GuideFacilityLayerItem compareItem = (GuideFacilityLayerItem) layerItem;
                        Coord3DDouble gfpos = compareItem.getPosition();
                        Timber.d("BizRoadFacilityTypeGuideFacility lat = " + gfpos.lat +
                                ", lon =" + gfpos + ",lon z = " + gfpos.z);

                        switch (compareItem.getMType()) {
                            case FacilityType.FacilityTypeLeftInterflow:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("guide_facilities_left_in");
                                break;

                            case FacilityType.FacilityTypeRightInterflow:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("guide_facilities_right_in");
                                break;

                            case FacilityType.FacilityTypeSharpTurn:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("guide_facilities_sharp_turn_right");
                                break;

                            case FacilityType.FacilityTypeRightSharpTurn:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("guide_facilities_sharp_turn_right");
                                break;

                            case FacilityType.FacilityTypeLeftSharpTurn:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("guide_facilities_sharp_turn_left");
                                break;

                            case FacilityType.FacilityTypeLinkingTurn:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("guide_facilities_continuous_curve");
                                break;

                            case FacilityType.FacilityTypeAccidentArea:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("guide_facilities_accident_prone_sections");
                                break;

                            case FacilityType.FacilityTypeFallingRocks:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("guide_facilities_right_falling_rocks");
                                break;

                            case FacilityType.FacilityTypeRightFallingRocks:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("guide_facilities_right_falling_rocks");
                                break;

                            case FacilityType.FacilityTypeLeftFallingRocks:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("guide_facilities_left_falling_rocks");
                                break;

                            case FacilityType.FacilityTypeRailwayCross:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("guide_facilities_railway_crossing");
                                break;

                            case FacilityType.FacilityTypeSlippery:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("guide_facilities_easy_slip_road");
                                break;

                            case FacilityType.FacilityTypeVillage:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("guide_facilities_village");
                                break;

                            case FacilityType.FacilityTypeLeftNarrow:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("guide_facilities_left_lane_narrowing");
                                break;

                            case FacilityType.FacilityTypeRightNarrow:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("guide_facilities_right_lane_narrowing");
                                break;

                            case FacilityType.FacilityTypeDoubleNarrow:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("guide_facilities_lane_narrowing_on_both_sides");
                                break;

                            case FacilityType.FacilityTypeCrosswindArea:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("guide_facilities_the_wind");
                                break;

                            case FacilityType.FacilityTypeSchoolZone:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("guide_facilities_school");
                                break;

                            case FacilityType.FacilityTypeUpperSteep:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("guide_facilities_upper_slope");
                                break;

                            case FacilityType.FacilityTypeDownSteep:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("guide_facilities_down_slope");
                                break;
                            default:
                                break;
                        }
                        break;
                    case BizSearchType.BizSearchTypePoiLabel:
                        if (layerItem instanceof PointLayerItem) {
                            PointLayerItem pointLayerItem = (PointLayerItem) layerItem;
                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_poi_label");//纹理为点图层10
                        }
                        break;
                    case BizSearchType.BizSearchTypePoiChildPoint:
                        SearchChildLayerItem childItem = (SearchChildLayerItem) layerItem;

                        if (childItem != null) {
                            String overlayType = "point_search_child_more";
                            switch (childItem.getMChildType()) {
                                case 31: /**< 门 */
                                    overlayType = "point_search_child_door";
                                    break;

                                case 34: /**< 航站楼 */
                                    overlayType = "point_search_child_airport";
                                    break;

                                case 41: /**< 停车场 */
                                    overlayType = "point_search_child_park";
                                    break;

                                case 42: /**< 票 */
                                    overlayType = "point_search_child_ticket";
                                    break;

                                case 43: /**< 出发楼 */
                                    overlayType = "point_search_child_fly";
                                    break;

                                case 44: /**< 到达楼 */
                                    overlayType = "point_search_child_arrive";
                                    break;

                                case 45: /**< 入口 */
                                    overlayType = "point_search_child_in";
                                    break;

                                case 46: /**< 出口 */
                                    overlayType = "point_search_child_out";
                                    break;

                                case 107: /**< 地铁站 */
                                    overlayType = "point_search_child_subway";
                                    break;

                                case 999: /**< 公交站， BL返回类型跟普通POI一样特殊定义 */
                                    overlayType = "point_search_child_bus_station";
                                    break;

                                default:
                                    overlayType = "point_search_child_more";
                                    break;
                            }
                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson(overlayType);
                        }
                        break;

                    case BizSearchType.BizSearchTypePoiCentralPos:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_central");
                        break;

                    case BizSearchType.BizSearchTypePoiExitEntrance:
                        SearchExitEntranceLayerItem entranceItem = (SearchExitEntranceLayerItem) layerItem;
                        switch (entranceItem.getMType()) {
                            case 0: //出入口
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("search_park_double");
                                break;
                            case 1: //出口
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("search_park_in");
                                break;
                            case 2: //入口
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("search_park_out");
                                break;
                            default:
                                break;
                        }

                        break;

                    case BizSearchType.BizSearchTypePoiBeginEnd:
                        SearchBeginEndLayerItem beginEndItem = (SearchBeginEndLayerItem) layerItem;
                        switch (beginEndItem.getMPointType()) {
                            case 0: //起点
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_route_start");
                                break;
                            case 1: //终点
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_route_end");
                                break;
                            case 2: //中途点
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_route_via");
                                break;
                            default:
                                break;
                        }

                        break;

                    case BizSearchType.BizSearchTypePoiAlongRoute:
                        SearchAlongWayLayerItem alongRouteItem = (SearchAlongWayLayerItem) layerItem;
                        Timber.i("businessType alongRouteItem.getMTypeCode() = " + alongRouteItem.getMTypeCode());
                        Timber.i("businessType alongRouteItem.getMSearchType() = " + alongRouteItem.getMSearchType());
                        switch (alongRouteItem.getMSearchType()) {
                            case SearchAlongwayType.SearchAlongwayTypeGas: //SearchAlongwayTypeGas
                                switch (alongRouteItem.getMTypeCode()) {
//                                    case 10100:
//                                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_alongroute_gas_10100");
//                                        break;
//
//                                    case 10101:
//                                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_alongroute_gas_10101");
//                                        break;
//
//                                    case 10102:
//                                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_alongroute_gas_10102");
//                                        break;
//
//                                    case 10103:
//                                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_alongroute_gas_10103");
//                                        break;

                                    default:
                                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_alongroute_gas_default");
                                        break;
                                }
                                break;
                            case SearchAlongwayType.SearchAlongwayTypeCharge: //SearchAlongwayTypeCharge
                                switch (alongRouteItem.getMTypeCode()) {
//                                    case ChargeStationType.ChargeStationTypeNormal:
//                                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_alongroute_charge_11100");
//                                        break;
//                                    case ChargeStationType.ChargeStationTypeGuoJiaDianWang:
//                                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_alongroute_charge_11101");
//                                        break;
//                                    case ChargeStationType.ChargeStationTypeTeLaiDian:
//                                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_alongroute_charge_11121");
//                                        break;
//                                    case ChargeStationType.ChargeStationTypeXingXing:
//                                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_alongroute_charge_11122");
//                                        break;
//                                    case ChargeStationType.ChargeStationTypePuTian:
//                                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_alongroute_charge_11105");
//                                        break;
                                    default:
                                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_alongroute_charge_default");
                                        break;
                                }
                                break;
//                            case SearchAlongwayType.SearchAlongwayTypeATM: //SearchAlongwayTypeATM
//                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_alongroute_atm");
//                                break;
                            case SearchAlongwayType.SearchAlongwayTypeFood: //SearchAlongwayTypeFood
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_alongroute_food");
                                break;
                            case SearchAlongwayType.SearchAlongwayTypeToilet: //SearchAlongwayTypeToilet
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_alongroute_wc");
                                break;
                            case SearchAlongwayType.SearchAlongwayTypeMaintenance: //SearchAlongwayTypeMaintenancpoint_search_alongroute_repaire
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_alongroute_repair");
                                break;
//                            case SearchAlongwayType.SearchAlongwayTypeCng:
//                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_alongroute_cng");
//                                break;
                            default:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_alongroute_default");
                                break;
                        }

                        break;
                    case BizAreaType.BizAreaTypePolygonExhaustedPoint:

                        if ("0".equals(layerItem.getID().trim())) {
                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_exhausted_central");
                        } else if ("1".equals(layerItem.getID().trim())) {
                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_exhausted_central_alert");
                        }
                        break;
                    case BizAreaType.BizAreaTypeCircleExhaustedPoint:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_exhausted_central");
                        break;
                    case BizSearchType.BizSearchTypePoiParkRoute:
                        switch (layerItem.getID().trim()) {
                            case "0":
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_parkroute_1");
                                break;
                            case "1":
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_parkroute_2");
                                break;
                            case "2":
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_search_parkroute_3");
                                break;
                            default:
                                break;
                        }
                        break;

                    case BizRouteType.BizRouteTypeWeather:
                        RouteWeatherLayerItem weatherItem = (RouteWeatherLayerItem) layerItem;

                        WeatherLabelItem mWeatherInfo = weatherItem.getMWeatherInfo();
                        Timber.d("WeatherLabelItem: "
                                + "\nmCityID:" + mWeatherInfo.mCityID
                                + "\nmCityName:" + mWeatherInfo.mCityName
                                + "\n(lon,lat):" + mWeatherInfo.mPosition.lon + "," + mWeatherInfo.mPosition.lat
                                + "\nmTimestamp:" + mWeatherInfo.mTimestamp
                                + "\nmWeatherID:" + mWeatherInfo.mWeatherID
                                + "\nmWeatherName:" + mWeatherInfo.mWeatherName
                                + "\nmWeatherType:" + mWeatherInfo.mWeatherType);
                        switch (mWeatherInfo.mWeatherID) {
                            case 13:
                            case 16:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson(mIsNightMode ? "point_route_weather_13_16_night" : "point_route_weather_13_16");
                                break;
                            case 14:
                            case 21:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson(mIsNightMode ? "point_route_weather_14_21_night" : "point_route_weather_14_21");
                                break;
                            case 102:
                            case 103:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson(mIsNightMode ? "point_route_weather_102_103_night" : "point_route_weather_102_103");
                                break;
                            case 200:
                            case 201:
                            case 202:
                            case 203:
                            case 204:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson(mIsNightMode ? "point_route_weather_200_201_202_203_204_night" : "point_route_weather_200_201_202_203_204");
                                break;
                            case 205:
                            case 206:
                            case 208:
                            case 207:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson(mIsNightMode ? "point_route_weather_205_206_207_208_night" : "point_route_weather_205_206_207_208");
                                break;
                            case 209:
                            case 210:
                            case 213:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson(mIsNightMode ? "point_route_weather_213_211_210_209_night" : "point_route_weather_213_211_210_209");
                                break;
                            case 211:
                            case 212:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson(mIsNightMode ? "point_route_weather_211_212_night" : "point_route_weather_211_212");
                                break;
                            case 300:
                            case 306:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson(mIsNightMode ? "point_route_weather_300_306_night" : "point_route_weather_300_306");
                                break;
                            case 301:
                            case 307:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson(mIsNightMode ? "point_route_weather_301_307_night" : "point_route_weather_301_307");
                                break;
                            case 305:
                            case 309:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson(mIsNightMode ? "point_route_weather_305_309_night" : "point_route_weather_305_309");
                                break;
                            case 308:
                            case 310:
                            case 311:
                            case 312:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson(mIsNightMode ? "point_route_weather_308_310_311_312_night" : "point_route_weather_308_310_311_312");
                                break;
                            case 313:
                            case 1001:
                            case 1002:
                            case 1003:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson(mIsNightMode ? "point_route_weather_313_1001_1002_1003_night" : "point_route_weather_313_1001_1002_1003");
                                break;
                            case 401:
                            case 407:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson(mIsNightMode ? "point_route_weather_407_401_night" : "point_route_weather_407_401");
                                break;
                            case 402:
                            case 403:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson(mIsNightMode ? "point_route_weather_403_402_night" : "point_route_weather_403_402");
                                break;
                            case 404:
                            case 405:
                            case 406:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson(mIsNightMode ? "point_route_weather_406_405_404_night" : "point_route_weather_406_405_404");
                                break;
                            case 1004:
                            case 1005:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson(mIsNightMode ? "point_route_weather_1004_1005_night" : "point_route_weather_1004_1005");
                                break;
                            default:
                                String weatherJsonName = "point_route_weather_" + mWeatherInfo.mWeatherID;
                                if (mIsNightMode) {
                                    weatherJsonName = weatherJsonName + "_night";
                                }
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson(weatherJsonName);
                                break;
                        }
                        break;
                    case BizRouteType.BizRouteTypeRestArea:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson(mIsNightMode ? "point_search_alongroute_restarea_night" : "point_search_alongroute_restarea");
                        break;

                    case BizRoadFacilityType.BizRoadFacilityTypeGuideCameraNormal: {
                        GuideCameraLayerItem cameraItem = (GuideCameraLayerItem) layerItem;
                        strStyleJson = getFacilityTypeGuideCameraNormalStyle(cameraItem.getMCameraExtType());
                    }
                    break;
                    case BizRoadFacilityType.BizRoadFacilityTypeGuideCameraActive: {
                        GuideCameraLayerItem cameraItem = (GuideCameraLayerItem) layerItem;
                        int mDirectionStyle = cameraItem.getMDirectionStyle();
                        boolean mbContinuous = cameraItem.getMNeedShowNewCamera();
                        if (mbContinuous) {
                            Timber.d("触发连续电子眼" + mDirectionStyle);
                        }
                        switch (cameraItem.getMCameraExtType()) {
                            case SubCameraExtType.SubCameraExtTypeFlowSpeed:
                                switch (mDirectionStyle) {
                                    case BizDirectionStyle.DIRECTION_STYLE_LEFT:
                                        if (mbContinuous) {
                                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_camera_illega_use_light_left_big");
                                        } else {
                                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_camera_illega_use_light_left");
                                        }
                                        break;
                                    case BizDirectionStyle.DIRECTION_STYLE_RIGHT:
                                        if (mbContinuous) {
                                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_camera_illega_use_light_right_big");
                                        } else {
                                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_camera_illega_use_light_right");
                                        }
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            case SubCameraExtType.SubCameraExtTypeBreakRule:
                                switch (mDirectionStyle) {
                                    case BizDirectionStyle.DIRECTION_STYLE_LEFT:
                                        if (mbContinuous) {
                                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_camera_breakrule_left_big");
                                        } else {
                                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_camera_breakrule_left");
                                        }
                                        break;
                                    case BizDirectionStyle.DIRECTION_STYLE_RIGHT:
                                        if (mbContinuous) {
                                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_camera_breakrule_right_big");
                                        } else {
                                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_camera_breakrule_right");
                                        }
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            case SubCameraExtType.SubCameraExtTypeBusway:
                                switch (mDirectionStyle) {
                                    case BizDirectionStyle.DIRECTION_STYLE_LEFT:
                                        if (mbContinuous) {
                                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_camera_busway_left_big");
                                        } else {
                                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_camera_busway_left");
                                        }
                                        break;
                                    case BizDirectionStyle.DIRECTION_STYLE_RIGHT:
                                        if (mbContinuous) {
                                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_camera_busway_right_big");
                                        } else {
                                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_camera_busway_right");
                                        }
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            case SubCameraExtType.SubCameraExtTypeBicyclelane:
                                switch (mDirectionStyle) {
                                    case BizDirectionStyle.DIRECTION_STYLE_LEFT:
                                        if (mbContinuous) {
                                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_camera_bicyclelane_left_big");
                                        } else {
                                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_camera_bicyclelane_left");
                                        }
                                        break;
                                    case BizDirectionStyle.DIRECTION_STYLE_RIGHT:
                                        if (mbContinuous) {
                                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_camera_bicyclelane_right_big");
                                        } else {
                                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_camera_bicyclelane_right");
                                        }
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            case SubCameraExtType.SubCameraExtTypeTrafficLight:
                                switch (mDirectionStyle) {
                                    case BizDirectionStyle.DIRECTION_STYLE_LEFT:
                                        if (mbContinuous) {
                                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_camera_trafficLight_left_big");
                                        } else {
                                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_camera_trafficLight_left");
                                        }
                                        break;
                                    case BizDirectionStyle.DIRECTION_STYLE_RIGHT:
                                        if (mbContinuous) {
                                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_camera_trafficLight_right_big");
                                        } else {
                                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_camera_trafficLight_right");
                                        }
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            case SubCameraExtType.SubCameraExtTypeEmergencyLane:
                                switch (mDirectionStyle) {
                                    case BizDirectionStyle.DIRECTION_STYLE_LEFT:
                                        if (mbContinuous) {
                                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_camera_emergencylane_left_big");
                                        } else {
                                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_camera_emergencylane_left");
                                        }
                                        break;
                                    case BizDirectionStyle.DIRECTION_STYLE_RIGHT:
                                        if (mbContinuous) {
                                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_camera_emergencylane_right_big");
                                        } else {
                                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_camera_emergencylane_right");
                                        }
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            case SubCameraExtType.SubCameraExtTypeSurveillance:
                                switch (mDirectionStyle) {
                                    case BizDirectionStyle.DIRECTION_STYLE_LEFT:
                                        if (mbContinuous) {
                                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_camera_surveillance_left_big");
                                        } else {
                                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_camera_surveillance_left");
                                        }
                                        break;
                                    case BizDirectionStyle.DIRECTION_STYLE_RIGHT:
                                        if (mbContinuous) {
                                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_camera_surveillance_right_big");
                                        } else {
                                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_camera_surveillance_right");
                                        }
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            default:
                                break;
                        }
                    }
                    break;
                    case BizRoadFacilityType.BizRoadFacilityTypeCruiseCongestionPrompt:
                    /*case BizRoadFacilityType.BizRoadFacilityTypeGuideIntervalCamera:
                        GuideIntervalCameraLayerItem cameraItem = (GuideIntervalCameraLayerItem) layerItem;
                        switch (cameraItem.getMCameraType()) {
                            case CameraType.CameraTypeIntervalvelocitystart:
                                strStyleJson =  mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_interval_camera_start");
                                break;
                            case CameraType.CameraTypeIntervalvelocityend:
                                strStyleJson =  mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_interval_camera_end");
                                break;
                        }
                        break;*/
                    case BizLabelType.BizLabelTypeGuideMixFork: {
                        GuideMixForkLayerItem mixForkItem = (GuideMixForkLayerItem) layerItem;
                        switch (mixForkItem.getMDirectionStyle()) {
                            case BizDirectionStyle.DIRECTION_STYLE_LEFT: {
                                switch (mixForkItem.getID().trim()) {
                                    case "0":
                                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_mix_fork_left_day1");
                                        break;

                                    case "1":
                                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_mix_fork_left_day2");
                                        break;

                                    case "2":
                                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_mix_fork_left_day3");
                                        break;

                                    case "3":
                                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_mix_fork_left_day4");
                                        break;
                                    default:
                                        break;
                                }
                            }
                            break;

                            case BizDirectionStyle.DIRECTION_STYLE_RIGHT: {
                                switch (mixForkItem.getID().trim()) {
                                    case "0":
                                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_mix_fork_right_day1");
                                        break;

                                    case "1":
                                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_mix_fork_right_day2");
                                        break;

                                    case "2":
                                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_mix_fork_right_day3");
                                        break;

                                    case "3":
                                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_mix_fork_right_day4");
                                        break;
                                    default:
                                        break;
                                }
                            }
                            break;
                            default:
                                break;
                        }
                    }
                    break;
                    case BizRouteType.BizRouteTypeGuidePathBoard:
                        PathBoardLayerItem pathBoardItem = (PathBoardLayerItem) (layerItem);

                        if (pathBoardItem != null) {
                            int directionStyle = pathBoardItem.getMDirectionStyle();
                            if (directionStyle == BizDirectionStyle.DIRECTION_STYLE_LEFT) {
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_path_board_left");
                            } else if (directionStyle == BizDirectionStyle.DIRECTION_STYLE_RIGHT) {
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_path_board_right");
                            }
                        }
                        break;

                    case BizRouteType.BizRouteTypeGuideEtaEvent:
                        GuideETAEventLayerItem etaEventItem = (GuideETAEventLayerItem) layerItem;
                        Timber.d("BizRouteTypeGuideEtaEvent: getPathId" + etaEventItem.getMType());

                        switch (etaEventItem.getMType()) {
                            case RouteType.RouteTypeTMC:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_eta_event_tmc");
                                break;
                            case RouteType.RouteTypeLimitForbid:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_eta_event_forbidden");
                                break;
                            case RouteType.RouteTypeDamagedRoad:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_eta_event_close");
                                break;
                            default:
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_eta_event_tmc");
                                break;
                        }
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_eta_event");
                        break;

                    case BizLabelType.BizLabelTypeGpsPoints:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("gps_points");
                        break;
                    case BizFlyLineType.BizFlyLineTypePoint: {
                        String overlayType;
                        switch (getFlyLineType()) {
                            case FLYLINE_SCENE_TYPE_RESULT_LIST:
                                overlayType = "fly_line_style_search_result";
                                break;

                            case FLYLINE_SCENE_TYPE_RESULT_DETAIL:
                                overlayType = "fly_line_style_search_detail";
                                break;

                            case FLYLINE_SCENE_TYPE_SELECT_POI:
                                overlayType = "fly_line_style_search_select";
                                break;

                            default:
                                overlayType = "fly_line_style";
                                break;
                        }

                        int drawEndMode = FlylineDrawMode.FLYLINE_NONE_END;


                        BizControlService bizService = SDKManager.getInstance().getLayerController().getBizControlService();
                        if (null != bizService) {
                            drawEndMode = bizService.getBizFlyLineControl(pLayer.getMapView()).getDrawMode();
                        }
//                        drawEndMode = Integer.parseInt(layerItem.getInfo());
                        Timber.d("====lane drawEndMode = %s", layerItem.getInfo());

                        switch (drawEndMode) {
                            case FlylineDrawMode.FLYLINE_NONE_END:              /**< 不绘制终点*/
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanFromParent(overlayType, "point_none");
                                break;

                            case FlylineDrawMode.FLYLINE_MOVE_END:              /**< 绘制移图状态下的终点 圆形 + 十字*/
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanFromParent(overlayType, "point_move");
                                break;

                            case FlylineDrawMode.FLYLINE_SELECT_END:            /**< 绘制选点状态下的终点 可点击的手状气泡 */
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanFromParent(overlayType, "point_select");
                                break;

                            case FlylineDrawMode.FLYLINE_CLICKED_NORMAL_END:           /**< 绘制点击后状态的终点, 常规的扎点icon */
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanFromParent(overlayType, "point_click");
                                break;

                            case FlylineDrawMode.FLYLINE_CLICKED_TRAFFIC_EVENT_END:     /**< 绘制交通事件点击后的终点*/
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanFromParent(overlayType, "point_traffic_event");
                                break;

                            case FlylineDrawMode.FLYLINE_CLICKED_CHARGING_STATION_END:       /**< 使用用户自定义纹理*/
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanFromParent(overlayType, "point_user_custom");
                                break;
                            default:
                                break;
                        }
                    }
                    break;
                    case BizRouteType.BizRouteTypeGuideLabel: {
                        GuideLabelLayerItem mixForkItem = (GuideLabelLayerItem) layerItem;
                        String sb = "多备选红绿灯信息:\nmPathCost = " + mixForkItem.getMPathCost() +
                                "\nmPathId = " + mixForkItem.getMPathId() +
                                "\nmDistanceDiff = " + mixForkItem.getMDistanceDiff() +
                                "\nmTravelTimeDiff = " + mixForkItem.getMTravelTimeDiff() +
                                "\nmRoadName = " + mixForkItem.getMRoadName() +
                                "\nmPreviewMode = " + mixForkItem.getMPreviewMode() +
                                "\nmTrafficLightDiff = " + mixForkItem.getMTrafficLightDiff();
                        Timber.d(sb);

                        if (BaseConstant.USE_CARD_TEXTURE) {
                            switch (mixForkItem.getMBoardStyle()) {
                                //卡片样式
                                case BizDirectionStyle.DIRECTION_STYLE_LEFTUP:
                                    strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_label_left_up_fast");
                                    break;
                                case BizDirectionStyle.DIRECTION_STYLE_LEFTDOWN:
                                    strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_label_left_down_fast");
                                    break;
                                case BizDirectionStyle.DIRECTION_STYLE_RIGHTDOWN:
                                    strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_label_right_down_fast");
                                    break;
                                case BizDirectionStyle.DIRECTION_STYLE_RIGHTUP:
                                    strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_label_right_up_fast");
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            switch (mixForkItem.getMBoardStyle()) {
//                            //右上
                                case BizDirectionStyle.DIRECTION_STYLE_RIGHTUP:
                                    strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_label_board_rightup");
                                    break;

                                //左上
                                case BizDirectionStyle.DIRECTION_STYLE_LEFTUP:
                                    strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_label_board_leftup");
                                    break;

                                //左下
                                case BizDirectionStyle.DIRECTION_STYLE_LEFTDOWN:
                                    strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_label_board_leftdown");
                                    break;

                                //右下
                                case BizDirectionStyle.DIRECTION_STYLE_RIGHTDOWN:
                                    strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("point_guide_label_board_rightdown");
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                    break;
                    default:
                        break;
                }
                break;
            case LayerItemType.LayerItemNaviCarType:
                if (null != mCarLayerStyleParser) {
                    strStyleJson = mCarLayerStyleParser.getCarLayerStyle(pLayer, layerItem, mIsNightMode, businessType, mStyleJsonAnalysisUtil);
                    CarTypeBean carTypeBean = mStyleJsonAnalysisUtil.getCarTypeBeanFromJson(strStyleJson);
                    if (carTypeBean != null) {
                        if (carTypeBean.getCar_layer_style() != null && carTypeBean.getCar_layer_style().getCompass_marker_info() != null) {
                            Gson gson = new Gson();
                            strStyleJson = gson.toJson(carTypeBean);
                        }
                    }
                }
                break;
            case LayerItemType.LayerItemVectorCrossType://矢量路口
                switch (businessType) {
                    case BizRoadCrossType.BizRoadCrossTypeVector:
                        //根据屏幕适配路口大图宽高
                        VectorCrossBean vectorCrossBean = mStyleJsonAnalysisUtil.getVectorCrossBeanFromJson(mStyleJsonAnalysisUtil.getStyleBeanJson("vector_cross_style"));
                        if (vectorCrossBean != null) {
                            if (null == vectorCrossBean.getVector_cross_layer_style()) {
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("vector_cross_style");
                                return strStyleJson;
                            }
                            if (null == vectorCrossBean.getVector_cross_layer_style().getVector_cross_attr()) {
                                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("vector_cross_style");
                                return strStyleJson;
                            }
                            VectorCrossBean.VectorCrossLayerStyleBean.VectorCrossAttrBean.RectBean rectBean = vectorCrossBean.getVector_cross_layer_style().getVector_cross_attr().getRect();
                            Rect rect = EnlargeInfo.getInstance().getRect();
                            rectBean.setX_min(rect.left);
                            rectBean.setY_min(rect.top);
                            rectBean.setX_max(rect.right);
                            rectBean.setY_max(rect.bottom);
                            Gson gson = new Gson();
                            strStyleJson = gson.toJson(vectorCrossBean);
                            Timber.d("LayerItemVectorCrossType  rect: " + rect);
                        } else {
                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("vector_cross_style");
                        }
                        break;
                    default:
                        break;
                }
                break;
            case LayerItemType.LayerItemRealCityCrossType:
                switch (businessType) {
                    case BizRoadCrossType.BizRoadCrossTypeRealCity:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("rct_cross_style");
                        break;
                    default:
                        break;
                }
                break;
            case LayerItemType.LayerItemRasterImageType:
                switch (businessType) {
                    case BizRoadCrossType.BizRoadCrossTypeRasterImage://栅格图图层
                        //根据屏幕适配栅格大图宽高
                        RasterImageBean rasterImageBean = mStyleJsonAnalysisUtil.getRasterImageBeanFromJson(mStyleJsonAnalysisUtil.getStyleBeanJson("raster_image_style"));
                        if (rasterImageBean != null) {
                            RasterImageBean.RasterImageLayerItemStyleBean rasterImageLayerItemStyleBean = rasterImageBean.getRaster_image_layer_item_style();
                            Rect rect = EnlargeInfo.getInstance().getRect();
                            rasterImageLayerItemStyleBean.setWinx(rect.left);
                            rasterImageLayerItemStyleBean.setWiny(rect.top);
                            rasterImageLayerItemStyleBean.setWidth(rect.width());
                            rasterImageLayerItemStyleBean.setHeight(rect.height());
                            Gson gson = new Gson();
                            strStyleJson = gson.toJson(rasterImageBean);
                            Timber.d("LayerItemRasterImageType  rect: " + rect);
                        } else {
                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("raster_image_style");
                        }
                        break;
                    default:
                        break;
                }
                break;
            case LayerItemType.LayerItemPathType:
                switch (businessType) {
                    case BizRouteType.BizRouteTypePath:
                        //strStyleJson =  mStyleJsonAnalysis.getStyleBeanJson("route_demo_style");
                        Timber.i("BizRouteType.BizRouteTypePath");
                        break;
                    case BizRouteType.BizRouteTypeEaglePath:
                        //strStyleJson =  mStyleJsonAnalysis.getStyleBeanJson("route_eagle_demo_style");
                        Timber.i("BizRouteType.BizRouteTypeEaglePath");
                        break;
                    default:
                        Timber.i("LayerItemType.LayerItemPathType default");
                        break;
                }
                break;
            case LayerItemType.LayerItemLineType:
                switch (businessType) {
                    case BizCustomTypeLine.BizCustomTypeLine1:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("line_custom_style", mIsNightMode);
                        break;
                    case BizCustomTypeLine.BizCustomTypeLine2:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("line_custom_style", mIsNightMode);
                        break;
                    case BizRouteType.BizRouteTypeDodgeLine:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("line_route_dodge_line");
                        break;
                    case BizCustomTypeLine.BizCustomTypeLine3:
                        if (mIsNightMode){
                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("aha_line_custom_style_night", true);
                        } else {
                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("aha_line_custom_style_day", false);
                        }
                        break;
                    case BizSearchType.BizSearchTypeLine:
                        if (layerItem.getID().indexOf("Park") >= 0) {
                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("line_search_line_park");
                        } else if (layerItem.getID().indexOf("Road") >= 0) {
                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("line_search_line_road");
                        } else {
                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("line_search_line_road");
                        }
                        break;
                    case BizRouteType.BizRouteTypeJamLine:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("route_avoid_jam_line");
                        break;

                    case BizRouteType.BizRouteTypeStartEndLine:
                    case BizRouteType.BizRouteTypeStartEndPoint:
                        //起终点连�?
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("fly_line_style");
                        break;
                    case BizAreaType.BizAreaTypeRangeOnMapPolygonLine:
                        if ("0".equals(layerItem.getID().trim())) {
                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("line_range_on_map");
                        } else if ("1".equals(layerItem.getID().trim())) {
                            strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("line_range_on_map_alert");
                        }

                        break;

                    case BizAreaType.BizAreaTypeRangeOnMapCircleLine:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("line_range_on_map");
                        break;

                    case BizLabelType.BizLabelTypeCruiseCongestion:
                        CruiseCongestionItem cruiseCongestionItem = (CruiseCongestionItem) layerItem;
                        if (cruiseCongestionItem != null) {
                            switch (cruiseCongestionItem.getMStatus()) {
                                case 2:
                                    strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_congestion_slow");
                                    break;
                                case 3:
                                    strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_congestion_feeroad");
                                    break;
                                case 4:
                                    strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_congestion_bad");
                                    break;
                                default:
                                    strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("cruise_congestion_slow");
                                    break;
                            }
                        }
                        break;

                    case BizUserType.BizUserTypeGpsTrackLine:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("line_gps_track");
                        break;

                    case BizAreaType.BizAreaTypeEndAreaPolyline:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("line_end_area_style");
                        break;

                    case BizAreaType.BizAreaTypeRestrictPolygon:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("line_route_restrict");
                        break;

                    case BizFlyLineType.BizFlyLineTypeLine:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("fly_line_style");
                        break;
                    default:
                        break;

                }
                break;
            case LayerItemType.LayerItemPolygonType:
                switch (businessType) {
                    case BizCustomTypePolygon.BizCustomTypePolygon1:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("polygon_custom_style", mIsNightMode);
                        break;
                    case BizCustomTypePolygon.BizCustomTypePolygon2:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("polygon_custom_style2", mIsNightMode);
                        break;
                    case BizSearchType.BizSearchTypePoiEndAreaPolygon:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("polygon_search_end_area");
                        break;
                    case BizAreaType.BizAreaTypeRestrictPolygon:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("polygon_route_restrict");
                        break;

                    case BizAreaType.BizAreaTypeEndAreaPolygon:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("polygon_end_area_style");
                        break;

                    case BizAreaType.BizAreaTypeRangeOnMapPolygon:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("polygon_range_on_map_style_normal");
                        break;

                    case BizAreaType.BizAreaTypeRangeOnMapCircle:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("sector_circle_style_normal");
                        break;
                    default:
                        break;
                }
                break;
            case LayerItemType.LayerItemSectorType:
                switch (businessType) {
                    case BizCustomTypeCircle.BizCustomTypeCircle1:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("circle_custom_style", mIsNightMode);
                        break;
                    case BizAreaType.BizAreaTypeRangeOnMapCircle:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("sector_circle_style_normal");
                        break;
                    default:
                        break;
                }
                break;
            case LayerItemType.LayerItem3DModelType:
                if (null != mCarLayerStyleParser) {
                    strStyleJson = mCarLayerStyleParser.getCarLayer3DModelStyle(businessType, mStyleJsonAnalysisUtil);
                }
                break;
            case LayerItemType.LayerItemArrowType:
                switch (businessType) {
                    case BizCustomTypeArrow.BizCustomTypeArrow1:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("arrow_custom_style", mIsNightMode);
                        break;
                    case BizRouteType.BizRouteTypeArrow:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("arrow_route_arrow_style");
                        break;
                    default:
                        break;
                }
                break;
            default:
                switch (businessType) {
                    case BizCustomTypeArrow.BizCustomTypeArrow1:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("arrow_custom_style", mIsNightMode);
                        break;
                    case BizRouteType.BizRouteTypeArrow:
                        strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJson("arrow_route_arrow_style");
                        break;
                    default:
                        break;
                }
                break;
        }

        return strStyleJson;
    }

    private String getFacilityTypeGuideCameraNormalStyle(int mCameraExtType) {
        String strStyleJson = "";
        switch (mCameraExtType) {
            case SubCameraExtType.SubCameraExtTypeIllegalUseLight:
                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("point_guide_camera_illega_use_light", mIsNightMode);
                break;
            case SubCameraExtType.SubCameraExtTypeIllegalUseSafetyBelt:
                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("point_guide_camera_illega_use_safety_belt", mIsNightMode);
                break;
            case SubCameraExtType.SubCameraExtTypeDoNotFollowLane:
            case SubCameraExtType.SubCameraExtTypeIllegalPassCross:
            case SubCameraExtType.SubCameraExtTypeLaneLimitSpeed:
            case SubCameraExtType.SubCameraExtTypeVeryLowSpeed:
            case SubCameraExtType.SubCameraExtTypeBreachProhibitionSign:
            case SubCameraExtType.SubCameraExtTypeCourtesyCarCrossing:
            case SubCameraExtType.SubCameraExtTypeSurveillance:
            case SubCameraExtType.SubCameraExtTypeBreakRule:
                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("point_guide_camera_break_rule", mIsNightMode);
                break;
            case SubCameraExtType.SubCameraExtTypeDialPhoneWhenDriving:
                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("point_guide_camera_dial_phone", mIsNightMode);
                break;
            case SubCameraExtType.SubCameraExtTypeUltrahighSpeed:
                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("point_guide_camera_ultra_high_speed", mIsNightMode);
                break;
            case SubCameraExtType.SubCameraExtTypeTrafficLight:
                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("point_guide_camera_traffic_light", mIsNightMode);
                break;
            case SubCameraExtType.SubCameraExtTypeEndNumberLimit:
                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("point_guide_camera_end_number_limit", mIsNightMode);
                break;
            case SubCameraExtType.SubCameraExtTypeEnvironmentalLimit:
                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("point_guide_camera_environmental_limit", mIsNightMode);
                break;
            case SubCameraExtType.SubCameraExtTypeCourtesyCrossing:
                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("point_guide_camera_courtesy_crossing", mIsNightMode);
                break;
            case SubCameraExtType.SubCameraExtTypeReverseDriving:
                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("point_guide_camera_reverse_driving", mIsNightMode);
                break;
            case SubCameraExtType.SubCameraExtTypeIllegalParking:
                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("point_guide_camera_illegal_parking", mIsNightMode);
                break;
            case SubCameraExtType.SubCameraExtTypeBicyclelane:
                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("point_guide_camera_bicyclelane_new", mIsNightMode);
                break;
            case SubCameraExtType.SubCameraExtTypeBusway:
                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("point_guide_camera_busway_new", mIsNightMode);
                break;
            case SubCameraExtType.SubCameraExtTypeEmergencyLane:
                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("point_guide_camera_emergency_lane", mIsNightMode);
                break;
            case SubCameraExtType.SubCameraExtTypeHonk:
                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("point_guide_camera_type_honk", mIsNightMode);
                break;
            case SubCameraExtType.SubCameraExtTypeFlowSpeed:
                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("point_guide_camera_flow_speed", mIsNightMode);
                break;
            case SubCameraExtType.SubCameraExtTypeRailwayCrossing:
                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("point_guide_camera_railway_crossing", mIsNightMode);
                break;
            case SubCameraExtType.SubCameraExtTypeIntervalvelocityStart:
            case SubCameraExtType.SubCameraExtTypeIntervalvelocityEnd:
                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("point_guide_camera_intervalvelocity", mIsNightMode);
                break;
            case SubCameraExtType.SubCameraExtTypeCarSpacing:
                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("point_guide_camera_car_spacing", mIsNightMode);
                break;
            case SubCameraExtType.SubCameraExtTypeHOVLane:
                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("point_guide_camera_hov_lane", mIsNightMode);
                break;
            case SubCameraExtType.SubCameraExtTypeOccupiedLine:
                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("point_guide_camera_line", mIsNightMode);
                break;
            case SubCameraExtType.SubCameraExtTypeETC:
                strStyleJson = mStyleJsonAnalysisUtil.getStyleBeanJsonWithNightMode("point_guide_camera_etc", mIsNightMode);
                break;
            default:
                break;
        }
        return strStyleJson;
    }

    /**
     * <
     *
     * @return bool        true:成功 false:失败
     * @brief 路线图元样式回调接口
     * @param[in] pLayer         图元所在图层
     * @param[in] pItem          需要更新样式的具体图元，通过GetBusinessType返回值判断具体图层
     * @param[in] style          返回的样式数据结果
     * @note thread：main
     */
    @Override
    public boolean getRouteLayerStyle(BaseLayer baseLayer, LayerItem layerItem, RouteLayerStyle routeLayerStyle) {

        int itemType = layerItem.getItemType();
        int businessType = layerItem.getBusinessType();
        Timber.d("getLayerStyle: route itemType = " + itemType + ",businessType = " + businessType);

        RouteLayerItem routeLayerItem = (RouteLayerItem) layerItem;
        RouteLayerDrawParam routeDrawParam = routeLayerItem.getRouteDrawParam();

        mNaviRouteLayerParser.getRouteLayerStyle(baseLayer, layerItem, routeLayerStyle,
                this, routeDrawParam.mRouteStyleType, mIsNightMode);
        return true;
    }

    /**
     * 卡片纹理销毁
     *
     * @param markerId
     */
    private void destroyCustomTexture(int markerId) {
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

        //卡片销毁
        if (mCardLayerDynamicIds.containsKey(layerId)) {
            ArrayList<DynamicItemsId> dynamicIds = mCardLayerDynamicIds.get(layerId);

            for (int i = 0; i < dynamicIds.size(); i++) {
                DynamicItemsId itemsId = dynamicIds.get(i);
                destroyCustomTexture(itemsId.dynamicId);
            }

            mCardLayerDynamicIds.remove(layerId);
        }
    }

    /**
     * <
     *
     * @brief 清除图层所有item时，通知HMI的接口，上层根据该接口删除动态纹理或者静态纹理
     * @param[in] pLayer        图层对象
     * @note thread：main       HMI或BL内部调用ClearAllItems时触发该回调
     */
    @Override
    public synchronized void clearLayerItem(BaseLayer pLayer, LayerItem layerItem) {
        if (pLayer == null || layerItem == null) {
            return;
        }

        Integer layerId = Integer.valueOf((int) pLayer.getLayerID());
        String itemId = layerItem.getID();

        if (mLayerDynamicIds.containsKey(layerId)) {
            boolean removed = false;
            int removedIndex = -1;
            ArrayList<DynamicItemsId> dynamicIds = mLayerDynamicIds.get(layerId);

            for (int i = 0; i < dynamicIds.size(); i++) {
                DynamicItemsId itemsId = dynamicIds.get(i);
                if (itemsId.itemId.equals(itemId)) {
                    removed = true;
                    removedIndex = i;
                    pLayer.getMapView().destroyTexture(itemsId.dynamicId);
                }
            }

            if (removed) {
                dynamicIds.remove(removedIndex);
                mLayerDynamicIds.put(layerId, dynamicIds);
            }
        }

        //卡片纹理销毁
        if (mCardLayerDynamicIds.containsKey(layerId)) {
            boolean removed = false;
            int removedPosition = -1;
            ArrayList<DynamicItemsId> dynamicIds = mCardLayerDynamicIds.get(layerId);

            for (int i = 0; i < dynamicIds.size(); i++) {
                DynamicItemsId itemsId = dynamicIds.get(i);
                if (itemsId.itemId.equals(itemId)) {
                    removed = true;
                    removedPosition = i;
                    destroyCustomTexture(itemsId.dynamicId);
                }
            }

            if (removed) {
                dynamicIds.remove(removedPosition);
                mCardLayerDynamicIds.put(layerId, dynamicIds);
            }
        }
    }

    @Override
    public int getMarkerId(BaseLayer pLayer, LayerItem pItem, ItemStyleInfo itemStyleInfo) {
        Timber.d("getMarkerId: pLayer = " + pLayer + ", pItem = " + pItem.getID() + ", itemStyleInfo = " + itemStyleInfo);
        int markerId = -1;
        itemStyleInfo.markerId = itemStyleInfo.markerId.trim();
        if (TextUtils.isEmpty(itemStyleInfo.markerId)) {
            Timber.d("getMarkerId: strMarkerId为空字符" + itemStyleInfo.markerId);
            return markerId;
        }
        Timber.d("getMarkerId: " + itemStyleInfo.markerId);

        if (StringUtils.isInteger(itemStyleInfo.markerId)) {
            markerId = StringUtils.str2Int(itemStyleInfo.markerId, 10, DEFAULT_ERR_NUM);
        } else {
            int dynamicMarderId = isDynamicMarker(pLayer, pItem, itemStyleInfo.markerId, itemStyleInfo.markerInfo);
            if (dynamicMarderId >= 0) {
                return dynamicMarderId;
            }

            int index = mImageNameList.indexOf(itemStyleInfo.markerId);
            if (index >= 0) {
                markerId = getStaticMarkerId(index);
//                Timber.d("getMarkerId markerId = " + markerId);
                return markerId;
            }

            if (dynamicMarderId != -2 && dynamicMarderId != 0) {//速度值或字符串为空时不显示overlay
                markerId = addStaticMarker(pLayer, pItem, itemStyleInfo.markerId, itemStyleInfo.markerInfo);
            }
        }

//        Timber.d("getMarkerId markerId = " + markerId);
        return markerId;
    }

    @Override
    public boolean getCustomTexture(BaseLayer layer, LayerItem layerItem, ItemStyleInfo styleInfo, CustomTextureParam customTexture) {
        int itemType = layerItem.getItemType();
        int businessType = layerItem.getBusinessType();
        Timber.d("itemType=" + itemType + ", businessType=" + businessType);

        switch (businessType) {
            case BizRouteType.BizRouteTypeGuideLabel: {
                GuideLabelLayerItem labelItem = (GuideLabelLayerItem) layerItem;
                if (TextUtils.isEmpty(labelItem.getMRoadName())) {
                    return false;
                }
                if (BaseConstant.USE_CARD_TEXTURE) {
                    addCardGuideLabelMarder(layer, labelItem, styleInfo.markerId, styleInfo.markerInfo, customTexture);
                }
                break;
            }
            default:
                break;
        }

        return true;
    }

    @Override
    public boolean updateCustomTexture(BaseLayer baseLayer, LayerItem layerItem, ItemStyleInfo itemStyleInfo, CustomUpdateParam customUpdateParam) {
        return false;
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

    public synchronized void uninit() {
        mImageNameList.clear();
        mLayerDynamicIds.clear();
        htmlMap.clear();
        mGroupDynamicIds.clear();
    }
}