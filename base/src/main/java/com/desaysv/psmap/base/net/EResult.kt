package com.desaysv.psmap.base.net

import com.google.gson.annotations.SerializedName

/**
 *@author uidq3334
 *@emily weilon.huang@foxmail.com
 *@date 2022/2/25
 *
 *
 * * code    message
 *   200     成功
 *   300     应用未授权当前接口
 *   2403    缺少signType参数
 *   2402    缺少sign参数
 *   2404    缺少timestamp参数
 *   2405    缺少appId参数
 *   2406    应用未完成加密配置
 *   2501    缺少channel参数
 *   2502    缺少deviceId参数
 *   2503    缺少deviceType参数
 *   2504    缺少appVer参数
 *   2505    缺少os参数
 *   2506    缺少osVer参数
 *   2507    缺少brand参数
 *   2508    缺少model参数
 *   401     非法参数
 *   402     非法业务参数
 *   1401    不支持的HTTP方法
 *   1402    非法sign参数
 *   1403    非法signType参数
 *   1404    非法timestamp参数
 *   1405    非法appId参数
 *   1406    accessToken过期
 *   1407    非法的URL
 *   -444    请求太过频繁
 *   -445    请求太过频繁
 *   -446    请求太过频繁
 * 播客
 * code      message
 * 200       成功
 * 游戏
 * code	     message
 * 200	     成功
 */

data class ETagList<T>(
    @SerializedName("records", alternate = ["list"])
    var records: List<T>? = null,
    @SerializedName("hasMore")
    var hasMore: Boolean
)

data class EResult<T>(
    /**
     * 公共错误码，200表示成功，其他为错误，详情见EResult的数据类文件
     */
    @SerializedName("code") var code: Int? = null,

    @SerializedName("subCode") var subCode: Int? = null,

    @SerializedName("message") var message: String? = null,

    @SerializedName("data") var data: T? = null

) {
    /**
     * 转换Result的泛型数据，Loading、Error由此方法直接转换，而data则由实现的Function函数进行转换
     */
    fun <X> transform(transform: (T) -> X?): EResult<X?> {
        return if (code == 200) {
            EResult(code, subCode, message, data = transform(data!!))
        } else {
            EResult(code, subCode, message, data = null)
        }
    }
}
