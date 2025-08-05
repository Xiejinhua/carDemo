package com.autonavi.auto.common.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.autosdk.R;


/**
 * Created by AutoSdk.
 */
public class AutoGuideLine extends BaseGuideline {
    static public final int UNSPECIFIED_VALUE = -1;

    static public class AutoGuideLineParams {
        public float guidePercent4Default;
        public float guidePercent4Landscape;
        public float guidePercent4LandscapeWide;
        public float guidePercent4Portrait;
        public float guidePercent4Square;

        AutoGuideLineParams(){
            guidePercent4Default = UNSPECIFIED_VALUE;
            guidePercent4Landscape = UNSPECIFIED_VALUE;
            guidePercent4LandscapeWide = UNSPECIFIED_VALUE;
            guidePercent4Portrait = UNSPECIFIED_VALUE;
            guidePercent4Square = UNSPECIFIED_VALUE;
        }

        AutoGuideLineParams(AutoGuideLineParams other){
            this.guidePercent4Default = other.guidePercent4Default;
            this.guidePercent4Portrait = other.guidePercent4Portrait;
            this.guidePercent4Landscape = other.guidePercent4Landscape;
            this.guidePercent4LandscapeWide = other.guidePercent4LandscapeWide;
            this.guidePercent4Square = other.guidePercent4Square;
        }
    }

    private AutoGuideLineParams mAutoGuideLineParams;
    private AutoGuideLineParams mAutoGuideLineRawParams;

    public AutoGuideLine(Context context) {
        super(context);
        init(null);
    }

    public AutoGuideLine(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public AutoGuideLine(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs){
        //自定义比例值
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.autoGuideLine);
        if (typedArray != null) {
            mAutoGuideLineParams = new AutoGuideLineParams();
            mAutoGuideLineParams.guidePercent4Landscape = typedArray.getFloat(
                    R.styleable.autoGuideLine_layout_constraintGuide_percent4Landscape, UNSPECIFIED_VALUE);
            mAutoGuideLineParams.guidePercent4LandscapeWide = typedArray.getFloat(
                    R.styleable.autoGuideLine_layout_constraintGuide_percent4LandscapeWide, UNSPECIFIED_VALUE);
            mAutoGuideLineParams.guidePercent4Portrait = typedArray.getFloat(
                    R.styleable.autoGuideLine_layout_constraintGuide_percent4Portrait,UNSPECIFIED_VALUE);
            mAutoGuideLineParams.guidePercent4Square = typedArray.getFloat(
                R.styleable.autoGuideLine_layout_constraintGuide_percent4Square,UNSPECIFIED_VALUE);
            typedArray.recycle();
        }

        //默认比例值
        typedArray = getContext().obtainStyledAttributes(attrs, androidx.constraintlayout.widget.R.styleable.ConstraintLayout_Layout);
        if (typedArray != null){
            if (mAutoGuideLineParams == null){
                mAutoGuideLineParams = new AutoGuideLineParams();
            }
            mAutoGuideLineParams.guidePercent4Default = typedArray.getFloat(
                    androidx.constraintlayout.widget.R.styleable.ConstraintLayout_Layout_layout_constraintGuide_percent, UNSPECIFIED_VALUE);
            typedArray.recycle();
        }

        /**
         * 备份原始数据
         */
        mAutoGuideLineRawParams = new AutoGuideLineParams(mAutoGuideLineParams);
    }

    public AutoGuideLineParams getAutoGuideLineParams(){
        return mAutoGuideLineParams;
    }

    /**
     * 获取布局中加载的原始数据
     */
    public AutoGuideLineParams getAutoGuideLineRawParams(){
        return new AutoGuideLineParams(mAutoGuideLineRawParams);
    }
}
