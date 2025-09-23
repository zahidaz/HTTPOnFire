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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppBar(
    serverStatus: ServerStatus,
    isServerRunning: Boolean,
    serverPort: Int,
    onToggleServer: () -> Unit,
    onShareClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
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
                                ServerStatus.RUNNING -> Color(0xFF4CAF50)
                                ServerStatus.ERROR -> Color.Red
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                )

                Column {
                    Text(
                        text = when (serverStatus) {
                            ServerStatus.RUNNING -> stringResource(R.string.server_status_running)
                            ServerStatus.ERROR -> stringResource(R.string.server_status_error)
                            else -> stringResource(R.string.server_status_stopped)
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (isServerRunning) {
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
            if (isServerRunning) {
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.cd_share_server_url)
                    )
                }
            }

            IconButton(onClick = onToggleServer) {
                Icon(
                    imageVector = if (isServerRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = stringResource(
                        R.string.cd_start_stop_server,
                        if (isServerRunning) stringResource(R.string.server_stop) else stringResource(
                            R.string.server_start
                        )
                    )
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