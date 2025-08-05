# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontskipnonpubliclibraryclasses # 不忽略非公共的库类
-optimizationpasses 5            # 指定代码的压缩级别
-dontusemixedcaseclassnames      # 是否使用大小写混合
-dontpreverify                   # 混淆时是否做预校验
-verbose                         # 混淆时是否记录日志
-keepattributes *Annotation*     # 保持注解
-ignorewarnings                   # 忽略警告
-dontoptimize                    # 优化不优化输入的类文件

-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*  # 混淆时所采用的算法

#保持哪些类不被混淆
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

#生成日志数据，gradle build时在本项目根目录输出
-dump class_files.txt            #apk包内所有class的内部结构
-printseeds seeds.txt            #未混淆的类和成员
-printusage unused.txt           #打印未被使用的代码
-printmapping mapping.txt        #混淆前后的映射

-keep public class * extends android.support.** #如果有引用v4或者v7包，需添加
#-libraryjars libs/xxx.jar        #混淆第三方jar包，其中xxx为jar包名
-keep class com.autosdk.**{*;}       #不混淆某个包内的所有文件
-keep class com.autonavi.**{*;}       #不混淆某个包内的所有文件
-dontwarn com.autosdk**              #忽略某个包的警告
-keepattributes Signature        #不混淆泛型
-keepnames class * implements java.io.Serializable #不混淆Serializable

-keepclassmembers class **.R$* { #不混淆资源类
　　public static <fields>;
}
-keepclasseswithmembernames class * {  # 保持 native 方法不被混淆
    native <methods>;
}
-keepclasseswithmembers class * {      # 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {      # 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * extends android.app.Activity { # 保持自定义控件类不被混淆
    public void *(android.view.View);
}
-keepclassmembers enum * {             # 保持枚举 enum 类不被混淆
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable {         # 保持 Parcelable 不被混淆
    public static final android.os.Parcelable$Creator *;
}
# androidx 混淆
-keep class com.google.android.material.** {*;}
-keep class androidx.** {*;}
-keep public class * extends androidx.**
-keep interface androidx.** {*;}
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**
-dontwarn androidx.**
-printconfiguration
-keep,allowobfuscation @interface androidx.annotation.Keep

-keep @androidx.annotation.Keep class *
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# 不混淆类名中包含了"viewmodel"的类，及类中内容
-keep class **.*viewmodel*.** {*;}
-keep class **.*data*.** {*;}
-keep class **.*bean*.** {*;}

-keep class com.desaysv.btcall.** {*;}
-keep class android.** {*;}
-keep class com.android.** {*;}
-keep class com.desay_svautomotive.** {*;}
-keep class com.zlw.** {*;}
-keep class com.zyp.** {*;}
-keep class com.sgmw.** {*;}
-keep class com.sensorsdata.** {*;}

#-libraryjars lib/desaysv-platformadapter.jar
-keep class cn.hutool.** {*;}
-keep class com.alibaba.** {*;}
-keep class com.blankj.** {*;}
-keep class com.github.** {*;}
-keep class com.google.** {*;}
-keep class com.jakewharton.** {*;}

-keep class com.kunminx.** {*;}
-keep class com.netease.** {*;}
-keep class com.lcodecore.** {*;}
-keep class com.scwang.** {*;}
-keep class com.tencent.** {*;}

# WebView
-dontwarn android.webkit.WebView
-dontwarn android.net.http.SslError
-dontwarn android.webkit.WebViewClient
-keep public class android.webkit.WebView
-keep public class android.net.http.SslError
-keep public class android.webkit.WebViewClient

# glide混淆
-dontwarn com.bumptech.glide.**
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
#glide如果你的API级别<=Android API 27 则需要添加 4.6.1
-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder
-dontwarn me.iwf.photopicker.adapter.**

#==================OkHttp3==========================
-dontwarn okhttp3.logging.**
-keep class okhttp3.** {*;}
-keep interface okhttp3.** {*;}
-dontwarn okio.**
#==================Retrofit==========================
-keep class retrofit2.** { *; }  # 保持 Retrofit 相关类
-keep interface retrofit2.** { *; }  # 保持 Retrofit 接口
-keep class retrofit2.Call { *; }
-keepattributes Signature  # 保持 Java 泛型信息
-keepattributes *Annotation*  # 保持注解信息
-keep,includedescriptorclasses class * extends retrofit2.Call
-keep,allowobfuscation interface * { @retrofit2.http.* <methods>; }
# 保持 Retrofit 方法的参数类及其成员信息
-keep class * {
    @retrofit2.http.* <methods>;
}

-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn com.fasterxml.**
-dontwarn retrofit2.**

#------------------  下方是共性的排除项目         ----------------
# 方法名中含有“JNI”字符的，认定是Java Native Interface方法，自动排除
# 方法名中含有“JRI”字符的，认定是Java Reflection Interface方法，自动排除

-keepclasseswithmembers class * {
    ... *JNI*(...);
}

-keepclasseswithmembernames class * {
	... *JRI*(...);
}

-keep class **JNI* {*;}

-keepclassmembers class * extends com.chad.library.adapter.base.BaseQuickAdapter {
    public <init>();
 }

-keep class androidx.recyclerview.widget.RecyclerView {*;}
-keep class androidx.lifecycle.LiveData { *; }

#-keep class com.desaysv.psmap.baselibrary.** {*;}
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

# 万能适配器 BaseRecyclerViewAdapterHelper
-keep class com.chad.library.adapter.** {
*;
}
-keep public class * extends com.chad.library.adapter.base.BaseQuickAdapter

#vdb
-keep class com.desaysv.ivi.** {*;}

#adapter 打包
-keep class com.desaysv.psmap.adapter.** {*;}

#bean
-keep class com.desaysv.psmap.base.bean.** {*;}
-keep class com.desaysv.psmap.model.bean.** {*;}
-keep class com.desaysv.psmap.base.utils.Result

#dji
-keep class com.dji.** {*;}

#hilt注入
-keep class com.desaysv.psmap.base.di.** {*;}
-keep class com.desaysv.psmap.model.di.** {*;}
-keep class com.desaysv.psmap.base.def.** {*;}
-keep class com.desaysv.psmap.base.net.** {*;}

#外部包
-keep class com.sy.swbt.** {*;}
-keep class com.zyt.** {*;}
-keep class com.iwall.** {*;}

#解决混淆后gson转换报错
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

-keep class com.aha.autocar.aha_api_sdk.manger.** { *; }

