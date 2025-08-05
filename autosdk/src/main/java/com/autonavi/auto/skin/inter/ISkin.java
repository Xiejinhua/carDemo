package com.autonavi.auto.skin.inter;

import android.view.View;


/**
 * Created by AutoSdk.
 */
public interface ISkin {
    ISkinAdapter getAdpter();

    interface ISkinAdapter {

        /**
         * 更新皮肤
         */
        void initSkin(View view);

        /**
         * 让皮肤功能生效
         */
        void apply(boolean isNight);

        /**
         * 皮肤变更时的一些自己的操作
         */
        void setViewApplyImplListener(ViewApplyImplListener listener);
    }


    interface IViewSkin {
        void setBackground(int dayResId, int nightResId);
    }

    interface IImageViewSkin extends IViewSkin {
        void setImageResource(int dayResId, int nightResId);
    }

    interface ISVGSkin extends IViewSkin {
        void setSVGColor(int dayResId, int nightResId);
    }

    interface ITextViewSkin extends IViewSkin {
        void setTextColor(int dayColorResId, int nightColorResId);

        void setHintTextColor(int dayColorResId, int nightColorResId);
        //        void setDrawableLeft(int dayResId, int nightResId);
        //        void setDrawableRight(int dayResId, int nightResId);
        //        void setDrawableTop(int dayResId, int nightResId);
        //        void setDrawableBottom(int dayResId, int nightResId);
    }

    interface IProgressBarViewSkin {
        void setProgressDrawable(int dayResId, int nightResId);
    }

    interface ISeekBarViewSkin {
        void setThumb(int dayResId, int nightResId);
    }
}

