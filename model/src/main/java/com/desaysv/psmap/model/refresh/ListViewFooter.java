package com.desaysv.psmap.model.refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.autonavi.auto.skin.SkinManager;
import com.desaysv.psmap.model.R;

public class ListViewFooter extends LinearLayout {
    public final static int STATE_NORMAL = 0;
    public final static int STATE_READY = 1;
    public final static int STATE_LOADING = 2;

    private Context mContext;

    private View mContentView;
    private TextView mHintView;

    public ListViewFooter(Context context) {
        super(context);
        initView(context);
    }

    public ListViewFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }


    public void setState(int state) {
        mHintView.setVisibility(View.VISIBLE);
        if (color != 0) {
            mHintView.setTextColor(color);
        }
        if (state == STATE_READY) {
            mHintView.setVisibility(View.VISIBLE);
            mHintView.setText(readyTips);
        } else if (state == STATE_LOADING) {
        } else {
            mHintView.setVisibility(View.VISIBLE);
            mHintView.setText(tips);
        }
    }

    public void setBottomMargin(int height) {
        if (height < 0) {
            return;
        }
        LayoutParams lp = (LayoutParams) mContentView.getLayoutParams();
        lp.bottomMargin = height;
        mContentView.setLayoutParams(lp);
    }

    public int getBottomMargin() {
        LayoutParams lp = (LayoutParams) mContentView.getLayoutParams();
        return lp.bottomMargin;
    }


    /**
     * normal status
     */
    public void normal() {
        mHintView.setVisibility(View.VISIBLE);
    }


    /**
     * loading status
     */
    public void loading() {
        mHintView.setVisibility(View.GONE);
    }

    /**
     * hide footer when disable pull load more
     */
    public void hide() {
        LayoutParams lp = (LayoutParams) mContentView.getLayoutParams();
        lp.height = 0;
        mContentView.setLayoutParams(lp);
    }

    /**
     * show footer
     */
    public void show() {
        LayoutParams lp = (LayoutParams) mContentView.getLayoutParams();
        lp.height = LayoutParams.WRAP_CONTENT;
        mContentView.setLayoutParams(lp);
    }

    private void initView(Context context) {
        mContext = context;
        LinearLayout moreView = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.pull_to_refresh_footer, null);
        addView(moreView);
        moreView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        mContentView = moreView.findViewById(R.id.ll_content);
        mHintView = (TextView) moreView.findViewById(R.id.footer_tips);
        SkinManager.getInstance().updateView(this, true);
    }

    private String tips = "";
    private String readyTips = "松开加载下一页";

    public void setTips(String tips) {
        this.tips = tips;
    }

    public void setReadyTips(String tips) {
        this.readyTips = tips;
    }

    private int color = 0;

    public void setTextColor(int color) {
        this.color = color;
    }

    public void setTextColors(int colors) {
        this.mHintView.setTextColor(colors);
    }
}
