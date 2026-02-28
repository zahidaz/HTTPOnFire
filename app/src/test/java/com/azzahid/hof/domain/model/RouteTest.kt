package com.azzahid.hof.domain.model

import io.ktor.http.HttpMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteTest {

    @Test
    fun `Route default values`() {
        val route = Route(
            id = "test",
            path = "/api/test",
            method = HttpMethod.Get,
            type = RouteType.ApiRoute()
        )
        assertEquals("", route.description)
        assertTrue(route.isEnabled)
        assertEquals(0, route.order)
    }

    @Test
    fun `Route with all fields`() {
        val route = Route(
            id = "route-1",
            path = "/api/users",
            method = HttpMethod.Post,
            description = "Create user",
            type = RouteType.ApiRoute(
                responseBody = """{"id": 1}""",
                statusCode = 201,
                headers = mapOf("X-Custom" to "value")
            ),
            isEnabled = false,
            order = 5
        )
        assertEquals("route-1", route.id)
        assertEquals("/api/users", route.path)
        assertEquals(HttpMethod.Post, route.method)
        assertEquals("Create user", route.description)
        assertFalse(route.isEnabled)
        assertEquals(5, route.order)
    }

    @Test
    fun `Route copy toggles enabled state`() {
        val route = Route(
            id = "test",
            path = "/test",
            method = HttpMethod.Get,
            type = RouteType.ApiRoute(),
            isEnabled = true
        )
        val toggled = route.copy(isEnabled = !route.isEnabled)
        assertFalse(toggled.isEnabled)
    }

    @Test
    fun `Route supports all HTTP methods`() {
        val methods = listOf(
            HttpMethod.Get, HttpMethod.Post, HttpMethod.Put,
            HttpMethod.Delete, HttpMethod.Patch, HttpMethod.Options,
            HttpMethod.Head
        )
        methods.forEach { method ->
            val route = Route(id = "test", path = "/test", method = method, type = RouteType.ApiRoute())
            assertEquals(method, route.method)
        }
    }

    @Test
    fun `Route equality based on all fields`() {
        val route1 = Route(id = "1", path = "/a", method = HttpMethod.Get, type = RouteType.ApiRoute())
        val route2 = Route(id = "1", path = "/a", method = HttpMethod.Get, type = RouteType.ApiRoute())
        assertEquals(route1, route2)
    }
}
