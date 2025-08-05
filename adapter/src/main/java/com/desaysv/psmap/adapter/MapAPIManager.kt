package com.desaysv.psmap.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.Surface
import com.desaysv.psmap.adapter.command.MapDataServiceProxy
import com.desaysv.psmap.adapter.command.MapNavigationServiceProxy
import com.desaysv.psmap.adapter.screenshot.MapScreenshotServiceProxy

@SuppressLint("StaticFieldLeak")
object MapAPIManager {
    private const val TAG = "MapAPIManager"
    private var mapScreenshotServiceProxy: MapScreenshotServiceProxy? = null
    private var mapDataServiceProxy: MapDataServiceProxy? = null
    private var mapNavigationServiceProxy: MapNavigationServiceProxy? = null

    fun initScreenshotService(context: Context) {
        Log.i(TAG, "initScreenshotService")
        mapScreenshotServiceProxy?.unbindService()
        mapScreenshotServiceProxy = MapScreenshotServiceProxy(context)
    }

    fun unInitScreenshotService() {
        Log.i(TAG, "unInitScreenshotService")
        mapScreenshotServiceProxy?.unbindService()
        mapScreenshotServiceProxy = null
    }

    fun isConnectScreenshotService(): Boolean? {
        return mapScreenshotServiceProxy?.isConnected().also {
            Log.i(TAG, "isConnectScreenshotService $it")
        }
    }

    fun registerScreenshotServiceCallback(callback: ScreenshotServiceCallback) {
        Log.i(TAG, "registerScreenshotServiceCallback")
        mapScreenshotServiceProxy?.registerScreenshotServiceCallback(callback)
    }

    fun unregisterScreenshotServiceCallback() {
        Log.i(TAG, "unregisterScreenshotServiceCallback")
        mapScreenshotServiceProxy?.unregisterScreenshotServiceCallback()
    }

    /**
     * @param aSurface 展示图层的Surface
     * @param width 宽
     * @param height 高
     * @param x 默认0 偏移量 >0向右偏移，<0向左偏移  默认传0
     * @param y 默认0 偏移量 >0向上偏移，<0向下偏移  默认传0
     */
    fun addSurface(aSurface: Surface, width: Int, height: Int, x: Int, y: Int) {
        Log.i(TAG, "addSurface is called ")
        mapScreenshotServiceProxy?.addSurface("", aSurface, width, height, x, y)
    }

    fun removedSurface() {
        Log.i(TAG, "removedSurface is called ")
        mapScreenshotServiceProxy?.removedSurface("")
    }

    /**
     * @param x 偏移量 >0向右偏移，<0向左偏移
     * @param y 默认0从主图底部截取，所以y必须大于0，向上偏移y
     */
    fun addBitmapBufferCallback(key: String, width: Int, height: Int, x: Int, y: Int) {
        Log.i(TAG, "addBitmapBufferCallback key=$key")
        mapScreenshotServiceProxy?.addBitmapBufferCallback(key, width, height, x, y)
    }

    fun removedBufferCallback(key: String) {
        Log.i(TAG, "removedBufferCallback key=$key")
        mapScreenshotServiceProxy?.removedBufferCallback(key)
    }

    interface ScreenshotServiceCallback {
        fun onServiceConnect(connected: Boolean)
        fun onMapScreenshotpBitmapBuffer(pBitmapBuffer: ByteArray?)
        fun onMapScreenshotReady()
        fun onErrorInfo(code: Int, info: String?)
    }

    /*===============================================数据透出和指令服务============================================*/

    /**
     * @param context
     */
    fun initMapDataService(context: Context) {
        Log.i(TAG, "initMapDataService")
        mapDataServiceProxy?.unbindService()
        mapDataServiceProxy = MapDataServiceProxy(context)
    }

    fun unInitMapDataService() {
        Log.i(TAG, "unInitMapDataService")
        mapDataServiceProxy?.unbindService()
        mapDataServiceProxy = null
    }

    fun isConnectMapDataService(): Boolean? {
        return mapDataServiceProxy?.isConnected().also {
            Log.i(TAG, "isConnectMapDataService $it")
        }
    }

    fun registerMapDataServiceCallback(callback: MapDataServiceCallback) {
        Log.i(TAG, "registerMapDataServiceCallback")
        mapDataServiceProxy?.registerMapDataServiceCallback(callback)
    }

    fun unregisterMapDataServiceCallback(callback: MapDataServiceCallback) {
        Log.i(TAG, "unregisterMapDataServiceCallback")
        mapDataServiceProxy?.unregisterMapDataServiceCallback(callback)
    }

    fun sendMassage(msg: String) {
        Log.i(TAG, "sendMassage")
        mapDataServiceProxy?.sendMassage(msg)
    }

    fun getNaviStatus(): Int? {
        Log.i(TAG, "getNaviStatus")
        return mapDataServiceProxy?.getNaviStatus()
    }

    interface MapDataServiceCallback {
        fun onServiceConnect(connected: Boolean)
        fun onMassage(msg: String?)
        fun onByteMassage(msg: String?, byteArray: ByteArray?)

    }


    /*===============================================智驾数据透出和指令服务============================================*/
    /**
     * 初始化智驾导航服务。
     *
     * 该方法用于初始化地图导航服务代理，并确保在重新初始化之前解绑之前的服务。
     *
     * @param context 应用程序上下文，用于绑定服务。
     */
    fun initMapNavigationService(context: Context) {
        Log.i(TAG, "initMapNavigationService")
        mapNavigationServiceProxy?.unbindService()
        mapNavigationServiceProxy = MapNavigationServiceProxy(context)
    }

    /**
     * 反初始化智驾导航服务。
     *
     * 该方法用于解绑地图导航服务代理并将其置为 null，以释放相关资源。
     */
    fun unInitMapNavigationService() {
        Log.i(TAG, "unInitMapNavigationService")
        mapNavigationServiceProxy?.unbindService()
        mapNavigationServiceProxy = null
    }

    /**
     * 检查地图导航服务是否已连接。
     *
     * 此方法通过检查 [mapNavigationServiceProxy] 的连接状态来确定地图导航服务是否已连接。
     * 如果 [mapNavigationServiceProxy] 为 null，则返回 null。
     *
     * @return 如果服务已连接，则返回 true；如果未连接，则返回 false；如果代理为 null，则返回 null。
     */
    fun isConnectMapNavigationService(): Boolean {
        return mapNavigationServiceProxy?.isConnected().also {
            Log.i(TAG, "isConnectMapNavigationService $it")
        } ?: false
    }

    /**
     * 注册地图导航服务的回调函数。
     *
     * 该方法用于将一个 [MapNavigationServiceCallback] 类型的回调函数注册到地图导航服务代理中。
     * 当地图导航服务发生特定事件时，会调用该回调函数通知调用者。
     *
     * @param callback 要注册的地图导航服务回调函数。
     */
    fun registerMapNavigationServiceCallback(callback: MapNavigationServiceCallback) {
        Log.i(TAG, "registerMapNavigationServiceCallback")
        mapNavigationServiceProxy?.registerMapNavigationServiceCallback(callback)
    }

    /**
     * 取消注册地图导航服务的回调函数。
     *
     * 该方法用于从地图导航服务代理中移除指定的回调函数，
     * 之后地图导航服务发生特定事件时，将不再调用该回调函数通知调用者。
     *
     * @param callback 需要取消注册的地图导航服务回调函数。
     */
    fun unregisterMapNavigationServiceCallback(callback: MapNavigationServiceCallback) {
        Log.i(TAG, "unregisterMapNavigationServiceCallback")
        mapNavigationServiceProxy?.unregisterMapNavigationServiceCallback(callback)
    }

    interface MapNavigationServiceCallback {

        fun onServiceConnect(connected: Boolean)

        /*fun updateDrivingAppInfo(data: ByteArray?)

        fun deleteRoute(mapID: Int)

        fun registerCallback(callback: IAutoEventCallback?)

        fun unregisterCallback()

        fun updateModStudyDistance(distance: Int)

        fun updateMapSavingProgress(progress: Float)

        fun updateModState(state: Int)

        fun updateModMapperState(state: Int)*/

        /**
         * 智驾App对外提供的智驾数据接⼝
         * 具体转换使⽤protobuf进⾏解析
         *
         * @param data ByteArray?
         */
        fun onDrivingAppInfoUpdate(data: ByteArray?)

        /**
         * 智驾App通知导航对应ID的地图被删除
         *
         * @param mapID Int
         */
        fun onRouteDelete(mapID: Int)

    }
}