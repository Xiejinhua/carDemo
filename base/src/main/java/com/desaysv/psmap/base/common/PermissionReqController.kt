package com.desaysv.psmap.base.common

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.desaysv.psmap.base.utils.BaseConstant
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.LinkedList
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 权限请求管理类
 */
@Singleton
class PermissionReqController @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        const val PERMISSION_OK = "permission_ok"
    }

    private lateinit var activity: Activity

    //申请中的权限
    private val requestPermissions: MutableList<String> = LinkedList()

    //必要权限 捷途FL2定制化FL2定制化，需要检查的权限
    private val checkRequiredPermissions: List<String> = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.RECORD_AUDIO
    )

    //提示需要这些权限的理由
    private val showRequestRationale: MutableList<String> = LinkedList()
    private val _showRequestPermissionRationale: MutableLiveData<List<String>> = MutableLiveData(showRequestRationale)//需要提示用户需要此权限的理由
    val showRequestPermissionRationale: LiveData<List<String>> = _showRequestPermissionRationale

    /**
     * 申请权限
     */
    fun requestPermissions(mainActivity: Activity) {
        if (checkRequiredPermissions.isEmpty()) {
            Timber.w("requestPermissions isEmpty")
            return
        }
        activity = mainActivity
        showRequestRationale.clear()
        requestPermissions.clear()
        requestPermissions.addAll(lackOfPermissions())
        if (requestPermissions.isNotEmpty()) {
            /* 捷途FL2定制化，注释掉
            if (activity.shouldShowRequestPermissionRationale(requestPermissions[0])) {
                showRequestPermission.add(requestPermissions[0])
                requestNextPermissions()
            } else {
                activity.requestPermissions(arrayOf(requestPermissions[0]), BaseConstant.PERMISSION_REQUEST_CODE)
            }*/
            activity.requestPermissions(arrayOf(requestPermissions[0]), BaseConstant.PERMISSION_REQUEST_CODE)
        } else {
            _showRequestPermissionRationale.postValue(listOf(PERMISSION_OK))
        }

    }

    /**
     * 检查运行必要权限
     */
    fun requiredPermissionsIsOk(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        checkRequiredPermissions.forEach {
            if (context.checkSelfPermission(it) == PackageManager.PERMISSION_DENIED) {
                Timber.i("$it PERMISSION_DENIED")
                return false
            }
        }
        return true
    }

    private fun requestNextPermissions(hasResult: Boolean = true) {
        if (hasResult)
            requestPermissions.removeAt(0)
        Timber.i("requestNextPermissions $requestPermissions")
        if (requestPermissions.isNotEmpty()) {
            /*if (activity.shouldShowRequestPermissionRationale(requestPermissions[0])) {
                showRequestPermission.add(requestPermissions[0])
                requestNextPermissions()
            } else {
                activity.requestPermissions(arrayOf(requestPermissions[0]), BaseConstant.PERMISSION_REQUEST_CODE)
            }*/
            activity.requestPermissions(arrayOf(requestPermissions[0]), BaseConstant.PERMISSION_REQUEST_CODE)
        } else {
            _showRequestPermissionRationale.postValue(showRequestRationale)
            Timber.i("shouldShowRequestPermissionRationale $showRequestRationale")
        }
    }

    private fun lackOfPermissions(): List<String> {
        val lackPermissions: MutableList<String> = LinkedList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkRequiredPermissions.forEach {
                if (it == Settings.ACTION_MANAGE_OVERLAY_PERMISSION && !Settings.canDrawOverlays(context)) { //悬浮窗弹窗
                    lackPermissions.add(it)
                }
                if (it != Settings.ACTION_MANAGE_OVERLAY_PERMISSION && context.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED) {
                    lackPermissions.add(it)
                }
            }
        }
        Timber.i("lackOfPermissions $lackPermissions")
        return lackPermissions
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Timber.i("onRequestPermissionsResult $requestCode ${permissions.contentToString()} ${grantResults.contentToString()}")
        if (BaseConstant.PERMISSION_REQUEST_CODE == requestCode) {
            if (permissions.isNotEmpty() && permissions[0] == requestPermissions[0]) {
                if (context.checkSelfPermission(permissions[0]) == PackageManager.PERMISSION_GRANTED) {
                    Timber.i("onRequestPermissionsResult ${permissions[0]} PERMISSION_GRANTED")
                    requestNextPermissions()//请求下一个权限
                } else {
                    Timber.i("onRequestPermissionsResult ${permissions[0]} PERMISSION_DENIED")
                    showRequestRationale.add(permissions[0])//拒绝加入提示
                    requestNextPermissions()//请求下一个权限
                }
            } else {
                requestNextPermissions(false)
            }
        }
    }

    fun clearPermissions() {
        Timber.i("clearPermissions")
        requestPermissions.clear()
        showRequestRationale.clear()
    }

    fun isPermissionRequesting(): Boolean {
        return requestPermissions.isNotEmpty().also {
            Timber.i("isPermissionRequesting $it")
        }
    }

    fun notificationPermissionUse(context: Context, type: String? = null, used: Boolean? = null, reset: Boolean = false) {
        Timber.d("notificationPermissionUse $type $used $reset")
        val typeInt = when (type) {
            Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION -> 0
            Manifest.permission.RECORD_AUDIO -> 1
            Manifest.permission.CAMERA -> 2
            else -> -1
        }
        val intent = Intent("com.android.systemui.action_PERMISSION")
        intent.setPackage("com.android.systemui")
        //value 替换为要发送应用的包名，不可随意更换
        intent.putExtra("package", context.packageName)
        //value为true表示首次启动应用，所有权限都不使用；为false表示有使用权限，具体权限参考以下两个值
        intent.putExtra("reset", reset)
        if (!reset) {
            //可选，reset为true时可不发，reset为false时要下发，0表示位置，1表示麦克风，2表示摄像头
            intent.putExtra("type", typeInt)
            //可选，reset为true时可不发，reset为false时要下发，0表示没有使用，1表示正在使用
            intent.putExtra("being_used", if (used == true) 1 else 0)
        }
        context.sendBroadcast(intent)
    }
}