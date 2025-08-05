package com.autonavi.auto.skin;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;

import com.autonavi.auto.skin.inter.ISkin;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by AutoSdk.
 */
public class SkinManager {

    private static SkinManager instance;
    private Handler mSkinHandler = null;
    private Handler mMainLooper1 = new Handler(Looper.getMainLooper());
    private onNightModeListener modeListener;

    private SkinManager() {
        HandlerThread thread = new HandlerThread("skinThread");
        thread.start();
        Looper looper = thread.getLooper();
        if(looper!=null){
            mSkinHandler = new Handler(looper);
        }
    }

    public static synchronized SkinManager getInstance() {
        if (instance == null) {
            instance = new SkinManager();
        }
        return instance;
    }


    public void registerNightModeListener(onNightModeListener listener){
        modeListener = listener;
    }

    public boolean isNightMode(){
        if (modeListener != null){
            return modeListener.isNightMode();
        }
        return false;
    }

    public static synchronized void destroy() {
        if (instance != null) {
            instance.mSkinHandler.getLooper().quit();
            instance.mSkinHandler = null;
        }
    }

    /**
     * 更新View中得皮肤
     *
     * @param view
     * @return
     */
    public SkinTask updateView(final View view) {
        return updateView(view, NightModeGlobal.isNightMode(), false);
    }

    /**
     * 更新View中得皮肤,昼夜由globa中获取
     *
     * @param view        需要转化的view本身
     * @param isRecursion 是否递归,包含子节点
     * @return
     */
    public SkinTask updateView(final View view, final boolean isRecursion) {
        return updateView(view, NightModeGlobal.isNightMode(), isRecursion);
    }

    /**
     * 更新View中得皮肤
     *
     * @param view        需要转化的view本身
     * @param isNight     白天或者是黑夜的皮肤
     * @param isRecursion 是否递归,包含子节点
     * @return
     */
    public SkinTask updateView(final View view, final boolean isNight, final boolean isRecursion) {
        if (view == null) {
            return null;
        }
        if (!NightModeGlobal.IS_SUPPORT_DAY_NIGHT) {
            return null;
        }

        final List<ISkin.ISkinAdapter> list = new LinkedList<ISkin.ISkinAdapter>();
        restore(view, list, isNight, isRecursion);
        updateView(list, isNight);
        SkinTask task = new SkinTask() {
            private boolean isCancel = false;

            @Override
            public void cancel() {
                isCancel = true;
            }

            @Override
            public void run() {
                if (isCancel) {
                    return;
                }
                mMainLooper1.post(new Runnable() {
                    @Override
                    public void run() {
                        updateView(list, isNight);
                    }
                });
            }
        };
        //        mSkinHandler.post(task);
        return task;
    }

    /**
     * 移除任务
     *
     * @param task
     */
    public void removeTask(SkinTask task) {
        task.cancel();
        if (mSkinHandler != null) {
            mSkinHandler.removeCallbacks(task);
        }
    }

    /**
     * 初始化layout中得布局,涉及到昼夜模式的信息
     *
     * @param view
     * @param list
     * @param isNight
     */
    private void restore(View view, List<ISkin.ISkinAdapter> list, boolean isNight, boolean isRecursion) {
        if (view instanceof ISkin) {
            ISkin skin = (ISkin)view;
            ISkin.ISkinAdapter wrapper = skin.getAdpter();
            if (wrapper != null) {
                wrapper.initSkin(view);
                list.add(wrapper);
            }
        }
        if (!isRecursion) {
            return;
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup)view;
            int count = viewGroup.getChildCount();
            for (int i = 0; i < count; i++) {
                restore(viewGroup.getChildAt(i), list, isNight, isRecursion);
            }
        }
    }

    private void updateView(List<ISkin.ISkinAdapter> list, boolean isNight) {
        for (ISkin.ISkinAdapter wrapper : list) {
            wrapper.apply(isNight);
        }
    }

    public interface SkinTask extends Runnable {
        void cancel();
    }

    public interface onNightModeListener {
        boolean isNightMode();
    }
}
