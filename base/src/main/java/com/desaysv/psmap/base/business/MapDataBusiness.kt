package com.desaysv.psmap.base.business

import android.content.Context
import com.autonavi.gbl.data.model.AdminCode
import com.autonavi.gbl.data.model.Area
import com.autonavi.gbl.data.model.AreaExtraInfo
import com.autonavi.gbl.data.model.CityDownLoadItem
import com.autonavi.gbl.data.model.CityItemInfo
import com.autonavi.gbl.data.model.DownLoadMode
import com.autonavi.gbl.data.model.MapDataFileType
import com.autonavi.gbl.data.model.MapDataMode
import com.autonavi.gbl.data.model.MapNum
import com.autonavi.gbl.data.model.ProvinceInfo
import com.autonavi.gbl.data.model.TaskStatusCode
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_READY
import com.autonavi.gbl.data.model.TaskStatusCode.TaskStatusCode1
import com.autonavi.gbl.data.observer.IMapNumObserver
import com.autosdk.bussiness.data.HotUpdateController
import com.autosdk.bussiness.data.MapDataController
import com.autosdk.bussiness.data.ThemeDataController
import com.autosdk.bussiness.data.observer.IMapDataObserver
import com.autosdk.common.AutoConstant
import com.desaysv.psmap.base.utils.BaseConstant
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author 王漫生
 * @description 地图离线数据，主题，热更新业务
 */
@Singleton
class MapDataBusiness @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mapDataController: MapDataController,
    private val hotUpdateController: HotUpdateController,
    private val themeDataController: ThemeDataController
) {
    /**
     * mapData服务初始化
     */
    fun initMapData() {
        var mapDataMode = MapDataMode.MAP_DATA_MODE_BASE
//        if (AssertUtils.isFileExist(context, "blRes/EhpConfig.dat")) {
//            mapDataMode = MapDataMode.MAP_DATA_MODE_EHP_ADAS
//        }
        mapDataController.initService(AutoConstant.OFFLINE_CONF_DIR, AutoConstant.OFFLINE_DOWNLOAD_DIR, mapDataMode)
    }

    /**
     * HotUpdate服务初始化
     */
    fun initHotUpdateService(path: String) {
        hotUpdateController.initHotUpdateService(path)
    }

    /**
     * ThemeData服务初始化
     */
    fun initThemeDataService(strConfigfilePath: String, themeStrdownloadpath: String, dataVersion: String) {
        themeDataController.initService(strConfigfilePath, themeStrdownloadpath, dataVersion)
    }

    // ============================ MapDataController管理类相关 ================================

    /**
     * @return 1 成功，需等待pObserver回调初始化的结果\n
     * -  0 失败；
     * -  -1 pObserver 传入的观察者为空；
     * -  -2 config.strConfigfilePath 为空或不存在且创建失败；
     * -  -3 config.strConfigfilePath 文件夹下，不存在预置文件all_city_compile.json
     */
    fun isInitSuccess(): Boolean {
        return mapDataController.isInitSuccess
    }

    /**
     * 获取data engine版本号
     */
    fun getEngineVersion(): String? {
        return mapDataController.engineVersion
    }

    /**
     * 根据经纬度解析获取城市adcode
     * 若解析失败,则返回0
     * 调用该函数只需要获取MapDataService即可，不要求初始化成功
     */
    fun getAdCodeByLonLat(lon: Double, lat: Double): Int {
        return mapDataController.getAdcodeByLonLat(lon, lat)
    }

    /**
     * 获取指定adCode的行政区域扩展信息
     */
    fun getAreaExtraInfo(adCode: AdminCode): AreaExtraInfo? {
        return mapDataController.getAreaExtraInfo(adCode)
    }

    /**
     * 通过adCode获取城市信息
     * 若解析失败,则返回null
     */
    fun getCityInfo(adCode: Int): CityItemInfo? {
        return mapDataController.getCityInfo(adCode)
    }

    fun getCityInfo(lon: Double, lat: Double): CityItemInfo? {
        return mapDataController.let {
            it.getCityInfo(it.getAdcodeByLonLat(lon, lat))
        }
    }

    /**
     * 获取所有城市下载信息
     */
    fun getCityList(): ArrayList<CityItemInfo>? {
        return mapDataController.cityList
    }

    /**
     * 获取离线数据版本号
     */
    fun getDataFileVersion(adCode: Int, @MapDataFileType.MapDataFileType1 fileType: Int): String? {
        return mapDataController.getDataFileVersion(adCode, fileType)
    }

    /**
     * 获取所有离线数据版本信息
     */
    fun getAllDataFileVersion(adCode: Int): String? {
        return mapDataController.getAllDataFileVersion(adCode)
    }

    /**
     * 检测磁盘中是否有效数据
     *
     * @param downLoadMode 数据下载模式
     * @param path         路径, 当【downLoadMode = DOWNLOAD_MODE_USB】时，path传入已知存在U盘路径。其他模式可以传""
     * 离线地图数据下载专用接口
     * 需要等Init的bl::IDataInitObserver * pObserver初始化OnInit回调后，才能调用本接口。
     * 下载模式downLoadMode相同，多次调用本接口时，前一次的调用传入的pObserver对象地址会被下一次调用传入的替换。
     * 调用者传入的pObserver对象地址, 当执行AbortRequestDataListCheck，或者UnInit，再或者IServiceMgr.UnInitBL后，将不再被使用。
     * thread: main
     * @return int32_t          是否成功发起检测数据列表的请求
     * -  0 发起请求成功，并通过pObserver回调结果
     */
    fun checkDataInDisk(downLoadMode: Int, path: String?): Int {
        return mapDataController.checkDataInDisk(downLoadMode, path)
    }

    /**
     * 检测数据列表
     * @param downLoadMode 数据下载模式
     * @param path         路径, 当【downLoadMode = DOWNLOAD_MODE_USB】时，path传入已知存在U盘路径。其他模式可以传""
     * 离线地图数据下载专用接口
     * 需要等Init的bl::IDataInitObserver * pObserver初始化OnInit回调后，才能调用本接口。
     * 下载模式downLoadMode相同，多次调用本接口时，前一次的调用传入的pObserver对象地址会被下一次调用传入的替换。
     * 调用者传入的pObserver对象地址, 当执行AbortRequestDataListCheck，或者UnInit，再或者IServiceMgr.UnInitBL后，将不再被使用。
     * thread: main
     * @return int32_t          是否成功发起检测数据列表的请求
     * -  1 发起请求成功，并通过pObserver回调结果
     * -  0 发起请求失败
     * -  -1 pObserver 观察者为空
     * -  -2 当downLoadMode = DOWNLOAD_MODE_USB（U盘升级）时，U盘路径path 为空值.
     */
    fun requestDataListCheck(downLoadMode: Int, path: String?): Int {
        return mapDataController.requestDataListCheck(downLoadMode, path)
    }

    /**
     * 终止数据列表检测
     */
    fun abortDataListCheck(@DownLoadMode.DownLoadMode1 downloadMode: Int) {
        mapDataController.abortDataListCheck(downloadMode)
    }

    /**
     * 根据省市类型获取id集合
     */
    fun getAdCodeList(downLoadMode: Int, areaType: Int): ArrayList<Int>? {
        return mapDataController.getAdcodeList(downLoadMode, areaType)
    }

    /**
     * 通过adCode获取城市下载项信息
     * 离线地图数据下载专用接口
     * 下载模式downLoadMode=DOWNLOAD_MODE_NET时，需要等【首次】RequestDataListCheck请求的观察者监听pObserver回调OnRequestDataListCheck后调用。
     * 下载模式downLoadMode=DOWNLOAD_MODE_USB时，需要每次等RequestDataListCheck请求的观察者监听pObserver回调OnRequestDataListCheck后调用。
     * @param downLoadMode 数据下载模式
     * @param adCode       城市行政编码
     */
    fun getCityDownLoadItem(downLoadMode: Int, adCode: Int): CityDownLoadItem? {
        return mapDataController.getCityDownLoadItem(downLoadMode, adCode)
    }

    /**
     * 通过adCode获取省份信息
     */
    fun getProvinceInfo(adCode: Int): ProvinceInfo? {
        return mapDataController.getProvinceInfo(adCode)
    }

    /**
     * 获取省份信息列表
     */
    fun getProvinceInfoList(): java.util.ArrayList<ProvinceInfo>? {
        return mapDataController.provinceInfoList
    }

    /**
     * 通过adCode获省市取区域信息
     */
    fun getArea(@DownLoadMode.DownLoadMode1 downloadMode: Int, adCode: Int): Area? {
        return mapDataController.getArea(downloadMode, adCode)
    }

    /**
     * 下载请先检查网络
     * @param downLoadMode
     * @param opType
     * @param adCodeDiyLst
     */
    fun operate(downLoadMode: Int, opType: Int, adCodeDiyLst: ArrayList<Int>?) {
        mapDataController.operate(downLoadMode, opType, adCodeDiyLst)
    }

    /**
     * 取得等待中、下载中、暂停、解压中、重试状态下的所有城市adCode列表
     * 离线地图数据下载专用接口
     * 下载模式downLoadMode=DOWNLOAD_MODE_NET时，需要等【首次】RequestDataListCheck请求的观察者监听pObserver回调OnRequestDataListCheck后调用。
     * 下载模式downLoadMode=DOWNLOAD_MODE_USB时，需要每次等RequestDataListCheck请求的观察者监听pObserver回调OnRequestDataListCheck后调用。
     * @param downLoadMode 数据下载模式
     */
    fun getWorkingQueueAdCodeList(downLoadMode: Int): ArrayList<Int>? {
        return mapDataController.getWorkingQueueAdcodeList(downLoadMode)
    }

    /**
     * 取消所有下载中的任务
     * 主要是页面销毁前先中止未完成的task,否则ANR
     * @param adCodeList 要取消下载的adCode列表,若为null,则会取消所有未完成的task
     */
    fun cancelAllTask(@DownLoadMode.DownLoadMode1 downloadMode: Int, adCodeList: ArrayList<Int>?) {
        mapDataController.cancelAllTask(downloadMode, adCodeList)
    }

    /**
     * 暂停所有下载中的任务
     * @param adCodeList 暂停下载操作 ， adCodeList为空时暂停当前进行中的adCodeList
     */
    fun pauseAllTask(@DownLoadMode.DownLoadMode1 downloadMode: Int, adCodeList: ArrayList<Int>?) {
        mapDataController.pauseAllTask(downloadMode, adCodeList)
    }

    /**
     * 开始所有下载中的任务
     * @param adCodeList 继续下载操作 ， adCodeList为空时继续当前暂停中待继续的adCodeList
     */
    fun startAllTask(@DownLoadMode.DownLoadMode1 downloadMode: Int, adCodeList: ArrayList<Int>?) {
        mapDataController.startAllTask(downloadMode, adCodeList)
    }

    fun registerMapDataObserver(lMapDataObserver: IMapDataObserver) {
        mapDataController.registerMapDataObserver(lMapDataObserver)
    }

    fun unregisterMapDataObserver() {
        mapDataController.unregisterMapDataObserver()
    }

    fun searchAdCode(name: String?): ArrayList<Int>? {
        return mapDataController.searchAdcode(name)
    }

    /**
     * 匹配省份任一子城市离线数据的下载状态
     * @param adCode 省份行政编码
     * @param state 状态
     * @return
     */

    fun findDownLoadItemAnyTaskState(adCode: Int, @TaskStatusCode1 vararg state: Int): Boolean {
        return mapDataController.findDownLoadItemAnyTaskState(adCode, *state) // 使用 * 运算符展开数组
    }

    //全省大小
    fun getProvinceFullZipSize(adCode: Int): Long {
        return mapDataController.getProvinceFullZipSize(adCode)
    }

    // ============================ HotUpdateController管理类相关 ================================
    /**
     * 审图号Observer
     */
    fun setMapNumObserver(mapNumObserver: IMapNumObserver?) {
        hotUpdateController.setMapNumObserver(mapNumObserver)
    }

    //移除监听
    fun removeMapNumObserver(mapNumObserver: IMapNumObserver?) {
        hotUpdateController.removeMapNumObserver(mapNumObserver)
    }

    /**
     * 中断网络请求aos审图号信息
     */
    fun abortRequestMapNum() {
        hotUpdateController.abortRequestMapNum()
    }

    /**
     * 请求审图号信息
     */
    fun requestMapNum(localMapNum: MapNum): Int {
        return hotUpdateController.requestMapNum(localMapNum)
    }

    // ============================ 其他 ================================
    //获取下载状态，方便进行全部下载，全部暂停，全部删除功能
    fun getTaskStatusCode(cityItemInfos: ArrayList<CityItemInfo>?): Int {
        var taskStatusCodeReady = 0 //全新未下载标志
        var taskStatusCodeReadyUpdate = 0 //更新标志
        var hasDownLoad = 0
        var hasOneDownLoad = false //true.有暂停的任务 false.都是下载或者完成的
        var taskStatusCodeProgress = 0 //用于判断是否在任务中，比如下载，等待中
        cityItemInfos?.run {
            for (i in 0 until cityItemInfos.size) {
                val cityDownLoadItem = getCityDownLoadItem(DownLoadMode.DOWNLOAD_MODE_NET, cityItemInfos[i].cityAdcode)
                if (cityDownLoadItem != null) {
                    if (cityDownLoadItem.taskState != TaskStatusCode.TASK_STATUS_CODE_SUCCESS) {
                        hasDownLoad++
                    }
                    if (cityDownLoadItem.taskState == TaskStatusCode.TASK_STATUS_CODE_READY) {
                        if (cityDownLoadItem.bUpdate) {
                            taskStatusCodeReadyUpdate += 1
                        } else {
                            taskStatusCodeReady += 1
                        }
                    } else if (cityDownLoadItem.taskState != TaskStatusCode.TASK_STATUS_CODE_READY || cityDownLoadItem.taskState != TaskStatusCode.TASK_STATUS_CODE_PAUSE ||
                        cityDownLoadItem.taskState != TaskStatusCode.TASK_STATUS_CODE_SUCCESS || cityDownLoadItem.taskState != TaskStatusCode.TASK_STATUS_CODE_ERR
                    ) {
                        taskStatusCodeProgress += 1
                    }
                }
            }
            for (i in 0 until cityItemInfos.size) {
                val cityDownLoadItem = getCityDownLoadItem(DownLoadMode.DOWNLOAD_MODE_NET, cityItemInfos[i].cityAdcode)
                if (cityDownLoadItem != null) {
                    if (provinceItemStatus(cityDownLoadItem) == BaseConstant.OFFLINE_STATE_DOWNLOAD) {
                        hasOneDownLoad = true
                        break
                    }
                } else {
                    hasOneDownLoad = false
                }
            }
            return if (taskStatusCodeReady + taskStatusCodeReadyUpdate == cityItemInfos.size) {
                if (taskStatusCodeReadyUpdate == cityItemInfos.size) {
                    BaseConstant.OFFLINE_STATE_UPDATE //更新状态
                } else {
                    BaseConstant.OFFLINE_STATE_T0_DOWNLOAD //全新未下载状态
                }
            } else {
                if (hasDownLoad != 0) {
                    if (taskStatusCodeProgress == cityItemInfos.size) {
                        if (hasOneDownLoad) {
                            BaseConstant.OFFLINE_STATE_PAUSE //暂停状态
                        } else {
                            BaseConstant.OFFLINE_STATE_DOWNLOAD //下载状态
                        }
                    } else {
                        BaseConstant.OFFLINE_STATE_PAUSE //暂停状态
                    }
                } else {
                    BaseConstant.OFFLINE_STATE_COMPLETE //完成状态
                }
            }
        }
        return BaseConstant.OFFLINE_STATE_T0_DOWNLOAD //全新未下载状态
    }

    private fun provinceItemStatus(cityDownLoadItem: CityDownLoadItem?): Int { //1.下载 2.暂停 3.完成
        when (cityDownLoadItem!!.taskState) {
            TaskStatusCode.TASK_STATUS_CODE_ERR -> return BaseConstant.OFFLINE_STATE_DOWNLOAD
            TaskStatusCode.TASK_STATUS_CODE_READY -> return if (cityDownLoadItem.bUpdate) {
                BaseConstant.OFFLINE_STATE_COMPLETE
            } else {
                BaseConstant.OFFLINE_STATE_DOWNLOAD
            }

            TaskStatusCode.TASK_STATUS_CODE_WAITING -> return BaseConstant.OFFLINE_STATE_PAUSE
            TaskStatusCode.TASK_STATUS_CODE_DOING -> return BaseConstant.OFFLINE_STATE_PAUSE
            TaskStatusCode.TASK_STATUS_CODE_PAUSE -> return BaseConstant.OFFLINE_STATE_DOWNLOAD
            TaskStatusCode.TASK_STATUS_CODE_CHECKING -> return BaseConstant.OFFLINE_STATE_PAUSE
            TaskStatusCode.TASK_STATUS_CODE_UNZIPPING -> return BaseConstant.OFFLINE_STATE_PAUSE
            TaskStatusCode.TASK_STATUS_CODE_UNZIPPED -> return BaseConstant.OFFLINE_STATE_PAUSE
            TaskStatusCode.TASK_STATUS_CODE_SUCCESS -> return BaseConstant.OFFLINE_STATE_COMPLETE
            else -> return BaseConstant.OFFLINE_STATE_DOWNLOAD
        }
    }

    fun hasNewVersion(cityItemInfos: ArrayList<CityItemInfo>): Boolean {
        var hasNewVersion = false
        for (i in 0 until cityItemInfos.size) {
            val cityDownLoadItem = getCityDownLoadItem(DownLoadMode.DOWNLOAD_MODE_NET, cityItemInfos[i].cityAdcode)
            if (cityDownLoadItem != null && (cityDownLoadItem.bUpdate) && cityDownLoadItem.taskState == TASK_STATUS_CODE_READY) {
                hasNewVersion = true
                break
            }
        }
        return hasNewVersion
    }
}