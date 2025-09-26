package com.azzahid.hof.ui.components.appbars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.azzahid.hof.R
import com.azzahid.hof.domain.state.ServerStatus

@Composable
fun HomeAppBar(
    serverStatus: ServerStatus,
    serverPort: Int,
    onToggleServer: () -> Unit,
    onShareClick: () -> Unit,
    onSettingsClick: () -> Unit
) {

    @OptIn(ExperimentalMaterial3Api::class)
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            when (serverStatus) {
                                ServerStatus.STARTED -> MaterialTheme.colorScheme.primary
                                ServerStatus.ERROR -> MaterialTheme.colorScheme.error
                                ServerStatus.STARTING, ServerStatus.STOPPING -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.outline
                            }
                        )
                )

                Column {
                    Text(
                        text = when (serverStatus) {
                            ServerStatus.STARTED -> stringResource(R.string.server_status_running)
                            ServerStatus.STARTING -> stringResource(R.string.server_status_starting)
                            ServerStatus.STOPPING -> stringResource(R.string.server_status_stopping)
                            ServerStatus.ERROR -> stringResource(R.string.server_status_error)
                            ServerStatus.STOPPED -> stringResource(R.string.server_status_stopped)
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (serverStatus == ServerStatus.STARTED) {
                        Text(
                            text = stringResource(R.string.server_url_localhost, serverPort),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        actions = {
            if (serverStatus == ServerStatus.STARTED) {
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.cd_share_server_url)
                    )
                }
            }

            IconButton(
                onClick = onToggleServer,
                enabled = serverStatus !in listOf(ServerStatus.STARTING, ServerStatus.STOPPING)
            ) {
                val icon = when (serverStatus) {
                    ServerStatus.STARTED -> Icons.Default.Stop
                    ServerStatus.STARTING, ServerStatus.STOPPING -> Icons.Default.Stop
                    else -> Icons.Default.PlayArrow
                }
                val actionText = if (serverStatus == ServerStatus.STARTED) {
                    stringResource(R.string.server_stop)
                } else {
                    stringResource(R.string.server_start)
                }

                Icon(
                    imageVector = icon,
                    contentDescription = stringResource(R.string.cd_start_stop_server, actionText)
                )
            }

            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.cd_settings)
                )
            }
        }
    )
}