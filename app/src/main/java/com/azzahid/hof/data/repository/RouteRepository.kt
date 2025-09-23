package com.azzahid.hof.data.repository

import com.azzahid.hof.data.dao.RouteDao
import com.azzahid.hof.data.entity.toDomain
import com.azzahid.hof.data.entity.toEntity
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.RouteType
import com.azzahid.hof.domain.model.ServerConfiguration
import io.ktor.http.HttpMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RouteRepository(private val routeDao: RouteDao) {

    fun getAllRoutes(): Flow<List<Route>> {
        return routeDao.getAllRoutes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getRouteById(id: String): Route? {
        return routeDao.getRouteById(id)?.toDomain()
    }

    fun getEnabledRoutes(): Flow<List<Route>> {
        return routeDao.getEnabledRoutes().map { entities ->
            entities.map { it.toDomain() }
        }
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

    fun getUserRoutes(): Flow<List<Route>> {
        return routeDao.getAllRoutes().map { entities ->
            entities.map { it.toDomain() }
                .filter { !isBuiltInRoute(it) }
        }
    }

    fun getAllBuiltInRoutes(config: ServerConfiguration): List<Route> {
        val routes = mutableListOf<Route>()

        routes.add(
            Route(
                id = "built-in-status",
                path = "/api/status",
                method = HttpMethod.Get,
                description = "Server health and status check",
                type = RouteType.StatusRoute,
                isEnabled = config.enableStatus,
                order = -1000
            )
        )

        routes.add(
            Route(
                id = "built-in-openapi",
                path = "/api/json",
                method = HttpMethod.Get,
                description = "OpenAPI JSON specification",
                type = RouteType.OpenApiRoute,
                isEnabled = config.enableOpenApi,
                order = -999
            )
        )

        routes.add(
            Route(
                id = "built-in-swagger",
                path = "/api/swagger",
                method = HttpMethod.Get,
                description = "Swagger UI documentation",
                type = RouteType.SwaggerRoute,
                isEnabled = config.enableSwagger,
                order = -998
            )
        )

        return routes
    }


    private fun isBuiltInRoute(route: Route): Boolean {
        return route.type is RouteType.BuiltInRoute
    }
}