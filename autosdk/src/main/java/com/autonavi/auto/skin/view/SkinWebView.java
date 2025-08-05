package com.autonavi.auto.skin.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;

import com.autonavi.auto.common.shadow.IShadowView;
import com.autonavi.auto.common.shadow.ShadowViewController;
import com.autonavi.auto.skin.impl.ViewSkinAdapter;
import com.autonavi.auto.skin.inter.ISkin;

/**
 * WebView 基类，扩展按钮翻页功能
 * Created by AutoSdk.
 */
public class SkinWebView extends WebView implements ISkin, IShadowView {
    private ViewSkinAdapter mWrapper;
    private ShadowViewController mShadowController;

    private BaseWebViewListener webViewListener = null;

    public SkinWebView(Context context) {
        super(context);
        init(null);
    }

    public SkinWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(attrs);
    }

    public SkinWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(attrs);
    }

    private void init(AttributeSet attrs) {
        mWrapper = new ViewSkinAdapter(this, attrs);
        mWrapper.updateView(this);
        initShadowView(attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        draw(canvas,this);
    }

    @Override
    public void initShadowView(AttributeSet attrs) {
        if (null == mShadowController) {
            mShadowController = new ShadowViewController(this, attrs);
        }
    }

    @Override
    public void draw(Canvas canvas, View view) {
        if (null != mShadowController) {
            mShadowController.draw(canvas,view);
        }
    }

    @Override
    public void setShadowVisibility(int visibility) {
        if (null != mShadowController) {
            mShadowController.setVisibility(visibility);
        }
    }

    public void setWebViewListener(BaseWebViewListener webViewListener) {
        this.webViewListener = webViewListener;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (webViewListener != null) {
            webViewListener.onScrollChanged(this, l, t, oldl, oldt);
        }
    }

    @Override
    public ISkinAdapter getAdpter() {
        return mWrapper;
    }

    public interface BaseWebViewListener {
        void onScrollChanged(SkinWebView webView, int x, int y, int oldx, int oldy);
    }

    @Override
    public void setBackgroundResource(int resid) {
        super.setBackgroundResource(resid);
        mWrapper.initSkin(this);
    }
}
