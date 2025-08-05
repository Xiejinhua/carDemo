package com.autosdk.bussiness.data.observer;

import com.autonavi.gbl.data.model.DataErrorType;
import com.autonavi.gbl.data.model.DataType;
import com.autonavi.gbl.data.model.DownLoadMode;
import com.autonavi.gbl.data.model.MergedStatusInfo;
import com.autonavi.gbl.data.model.OperationType;
import com.autonavi.gbl.data.model.TaskStatusCode;
import com.autonavi.gbl.data.observer.IDataInitObserver;
import com.autonavi.gbl.data.observer.IDataListObserver;
import com.autonavi.gbl.data.observer.IDownloadObserver;
import com.autonavi.gbl.data.observer.IErrorDataObserver;
import com.autonavi.gbl.data.observer.IMergedStatusInfoObserver;

import java.util.ArrayList;

/**
 * Created by AutoSdk on 2020/11/10.
 **/
public interface IMapDataObserver extends IDownloadObserver, IDataInitObserver, IErrorDataObserver, IDataListObserver, IMergedStatusInfoObserver {
    /**
     * @param downLoadMode 下载模式（请忽略本回调该参数值，无意义）
     * @param dataType     数据类型
     * @param opCode       为Service.ErrorCodeOK，表示获取id列表成功；
     *                     为其他值，表示获取id列表失败，并明确失败具体原因。
     * @brief 初始化回调
     */
    @Override
    void onInit(@DownLoadMode.DownLoadMode1 int downLoadMode, @DataType.DataType1 int dataType,  int opCode);

    /**
     * @param downLoadMode 下载模式
     * @param dataType     数据类型
     * @param opCode       为Service.ErrorCodeOK，表示获取数据id列表成功；
     *                     为其他值，表示获取数据id列表失败，并明确失败具体原因。
     * @brief 数据列表获校验请求回调
     */
    @Override
    void onRequestDataListCheck(@DownLoadMode.DownLoadMode1 int downLoadMode, @DataType.DataType1 int dataType,  int opCode);

    /**
     * @param downLoadMode   下载模式
     * @param dataType       数据类型
     * @param opType         下载操作类型
     * @param opreatedIdList 为参与下载操作的数据id列表组合
     *                       当dataType参数值为DATA_TYPE_MAP时，opreatedIdList参数为城市行政编码adcode值的队列组合。
     *                       当dataType参数值为DATA_TYPE_VOICE时，opreatedIdList参数为语音记录voiceId值的队列组合。
     * @brief 下载操作回调
     */
    @Override
    void onOperated(@DownLoadMode.DownLoadMode1 int downLoadMode, @DataType.DataType1 int dataType, @OperationType.OperationType1 int opType,
                    ArrayList<Integer> opreatedIdList);

    /**
     * @param downLoadMode 下载模式
     * @param dataType     数据类型
     * @param id           数据id
     * @param taskCode     任务状态
     * @param opCode       操作状态码
     *                     当dataType参数值为DATA_TYPE_MAP时，id参数为城市行政编码adcode值。
     *                     当dataType参数值为DATA_TYPE_VOICE时，id参数为语音记录voiceId值。
     * @brief 下载状态回调
     */
    @Override
    void onDownLoadStatus(@DownLoadMode.DownLoadMode1 int downLoadMode, @DataType.DataType1 int dataType, int id,
                          @TaskStatusCode.TaskStatusCode1 int taskCode,  int opCode);

    /**
     * @param downLoadMode 下载模式
     * @param dataType     数据类型
     * @param id           数据id
     * @param percentType  百分比类型 (默认0表示下载; 1表示解压融合进度)
     * @param percent      百分比值
     *                     当dataType参数值为DATA_TYPE_MAP时，id参数为城市行政编码adcode值。
     *                     当dataType参数值为DATA_TYPE_VOICE时，id参数为语音记录voiceId值。
     * @brief 下载进度回调
     */
    @Override
    void onPercent(@DownLoadMode.DownLoadMode1 int downLoadMode, @DataType.DataType1 int dataType, int id, int percentType, float percent);

    /**
     * @param downLoadMode 数据下载方式
     * @param dataType     数据类型
     * @param id           数据Id
     * @param errType      数据异常类型
     * @param errMsg       数据异常具体字符串描述
     *                     当dataType参数值为DATA_TYPE_MAP时，id参数为城市行政编码adcode值。
     *                     当dataType参数值为DATA_TYPE_VOICE时，id参数为语音记录voiceId值。
     * @brief 数据异常通知
     */
    @Override
    void onErrorNotify(@DownLoadMode.DownLoadMode1 int downLoadMode, @DataType.DataType1 int dataType, int id,
                       @DataErrorType.DataErrorType1 int errType, String errMsg);

    /**
     * @param downLoadMode 数据下载方式
     * @param dataType     数据类型
     * @param id           数据Id
     * @param opCode       操作状态码
     *                     当dataType参数值为DATA_TYPE_MAP时，id参数为城市行政编码adcode值。
     *                     当dataType参数值为DATA_TYPE_VOICE时，id参数为语音记录voiceId值。
     * @brief 异常数据清除回调通知
     */
    @Override
    void onDeleteErrorData(@DownLoadMode.DownLoadMode1 int downLoadMode, @DataType.DataType1 int dataType, int id,  int opCode);

    @Override
    void onMergedStatusInfo(MergedStatusInfo mergedStatusInfo);
}
