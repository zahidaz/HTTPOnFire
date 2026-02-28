package com.azzahid.hof.data.repository

import com.azzahid.hof.data.dao.RouteDao
import com.azzahid.hof.data.entity.RouteEntity
import com.azzahid.hof.data.entity.toEntity
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.RouteType
import io.ktor.http.HttpMethod
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RouteRepositoryTest {

    private lateinit var routeDao: RouteDao
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var repository: RouteRepository

    @Before
    fun setup() {
        routeDao = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        repository = RouteRepository(routeDao, settingsRepository)
    }

    @Test
    fun `insertRoute converts to entity and delegates to dao`() = runTest {
        val entitySlot = slot<RouteEntity>()
        coEvery { routeDao.insertRoute(capture(entitySlot)) } returns Unit

        val route = Route(
            id = "test-1",
            path = "/api/test",
            method = HttpMethod.Get,
            type = RouteType.ApiRoute(responseBody = "{}", statusCode = 200)
        )
        repository.insertRoute(route)

        assertEquals("test-1", entitySlot.captured.id)
        assertEquals("/api/test", entitySlot.captured.path)
        assertEquals("GET", entitySlot.captured.method)
    }

    @Test
    fun `deleteRouteById delegates to dao`() = runTest {
        repository.deleteRouteById("test-1")
        coVerify { routeDao.deleteRouteById("test-1") }
    }

    @Test
    fun `deleteAllRoutes delegates to dao`() = runTest {
        repository.deleteAllRoutes()
        coVerify { routeDao.deleteAllRoutes() }
    }

    @Test
    fun `getRouteCount delegates to dao`() = runTest {
        coEvery { routeDao.getRouteCount() } returns 5
        assertEquals(5, repository.getRouteCount())
    }

    @Test
    fun `updateRouteEnabled delegates to dao`() = runTest {
        repository.updateRouteEnabled("test-1", false)
        coVerify { routeDao.updateRouteEnabled("test-1", false) }
    }

    @Test
    fun `updateRouteOrder delegates to dao`() = runTest {
        repository.updateRouteOrder("test-1", 3)
        coVerify { routeDao.updateRouteOrder("test-1", 3) }
    }

    @Test
    fun `insertRoutes converts all routes to entities`() = runTest {
        val routes = listOf(
            Route(id = "1", path = "/a", method = HttpMethod.Get, type = RouteType.ApiRoute()),
            Route(id = "2", path = "/b", method = HttpMethod.Post, type = RouteType.ApiRoute())
        )
        repository.insertRoutes(routes)
        coVerify { routeDao.insertRoutes(match { it.size == 2 }) }
    }

    @Test
    fun `updateRoute converts to entity and delegates`() = runTest {
        val route = Route(
            id = "test-1",
            path = "/updated",
            method = HttpMethod.Put,
            type = RouteType.ApiRoute()
        )
        repository.updateRoute(route)
        coVerify { routeDao.updateRoute(route.toEntity()) }
    }

    @Test
    fun `toggleRoute for user route updates enabled state`() = runTest {
        val route = Route(
            id = "user-1",
            path = "/test",
            method = HttpMethod.Get,
            type = RouteType.ApiRoute(),
            isEnabled = true
        )
        repository.toggleRoute(route)
        coVerify { routeDao.updateRouteEnabled("user-1", false) }
    }

    @Test
    fun `toggleRoute for built-in route delegates to settings`() = runTest {
        val route = Route(
            id = "built-in-status",
            path = "/api/status",
            method = HttpMethod.Get,
            type = RouteType.StatusRoute,
            isEnabled = true
        )
        repository.toggleRoute(route)
        coVerify { settingsRepository.toggleBuiltInRoute(RouteType.StatusRoute) }
    }

    @Test
    fun `getRouteById returns null when not found`() = runTest {
        coEvery { routeDao.getRouteById("nonexistent") } returns null
        assertEquals(null, repository.getRouteById("nonexistent"))
    }
}
