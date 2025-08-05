package com.desaysv.psmap.base.business

import com.autonavi.gbl.aosclient.model.CEtaRequestReponseParam
import com.autonavi.gbl.aosclient.model.CEtaRequestRequestParam
import com.autonavi.gbl.aosclient.model.GFeedbackReportRequestParam
import com.autonavi.gbl.aosclient.model.GReStrictedAreaDataRuleRes
import com.autonavi.gbl.aosclient.model.GReStrictedAreaRequestParam
import com.autonavi.gbl.aosclient.model.GTrafficRestrictRequestParam
import com.autonavi.gbl.aosclient.model.GWsUserviewFootprintSummaryRequestParam
import com.autonavi.gbl.aosclient.observer.ICallBackFeedbackReport
import com.autonavi.gbl.aosclient.observer.ICallBackTrafficRestrict
import com.autonavi.gbl.aosclient.observer.ICallBackWsUserviewFootprintSummary
import com.autonavi.gbl.common.model.Coord2DDouble
import com.autosdk.bussiness.aos.AosController
import com.desaysv.psmap.base.utils.Result
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * @author 张楠
 * @time 2024/1/31
 * @description 网络服务相关业务类
 *
 */
@Singleton
class AosBusiness @Inject constructor(
    private val mAosController: AosController,
    private val mLocationBusiness: LocationBusiness,
    private val dataBusiness: MapDataBusiness
) {

    private val timeOut = 8 * 1000L;

    /**
     * 根据传入的两个点的经纬度计算距离和预计到达时间
     *
     * @param startPoint    起点坐标
     * @param endPoint  终点坐标
     * @return CEtaRequestReponseParam
     */
    suspend fun getDisTime(
        startPoint: Coord2DDouble? = null,
        endPoint: Coord2DDouble
    ): Result<CEtaRequestReponseParam> {
        var tempStartPoint = startPoint ?: mLocationBusiness.getLastLocation()
            .let { Coord2DDouble(it.longitude, it.latitude) }
        return withTimeoutOrNull(timeOut) {
            suspendCancellableCoroutine { continuation ->
                val requestParam = CEtaRequestRequestParam()
                requestParam.start_x = tempStartPoint.lon.toString()
                requestParam.start_y = tempStartPoint.lat.toString()
                requestParam.end_x = endPoint.lon.toString()
                requestParam.end_y = endPoint.lat.toString()
                mAosController.sendReqEtaRequestRequest(
                    requestParam
                ) { data -> continuation.resume(Result.success(data as CEtaRequestReponseParam)) }

            }
        } ?: Result.error("Time Out!")
    }

    /**
     * @param inputPlate 车牌号
     * @param adCodes 城市代码，允许多adcode，多个使用|分割，格式:adcode1|adcode2|adcode3
     *获取限行信息
     */
    suspend fun getStrictedAreaInfo(
        inputPlate: String,
        adCodes: String? = null
    ): Result<GReStrictedAreaDataRuleRes> {
        var checkAdCodes = adCodes
        if (checkAdCodes == null) {
            val lastLocation = mLocationBusiness.getLastLocation()
            Timber.i("getStrictedAreaInfo longitude=${lastLocation.longitude} latitude=${lastLocation.latitude}")
            checkAdCodes =
                dataBusiness.getAdCodeByLonLat(lastLocation.longitude, lastLocation.latitude)
                    .toString()
        }
        Timber.i("getStrictedAreaInfo adCodes=$checkAdCodes inputPlate=$inputPlate")
        return withTimeoutOrNull(timeOut) {
            suspendCancellableCoroutine { continuation ->
                val requestParam = GReStrictedAreaRequestParam().apply {
                    restrict_type = 1 //请求城市全部规则（必须有adcodes参数）
                    adcodes = checkAdCodes
                    plate = inputPlate
                }
                mAosController.requestRestrictedArea(
                    requestParam
                ) { data ->
                    continuation.resume(Result.success(data.data.mDataRule))
                }

            }
        } ?: Result.error("Time Out!")
    }

    /**
     * 用户反馈-错误上报
     */
    fun sendReqFeedbackReport(
        pAosRequest: GFeedbackReportRequestParam,
        callBackFeedbackReport: ICallBackFeedbackReport
    ): Long {
        return mAosController.sendReqFeedbackReport(pAosRequest, callBackFeedbackReport)
    }

    /**
     * 获取足迹信息
     */
    fun sendReqFootprintSummary(
        pAosRequest: GWsUserviewFootprintSummaryRequestParam?,
        iCallBackWsUserviewFootprintSummary: ICallBackWsUserviewFootprintSummary
    ): Long {
        return mAosController.sendReqFootprintSummary(
            pAosRequest,
            iCallBackWsUserviewFootprintSummary
        )
    }

    fun abortRequest(abort: Long) {
        mAosController.abortRequest(abort)
    }

    /**
     * 限行
     */
    fun sendReqTrafficRestrict(
        gTrafficRestrictRequestParam: GTrafficRestrictRequestParam?,
        callBackTrafficRestrict: ICallBackTrafficRestrict
    ): Long {
        return mAosController.sendReqTrafficRestrict(
            gTrafficRestrictRequestParam,
            callBackTrafficRestrict
        )
    }
}
