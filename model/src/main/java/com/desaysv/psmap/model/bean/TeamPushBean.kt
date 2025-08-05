package com.desaysv.psmap.model.bean

import com.autonavi.gbl.user.group.model.GroupDestination
import com.autonavi.gbl.user.msgpush.model.TeamPushMsg

//组队推送--包括消息推送或者目的地变更通知
data class TeamPushBean(var type: Int, var teamPushMsg: TeamPushMsg, var destination: GroupDestination) //type: 0.消息推送 1.目的地变更
