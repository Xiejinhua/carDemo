package com.desaysv.psmap.model.business

import android.app.Application
import androidx.lifecycle.LiveData
import com.desaysv.psmap.model.data.db.TeamMessage
import com.desaysv.psmap.model.data.repository.TeamMessageRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


/**
 * 组队出行消息业务类
 */
@Singleton
class TeamMessageBusiness @Inject constructor(
    private val messageRepository: TeamMessageRepository,
    private val gson: Gson,
    private val application: Application
) {

    private val messageScope = CoroutineScope(Dispatchers.IO + Job())

    // 监听当前队伍消息
    fun observeTeamMessages(teamId: String): LiveData<List<TeamMessage>> {
        return messageRepository.observeTeamMessages(teamId)
    }

    // 发送文本消息
    fun sendTextMessage(teamId: String, senderId: String, senderName: String, content: String) {
        messageScope.launch {
            val message = TeamMessage(
                teamId = teamId,
                senderId = senderId,
                senderName = senderName,
                content = content,
                messageType = 1 // 文本消息
            )
            messageRepository.sendMessage(message)
        }
    }

    // 标记消息为已读
    fun markMessageAsRead(message: TeamMessage) {
        messageScope.launch {
            messageRepository.markAsRead(message)
        }
    }

    // 获取未读消息数量
    suspend fun getUnreadMessageCount(teamId: String): Int {
        return messageRepository.getUnreadCount(teamId)
    }

    // 标记所有消息为已读
    fun markAllAsRead(teamId: String) {
        messageScope.launch {
            messageRepository.markAllAsRead(teamId)
        }
    }

    // 清空所有消息
    fun clearAllMessages() {
        messageScope.launch {
            messageRepository.clearAllMessages()
        }
    }

    fun deleteMessage(message: TeamMessage) {
        messageScope.launch {
            messageRepository.deleteMessage(message)
        }
    }
}