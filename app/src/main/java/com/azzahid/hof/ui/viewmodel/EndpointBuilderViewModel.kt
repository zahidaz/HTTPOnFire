package com.azzahid.hof.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.azzahid.hof.domain.model.Endpoint
import com.azzahid.hof.domain.model.EndpointType
import com.azzahid.hof.domain.model.HttpMethod
import com.azzahid.hof.domain.state.EndpointBuilderUiState
import com.azzahid.hof.domain.state.HeaderEntry
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class EndpointBuilderViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EndpointBuilderUiState())
    val uiState: StateFlow<EndpointBuilderUiState> = _uiState.asStateFlow()

    private val _headers = MutableStateFlow(listOf(HeaderEntry()))
    val headers: StateFlow<List<HeaderEntry>> = _headers.asStateFlow()

    fun updatePath(path: String) {
        val cleanPath = if (path.startsWith("/")) path else "/$path"
        _uiState.value = _uiState.value.copy(path = cleanPath)
        validateForm()
    }

    fun updateMethod(method: HttpMethod) {
        _uiState.value = _uiState.value.copy(method = method)
        validateForm()
    }


    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
        validateForm()
    }

    fun updateResponseBody(responseBody: String) {
        _uiState.value = _uiState.value.copy(responseBody = responseBody)
        validateForm()
    }

    fun updateStatusCode(statusCode: String) {
        _uiState.value = _uiState.value.copy(statusCode = statusCode)
        validateForm()
    }

    fun updateHeaderKey(index: Int, key: String) {
        val currentHeaders = _headers.value.toMutableList()
        if (index < currentHeaders.size) {
            currentHeaders[index] = currentHeaders[index].copy(key = key)
            _headers.value = currentHeaders
            updateHeadersInState()
        }
    }

    fun updateHeaderValue(index: Int, value: String) {
        val currentHeaders = _headers.value.toMutableList()
        if (index < currentHeaders.size) {
            currentHeaders[index] = currentHeaders[index].copy(value = value)
            _headers.value = currentHeaders
            updateHeadersInState()
        }
    }

    fun addHeader() {
        val currentHeaders = _headers.value.toMutableList()
        currentHeaders.add(HeaderEntry())
        _headers.value = currentHeaders
    }

    fun removeHeader(index: Int) {
        val currentHeaders = _headers.value.toMutableList()
        if (currentHeaders.size > 1 && index < currentHeaders.size) {
            currentHeaders.removeAt(index)
            _headers.value = currentHeaders
            updateHeadersInState()
        }
    }

    private fun updateHeadersInState() {
        val headersMap = _headers.value
            .filter { it.key.isNotBlank() && it.value.isNotBlank() }
            .associate { it.key to it.value }
        _uiState.value = _uiState.value.copy(headers = headersMap)
        validateForm()
    }

    private fun validateForm() {
        val state = _uiState.value
        val statusCodeInt = state.statusCode.toIntOrNull()
        val isValidStatusCode = statusCodeInt != null &&
                statusCodeInt in 100..599 &&
                try {
                    HttpStatusCode.fromValue(statusCodeInt); true
                } catch (_: Exception) {
                    false
                }

        val isValid = state.path.isNotBlank() && isValidStatusCode
        _uiState.value = state.copy(isValid = isValid)
    }

    fun loadEndpointForEditing(endpoint: Endpoint) {
        when (val type = endpoint.type) {
            is EndpointType.ApiEndpoint -> {
                _uiState.value = _uiState.value.copy(
                    path = endpoint.path,
                    method = endpoint.method,
                    description = endpoint.description,
                    responseBody = type.responseBody,
                    statusCode = type.statusCode.toString(),
                    headers = type.headers,
                    editingEndpointId = endpoint.id
                )

                val headerEntries = if (type.headers.isEmpty()) {
                    listOf(HeaderEntry())
                } else {
                    type.headers.map { (key, value) -> HeaderEntry(key, value) } + HeaderEntry()
                }
                _headers.value = headerEntries
            }

            is EndpointType.StaticFile -> {
                // TODO: Handle StaticFile type when UI is implemented
            }

            is EndpointType.Directory -> {
                // TODO: Handle Directory type when UI is implemented
            }

            is EndpointType.RedirectEndpoint -> {
                // TODO: Handle RedirectEndpoint type when UI is implemented
            }

            is EndpointType.ProxyEndpoint -> {
                // TODO: Handle ProxyEndpoint type when UI is implemented
            }
        }
        validateForm()
    }

    fun createEndpoint(): Endpoint? {
        return if (_uiState.value.isValid) {
            val state = _uiState.value
            Endpoint(
                id = state.editingEndpointId ?: UUID.randomUUID().toString(),
                path = state.path,
                method = state.method,
                description = state.description,
                type = EndpointType.ApiEndpoint(
                    responseBody = state.responseBody,
                    statusCode = state.statusCode.toIntOrNull() ?: 200,
                    headers = state.headers
                )
            )
        } else null
    }

    fun clearForm() {
        _uiState.value = EndpointBuilderUiState()
        _headers.value = listOf(HeaderEntry())
    }
}