package com.desaysv.psmap.ui.settings.help

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.desaysv.psmap.R
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.model.bean.HelpBean
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 帮助界面ViewModel
 */
@HiltViewModel
class HelpViewModel @Inject constructor(private val application: Application) : ViewModel() {
    private val popularQuestionsList = mutableListOf<HelpBean>().apply {
        add(
            HelpBean(
                application.getString(R.string.sv_agreement_help_popular_questions_text_title_1),
                application.getString(R.string.sv_agreement_help_popular_questions_text_content_1)
            )
        )
        add(
            HelpBean(
                application.getString(R.string.sv_agreement_help_popular_questions_text_title_2),
                application.getString(R.string.sv_agreement_help_popular_questions_text_content_2)
            )
        )
    }
    private val drawingDisplayList = mutableListOf<HelpBean>().apply {
        add(
            HelpBean(
                application.getString(R.string.sv_agreement_help_drawing_display_text_title_1),
                application.getString(R.string.sv_agreement_help_drawing_display_text_content_1)
            )
        )
        add(
            HelpBean(
                application.getString(R.string.sv_agreement_help_drawing_display_text_title_2),
                application.getString(R.string.sv_agreement_help_drawing_display_text_content_2)
            )
        )
        add(
            HelpBean(
                application.getString(R.string.sv_agreement_help_drawing_display_text_title_3),
                application.getString(R.string.sv_agreement_help_drawing_display_text_content_3)
            )
        )
    }
    private val routePlanningList = mutableListOf<HelpBean>().apply {
        add(
            HelpBean(
                application.getString(R.string.sv_agreement_help_route_planning_text_title_1),
                application.getString(R.string.sv_agreement_help_route_planning_text_content_1)
            )
        )
        add(
            HelpBean(
                application.getString(R.string.sv_agreement_help_route_planning_text_title_2),
                application.getString(R.string.sv_agreement_help_route_planning_text_content_2)
            )
        )
        add(
            HelpBean(
                application.getString(R.string.sv_agreement_help_route_planning_text_title_3),
                application.getString(R.string.sv_agreement_help_route_planning_text_content_3)
            )
        )
        add(
            HelpBean(
                application.getString(R.string.sv_agreement_help_route_planning_text_title_4),
                application.getString(R.string.sv_agreement_help_route_planning_text_content_4)
            )
        )
        add(
            HelpBean(
                application.getString(R.string.sv_agreement_help_route_planning_text_title_5),
                application.getString(R.string.sv_agreement_help_route_planning_text_content_5)
            )
        )
    }
    private val searchFunctionList = mutableListOf<HelpBean>().apply {
        add(
            HelpBean(
                application.getString(R.string.sv_agreement_help_search_function_text_title_1),
                application.getString(R.string.sv_agreement_help_search_function_text_content_1)
            )
        )
        add(
            HelpBean(
                application.getString(R.string.sv_agreement_help_search_function_text_title_2),
                application.getString(R.string.sv_agreement_help_search_function_text_content_2)
            )
        )
        add(
            HelpBean(
                application.getString(R.string.sv_agreement_help_search_function_text_title_3),
                application.getString(R.string.sv_agreement_help_search_function_text_content_3)
            )
        )
        add(
            HelpBean(
                application.getString(R.string.sv_agreement_help_search_function_text_title_4),
                application.getString(R.string.sv_agreement_help_search_function_text_content_4)
            )
        )
        add(
            HelpBean(
                application.getString(R.string.sv_agreement_help_search_function_text_title_5),
                application.getString(R.string.sv_agreement_help_search_function_text_content_5)
            )
        )
    }
    private val voiceBroadcastList = mutableListOf<HelpBean>().apply {
        add(
            HelpBean(
                application.getString(R.string.sv_agreement_help_voice_broadcast_text_title_1),
                application.getString(R.string.sv_agreement_help_voice_broadcast_text_content_1)
            )
        )
        add(
            HelpBean(
                application.getString(R.string.sv_agreement_help_voice_broadcast_text_title_2),
                application.getString(R.string.sv_agreement_help_voice_broadcast_text_content_2)
            )
        )
        add(
            HelpBean(
                application.getString(R.string.sv_agreement_help_voice_broadcast_text_title_3),
                application.getString(R.string.sv_agreement_help_voice_broadcast_text_content_3)
            )
        )
    }
    private val mapDataList = mutableListOf<HelpBean>().apply {
        add(
            HelpBean(
                application.getString(R.string.sv_agreement_help_map_data_text_title_1),
                application.getString(R.string.sv_agreement_help_map_data_text_content_1)
            )
        )
        add(
            HelpBean(
                application.getString(R.string.sv_agreement_help_map_data_text_title_2),
                application.getString(R.string.sv_agreement_help_map_data_text_content_2)
            )
        )
    }
    private var selectList = mutableListOf<HelpBean>()

    val selectTab = MutableLiveData(BaseConstant.TYPE_HELP_POPULAR_QUESTIONS) //tab选中标志

    val selectCheckedId = MutableLiveData(R.id.rb_popular_questions) //选择的tab id

    fun setSelectTab(select: Int) {
        selectTab.postValue(select)
    }

    fun setSelectCheckedId(checkedId: Int) {
        selectCheckedId.postValue(checkedId)
    }

    fun setSelectList(select: Int) {
        selectList = when (select) {
            BaseConstant.TYPE_HELP_POPULAR_QUESTIONS -> popularQuestionsList
            BaseConstant.TYPE_HELP_DRAWING_DISPLAY -> drawingDisplayList
            BaseConstant.TYPE_HELP_ROUTE_PLANNING -> routePlanningList
            BaseConstant.TYPE_HELP_SEARCH_FUNCTION -> searchFunctionList
            BaseConstant.TYPE_HELP_VOICE_BROADCAST -> voiceBroadcastList
            BaseConstant.TYPE_HELP_MAP_DATA -> mapDataList
            else -> popularQuestionsList
        }
    }

    fun getSelectList(): MutableList<HelpBean> {
        return selectList
    }
}