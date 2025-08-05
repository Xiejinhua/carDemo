package com.desaysv.psmap.ui.navi.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.autonavi.gbl.common.path.model.LightBarItem;
import com.autonavi.gbl.common.path.model.TrafficStatus;
import com.autosdk.common.utils.ResUtil;
import com.desaysv.psmap.base.bean.MapLightBarItem;

import java.util.List;

/**
 * 柱状图自定义view
 */
public class TmcBarHorizontalView extends View {
    // 没有路况
    private int NOTRAFFIC = 0;
    private int NOTRAFFIC_NIGHT = 0;
    // 未知状态
    private int UNKNOWN = 0;
    private int UNKNOWN_NIGHT = 0;
    // 畅通
    private int UNBLOCK = 0;
    private int UNBLOCK_NIGHT = 0;
    // 缓行
    private int SLOW = 0;
    private int SLOW_NIGHT = 0;
    // 拥堵
    private int BLOCK = 0;
    private int BLOCK_NIGHT = 0;
    // 严重拥堵
    private int GRIDLOCKED = 0;
    private int GRIDLOCKED_NIGHT = 0;

    private List<MapLightBarItem> tmcBarItems;
    private long mRouteTotalLength;
    private float mCursorPos;
    private boolean mIsNightMode;
    /**
     * 绘制TmcBar的公用画笔
     */
    private Paint mPaint;
    private final Path mClipPath = new Path();

    private TmcBarHorizontalViewFirstDraw mFirstDrawListener;

    private boolean mIsNetworkConnected = true;

    public TmcBarHorizontalView(Context context) {
        super(context);
        intTmcBarColor(context);
    }

    public TmcBarHorizontalView(Context context, AttributeSet attrs) {
        super(context, attrs);
        intTmcBarColor(context);
    }

    public TmcBarHorizontalView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        intTmcBarColor(context);
    }

    private void intTmcBarColor(Context mContext) {
        // 没有路况
        NOTRAFFIC = ResUtil.getColor(com.desaysv.psmap.model.R.color.auto_color_727577);
        NOTRAFFIC_NIGHT = ResUtil.getColor(com.desaysv.psmap.model.R.color.auto_color_5e5f61);
        // 未知状态
        UNKNOWN = ResUtil.getColor(com.desaysv.psmap.model.R.color.auto_color_1d8cf5);
        UNKNOWN_NIGHT = ResUtil.getColor(com.desaysv.psmap.model.R.color.auto_color_0a80fb);
        // 畅通
        UNBLOCK = ResUtil.getColor(com.desaysv.psmap.model.R.color.auto_color_42b986);
        UNBLOCK_NIGHT = ResUtil.getColor(com.desaysv.psmap.model.R.color.auto_color_0ca763);
        // 缓行
        SLOW = ResUtil.getColor(com.desaysv.psmap.model.R.color.auto_color_f4cf4b);
        SLOW_NIGHT = ResUtil.getColor(com.desaysv.psmap.model.R.color.auto_color_bfa92e);
        // 拥堵
        BLOCK = ResUtil.getColor(com.desaysv.psmap.model.R.color.auto_color_e85466);
        BLOCK_NIGHT = ResUtil.getColor(com.desaysv.psmap.model.R.color.auto_color_bf714d);
        // 严重拥堵
        GRIDLOCKED = ResUtil.getColor(com.desaysv.psmap.model.R.color.auto_color_c04361);
        GRIDLOCKED_NIGHT = ResUtil.getColor(com.desaysv.psmap.model.R.color.auto_color_81132b);
    }

    public void setData(List<MapLightBarItem> items, long totalLength) {
        if (items != null) {
            tmcBarItems = items;
        } else {
            tmcBarItems = null;
        }
        mRouteTotalLength = totalLength;
    }

    public void updateNetworkStatus(boolean isConnect) {
        mIsNetworkConnected = isConnect;
    }

    public void setFirstDrawListener(TmcBarHorizontalViewFirstDraw listener) {
        mFirstDrawListener = listener;
    }

    /**
     * 设置游标位置
     *
     * @param cursorPos
     */
    public void setCursorPos(float cursorPos) {
        mCursorPos = cursorPos;
        postInvalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initClipPath(getWidth(), getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        Path clipPath = mClipPath;

        canvas.save();
        canvas.clipPath(clipPath);

        if (mFirstDrawListener != null) {
            mFirstDrawListener.onDraw(width, height);
            mFirstDrawListener = null;
        }
        if (tmcBarItems != null) {
            //计算过程中实际距离转换成像素的长度累加
            int pixelDistanceSum = 0;
            //距离和View高度的比率,用于在view高度和实际距离之间进行转换,单位:像素/米
            float rateDistanceToViewWidth = (width * 1.0f) / (mRouteTotalLength * 1.0f);
            //从上往下绘制
            for (int i = 0; i < tmcBarItems.size(); i++) {
                MapLightBarItem item = tmcBarItems.get(i);
                if (item != null) {
                    //计算tmcBar在绘制过程中每一小段的长度
                    float itemWidth = Math.round(item.getLength() * rateDistanceToViewWidth);

                    canvas.drawRect(pixelDistanceSum + mCursorPos, height, pixelDistanceSum + mCursorPos + itemWidth, 0, getPaintInColor(getColor(item.getStatus())));// 画矩形
                    pixelDistanceSum += itemWidth;
                }
            }

            //走过的路使用灰色
            if (width > mCursorPos) {
                canvas.drawRect(0, height, mCursorPos, 0, getPaintInColor(mIsNightMode ? NOTRAFFIC_NIGHT : NOTRAFFIC));// 画矩形
            }
        }
        canvas.restore();
    }

    /**
     * 获取指定颜色的画笔.
     *
     * @param color 画笔颜色
     * @return 设置成指定颜色的画笔
     */
    private Paint getPaintInColor(int color) {
        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
        }
        mPaint.setColor(color);
        return mPaint;
    }

    private void initClipPath(int width, int height) {
        mClipPath.reset();
        float[] radiusArray = new float[8];
        for (int i = 0; i < radiusArray.length; i++) {
            radiusArray[i] = width / 2f;
        }
        RectF rectF = new RectF();
        rectF.set(0, 0, width, height);
//        mClipPath.addRoundRect(rectF, radiusArray, Path.Direction.CW);
        mClipPath.addRect(rectF, Path.Direction.CW);
    }

    /**
     * 根据路况取对应的颜色值
     *
     * @param status
     * @return
     */
    private int getColor(int status) {
        // 网络未连接，并且不是等于已经走过的路
        if (!mIsNetworkConnected && status != TrafficStatus.AUTO_UNKNOWN_ERROR) {
            return mIsNightMode ? UNKNOWN_NIGHT : UNKNOWN;
        }

        switch (status) {
            case TrafficStatus.TrafficStatusUnkonw: // 未知路况：蓝色
                return mIsNightMode ? UNKNOWN_NIGHT : UNKNOWN;
            case TrafficStatus.TrafficStatusOpen: // 畅通：绿色
                return UNBLOCK;
            case TrafficStatus.TrafficStatusSlow: // 缓行：黄色
                return mIsNightMode ? SLOW_NIGHT : SLOW;
            case TrafficStatus.TrafficStatusJam: // 拥堵：红色
                return mIsNightMode ? BLOCK_NIGHT : BLOCK;
            case TrafficStatus.TrafficStatusCongested: // 严重拥堵：深红
                return mIsNightMode ? GRIDLOCKED_NIGHT : GRIDLOCKED;
            case TrafficStatus.TrafficStatusExtremelyOpen: // 极度畅通：深绿色
                return UNBLOCK_NIGHT;
            default: // 没有路况：灰色
                return mIsNightMode ? NOTRAFFIC_NIGHT : NOTRAFFIC;
        }
    }

    public void setNightMode(boolean isNightMode) {
        mIsNightMode = isNightMode;
        refresh();
    }

    public void refresh() {
        invalidate();
    }

    public interface TmcBarHorizontalViewFirstDraw {
        void onDraw(int width, int height);
    }
}
