package com.desaysv.psmap.model.business

import android.app.Application
import android.content.Intent
import com.desaysv.ivi.vdb.client.VDBus
import com.desaysv.ivi.vdb.event.base.VDValue
import com.desaysv.ivi.vdb.event.id.bt.VDEventBT
import com.desaysv.ivi.vdb.event.id.bt.VDValueBT
import com.desaysv.ivi.vdb.event.id.bt.bean.VDBTDevice
import com.desaysv.ivi.vdb.event.id.bt.bean.VDBTDial
import com.desaysv.ivi.vdb.event.id.bt.bean.VDBTEnable
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 蓝牙电话模块
 */
@Singleton
class BluetoothBusiness @Inject constructor(private val application: Application) {

    /**获取蓝牙开关状态
     *@return {@link VDValue.EnableStatus}
     */
    fun getBTEnable(): Boolean {
        if (CommonUtils.isVehicle()) {
            val event = VDBus.getDefault().getOnce(VDEventBT.SETTING_ENABLE)
            val param = VDBTEnable.getValue(event)
            if (param == null) {
                Timber.i("getBTEnable: param = null")
                return false
            }
            return param.enableStatus == VDValue.EnableStatus.ENABLED
        }
        Timber.i("麻烦在车机上调试")
        return false
    }

    /**获取主蓝牙设备信息
     *@return主蓝牙设备信息
     */
    fun getMasterDevice(): VDBTDevice? {
        if (CommonUtils.isVehicle()) {
            val event = VDBus.getDefault().getOnce(VDEventBT.SETTING_MASTER_DEVICE) ?: return null
            return VDBTDevice.getValue(event)
        }
        Timber.i("麻烦在车机上调试")
        return null
    }

    /**根据设备地址拨打电话
     *@param address 设备地址
     *@param number 电话号码
     */
    fun dial(address: String, number: String) {
        if (CommonUtils.isVehicle()) {
            val param = VDBTDial().apply {
                putAddress(address)
                putDialCtrl(VDValueBT.DialCtrl.DIAL)
                putNumber(number)
            }
            VDBus.getDefault().set(VDBTDial.createEvent(VDEventBT.DIAL, param))
            return
        }
        Timber.i("麻烦在车机上调试")
    }

    /**
     * 跳转设置模块蓝牙界面
     */
    fun startSettingBluetooth() {
        try {
            if (CommonUtils.isVehicle()) {
                val intent = Intent(BaseConstant.ACTION_BLUETOOTH)
                intent.setPackage(BaseConstant.SETTING)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                application.startActivity(intent)
            } else {
                Timber.i("麻烦在车机上调试")
            }
        } catch (e: Exception) {
            Timber.d("startSettingBluetooth: Exception e: ${e.message}")
        }
    }

    //判断是否拨号还是进入蓝牙设置界面
    fun callPhone(number: String) {
        if (CommonUtils.isVehicle()) {
            val btEnable = getBTEnable()
            val vDBTDevice = getMasterDevice()
            if (btEnable && vDBTDevice != null) {//蓝牙连接了
                dial(vDBTDevice.mac, number)
            } else {
                Timber.i("蓝牙没有连接")
                startSettingBluetooth()
            }
        } else {
            Timber.i("麻烦在车机上调试")
        }
    }
}