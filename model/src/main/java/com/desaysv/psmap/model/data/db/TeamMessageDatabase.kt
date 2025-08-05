package com.desaysv.psmap.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.desaysv.psmap.model.data.db.TeamMessage
import com.desaysv.psmap.model.data.db.TeamMessageDao

@Database(
    entities = [TeamMessage::class],
    version = 1,
    exportSchema = false
)
abstract class TeamMessageDatabase : RoomDatabase() {
    abstract fun teamMessageDao(): TeamMessageDao
}
