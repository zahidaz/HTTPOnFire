package com.azzahid.hof.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.azzahid.hof.data.entity.EndpointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EndpointDao {

    @Query("SELECT * FROM endpoints ORDER BY `order` ASC")
    fun getAllEndpoints(): Flow<List<EndpointEntity>>

    @Query("SELECT * FROM endpoints WHERE id = :id")
    suspend fun getEndpointById(id: String): EndpointEntity?

    @Query("SELECT * FROM endpoints WHERE isEnabled = 1 ORDER BY `order` ASC")
    fun getEnabledEndpoints(): Flow<List<EndpointEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEndpoint(endpoint: EndpointEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEndpoints(endpoints: List<EndpointEntity>)

    @Update
    suspend fun updateEndpoint(endpoint: EndpointEntity)

    @Delete
    suspend fun deleteEndpoint(endpoint: EndpointEntity)

    @Query("DELETE FROM endpoints WHERE id = :id")
    suspend fun deleteEndpointById(id: String)

    @Query("DELETE FROM endpoints")
    suspend fun deleteAllEndpoints()

    @Query("SELECT COUNT(*) FROM endpoints")
    suspend fun getEndpointCount(): Int

    @Query("UPDATE endpoints SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun updateEndpointEnabled(id: String, isEnabled: Boolean)

    @Query("UPDATE endpoints SET `order` = :order WHERE id = :id")
    suspend fun updateEndpointOrder(id: String, order: Int)
}