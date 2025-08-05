package com.desaysv.psmap.ui.home.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.autonavi.auto.skin.view.SkinConstraintLayout
import com.desaysv.psmap.R
import com.desaysv.psmap.base.bean.CommutingScenariosData
import com.desaysv.psmap.databinding.ViewCommutingScenariosBinding
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import timber.log.Timber

/**
 * 通勤场景
 */
class CommutingScenariosView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SkinConstraintLayout(context, attrs, defStyleAttr) {
    private var binding: ViewCommutingScenariosBinding

    private var csData: CommutingScenariosData? = null

    init {
        binding = ViewCommutingScenariosBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun updateData(data: CommutingScenariosData) {
        Timber.i("updateData")
        val title0 = if (data.type == 0) this.context.getString(R.string.sv_common_text_home) + " " else this.context
            .getString(R.string.sv_common_text_company) + " "
        binding.ivAction.text = title0
        binding.tvTitle.text = title0 + data.title
        binding.tvContent.text = data.content
        binding.tmcBarHorizontalView.setData(data.lightBarItems, data.totalDistance)
        binding.tmcBarHorizontalView.setCursorPos(0f)
        this.csData = data
    }

    fun clickListener(closeClick: () -> Unit, actionClick: (data: CommutingScenariosData) -> Unit) {
        binding.ivClose.setDebouncedOnClickListener {
            Timber.i("ivClose")
            csData?.run { closeClick.invoke() }
        }

        binding.ivAction.setDebouncedOnClickListener {
            Timber.i("ivAction")
            csData?.run { actionClick.invoke(this) }
        }
    }

}