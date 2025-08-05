package com.desaysv.psmap.model.bean.iflytek

import com.desaysv.psmap.base.utils.BaseConstant

data class State(
    //表示业务（应用）的激活状态，有fg/bg/noExists三种取值，分别表示前景/背景/未启动
    var activeStatus: String = if (BaseConstant.MAP_APP_FOREGROUND) "fg" else "bg",
    //表示业务某个场景状态，有oneTarget/moreTarget/naviConfirm/noNavi/routing/navigation，分别代表单候选/多候选/导航确认/未导航/路线规划/导航中
    var sceneStatus: String = "",
    //讯飞说可以不传
    var selectStatus: String = "",
    var data: MapStateDataIFlyTek? = null,
)