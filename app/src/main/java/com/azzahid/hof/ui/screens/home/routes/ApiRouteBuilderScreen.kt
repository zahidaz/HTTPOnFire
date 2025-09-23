package com.azzahid.hof.ui.screens.home.routes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azzahid.hof.R
import com.azzahid.hof.domain.state.ApiRouteUiState
import com.azzahid.hof.domain.state.HeaderEntry
import com.azzahid.hof.ui.components.appbars.RouteBuilderAppBar
import com.azzahid.hof.ui.providers.LocalViewModelFactory
import com.azzahid.hof.ui.viewmodel.route.ApiRouteBuilderViewModel
import io.ktor.http.HttpMethod
import kotlinx.coroutines.launch

@Composable
fun ApiRouteBuilderScreen(
    onNavigateBack: () -> Unit,
    onRouteCreated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModelFactory = LocalViewModelFactory.current
    val viewModel: ApiRouteBuilderViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize()) {
        RouteBuilderAppBar(
            isValid = uiState.isValid,
            onNavigateBack = onNavigateBack,
            onSave = {
                viewModel.saveRoute(
                    onSuccess = onRouteCreated,
                    onError = { error ->
                        scope.launch {
                            snackbarHostState.showSnackbar(error)
                        }
                    }
                )
            }
        )

        ApiRouteBuilderContent(
            modifier = Modifier.weight(1f),
            uiState = uiState,
            viewModel = viewModel
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun ApiRouteBuilderContent(
    modifier: Modifier = Modifier,
    uiState: ApiRouteUiState,
    viewModel: ApiRouteBuilderViewModel
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        RouteBasicInfoCard(
            path = uiState.path,
            onPathChange = viewModel::updatePath,
            method = uiState.method,
            onMethodChange = viewModel::updateMethod,
            description = uiState.description,
            onDescriptionChange = viewModel::updateDescription
        )

        ResponseConfigCard(
            responseBody = uiState.responseBody,
            onResponseBodyChange = viewModel::updateResponseBody,
            statusCode = uiState.statusCode,
            onStatusCodeChange = viewModel::updateStatusCode
        )

        HeadersCard(
            headers = uiState.headers,
            onHeaderKeyChange = viewModel::updateHeaderKey,
            onHeaderValueChange = viewModel::updateHeaderValue,
            onAddHeader = viewModel::addHeader,
            onRemoveHeader = viewModel::removeHeader
        )
    }
}

@Composable
private fun RouteBasicInfoCard(
    path: String,
    onPathChange: (String) -> Unit,
    method: HttpMethod,
    onMethodChange: (HttpMethod) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.api_basic_info),
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = path,
                onValueChange = onPathChange,
                label = { Text(stringResource(R.string.api_field_path)) },
                placeholder = { Text(stringResource(R.string.api_field_path_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    Text(stringResource(R.string.api_field_path_help))
                },
                isError = path.isNotEmpty() && !path.startsWith("/")
            )

            HttpMethodDropdown(
                selectedMethod = method,
                onMethodSelected = onMethodChange
            )

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text(stringResource(R.string.api_field_description)) },
                placeholder = { Text(stringResource(R.string.api_field_description_placeholder)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HttpMethodDropdown(
    selectedMethod: HttpMethod,
    onMethodSelected: (HttpMethod) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedMethod.value,
            onValueChange = { },
            readOnly = true,
            label = { Text(stringResource(R.string.api_http_method)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listOf(
                HttpMethod.Get,
                HttpMethod.Post,
                HttpMethod.Put,
                HttpMethod.Delete,
                HttpMethod.Patch
            ).forEach { method ->
                DropdownMenuItem(
                    text = { Text(method.value) },
                    onClick = {
                        onMethodSelected(method)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ResponseConfigCard(
    responseBody: String,
    onResponseBodyChange: (String) -> Unit,
    statusCode: String,
    onStatusCodeChange: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.api_response_config),
                style = MaterialTheme.typography.titleMedium
            )

            StatusCodeDropdown(
                selectedStatusCode = statusCode,
                onStatusCodeSelected = onStatusCodeChange
            )

            OutlinedTextField(
                value = responseBody,
                onValueChange = onResponseBodyChange,
                label = { Text(stringResource(R.string.api_response_body)) },
                placeholder = { Text(stringResource(R.string.api_response_body_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                supportingText = {
                    Text(stringResource(R.string.api_response_body_help))
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusCodeDropdown(
    selectedStatusCode: String,
    onStatusCodeSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val commonStatusCodes = listOf(
        "200" to stringResource(R.string.http_status_200),
        "201" to stringResource(R.string.http_status_201),
        "204" to stringResource(R.string.http_status_204),
        "400" to stringResource(R.string.http_status_400),
        "401" to stringResource(R.string.http_status_401),
        "403" to stringResource(R.string.http_status_403),
        "404" to stringResource(R.string.http_status_404),
        "500" to stringResource(R.string.http_status_500)
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedStatusCode,
            onValueChange = onStatusCodeSelected,
            label = { Text(stringResource(R.string.api_status_code)) },
            placeholder = { Text(stringResource(R.string.api_status_code_placeholder)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable),
            supportingText = {
                val description = commonStatusCodes.find { it.first == selectedStatusCode }?.second
                if (description != null) {
                    Text(description)
                }
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            commonStatusCodes.forEach { (code, description) ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(code, style = MaterialTheme.typography.bodyLarge)
                            Text(description, style = MaterialTheme.typography.bodySmall)
                        }
                    },
                    onClick = {
                        onStatusCodeSelected(code)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun HeadersCard(
    headers: List<HeaderEntry>,
    onHeaderKeyChange: (Int, String) -> Unit,
    onHeaderValueChange: (Int, String) -> Unit,
    onAddHeader: () -> Unit,
    onRemoveHeader: (Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.api_custom_headers),
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedButton(
                    onClick = onAddHeader
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.api_add_header))
                }
            }

            headers.forEachIndexed { index, header ->
                HeaderEntryRow(
                    header = header,
                    onKeyChange = { key -> onHeaderKeyChange(index, key) },
                    onValueChange = { value -> onHeaderValueChange(index, value) },
                    onRemove = if (headers.size > 1) {
                        { onRemoveHeader(index) }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun HeaderEntryRow(
    header: HeaderEntry,
    onKeyChange: (String) -> Unit,
    onValueChange: (String) -> Unit,
    onRemove: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = header.key,
            onValueChange = onKeyChange,
            label = { Text(stringResource(R.string.api_header_name)) },
            placeholder = { Text(stringResource(R.string.api_header_name_placeholder)) },
            modifier = Modifier.weight(1f)
        )

        OutlinedTextField(
            value = header.value,
            onValueChange = onValueChange,
            label = { Text(stringResource(R.string.api_header_value)) },
            placeholder = { Text(stringResource(R.string.api_header_value_placeholder)) },
            modifier = Modifier.weight(1f)
        )

        if (onRemove != null) {
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.cd_remove_header)
                )
            }
        } else {
            Spacer(modifier = Modifier.width(48.dp))
        }
    }
}