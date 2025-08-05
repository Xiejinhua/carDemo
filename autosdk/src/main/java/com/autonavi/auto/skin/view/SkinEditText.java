package com.autonavi.auto.skin.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.autosdk.R;
import com.autonavi.auto.common.shadow.IShadowView;
import com.autonavi.auto.common.shadow.ShadowViewController;
import com.autonavi.auto.skin.impl.TextViewSkinAdapter;
import com.autonavi.auto.skin.inter.ISkin;

/**
 * Created by AutoSdk.
 */
public class SkinEditText extends EditText implements ISkin, ISkin.ITextViewSkin, IShadowView {
    /**
     * /*
     * 修改字体信息
     */
    private TextViewSkinAdapter mWrapper;
    private ShadowViewController mShadowController;

    public static String OswaldRegular = "Oswald-Regular";
    static Typeface oswldregular = null; //数字英文字体
    static Typeface normalType = null; //默认字体

    public SkinEditText(Context context) {
        super(context);
        init(null);
    }

    public SkinEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    @SuppressWarnings("deprecation")
    public SkinEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        // 该属性在某些昼夜的EditText中没有生效，这边强制设置，后续再跟进
        setIncludeFontPadding(false);
        mWrapper = new TextViewSkinAdapter(this, attrs);
        mWrapper.updateView(this);
        initShadowView(attrs);
//        initTypeface(attrs);
    }

    protected void initTypeface(AttributeSet attrs){
        boolean isNormalType = true;
        if (attrs != null) {
            TypedArray typedArray = this.getContext().obtainStyledAttributes(attrs, R.styleable.autoui);
            if(typedArray != null){
                String typeface = typedArray.getString(R.styleable.autoui_typeface);
                if(OswaldRegular.equals(typeface)){
                    if(null == oswldregular) {
                        oswldregular = Typeface.createFromAsset(getResources().getAssets(), "Oswald-Regular.ttf");
                    }
                    setTypeface(oswldregular);
                    isNormalType = false;
                }
                typedArray.recycle();
            }
        }
        if(isNormalType){
            if(null == normalType) {
                normalType = Typeface.createFromAsset(getResources().getAssets(), "font_cn.ttf");
            }
            setTypeface(normalType);

        }
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

    @Override
    public ISkinAdapter getAdpter() {
        return mWrapper;
    }

    @Override
    public void setBackgroundResource(int resid) {
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getPaddingRight();
        int bottom = getPaddingBottom();
        super.setBackgroundResource(resid);
        setPadding(left, top, right, bottom);
    }


    @Override
    public void setBackground(int dayResId, int nightResId) {
        mWrapper.setBackground(dayResId, nightResId);
    }

    @Override
    public void setTextColor(int dayColorResId, int nightColorResId) {
        mWrapper.setTextColor(dayColorResId, nightColorResId);
    }

    @Override
    public void setHintTextColor(int dayColorResId, int nightColorResId) {
        mWrapper.setHintTextColor(dayColorResId, nightColorResId);
    }

}
