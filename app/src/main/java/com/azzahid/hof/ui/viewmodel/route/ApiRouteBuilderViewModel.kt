package com.azzahid.hof.ui.viewmodel.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azzahid.hof.data.repository.RouteRepository
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.RouteType
import com.azzahid.hof.domain.state.ApiRouteUiState
import com.azzahid.hof.domain.state.HeaderEntry
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ApiRouteBuilderViewModel(
    private val routeRepository: RouteRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ApiRouteUiState())
    val uiState: StateFlow<ApiRouteUiState> = _uiState.asStateFlow()

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
        val currentHeaders = _uiState.value.headers.toMutableList()
        if (index < currentHeaders.size) {
            currentHeaders[index] = currentHeaders[index].copy(key = key)
            _uiState.value = _uiState.value.copy(headers = currentHeaders)
            validateForm()
        }
    }

    fun updateHeaderValue(index: Int, value: String) {
        val currentHeaders = _uiState.value.headers.toMutableList()
        if (index < currentHeaders.size) {
            currentHeaders[index] = currentHeaders[index].copy(value = value)
            _uiState.value = _uiState.value.copy(headers = currentHeaders)
            validateForm()
        }
    }

    fun addHeader() {
        val currentHeaders = _uiState.value.headers.toMutableList()
        currentHeaders.add(HeaderEntry())
        _uiState.value = _uiState.value.copy(headers = currentHeaders)
    }

    fun removeHeader(index: Int) {
        val currentHeaders = _uiState.value.headers.toMutableList()
        if (currentHeaders.size > 1 && index < currentHeaders.size) {
            currentHeaders.removeAt(index)
            _uiState.value = _uiState.value.copy(headers = currentHeaders)
            validateForm()
        }
    }

    private fun validateForm() {
        val state = _uiState.value
        val statusCodeInt = state.statusCode.toIntOrNull()
        val isValidStatusCode = statusCodeInt != null &&
                statusCodeInt in 100..599 &&
                try {
                    HttpStatusCode.fromValue(statusCodeInt)
                    true
                } catch (_: Exception) {
                    false
                }
        val isValid = state.path.isNotBlank() && isValidStatusCode
        _uiState.value = state.copy(isValid = isValid)
    }

    fun saveRoute(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!_uiState.value.isValid) {
            onError("Please fill in all required fields correctly")
            return
        }

        val state = _uiState.value
        val route = Route(
            id = UUID.randomUUID().toString(),
            path = state.path,
            method = state.method,
            description = state.description,
            type = RouteType.ApiRoute(
                responseBody = state.responseBody,
                statusCode = state.statusCode.toIntOrNull() ?: 200,
                headers = state.headersMap
            )
        )

        viewModelScope.launch {
            try {
                routeRepository.insertRoute(route)
                clearForm()
                onSuccess()
            } catch (e: Exception) {
                onError("Failed to save route: ${e.message}")
            }
        }
    }

    fun clearForm() {
        _uiState.value = ApiRouteUiState()
    }
}