package com.desaysv.psmap.base.net

import com.desaysv.psmap.base.net.bean.CustomFullPoiListBean
import com.desaysv.psmap.base.net.bean.CustomPoiCategoryBean
import com.desaysv.psmap.base.net.bean.CustomPoiCategoryRequsetBody
import com.desaysv.psmap.base.net.bean.CustomPoiDetailBean
import com.desaysv.psmap.base.net.bean.CustomPoiDetailRequsetBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface JetourTspApiService {

    @POST(URLConfig.CUSTOM_GET_POICATEGORY_URL)
    @Headers("Content-Type: application/json")
    suspend fun getPoiCategory(
        @Body requestBody: CustomPoiCategoryRequsetBody
    ): CustomPoiCategoryBean

    @POST(URLConfig.CUSTOM_GET_FULLPOILIST_URL)
    @Headers("Content-Type: application/json")
    suspend fun getFullPoiList(
        @Body requestBody: CustomPoiCategoryRequsetBody
    ): CustomFullPoiListBean

    @POST(URLConfig.CUSTOM_GET_POIDETAIL_URL)
    @Headers("Content-Type: application/json")
    suspend fun getPoiDetail(
        @Body requestBody: CustomPoiDetailRequsetBody
    ): CustomPoiDetailBean
}