package com.azzahid.hof.domain.registry

import com.azzahid.hof.domain.model.RouteType
import io.ktor.http.HttpMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BuiltInRouteRegistryTest {

    @Test
    fun `registry contains 5 built-in routes`() {
        assertEquals(5, BuiltInRouteRegistry.routes.size)
    }

    @Test
    fun `all built-in routes have negative order`() {
        BuiltInRouteRegistry.routes.forEach { route ->
            assertTrue("Route ${route.id} should have negative order", route.order < 0)
        }
    }

    @Test
    fun `all built-in routes are enabled by default`() {
        BuiltInRouteRegistry.routes.forEach { route ->
            assertTrue("Route ${route.id} should be enabled", route.isEnabled)
        }
    }

    @Test
    fun `all built-in routes have BuiltInRoute type`() {
        BuiltInRouteRegistry.routes.forEach { route ->
            assertTrue("Route ${route.id} should be BuiltInRoute", route.type is RouteType.BuiltInRoute)
        }
    }

    @Test
    fun `status route exists with correct path`() {
        val statusRoute = BuiltInRouteRegistry.routes.find { it.id == "built-in-status" }
        assertNotNull(statusRoute)
        assertEquals("/api/status", statusRoute!!.path)
        assertEquals(HttpMethod.Get, statusRoute.method)
        assertEquals(RouteType.StatusRoute, statusRoute.type)
    }

    @Test
    fun `openapi route exists with correct path`() {
        val route = BuiltInRouteRegistry.routes.find { it.id == "built-in-openapi" }
        assertNotNull(route)
        assertEquals("/api/json", route!!.path)
        assertEquals(HttpMethod.Get, route.method)
    }

    @Test
    fun `swagger route exists with correct path`() {
        val route = BuiltInRouteRegistry.routes.find { it.id == "built-in-swagger" }
        assertNotNull(route)
        assertEquals("/api/swagger", route!!.path)
        assertEquals(HttpMethod.Get, route.method)
    }

    @Test
    fun `notification route uses POST method`() {
        val route = BuiltInRouteRegistry.routes.find { it.id == "built-in-notification" }
        assertNotNull(route)
        assertEquals("/api/notify", route!!.path)
        assertEquals(HttpMethod.Post, route.method)
    }

    @Test
    fun `proxy route exists with correct path`() {
        val route = BuiltInRouteRegistry.routes.find { it.id == "built-in-proxy" }
        assertNotNull(route)
        assertEquals("/api/proxy", route!!.path)
        assertEquals(HttpMethod.Get, route.method)
    }

    @Test
    fun `getPreferenceKey generates correct keys`() {
        val statusRoute = BuiltInRouteRegistry.routes.first { it.id == "built-in-status" }
        assertEquals("enable_status", BuiltInRouteRegistry.getPreferenceKey(statusRoute))

        val proxyRoute = BuiltInRouteRegistry.routes.first { it.id == "built-in-proxy" }
        assertEquals("enable_proxy", BuiltInRouteRegistry.getPreferenceKey(proxyRoute))
    }

    @Test
    fun `getAllPreferenceKeys returns all keys`() {
        val keys = BuiltInRouteRegistry.getAllPreferenceKeys()
        assertEquals(5, keys.size)
        assertTrue(keys.contains("enable_status"))
        assertTrue(keys.contains("enable_openapi"))
        assertTrue(keys.contains("enable_swagger"))
        assertTrue(keys.contains("enable_notification"))
        assertTrue(keys.contains("enable_proxy"))
    }

    @Test
    fun `findByRouteType returns correct route`() {
        val statusRoute = BuiltInRouteRegistry.findByRouteType(RouteType.StatusRoute)
        assertNotNull(statusRoute)
        assertEquals("built-in-status", statusRoute!!.id)
    }

    @Test
    fun `findByRouteType returns correct route for all types`() {
        assertNotNull(BuiltInRouteRegistry.findByRouteType(RouteType.OpenApiRoute))
        assertNotNull(BuiltInRouteRegistry.findByRouteType(RouteType.SwaggerRoute))
        assertNotNull(BuiltInRouteRegistry.findByRouteType(RouteType.NotificationRoute))
        assertNotNull(BuiltInRouteRegistry.findByRouteType(RouteType.ProxyRoute))
    }

    @Test
    fun `routes have unique ids`() {
        val ids = BuiltInRouteRegistry.routes.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `routes have unique paths`() {
        val paths = BuiltInRouteRegistry.routes.map { it.path }
        assertEquals(paths.size, paths.toSet().size)
    }

    @Test
    fun `routes have unique orders`() {
        val orders = BuiltInRouteRegistry.routes.map { it.order }
        assertEquals(orders.size, orders.toSet().size)
    }
}
