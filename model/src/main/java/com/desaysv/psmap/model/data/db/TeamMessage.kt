package com.desaysv.psmap.model.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "team_messages")
data class TeamMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teamId: String, // 队伍ID
    val senderId: String, // 发送者ID
    val senderName: String, // 发送者名称
    val content: String, // 消息内容
    val messageType: Int, // 消息类型：1-文本，2-位置，3-系统通知
    val isRead: Int = 0, // 是否已读 0-未读，1-已读
    val timestamp: Long = System.currentTimeMillis() // 发送时间戳
)
