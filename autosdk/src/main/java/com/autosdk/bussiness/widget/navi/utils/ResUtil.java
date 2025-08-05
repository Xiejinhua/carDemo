package com.autosdk.bussiness.widget.navi.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.TypedValue;

import com.autosdk.bussiness.widget.BusinessApplicationUtils;

import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

/**
 * Res资源Utils
 */
public class ResUtil {

    private static final String TAG = ResUtil.class.getSimpleName();
    public static byte[] decodeAssetResData(Context context,
                                            String resName) {
        // on 1.6 later

        AssetManager assetManager = context.getAssets();

        InputStream is = null;
        java.io.ByteArrayOutputStream bout = null;
        try {
            is = assetManager.open(resName);
            bout = new java.io.ByteArrayOutputStream();

            byte[] bufferByte = new byte[1024];
            int l = -1;
            while ((l = is.read(bufferByte)) > -1) {
                bout.write(bufferByte, 0, l);
            }
            byte[] rBytes = bout.toByteArray();
            return rBytes;
        } catch (IOException e) {
            Timber.e(  e,"decodeAssetResData %s not found", resName);
            return null;
        } catch (OutOfMemoryError e) {
            Timber.e(  e,"decodeAssetResData %s OutOfMemoryError", resName);
            return null;
        } catch (Exception e){
            Timber.e(  e,"decodeAssetResData %s exception", resName);
            return null;
        }finally {
            try {
                if (bout != null) {
                    bout.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                //ignore
            }
        }
    }

    public static int dipToPixel(Context context, int dipValue) {
        if (context == null) {
            // 原值返回
            return dipValue;
        }
        try {
            float pixelFloat = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, dipValue, context
                            .getResources().getDisplayMetrics());
            return (int) pixelFloat;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dipValue;
    }

    public static int getAutoDimenValue(Context mContext,int resId){
        if (mContext != null && mContext.getResources() != null) {
            return (int) (mContext.getResources().getDimension(resId));
        } else {
            return 0;
        }
    }
    public static String getString(int resId){
        return getResources().getString(resId);
    }

    public static String getString(int resId, Object... formatArgs){
        return getResources().getString(resId,formatArgs);
    }

    public static int getDimension(int resId) {
        return (int) getResources().getDimension(resId);
    }


    public static int getColor(int resId) {
        return getResources().getColor(resId);
    }

    public static Resources getResources(){
        return BusinessApplicationUtils.getApplication().getApplicationContext().getResources();
    }

    public static float getFloatDimension(int resId) {
        return getResources().getDimension(resId);
    }

}

