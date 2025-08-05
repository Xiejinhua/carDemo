package com.desaysv.psmap.model.banner.indicator;

import android.view.View;

import androidx.annotation.NonNull;

import com.desaysv.psmap.model.banner.config.IndicatorConfig;
import com.desaysv.psmap.model.banner.listener.OnPageChangeListener;

public interface Indicator extends OnPageChangeListener {
    @NonNull
    View getIndicatorView();

    IndicatorConfig getIndicatorConfig();

    void onPageChanged(int count, int currentPosition);

}
