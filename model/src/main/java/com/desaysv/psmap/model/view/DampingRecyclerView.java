package com.desaysv.psmap.model.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.PathInterpolator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author uidq5012.
 * @ClassName: DampingRecyclerView.
 * @Description: 带阻尼的 RecyclerView.
 * @Date: 2025/4/30 12:09.
 */
public class DampingRecyclerView extends RecyclerView {

    private final String TAG = "DampingRecyclerView";

    private final float mDamping = 0.2f; // 阻尼系数（0-1，越小阻力越大）

    /**
     * 弹性最大便宜
     */
    private final int BOUNCE_OFFSET_MAX = 250;

    /**
     * 弹性动画时长...
     */
    private final int BOUNCE_ANIM_DURATION = 400;

    /**
     * 上一个位置
     */
    private float mLastPos = 0;

    /**
     * 滚动偏移
     */
    private float mOverScrollOffset = 0;

    /**
     * 弹性动效
     */
    private ValueAnimator mAnimator;

    /**
     * 是否水平方向
     */
    private boolean mHorizontal = false;

    /**
     * 是否拖动状态
     */
    private boolean mIsDrag = false;

    public DampingRecyclerView(Context context) {
        super(context);
        init();
    }

    public DampingRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        LayoutManager layoutManager = getLayoutManager();
        if (null != layoutManager) {
            mHorizontal = ((LinearLayoutManager) layoutManager).getOrientation() == HORIZONTAL;
        }
    }

    private void init() {
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (mIsDrag) {
            return super.onTouchEvent(e);
        }

        int action = e.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastPos = mHorizontal ? e.getX() : e.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaOffset = (mHorizontal ? e.getX() : e.getY()) - mLastPos;
                mLastPos = mHorizontal ? e.getX() : e.getY();
                if (deltaOffset > 50) {
                    deltaOffset = 50;
                } else if (deltaOffset < -50) {
                    deltaOffset = -50;
                }

                // 判断是否在左边缘或右边缘
                boolean startReached = canScrollStart(); // 无法向左滚动（已到左边缘）
                boolean endReached = canScrollEnd(); // 无法向右滚动（已到右边缘）
                if ((deltaOffset > 0 && startReached) || (deltaOffset < 0 && endReached)) {
                    // 应用阻尼
                    deltaOffset *= mDamping;
                    mOverScrollOffset += deltaOffset;
                    if (mOverScrollOffset > BOUNCE_OFFSET_MAX) {
                        mOverScrollOffset = BOUNCE_OFFSET_MAX;
                    } else if (mOverScrollOffset < -BOUNCE_OFFSET_MAX) {
                        mOverScrollOffset = -BOUNCE_OFFSET_MAX;
                    }
                    setTranslationOffset(mOverScrollOffset);
                    return true; // 消费事件
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mOverScrollOffset != 0) {
                    startReboundAnimation();
                }
                mLastPos = 0;
                mOverScrollOffset = 0;
                break;
        }
        return super.onTouchEvent(e);
    }

    /**
     * 能滚动顶部
     */
    private boolean canScrollStart() {
        if (mHorizontal) {
            return !canScrollHorizontally(-1);
        } else {
            return !canScrollVertically(-1);
        }
    }

    /**
     * 能滚动底部
     */
    private boolean canScrollEnd() {
        if (mHorizontal) {
            return !canScrollHorizontally(1);
        } else {
            return !canScrollVertically(1);
        }
    }

    /**
     * 设置偏移
     *
     * @param offset
     */
    private void setTranslationOffset(float offset) {
        if (mHorizontal) {
            setTranslationX(offset);
        } else {
            setTranslationY(offset);
        }
    }

    public void setDrag(boolean isDrag) {
        mIsDrag = isDrag;
    }

    /**
     * 启动弹性动效
     */
    private void startReboundAnimation() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
        mAnimator = ValueAnimator.ofFloat(-mOverScrollOffset, 0);
        mAnimator.setInterpolator(new PathInterpolator(0.42f, 0f, 0.58f, 1f));
        mAnimator.setDuration(BOUNCE_ANIM_DURATION);
        mAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            setTranslationOffset(value);
        });
        mAnimator.start();
    }
}