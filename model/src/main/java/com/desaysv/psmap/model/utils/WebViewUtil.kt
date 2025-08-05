package com.desaysv.psmap.model.utils

import android.annotation.SuppressLint
import android.os.Build
import android.webkit.WebSettings
import android.webkit.WebView
import com.desaysv.psmap.base.utils.BaseConstant
import timber.log.Timber
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Author : wangmansheng
 * Date : 2024-1-11
 * Description : WebView工具类
 */
object WebViewUtil {
    /**
     * 配置了 android:sharedUserId="android.uid.system",
     * 出现For security reasons, WebView is not allowed in privileged processes
     * 在使用 WebView 之前，我们先 Hook WebViewFactory，创建 sProviderInstance 对象，从而绕过系统检查
     */
    @SuppressLint("SoonBlockedPrivateApi", "PrivateApi", "DiscouragedPrivateApi")
    fun hookWebView() {
        val sdkInt = Build.VERSION.SDK_INT
        try {
            val factoryClass = Class.forName("android.webkit.WebViewFactory")
            val field: Field = factoryClass.getDeclaredField("sProviderInstance")
            field.isAccessible = true
            var sProviderInstance: Any? = field.get(null)
            if (sProviderInstance != null) {
                Timber.d("sProviderInstance isn't null")
                return
            }
            val getProviderClassMethod: Method = if (sdkInt > 22) {
                factoryClass.getDeclaredMethod("getProviderClass")
            } else if (sdkInt == 22) {
                factoryClass.getDeclaredMethod("getFactoryClass")
            } else {
                Timber.d("Don't need to Hook WebView")
                return
            }
            getProviderClassMethod.isAccessible = true
            val factoryProviderClass = getProviderClassMethod.invoke(factoryClass) as Class<*>
            val delegateClass = Class.forName("android.webkit.WebViewDelegate")
            val delegateConstructor = delegateClass.getDeclaredConstructor()
            delegateConstructor.isAccessible = true
            if (sdkInt < 26) { //低于Android O版本
                val providerConstructor = factoryProviderClass.getConstructor(delegateClass)
                if (providerConstructor != null) {
                    providerConstructor.isAccessible = true
                    sProviderInstance =
                        providerConstructor.newInstance(delegateConstructor.newInstance())
                }
            } else {
                val chromiumMethodName: Field =
                    factoryClass.getDeclaredField("CHROMIUM_WEBVIEW_FACTORY_METHOD")
                chromiumMethodName.isAccessible = true
                var chromiumMethodNameStr = chromiumMethodName.get(null) as String
                if (chromiumMethodNameStr == null) {
                    chromiumMethodNameStr = "create"
                }
                val staticFactory =
                    factoryProviderClass.getMethod(chromiumMethodNameStr, delegateClass)
                if (staticFactory != null) {
                    sProviderInstance =
                        staticFactory.invoke(null, delegateConstructor.newInstance())
                }
            }
            if (sProviderInstance != null) {
                field.set("sProviderInstance", sProviderInstance)
                Timber.d("Hook success!")
            } else {
                Timber.d("Hook failed!")
            }
        } catch (e: Throwable) {
            Timber.d("Throwable ${e.message}")
        }
    }

    //webView属性设置
    @SuppressLint("SetJavaScriptEnabled")
    fun webViewSettings(webView: WebView, type: Int, isNight: Boolean) {
        webView.apply {
            loadUrl(if (type == 0) (if (isNight) BaseConstant.TERMS_LINK_NIGHT else BaseConstant.TERMS_LINK) else if (isNight) BaseConstant.POLICY_LINK_NIGHT else BaseConstant.POLICY_LINK)
            settings.javaScriptEnabled = true
            setBackgroundColor(0)
            settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK //优先使用缓存
            settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING //支持内容重新布局
            settings.textZoom = 180
            settings.savePassword = false
        }
    }
}