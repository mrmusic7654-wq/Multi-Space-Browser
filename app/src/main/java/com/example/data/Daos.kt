package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SpaceDao {
    @Query("SELECT * FROM spaces")
    fun getAllSpaces(): Flow<List<Space>>

    @Insert
    suspend fun insertSpace(space: Space)
}

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads WHERE spaceId = :spaceId ORDER BY timestamp DESC")
    fun getDownloadsForSpace(spaceId: Int): Flow<List<Download>>
    
    @Query("SELECT * FROM downloads ORDER BY timestamp DESC")
    fun getAllDownloads(): Flow<List<Download>>

    @Insert
    suspend fun insertDownload(download: Download)
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history WHERE spaceId = :spaceId ORDER BY timestamp DESC")
    fun getHistoryForSpace(spaceId: Int): Flow<List<History>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: History)

    @Query("DELETE FROM history")
    suspend fun clearAllHistory()
    
    @Query("DELETE FROM history WHERE spaceId = :spaceId")
    suspend fun clearHistoryForSpace(spaceId: Int)
}
