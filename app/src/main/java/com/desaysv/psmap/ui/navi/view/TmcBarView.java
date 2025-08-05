package com.desaysv.psmap.ui.navi.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.autonavi.gbl.common.path.model.TrafficStatus;
import com.autosdk.common.utils.ResUtil;
import com.desaysv.psmap.base.bean.MapLightBarItem;
import com.desaysv.psmap.model.R;

import java.util.List;

/**
 * 柱状图自定义view
 */
public class TmcBarView extends View {

    // 没有路况
    private static final int NOTRAFFIC = ResUtil.getColor(R.color.auto_color_727577);
    private static final int NOTRAFFIC_NIGHT = ResUtil.getColor(R.color.auto_color_5e5f61);

    // 未知状态
    private static final int UNKNOWN = ResUtil.getColor(R.color.auto_color_1d8cf5);
    private static final int UNKNOWN_NIGHT = ResUtil.getColor(R.color.auto_color_0a80fb);

    // 畅通
    private static final int UNBLOCK = ResUtil.getColor(R.color.auto_color_42b986);
    private static final int UNBLOCK_NIGHT = ResUtil.getColor(R.color.auto_color_0ca763);

    // 缓行
    private static final int SLOW = ResUtil.getColor(R.color.auto_color_f4cf4b);
    private static final int SLOW_NIGHT = ResUtil.getColor(R.color.auto_color_bfa92e);

    // 拥堵
    private static final int BLOCK = ResUtil.getColor(R.color.auto_color_e85466);
    private static final int BLOCK_NIGHT = ResUtil.getColor(R.color.auto_color_bf714d);

    // 严重拥堵
    private static final int GRIDLOCKED = ResUtil.getColor(R.color.auto_color_c04361);
    private static final int GRIDLOCKED_NIGHT = ResUtil.getColor(R.color.auto_color_81132b);

    /**
     * 显示Pop距离的最小阈值
     */
    private static final int DISTANCE_MIN = 10;
    /**
     * 显示Pop距离的最大阈值
     */
    private static final int DISTANCE_MID = 5 * 1000;
    /**
     * 显示Pop距离的最大阈值
     */
    private static final int DISTANCE_MAX = 50 * 1000;
    private List<MapLightBarItem> tmcBarItems;
    private long mRouteTotalLength;
    private float mCursorPos;
    private TmcBarListener mTmcBarListener;
    private boolean mIsNightMode;
    /**
     * 由于canvas中操作tmcTag对象过于频繁，性能考虑cache一份
     */
    private final TmcTag mTagCache = new TmcTag();
    /**
     * 绘制TmcBar的公用画笔
     */
    private Paint mPaint;
    private final Path mClipPath = new Path();

    private TmcBarViewFirstDraw mFirstDrawListener;

    private boolean mIsNetworkConnected = true;

    public TmcBarView(Context context) {
        super(context);
    }

    public TmcBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TmcBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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

    public void setFirstDrawListener(TmcBarViewFirstDraw listener) {
        mFirstDrawListener = listener;
    }

    /**
     * 设置游标位置
     *
     * @param cursorPos
     */
    public void setCursorPos(float cursorPos) {
        mCursorPos = cursorPos;
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
        if (tmcBarItems != null) { //计算矩形内的填充高度
            int tmcBarLength = tmcBarItems.size();
            //计算过程中实际距离转换成像素的长度累加
            int pixelDistanceSum = 0;
            //计算过程中实际距离的累加
            int realDistanceSum = 0;
            //标记是否有需要显示Pop信息的Item
            boolean shouldPopItem = false;
            //距离和View高度的比率,用于在view高度和实际距离之间进行转换,单位:像素/米
            float rateDistanceToViewHeight = (height * 1.0f) / (mRouteTotalLength * 1.0f);
            //从上往下绘制
            for (int i = tmcBarLength - 1; i >= 0; i--) {
                MapLightBarItem item = tmcBarItems.get(i);
                realDistanceSum += item.getLength();
                //计算tmcBar在绘制过程中每一小段的长度
                float itemHeight = Math.round(item.getLength() * rateDistanceToViewHeight);

                if (item.getStatus() >= 2 && mIsNetworkConnected) { //只计算拥堵气泡的显示位置
                    //当前item距离车的位置
                    int distanceFromCar = (int) (mCursorPos / rateDistanceToViewHeight - realDistanceSum);
                    if (pixelDistanceSum <= mCursorPos && distanceFromCar < DISTANCE_MAX) {
                        //1.距离自车位大于5公里小于50公里，且拥堵长度大于500米才显示。
                        if ((distanceFromCar > DISTANCE_MID) && (distanceFromCar < DISTANCE_MAX)) {
                            if (updateTmcTag(item.getStatus(), item.getLength(), pixelDistanceSum, itemHeight) && item.getLength() > 500) {
                                shouldPopItem = true;
                            }
                        } else if (distanceFromCar <= DISTANCE_MID) { //2.距离自车位小于5公里，不限制拥堵长度统一都显示
                            if (updateTmcTag(item.getStatus(), item.getLength(), pixelDistanceSum, itemHeight)) {
                                shouldPopItem = true;
                            }
                        }
                    }
                }
                pixelDistanceSum += itemHeight;

                //小于当前位置的，使用交通状况对应的颜色值进行绘制;
                if (pixelDistanceSum < mCursorPos) {
                    canvas.drawRect(0, pixelDistanceSum - itemHeight, width, pixelDistanceSum,
                            getPaintInColor(getColor(item.getStatus())));// 画矩形
                } else if ((pixelDistanceSum - itemHeight) < mCursorPos) {
                    canvas.drawRect(0, pixelDistanceSum - itemHeight, width, mCursorPos,
                            getPaintInColor(getColor(item.getStatus())));// 画矩形
                }
            }

            //走过的路使用灰色
            if (height > mCursorPos) {
                canvas.drawRect(0, mCursorPos, width, height, getPaintInColor(getColor(TrafficStatus.AUTO_UNKNOWN_ERROR)));// 画矩形
            }
            //通知监听需要Pop信息的item
            if (mTmcBarListener != null) {
                if (!shouldPopItem || !mIsNetworkConnected) {
                    mTmcBarListener.dismissBottomTag();
                } else {
                    mTmcBarListener.showBottomTag(mTagCache);
                    if (mTagCache.viewHeight < 1) { //如果要画的气泡所在的拥堵段小于1像素，则画出最低的一像素在光柱图上，以便于区分，否则线太细看不见
                        if (mTagCache.translationY > height) { //如果计算出的长度大于总长度，则在光柱图最末尾画出一像素以示意气泡位置
                            canvas.drawRect(0, height - 1, width, height,
                                    getPaintInColor(getColor(mTagCache.status)));// 画矩形
                        } else {
                            canvas.drawRect(0, mTagCache.translationY, width, mTagCache.translationY + 1,
                                    getPaintInColor(getColor(mTagCache.status)));// 画矩形
                        }

                    }
                }
            }
        }
        canvas.restore();
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
     * 获取指定颜色的画笔.
     *
     * @param color 画笔颜色
     * @return 设置成指定颜色的画笔
     */
    private Paint getPaintInColor(int color) {
        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.FILL);
        }
        mPaint.setColor(color);
        return mPaint;
    }

    /**
     * Pop信息View绘制需要的信息.
     *
     * @param totalChildLength 该段距离tmcBar顶部的位置
     * @return true有更新，false没有更新
     */
    private boolean updateTmcTag(int status, int length, int totalChildLength, float viewHeight) {
        switch (status) {
            case TrafficStatus.TrafficStatusSlow:
                mTagCache.status = status;
                mTagCache.roadLength = length;
//                if (mIsNightMode) {
//                    mTagCache.bgResId = R.drawable.global_image_station_slow_day;
//                    mTagCache.textColor = Color.parseColor("#202025");
//                } else {
                    mTagCache.bgResId = R.drawable.global_image_station_slow_day;
                    mTagCache.textColor = Color.parseColor("#202025");
//                }
                mTagCache.translationY = totalChildLength;
                mTagCache.viewHeight = viewHeight;
                return true;
            case TrafficStatus.TrafficStatusJam:
                mTagCache.status = status;
                mTagCache.roadLength = length;
//                if (mIsNightMode) {
//                    mTagCache.bgResId = R.drawable.index_chargeing_small_day;
//                    mTagCache.textColor = Color.parseColor("#ffffff");
//                } else {
                    mTagCache.bgResId = R.drawable.index_chargeing_small_day;
                    mTagCache.textColor = Color.parseColor("#ffffff");
//                }
                mTagCache.translationY = totalChildLength;
                mTagCache.viewHeight = viewHeight;
                return true;
            case TrafficStatus.TrafficStatusCongested:
                mTagCache.status = status;
                mTagCache.roadLength = length;
//                if (mIsNightMode) {
//                    mTagCache.bgResId = R.drawable.index_chargeing_big_day;
//                    mTagCache.textColor = Color.parseColor("#ffffff");
//                } else {
                    mTagCache.bgResId = R.drawable.index_chargeing_big_day;
                    mTagCache.textColor = Color.parseColor("#ffffff");
//                }
                mTagCache.translationY = totalChildLength;
                mTagCache.viewHeight = viewHeight;
                return true;
            default:
                return false;
        }
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

    public void setTacBarListener(TmcBarListener listener) {
        mTmcBarListener = listener;
    }

    public void setNightMode(boolean isNightMode) {
        mIsNightMode = isNightMode;
        refresh();
    }

    public void refresh() {
        invalidate();
    }

    public interface TmcBarListener {
        void showBottomTag(TmcTag tmcTag); //显示底部tag

        void dismissBottomTag();//隐藏底部tag
    }

    public interface TmcBarViewFirstDraw {
        void onDraw(int width, int height);
    }

    /**
     * 光柱图左侧标签显示内容封装类
     */
    public static class TmcTag {
        public int status;//状态
        public int translationY; //相对坐标
        public int roadLength; //道路长度
        public int bgResId; //textview控件背景资源
        public int textColor; //字体颜色
        public float viewHeight; //tag对应的拥堵段在光柱图上的高度
        public int index; //在数组中的序号
    }
}
