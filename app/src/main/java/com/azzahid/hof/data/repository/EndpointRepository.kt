package com.azzahid.hof.data.repository

import com.azzahid.hof.data.dao.EndpointDao
import com.azzahid.hof.data.entity.toDomain
import com.azzahid.hof.data.entity.toEntity
import com.azzahid.hof.domain.model.Endpoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EndpointRepository(private val endpointDao: EndpointDao) {

    fun getAllEndpoints(): Flow<List<Endpoint>> {
        return endpointDao.getAllEndpoints().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getEndpointById(id: String): Endpoint? {
        return endpointDao.getEndpointById(id)?.toDomain()
    }

    fun getEnabledEndpoints(): Flow<List<Endpoint>> {
        return endpointDao.getEnabledEndpoints().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun insertEndpoint(endpoint: Endpoint) {
        endpointDao.insertEndpoint(endpoint.toEntity())
    }

    suspend fun insertEndpoints(endpoints: List<Endpoint>) {
        endpointDao.insertEndpoints(endpoints.map { it.toEntity() })
    }

    suspend fun updateEndpoint(endpoint: Endpoint) {
        endpointDao.updateEndpoint(endpoint.toEntity())
    }

    suspend fun deleteEndpoint(endpoint: Endpoint) {
        endpointDao.deleteEndpoint(endpoint.toEntity())
    }

    suspend fun deleteEndpointById(id: String) {
        endpointDao.deleteEndpointById(id)
    }

    suspend fun deleteAllEndpoints() {
        endpointDao.deleteAllEndpoints()
    }

    suspend fun getEndpointCount(): Int {
        return endpointDao.getEndpointCount()
    }

    suspend fun updateEndpointEnabled(id: String, isEnabled: Boolean) {
        endpointDao.updateEndpointEnabled(id, isEnabled)
    }

    suspend fun updateEndpointOrder(id: String, order: Int) {
        endpointDao.updateEndpointOrder(id, order)
    }
}