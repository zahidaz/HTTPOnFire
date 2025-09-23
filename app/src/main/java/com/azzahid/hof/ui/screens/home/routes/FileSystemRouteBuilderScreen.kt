package com.azzahid.hof.ui.screens.home.routes

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azzahid.hof.R
import com.azzahid.hof.domain.state.FileSelection
import com.azzahid.hof.domain.state.FileSystemRouteUiState
import com.azzahid.hof.ui.components.appbars.FileSystemRouteAppBar
import com.azzahid.hof.ui.providers.LocalViewModelFactory
import com.azzahid.hof.ui.viewmodel.route.FileSystemRouteBuilderViewModel
import kotlinx.coroutines.launch

@Composable
fun FileSystemRouteBuilderScreen(
    onNavigateBack: () -> Unit,
    onRouteCreated: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LocalContext.current
    val viewModelFactory = LocalViewModelFactory.current
    val viewModel: FileSystemRouteBuilderViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize()) {
        FileSystemRouteAppBar(
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

        FileSystemRouteBuilderContent(
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
private fun FileSystemRouteBuilderContent(
    modifier: Modifier = Modifier,
    uiState: FileSystemRouteUiState,
    viewModel: FileSystemRouteBuilderViewModel
) {

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.addSelectedFile(it) }
    }

    val directoryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            viewModel.addSelectedDirectory(it)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (uiState.selectedFile == null) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.filesystem_select_what_to_share),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = stringResource(R.string.filesystem_instruction),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                filePicker.launch(arrayOf("*/*"))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                                contentDescription = null
                            )
                            Text(stringResource(R.string.filesystem_select_file))
                        }

                        Button(
                            onClick = {
                                directoryPicker.launch(null)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null
                            )
                            Text(stringResource(R.string.filesystem_select_folder))
                        }
                    }
                }
            }
        }

        uiState.selectedFile?.let { selectedFile ->
            SelectedItemCard(
                item = selectedFile,
                shareName = uiState.shareName,
                routePath = uiState.path,
                onShareNameChange = viewModel::updateShareName,
                onRoutePathChange = viewModel::updatePath,
                onRemoveItem = viewModel::removeSelectedFile,
                onChangeSelection = {
                    viewModel.removeSelectedFile()
                }
            )
        }

        if (uiState.selectedFile?.isDirectory == true) {
            IndexHtmlOptionCard(
                respectIndexHtml = uiState.respectIndexHtml,
                onToggle = { viewModel.updateRespectIndexHtml(!uiState.respectIndexHtml) }
            )
        }
    }
}

@Composable
private fun SelectedItemCard(
    item: FileSelection,
    shareName: String,
    routePath: String,
    onShareNameChange: (String) -> Unit,
    onRoutePathChange: (String) -> Unit,
    onRemoveItem: () -> Unit,
    onChangeSelection: () -> Unit
) {
    var isEditingShareName by remember { mutableStateOf(false) }
    var isEditingRoutePath by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (item.isDirectory) Icons.Default.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )

                Text(
                    text = if (item.isDirectory) stringResource(R.string.filesystem_sharing_folder) else stringResource(
                        R.string.filesystem_sharing_file
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                TextButton(onClick = onChangeSelection) {
                    Text(stringResource(R.string.action_change))
                }
                TextButton(onClick = onRemoveItem) {
                    Text(stringResource(R.string.action_remove))
                }
            }

            Text(
                text = item.displayName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.filesystem_share_as),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (isEditingShareName) {
                    TextField(
                        value = shareName,
                        onValueChange = onShareNameChange,
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    TextButton(onClick = { isEditingShareName = false }) {
                        Text(stringResource(R.string.action_done))
                    }
                } else {
                    Text(
                        text = "\"$shareName\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isEditingShareName = true }
                    )
                    TextButton(onClick = { isEditingShareName = true }) {
                        Text(stringResource(R.string.action_edit))
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.filesystem_available_at),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (isEditingRoutePath) {
                    TextField(
                        value = routePath,
                        onValueChange = onRoutePathChange,
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    TextButton(onClick = { isEditingRoutePath = false }) {
                        Text(stringResource(R.string.action_done))
                    }
                } else {
                    Text(
                        text = routePath,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isEditingRoutePath = true }
                    )
                    TextButton(onClick = { isEditingRoutePath = true }) {
                        Text(stringResource(R.string.action_edit))
                    }
                }
            }
        }
    }
}

@Composable
private fun IndexHtmlOptionCard(
    respectIndexHtml: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.filesystem_directory_options),
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                androidx.compose.material3.Switch(
                    checked = respectIndexHtml,
                    onCheckedChange = { onToggle() }
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.filesystem_respect_index),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = stringResource(R.string.filesystem_respect_index_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

