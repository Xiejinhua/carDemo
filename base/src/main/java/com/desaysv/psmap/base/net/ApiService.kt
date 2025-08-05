package com.desaysv.psmap.base.net

import com.desaysv.psmap.base.net.bean.AccountResultBean
import com.desaysv.psmap.base.net.bean.ActiveResultBean
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.HeaderMap
import retrofit2.http.POST


interface ApiService {

    //激活统计请求api
    @POST(URLConfig.sdkActivate)
    suspend fun activePostBody(
        @Header(NetworkConstants.BASE_URL) baseUrl: String,
        @Body map: HashMap<String, Any>
    ): ActiveResultBean

    //账号绑定请求api
    @POST(URLConfig.saveGDBind)
    suspend fun accountPostBody(
        @Header(NetworkConstants.BASE_URL) baseUrl: String,
        @HeaderMap key: HashMap<String, Any>,
        @Body map: HashMap<String, Any>
    ): AccountResultBean
}