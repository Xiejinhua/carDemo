package com.desaysv.psmap.base.auto.layerstyle.utils;

import android.text.TextUtils;

import com.autosdk.common.AutoConstant;
import com.autosdk.common.storage.MapSharePreference;
import com.desaysv.psmap.base.auto.layerstyle.bean.CarTypeBean;
import com.desaysv.psmap.base.auto.layerstyle.bean.MarkerInfoBean;
import com.desaysv.psmap.base.auto.layerstyle.bean.RasterImageBean;
import com.desaysv.psmap.base.auto.layerstyle.bean.VectorCrossBean;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/**
 * 图层json解析util类, 以便其它位置使用
 * Created by AutoSdk on 2020/9/3.
 */
public class StyleJsonAnalysisUtil {
    private final String TAG = "JsonUtil";
    private Gson mGson;
    private JSONObject mObjJsonBean;
    private JSONObject mObjStyleJsonBean;
    private JSONObject mObjMarkerJsonBean;
    private JSONObject mObjDemoJsonBean;

    public Gson getGson() {
        if (mGson == null) {
            mGson = new Gson();
        }
        return mGson;
    }

    public StyleJsonAnalysisUtil(String fileStringJson) {
        try {
            if (mObjJsonBean == null) {
                mObjJsonBean = new JSONObject(fileStringJson);
            }

            mObjStyleJsonBean = mObjJsonBean.getJSONObject("layer_item_info");
            mObjMarkerJsonBean = mObjJsonBean.getJSONObject("marker_info");
            mObjDemoJsonBean = mObjJsonBean.getJSONObject("demo_info");

        } catch (JSONException e) {
//            e.printStackTrace();
            Timber.e(e, "解析出错1, JSONException:%s", e.getMessage());
        }
    }

    /**
     * @param key isIncludeKey true:拼凑json包含外层为key，false只拼凑value的json
     */
    public String getStyleBeanJson(String key) {
        StringBuilder jsonBeanBuild = new StringBuilder();
        try {
            jsonBeanBuild.append(mObjStyleJsonBean.get(key).toString());
        } catch (JSONException e) {
            Timber.e(e, "解析出错2, JSONException:%s", e.getMessage());
//            e.printStackTrace();
        }

        return jsonBeanBuild.toString();
    }

    public String getStyleBeanJsonWithNightMode(String key, boolean nightMode) {
        StringBuilder jsonBeanBuild = new StringBuilder();
        try {
            Object object = mObjStyleJsonBean.get(key);
            if (nightMode && object == null) {
                object = mObjStyleJsonBean.get(key + "_night");
            }
            jsonBeanBuild.append(object.toString());
        } catch (JSONException e) {
            Timber.e(e, "解析出错3, JSONException:%s", e.getMessage());
        }

        return jsonBeanBuild.toString();
    }

    public String getDemoStyleBeanJsonWithNightMode(String key, boolean nightMode) {
        StringBuilder jsonBeanBuild = new StringBuilder();
        try {
            Object object = mObjDemoJsonBean.get(key);
            if (nightMode && object == null) {
                object = mObjDemoJsonBean.get(key + "_night");
            }
            jsonBeanBuild.append(object.toString());
        } catch (JSONException e) {
            Timber.e(e, "解析出错4, JSONException:%s", e.getMessage());
        }

        return jsonBeanBuild.toString();
    }

    public String formatJsonStr(String json) {
        if (json == null || json.length() == 0) {
            return "";
        }

        String message = json;
        try {
            json = json.trim();
            if (json.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(json);
                message = jsonObject.toString(2);
                message = message.replaceAll("\n", "\n║ ");
            } else if (json.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(json);
                message = jsonArray.toString(2);
                message = message.replaceAll("\n", "\n║ ");
            }
        } catch (Exception e) {
            Timber.e(e, "Exception:%s", e.getMessage());
        }
        return message;
    }


    /**
     * 从夫json中获取子json数据
     *
     * @param parent
     * @param key
     * @return
     */
    public String getStyleBeanFromParent(String parent, String key) {
        String json = getStyleBeanJson(parent);
        StringBuilder jsonBeanBuild = new StringBuilder();
        JSONObject obj = null;
        try {
            obj = new JSONObject(json);
            jsonBeanBuild.append(obj.get(key).toString());
        } catch (JSONException e) {
            Timber.e(e, "解析出错5, JSONException:%s", e.getMessage());
        }
        Timber.i("getStyleBeanJsonWithKey: " + key + ":" + jsonBeanBuild.toString());
        return jsonBeanBuild.toString();
    }


    /**
     * 传入现有allJson返回指定key的json
     *
     * @param allJson
     * @return
     */
    public String getBeanJson(String allJson, String key) {
        StringBuilder jsonBeanBuild = new StringBuilder();
        JSONObject obj = null;
        try {
            obj = new JSONObject(allJson);
            jsonBeanBuild.append(obj.get(key).toString());
        } catch (JSONException e) {
            Timber.e(e, "解析出错6, JSONException:%s", e.getMessage());
        }

        Timber.i("getBeanJson，allJson = : %s", allJson);
        Timber.i("getBeanJson，json = : %s", jsonBeanBuild.toString());
        return jsonBeanBuild.toString();
    }


    /**
     * @param strMarkerInfo 最外层markerinfo名称，通常为marker_info
     */
    public MarkerInfoBean getMarkerInfoFromJson(String strMarkerInfo) {
        MarkerInfoBean markerInfoBean = null;
        if (TextUtils.isEmpty(strMarkerInfo)) {
            return null;
        }

        Timber.i("getMarkerInfoFromJson:strMarkerInfo =  %s", strMarkerInfo);

        try {
            markerInfoBean = getGson().fromJson(mObjMarkerJsonBean.getJSONObject(strMarkerInfo)
                    .toString(), MarkerInfoBean.class);
        } catch (JSONException e) {

//            e.printStackTrace();
        }

        Timber.i("getMarkerInfoFromJson: info%s", getGson().toJson(markerInfoBean));

        return markerInfoBean;
    }

    /**
     * 栅格路口大图json配置
     *
     * @param strMarkerInfo 最外层markerinfo名称，通常为marker_info
     */
    public RasterImageBean getRasterImageBeanFromJson(String strMarkerInfo) {

        RasterImageBean rasterImageBean = null;
        if (TextUtils.isEmpty(strMarkerInfo)) {
            return null;
        }

        Timber.i("getRasterImageBeanFromJson:strMarkerInfo =  %s", strMarkerInfo);


        rasterImageBean = getGson().fromJson(strMarkerInfo, RasterImageBean.class);

        Timber.i("getRasterImageBeanFromJson: info%s", getGson().toJson(rasterImageBean));

        return rasterImageBean;
    }


    /**
     * 2d路口大图json配置
     *
     * @param strMarkerInfo 最外层markerinfo名称，通常为marker_info
     */
    public VectorCrossBean getVectorCrossBeanFromJson(String strMarkerInfo) {

        VectorCrossBean vectorCrossBean = null;
        if (TextUtils.isEmpty(strMarkerInfo)) {
            return null;
        }

        Timber.i("getVectorCrossBeanFromJson:strMarkerInfo =  %s", strMarkerInfo);

        vectorCrossBean = getGson().fromJson(strMarkerInfo, VectorCrossBean.class);

        Timber.i("getVectorCrossBeanFromJson: info%s", getGson().toJson(vectorCrossBean));

        return vectorCrossBean;
    }

    /**
     * 车标样式json
     */
    public CarTypeBean getCarTypeBeanFromJson(String json) {

        CarTypeBean carTypeBean = null;
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        if ("EMPTY".equals(json)) {
            return null;
        }
        Timber.i("getVectorCrossBeanFromJson:strMarkerInfo =  %s", json);

        carTypeBean = getGson().fromJson(json, CarTypeBean.class);

        MapSharePreference mapSharePreference = new MapSharePreference(MapSharePreference.SharePreferenceName.userSetting);
        boolean showCarCompass = mapSharePreference.getBooleanValue(MapSharePreference.SharePreferenceKeyEnum.showCarCompass, true);
        if (!TextUtils.equals(carTypeBean.getCar_layer_style().getCar_marker().compass_marker_id, "-1")) {
            AutoConstant.compassMarkerId = carTypeBean.getCar_layer_style().getCar_marker().compass_marker_id;
            AutoConstant.track_arc_marker_id = carTypeBean.getCar_layer_style().getCar_marker().track_arc_marker_id;
            if (null != carTypeBean.getCar_layer_style().getCompass_marker_info()) {
                AutoConstant.east_marker_id = carTypeBean.getCar_layer_style().getCompass_marker_info().east_marker_id;
                AutoConstant.south_marker_id = carTypeBean.getCar_layer_style().getCompass_marker_info().south_marker_id;
                AutoConstant.west_marker_id = carTypeBean.getCar_layer_style().getCompass_marker_info().west_marker_id;
                AutoConstant.north_marker_id = carTypeBean.getCar_layer_style().getCompass_marker_info().north_marker_id;
            }
        }
        if (showCarCompass) {
            carTypeBean.getCar_layer_style().getCar_marker().compass_marker_id = AutoConstant.compassMarkerId;
            carTypeBean.getCar_layer_style().getCar_marker().track_arc_marker_id = AutoConstant.track_arc_marker_id;
            if (null != carTypeBean.getCar_layer_style().getCompass_marker_info()) {
                carTypeBean.getCar_layer_style().getCompass_marker_info().east_marker_id = AutoConstant.east_marker_id;
                carTypeBean.getCar_layer_style().getCompass_marker_info().south_marker_id = AutoConstant.south_marker_id;
                carTypeBean.getCar_layer_style().getCompass_marker_info().west_marker_id = AutoConstant.west_marker_id;
                carTypeBean.getCar_layer_style().getCompass_marker_info().north_marker_id = AutoConstant.north_marker_id;
            }
        } else {
            if (null != carTypeBean.getCar_layer_style().getCompass_marker_info()) {
                carTypeBean.getCar_layer_style().getCompass_marker_info().east_marker_id = "-1";
                carTypeBean.getCar_layer_style().getCompass_marker_info().south_marker_id = "-1";
                carTypeBean.getCar_layer_style().getCompass_marker_info().west_marker_id = "-1";
                carTypeBean.getCar_layer_style().getCompass_marker_info().north_marker_id = "-1";
            }
        }

        Timber.i("getVectorCrossBeanFromJson: info%s", getGson().toJson(carTypeBean));

        return carTypeBean;
    }
}