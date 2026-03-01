package com.azzahid.hof.ui.screens.home.routes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azzahid.hof.R
import com.azzahid.hof.ui.providers.LocalViewModelFactory
import com.azzahid.hof.ui.viewmodel.route.ApiRouteBuilderViewModel
import com.azzahid.hof.ui.viewmodel.route.FileSystemRouteBuilderViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteBuilderScreen(
    onNavigateBack: () -> Unit,
    onRouteCreated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModelFactory = LocalViewModelFactory.current
    val fileSystemViewModel: FileSystemRouteBuilderViewModel = viewModel(factory = viewModelFactory)
    val apiViewModel: ApiRouteBuilderViewModel = viewModel(factory = viewModelFactory)
    val fileSystemUiState by fileSystemViewModel.uiState.collectAsState()
    val apiUiState by apiViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

    val isValid = when (selectedIndex) {
        0 -> fileSystemUiState.isValid
        1 -> apiUiState.isValid
        else -> false
    }

    val options = listOf(
        stringResource(R.string.route_type_file_folder),
        stringResource(R.string.route_type_custom_response)
    )

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.route_create_title)) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.action_back)
                    )
                }
            },
            actions = {
                TextButton(
                    onClick = {
                        val onError: (String) -> Unit = { error ->
                            scope.launch {
                                snackbarHostState.showSnackbar(error)
                            }
                        }
                        when (selectedIndex) {
                            0 -> fileSystemViewModel.saveRoute(onRouteCreated, onError)
                            1 -> apiViewModel.saveRoute(onRouteCreated, onError)
                        }
                    },
                    enabled = isValid
                ) {
                    Text(stringResource(R.string.action_save))
                }
            }
        )

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 8.dp)
        ) {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    onClick = { selectedIndex = index },
                    selected = selectedIndex == index
                ) {
                    Text(label)
                }
            }
        }

        when (selectedIndex) {
            0 -> FileSystemRouteBuilderContent(
                modifier = Modifier.weight(1f),
                uiState = fileSystemUiState,
                viewModel = fileSystemViewModel
            )
            1 -> ApiRouteBuilderContent(
                modifier = Modifier.weight(1f),
                uiState = apiUiState,
                viewModel = apiViewModel
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(16.dp)
        )
    }
}
