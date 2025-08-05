package com.autonavi.auto.common.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.core.widget.NestedScrollView;

import java.util.ArrayList;

/**
 * 与GeneralScrollBtnBar配套使用的ScrollView
 * <p/>
 * 由于ScrollView没有提供setOnScrollListener的方法，
 * 因此要判断是否滑动到顶部底部只能通过重写onScrollChanged()方法实现
 * <p/>
 * Created by AutoSdk.
 */
public class BaseScrollView extends NestedScrollView {
    private ArrayList<ScrollViewListener> scrollViewListenerLists = new ArrayList<>();
    private ScrollViewListener scrollViewListener = null;

    public BaseScrollView(Context context) {
        super(context);
    }

    public BaseScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setScrollViewListener(ScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }

    public void addScrollViewListener(ScrollViewListener scrollViewListener) {
        if (scrollViewListenerLists == null) {
            scrollViewListenerLists = new ArrayList<>();
        }
        scrollViewListenerLists.add(scrollViewListener);
    }

    public void removeScrollViewListener(ScrollViewListener scrollViewListener) {
        if (scrollViewListenerLists == null) {
            return;
        }
        scrollViewListenerLists.remove(scrollViewListener);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (scrollViewListener != null) {
            scrollViewListener.onScrollChanged(this, l, t, oldl, oldt);
        }
        for (ScrollViewListener listener : scrollViewListenerLists) {
            if (listener != null) {
                listener.onScrollChanged(this, l, t, oldl, oldt);
            }
        }
    }

    @Override
    protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
        return 0;
    }

    public interface ScrollViewListener {
        void onScrollChanged(BaseScrollView scrollView, int x, int y, int oldx, int oldy);
    }
}
