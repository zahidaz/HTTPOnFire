package com.azzahid.hof.ui.components.appbars

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.azzahid.hof.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsAppBar(
    totalEntries: Int,
    errorCount: Int,
    hasLogs: Boolean,
    onStatsClick: () -> Unit,
    onFilterClick: () -> Unit,
    onCopyAllClick: () -> Unit,
    onClearClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(stringResource(R.string.logs_title, totalEntries))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        actions = {
            BadgedBox(
                badge = {
                    if (errorCount > 0) {
                        Badge { Text(errorCount.toString()) }
                    }
                }
            ) {
                IconButton(onClick = onStatsClick) {
                    Icon(
                        imageVector = Icons.Outlined.BarChart,
                        contentDescription = stringResource(R.string.cd_statistics)
                    )
                }
            }

            IconButton(onClick = onFilterClick) {
                Icon(
                    imageVector = Icons.Outlined.FilterList,
                    contentDescription = stringResource(R.string.cd_filter)
                )
            }

            if (hasLogs) {
                IconButton(onClick = onCopyAllClick) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = stringResource(R.string.cd_copy_all_logs)
                    )
                }
            }

            if (hasLogs) {
                IconButton(onClick = onClearClick) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = stringResource(R.string.cd_clear_all_logs)
                    )
                }
            }
        }
    )
}
