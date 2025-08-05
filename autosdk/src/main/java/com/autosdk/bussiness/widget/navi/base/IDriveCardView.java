package com.autosdk.bussiness.widget.navi.base;

/**
 * 引导卡片接口
 */
public interface IDriveCardView {

    void show();

    void onResume();

    void onPause();

    void dismiss(int type);

    void onNightModeChanged();

    Enum getType();

    void setType(Enum cardType);

    boolean isShowing();

    void refreshSingleButtonWidth();

    void refreshSingleButtonWidth(float ratio);

    void setInfos(Object... infos);

    Object[] getInfos();
}
