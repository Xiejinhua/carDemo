package com.desaysv.psmap.model.presentation

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.autonavi.auto.skin.SkinManager
import com.autosdk.view.SDKMapSurfaceView
import com.desaysv.psmap.model.R
import com.desaysv.psmap.model.databinding.PresentationLayoutBinding
import timber.log.Timber

class PresentationDisplay @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    private val mContext: Context = context
    private var binding: PresentationLayoutBinding? = null

    fun initView(surfaceView: SDKMapSurfaceView) {
        //加载副屏布局
        Timber.i("Presentation initView ")
        binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.presentation_layout, this, true)
        binding?.run {
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
            this.mapContainer.addView(surfaceView, lp)
            this.naviInit.visibility = View.GONE
            this.mapContainerBg.visibility = View.VISIBLE
        }
    }

    fun updateView(isNight: Boolean) {
        binding?.run {
            SkinManager.getInstance().updateView(this.root, isNight, true)
        }
    }

//    fun showView(show: Boolean) {
//        binding?.root?.visibility = if (show) View.VISIBLE else View.GONE
//    }
}
