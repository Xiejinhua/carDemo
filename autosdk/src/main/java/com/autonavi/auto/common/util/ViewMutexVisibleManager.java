package com.autonavi.auto.common.util;

import android.text.TextUtils;
import android.view.View;


import java.util.ArrayList;
import java.util.List;

/**
 * @author AutoSdk
 */
public class ViewMutexVisibleManager {


    /**
     * 最后一次显示的容器类型
     */
    private String mLastType;

    /**
     * 基础的容器类型
     */
    private String mBaseType;

    public ViewMutexVisibleManager() {
    }

    private List<RelationView> mutexViews;


    /**
     * 设置互斥显示的容器面板
     * @param view 容器
     * @param type 类型
     * @return
     */
    public ViewMutexVisibleManager mutex(View view, String type) {
        if (mutexViews == null) {
            mutexViews = new ArrayList<>();
        }
        mutexViews.add(new RelationView(view, type));
        return this;
    }


    /**
     * 设置关联互斥的子view
     * @param type 类型
     * @param views 子view
     */
    public void concatMutex(String type, View... views) {
        RelationView relationView = getRelationView(type);
        if (relationView != null) {
            relationView.concatMutex(views);
        }
    }


    private RelationView getRelationView(String type) {
        for (RelationView mutexView : mutexViews) {
            if (mutexView.type.equals(type)) {
                return mutexView;
            }
        }
        return null;
    }


    /**
     * 设置基础显示的容器面板
     * 互斥容器隐藏，则显示基础容器
     * @return
     */
    public ViewMutexVisibleManager setBase() {
        if (mutexViews != null && mutexViews.size() > 0) {
            mutexViews.get(mutexViews.size() - 1).isBase = true;
            mBaseType = mutexViews.get(mutexViews.size() - 1).type;

        }
        return this;
    }


    /**
     * 隐藏，显示基础容器面板
     */
    public void hide() {
        for (RelationView relationView : mutexViews) {
            if (relationView.isBase) {
                mLastType = mBaseType;
                relationView.setVisibility(View.VISIBLE);
            } else {
                relationView.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * 显示指定类型的容器面板
     * @param type 类型
     */
    public void show(String type){
        mLastType = type;
        for (RelationView relationView : mutexViews) {
            if (relationView.type.equals(type)) {
                relationView.setVisibility(View.VISIBLE);
            } else {
                relationView.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * 保持上一次显示的容器面板
     * 场景：界面调整后再返回
     */
    public void keepViewVisibility(){
        String showType = TextUtils.isEmpty(mLastType) ? mBaseType : mLastType;
        if (!TextUtils.isEmpty(showType)){
            show(showType);
        }
    }

    public void setLastType(String lastType) {
        this.mLastType = lastType;
    }

    public View addAdditionalAction(AdditionalAction additionalAction, View view){
        view.setTag(additionalAction);
        return view;
    }

    public interface AdditionalAction{
        void action(int visibility);
    }

    public static class RelationView {
        View mView;
        String type;
        boolean isBase;
        List<RelationView> concatMutexViews;

        public RelationView(View mView, String type) {
            this.mView = mView;
            this.type = type;
        }

        public void concatMutex(View... views) {
            concatMutexViews = concatInner(null,views);
        }


        public void setVisibility(int visibility) {
            mView.setVisibility(visibility);
            mutexVisibility(visibility);
        }

        private void mutexVisibility(int visibility) {
            if (concatMutexViews != null) {
                for (RelationView concatMutexView : concatMutexViews) {
                    concatMutexView.mView.setVisibility(visibility == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
                    Object tag = concatMutexView.mView.getTag();
                    if (tag instanceof AdditionalAction) {
                        ((AdditionalAction) tag).action(concatMutexView.mView.getVisibility());
                    }
                }
            }
        }

        private List<RelationView> concatInner(String childType,View... views) {
            ArrayList<RelationView> list = new ArrayList<>();
            if (views.length > 0) {
                for (View view : views) {
                    list.add(new RelationView(view, TextUtils.isEmpty(childType) ? type : childType));
                }
            }
            return list;
        }
    }

}
