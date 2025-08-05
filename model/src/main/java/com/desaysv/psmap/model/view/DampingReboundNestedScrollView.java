package com.desaysv.psmap.model.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.view.animation.TranslateAnimation;

import androidx.core.widget.NestedScrollView;

/**
 * ================================
 * Des: 阻尼回弹 DampingReboundNestedScrollView
 * Created by kele on 2021/2/22.
 * E-mail:984127585@qq.com
 * ================================
 */
public class DampingReboundNestedScrollView extends NestedScrollView {

    // y方向上当前触摸点的前一次记录位置
    private int previousY = 0;
    // y方向上的触摸点的起始记录位置
    private int startY = 0;
    // y方向上的触摸点当前记录位置
    private int currentY = 0;
    // y方向上两次移动间移动的相对距离
    private int deltaY = 0;

    private static final int BOUNCE_OFFSET_MAX = 250;
    private static final int BOUNCE_ANIM_TIME = 400;
    // 第一个子视图
    private View childView;

    // 用于记录childView的初始位置
    private Rect topRect = new Rect();

    //水平移动搞定距离
    private float moveHeight;

    public DampingReboundNestedScrollView(Context context) {
        this(context, null);
        // 不要原生的 顶部/底部 拖动的阴影效果
        this.setOverScrollMode(2);

    }

    public DampingReboundNestedScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.setOverScrollMode(2);
    }

    public DampingReboundNestedScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFillViewport(true);
        this.setOverScrollMode(2);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 0) {
            childView = getChildAt(0);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (null == childView) {
            return super.dispatchTouchEvent(event);
        }
        if (childView.getHeight() <= this.getHeight()) {
            // 子元素不超过整个view 的高度, 没有滑动效果
            return super.dispatchTouchEvent(event);

        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = (int) event.getY();
                previousY = startY;

                // 记录childView的初始位置
                topRect.set(childView.getLeft(), childView.getTop(),
                        childView.getRight(), childView.getBottom());
                moveHeight = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                currentY = (int) event.getY();
                deltaY = currentY - previousY;
                previousY = currentY;

                //判定是否在顶部或者滑到了底部
                if ((!childView.canScrollVertically(-1) && (currentY - startY) > 0) || (!childView.canScrollVertically(1) && (currentY - startY) < 0)) {
                    //计算阻尼
                    float distance = (float) currentY - startY;
                    if (distance < 0) {
                        distance *= -1;
                    }

                    float damping = 0.2f;//阻尼值
                    float height = getHeight();
                    if (height != 0) {
                        if (distance > height) {
                            damping = 0;
                        } else {
                            damping = (height - distance) / height;
                        }
                    }
                    if (currentY - startY < 0) {
                        damping = 1 - damping;
                    }

                    //阻力值限制再0.3-0.5之间，平滑过度
                    damping *= 0.25;
                    damping += 0.25;

                    moveHeight = moveHeight + (deltaY * damping);
                    if (moveHeight > BOUNCE_OFFSET_MAX) {
                        moveHeight = BOUNCE_OFFSET_MAX;
                    }
                    childView.layout(topRect.left, (int) (topRect.top + moveHeight), topRect.right,
                            (int) (topRect.bottom + moveHeight));
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!topRect.isEmpty()) {
                    //开始回移动画
                    upDownMoveAnimation();
                    // 子控件回到初始位置
                    childView.layout(topRect.left, topRect.top, topRect.right,
                            topRect.bottom);
                }
                //重置一些参数
                startY = 0;
                currentY = 0;
                topRect.setEmpty();
                break;
        }

        return super.dispatchTouchEvent(event);
    }

    // 初始化上下回弹的动画效果
    private void upDownMoveAnimation() {
        TranslateAnimation animation = new TranslateAnimation(0.0f, 0.0f,
                childView.getTop(), topRect.top);
        animation.setDuration(BOUNCE_ANIM_TIME);
        animation.setFillAfter(true);
        //设置阻尼动画效果
        animation.setInterpolator(new PathInterpolator(0.42f, 0f, 0.58f, 1f));
        childView.setAnimation(animation);
    }


}