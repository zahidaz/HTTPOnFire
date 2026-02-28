package com.azzahid.hof.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationRequestTest {

    @Test
    fun `isValid returns true for valid request`() {
        val request = NotificationRequest(title = "Hello", body = "World")
        assertTrue(request.isValid())
    }

    @Test
    fun `isValid returns false for blank title`() {
        val request = NotificationRequest(title = "", body = "World")
        assertFalse(request.isValid())
    }

    @Test
    fun `isValid returns false for blank body`() {
        val request = NotificationRequest(title = "Hello", body = "")
        assertFalse(request.isValid())
    }

    @Test
    fun `isValid returns false for whitespace-only title`() {
        val request = NotificationRequest(title = "   ", body = "World")
        assertFalse(request.isValid())
    }

    @Test
    fun `isValid returns false for invalid priority`() {
        val request = NotificationRequest(title = "Hello", body = "World", priority = "INVALID")
        assertFalse(request.isValid())
    }

    @Test
    fun `isValid returns true for all valid priorities`() {
        NotificationRequest.validPriorities.forEach { priority ->
            val request = NotificationRequest(title = "Hello", body = "World", priority = priority)
            assertTrue("Priority $priority should be valid", request.isValid())
        }
    }

    @Test
    fun `getPriorityLevel returns correct values`() {
        assertEquals(-2, NotificationRequest(title = "t", body = "b", priority = "MIN").getPriorityLevel())
        assertEquals(-1, NotificationRequest(title = "t", body = "b", priority = "LOW").getPriorityLevel())
        assertEquals(0, NotificationRequest(title = "t", body = "b", priority = "DEFAULT").getPriorityLevel())
        assertEquals(1, NotificationRequest(title = "t", body = "b", priority = "HIGH").getPriorityLevel())
        assertEquals(2, NotificationRequest(title = "t", body = "b", priority = "MAX").getPriorityLevel())
    }

    @Test
    fun `getPriorityLevel is case insensitive`() {
        assertEquals(-2, NotificationRequest(title = "t", body = "b", priority = "min").getPriorityLevel())
        assertEquals(1, NotificationRequest(title = "t", body = "b", priority = "high").getPriorityLevel())
    }

    @Test
    fun `getPriorityLevel returns 0 for unknown priority`() {
        assertEquals(0, NotificationRequest(title = "t", body = "b", priority = "UNKNOWN").getPriorityLevel())
    }

    @Test
    fun `default values are correct`() {
        val request = NotificationRequest(title = "Hello", body = "World")
        assertEquals("DEFAULT", request.priority)
        assertTrue(request.autoCancel)
        assertFalse(request.ongoing)
    }
}
