package com.autosdk.bussiness.account.utils;

import android.media.MediaRecorder;
import android.os.Environment;

import com.autosdk.bussiness.common.utils.FileUtils;

import java.io.IOException;

/**
 * Created by AutoSdk on 2021/5/10.
 **/
public class SoundRecorderUtils {

    private static MediaRecorder mMediaRecorder;
    private static long mStartingTimeMillis;
    private static long mElapsedMillis = 0;
    public static  String mFilePath = "";
    //1正在录音，0无录音
    public static  int  mStatus = 0;

    public static void startRecording(String dirpath,String fileName) {
        if (mMediaRecorder == null){
            mMediaRecorder = new MediaRecorder();
        }
        FileUtils.createDir(dirpath);
        mFilePath = dirpath + fileName+".amr";
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
        mMediaRecorder.setOutputFile(mFilePath);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setAudioChannels(1);
        mMediaRecorder.setAudioSamplingRate(44100);
        mMediaRecorder.setAudioEncodingBitRate(192000);

        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            mStatus = 1;
            mStartingTimeMillis = System.currentTimeMillis();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void stopRecording() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mStatus = 0;
            mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    public static long getRecordTime() {
          return mElapsedMillis;
    }

    public static long currentRecordTime() {
          return  System.currentTimeMillis() - mStartingTimeMillis;
    }

}
