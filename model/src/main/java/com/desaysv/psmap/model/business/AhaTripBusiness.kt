package com.desaysv.psmap.model.business

import android.app.Application
import android.os.Handler
import androidx.lifecycle.MutableLiveData
import com.autonavi.gbl.common.model.Coord2DDouble
import com.autonavi.gbl.common.model.Coord3DDouble
import com.autonavi.gbl.data.model.CityItemInfo
import com.autonavi.gbl.layer.CustomPointLayerItem
import com.autonavi.gbl.layer.model.BizCustomLineInfo
import com.autonavi.gbl.layer.model.BizCustomTypeLine
import com.autonavi.gbl.layer.model.BizCustomTypePoint
import com.autonavi.gbl.map.MapView
import com.autonavi.gbl.map.layer.BaseLayer
import com.autonavi.gbl.map.layer.LayerItem
import com.autonavi.gbl.map.layer.model.ClickViewIdInfo
import com.autonavi.gbl.map.layer.observer.ILayerClickObserver
import com.autonavi.gbl.map.model.PreviewParam
import com.autonavi.gbl.search.model.SearchPoiBasicInfo
import com.autonavi.gbl.search.model.SearchPoiInfo
import com.autosdk.bussiness.layer.CustomLayer
import com.autosdk.bussiness.layer.LayerController
import com.autosdk.bussiness.map.MapController
import com.autosdk.bussiness.map.SurfaceViewID
import com.autosdk.bussiness.widget.search.util.SearchMapUtil
import com.desaysv.psmap.base.R
import com.desaysv.psmap.base.business.CruiseBusiness
import com.desaysv.psmap.base.business.InitSDKBusiness
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.model.bean.GuideData
import com.desaysv.psmap.model.bean.MineGuideList
import com.desaysv.psmap.model.bean.MineTankList
import com.desaysv.psmap.model.bean.ScenicDetailBean
import com.desaysv.psmap.model.bean.ScenicSectorBean
import com.desaysv.psmap.model.bean.TankCollectItem
import com.desaysv.psmap.model.bean.TankData
import com.example.aha_api_sdkd01.manger.models.LineDetailModel
import com.example.aha_api_sdkd01.manger.models.LineListModel
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 路书业务类
 */
@Singleton
class AhaTripBusiness @Inject constructor(
    private val application: Application,
    private val initSDKBusiness: InitSDKBusiness,
    private val mapBusiness: MapBusiness,
    private val cruiseBusiness: CruiseBusiness,
    private val navigationSettingBusiness: NavigationSettingBusiness,
    private val mapController: MapController,
    private val layerController: LayerController,
    private val mMapController: MapController,
    private val iCarInfoProxy: ICarInfoProxy) {
    private var currentCity: CityItemInfo? = null
    val setToast = MutableLiveData<String>() //toast
    val lineList = MutableLiveData<List<LineListModel.DataDTO.ListDTO>>() //路书列表数据
    val lineAllList = MutableLiveData<List<LineListModel.DataDTO.ListDTO>>() //路书列表总数据
    val lineLoading = MutableLiveData(false) //路书加载loading
    var lineMaxPage = MutableLiveData(1) //路书列表总页码
    val lineDetail = MutableLiveData<LineDetailModel.DataDTO>() //路书详情数据
    val lineDetailLoading = MutableLiveData(false) //路书详情加载loading
    val isLineFav = MutableLiveData(false) //路书是否已经收藏
    val isLineFavChange = MutableLiveData<Boolean>() //路书是否已经收藏--变化
    val scenicDetail = MutableLiveData<ScenicDetailBean>(null) //景点详情数据
    val scenicDetailLoading = MutableLiveData(false) //路书详情加载loading
    val requestScenicSector = MutableLiveData<Boolean>() //巡航中开始请求景点播报
    val scenicSectorData = MutableLiveData<ScenicSectorBean>() //巡航中开始请求景点播报
    val guideList = MutableLiveData<List<MineGuideList>>() //共创路书列表数据
    val guideAllList = MutableLiveData<List<MineGuideList>>() //共创路书列表总数据
    val guideLoading = MutableLiveData(false) //共创路书加载loading
    var guideMaxPage = MutableLiveData(1) //共创路书列表总页码
    val deleteGuideResult = MutableLiveData<String>() //共创路书删除结果
    val guideDetail = MutableLiveData<GuideData>() //共创路书详情数据
    val guideDetailLoading = MutableLiveData(false) //共创路书详情加载loading
    val isGuideFav = MutableLiveData(false) //共创路书是否已经收藏
    val isGuideFavChange = MutableLiveData<Boolean>() //共创路书是否已经收藏--变化
    val tankList = MutableLiveData<List<MineTankList>>() //轨迹路书列表数据
    val tankAllList = MutableLiveData<List<MineTankList>>() //轨迹路书列表总数据
    val tankLoading = MutableLiveData(false) //轨迹路书加载loading
    var tankMaxPage = MutableLiveData(1) //轨迹路书列表总页码
    val deleteTankResult = MutableLiveData<String>() //轨迹路书删除结果
    val tankDetail = MutableLiveData<TankData>() //轨迹路书详情数据
    val tankDetailLoading = MutableLiveData(false) //轨迹路书详情加载loading
    val isTankFav = MutableLiveData(false) //轨迹路书是否已经收藏
    val isTankFavChange = MutableLiveData<Boolean>() //轨迹路书是否已经收藏--变化
    val guideCollectList = MutableLiveData<List<MineGuideList>>() //收藏共创路书列表数据
    val guideCollectAllList = MutableLiveData<List<MineGuideList>>() //收藏共创路书列表总数据
    val guideCollectLoading = MutableLiveData(false) //收藏共创路书加载loading
    var guideCollectMaxPage = MutableLiveData(1) //收藏共创路书列表总页码
    val deleteGuideCollectResult = MutableLiveData<String>() //收藏共创路书删除结果
    val lineCollectList = MutableLiveData<List<LineListModel.DataDTO.ListDTO>>() //精选路书列表数据
    val lineCollectAllList = MutableLiveData<List<LineListModel.DataDTO.ListDTO>>() //收藏精选路书列表总数据
    val lineCollectLoading = MutableLiveData(false) //精选路书加载loading
    var lineCollectMaxPage = MutableLiveData(1) //收藏精选路书列表总页码
    val deleteLineCollectResult = MutableLiveData<String>() //收藏精选路书删除结果
    val tankCollectList = MutableLiveData<List<TankCollectItem>>() //收藏轨迹路书列表数据
    val tankCollectAllList = MutableLiveData<List<TankCollectItem>>() //收藏轨迹路书列表总数据
    val tankCollectLoading = MutableLiveData(false) //收藏轨迹路书加载loading
    var tankCollectMaxPage = MutableLiveData(1) //收藏轨迹路书列表总页码
    val deleteTankCollectResult = MutableLiveData<String>() //收藏轨迹路书删除结果
    val gotoAhaScenicDetail = MutableLiveData<String>() //进入景点详情页
    val gotoAhaDayDetail = MutableLiveData<Int>() //进入对应天详情
    var mScenicSectorHandler: Handler = Handler()

    private val customLayer: CustomLayer by lazy {
        layerController.getCustomLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    //主图
    private val mainMapView: MapView? by lazy {
        mapController.getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    //设置城市信息
    fun setCityItemInfo(currentCity: CityItemInfo) {
        this.currentCity = currentCity
    }

    //获取城市信息
    fun getCityItemInfo(): CityItemInfo?{
        return currentCity
    }

    fun setIsGuideFav(isGuideFav: Boolean){
        this.isGuideFav.postValue(isGuideFav)
    }

    fun setIsLineFav(isLineFav: Boolean){
        this.isLineFav.postValue(isLineFav)
    }

    fun setIsTankFav(isTankFav: Boolean){
        this.isTankFav.postValue(isTankFav)
    }

    //路书简介图层扎标和画线
    fun setLinePointLine(){
        removeAllLayerItems(BizCustomTypeLine.BizCustomTypeLine3.toLong())
        removeAllLayerItems(BizCustomTypePoint.BizCustomTypePoint2.toLong())
        removeAllLayerItems(BizCustomTypePoint.BizCustomTypePoint3.toLong())
        val customLines = ArrayList<BizCustomLineInfo>()
        val points = ArrayList<Coord3DDouble>()
        val customLineInfo = BizCustomLineInfo()
        val searchPoiInfos = ArrayList<SearchPoiInfo>()
        val lineData = lineDetail.value?.lineData
        if (lineData != null && lineData.size > 0){
            lineData.forEachIndexed {index, data ->
                if (data.node != null && data.node.size > 0){
                    points.add(Coord3DDouble(data.node[0].lng, data.node[0].lat, 0.0))
                    searchPoiInfos.add(SearchPoiInfo().apply {
                        basicInfo = SearchPoiBasicInfo().apply {
                            location = Coord2DDouble(data.node[0].lng, data.node[0].lat)
                            poiId = "${data.node[0].nodeId}|0" // 0.路书 1.共创路书
                            name = data.dayId.toString()
                            adcode = index
                        }
                    })
                }
            }
        }
        if (points.size > 1){
            customLineInfo.mVecPoints = points
            customLines.add(customLineInfo)
            customLayer.showAhaCustomLineLayer(customLines) //显示路书详情扎标画线
        }
        if (searchPoiInfos.size == 1){
            customLayer.showAhaLineDayPoint(searchPoiInfos) //简介图层扎标
            mapBusiness.setMapCenter(searchPoiInfos[0].basicInfo.location.lon, searchPoiInfos[0].basicInfo.location.lat)
        }else if (searchPoiInfos.size > 1){
            customLayer.showAhaLineDayPoint(searchPoiInfos) //简介图层扎标
            showPreview(points)
        }
    }

    //共创路书简介图层扎标和画线
    fun setGuidePointLine(){
        removeAllLayerItems(BizCustomTypeLine.BizCustomTypeLine3.toLong())
        removeAllLayerItems(BizCustomTypePoint.BizCustomTypePoint2.toLong())
        removeAllLayerItems(BizCustomTypePoint.BizCustomTypePoint3.toLong())
        val customLines = ArrayList<BizCustomLineInfo>()
        val points = ArrayList<Coord3DDouble>()
        val customLineInfo = BizCustomLineInfo()
        val searchPoiInfos = ArrayList<SearchPoiInfo>()
        val nodeList = guideDetail.value?.nodeList
        if (!nodeList.isNullOrEmpty()){
            nodeList.forEachIndexed { index, data ->
                data.node?.let {
                    points.add(Coord3DDouble(it[0].lng, it[0].lat, 0.0))
                    searchPoiInfos.add(SearchPoiInfo().apply {
                        basicInfo = SearchPoiBasicInfo().apply {
                            location = Coord2DDouble(it[0].lng, it[0].lat)
                            poiId = "${it[0].nodeId}|1" // 0.路书 1.共创路书
                            name = data.dayId.toString()
                            adcode = index
                        }
                    })
                }
            }
        }
        if (points.size > 1){
            customLineInfo.mVecPoints = points
            customLines.add(customLineInfo)
            customLayer.showAhaCustomLineLayer(customLines) //显示路书详情扎标画线
        }
        if (searchPoiInfos.size == 1){
            customLayer.showAhaLineDayPoint(searchPoiInfos) //简介图层扎标
            mapBusiness.setMapCenter(searchPoiInfos[0].basicInfo.location.lon, searchPoiInfos[0].basicInfo.location.lat)
        }else if (searchPoiInfos.size > 1){
            customLayer.showAhaLineDayPoint(searchPoiInfos) //简介图层扎标
            showPreview(points)
        }
    }

    //轨迹详情图层扎标和画线
    fun setTankNodePointLine(){
        removeAllLayerItems(BizCustomTypeLine.BizCustomTypeLine3.toLong())
        removeAllLayerItems(BizCustomTypePoint.BizCustomTypePoint2.toLong())
        removeAllLayerItems(BizCustomTypePoint.BizCustomTypePoint3.toLong())
        val customLines = ArrayList<BizCustomLineInfo>()
        val points = ArrayList<Coord3DDouble>()
        val customLineInfo = BizCustomLineInfo()
        val searchPoiInfos = ArrayList<SearchPoiInfo>()
        val markers = tankDetail.value?.markers
        if (!markers.isNullOrEmpty()){
            markers.forEachIndexed { index, data ->
                points.add(Coord3DDouble(data.lng, data.lat, 0.0))
                searchPoiInfos.add(SearchPoiInfo().apply {
                    basicInfo = SearchPoiBasicInfo().apply {
                        location = Coord2DDouble(data.lng, data.lat)
                        poiId = index.toString()
                        poiId = "$index|3" // 1.路书 2.共创路书 3.轨迹路书 4.景点详情
                        name = data.caption
                        adcode = -1
                    }
                })
            }
        }
        if (points.size > 1){
            customLineInfo.mVecPoints = points
            customLines.add(customLineInfo)
            customLayer.showAhaCustomLineLayer(customLines) //显示路书详情扎标画线
        }
        if (searchPoiInfos.size == 1){
            customLayer.showAhaLineDayNodePoint(searchPoiInfos) //简介图层扎标
            mapBusiness.setMapCenter(searchPoiInfos[0].basicInfo.location.lon, searchPoiInfos[0].basicInfo.location.lat)
        }else if (searchPoiInfos.size > 1){
            customLayer.showAhaLineDayNodePoint(searchPoiInfos) //简介图层扎标
            showPreview(points)
        }
    }

    //DAY几图层扎标和画线
    fun setLineDayNodePointLine(selectTab: Int){
        if (selectTab > 0){
            removeAllLayerItems(BizCustomTypeLine.BizCustomTypeLine3.toLong())
            removeAllLayerItems(BizCustomTypePoint.BizCustomTypePoint2.toLong())
            removeAllLayerItems(BizCustomTypePoint.BizCustomTypePoint3.toLong())
            val customLines = ArrayList<BizCustomLineInfo>()
            val points = ArrayList<Coord3DDouble>()
            val customLineInfo = BizCustomLineInfo()
            val searchPoiInfos = ArrayList<SearchPoiInfo>()
            val lineData = lineDetail.value?.lineData
            val index = selectTab -1
            if (lineData != null && lineData.size > 0 && index < lineData.size && lineData[index] != null){
                val node = lineData[index].node
                if (node != null && node.size > 0){
                    node.forEachIndexed { num, data ->
                        points.add(Coord3DDouble(data.lng, data.lat, 0.0))
                        searchPoiInfos.add(SearchPoiInfo().apply {
                            basicInfo = SearchPoiBasicInfo().apply {
                                location = Coord2DDouble(data.lng, data.lat)
                                poiId = "$num|1" // 1.路书 2.共创路书 3.轨迹路书 4.景点详情
                                name = data.caption
                                adcode = index
                            }
                        })
                    }
                }
            }
            if (points.size > 1){
                customLineInfo.mVecPoints = points
                customLines.add(customLineInfo)
                customLayer.showAhaCustomLineLayer(customLines) //显示路书详情扎标画线
            }
            if (searchPoiInfos.size == 1){
                customLayer.showAhaLineDayNodePoint(searchPoiInfos) //简介图层扎标
                mapBusiness.setMapCenter(searchPoiInfos[0].basicInfo.location.lon, searchPoiInfos[0].basicInfo.location.lat)
            }else if (searchPoiInfos.size > 1){
                customLayer.showAhaLineDayNodePoint(searchPoiInfos) //简介图层扎标
                showPreview(points)
            }
        }
    }

    //共创路书DAY几图层扎标和画线
    fun setGuideDayNodePointLine(selectTab: Int){
        if (selectTab > 0){
            removeAllLayerItems(BizCustomTypeLine.BizCustomTypeLine3.toLong())
            removeAllLayerItems(BizCustomTypePoint.BizCustomTypePoint2.toLong())
            removeAllLayerItems(BizCustomTypePoint.BizCustomTypePoint3.toLong())
            val customLines = ArrayList<BizCustomLineInfo>()
            val points = ArrayList<Coord3DDouble>()
            val customLineInfo = BizCustomLineInfo()
            val searchPoiInfos = ArrayList<SearchPoiInfo>()
            val nodeList = guideDetail.value?.nodeList
            val index = selectTab -1
            if (!nodeList.isNullOrEmpty() && index < nodeList.size && nodeList[index] != null){
                val node = nodeList[index].node
                if (!node.isNullOrEmpty()){
                    node.forEachIndexed { num, data ->
                        points.add(Coord3DDouble(data.lng, data.lat, 0.0))
                        searchPoiInfos.add(SearchPoiInfo().apply {
                            basicInfo = SearchPoiBasicInfo().apply {
                                location = Coord2DDouble(data.lng, data.lat)
                                poiId = "$num|2" // 1.路书 2.共创路书 3.轨迹路书 4.景点详情
                                name = data.caption
                                adcode = index
                            }
                        })
                    }
                }
            }
            if (points.size > 1){
                customLineInfo.mVecPoints = points
                customLines.add(customLineInfo)
                customLayer.showAhaCustomLineLayer(customLines) //显示路书详情扎标画线
            }
            if (searchPoiInfos.size == 1){
                customLayer.showAhaLineDayNodePoint(searchPoiInfos) //简介图层扎标
                mapBusiness.setMapCenter(searchPoiInfos[0].basicInfo.location.lon, searchPoiInfos[0].basicInfo.location.lat)
            }else if (searchPoiInfos.size > 1){
                customLayer.showAhaLineDayNodePoint(searchPoiInfos) //简介图层扎标
                showPreview(points)
            }
        }
    }

    //景点详情扎点
    fun setScenicDetailPoint(){
        removeAllLayerItems(BizCustomTypeLine.BizCustomTypeLine3.toLong())
        removeAllLayerItems(BizCustomTypePoint.BizCustomTypePoint2.toLong())
        removeAllLayerItems(BizCustomTypePoint.BizCustomTypePoint3.toLong())
        val searchPoiInfos = ArrayList<SearchPoiInfo>()
        val scenicData = scenicDetail.value
        scenicData?.geo?.let { geo ->
            searchPoiInfos.add(SearchPoiInfo().apply {
                basicInfo = SearchPoiBasicInfo().apply {
                    location = Coord2DDouble(geo.lng, geo.lat)
                    poiId = "${scenicData.id}|4" // 1.路书 2.共创路书 3.轨迹路书 4.景点详情
                    name = scenicData.caption
                    adcode = -1
                }
            })
        }

        if (searchPoiInfos.size == 1){
            customLayer.showAhaLineDayNodePoint(searchPoiInfos) //简介图层扎标
            mapBusiness.setMapCenter(searchPoiInfos[0].basicInfo.location.lon, searchPoiInfos[0].basicInfo.location.lat)
        }
    }

    fun removeAllLayerItems(bizType: Long) {
        customLayer.bizCustomControl.clearAllItems(bizType)
    }

    /**
     * 显示全览
     *
     */
    fun showPreview(pointList: List<Coord3DDouble>) {
        Timber.i("showPreview getScreenStatus = ${iCarInfoProxy.getScreenStatus().value}")
        SearchMapUtil.getSmartDriveBound(pointList)?.let { mapRect ->
            val previewParam = PreviewParam().apply {
                leftOfMap = CommonUtils.getAutoDimenValue(
                    application,
                    if (iCarInfoProxy.getScreenStatus().value == true) R.dimen.sv_dimen_1500 else R.dimen.sv_dimen_0
                )
                topOfMap = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_351)
                screenLeft = CommonUtils.getAutoDimenValue(
                    application,
                    if (iCarInfoProxy.getScreenStatus().value == true) R.dimen.sv_dimen_1640 else R.dimen.sv_dimen_900
                )
                screenTop = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_300)
                screenRight = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_300)
                screenBottom = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_300)
                bUseRect = true
                mapBound = mapRect
            }
            mMapController.getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
                ?.showPreview(previewParam, true, 500, -1)
        }
    }

    fun exitPreview(){
        mainMapView?.exitPreview(true)
        mapBusiness.backCurrentCarPosition()
    }

    //景区播报--巡航状态
    fun scenicSectorObserve(){
        cruiseBusiness.cruiseStatus.unPeek().observeForever {
            Timber.i("cruiseStatus = $it")
            mScenicSectorHandler.removeCallbacks(scenicSectorRunnable)
            if (it){ //启动定时器
                mScenicSectorHandler.post(scenicSectorRunnable)
            }
        }
    }
    private var scenicSectorRunnable: Runnable = object : Runnable {
        override fun run() {
            Timber.i("scenicSectorRunnable cruiseStatus:${cruiseBusiness.cruiseStatus.value == true} isInitSuccess:${initSDKBusiness.isInitSuccess()} getAhaScenicBroadcastSwitch:${navigationSettingBusiness.getAhaScenicBroadcastSwitch()}")
            if (cruiseBusiness.cruiseStatus.value == true && initSDKBusiness.isInitSuccess() && navigationSettingBusiness.getAhaScenicBroadcastSwitch()){
                val isTimeBetween8To21 = isTimeBetween8To21()
                Timber.i("scenicSectorRunnable onSuccess isTimeBetween8To21:$isTimeBetween8To21")
                if (isTimeBetween8To21){
                    requestScenicSector.postValue(true)
                }
                mScenicSectorHandler.postDelayed(this, 1000 * 30)
            }
        }
    }

    // 判断当前小时是否在 8 到 21 之间
    fun isTimeBetween8To21(): Boolean {
        val calendar: Calendar = Calendar.getInstance()
        val currentHour: Int = calendar.get(Calendar.HOUR_OF_DAY)
        // 判断当前小时是否在 8 到 21 之间
        return currentHour in 8..20
    }

    //添加图层点击
    fun customLayerAddClickObserver(){
        customLayer.addClickObserver(iLayerClickObserver)
    }

    //移除图层点击
    fun customLayerRemoveClickObserver(){
        customLayer.removeClickObserver(iLayerClickObserver)
    }

    /**
     * 图层上POI点击监听
     */
    private var iLayerClickObserver: ILayerClickObserver = object : ILayerClickObserver {

        override fun onBeforeNotifyClick(
            baseLayer: BaseLayer?,
            layerItem: LayerItem?,
            clickViewIdInfo: ClickViewIdInfo?
        ) {
        }

        /**
         * 图层点击回调
         *
         * @param baseLayer
         * @param layerItem       图层回调信息,需根据不同业务场景转换
         * @param clickViewIdInfo
         */
        override fun onNotifyClick(baseLayer: BaseLayer?, layerItem: LayerItem?, clickViewIdInfo: ClickViewIdInfo?) {
            try {
                //需要特别注意：baseLayer和layerItem只能在这个函数中使用，不能抛到其它线程中使用（因为对象可能已经被释放）
                if (baseLayer == null || layerItem == null) {
                    return
                }
                val businessType = layerItem.businessType
                val id = layerItem.id
                Timber.i("onNotifyClick businessType: $businessType  , id: ${layerItem.id}")
                when (businessType) {
                    BizCustomTypePoint.BizCustomTypePoint2 -> { //路书详情图层景点POI点击
                        if (layerItem is CustomPointLayerItem) {
                            val index = layerItem.mType
                            Timber.i("onNotifyClick BizCustomTypePoint2 index: $index")
                            if (id.contains('|')) {
                                val first = id.indexOf('|')
                                if (first != -1){
                                    val num = id.substring(0, first)
                                    val type = id.substring(first + 1, id.length) // 1.路书 2.共创路书 3.轨迹路书 4.景点详情
                                    when(type.toInt()){
                                        1 -> {
                                            val lineData = lineDetail.value?.lineData
                                            if (lineData != null && lineData.size > 0 && index < lineData.size && lineData[index] != null){
                                                val node = lineData[index].node
                                                if (node != null && node.size > 0){
                                                    Timber.i("onNotifyClick BizCustomTypePoint2 nodeId:${node[num.toInt()].poiDetail.id}")
                                                    gotoAhaScenicDetail.postValue(node[num.toInt()].poiDetail.id.toString())
                                                }
                                            }
                                        }
                                        2 -> {
                                            val nodeList = guideDetail.value?.nodeList
                                            if (!nodeList.isNullOrEmpty() && index < nodeList.size){
                                                val node = nodeList[index].node
                                                if (!node.isNullOrEmpty()){
                                                    Timber.i("onNotifyClick BizCustomTypePoint2 nodeId:${node[num.toInt()].nodeId}")
                                                    gotoAhaScenicDetail.postValue(node[num.toInt()].nodeId.toString())
                                                }
                                            }
                                        }
                                        else -> {
                                            Timber.i("onNotifyClick BizCustomTypePoint2 不作处理")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    BizCustomTypePoint.BizCustomTypePoint3 -> {
                        if (layerItem is CustomPointLayerItem) {
                            val index = layerItem.mType
                            Timber.i("onNotifyClick BizCustomTypePoint3 index: $index")
                            if (id.contains('|')) {
                                val first = id.indexOf('|')
                                if (first != -1){
                                    val type = id.substring(first + 1, id.length) // 0.路书 1.共创路书
                                    when(type.toInt()){
                                        0, 1 -> {
                                            gotoAhaDayDetail.postValue(index)
                                        }
                                        else -> {
                                            Timber.i("onNotifyClick BizCustomTypePoint3 不作处理")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    else -> {}
                }
            } catch (e: Exception){
                Timber.i("onNotifyClick Exception:${e.message}")
            }
        }

        override fun onAfterNotifyClick(
            baseLayer: BaseLayer,
            layerItem: LayerItem,
            clickViewIdInfo: ClickViewIdInfo?
        ) {
        }
    }
}