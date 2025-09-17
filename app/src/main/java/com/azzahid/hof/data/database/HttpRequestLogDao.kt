package com.azzahid.hof.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.azzahid.hof.domain.model.HttpRequestLog
import kotlinx.coroutines.flow.Flow

@Dao
interface HttpRequestLogDao {

    @Query("SELECT * FROM http_request_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<HttpRequestLog>>

    @Query("SELECT * FROM http_request_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int): Flow<List<HttpRequestLog>>

    @Query("SELECT * FROM http_request_logs WHERE method = :method ORDER BY timestamp DESC")
    fun getLogsByMethod(method: String): Flow<List<HttpRequestLog>>

    @Query("SELECT * FROM http_request_logs WHERE path LIKE '%' || :pathPattern || '%' ORDER BY timestamp DESC")
    fun getLogsByPath(pathPattern: String): Flow<List<HttpRequestLog>>

    @Query("SELECT * FROM http_request_logs WHERE clientIp = :clientIp ORDER BY timestamp DESC")
    fun getLogsByClientIp(clientIp: String): Flow<List<HttpRequestLog>>

    @Query("SELECT * FROM http_request_logs WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getLogsSince(startTime: Long): Flow<List<HttpRequestLog>>

    @Query("SELECT COUNT(*) FROM http_request_logs")
    suspend fun getLogCount(): Int

    @Query("SELECT COUNT(*) FROM http_request_logs WHERE timestamp >= :startTime")
    suspend fun getLogCountSince(startTime: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HttpRequestLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<HttpRequestLog>)

    @Query("DELETE FROM http_request_logs")
    suspend fun clearAllLogs()

    @Query("DELETE FROM http_request_logs WHERE timestamp < :olderThan")
    suspend fun deleteLogsOlderThan(olderThan: Long)

    @Query("DELETE FROM http_request_logs WHERE id IN (SELECT id FROM http_request_logs ORDER BY timestamp ASC LIMIT :count)")
    suspend fun deleteOldestLogs(count: Int)

    @Query("SELECT * FROM http_request_logs WHERE id = :id")
    suspend fun getLogById(id: String): HttpRequestLog?
}