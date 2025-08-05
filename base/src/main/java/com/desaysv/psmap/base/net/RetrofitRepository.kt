package com.desaysv.psmap.base.net

import com.desaysv.psmap.base.net.bean.BaseRequestBody
import com.desaysv.psmap.base.utils.CommonUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import com.desaysv.psmap.base.utils.Result

@Singleton
class RetrofitRepository @Inject constructor(private val apiService: ApiService) {
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO

    // 激活统计api请求
    suspend fun requestActive(requestBody: BaseRequestBody) = getResultFlow {
        Result.success(
            apiService.activePostBody(
                if (CommonUtils.isProdEnvironment())
                    URLConfig.ACTIVATE_BASE_URL + URLConfig.sdkActivate
                else URLConfig.ACTIVATE_TEST_URL + URLConfig.sdkActivate, requestBody.mapParam
            )
        )
    }

    // 账号绑定api请求
    suspend fun requestAccountBind(key: HashMap<String, Any>, requestBody: BaseRequestBody) = getResultFlow {
        Result.success(
            apiService.accountPostBody(
                if (CommonUtils.isProdEnvironment())
                    URLConfig.G50_BASE_ACC_URL + URLConfig.saveGDBind
                else URLConfig.G50_TEST_ACC_URL + URLConfig.saveGDBind, key, requestBody.mapParam
            )
        )
    }

    private suspend fun <T> getResultFlow(request: suspend () -> Result<T>): Flow<Result<T>> = flow {
        try {
            emit(Result.loading()) // 发送 loading 状态
            emit(request())
        } catch (e: Exception) {
            emit(Result.error(e))
        }
    }.flowOn(dispatcher)
}