package com.autosdk.bussiness.widget.navi.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.autonavi.auto.skin.view.SkinLinearLayout;
import com.autosdk.bussiness.widget.navi.utils.NaviUiUtil;

/**
 * 卡片生命周期基类
 * @author AutoSDK
 */
public abstract class BaseDriveCardView extends SkinLinearLayout implements IDriveCardView {

    private boolean isShowing = false;
    protected Enum mCardType;
    protected View mParentView;
    protected View mSingleButton;
    private Object[] mInfos;

    public BaseDriveCardView(Context context,Enum cardId) {
        super(context);
        mCardType = cardId;
    }

    public BaseDriveCardView(Context context) {
        super(context);
    }

    /**
     * 显示卡片.
     * <p>
     * 如果正在显示那么不再重复显示
     * </p>
     */
    @Override
    public void show() {
        notifyShow();
        if (isShowing()) {
            return;
        }
        isShowing = true;
    }

    @Override
    public void onResume() {
        isShowing = true;
    }

    @Override
    public void onPause() {
        isShowing = false;
    }

    @Override
    public void dismiss(int type) {
        isShowing = false;
    }

    @Override
    public void onNightModeChanged() {
    }

    @Override
    public void refreshSingleButtonWidth() {
        refreshSingleButtonWidth(0.7f);
    }

    @Override
    public void refreshSingleButtonWidth(float ratio) {
        if (mParentView == null || mSingleButton == null) {
            return;
        }

        int panelWidth = 350;
        if (panelWidth > 0) {
            ViewGroup.MarginLayoutParams lp = NaviUiUtil.getViewLayoutParmas(mSingleButton);
            if (lp != null) {
                lp.width = (int) (panelWidth * ratio);
                mSingleButton.setLayoutParams(lp);
            }
        }
    }


    /**
     * 获取通用Layout Inflater.
     */
    protected LayoutInflater getInflater(View view) {
        return LayoutInflater.from(view.getContext());
    }

    @Override
    public Enum getType() {
        return mCardType;
    }

    @Override
    public void setType(Enum cardType) {
        mCardType=cardType;
    }

    /**
     * 是否正在显示.
     *
     * @return true显示；false不显示
     */
    @Override
    public boolean isShowing() {
        return isShowing;
    }

    @Override
    public void setInfos(Object... infos) {
        mInfos = infos;
    }

    @Override
    public Object[] getInfos() {
        return mInfos;
    }

    public interface OnShowListener {
        void onShow();
    }

    private OnShowListener mOnShowListener;

    public void setOnShowListener(OnShowListener onShowListener) {
        this.mOnShowListener = onShowListener;
    }

    public void notifyShow() {
        if (null == this.mOnShowListener) {
            return;
        }
        this.mOnShowListener.onShow();
    }
}
