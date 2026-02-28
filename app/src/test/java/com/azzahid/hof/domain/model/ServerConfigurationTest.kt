package com.azzahid.hof.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ServerConfigurationTest {

    @Test
    fun `CorsConfiguration default values`() {
        val cors = CorsConfiguration()
        assertFalse(cors.allowAnyHost)
        assertTrue(cors.allowedHosts.isEmpty())
        assertEquals(listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"), cors.allowedMethods)
        assertEquals(listOf("Content-Type", "Authorization"), cors.allowedHeaders)
        assertFalse(cors.allowCredentials)
    }

    @Test
    fun `CorsConfiguration custom values`() {
        val cors = CorsConfiguration(
            allowAnyHost = true,
            allowedHosts = listOf("example.com"),
            allowedMethods = listOf("GET"),
            allowedHeaders = listOf("X-Custom"),
            allowCredentials = true
        )
        assertTrue(cors.allowAnyHost)
        assertEquals(listOf("example.com"), cors.allowedHosts)
        assertEquals(listOf("GET"), cors.allowedMethods)
        assertEquals(listOf("X-Custom"), cors.allowedHeaders)
        assertTrue(cors.allowCredentials)
    }

    @Test
    fun `ServerConfiguration default values`() {
        val config = ServerConfiguration(
            port = 8080,
            enableLogs = true,
            autoStart = false,
            routes = emptyList()
        )
        assertEquals(8080, config.port)
        assertTrue(config.enableLogs)
        assertFalse(config.autoStart)
        assertTrue(config.routes.isEmpty())
        assertEquals("INFO", config.logLevel)
        assertFalse(config.corsConfiguration.allowAnyHost)
    }

    @Test
    fun `ServerConfiguration with custom cors`() {
        val cors = CorsConfiguration(allowAnyHost = true)
        val config = ServerConfiguration(
            port = 9090,
            enableLogs = false,
            autoStart = true,
            routes = emptyList(),
            corsConfiguration = cors,
            logLevel = "DEBUG"
        )
        assertEquals(9090, config.port)
        assertFalse(config.enableLogs)
        assertTrue(config.autoStart)
        assertEquals("DEBUG", config.logLevel)
        assertTrue(config.corsConfiguration.allowAnyHost)
    }
}
