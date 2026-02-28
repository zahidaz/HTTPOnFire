package com.azzahid.hof.data.entity

import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.RouteType
import io.ktor.http.HttpMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteEntityTest {

    @Test
    fun `toEntity converts ApiRoute correctly`() {
        val route = Route(
            id = "test-1",
            path = "/api/users",
            method = HttpMethod.Get,
            description = "Get users",
            type = RouteType.ApiRoute(
                responseBody = """{"users": []}""",
                statusCode = 200,
                headers = mapOf("X-Custom" to "value")
            ),
            isEnabled = true,
            order = 1
        )

        val entity = route.toEntity()

        assertEquals("test-1", entity.id)
        assertEquals("/api/users", entity.path)
        assertEquals("GET", entity.method)
        assertEquals("Get users", entity.description)
        assertTrue(entity.isEnabled)
        assertEquals(1, entity.order)
        assertTrue(entity.type.isNotEmpty())
    }

    @Test
    fun `toDomain converts ApiRoute entity back to Route`() {
        val route = Route(
            id = "test-1",
            path = "/api/users",
            method = HttpMethod.Post,
            description = "Create user",
            type = RouteType.ApiRoute(
                responseBody = """{"id": 1}""",
                statusCode = 201,
                headers = mapOf("Content-Type" to "application/json")
            ),
            isEnabled = false,
            order = 5
        )

        val entity = route.toEntity()
        val restored = entity.toDomain()

        assertEquals(route.id, restored.id)
        assertEquals(route.path, restored.path)
        assertEquals(route.method, restored.method)
        assertEquals(route.description, restored.description)
        assertEquals(route.isEnabled, restored.isEnabled)
        assertEquals(route.order, restored.order)

        val originalType = route.type as RouteType.ApiRoute
        val restoredType = restored.type as RouteType.ApiRoute
        assertEquals(originalType.responseBody, restoredType.responseBody)
        assertEquals(originalType.statusCode, restoredType.statusCode)
        assertEquals(originalType.headers, restoredType.headers)
    }

    @Test
    fun `round-trip for StaticFile route type`() {
        val route = Route(
            id = "file-1",
            path = "/files/doc",
            method = HttpMethod.Get,
            type = RouteType.StaticFile(
                fileUri = "content://media/doc.pdf",
                mimeType = "application/pdf"
            )
        )

        val restored = route.toEntity().toDomain()
        val restoredType = restored.type as RouteType.StaticFile

        assertEquals("content://media/doc.pdf", restoredType.fileUri)
        assertEquals("application/pdf", restoredType.mimeType)
    }

    @Test
    fun `round-trip for Directory route type`() {
        val route = Route(
            id = "dir-1",
            path = "/browse",
            method = HttpMethod.Get,
            type = RouteType.Directory(
                directoryUri = "content://media/folder",
                allowBrowsing = false,
                indexFile = "main.html"
            )
        )

        val restored = route.toEntity().toDomain()
        val restoredType = restored.type as RouteType.Directory

        assertEquals("content://media/folder", restoredType.directoryUri)
        assertEquals(false, restoredType.allowBrowsing)
        assertEquals("main.html", restoredType.indexFile)
    }

    @Test
    fun `round-trip for RedirectRoute type`() {
        val route = Route(
            id = "redirect-1",
            path = "/old",
            method = HttpMethod.Get,
            type = RouteType.RedirectRoute(
                targetUrl = "https://example.com/new",
                statusCode = 301
            )
        )

        val restored = route.toEntity().toDomain()
        val restoredType = restored.type as RouteType.RedirectRoute

        assertEquals("https://example.com/new", restoredType.targetUrl)
        assertEquals(301, restoredType.statusCode)
    }

    @Test
    fun `toEntity method value is uppercase`() {
        val route = Route(
            id = "test",
            path = "/test",
            method = HttpMethod.Delete,
            type = RouteType.ApiRoute()
        )
        assertEquals("DELETE", route.toEntity().method)
    }

    @Test
    fun `toDomain parses method correctly`() {
        val entity = RouteEntity(
            id = "test",
            path = "/test",
            method = "PATCH",
            description = "",
            type = """{"type":"com.azzahid.hof.domain.model.RouteType.ApiRoute","responseBody":"","statusCode":200,"headers":{}}""",
            isEnabled = true,
            order = 0
        )
        assertEquals(HttpMethod.Patch, entity.toDomain().method)
    }
}
