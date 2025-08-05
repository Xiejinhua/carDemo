package com.autonavi.auto.common.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.autosdk.R;

import java.util.Arrays;

public class GroupVirtual extends View {
    private static final String TAG = "[GroupVirtual]";
    protected int[] mIds = new int[32];
    protected int mCount = 0;
    protected Context myContext;

    public GroupVirtual(Context context){
        this(context, null);
    }

    public GroupVirtual(Context context, @Nullable AttributeSet attrs){
        this(context, attrs, -1);
    }

    public GroupVirtual(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        this.myContext = context;
        init(attrs);
    }

    public void init(AttributeSet attrs){
        if (attrs != null) {
            TypedArray typedArray = this.getContext().obtainStyledAttributes(attrs, R.styleable.autoui);
            if(typedArray != null){
                String referenceIds = typedArray.getString(R.styleable.autoui_constraint_referenced_ids);
                this.setIds(referenceIds);
                typedArray.recycle();
            }
        }
    }

    private void setIds(String idList) {
        if (idList != null) {
            int begin = 0;
            while(true) {
                int end = idList.indexOf(44, begin);
                if (end == -1) {
                    this.addId(idList.substring(begin));
                    return;
                }
                this.addId(idList.substring(begin, end));
                begin = end + 1;
            }
        }
    }

    private void addId(String idString) {
        if (idString != null) {
            if (this.myContext != null) {
                idString = idString.trim();
                int tag = this.myContext.getResources().getIdentifier(idString, "id", this.myContext.getPackageName());
                if (tag != 0) {
                    this.setTag(tag, (Object)null);
                } else {
                    Log.d(TAG, "Could not find id of \"" + idString +"\"");
                }
            }
        }
    }
    @Override
    public void setTag(int tag, Object value) {
        if (this.mCount + 1 > this.mIds.length) {
            this.mIds = Arrays.copyOf(this.mIds, this.mIds.length * 2);
        }
        this.mIds[this.mCount] = tag;
        ++this.mCount;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        for(int id : mIds) {
            View view = getViewById(id);
            if(view != null){
                view.setVisibility(visibility);
            }
        }
    }

    public View getViewById(int id){
        ConstraintLayout constraintLayout = (ConstraintLayout) getParent();
        if(constraintLayout == null){
            Log.d(TAG, "getViewById Could not find parent");
            return null;
        }
        return constraintLayout.getViewById(id);
    }

    @Override
    public void onDraw(Canvas canvas) {

    }

}
