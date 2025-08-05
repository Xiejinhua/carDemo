package com.desaysv.psmap.base.common

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import com.autonavi.gbl.map.model.MapPoiCustomOperateType
import com.autonavi.gbl.map.model.MapPoiCustomType
import com.autonavi.gbl.pos.model.LocInfo
import com.autonavi.gbl.pos.observer.IPosLocInfoObserver
import com.autosdk.bussiness.location.LocationController
import com.autosdk.bussiness.map.MapController
import com.autosdk.bussiness.map.Observer.MapGestureObserver
import com.autosdk.bussiness.map.SurfaceViewID
import com.desaysv.psmap.base.business.CruiseBusiness
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.data.NaviRepository
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.unPeek
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 精简POI逻辑
 */
@RequiresApi(Build.VERSION_CODES.Q)
@Singleton
class SimplePOIController @Inject constructor(
    private val cruiseBusiness: CruiseBusiness,
    private val naviRepository: NaviRepository,
    private val mapController: MapController,
    private val naviBusiness: NaviBusiness
) : IPosLocInfoObserver {
    private val NAVI_SIMPLE_POI = true //导航是否支持精简POI模式

    private val CRUISE_SIMPLE_POI = true//巡航是否支持精简POI模式

    private val TIME_VALUE = 10000L //速度达到后多久进入精简POI模式
    private val SPEED_VALUE = 20 // 速度达到后进入精简POI模式 km/h

    private val mHandler = Handler(Looper.getMainLooper())

    /**
     * 精简模式隐藏的POI类型
     */
    private val mHideList = arrayListOf(
        MapPoiCustomType.POITYPE_LIFE_LIVING_SERVICE,
        MapPoiCustomType.POITYPE_HEALTH_CARE,
        MapPoiCustomType.POITYPE_LIFE_CVS,
        MapPoiCustomType.POITYPE_PLACE_NAME_BUILDING,
        MapPoiCustomType.POITYPE_PUBLIC_HOUSE,
        MapPoiCustomType.POITYPE_LIFE_FOOD,
        MapPoiCustomType.POITYPE_AREA_NAME_BUSINESS,
        MapPoiCustomType.POITYPE_BUSINESS_HOTEL,
        MapPoiCustomType.POITYPE_AUTO_SERVICE,
        MapPoiCustomType.POITYPE_BUSINESS_SHOPPING,
        MapPoiCustomType.POITYPE_AUTO_REPAIR,
        MapPoiCustomType.POITYPE_PLACE_NAME_FLYOVER,
        MapPoiCustomType.POITYPE_PUBLIC_PUBLIC_FACILITIES,
        MapPoiCustomType.POITYPE_SCI_EDU_SPORTS,
        MapPoiCustomType.POITYPE_AREA_NAME_PUBLIC

    )

    private var mSpeed = 0f

    private val simpleRunnable: Runnable = Runnable {
        Timber.d("simpleRunnable")
        if (mSpeed >= SPEED_VALUE && naviRepository.isNavigating()) {
            simplePOI(true)
        }
    }

    private var mInSimplePoi = false

    @Synchronized
    private fun simplePOI(isSimple: Boolean) {
        Timber.d("simplePOI mInSimplePoi=$mInSimplePoi isSimple=$isSimple")
        if (mInSimplePoi == isSimple) return
        mInSimplePoi = isSimple
        val mapView = mapController.getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN) ?: return
        if (!isSimple) {
            mapView.operatorBusiness.clearCustomStyle()
        } else {
            mapView.operatorBusiness.setCustomLabelTypeVisable(
                mHideList,
                MapPoiCustomOperateType.CUSTOM_POI_OPERATE_ONLY_LIST_HIDE
            )
        }
        mapController.refresh(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    override fun onLocInfoUpdate(p0: LocInfo?) {
        p0?.run {
            mSpeed = this.speed
            if (NAVI_SIMPLE_POI && naviRepository.isNavigating()) {
                if (mSpeed >= SPEED_VALUE && !mHandler.hasCallbacks(simpleRunnable)) {
                    mHandler.postDelayed(simpleRunnable, TIME_VALUE)
                } else if (mSpeed < SPEED_VALUE) {
                    MainScope().launch { simplePOI(false) }
                    if (mHandler.hasCallbacks(simpleRunnable))
                        mHandler.removeCallbacks(simpleRunnable)
                }
            }
        }
    }

    private val cruiseStatusOb = Observer<Boolean> {
        Timber.i("cruiseStatusOb simplePOI $it")
        if (CRUISE_SIMPLE_POI)
            simplePOI(it)
    }

    private val naviStatusOb = Observer<Int> {
        if (NAVI_SIMPLE_POI) {
            when (it) {
                BaseConstant.NAVI_STATE_STOP_REAL_NAVI, BaseConstant.NAVI_STATE_STOP_SIM_NAVI -> {
                    Timber.i("naviStatusOb simplePOI false")
                    simplePOI(false)
                }

                BaseConstant.NAVI_STATE_REAL_NAVING, BaseConstant.NAVI_STATE_SIM_NAVING -> {
                    Timber.i("naviStatusOb simplePOI true")
                    simplePOI(true)
                }
            }
        }
    }

    /**
     * 手势识别
     */
    private val mMapGestureObserver = object : MapGestureObserver() {
        override fun onMotionEvent(engineId: Long, action: Int, px: Long, py: Long) {
            super.onMotionEvent(engineId, action, px, py)
            //Timber.d("MapGestureObserver.onMotionEvent() engineId: $engineId, action: $action, px: $px, py:$py")
            if (MotionEvent.ACTION_DOWN == action) {
                if (naviRepository.isNavigating())
                    simplePOI(false)
            }
        }

        override fun onMoveBegin(l: Long, l1: Long, l2: Long) {
            super.onMoveBegin(l, l1, l2)
            //Timber.d("MapGestureObserver.onMoveBegin() $l, $l1, $l2")
            if (naviRepository.isNavigating())
                simplePOI(false)
        }

        override fun onSinglePress(l: Long, l1: Long, l2: Long, b: Boolean): Boolean {
            //Timber.d("MapGestureObserver.onSinglePress() $l, $l1, $l2")
            if (naviRepository.isNavigating())
                simplePOI(false)
            return super.onSinglePress(l, l1, l2, b)
        }

        override fun onLongPress(engineId: Long, px: Long, py: Long) {
            super.onLongPress(engineId, px, py)
            Timber.d("MapGestureObserver.onLongPress() $engineId, $px, $py")
            if (naviRepository.isNavigating())
                simplePOI(false)
        }
    }


    fun init() {
        Timber.i("init")
        LocationController.getInstance().addLocInfoObserver(this)
        cruiseBusiness.cruiseStatus.unPeek().observeForever(cruiseStatusOb)
        naviBusiness.naviStatus.unPeek().observeForever(naviStatusOb)
        mapController.addGestureObserver(SurfaceViewID.SURFACE_VIEW_ID_MAIN, mMapGestureObserver)
    }

    fun unInit() {
        Timber.i("unInit")
        LocationController.getInstance().removeLocInfoObserver(this)
        cruiseBusiness.cruiseStatus.removeObserver(cruiseStatusOb)
        naviBusiness.naviStatus.removeObserver(naviStatusOb)
        mapController.removeGestureObserver(SurfaceViewID.SURFACE_VIEW_ID_MAIN, mMapGestureObserver)
    }

}