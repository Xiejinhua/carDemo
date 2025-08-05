package com.autosdk.common.utils;

import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * 避免重复点击的实现自View.OnClickListener的抽象类
 * 默认避免快速点击的间隔是500ms；
 * 默认按钮本身避免重复点击；
 */
public abstract class BaseAvoidDoubleClickListener implements OnClickListener {
    private long mLastClickTime;
    @Override
    public void onClick(final View v) {
        if (!isFastDoubleClick()) {
            onViewClick(v);
        }
    }

    private boolean isFastDoubleClick(){
        if (isAvoidDoubleClickGlobal()){
            return ClickUtil.isFastDoubleClick(getRepeatClickInterval());
        }else{
            return isFastDoubleClick(getRepeatClickInterval());
        }
    }

    private boolean isFastDoubleClick(long repeatTime) {
        /**
         * 采用 开机时间，避免用户手动调整时间，导致无法点击出错
         */
        long time = SystemClock.elapsedRealtime();
        if (Math.abs(time - mLastClickTime) < repeatTime) {
            return true;
        }
        mLastClickTime = time;
        return false;
    }

    /**
     * 判断重复点击考虑全局按钮还是该按钮本身
     * */
    protected boolean isAvoidDoubleClickGlobal(){
        return false;
    }

    /**
     * 获取重复点击的时间间隔下限
     * */
    protected int getRepeatClickInterval(){
        return 500;
    }

    /**
     *
     * */
    public abstract void onViewClick(View v);
}
