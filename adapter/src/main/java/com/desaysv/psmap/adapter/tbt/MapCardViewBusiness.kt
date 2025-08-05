package com.desaysv.psmap.adapter.tbt

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.desaysv.psmap.adapter.MapAPIManager
import com.desaysv.psmap.adapter.R
import com.desaysv.psmap.adapter.command.MassageType
import com.desaysv.psmap.adapter.command.MassageType.ON_NAVI_PANEL_INFO
import com.desaysv.psmap.adapter.tbt.bean.OutputNaviPanelData
import com.desaysv.psmap.adapter.tbt.view.MapCardView
import com.google.gson.GsonBuilder
import org.json.JSONObject

/**
 * @author 谢锦华
 * @time 2025/2/19
 * @description TBT卡片管理类
 */


class MapCardViewBusiness private constructor(context: Context) {
    private val TAG = "MapTBTCardViewBusiness"
    private var mContext = context
    private var mapCardView: View? = null
    private var tbtMapCardView: MapCardView? = null
    private val gson = GsonBuilder().serializeSpecialFloatingPointValues().create()
    private var isViewInitialized = false // 标志位，确保只初始化一次

    companion object {

        @Volatile
        private var instance: MapCardViewBusiness? = null

        fun getInstance(context: Context): MapCardViewBusiness {
            return instance ?: synchronized(this) {
                instance ?: MapCardViewBusiness(context).also { instance = it }
            }
        }
    }

    /**
     * 初始化地图卡片
     */
    fun init() {
        Log.i(TAG, " init is called isViewInitialized=$isViewInitialized")
        if (isViewInitialized) return // 如果已经初始化，直接返回
        isViewInitialized = true // 标记为已初始化
        // 初始化布局
        initMapCardLayout()
        MapAPIManager.registerMapDataServiceCallback(mapDataServiceCallback)//监听地图数据
        val json = JSONObject().also { it.put("massageType", MassageType.DAY_AND_NIGHT_MODE_STATUS.name) }.toString()
        Log.i(TAG, "DAY_AND_NIGHT_MODE_STATUS is: json = $json")
        MapAPIManager.sendMassage(json)
    }

    fun unInit() {
        Log.i(TAG, " unInit is called")
        MapAPIManager.unregisterMapDataServiceCallback(mapDataServiceCallback)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initMapCardLayout() {
        //地图图层
        mapCardView = LayoutInflater.from(mContext).inflate(R.layout.map_card_view, null)
        tbtMapCardView = mapCardView?.findViewById(R.id.tbtMapCardView)
    }

    private val mapDataServiceCallback = object : MapAPIManager.MapDataServiceCallback {
        override fun onServiceConnect(connected: Boolean) {
        }

        override fun onMassage(json: String?) {
            val msg = JSONObject(json)
            val messageType = msg.getString("massageType")
            when (messageType) {
                ON_NAVI_PANEL_INFO.name -> {
                    Log.d(TAG, "MapCardViewBusiness onMassage() called with: json = $json")
                    val panelData = gson.fromJson(msg.getJSONObject("data").toString(), OutputNaviPanelData::class.java)
                    tbtMapCardView?.updaterTbtData(panelData)
                }

                MassageType.ON_DAY_AND_NIGHT_MODE_STATUS.name -> {
                    Log.d(TAG, "MapCardViewBusiness onMassage() called with: json = $json")
                    tbtMapCardView?.setUiMode(msg.getBoolean("data"))
                }
            }
        }

        override fun onByteMassage(msg: String?, byteArray: ByteArray?) {
            Log.d(TAG, "MapCardViewBusiness onByteMassage() called")
            tbtMapCardView?.setCrossView(byteArray)
        }

    }


    fun getNaviTBTCardView(): View? {
        if (mapCardView != null) {
            Log.i(TAG, " getNaviTBTCardView() success")
            return mapCardView
        } else {
            Log.i(TAG, " getNaviTBTCardView() View is Empty, Reset Layout")
            //初始化地图卡片布局
            initMapCardLayout()

            return mapCardView
        }
    }

}

