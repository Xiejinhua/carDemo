package com.autosdk.bussiness.common;

import android.graphics.Point;

import com.autonavi.gbl.common.model.Coord2DDouble;
import com.autonavi.gbl.layer.model.BizLayerUtil;
import com.autonavi.gbl.map.OperatorPosture;
import com.autonavi.gbl.map.model.GLGeoPoint;
import com.autonavi.gbl.map.model.PointD;
import com.autosdk.bussiness.data.MapDataController;

import java.io.Serializable;

public class GeoPoint extends GLGeoPoint implements Serializable, Cloneable {
    private static final long serialVersionUID = 927014135610245467L;

    private static final String TAG = "GeoPoint";

    private double lon, lat;
    private int addressCode;

    /**
     * 无参构造方法
     */
    public GeoPoint() {

    }

    /**
     * 地图P20坐标
     * @param point
     */
    public GeoPoint(Point point) {
        this.m_X = point.x;
        this.m_Y = point.y;
    }

    /**
     * 构造方法
     *
     * @param x 20级像素坐标
     * @param y 20级像素坐标
     */
    public GeoPoint(int x, int y) {
        this.m_X = x;
        this.m_Y = y;
    }

    public GeoPoint(GLGeoPoint point) {
        if (point == null) {
            return;
        }
        this.m_X = point.m_X;
        this.m_Y = point.m_Y;
    }

    public GeoPoint(PointD pointD) {
        if (pointD == null) {
            return;
        }
        Coord2DDouble coord2DDouble = OperatorPosture.mapToLonLat(pointD.x, pointD.y);
        this.lat = coord2DDouble.lat;
        this.lon = coord2DDouble.lon;
    }

    /**
     * 构造方法
     *
     * @param lon 真实经纬度坐标--经度（对应x）
     * @param lat 真实经纬度坐标--纬度（对应y）
     */
    public GeoPoint(double lon, double lat) {
        this.lon = lon;
        this.lat = lat;
    }

    /**
     * 获取当前精度信息（与x对应，修改x值，该方法返回数据也自动修改）
     *
     * @return
     */
    public double getLongitude() {
        if (this.lon == 0) {
            toLonlat();
        }
        return this.lon;
    }

    /**
     * 获取当前纬度 信息（与y对应，修改y值，也将印象该方法的返回数据）
     *
     * @return
     */
    public double getLatitude() {
        if (this.lat == 0) {
            toLonlat();
        }
        return this.lat;
    }


    public double getRawLongitude() {
        return this.lon;
    }

    public double getRawLatitude() {
        return this.lat;
    }

    /**
     * 重新设置geopoint 位置，同步修改 x,y 值
     *
     * @param lon
     * @param lat
     * @return this
     */
    public GeoPoint setLonLat(double lon, double lat) {
        this.lat = lat;
        this.lon = lon;
        latLongToPixels(this.lat, this.lon);
        return this;
    }

    /**
     * 经纬度转P20坐标
     *
     * @param latitude
     * @param longitude
     */
    private void latLongToPixels(double latitude, double longitude) {
        PointD coord2DDouble = OperatorPosture.lonLatToMap(longitude, latitude);
        this.m_X = new Double(coord2DDouble.x).intValue();
        this.m_Y = new Double(coord2DDouble.y).intValue();
    }

    /**
     * 拷贝一份独立的GeoPoint,以后冲构成clone吧？
     */
    @Override
    public GeoPoint clone() {
        return new GeoPoint(m_X, m_Y);
    }

    public static GeoPoint glGeoPoint2GeoPoint(GLGeoPoint glGeoPoint) {
        if (glGeoPoint == null) {
            return null;
        }
        return new GeoPoint(glGeoPoint);
    }

    public static GLGeoPoint geoPoint2GlGeoPoint(GeoPoint geoPoint) {
        if (geoPoint == null) {
            return null;
        }
        return new GLGeoPoint(geoPoint.m_X, geoPoint.m_Y);
    }

    public static GLGeoPoint[] geoPoints2GlGeoPoints(GeoPoint[] geoPoints) {
        if (geoPoints == null) {
            return null;
        }
        int size = geoPoints.length;
        GLGeoPoint[] glGeoPoints = new GLGeoPoint[size];
        for (int i = 0; i < size; i++) {
            glGeoPoints[i] = geoPoint2GlGeoPoint(geoPoints[i]);
        }
        return glGeoPoints;
    }

    /**
     * 计算两经纬度的间的直线距离
     */
    public static double calcDistanceBetweenPoints(final GeoPoint startPoint, final GeoPoint endPoint) {
        if (null == startPoint || null == endPoint) {
            return 0.0;
        }
        Coord2DDouble startP = new Coord2DDouble(startPoint.getLongitude(), startPoint.getLatitude());
        Coord2DDouble endP = new Coord2DDouble(endPoint.getLongitude(), endPoint.getLatitude());

        return BizLayerUtil.calcDistanceBetweenPoints(startP, endP);
    }


    /**
     * 判断2个点经纬度是否一致。<br/>
     * 由于ext是transient，因此采用Framgemtn.setResult的时候返回后变成空。<br/>
     * 后面根据getLongitude()和用getLatitude()获取到的值可能和GeoPoint(double lon,double lat)不一致<br/>
     * 因此采用x,y直接进行比较
     *
     * @param point
     * @return
     */
    public boolean isSame(GeoPoint point) {
        if (point == null) {
            return false;
        }
        if (this.m_X != point.m_X || this.m_Y != point.m_Y) {
            return false;
        }
        return true;
    }

    /**
     * 地图P20xy坐标转换到经纬度坐标
     *
     * @hide
     */
    public void toLonlat() {
        if (m_X == 0 || m_Y == 0) {
            return;
        }
        Coord2DDouble coord2DDouble = OperatorPosture.mapToLonLat(m_X, m_Y);
        this.lon = coord2DDouble.lon;
        this.lat = coord2DDouble.lat;
    }

    /**
     * 获取adCode
     * @return
     */
    public int getAdCode() {
        if (addressCode == 0) {
            addressCode = getAddressCode(lon, lat);
        }
        return addressCode;
    }

    /**
     * 获取地区编码信息
     * @return
     */
    private int getAddressCode(double lon, double lat) {
        return MapDataController.getInstance().getAdcodeByLonLat(lon, lat);
    }

    @Override
    public String toString() {
        return "GeoPoint{" +
                "lon=" + lon +
                ", lat=" + lat +
                "x=" + m_X + ", y = " + m_Y +
                '}';
    }
}
