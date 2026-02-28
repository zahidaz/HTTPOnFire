package com.azzahid.hof.domain.model

import io.ktor.http.HttpStatusCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteTypeTest {

    @Test
    fun `ApiRoute default values`() {
        val apiRoute = RouteType.ApiRoute()
        assertEquals("", apiRoute.responseBody)
        assertEquals(200, apiRoute.statusCode)
        assertTrue(apiRoute.headers.isEmpty())
    }

    @Test
    fun `ApiRoute getKtorStatusCode returns correct status`() {
        assertEquals(HttpStatusCode.OK, RouteType.ApiRoute(statusCode = 200).getKtorStatusCode())
        assertEquals(HttpStatusCode.NotFound, RouteType.ApiRoute(statusCode = 404).getKtorStatusCode())
        assertEquals(HttpStatusCode.InternalServerError, RouteType.ApiRoute(statusCode = 500).getKtorStatusCode())
        assertEquals(HttpStatusCode.Created, RouteType.ApiRoute(statusCode = 201).getKtorStatusCode())
    }

    @Test
    fun `StaticFile stores file info`() {
        val staticFile = RouteType.StaticFile(fileUri = "content://file/test.txt", mimeType = "text/plain")
        assertEquals("content://file/test.txt", staticFile.fileUri)
        assertEquals("text/plain", staticFile.mimeType)
    }

    @Test
    fun `StaticFile default mimeType is null`() {
        val staticFile = RouteType.StaticFile(fileUri = "content://file/test.txt")
        assertEquals(null, staticFile.mimeType)
    }

    @Test
    fun `Directory default values`() {
        val directory = RouteType.Directory(directoryUri = "content://dir/test")
        assertTrue(directory.allowBrowsing)
        assertEquals("index.html", directory.indexFile)
    }

    @Test
    fun `RedirectRoute default statusCode is 302`() {
        val redirect = RouteType.RedirectRoute(targetUrl = "https://example.com")
        assertEquals(302, redirect.statusCode)
    }

    @Test
    fun `RedirectRoute isPermanentRedirect for 301`() {
        val permanent = RouteType.RedirectRoute(targetUrl = "https://example.com", statusCode = 301)
        assertTrue(permanent.isPermanentRedirect())
    }

    @Test
    fun `RedirectRoute isPermanentRedirect false for 302`() {
        val temporary = RouteType.RedirectRoute(targetUrl = "https://example.com", statusCode = 302)
        assertFalse(temporary.isPermanentRedirect())
    }

    @Test
    fun `BuiltInRoute subtypes are BuiltInRoute instances`() {
        assertTrue(RouteType.StatusRoute is RouteType.BuiltInRoute)
        assertTrue(RouteType.OpenApiRoute is RouteType.BuiltInRoute)
        assertTrue(RouteType.SwaggerRoute is RouteType.BuiltInRoute)
        assertTrue(RouteType.NotificationRoute is RouteType.BuiltInRoute)
        assertTrue(RouteType.ProxyRoute is RouteType.BuiltInRoute)
    }

    @Test
    fun `user route types are not BuiltInRoute`() {
        assertFalse(RouteType.ApiRoute() is RouteType.BuiltInRoute)
        assertFalse(RouteType.StaticFile(fileUri = "x") is RouteType.BuiltInRoute)
        assertFalse(RouteType.Directory(directoryUri = "x") is RouteType.BuiltInRoute)
        assertFalse(RouteType.RedirectRoute(targetUrl = "x") is RouteType.BuiltInRoute)
    }
}
