package com.autosdk.bussiness.layer;

import com.autonavi.gbl.common.model.Coord2DDouble;
import com.autonavi.gbl.common.model.Coord3DDouble;
import com.autonavi.gbl.layer.BizControlService;
import com.autonavi.gbl.layer.BizUserControl;
import com.autonavi.gbl.layer.model.BizUserFavoritePoint;
import com.autonavi.gbl.layer.model.BizUserType;
import com.autonavi.gbl.layer.model.ColorSpeedPair;
import com.autonavi.gbl.layer.model.RainbowLinePoint;
import com.autonavi.gbl.map.MapView;
import com.autonavi.gbl.map.OperatorPosture;
import com.autonavi.gbl.map.layer.BaseLayer;
import com.autonavi.gbl.map.layer.observer.ILayerClickObserver;
import com.autonavi.gbl.user.behavior.model.FavoriteBaseItem;
import com.autonavi.gbl.user.behavior.model.FavoriteItem;
import com.autonavi.gbl.user.behavior.model.FavoriteType;
import com.autonavi.gbl.user.behavior.model.SimpleFavoriteItem;
import com.autonavi.gbl.user.usertrack.model.GpsTrackDepthInfo;
import com.autosdk.bussiness.account.BehaviorController;
import com.autosdk.bussiness.map.SurfaceViewID;

import java.util.ArrayList;

public class UserBehaviorLayer extends HMIBaseLayer{

    private BizUserControl mUserControl;
    private BaseLayer mBaseLayer;

    /**
     * @brief 初始化所有control
     */
    protected UserBehaviorLayer(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID, BizControlService bizService, MapView mapView) {
        super(nSurfaceViewID);
        if (null != bizService && null != mapView) {
            // 搜索图层
            mUserControl = bizService.getBizUserControl(mapView);
        }
    }

    public BizUserControl getUserControl() {
        return mUserControl;
    }

    /**
     * 获取对应图层的操作Layer，当前提供给搜索配置焦点态
     *
     * @param type
     * @return
     */
    public BaseLayer getBaseLayer(int type) {
        return mUserControl.getUserLayer(type);
    }

    public void addClickObserver(ILayerClickObserver observer) {
        if (mUserControl != null) {
            mUserControl.addClickObserver(observer);
        }
    }

    public void removeClickObserver(ILayerClickObserver observer) {
        if (mUserControl != null) {
            mUserControl.removeClickObserver(observer);
        }
    }

    public void updateFavoriteMain(ArrayList<BizUserFavoritePoint> favoritePoints) {
        if (mUserControl != null) {
            mUserControl.updateFavoriteMain(favoritePoints);
        }
    }

    public void removeItem(String id) {
        if (mUserControl != null) {
            BaseLayer baseLayer = getBaseLayer(BizUserType.BizUserTypeFavoriteMain);
            baseLayer.removeItem(id);
        }
    }

    public void updateFavoriteMainByFavoriteItem(ArrayList<SimpleFavoriteItem> favoritePoints) {
        if (mUserControl != null) {
            ArrayList<BizUserFavoritePoint> favoriteList = new ArrayList<>();
            for (SimpleFavoriteItem simpleFavoriteItem : favoritePoints) {
                BizUserFavoritePoint bizUserFavoritePoint = new BizUserFavoritePoint();
                FavoriteBaseItem favoriteBaseItem = new FavoriteBaseItem();
                favoriteBaseItem.item_id = simpleFavoriteItem.item_id;
                FavoriteItem favoriteItem = BehaviorController.getInstance().getFavorite(favoriteBaseItem);
                if(favoriteItem != null){
                    bizUserFavoritePoint.id = favoriteItem.item_id;
                }
                if (simpleFavoriteItem.common_name == FavoriteType.FavoriteTypeHome) {
                    bizUserFavoritePoint.favoriteType = FavoriteType.FavoriteTypeHome;
                } else if (simpleFavoriteItem.common_name == FavoriteType.FavoriteTypeCompany) {
                    bizUserFavoritePoint.favoriteType = FavoriteType.FavoriteTypeCompany;
                } else {
                    bizUserFavoritePoint.favoriteType = FavoriteType.FavoriteTypePoi;
                }
                Coord2DDouble coord2DDouble = OperatorPosture.mapToLonLat(simpleFavoriteItem.point_x, simpleFavoriteItem.point_y);
                bizUserFavoritePoint.mPos3D = new Coord3DDouble(coord2DDouble.lon, coord2DDouble.lat, 0);
                favoriteList.add(bizUserFavoritePoint);
            }
            mUserControl.updateFavoriteMain(favoriteList);
        }
    }

    public void addFavoriteMainByFavoriteItem(FavoriteBaseItem favoriteBaseItem) {
        if (mUserControl != null) {
            ArrayList<BizUserFavoritePoint> favoriteList = new ArrayList<>();
            BizUserFavoritePoint bizUserFavoritePoint = new BizUserFavoritePoint();
            FavoriteItem favoriteItem = BehaviorController.getInstance().getFavorite(favoriteBaseItem);
            if(favoriteItem==null){
                return;
            }
            bizUserFavoritePoint.id = favoriteItem.item_id;
            if (favoriteItem.common_name == FavoriteType.FavoriteTypeHome) {
                bizUserFavoritePoint.favoriteType = FavoriteType.FavoriteTypeHome;
            } else if (favoriteItem.common_name == FavoriteType.FavoriteTypeCompany) {
                bizUserFavoritePoint.favoriteType = FavoriteType.FavoriteTypeCompany;
            } else {
                bizUserFavoritePoint.favoriteType = FavoriteType.FavoriteTypePoi;
            }
            Coord2DDouble coord2DDouble = OperatorPosture.mapToLonLat(favoriteItem.point_x, favoriteItem.point_y);
            bizUserFavoritePoint.mPos3D = new Coord3DDouble(coord2DDouble.lon, coord2DDouble.lat, 0);
            favoriteList.add(bizUserFavoritePoint);
            mUserControl.updateFavoriteMain(favoriteList);
        }
    }

    public void updateFavoritePoi(BizUserFavoritePoint favoritePoint) {
        if (mUserControl != null) {
            mUserControl.updateFavoritePoi(favoritePoint);
        }
    }


    /**
     * 清除收藏点
     */
    public void clearAllItems() {
        if (mUserControl != null) {
            mUserControl.clearAllItems();
        }
    }

    /**
     * 清除所有搜索结果
     */
    public void clearAllItems(int bizType) {
        if (mUserControl != null) {
            mUserControl.clearAllItems(bizType);
        }
    }

    /**
     * @brief 设置图元为焦点
     */
    public void setFocus(long bizType, String strID, boolean bFocus) {
        if (mUserControl != null) {
            mUserControl.setFocus(bizType, strID, bFocus);
        }
    }

    /**
     * 清除焦点
     *
     * @param bizType
     */
    public void clearFocus(long bizType) {
        if (mUserControl != null) {
            mUserControl.clearFocus(bizType);
        }
    }

    public void setVisible(boolean visible) {
        if (mUserControl != null) {
            mUserControl.setVisible(visible);
        }
    }

    public void setVisible(long bizType, boolean visible) {
        if (mUserControl != null) {
            mUserControl.setVisible(bizType, visible);
        }
    }

    public void clearGpsTrack() {
        if (mUserControl != null) {
            mUserControl.clearAllItems(BizUserType.BizUserTypeGpsTrack);
            mUserControl.clearAllItems(BizUserType.BizUserTypeGpsTrackLine);
        }
    }

    public void updateGpsTrack(GpsTrackDepthInfo gpsTrackDepthInfo) {
        if (mUserControl != null) {
            mUserControl.updateGpsTrack(gpsTrackDepthInfo);
        }
    }

    //更新彩虹线
    public void updateRainbowLine(ArrayList<ColorSpeedPair> vecColorSpeedPair, ArrayList<RainbowLinePoint> vecRainbowLinePoint){
        if (mUserControl != null) {
            mUserControl.updateRainbowLine(vecColorSpeedPair, vecRainbowLinePoint);
        }
    }
}
