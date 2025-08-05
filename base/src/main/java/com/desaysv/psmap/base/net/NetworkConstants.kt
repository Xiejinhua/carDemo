package com.desaysv.psmap.base.net

object NetworkConstants {
    /**
     * 请求超时
     */
    const val TIME_OUT_REQUEST = 20

    /**
     * 链接超时
     */
    const val TIME_OUT_CONNECT = 20

    /**
     * 读取超时
     * 网络数据已返回，读取返回数据
     */
    const val TIME_OUT_READ = 20

    /**
     * 写入超时
     * 写入数据到网络
     */
    const val TIME_OUT_WRITE = 20

    /**
     * 请求类型--激活统计
     */
    const val REQUEST_TYPE_ACTIVE = "active"

    /**
     * 请求类型--账号绑定
     */
    const val REQUEST_TYPE_ACCOUNT = "account"

    const val BASE_URL = "Base-Url"

    const val APP_ID = "C81D29040BAB424E87435596613DB30BF2018D0E02A4D184A42721917229FBF4" //生产

    /**
     * 请求参数最外层 header
     */
    const val KEY_HEADER = "header"

    /**
     * 请求参数最外层 body
     */
    const val KEY_BODY = "body"
}
