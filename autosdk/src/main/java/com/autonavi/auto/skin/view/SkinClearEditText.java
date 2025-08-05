package com.autonavi.auto.skin.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;

import androidx.annotation.DrawableRes;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import com.autonavi.auto.common.shadow.IShadowView;
import com.autonavi.auto.common.shadow.ShadowViewController;
import com.autonavi.auto.skin.impl.TextViewSkinAdapter;
import com.autonavi.auto.skin.inter.ISkin;
import com.autosdk.R;

import timber.log.Timber;

/**
 * 带有清除键的EditText
 */
public class SkinClearEditText extends AppCompatEditText implements View.OnFocusChangeListener, TextWatcher, ISkin, ISkin.ITextViewSkin, IShadowView {
    /**
     * /*
     * 修改字体信息
     */
    private TextViewSkinAdapter mWrapper;
    private ShadowViewController mShadowController;

    private Drawable mLeftDrawable;
    private boolean hasFocus;
    @DrawableRes
    private int mLeftDrawableResId;
    private boolean mClearEnable = false;
    @DrawableRes
    private int mClearDrawableResId = R.drawable.vector_delete_black;

    private float mClearDrawableSize = dp2px(20);
    private float mLeftDrawableSize = dp2px(20);

    public SkinClearEditText(Context context) {
        super(context);
        init(null);
    }

    public SkinClearEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    @SuppressWarnings("deprecation")
    public SkinClearEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        // 该属性在某些昼夜的EditText中没有生效，这边强制设置，后续再跟进
        setIncludeFontPadding(false);
        mWrapper = new TextViewSkinAdapter(this, attrs);
        mWrapper.updateView(this);
        initShadowView(attrs);

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ClearEditText);
            mClearDrawableResId = a.getResourceId(R.styleable.ClearEditText_clear_drawable, mClearDrawableResId);
            mClearDrawableSize = a.getDimension(R.styleable.ClearEditText_clear_size, dp2px(20));
            mLeftDrawableResId = a.getResourceId(R.styleable.ClearEditText_left_drawable, -1);
            mLeftDrawableSize = a.getDimension(R.styleable.ClearEditText_left_size, dp2px(20));
            a.recycle();
        }
        initClearIcon();
        initLeftIcon();
        setOnFocusChangeListener(this);
        addTextChangedListener(this);
        setCompoundDrawablePadding(8);
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

    private float dp2px(float dpValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, Resources.getSystem().getDisplayMetrics());
    }

    public void setClearDrawable(@DrawableRes int resId) {
        this.mClearDrawableResId = resId;
        initClearIcon(); // 刷新清除图标
    }

    private void initClearIcon() {
        Drawable clearDrawable = ContextCompat.getDrawable(getContext(), mClearDrawableResId);
        if (clearDrawable != null) {
            clearDrawable.setBounds(0, 0, (int) mClearDrawableSize, (int) mClearDrawableSize);
        }

        Drawable[] drawables = getCompoundDrawables();
        Drawable left = drawables[0];
        Drawable top = drawables[1];
        Drawable right = mClearEnable ? clearDrawable : null;
        Drawable bottom = drawables[3];

        setCompoundDrawables(left, top, right, bottom); // 更新右侧图标
    }

    private void initLeftIcon() {
        if (mLeftDrawableResId > 0) {
            mLeftDrawable = getCompoundDrawables()[0];
            if (mLeftDrawable == null) {
                mLeftDrawable = ContextCompat.getDrawable(getContext(), mLeftDrawableResId);
            }
            mLeftDrawable.setBounds(0, 0, (int) mLeftDrawableSize, (int) mLeftDrawableSize);
        }
        Drawable left = mLeftDrawable != null ? mLeftDrawable : null;
        setCompoundDrawables(left, getCompoundDrawables()[1], getCompoundDrawables()[2], getCompoundDrawables()[3]);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (getCompoundDrawables()[2] != null) {
                boolean touchable = event.getX() > (getWidth() - getTotalPaddingRight())
                        && (event.getX() < ((getWidth() - getPaddingRight())));
                if (touchable) {
                    this.setText("");
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        this.hasFocus = hasFocus;
        if (hasFocus) {
            setClearIconVisible(getText().length() > 0);
        } else {
            setClearIconVisible(false);
        }
    }

    public void setLeftIconResource(@DrawableRes int resId) {
        mLeftDrawableResId = resId;
        initLeftIcon();
    }

    public void setClearIconVisible(boolean visible) {
        this.mClearEnable = visible;
        initClearIcon();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int count, int after) {
        if (hasFocus) {
            setClearIconVisible(s.length() > 0);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        Timber.d("beforeTextChanged start:%s", start);
    }

    @Override
    public void afterTextChanged(Editable s) {
        Timber.d("afterTextChanged Editable:%s", s.toString());
    }

    /**
     * 设置震动效果
     */
    public void setShakeAnimation() {
        Animation animation = new TranslateAnimation(0, 10, 0, 0);
        animation.setInterpolator(new CycleInterpolator(5));
        animation.setDuration(1000);
        this.setAnimation(animation);
    }
}
