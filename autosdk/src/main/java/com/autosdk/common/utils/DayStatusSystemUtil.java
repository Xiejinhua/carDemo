package com.autosdk.common.utils;

import android.location.Location;

import com.autonavi.gbl.common.model.Coord2DDouble;
import com.autonavi.gbl.map.observer.IDayStatusSystemUtil;
import com.autonavi.gbl.util.TimeUtil;
import com.autonavi.gbl.util.model.DateTime;
import com.autosdk.bussiness.location.LocationController;

import java.util.Calendar;

import timber.log.Timber;

/**
 * 日夜状态工具
 */
public class DayStatusSystemUtil implements IDayStatusSystemUtil {
    private static class UtilHolder {
        private static DayStatusSystemUtil instance = new DayStatusSystemUtil();
    }

    public static DayStatusSystemUtil getInstance() {
        return UtilHolder.instance;
    }

    /**
     * 获取当前经纬度信息
     */
    @Override
    public Coord2DDouble getLonLat() {
        Coord2DDouble coor = new Coord2DDouble();
        Location location = LocationController.getInstance().getLastLocation();
        if (location != null) {
            coor.lon = location.getLongitude();
            coor.lat = location.getLatitude();
        }
        return coor;
    }


    /**
     * 获取当前时间.由昼夜模式的定时器调用，昼夜模式使用这个时间用来判断当前是白天还是黑夜
     *
     * @return false表示采用系统时间, true则表示由调用者设置,
     */
    @Override
    public boolean getDateTime(DateTime datetime) {
        if (datetime == null) {
            return false;
        }

        DateTime tDatetime = TimeUtil.getLocalTime2();
        datetime.date = tDatetime.date;
        datetime.time = tDatetime.time;
        return true;
    }

    public boolean firstTimeNightMode() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        Timber.i("firstTimeNightMode: hour = %s", hour);
        if (hour > 6 && hour < 18) { // 白天
            return false;
        } else {
            return true;
        }
    }
}
