package com.desaysv.psmap.base.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.ActivityOptions
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.view.Display
import androidx.core.app.ActivityOptionsCompat
import com.desaysv.psmap.base.R
import timber.log.Timber


/**
 * @author 王漫生
 * @description APP是否在前台等判断
 */
object AppUtils {

    private const val NAVI_GAODE_PKG_COMMON: String = "com.desaysv.jetour.t1n.psmap"
    private const val INTENT_LAUNCHER_OPEN_APP: String = "com.desaysv.launcher.openapp"
    private const val LAUNCHER_PACKAGE_NAME: String = "com.desaysv.launcher"
    private const val LAUNCHER_ACTIVITY_NAME: String = "com.desaysv.launcher.activity.LauncherActivity"

    /**
     * 地图APP是否在前台
     */
    fun mapIsTopAPP(context: Context): Boolean {
        return isAppForeground(context.packageName, context) || isTopPackage(context)
    }

    //判断地图是否在前台
    private fun isTopPackage(context: Context): Boolean {
        return TextUtils.equals(context.packageName, getTopPackageName(context))
    }

    /**
     * 获取栈中最顶层的PackageName
     *
     * @param context
     * @return
     */
    private fun getTopPackageName(context: Context?): String {
        if (context == null) return ""
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningTaskInfos = manager.getRunningTasks(1)
        return runningTaskInfos?.get(0)?.topActivity?.packageName ?: ""
    }

    /**
     * Return whether application is foreground.
     * Target APIs greater than 21 must hold
     * `<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" />`
     * @param pkgName The name of the package.
     * @return `true`: yes<br></br>`false`: no
     */
    private fun isAppForeground(pkgName: String?, context: Context): Boolean {
        return !TextUtils.isEmpty(pkgName) && pkgName == getForegroundProcessName(context)
    }

    /**
     * Return the foreground process name.
     *
     * Target APIs greater than 21 must hold
     * `<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" />`
     *
     * @return the foreground process name
     */
    @SuppressLint("ObsoleteSdkInt")
    fun getForegroundProcessName(context: Context): String? {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val pInfo = am.runningAppProcesses
        if (pInfo != null && pInfo.isNotEmpty()) {
            return pInfo.find { it.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND }?.processName
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            val pm: PackageManager = context.packageManager
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            val list = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            Timber.i("ProcessUtils%s", list.toString())
            if (list.isEmpty()) {
                Timber.i("getForegroundProcessName: noun of access to usage information.")
                return ""
            }
            try { // Access to usage information.
                val info = pm.getApplicationInfo(context.packageName, 0)
                val aom = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
                if (aom?.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, info.uid, info.packageName) != AppOpsManager.MODE_ALLOWED) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
                if (aom?.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, info.uid, info.packageName) != AppOpsManager.MODE_ALLOWED) {
                    Timber.i("getForegroundProcessName: refuse to device usage stats.")
                    return ""
                }
                val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                val endTime = System.currentTimeMillis()
                val beginTime = endTime - 86400000 * 7
                val usageStatsList = usageStatsManager?.queryUsageStats(UsageStatsManager.INTERVAL_BEST, beginTime, endTime)
                if (usageStatsList.isNullOrEmpty()) return ""
                return usageStatsList.maxByOrNull { it.lastTimeUsed }?.packageName
            } catch (e: PackageManager.NameNotFoundException) {
                Timber.d("NameNotFoundException ${e.message}")
            }
        }
        return ""
    }

    /**
     * 获取版本号
     * @param context
     * @return
     */
    fun getAppVersionName(context: Context): String {
        var versionName = ""
        try {
            val pm = context.packageManager
            val pi = pm.getPackageInfo(context.packageName, 0)
            versionName = pi.versionName
            if (versionName == null || versionName.isEmpty()) {
                return ""
            }
        } catch (e: java.lang.Exception) {
            Timber.e("VersionInfo Exception ${e.message}")
        }
        return versionName
    }

    /**
     * 启动APP
     */
    fun launchApp(packageName: String, context: Context) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            Timber.e("应用程序未安装或无法启动")
        }
    }


    fun startOrBringActivityToFront(context: Context) {
        Timber.i("startOrBringActivityToFront() called with: context = $context")
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appTasks = activityManager.appTasks
        var isActivityRunning = false
        for (appTask in appTasks) {
            val taskInfo = appTask.taskInfo
            val componentName = taskInfo.baseIntent.component
            if (componentName!!.className == "com.desaysv.psmap.ui.MainActivity") {
                isActivityRunning = true
                break
            }
        }
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        if (isActivityRunning) {
            // 如果该 Activity 已经在后台，则将其拉到前台
            //val intent = Intent(context, activityClass)
            launchIntent?.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            context.startActivity(launchIntent, ActivityOptionsCompat.makeCustomAnimation(context, R.anim.scale_up, R.anim.fade_out).toBundle())
        } else {
            // 如果该 Activity 没有在后台，则启动该 Activity
            //val intent = Intent(context, activityClass)
            context.startActivity(launchIntent, ActivityOptionsCompat.makeCustomAnimation(context, R.anim.scale_up, R.anim.fade_out).toBundle())
        }
    }

    fun startOrBringActivityToFrontByLauncher(context: Context, isFullScreen: Boolean = true) {
        Timber.i("startOrBringActivityToFrontByLauncher() called with: context = $context, isFullScreen = $isFullScreen")
        //捷途导航在launcher的虚拟屏中启动，需要通过Launcher来拉起
        val intent = Intent();
        intent.setAction(INTENT_LAUNCHER_OPEN_APP)
        intent.putExtra("pkg", NAVI_GAODE_PKG_COMMON)
        intent.putExtra("isFullScreenShow", isFullScreen)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setClassName(LAUNCHER_PACKAGE_NAME, LAUNCHER_ACTIVITY_NAME)
        val optins = ActivityOptions.makeBasic()
        optins.setLaunchDisplayId(Display.DEFAULT_DISPLAY)
        context.startActivity(intent, optins.toBundle())
    }


}