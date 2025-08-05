package com.desaysv.psmap.model.layerstyle.parser;

import com.autonavi.gbl.map.layer.BaseLayer;
import com.autonavi.gbl.map.layer.LayerItem;
import com.autonavi.gbl.map.layer.model.ItemStyleInfo;
import com.autonavi.gbl.map.layer.model.MapRouteTexture;
import com.autonavi.gbl.map.layer.model.PolylineCapTextureInfo;
import com.autonavi.gbl.map.layer.model.PolylineTextureInfo;
import com.autonavi.gbl.map.layer.model.RouteLayerParam;
import com.autonavi.gbl.map.layer.model.RouteLayerStyle;
import com.autonavi.gbl.map.layer.model.RouteLayerStyleType;
import com.autonavi.gbl.map.layer.observer.IPrepareLayerStyle;
import com.autosdk.bussiness.map.SurfaceViewID;
import com.autosdk.common.SdkApplicationUtils;
import com.autosdk.common.utils.ResUtil;
import com.desaysv.psmap.base.auto.layerstyle.utils.MarkerUtil;

import timber.log.Timber;

public class NaviRouteLayerParser {

    private static final String TAG = "NaviRouteLayerParser";

    public RouteLayerStyle getRouteLayerStyle(BaseLayer baseLayer, LayerItem layerItem, RouteLayerStyle routeLayerStyle,
                                              IPrepareLayerStyle prepareLayerStyle, @RouteLayerStyleType.RouteLayerStyleType1 int type,
                                              boolean isNightMode) {
        // 是否为小地图
        boolean isEaglLine = false;
        if (type == RouteLayerStyleType.EaglEye_Normal || type == RouteLayerStyleType.EaglEye_HightLight
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

        // 非高亮路线(包括动态导航的更优路线，和备选路线）
        boolean isHighLight = true;
        //是否为备选路线;
        boolean isAlternative = false;
        if (type == RouteLayerStyleType.Main_Normal || type == RouteLayerStyleType.EaglEye_Normal ||
                type == RouteLayerStyleType.Main_Offline || type == RouteLayerStyleType.EaglEye_Offline) {
            isHighLight = false;
//            if(isMultiple){
//                //若非高亮并且开启了多备选选项时;则即为备选路线;
            isAlternative = true;
//            }
        }
        for (Texture texture : Texture.values()) {
            boolean needShowArrow = (isHighLight) && getLineNeedShowArrow(texture.lineType) && !isEaglLine;
            RoadParam roadParam = getRoadParam(texture.textureType, isOfflineStyle, isHighLight, isAlternative,
                    isEaglLine, isNightMode, baseLayer, layerItem, prepareLayerStyle);
            OverlayParamBoolean paramBoolean = getOverlayParmBoolean(texture.lineType);
            RouteLayerParam overlayParam = new RouteLayerParam();
            overlayParam.routeTexture = texture.textureType;
            overlayParam.lineTextureInfo = getRouteTextureInfo(texture.lineType, false);
            overlayParam.lineCapTextureInfo = getRouteCapTextureInfo(texture.lineType);
            overlayParam.lineWidth = roadParam.fillLineWidth;
            overlayParam.fillColor = roadParam.fillLineColor;
            overlayParam.borderColor = roadParam.borderColor;
            overlayParam.unSelectFillColor = roadParam.fillLineColor;
            overlayParam.unSelectBorderColor = roadParam.borderColor;
            overlayParam.selectFillColor = roadParam.fillLineColor;
            overlayParam.selectBorderColor = roadParam.borderColor;
            overlayParam.fillMarker = roadParam.fillLineId;
            overlayParam.borderMarker = roadParam.borderLineId;
            overlayParam.lineExtract = paramBoolean.isLineExtract;
            overlayParam.useColor = paramBoolean.isUseColor;
            overlayParam.useCap = paramBoolean.isUseCap;
            overlayParam.canBeCovered = paramBoolean.isCanBeCovered;
            overlayParam.showArrow = needShowArrow;
            overlayParam.borderLineWidth = roadParam.borderLineWidth;
//            overlayParam.texPreMulAlpha = true;
            overlayParam.needColorGradient = true;
            routeLayerStyle.vecParam.add(overlayParam);
            routeLayerStyle.arrow3DTextureId = -1;
            routeLayerStyle.highLightParam.fillColorHightLight = 0;
            routeLayerStyle.mPassedColor = getPassedColor(type, isNightMode, isHighLight);
        }
        return routeLayerStyle;
    }

    private long[] getPassedColor(@RouteLayerStyleType.RouteLayerStyleType1 int type, boolean isNightMode, boolean isHighLight) {
        long[] passedColor = new long[3];
        switch (type) {
            case RouteLayerStyleType.Main_Normal:
            case RouteLayerStyleType.Main_Offline:
            case RouteLayerStyleType.Main_HightLight:
            case RouteLayerStyleType.Main_Offline_HightLight:
                if (isNightMode) {
                    if (!isHighLight) {
                        passedColor[0] = 0x00000000;
                        passedColor[1] = 0x00000000;
                        passedColor[2] = 0x00000000;
                    } else {
                        passedColor[0] = 0xff5e5f61;
                        passedColor[1] = 0xff484848;
                        passedColor[2] = 0xffffffff;
                    }
                } else {
                    if (!isHighLight) {
                        passedColor[0] = 0x00000000;
                        passedColor[1] = 0x00000000;
                        passedColor[2] = 0x00000000;
                    } else {
                        passedColor[0] = 0xffadb2b6;
                        passedColor[1] = 0xff8d8f91;
                        passedColor[2] = 0xffffffff;
                    }
                }
                break;
            case RouteLayerStyleType.EaglEye_Normal:
            case RouteLayerStyleType.EaglEye_Offline:
            case RouteLayerStyleType.EaglEye_HightLight:
            case RouteLayerStyleType.EaglEye_Offline_HightLight:
                if (isNightMode) {
                    passedColor[0] = 0xff838383;
                    passedColor[1] = 0xff484848;
                    passedColor[2] = 0xffffffff;
                } else {
                    passedColor[0] = 0xff717172;
                    passedColor[1] = 0xff8d8f91;
                    passedColor[2] = 0xffffffff;
                }
                break;
            default:
                break;
        }
        return passedColor;
    }

    public enum Texture {
        // 限行
        LIMIT(MapRouteTexture.MapRouteTextureLimit, GlLineItemType.TYPE_MARKER_LINE_RESTRICT),
        // 轮渡线
        FERRY(MapRouteTexture.MapRouteTextureFerry, GlLineItemType.TYPE_MARKER_LINE_FERRY),
        // 道路内部箭头
        ARROW(MapRouteTexture.MapRouteTextureArrow, GlLineItemType.TYPE_MARKER_LINE_ARROW),
        // 内部道路
        NAVIABLE(MapRouteTexture.MapRouteTextureNavi, GlLineItemType.TYPE_MARKER_LINE_DOT),
        // 内部道路备选状态
        NONAVI(MapRouteTexture.MapRouteTextureNonavi, GlLineItemType.TYPE_MARKER_LINE_DOT),
        // 未知路况
        DEFAULT(MapRouteTexture.MapRouteTextureDefault, GlLineItemType.TYPE_MARKER_LINE),
        // 畅通
        OPEN(MapRouteTexture.MapRouteTextureOpen, GlLineItemType.TYPE_MARKER_LINE),
        // 缓行
        AMBLE(MapRouteTexture.MapRouteTextureAmble, GlLineItemType.TYPE_MARKER_LINE),
        // 拥堵
        JAM(MapRouteTexture.MapRouteTextureJam, GlLineItemType.TYPE_MARKER_LINE),
        // 严重拥堵
        CONGESTED(MapRouteTexture.MapRouteTextureCongested, GlLineItemType.TYPE_MARKER_LINE),
        // 极畅通深绿
        RAPIDER(MapRouteTexture.MapRouteTextureRapider, GlLineItemType.TYPE_MARKER_LINE);

        private final int textureType;
        private final int lineType;

        Texture(int textureType, int lineType) {
            this.textureType = textureType;
            this.lineType = lineType;
        }
    }

    public boolean getLineNeedShowArrow(int lineType) {
        switch (lineType) {
            case GlLineItemType.TYPE_MARKER_LINE_DOT:
                return false;
            case GlLineItemType.TYPE_MARKER_LINE:
                return true;
            case GlLineItemType.TYPE_MARKER_LINE_ARROW:
                return true;
            default:
                return false;
        }
    }

    class RoadParam {
        /**
         * 道路纹理
         */
        public int fillLineId;

        /**
         * 道路纹理宽度
         */
        public int fillLineWidth;

        /**
         * 描边纹理
         */
        public int borderLineId;

        /**
         * 描边纹理宽度
         */
        public int borderLineWidth;
        /**
         * 道路颜色
         */
        public int fillLineColor;
        /**
         * 描边颜色
         */
        public int borderColor;
    }

    /**
     * 获取不同道路类型的纹理及颜色
     *
     * @param textureType   道路类型
     * @param isOffline     是否离线
     * @param isHighLight   是否非高亮路线
     * @param isAlternative 是否备选
     * @return
     */
    public RoadParam getRoadParam(int textureType, boolean isOffline, boolean isHighLight, boolean isAlternative,
                                  boolean isEaglLine, boolean isNightMode, BaseLayer baseLayer, LayerItem layerItem,
                                  IPrepareLayerStyle prepareLayerStyle) {
        int fillLineId;
        float fillLineRatio;
        int borderLineId;
        float borderLineRatio;
        int fillLineColor;
        int borderColor;

        switch (textureType) {
            // 畅通
            case MapRouteTexture.MapRouteTextureOpen:
                if (!isOffline) {
                    fillLineRatio = 0.375f;
                    borderLineRatio = 0.5f;
                    if (isNightMode) {
                        if (!isHighLight) {
                            fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem, new ItemStyleInfo("", "", "map_lr_road_white_front", ""));
                            borderLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem, new ItemStyleInfo("", "", "map_lr_road_white_back", ""));
                            fillLineColor = 0xff398069;
                            borderColor = 0xff333140;
                        } else {
                            fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_front", ""));
                            borderLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_back", ""));
                            fillLineColor = 0xff00b964;
                            borderColor = 0xff007b40;
                        }
                    } else {
                        if (!isHighLight) {
                            fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_front", ""));
                            borderLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_back", ""));
                            fillLineColor = isEaglLine ? 0xff668b62 : 0xff95c399;
                            borderColor = 0xff99b89b;
                        } else {
                            Timber.d("====RouteLayerParser map_lr_road_white_front textureType = %s", textureType);
                            fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_front", ""));
                            borderLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_back", ""));
                            fillLineColor = isEaglLine ? 0xff00d346 : 0xff00a938;
                            borderColor = 0xff00864c;
                        }
                    }
                    break;
                }
                // 缓行
            case MapRouteTexture.MapRouteTextureAmble:
                if (!isOffline) {
                    fillLineRatio = 0.375f;
                    borderLineRatio = 0.5f;
                    if (isNightMode) {
                        if (isAlternative || !isHighLight) {
                            fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_front", ""));
                            borderLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_back", ""));
                            fillLineColor = 0xff937b4e;
                            borderColor = 0xff333140;
                        } else {
                            fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_front", ""));
                            borderLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_back", ""));
                            fillLineColor = 0xffcea22a;
                            borderColor = 0xff996800;
                        }
                    } else {
                        if (!isHighLight) {
                            fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_front", ""));
                            borderLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_back", ""));
                            fillLineColor = isEaglLine ? 0xffaea873 : 0xffdbd286;
                            borderColor = 0xffd9cb56;
                        } else {
                            Timber.d("====RouteLayerParser map_lr_road_white_front textureType = %s", textureType);
                            fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_front", ""));
                            borderLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_back", ""));
                            fillLineColor = 0xffffea00;
                            borderColor = 0xffffcc00;
                        }
                    }
                    break;
                }
                // 拥堵
            case MapRouteTexture.MapRouteTextureJam:
                if (!isOffline) {
                    fillLineRatio = 0.375f;
                    borderLineRatio = 0.5f;
                    if (isNightMode) {
                        if (!isHighLight) {
                            fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_front", ""));
                            borderLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_back", ""));
                            fillLineColor = 0xff843a4a;
                            borderColor = 0xff333140;
                        } else {
                            fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_front", ""));
                            borderLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_back", ""));
                            fillLineColor = 0xffae0529;
                            borderColor = 0xffa01a00;
                        }
                    } else {
                        if (!isHighLight) {
                            fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_front", ""));
                            borderLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_back", ""));
                            fillLineColor = isEaglLine ? 0xff9c6e69 : 0xffc48d87;
                            borderColor = 0xffbf847e;
                        } else {
                            Timber.d("====RouteLayerParser map_lr_road_white_front textureType = %s", textureType);
                            fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_front", ""));
                            borderLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_back", ""));
                            fillLineColor = 0xfffd3021;
                            borderColor = 0xffdd0f00;
                        }
                    }
                    break;
                }
                // 严重拥堵
            case MapRouteTexture.MapRouteTextureCongested:
                if (!isOffline) {
                    fillLineRatio = 0.375f;
                    borderLineRatio = 0.5f;
                    if (isNightMode) {
                        if (!isHighLight) {
                            fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_front", ""));
                            borderLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_back", ""));
                            fillLineColor = 0xff532e36;
                            borderColor = 0xff333140;
                        } else {
                            fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_front", ""));
                            borderLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_back", ""));
                            fillLineColor = 0xff720c21;
                            borderColor = 0xff510012;
                        }
                    } else {
                        if (!isHighLight) {
                            fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_front", ""));
                            borderLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_back", ""));
                            fillLineColor = isEaglLine ? 0xff74535a : 0xff976f6c;
                            borderColor = 0xffa9635d;
                        } else {
                            Timber.d("====RouteLayerParser map_lr_road_white_front textureType = %s", textureType);
                            fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_front", ""));
                            borderLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                    new ItemStyleInfo("", "", "map_lr_road_white_back", ""));
                            fillLineColor = isEaglLine ? 0xffbf025b : 0xff910446;
                            borderColor = 0xff6e2900;
                        }
                    }
                    break;
                }
                // 未知路况
            case MapRouteTexture.MapRouteTextureDefault:
                fillLineRatio = 0.375f;
                borderLineRatio = 0.5f;
                if (isNightMode) {
                    if (!isHighLight) {
                        fillLineColor = 0xff056d98;
                        borderColor = 0xff333140;
                        fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                new ItemStyleInfo("", "", "map_lr_road_white_front", ""));
                        borderLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                new ItemStyleInfo("", "", "map_lr_road_white_back", ""));
                    } else {
                        fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                new ItemStyleInfo("", "", "map_lr_road_white_front", ""));
                        borderLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                new ItemStyleInfo("", "", "map_lr_road_white_back", ""));
                        fillLineColor = 0xff00a2ff;
                        borderColor = 0xff0084d0;
                    }
                } else {
                    if (!isHighLight) {
                        fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                new ItemStyleInfo("", "", "map_lr_road_white_front", ""));
                        borderLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                new ItemStyleInfo("", "", "map_lr_road_white_back", ""));
                        fillLineColor = 0xff9dc9ff;
                        borderColor = 0xff81baff;
                    } else {
                        Timber.d("====RouteLayerParser map_lr_road_white_front textureType = %s", textureType);
                        fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                new ItemStyleInfo("", "", "map_lr_road_white_front", ""));
                        borderLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                new ItemStyleInfo("", "", "map_lr_road_white_back", ""));
                        fillLineColor = 0xff2385ff;
                        borderColor = 0xff0a6fed;
                    }
                }
                break;
            // 内部道路
            case MapRouteTexture.MapRouteTextureNavi:
                if (!isHighLight) {
                    fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                            new ItemStyleInfo("", "", "map_lr_dott_car_light", ""));
                    borderLineId = -1;
                    fillLineRatio = 0.5f;
                    borderLineRatio = 0.5f;
                    fillLineColor = 0xffffffff;
                    borderColor = 0xffffffff;
                } else {
                    fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                            new ItemStyleInfo("", "", "map_lr_dott_car_fill", ""));
                    borderLineId = -1;
                    fillLineRatio = 0.4375f;
                    borderLineRatio = 0.5f;
                    fillLineColor = 0xff0096ff;
                    borderColor = 0xff0096ff;
                }
                break;
            // 内部道路备选状态
            case MapRouteTexture.MapRouteTextureNonavi:
                fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                        new ItemStyleInfo("", "", "map_lr_dott_car_light", ""));
                borderLineId = -1;
                fillLineRatio = 0.5f;
                borderLineRatio = 0.5f;
                fillLineColor = 0xffffffff;
                borderColor = 0xffffffff;
                break;
            // 道路内部箭头
            case MapRouteTexture.MapRouteTextureArrow:
                fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                        new ItemStyleInfo("", "", "global_image_map_aolr", ""));
                borderLineId = -1;
//                fillLineRatio = 0.296875f;
//                borderLineRatio = 0.296875f;
                fillLineRatio = 0.375f;
                borderLineRatio = 0.5f;
                fillLineColor = 0xffffffff;
                borderColor = 0xffffffff;
                Timber.d("====RouteLayerParser MapRouteTextureArrow textureType = %s", textureType);
                break;
            // 轮渡线
            case MapRouteTexture.MapRouteTextureFerry:
                fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                        new ItemStyleInfo("", "", "map_ferry", ""));
                borderLineId = -1;
                fillLineRatio = 0.4167f;
                borderLineRatio = 0.4167f;
                fillLineColor = 0xff0096ff;
                borderColor = 0xff0096ff;
                break;
            // 限行
            case MapRouteTexture.MapRouteTextureLimit:
                if (!isHighLight) {
                    fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                            new ItemStyleInfo("", "", "map_traffic_platenum_restrict_light", ""));
                    fillLineRatio = 0.40625f;
                } else {
                    fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                            new ItemStyleInfo("", "", "map_traffic_platenum_restrict_hl", ""));
                    fillLineRatio = 0.5f;
                }
                borderLineId = -1;
                borderLineRatio = fillLineRatio;
                fillLineColor = 0xfffbeeb9;
                borderColor = -1;
                break;
            // 极畅通深绿
            case MapRouteTexture.MapRouteTextureRapider:
                if (!isOffline) {
                    fillLineRatio = 0.375f;
                    borderLineRatio = 0.5f;
                    if (isNightMode) {
                        fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem, new ItemStyleInfo("", "", "map_lr_road_white_front", ""));
                        borderLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem, new ItemStyleInfo("", "", "map_lr_road_white_back", ""));
                        fillLineColor = 0xff007A7C;
                        borderColor = 0xff006567;
                    } else {
                        if (isHighLight) {
                            Timber.d("====RouteLayerParser map_lr_road_white_front textureType = %s", textureType);
                        }
                        fillLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                new ItemStyleInfo("", "", "map_lr_road_white_front", ""));
                        borderLineId = prepareLayerStyle.getMarkerId(baseLayer, layerItem,
                                new ItemStyleInfo("", "", "map_lr_road_white_back", ""));
                        fillLineColor = 0xff007D5D;
                        borderColor = 0xff006548;
                    }
                    break;
                }
            default:
                fillLineId = -1;
                borderLineId = -1;
                fillLineRatio = 1.0f;
                borderLineRatio = 1.0f;
                fillLineColor = 0xffffffff;
                borderColor = 0xffffffff;
                break;
        }
        //鹰眼图引导线宽度7，描边1px；其他的则宽度为14，描边为2px
        int fillLineWidth = isEaglLine ? (15 - 1 * 2) : 20 - 2 * 2;
        int borderLineWidth = isEaglLine ? 15 : 20;
        RoadParam roadParam = new RoadParam();
        roadParam.fillLineId = fillLineId;
        roadParam.fillLineWidth = MarkerUtil.getAdapterLineOverlayWidth(fillLineWidth, fillLineRatio, true, SurfaceViewID.SURFACE_VIEW_ID_MAIN);

        roadParam.borderLineId = borderLineId;
        roadParam.borderLineWidth = MarkerUtil.getAdapterLineOverlayWidth(borderLineWidth, borderLineRatio, true, SurfaceViewID.SURFACE_VIEW_ID_MAIN);
        roadParam.fillLineColor = fillLineColor;
        roadParam.borderColor = borderColor;
        Timber.d("====RouteLayerParser fillLineWidth = %s,  borderLineWidth = %s", roadParam.fillLineWidth, roadParam.borderLineWidth);
        return roadParam;
    }

    /**
     * @param lineType
     * @return
     */
    private OverlayParamBoolean getOverlayParmBoolean(int lineType) {
        boolean isUseCap = false;
        boolean isUseColor = true;
        boolean isLineExtract = false;
        boolean isCanBeCovered = false;

        switch (lineType) {
            //id==3000 || id ==3050
            case GlLineItemType.TYPE_MARKER_LINE_COLOR: {
                isUseCap = true;
                isUseColor = true;
                isLineExtract = false;
                isCanBeCovered = true;
                break;
            }
            //id < 3000
            case GlLineItemType.TYPE_MARKER_LINE: {
                isUseCap = true;
                isUseColor = true;
                isLineExtract = false;
                isCanBeCovered = true;
                break;
            }
            //id > 3000 && id < 3003
            case GlLineItemType.TYPE_MARKER_LINE_ARROW: {
                isUseCap = false;
                isUseColor = false;
                isLineExtract = false;
                isCanBeCovered = true;
                break;
            }
            //id >= 3003 && id< 3010
            case GlLineItemType.TYPE_MARKER_LINE_FERRY:
            case GlLineItemType.TYPE_MARKER_LINE_DOT: {
                isUseCap = false;
                isUseColor = true;
                isLineExtract = false;
                isCanBeCovered = true;
                break;
            }
            //id > = 3010
            case GlLineItemType.TYPE_MARKER_LINE_DOT_COLOR: {
                isUseCap = false;
                isUseColor = true;
                isLineExtract = false;
                isCanBeCovered = true;
                break;
            }
            case GlLineItemType.TYPE_MARKER_LINE_RESTRICT: {
                isUseCap = true;
                isUseColor = false;
                isLineExtract = false;
                isCanBeCovered = true;
                break;
            }
            default:
                break;
        }
        OverlayParamBoolean paramBoolean = new OverlayParamBoolean();
        paramBoolean.isUseCap = isUseCap;
        paramBoolean.isUseColor = isUseColor;
        paramBoolean.isLineExtract = isLineExtract;
        paramBoolean.isCanBeCovered = isCanBeCovered;
        return paramBoolean;
    }

    class OverlayParamBoolean {
        public boolean isLineExtract;
        public boolean isUseCap;
        public boolean isUseColor;
        public boolean isCanBeCovered;
    }

    public PolylineTextureInfo getRouteTextureInfo(int lineType, boolean isInNavi) {
        float x1 = 0f;
        float y1 = 0f;
        float x2 = 0f;
        float y2 = 0f;
        float glTexLen = 0f;

        switch (lineType) {
            //id==3000 || id ==3050
            case GlLineItemType.TYPE_MARKER_LINE_COLOR: {
                x1 = 0.05f;
                y1 = 0.5f;
                x2 = 0.95f;
                y2 = 0.5f;
                glTexLen = ResUtil.getAutoDimenValue(SdkApplicationUtils.getApplication(), com.desaysv.psmap.base.R.dimen.sv_dimen_16);
                break;
            }
            //id < 3000
            case GlLineItemType.TYPE_MARKER_LINE: {
//                x1 = 0.0f;
//                y1 = 0.25f;
//                x2 = 1.0f;
//                y2 = 0.25f;

                x1 = 0.0f;
                y1 = 0.25f;
                x2 = 1.0f;
                y2 = 0.25f;
                glTexLen = ResUtil.getAutoDimenValue(SdkApplicationUtils.getApplication(), com.desaysv.psmap.base.R.dimen.sv_dimen_16);
                break;
            }
            //id > 3000 && id < 3003
            case GlLineItemType.TYPE_MARKER_LINE_ARROW: {
                x1 = 0.0f;
                y1 = 1.0f;
                x2 = 1.0f;
                y2 = 0.0f;

//                mX1 = 0.0f;
//                mY1 = 1.0f;
//                mX2 = 0.5f;
//                mY2 = 0.0f;
                glTexLen = ResUtil.getAutoDimenValue(SdkApplicationUtils.getApplication(), com.desaysv.psmap.base.R.dimen.sv_dimen_108);
                break;
            }
            //id >= 3003 && id< 3010
            case GlLineItemType.TYPE_MARKER_LINE_DOT: {
                x1 = 0.0f;
                y1 = 1.0f;
                x2 = 1.0f;
                y2 = 0.0f;
                glTexLen = ResUtil.getAutoDimenValue(SdkApplicationUtils.getApplication(), com.desaysv.psmap.base.R.dimen.sv_dimen_30);
                break;
            }
            //id >= 3003 && id< 3010
            case GlLineItemType.TYPE_MARKER_LINE_FERRY: {
                x1 = 0.0f;
                y1 = 1.0f;
                x2 = 1.0f;
                y2 = 0.0f;
                glTexLen = ResUtil.getAutoDimenValue(SdkApplicationUtils.getApplication(), com.desaysv.psmap.base.R.dimen.sv_dimen_16);
                break;
            }
            //id > = 3010
            case GlLineItemType.TYPE_MARKER_LINE_DOT_COLOR: {
                x1 = 0.0f;
                y1 = 1.0f;
                x2 = 1.0f;
                y2 = 0.0f;
                glTexLen = ResUtil.getAutoDimenValue(SdkApplicationUtils.getApplication(), com.desaysv.psmap.base.R.dimen.sv_dimen_16);
                break;
            }
            case GlLineItemType.TYPE_MARKER_LINE_RESTRICT: {
                x1 = 0f;
                y1 = 1.0f;
                x2 = 0.5f;
                y2 = 0.0f;
                glTexLen = ResUtil.getAutoDimenValue(SdkApplicationUtils.getApplication(), com.desaysv.psmap.base.R.dimen.sv_dimen_32);
                break;
            }
            default:
                break;
        }
        PolylineTextureInfo textureInfo = new PolylineTextureInfo();
        textureInfo.x1 = x1;
        textureInfo.y1 = y1;
        textureInfo.x2 = x2;
        textureInfo.y2 = y2;
        textureInfo.textureLen = glTexLen;
        return textureInfo;
    }

    public PolylineCapTextureInfo getRouteCapTextureInfo(int lineType) {
        float x1 = 0f;
        float y1 = 0f;
        float x2 = 0f;
        float y2 = 0f;
        switch (lineType) {
            //id==3000 || id ==3050
            case GlLineItemType.TYPE_MARKER_LINE_COLOR: {
                x1 = 0.05f;
                y1 = 0.5f;
                x2 = 0.95f;
                y2 = 0.75f;
                break;
            }
            //id < 3000
            case GlLineItemType.TYPE_MARKER_LINE: {
                x1 = 0.0f;
                y1 = 0.5f;
                x2 = 1.0f;
                y2 = 0.75f;
                break;
            }
            //id > 3000 && id < 3003
            case GlLineItemType.TYPE_MARKER_LINE_FERRY:
            case GlLineItemType.TYPE_MARKER_LINE_ARROW: {
                x1 = 0.5f;
                y1 = 0.25f;
                x2 = 1.0f;
                y2 = 0.6f;
                break;
            }
            //id >= 3003 && id< 3010
            case GlLineItemType.TYPE_MARKER_LINE_DOT: {
                break;
            }
            //id > = 3010
            case GlLineItemType.TYPE_MARKER_LINE_DOT_COLOR: {
                break;
            }
            case GlLineItemType.TYPE_MARKER_LINE_RESTRICT: {
                x1 = 0.5f;
                y1 = 0.25f;
                x2 = 1.0f;
                y2 = 0.6f;
                break;
            }
            default:
                break;
        }
        PolylineCapTextureInfo textureInfo = new PolylineCapTextureInfo();
        textureInfo.x1 = x1;
        textureInfo.y1 = y1;
        textureInfo.x2 = x2;
        textureInfo.y2 = y2;
        return textureInfo;
    }
}
