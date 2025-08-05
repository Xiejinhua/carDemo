package com.desaysv.psmap.model.data.db


import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TeamMessageDao {
    // 插入新消息
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: TeamMessage)

    // 更新消息已读状态
    @Update
    suspend fun updateMessageReadStatus(message: TeamMessage)

    // 监听指定队伍的消息变化
    @Query("SELECT * FROM team_messages WHERE teamId = :teamId ORDER BY timestamp DESC")
    fun observeTeamMessages(teamId: String): LiveData<List<TeamMessage>>

    // 获取未读消息数量
    @Query("SELECT COUNT(*) FROM team_messages WHERE teamId = :teamId AND isRead = 0")
    suspend fun getUnreadCount(teamId: String): Int

    // 标记所有消息为已读
    @Query("UPDATE team_messages SET isRead = 1 WHERE teamId = :teamId")
    suspend fun markAllAsRead(teamId: String): Int

    // 添加：清空所有消息（删除整个表数据）
    @Query("DELETE FROM team_messages")
    suspend fun clearAllMessages(): Int  // 返回删除的总行数

    // 添加：删除单条消息
    @Delete
    suspend fun deleteMessage(message: TeamMessage): Int  // 返回删除的行数
}