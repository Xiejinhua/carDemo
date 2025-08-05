package com.autosdk.common.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;

import com.autosdk.common.SdkApplicationUtils;

import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

public class ResUtil {

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
            Timber.e(e, "decodeAssetResData %s not found", resName);
//            com.autonavi.common.utils.CatchExceptionUtil.normalPrintStackTrace(e);
            return null;
        } catch (OutOfMemoryError e) {
            Timber.e(e, "decodeAssetResData %s OutOfMemoryError", resName);
//            com.autonavi.common.utils.CatchExceptionUtil.normalPrintStackTrace(e);
            return null;
        } catch (Exception e) {
            Timber.e(e, "decodeAssetResData %s exception", resName);
            return null;
        } finally {
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
            return dipValue; // 原值返回
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

    public static int getAutoDimenValue(Context mContext, int resId) {
        if (mContext != null && mContext.getResources() != null) {
            return (int) (mContext.getResources().getDimension(resId));
        } else {
            return 0;
        }
    }

    public static int getColorWithAlpha(Context mContext, int colorId, int alphaId) {

        String alphaStr = getResources().getString(alphaId);
        int alpha = (int) (Double.parseDouble(alphaStr) * 255.0);

        int color = mContext.getResources().getColor(colorId);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public static String getString(int resId) {
        return getResources().getString(resId);
    }

    public static String getString(int resId, Object... formatArgs) {
        return getResources().getString(resId, formatArgs);
    }

    public static int getDimension(int resId) {
        return (int) getResources().getDimension(resId);
    }


    public static int getColor(int resId) {
        return getResources().getColor(resId);
    }

    public static Resources getResources() {
        //TODO 删除cc
        return SdkApplicationUtils.getApplication().getApplicationContext().getResources();
    }

    public static int countWidgetViewWidth(View view) {
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(widthMeasureSpec, heightMeasureSpec);
        return view.getMeasuredWidth();
    }

    /**
     * 测量View宽高
     *
     * @param view
     * @return 测量完成View的measureWidth 与measureHeight将会有值
     */
    public static void measureView(View view) {
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(widthMeasureSpec, heightMeasureSpec);
    }


    public static BitmapFactory.Options getImageWidthHeight(int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        //返回的bitmap为空，options.outHeight已计算
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeResource(SdkApplicationUtils.getApplication().getResources(), resId, options); // 此时返回的bitmap为null
        //options.outHeight为原始图片宽高
        return options;
    }
}
