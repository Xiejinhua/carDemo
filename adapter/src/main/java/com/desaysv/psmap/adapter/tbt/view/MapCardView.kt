package com.desaysv.psmap.adapter.tbt.view

import android.content.Context
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.desaysv.psmap.adapter.R
import com.desaysv.psmap.adapter.tbt.adapter.NaviLaneListAdapter
import com.desaysv.psmap.adapter.tbt.bean.OutputNaviPanelData
import com.desaysv.psmap.adapter.tbt.utils.NaviUiUtils
import com.desaysv.psmap.adapter.tbt.utils.NaviUiUtils.getDrawableID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


/**
 * @author 谢锦华
 * @time 2025/2/19
 * @description
 */

class MapCardView : ConstraintLayout {
    private var clTop: ConstraintLayout? = null
    private var clExitInfoShow: ConstraintLayout? = null
    private var clCenter: ConstraintLayout? = null
    private var showCrossLaneCl: ConstraintLayout? = null
    private var ivTopDirection: ImageView? = null
    private var enlargeCrossImage: ImageView? = null
    private var ivCenterNearDirection: ImageView? = null
    private var tvTopDistance: TextView? = null
    private var tvTopDistanceUnit: TextView? = null
    private var tvTopRoadName: TextView? = null
    private var tvCenterNearRoadName: TextView? = null
    private var tvExitInfo: TextView? = null
    private var tvExitInfoList: TextView? = null
    private var tvRemainDistanceTime: TextView? = null
    private var tvArriveTime: TextView? = null
    private var laneRecyclerView: RecyclerView? = null
    private var showCrossLaneComposeView: RecyclerView? = null
    private var crossImageProgressBar: ProgressBar? = null


    private var naviLaneListAdapter: NaviLaneListAdapter? = null
    private var showCrossView: Boolean = false
    private var isNightMode: Boolean = false
    private var isViewInitialized = false // 标志位，确保只初始化一次
    private var nearThumTurnIconID = 0
    private var turnIconID = 0

    //协程IO线程
    private var naviScopeIo = CoroutineScope(Dispatchers.IO + Job())
    private var naviScopeMain = CoroutineScope(Dispatchers.Main + Job())

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initView()
    }


    private fun initView() {
        if (isViewInitialized) return // 如果已经初始化，直接返回
        isViewInitialized = true // 标记为已初始化
        val view: View = LayoutInflater.from(context).inflate(R.layout.view_navi_info, null)
        val layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        clTop = view.findViewById(R.id.clTop)
        ivTopDirection = view.findViewById(R.id.ivTopDirection)
        enlargeCrossImage = view.findViewById(R.id.enlargeCrossImage)
        tvTopDistance = view.findViewById(R.id.tvTopDistance)
        tvTopDistanceUnit = view.findViewById(R.id.tvTopDistanceUnit)
        tvTopRoadName = view.findViewById(R.id.tvTopRoadName)
        ivCenterNearDirection = view.findViewById(R.id.ivCenterNearDirection)
        tvCenterNearRoadName = view.findViewById(R.id.tvCenterNearRoadName)
        clExitInfoShow = view.findViewById(R.id.clExitInfoShow)
        tvExitInfo = view.findViewById(R.id.tvExitInfo)
        tvExitInfoList = view.findViewById(R.id.tvExitInfoList)
        clCenter = view.findViewById(R.id.clCenter)
        tvRemainDistanceTime = view.findViewById(R.id.tvRemainDistanceTime)
        tvArriveTime = view.findViewById(R.id.tvArriveTime)
        crossImageProgressBar = view.findViewById(R.id.crossImageProgressBar)
        showCrossLaneCl = view.findViewById(R.id.showCrossLaneCl)
        showCrossLaneComposeView = view.findViewById(R.id.showCrossLaneComposeView)
        laneRecyclerView = view.findViewById(R.id.laneComposeView)
        initNaviLaneRecyclerView()
        addView(view, layoutParams)

    }


    private fun initNaviLaneRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = RecyclerView.HORIZONTAL
        laneRecyclerView?.layoutManager = linearLayoutManager

        val linearLayoutManager2 = LinearLayoutManager(context)
        linearLayoutManager2.orientation = RecyclerView.HORIZONTAL
        showCrossLaneComposeView?.layoutManager = linearLayoutManager2

        naviLaneListAdapter = NaviLaneListAdapter(showCrossView, context)
        laneRecyclerView?.adapter = naviLaneListAdapter
        showCrossLaneComposeView?.adapter = naviLaneListAdapter
        Log.i("initNaviLaneRecyclerView", "initNaviLaneRecyclerView is called isViewInitialized=$isViewInitialized")
    }

    /**
     * 更新设置tbt数据
     */
    fun updaterTbtData(mOutputNaviPanelData: OutputNaviPanelData?) {
        mOutputNaviPanelData?.let { outputNaviPanelData ->
            naviScopeMain.launch {
                turnIconID = outputNaviPanelData.turnIconID
                if (turnIconID != -1) {
                    ivTopDirection?.setImageResource(getDrawableID(NaviUiUtils.getRoadSignBitmap(turnIconID, false), isNightMode))
                }

                tvTopDistance?.text = outputNaviPanelData.distanceNextRoute
                tvTopDistanceUnit?.text = outputNaviPanelData.distanceNextRouteUnit
                tvCenterNearRoadName?.visibility = if (outputNaviPanelData.nearThumInfoVisible) VISIBLE else GONE
                tvCenterNearRoadName?.text = outputNaviPanelData.nearRoadName

                nearThumTurnIconID = outputNaviPanelData.nearThumTurnIconID
                var nearThumDrawableId = 0
                if (nearThumTurnIconID != -1) {
                    nearThumDrawableId = getDrawableID(NaviUiUtils.getRoadSignBitmap(nearThumTurnIconID, true), isNightMode)
                    ivCenterNearDirection?.setImageResource(nearThumDrawableId)
                }
                ivCenterNearDirection?.visibility =
                    if (outputNaviPanelData.nearThumInfoVisible) if (nearThumDrawableId == 0) INVISIBLE else VISIBLE else GONE

                tvTopRoadName?.text = outputNaviPanelData.nextRouteName
                clExitInfoShow?.visibility = if (outputNaviPanelData.exitVisible) VISIBLE else INVISIBLE
                tvExitInfo?.text = outputNaviPanelData.exitNumber
                tvExitInfoList?.text = outputNaviPanelData.exitDirectionInfo
                tvRemainDistanceTime?.text = outputNaviPanelData.timeAndDistance
                tvArriveTime?.text = outputNaviPanelData.arriveTime
                laneRecyclerView?.visibility = if (!outputNaviPanelData.crossViewVisible && outputNaviPanelData.naviLaneVisible) VISIBLE else GONE
                showCrossLaneCl?.visibility = if (outputNaviPanelData.crossViewVisible && outputNaviPanelData.naviLaneVisible) VISIBLE else GONE
                enlargeCrossImage?.visibility = if (outputNaviPanelData.crossViewVisible) VISIBLE else GONE
                crossImageProgressBar?.visibility = if (outputNaviPanelData.crossViewVisible) VISIBLE else GONE
                enlargeCrossImage?.visibility = if (outputNaviPanelData.crossViewVisible) VISIBLE else GONE
                crossImageProgressBar?.progress = outputNaviPanelData.distanceNextCross
                naviLaneListAdapter?.updateData(outputNaviPanelData.naviLaneInfo, isNightMode, outputNaviPanelData.crossViewVisible)
            }
        }
    }

    /**
     * 日夜模式适配
     */
    fun setUiMode(nightMode: Boolean) {
        Log.i("initNaviLaneRecyclerView", "setUiMode is called nightMode=$nightMode")
        isNightMode = nightMode
        naviScopeMain.launch {
            if (turnIconID != -1) {
                ivTopDirection?.setImageResource(getDrawableID(NaviUiUtils.getRoadSignBitmap(turnIconID, false), isNightMode))
            }
            if (nearThumTurnIconID != -1) {
                ivCenterNearDirection?.setImageResource(getDrawableID(NaviUiUtils.getRoadSignBitmap(nearThumTurnIconID, true), isNightMode))
            }
            clTop?.setBackgroundResource(if (isNightMode) R.drawable.ic_main_bg_night else R.drawable.ic_main_bg_day)
            clCenter?.setBackgroundResource(if (isNightMode) R.drawable.shape_navi_lane_night else R.drawable.shape_navi_lane_day)
            tvTopDistance?.setTextColor(
                if (isNightMode) context.resources.getColor(R.color.onPrimaryNight) else context.resources.getColor(R.color.onPrimaryDay)
            )
            tvTopDistanceUnit?.setTextColor(
                if (isNightMode) context.resources.getColor(R.color.onSecondaryNight) else context.resources.getColor(R.color.onSecondaryDay)
            )
            tvCenterNearRoadName?.setTextColor(
                if (isNightMode) context.resources.getColor(R.color.onPrimaryNight) else context.resources.getColor(R.color.onPrimaryDay)
            )
            tvTopRoadName?.setTextColor(
                if (isNightMode) context.resources.getColor(R.color.onSecondaryNight) else context.resources.getColor(R.color.onSecondaryDay)
            )
            tvExitInfo?.setTextColor(
                if (isNightMode) context.resources.getColor(R.color.onTertiaryContainerNight) else context.resources.getColor(R.color.onTertiaryContainerDay)
            )
            tvExitInfo?.setBackgroundResource(if (isNightMode) R.drawable.shape_navi_exit_bg_night else R.drawable.shape_navi_exit_bg_day)
            tvExitInfoList?.setTextColor(
                if (isNightMode) context.resources.getColor(R.color.onSecondaryNight) else context.resources.getColor(R.color.onSecondaryDay)
            )
            tvRemainDistanceTime?.setTextColor(
                if (isNightMode) context.resources.getColor(R.color.onPrimaryNight) else context.resources.getColor(R.color.onPrimaryDay)
            )
            tvArriveTime?.setTextColor(
                if (isNightMode) context.resources.getColor(R.color.onPrimaryNight) else context.resources.getColor(R.color.onPrimaryDay)
            )
            showCrossLaneCl?.setBackgroundResource(if (isNightMode) R.drawable.shape_navi_lane_show_cross_night else R.drawable.shape_navi_lane_show_cross_day)
            naviLaneListAdapter?.updateData(isNightMode)
        }
    }

    /**
     * 设置路口大图
     */
    fun setCrossView(byteArray: ByteArray?) {
        naviScopeMain.launch {
            byteArray?.let {
                enlargeCrossImage?.setImageBitmap(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size))
            } ?: run {
                enlargeCrossImage?.setImageBitmap(null)
            }
        }
    }

}
