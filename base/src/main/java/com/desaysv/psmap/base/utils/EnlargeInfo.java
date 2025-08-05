package com.desaysv.psmap.base.utils;

import android.graphics.Rect;

import com.autosdk.common.AutoConstant;
import com.autosdk.common.SdkApplicationUtils;
import com.autosdk.common.display.DisplayInfo;
import com.autosdk.common.utils.CardSizeHelper;
import com.autosdk.common.utils.ResUtil;
import com.desaysv.psmap.base.R;

import timber.log.Timber;

/**
 * 路口大图计算宽高
 */
public enum EnlargeInfo {

    /**
     * 横屏
     */
    LANDSCAPE {
        @Override
        public void adjustRect(DisplayInfo info) {
            this.mAppWidth = info.appWidth;
            this.mAppHeight = info.appHeight;
            final int margin = ResUtil.getDimension(R.dimen.sv_dimen_24);
            final int width = CardSizeHelper.getCardWidth(CardSizeHelper.CardType.DRIVE_CROSS, info);
            final int guideHeight = ResUtil.getDimension(R.dimen.sv_dimen_166);

            int height = info.appHeight - margin * 2 - guideHeight;

            int left = margin;
            int top = margin + guideHeight;
            int right = width - ResUtil.getDimension(R.dimen.sv_dimen_8);
            int bottom = top + height;

            this.mRect.set(left, top, right, bottom);
            Timber.d(" mRect : %s", mRect);
            Timber.d(" AutoConstant.mScreenWidth : %s  AutoConstant.mScreenHeight : %s", AutoConstant.mScreenWidth, AutoConstant.mScreenHeight);
        }
    };

    private static final String TAG = "EnlargeInfo";

    protected final Rect mRect = new Rect();

    private Rect mRectEx1 = null;

    protected int mAppWidth;
    protected int mAppHeight;

    public static EnlargeInfo getInstance() {
        return LANDSCAPE;
    }

    public boolean checkScreen(DisplayInfo info) {
        return mAppWidth == info.appWidth && mAppHeight == info.appHeight;
    }

    public Rect getRect() {
        Timber.i(this.name() + " getRect() : " + mRect);
        return mRect;
    }

    public void setRect(int left, int top, int right, int bottom) {
        mRect.set(left, top, right, bottom);
    }

    //设置路口放大图的位置和大小
    public void setEnlargeCrossImageSize(float x, float y) {
        int left = ResUtil.getAutoDimenValue(SdkApplicationUtils.getApplication().getApplicationContext(), R.dimen.sv_dimen_40);
        int top = ResUtil.getAutoDimenValue(SdkApplicationUtils.getApplication().getApplicationContext(), R.dimen.sv_dimen_280);
        int right = left + ResUtil.getAutoDimenValue(SdkApplicationUtils.getApplication().getApplicationContext(), R.dimen.sv_dimen_640);
        int bottom = top + ResUtil.getAutoDimenValue(SdkApplicationUtils.getApplication().getApplicationContext(), R.dimen.sv_dimen_405);
        setRect(left, top, right, bottom);
        Timber.i("setEnlargeCrossImageSize:%s", getRect().toString());
    }

    public Rect getEx1Rect() {
        if (mRectEx1 == null) {
            mRectEx1 = new Rect();
            int left = ResUtil.getAutoDimenValue(SdkApplicationUtils.getApplication().getApplicationContext(), R.dimen.sv_dimen_76);
            int top = ResUtil.getAutoDimenValue(SdkApplicationUtils.getApplication().getApplicationContext(), R.dimen.sv_dimen_235);
            int right = left + ResUtil.getAutoDimenValue(SdkApplicationUtils.getApplication().getApplicationContext(), R.dimen.sv_dimen_460);
            int bottom = top + ResUtil.getAutoDimenValue(SdkApplicationUtils.getApplication().getApplicationContext(), R.dimen.sv_dimen_333);
            mRectEx1.set(left, top, right, bottom);
        }
        return mRectEx1;
    }

    /**
     * 校准路口放大图显示位置
     */
    public abstract void adjustRect(DisplayInfo info);

}
