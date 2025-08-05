package com.desaysv.psmap.model.data.repository


import androidx.lifecycle.LiveData
import com.desaysv.psmap.model.data.db.TeamMessageDao
import com.desaysv.psmap.model.data.db.TeamMessage
import com.desaysv.psmap.model.database.TeamMessageDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeamMessageRepository @Inject constructor(
    appDatabase: TeamMessageDatabase
) {
    private val messageDao: TeamMessageDao = appDatabase.teamMessageDao()

    // 监听队伍消息
    fun observeTeamMessages(teamId: String): LiveData<List<TeamMessage>> {
        return messageDao.observeTeamMessages(teamId)
    }

    // 发送消息
    suspend fun sendMessage(message: TeamMessage) {
        messageDao.insertMessage(message)
    }

    // 标记消息为已读
    suspend fun markAsRead(message: TeamMessage) {
        if (message.isRead == 0) {
            messageDao.updateMessageReadStatus(message.copy(isRead = 1))
        }
    }

    // 获取未读消息数量
    suspend fun getUnreadCount(teamId: String): Int {
        return messageDao.getUnreadCount(teamId)
    }

    // 标记所有消息为已读
    suspend fun markAllAsRead(teamId: String) {
        messageDao.markAllAsRead(teamId)
    }

    suspend fun clearAllMessages(): Int {
        return messageDao.clearAllMessages()
    }

    suspend fun deleteMessage(message: TeamMessage){
        messageDao.deleteMessage(message)
    }
}