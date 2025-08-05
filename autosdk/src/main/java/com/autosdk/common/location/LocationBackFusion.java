package com.autosdk.common.location;

import static android.content.Context.SENSOR_SERVICE;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;

import com.autonavi.gbl.pos.model.LocAcce3d;
import com.autonavi.gbl.pos.model.LocDataType;
import com.autonavi.gbl.pos.model.LocGyro;
import com.autonavi.gbl.pos.model.LocThreeAxis;
import com.autosdk.bussiness.location.LocationController;
import com.autosdk.bussiness.location.utils.CustomTimer;
import com.autosdk.bussiness.location.utils.CustomTimerTask;

import java.math.BigInteger;

import timber.log.Timber;

public class LocationBackFusion extends LocationInstrument implements SensorEventListener {

    private static final String TAG = "LocationBackFusion";

    // 加速度计单位（系统给的是m/s^2，需要转换成定位需要的g）
    private static final double ACC_UNIT = 9.81;

    // 陀螺仪单位（系统给的是red/s，需要转成度/s)
    private static final double GYR_UNIT = 180 / 3.1415926;

    private SensorManager mSensorManager;

    private float mTemperature = 0;

    private float accXValue;
    private float accYValue;
    private float accZValue;
    private long accTimeStamp;
    private long mAccTime = 0;

    private float gyroXValue;
    private float gyroYValue;
    private float gyroZValue;
    private long gyroTimeStamp;
    private long mGyroTime = 0;

    private boolean mAccCallback, mGyroCallback;

    private static final int INTREV_AL = 100;//如果是后端融合频率要求10Hz 设置100毫秒


    private CustomTimer mGyrScheduledTimer;//陀螺仪 定时器
    private CustomTimerTask mGyrScheduledTimerTask;

    /**
     * 开始100毫秒获取一次陀螺仪
     */
    public void doGyrStart() {
        cancleGyr();
        if (mGyrScheduledTimer == null) {
            mGyrScheduledTimer = new CustomTimer();
        }
        if (mGyrScheduledTimerTask == null) {
            Timber.i("doGyrStart ");
            mGyrScheduledTimerTask = new CustomTimerTask() {
                @Override
                public void run() {
                    if (mGyroTime == 0) {
                        mGyroTime = SystemClock.elapsedRealtime();
                    }
                    long gyroTime = SystemClock.elapsedRealtime() - mGyroTime;
                    mGyroTime = SystemClock.elapsedRealtime();
                    LocGyro sensorData = new LocGyro();
                    sensorData.axis = LocThreeAxis.LocAxisAll; // 有效数据轴
                    sensorData.valueX = gyroXValue;
                    sensorData.valueY = gyroYValue;
                    sensorData.valueZ = gyroZValue;
                    sensorData.temperature = mTemperature;
//                Timber.v("陀螺仪温度 sensorData.temperature: " + sensorData.temperature);
                    sensorData.tickTime = BigInteger.valueOf(mGyroTime);
                    sensorData.interval = (int) gyroTime;
                    sensorData.dataType = LocDataType.LocDataGyro;
                    if (mGyroCallback) {
                        LocationController.getInstance().setLocGyroInfo(sensorData);
                    }
                }
            };
        }
        mGyrScheduledTimer.scheduleAtFixedRate(mGyrScheduledTimerTask, 0, INTREV_AL);
    }

    /**
     * 取消定时器
     */
    public void cancleGyr() {
        mGyroCallback = false;
        if (mGyrScheduledTimer != null && mGyrScheduledTimerTask != null) {
            mGyrScheduledTimer.cancel();
            mGyrScheduledTimerTask.cancel();
            mGyrScheduledTimerTask = null;
            mGyrScheduledTimer = null;
        }
        Timber.i("cancleGyr ");
    }

    private CustomTimer mAccScheduledTimer;//加速度计 定时器
    private CustomTimerTask mAccScheduledTimerTask;

    /**
     * 开始100毫秒获取一次陀螺仪
     */
    public void doAccStart() {
        cancleAcc();
        if (mAccScheduledTimer == null) {
            mAccScheduledTimer = new CustomTimer();
        }
        if (mAccScheduledTimerTask == null) {
            Timber.i("doAccStart ");
            mAccScheduledTimerTask = new CustomTimerTask() {
                @Override
                public void run() {
                    if (mAccTime == 0) {
                        mAccTime = SystemClock.elapsedRealtime();
                    }
                    long accTime = SystemClock.elapsedRealtime() - mAccTime;
                    mAccTime = SystemClock.elapsedRealtime();
                    LocAcce3d sensorData = new LocAcce3d();
                    sensorData.axis = LocThreeAxis.LocAxisAll; // 有效数据轴
                    sensorData.acceX = accXValue;
                    sensorData.acceY = accYValue;
                    sensorData.acceZ = accZValue;
                    sensorData.tickTime = BigInteger.valueOf(mAccTime);
                    sensorData.interval = (int) accTime;
                    sensorData.dataType = LocDataType.LocDataAcce3D;
//            Timber.i("sensorData: accXValue=" + sensorData.acceX + ", accYValue=" + sensorData.acceY + ", accZValue=" + sensorData.acceZ);
                    if (mAccCallback) {
                        LocationController.getInstance().setLocAcce3DInfo(sensorData);
                    }
                }
            };
        }
        mAccScheduledTimer.scheduleAtFixedRate(mAccScheduledTimerTask, 0, INTREV_AL);
    }

    /**
     * 取消定时器
     */
    public void cancleAcc() {
        mAccCallback = false;
        if (mAccScheduledTimer != null && mAccScheduledTimerTask != null) {
            mAccScheduledTimer.cancel();
            mAccScheduledTimerTask.cancel();
            mAccScheduledTimerTask = null;
            mAccScheduledTimer = null;
        }
        Timber.i("cancleAcc ");
    }

    public LocationBackFusion(Context context) {
        super(context);
        mSensorManager = (SensorManager) mContext.getSystemService(SENSOR_SERVICE);
    }

    @Override
    public void doStartLocate() {
        super.doStartLocate();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), 1000 * 100);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 1000 * 100);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE), 1000 * 100);
        doGyrStart();
        doAccStart();
        Timber.i("doStartLocate");
    }

    @Override
    public void doStopLocate() {
        super.doStopLocate();
        cancleGyr();
        cancleAcc();
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        mSensorManager.unregisterListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE));
    }

    @Override
    public void onDestory() {
        super.onDestory();
        cancleGyr();
        cancleAcc();
    }

    @Override
    public void doTimerStart() {
        Timber.i("doTimerStart");
        doGyrStart();
        doAccStart();
    }

    @Override
    public void doTimerStop() {
        Timber.i("doTimerStop");
        cancleGyr();
        cancleAcc();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        handleGyroAndAccData(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * 处理陀螺仪、脉冲等传感器数据
     *
     * @param event
     */
    private void handleGyroAndAccData(final SensorEvent event) {
        if (event == null) {
            Timber.d("handleGyroAndAccData: sensorEvent=null");
            return;
        }
        int sensorType = event.sensor.getType();
        //Timber.d("handleGyroAndAccData: sensorType=%s", sensorType);
        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            float[] values = event.values;
            if (values != null && values.length >= 3) {
                mAccCallback = true;
                accXValue = (float) (values[0] / ACC_UNIT);
                accYValue = (float) (values[1] / ACC_UNIT);
                accZValue = (float) (values[2] / ACC_UNIT);
                /*accTimeStamp = SystemClock.elapsedRealtime();
                Timber.d("handleGyroAndAccData: accXValue=%s, accYValue=%s, accZValue=%s, accTimestamp=%s",
                        accXValue, accYValue, accZValue, accTimeStamp);*/
            } else {
                Timber.d("handleGyroAndAccData: accValue=null");
            }
        } else if (sensorType == Sensor.TYPE_GYROSCOPE) {
            float[] values = event.values;
            if (values != null && values.length >= 3) {
                mGyroCallback = true;
                gyroXValue = (float) (values[0] * GYR_UNIT);
                gyroYValue = (float) (values[1] * GYR_UNIT);
                gyroZValue = (float) (values[2] * GYR_UNIT);
                gyroTimeStamp = SystemClock.elapsedRealtime();
                /*Timber.d("handleGyroAndAccData: gyroXValue=%s, gyroYValue=%s, gyroZValue=%s, mTemperature=%s, gyroTimestamp=%s",
                        gyroXValue, gyroYValue, gyroZValue, mTemperature, gyroTimeStamp);*/
            } else {
                Timber.d("handleGyroAndAccData: gyroValue=null");
            }
        }
//        else if (sensorType == Sensor.TYPE_AMBIENT_TEMPERATURE) {// TODO: 2020/12/25 陀螺温度可通过温度传感器上报上来
//            float[] values = event.values;
//            if (values != null && values.length >= 1) {
//                mTemperature = values[0];
//            }
//        }
    }


}
