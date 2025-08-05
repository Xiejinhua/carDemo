package com.desaysv.psmap.model.refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.autonavi.auto.skin.SkinManager;
import com.desaysv.psmap.model.R;

public class ListViewHeader extends LinearLayout {
    private LinearLayout mContainer;
    //	private ImageView mArrowImageView;
    private TextView mHintTextView;
    private int mState = STATE_NORMAL;

//	private Animation mRotateUpAnim;
//	private Animation mRotateDownAnim;

//	private final int ROTATE_ANIM_DURATION = 180;

    public final static int STATE_NORMAL = 0;
    public final static int STATE_READY = 1;
    public final static int STATE_REFRESHING = 2;

    public ListViewHeader(Context context) {
        super(context);
        initView(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public ListViewHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        // 初始情况，设置下拉刷新view高度为0
        LayoutParams lp = new LayoutParams(
                LayoutParams.MATCH_PARENT, 0);
        mContainer = (LinearLayout) LayoutInflater.from(context).inflate(
                R.layout.pull_to_refresh_header, null);
        addView(mContainer, lp);
        setGravity(Gravity.BOTTOM);

//		mArrowImageView = (ImageView)findViewById(R.id.head_arrowImageView);
        mHintTextView = (TextView) findViewById(R.id.head_tipsTextView);
        SkinManager.getInstance().updateView(this, true);
//		mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
//				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
//				0.5f);
//		mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
//		mRotateUpAnim.setFillAfter(true);
//		mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
//				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
//				0.5f);
//		mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
//		mRotateDownAnim.setFillAfter(true);
    }

    public void setState(int state) {
        if (state == mState) {
            return;
        }

        if (color != 0) {
            mHintTextView.setTextColor(color);
        }

        switch (state) {
            case STATE_NORMAL:
                if (mState == STATE_READY) {
//				mArrowImageView.startAnimation(mRotateDownAnim);
                }
                if (mState == STATE_REFRESHING) {
//				mArrowImageView.clearAnimation();
                }
                mHintTextView.setText(tips);
                break;
            case STATE_READY:
                if (mState != STATE_READY) {
//				mArrowImageView.clearAnimation();
//				mArrowImageView.startAnimation(mRotateUpAnim);
                    if ("正在同步数据".equals(tips)) {
                        mHintTextView.setText(tips);
                    } else {
                        mHintTextView.setText(readyTips);
                    }
                }
                break;
            case STATE_REFRESHING:
                if ("正在同步数据".equals(tips)) {
                    mHintTextView.setText(tips);
                } else {
                    mHintTextView.setText(refreshingTips);
                }
                break;
            default:
        }

        mState = state;
    }

    public void setVisiableHeight(int height) {
        if (height < 0) {
            height = 0;
        }

        LayoutParams lp = (LayoutParams) mContainer
                .getLayoutParams();
        lp.height = height;
        mContainer.setLayoutParams(lp);
    }

    public int getVisiableHeight() {
        return mContainer.getLayoutParams().height;
    }

    private String tips = "";

    public void setTips(String tips) {
        this.tips = tips;
    }

    private String readyTips = "松开加载上一页";

    public void setReadyTips(String tips) {
        this.readyTips = tips;
    }

    private String refreshingTips = "松开加载上一页";

    public void setRefreshingTips(String tips) {
        this.refreshingTips = tips;
    }

    private int color = 0;

    public void setTextColor(int color) {
        this.color = color;
    }

    public void setTextColors(int colors) {
        this.mHintTextView.setTextColor(colors);
    }

}
