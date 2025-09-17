package com.azzahid.hof.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azzahid.hof.domain.model.HttpMethod
import com.azzahid.hof.domain.state.EndpointBuilderUiState
import com.azzahid.hof.domain.state.HeaderEntry
import com.azzahid.hof.ui.viewmodel.EndpointBuilderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EndpointBuilderScreen(
    onNavigateBack: () -> Unit,
    onEndpointCreated: (com.azzahid.hof.domain.model.Endpoint) -> Unit,
    endpointId: String? = null,
    homeViewModel: com.azzahid.hof.ui.viewmodel.HomeViewModel? = null,
    endpointBuilderViewModel: EndpointBuilderViewModel = viewModel()
) {
    val uiState by endpointBuilderViewModel.uiState.collectAsStateWithLifecycle()
    val headers by endpointBuilderViewModel.headers.collectAsStateWithLifecycle()

    LaunchedEffect(endpointId) {
        if (endpointId != null && homeViewModel != null) {
            val homeUiState = homeViewModel.uiState.value
            val existingEndpoint = homeUiState.endpoints.find { it.id == endpointId }
            existingEndpoint?.let { endpoint ->
                endpointBuilderViewModel.loadEndpointForEditing(endpoint)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (endpointId != null) "Edit Endpoint" else "Create Endpoint") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            endpointBuilderViewModel.createEndpoint()?.let { endpoint ->
                                onEndpointCreated(endpoint)
                                onNavigateBack()
                            }
                        },
                        enabled = uiState.isValid
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        EndpointBuilderContent(
            modifier = Modifier.padding(paddingValues),
            uiState = uiState,
            headers = headers,
            onUpdatePath = endpointBuilderViewModel::updatePath,
            onUpdateMethod = endpointBuilderViewModel::updateMethod,
            onUpdateDescription = endpointBuilderViewModel::updateDescription,
            onUpdateResponseBody = endpointBuilderViewModel::updateResponseBody,
            onUpdateStatusCode = endpointBuilderViewModel::updateStatusCode,
            onUpdateHeaderKey = endpointBuilderViewModel::updateHeaderKey,
            onUpdateHeaderValue = endpointBuilderViewModel::updateHeaderValue,
            onAddHeader = endpointBuilderViewModel::addHeader,
            onRemoveHeader = endpointBuilderViewModel::removeHeader
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EndpointBuilderContent(
    modifier: Modifier = Modifier,
    uiState: EndpointBuilderUiState,
    headers: List<HeaderEntry>,
    onUpdatePath: (String) -> Unit,
    onUpdateMethod: (HttpMethod) -> Unit,
    onUpdateDescription: (String) -> Unit,
    onUpdateResponseBody: (String) -> Unit,
    onUpdateStatusCode: (String) -> Unit,
    onUpdateHeaderKey: (Int, String) -> Unit,
    onUpdateHeaderValue: (Int, String) -> Unit,
    onAddHeader: () -> Unit,
    onRemoveHeader: (Int) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Basic Information",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )


                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = onUpdateDescription,
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Optional description") }
                )
            }
        }

        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Request Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    var methodExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = methodExpanded,
                        onExpandedChange = { methodExpanded = !methodExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = uiState.method.name,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Method") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodExpanded) },
                            modifier = Modifier.menuAnchor(
                                MenuAnchorType.PrimaryNotEditable,
                                enabled = true
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = methodExpanded,
                            onDismissRequest = { methodExpanded = false }
                        ) {
                            HttpMethod.entries.forEach { method ->
                                DropdownMenuItem(
                                    text = { Text(method.name) },
                                    onClick = {
                                        onUpdateMethod(method)
                                        methodExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = uiState.statusCode,
                        onValueChange = onUpdateStatusCode,
                        label = { Text("Status Code") },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("200") }
                    )
                }

                OutlinedTextField(
                    value = uiState.path,
                    onValueChange = onUpdatePath,
                    label = { Text("Path") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("/api/example") },
                    prefix = { Text("/") }
                )
            }
        }

        Card {
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
                        text = "Headers",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedButton(onClick = onAddHeader) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Header")
                    }
                }

                headers.forEachIndexed { index, header ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = header.key,
                            onValueChange = { onUpdateHeaderKey(index, it) },
                            label = { Text("Key") },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Content-Type") }
                        )

                        OutlinedTextField(
                            value = header.value,
                            onValueChange = { onUpdateHeaderValue(index, it) },
                            label = { Text("Value") },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("application/json") }
                        )

                        if (headers.size > 1) {
                            IconButton(onClick = { onRemoveHeader(index) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove header"
                                )
                            }
                        }
                    }
                }
            }
        }

        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Response",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = uiState.responseBody,
                    onValueChange = onUpdateResponseBody,
                    label = { Text("Response Body") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    placeholder = { Text("{\n  \"message\": \"Hello World!\"\n}") }
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}