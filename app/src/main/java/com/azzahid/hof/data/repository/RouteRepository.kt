package com.azzahid.hof.data.repository

import com.azzahid.hof.data.dao.RouteDao
import com.azzahid.hof.data.entity.toDomain
import com.azzahid.hof.data.entity.toEntity
import com.azzahid.hof.domain.registry.BuiltInRouteRegistry
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.RouteType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class RouteRepository(
    private val routeDao: RouteDao,
    private val settingsRepository: SettingsRepository
) {


    suspend fun getRouteById(id: String): Route? {
        return routeDao.getRouteById(id)?.toDomain()
    }

    suspend fun insertRoute(route: Route) {
        routeDao.insertRoute(route.toEntity())
    }

    suspend fun insertRoutes(routes: List<Route>) {
        routeDao.insertRoutes(routes.map { it.toEntity() })
    }

    suspend fun updateRoute(route: Route) {
        routeDao.updateRoute(route.toEntity())
    }

    suspend fun deleteRoute(route: Route) {
        routeDao.deleteRoute(route.toEntity())
    }

    suspend fun deleteRouteById(id: String) {
        routeDao.deleteRouteById(id)
    }

    suspend fun deleteAllRoutes() {
        routeDao.deleteAllRoutes()
    }

    suspend fun getRouteCount(): Int {
        return routeDao.getRouteCount()
    }

    suspend fun updateRouteEnabled(id: String, isEnabled: Boolean) {
        routeDao.updateRouteEnabled(id, isEnabled)
    }

    suspend fun updateRouteOrder(id: String, order: Int) {
        routeDao.updateRouteOrder(id, order)
    }

    private fun getAllBuiltInRoutes(): Flow<List<Route>> {
        return settingsRepository.getAllBuiltInRoutesEnabled().map { enabledStates ->
            BuiltInRouteRegistry.routes.map { route ->
                val routeType = route.type as RouteType.BuiltInRoute
                route.copy(isEnabled = enabledStates[routeType] ?: true)
            }
        }
    }


    private fun getUserRoutes(): Flow<List<Route>> {
        return routeDao.getAllRoutes().map { entities ->
            entities.map { it.toDomain() }
                .filter { !isBuiltInRoute(it) }
        }
    }

    fun getAllRoutes(): Flow<List<Route>> {
        return combine(
            getUserRoutes(),
            getAllBuiltInRoutes()
        ) { userRoutes, builtInRoutes ->
            userRoutes + builtInRoutes
        }
    }

    suspend fun toggleRoute(route: Route) {
        if (route.type is RouteType.BuiltInRoute) {
            settingsRepository.toggleBuiltInRoute(route.type)
        } else {
            updateRouteEnabled(route.id, !route.isEnabled)
        }
    }


    private fun isBuiltInRoute(route: Route): Boolean {
        return route.type is RouteType.BuiltInRoute
    }
}