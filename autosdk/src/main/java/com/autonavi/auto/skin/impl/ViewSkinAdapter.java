package com.autonavi.auto.skin.impl;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.autonavi.auto.skin.NightModeGlobal;
import com.autonavi.auto.skin.ResBean;
import com.autonavi.auto.skin.SkinItems;
import com.autonavi.auto.skin.SkinManager;
import com.autonavi.auto.skin.SkinUtil;
import com.autonavi.auto.skin.inter.ISkin;
import com.autonavi.auto.skin.inter.ViewApplyImplListener;

/**
 * Created by AutoSdk.
 */
public class ViewSkinAdapter<T extends View> implements ISkin.ISkinAdapter {
    /**
     * 自定义皮肤属性名称
     */
    protected SkinItems mSkinProperter;
    protected Context mContext;
    protected T view;
    private SkinWrapper4Background mBackground;
    private boolean isNightMode = false;
    private String suffix = "";

    private ViewApplyImplListener viewApplyImplListener;

    protected ViewSkinAdapter( Context context, SkinItems skinProperter) {
        mContext = context;
        this.mSkinProperter = skinProperter;
    }

    public ViewSkinAdapter(View view, AttributeSet attrs) {
        mContext = view.getContext();
        mSkinProperter = SkinUtil.initSkinAttrs(mContext, attrs);

    }

    public void updateView(View view) {
        if (!mSkinProperter.isEmpty()) {
            SkinManager.getInstance().updateView(view, NightModeGlobal.isNightMode(), false);
        }
    }

    public static ISkin.ISkinAdapter build(Context context, SkinItems skinProperter) {
        ViewSkinAdapter wrapper = new ViewSkinAdapter(context, skinProperter);
        return wrapper;
    }

    private boolean isInit = false;

    /**
     * 资源是否有更新
     */
    private boolean isUpadteRes;

    /**
     * 子类初始化皮肤操作
     *
     * @param view
     */
    protected void initSkinImpl(View view) {

    }

    @Override
    final public void initSkin(View view) {
        //        if (isInit) {
        //            return;
        //        }
        isInit = true;
        //        Timber.d("tag_skin", "  initSkin  ", view);
        this.view = (T)view;
        if (mBackground == null) {
            mBackground = new SkinWrapper4Background();
        }
        mBackground.init(mContext, mSkinProperter);
        initSkinImpl(view);
    }

    /**
     * 通知资源有更新
     */
    final protected void onUpdateRes() {
        isUpadteRes = true;
    }

    protected void applyImpl(boolean isNight) {
        if (viewApplyImplListener != null) {
            viewApplyImplListener.onSkinApplyImpl(isNight);
        }
    }

    @Override
    public void setViewApplyImplListener(ViewApplyImplListener viewApplyImplListener) {
        this.viewApplyImplListener = viewApplyImplListener;
    }

    /**
     * 是否需要更新资源
     *
     * @param isNight
     * @return
     */
    final protected boolean isNeedChangeRes(boolean isNight) {
        if (isUpadteRes) {
            return true;
        }
        if (!TextUtils.equals(suffix, NightModeGlobal.getSuffix())) {
            return true;
        }
        if (isNightMode == isNight) {
            return false;
        }
        return true;
    }

    @Override
    final public void apply(boolean isNight) {
        //        Timber.d("tag_skin", "apply updateView = %s id=0x%s , isNight=%s isNightMode = %s", view,
        // Integer.toHexString(view.getId()), isNight, isNightMode);

        if (!isNeedChangeRes(isNight)) {
            return;
        }
        isNightMode = isNight;
        isUpadteRes = false;
        suffix = NightModeGlobal.getSuffix();
        if (mBackground != null) {
            mBackground.apply(view, isNight);
        }
        applyImpl(isNight);
    }

    /**
     * 设置背景资源
     */
    public void setBackground(int dayResId, int nightResId) {
        ResBean bean = new ResBean();
        bean.setDefaultResId(dayResId);
        bean.setNightResId(nightResId);
        mSkinProperter.setBackground(bean);
        if (mBackground == null) {
            mBackground = new SkinWrapper4Background();
        }
        mBackground.init(mContext, mSkinProperter);
        onUpdateRes();
    }
}
