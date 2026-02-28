package com.azzahid.hof.ui.viewmodel.route

import com.azzahid.hof.data.repository.RouteRepository
import io.ktor.http.HttpMethod
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ApiRouteBuilderViewModelTest {

    private lateinit var viewModel: ApiRouteBuilderViewModel
    private lateinit var routeRepository: RouteRepository

    @Before
    fun setup() {
        routeRepository = mockk(relaxed = true)
        viewModel = ApiRouteBuilderViewModel(routeRepository)
    }

    @Test
    fun `initial state is invalid`() {
        assertFalse(viewModel.uiState.value.isValid)
    }

    @Test
    fun `initial state has default values`() {
        val state = viewModel.uiState.value
        assertEquals("", state.path)
        assertEquals(HttpMethod.Get, state.method)
        assertEquals("", state.description)
        assertEquals("{}", state.responseBody)
        assertEquals("200", state.statusCode)
    }

    @Test
    fun `updatePath prepends slash when missing`() {
        viewModel.updatePath("api/test")
        assertEquals("/api/test", viewModel.uiState.value.path)
    }

    @Test
    fun `updatePath keeps existing leading slash`() {
        viewModel.updatePath("/api/test")
        assertEquals("/api/test", viewModel.uiState.value.path)
    }

    @Test
    fun `updateMethod changes method`() {
        viewModel.updateMethod(HttpMethod.Post)
        assertEquals(HttpMethod.Post, viewModel.uiState.value.method)
    }

    @Test
    fun `updateDescription changes description`() {
        viewModel.updateDescription("Test endpoint")
        assertEquals("Test endpoint", viewModel.uiState.value.description)
    }

    @Test
    fun `updateResponseBody changes response body`() {
        viewModel.updateResponseBody("""{"key": "value"}""")
        assertEquals("""{"key": "value"}""", viewModel.uiState.value.responseBody)
    }

    @Test
    fun `updateStatusCode changes status code`() {
        viewModel.updateStatusCode("404")
        assertEquals("404", viewModel.uiState.value.statusCode)
    }

    @Test
    fun `form is valid with path and valid status code`() {
        viewModel.updatePath("/test")
        viewModel.updateStatusCode("200")
        assertTrue(viewModel.uiState.value.isValid)
    }

    @Test
    fun `updatePath with empty string becomes slash`() {
        viewModel.updatePath("")
        assertEquals("/", viewModel.uiState.value.path)
    }

    @Test
    fun `form is invalid without setting path`() {
        assertFalse(viewModel.uiState.value.isValid)
    }

    @Test
    fun `form is invalid with non-numeric status code`() {
        viewModel.updatePath("/test")
        viewModel.updateStatusCode("abc")
        assertFalse(viewModel.uiState.value.isValid)
    }

    @Test
    fun `form is invalid with out of range status code`() {
        viewModel.updatePath("/test")
        viewModel.updateStatusCode("999")
        assertFalse(viewModel.uiState.value.isValid)
    }

    @Test
    fun `form is valid with status codes in 100 to 599 range`() {
        viewModel.updatePath("/test")

        listOf("100", "200", "201", "301", "400", "404", "500", "599").forEach { code ->
            viewModel.updateStatusCode(code)
            assertTrue("Status code $code should be valid", viewModel.uiState.value.isValid)
        }
    }

    @Test
    fun `addHeader adds new empty header entry`() {
        val initialCount = viewModel.uiState.value.headers.size
        viewModel.addHeader()
        assertEquals(initialCount + 1, viewModel.uiState.value.headers.size)
    }

    @Test
    fun `updateHeaderKey changes header key at index`() {
        viewModel.updateHeaderKey(0, "Content-Type")
        assertEquals("Content-Type", viewModel.uiState.value.headers[0].key)
    }

    @Test
    fun `updateHeaderValue changes header value at index`() {
        viewModel.updateHeaderValue(0, "application/json")
        assertEquals("application/json", viewModel.uiState.value.headers[0].value)
    }

    @Test
    fun `updateHeaderKey ignores out of bounds index`() {
        viewModel.updateHeaderKey(10, "X-Custom")
        assertEquals(1, viewModel.uiState.value.headers.size)
    }

    @Test
    fun `updateHeaderValue ignores out of bounds index`() {
        viewModel.updateHeaderValue(10, "value")
        assertEquals(1, viewModel.uiState.value.headers.size)
    }

    @Test
    fun `removeHeader removes header at index`() {
        viewModel.addHeader()
        viewModel.addHeader()
        assertEquals(3, viewModel.uiState.value.headers.size)

        viewModel.removeHeader(1)
        assertEquals(2, viewModel.uiState.value.headers.size)
    }

    @Test
    fun `removeHeader does not remove last header`() {
        assertEquals(1, viewModel.uiState.value.headers.size)
        viewModel.removeHeader(0)
        assertEquals(1, viewModel.uiState.value.headers.size)
    }

    @Test
    fun `removeHeader ignores out of bounds index`() {
        viewModel.addHeader()
        assertEquals(2, viewModel.uiState.value.headers.size)
        viewModel.removeHeader(10)
        assertEquals(2, viewModel.uiState.value.headers.size)
    }

    @Test
    fun `clearForm resets to default state`() {
        viewModel.updatePath("/test")
        viewModel.updateMethod(HttpMethod.Post)
        viewModel.updateDescription("desc")
        viewModel.updateResponseBody("body")
        viewModel.updateStatusCode("404")

        viewModel.clearForm()

        val state = viewModel.uiState.value
        assertEquals("", state.path)
        assertEquals(HttpMethod.Get, state.method)
        assertEquals("", state.description)
        assertEquals("{}", state.responseBody)
        assertEquals("200", state.statusCode)
        assertFalse(state.isValid)
    }

    @Test
    fun `saveRoute calls onError when form is invalid`() {
        var errorMessage: String? = null
        viewModel.saveRoute(
            onSuccess = {},
            onError = { errorMessage = it }
        )
        assertTrue(errorMessage != null)
    }
}
