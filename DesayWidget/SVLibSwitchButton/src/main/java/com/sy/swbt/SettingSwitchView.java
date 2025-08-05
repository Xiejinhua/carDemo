package com.sy.swbt;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.CompoundButton;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class SettingSwitchView extends SwitchButton implements CompoundButton.OnCheckedChangeListener {

    private Context context;
    private boolean IsForUser;
    private String TAG = getClass().getSimpleName();

    public boolean isChecked() {
        return super.isChecked();
    }

//    public void setChecked(boolean checked) {
//        if (checked == isChecked())
//            IsForUser = true;
//        else
//            super.setChecked(checked);
//    }

    public void setChecked(boolean checked, boolean isForUser) {
        IsForUser = isForUser;
        if (checked == isChecked())
            IsForUser = true;
        super.setChecked(checked);
        Log.d(TAG, "setChecked: " + isForUser);
    }

    private OnCheckedChangedListener valueChangedListener;

    public OnCheckedChangedListener getChangedListener() {
        return valueChangedListener;
    }

    public void setChangedListener(OnCheckedChangedListener valueChangedListener) {
        this.valueChangedListener = valueChangedListener;
    }

    public interface OnCheckedChangedListener {
        void onCheckedChanged(SettingSwitchView view, boolean isChecked);
    }

    public SettingSwitchView(@NonNull Context context) {
        super(context);
        super.setOnCheckedChangeListener(this);
        this.context = context;
    }

    public SettingSwitchView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        super.setOnCheckedChangeListener(this);
        this.context = context;

    }

    public SettingSwitchView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        super.setOnCheckedChangeListener(this);
        this.context = context;

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "onCheckedChanged: isForUser" + IsForUser);
        if (valueChangedListener != null && IsForUser) {    //添加是否按住的判断，只有手动才触发回调
            valueChangedListener.onCheckedChanged(this, this.isChecked());
        }
        IsForUser = true;
    }


}