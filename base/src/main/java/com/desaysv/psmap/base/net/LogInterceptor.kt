package com.desaysv.psmap.base.net

import android.annotation.SuppressLint
import android.text.TextUtils
import com.google.gson.Gson
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date

class LogInterceptor : Interceptor {

    //时间格式化
    private val PATTERN_FULL = "yyyy-MM-dd HH:mm:ss"

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        return getResponseBody(chain)!!
    }

    private fun determineNewUrl(request: Request): HttpUrl? {
        val headerValue = request.header(NetworkConstants.BASE_URL)
        Timber.d("determineNewUrl headerValue$headerValue")
        return headerValue?.let { it.toHttpUrlOrNull() }
    }

    @Throws(IOException::class)
    private fun getResponseBody(chain: Interceptor.Chain): Response? {
        val request = chain.request()

        val newUrl = determineNewUrl(request)
        val newRequest = request.newBuilder()
            .url(newUrl!!)
            .build()
        val response = chain.proceed(newRequest) // 修改此处代码
        val startTime = nowFullDateMillisStr

        return try {
            if (response.body != null && response.body!!.contentType() != null) {
                val mediaType = response.body!!.contentType()
                val dataStr = response.body!!.string() //获取到返回数据
                logResponse(startTime, request, dataStr)
                val body = ResponseBody.create(mediaType, dataStr)
                response.newBuilder().body(body).build() //将数据重新生成为返回数据
            } else {
                logResponse(startTime, request, "")
                response
            }
        } catch (e: Exception) {
            Timber.e("catch Exception: %s", e.message)
            logResponse(startTime, request, e.message)
            response
        }
    }

    private fun logResponse(
        startTime: String,
        request: Request,
        responseStr: String?
    ) {
        val mediaType = request.body!!.contentType()
        val requestStr: String
        requestStr = try {
            getRequestBody(request.body)
        } catch (e: JSONException) {
            Timber.e("catch Exception: %s", e.message)
            "JSONException:" + e.message
        } catch (ioe: IOException) {
            Timber.e("catch Exception: %s", ioe.message)
            "IOException:" + ioe.message
        }
        val headerBuilder = StringBuilder()
        val headers = request.headers
        for (i in 0 until headers.size) {
            val name = headers.name(i)
            val value = headers[name]
            headerBuilder.append("\n")
                .append(name).append(":").append(value)
        }
        val sb = StringBuilder()
            .append("\n")
            .append(request.method + "::")
            .append(request.url)
            .append("\n")
            .append("startTime:").append(startTime)
            .append("\n")
            .append("Headers:")
            .append("\n")
            .append("MediaType:").append(mediaType)
            .append(headerBuilder.toString())
            .append("\n")
            .append("RequestBody:")
            .append("\n")
            .append(requestStr)
            .append("\n")
            .append("ResponseBody:")
            .append("\n")
            .append(responseStr ?: "")
            .append("\n")
        Timber.d(sb.toString())
    }

    /**
     * 获取请求体
     */
    @Throws(JSONException::class, IOException::class)
    private fun getRequestBody(body: RequestBody?): String {
        if (body == null) return ""
        return if (body.contentType().toString().contains("multipart/form-data")) { //打印上传文件请求体
            val map: Map<String, String> =
                HashMap()
            JSONObject(Gson().toJson(map)).toString(1)
        } else { //打印请求体
            val buffer = Buffer()
            body.writeTo(buffer)
            val charset =
                if (body.contentType() == null) StandardCharsets.UTF_8 else body.contentType()!!
                    .charset(StandardCharsets.UTF_8)!!
            strFormat(buffer.readString(charset))
        }
    }

    /**
     * 格式化
     *
     * @param json
     * @return
     * @throws JSONException
     */
    @Throws(JSONException::class)
    private fun strFormat(json: String): String {
        var json = json
        if (TextUtils.isEmpty(json)) return ""
        json = json.trim { it <= ' ' }
        if (json.startsWith("{")) {
            val jsonObject = JSONObject(json)
            return jsonObject.toString(1)
        }
        if (json.startsWith("[")) {
            return JSONArray(json).toString(2)
        }
        if (json.contains("=")) {
            val map: MutableMap<String, String> = HashMap()
            if (json.contains("&")) {
                val splits = json.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (split in splits) {
                    if (!TextUtils.isEmpty(split) && split.contains("=")) {
                        val splites =
                            split.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        map[splites[0]] = splites[1]
                    }
                }
            } else {
                val s = json.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                map[s[0]] = s[1]
            }
            return JSONObject(Gson().toJson(map)).toString(1)
        }
        return json
    }

    @get:SuppressLint("SimpleDateFormat")
    private val nowFullDateMillisStr: String
        //获取当前时间2021-01-11 13:45:57.567
        private get() {
            val date = Date()
            return SimpleDateFormat(PATTERN_FULL)
                .format(date) + "." + (date.time % 1000).toString()
        }
}
