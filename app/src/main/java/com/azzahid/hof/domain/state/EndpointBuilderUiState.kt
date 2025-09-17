package com.azzahid.hof.domain.state

import com.azzahid.hof.domain.model.HttpMethod

data class EndpointBuilderUiState(
    val path: String = "",
    val method: HttpMethod = HttpMethod.GET,
    val description: String = "",
    val responseBody: String = "",
    val statusCode: String = "200",
    val headers: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isValid: Boolean = false,
    val editingEndpointId: String? = null
)

data class HeaderEntry(
    val key: String = "",
    val value: String = ""
)