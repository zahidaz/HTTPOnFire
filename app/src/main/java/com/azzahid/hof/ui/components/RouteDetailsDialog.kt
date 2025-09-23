package com.azzahid.hof.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.azzahid.hof.R
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.RouteType
import io.ktor.http.HttpStatusCode

@Composable
fun RouteDetailsDialog(
    route: Route,
    onDismiss: () -> Unit
) {

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.details_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.action_close)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                RouteInfoSection(route = route)

                Spacer(modifier = Modifier.height(16.dp))

                PathSection(route = route)


                Spacer(modifier = Modifier.height(16.dp))

                when (val type = route.type) {
                    is RouteType.ApiRoute -> {
                        ApiRouteConfigSection(type = type)
                    }

                    is RouteType.StaticFile -> {
                        StaticFileConfigSection(type = type)
                    }

                    is RouteType.Directory -> {
                        DirectoryConfigSection(type = type)
                    }

                    is RouteType.RedirectRoute -> {
                        RedirectConfigSection(type = type)
                    }

                    is RouteType.ProxyRoute -> {
                        ProxyConfigSection(type = type)
                    }

                    else -> {
                        BuiltInRouteConfigSection()
                    }
                }

            }
        }
    }
}

@Composable
private fun PathSection(route: Route) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.details_path),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = route.path,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun RouteInfoSection(route: Route) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MethodChip(method = route.method)

                Text(
                    text = route.path,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )
            }

            if (route.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = route.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (route.isEnabled) Icons.Default.PlayArrow else Icons.Default.Stop,
                    contentDescription = null,
                    tint = if (route.isEnabled)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (route.isEnabled) stringResource(R.string.details_enabled) else stringResource(
                        R.string.details_disabled
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (route.isEnabled)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
            }
        }
    }
}


@Composable
private fun ApiRouteConfigSection(type: RouteType.ApiRoute) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.details_api_config),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            DetailRow(
                label = stringResource(R.string.details_status_code),
                value = "${type.statusCode} ${HttpStatusCode.fromValue(type.statusCode).description}"
            )

            if (type.headers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.details_headers),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                type.headers.forEach { (key, value) ->
                    DetailRow(label = key, value = value, isSubItem = true)
                }
            }

            if (type.responseBody.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.details_response_body),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = type.responseBody,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StaticFileConfigSection(type: RouteType.StaticFile) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.details_file_config),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            DetailRow(label = stringResource(R.string.details_file_path), value = type.fileUri)
            type.mimeType?.let {
                DetailRow(label = stringResource(R.string.details_mime_type), value = it)
            }
        }
    }
}

@Composable
private fun DirectoryConfigSection(type: RouteType.Directory) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.details_directory_config),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            DetailRow(
                label = stringResource(R.string.details_directory_path),
                value = type.directoryUri
            )
            DetailRow(
                label = stringResource(R.string.details_allow_browsing),
                value = if (type.allowBrowsing) stringResource(R.string.details_yes) else stringResource(
                    R.string.details_no
                )
            )
            type.indexFile?.let {
                DetailRow(label = stringResource(R.string.details_index_file), value = it)
            }
        }
    }
}

@Composable
private fun RedirectConfigSection(type: RouteType.RedirectRoute) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.details_redirect_config),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            DetailRow(label = stringResource(R.string.details_target_url), value = type.targetUrl)
            DetailRow(
                label = stringResource(R.string.details_status_code),
                value = "${type.statusCode} (${
                    if (type.isPermanentRedirect()) stringResource(R.string.details_permanent) else stringResource(
                        R.string.details_temporary
                    )
                })"
            )
        }
    }
}

@Composable
private fun ProxyConfigSection(type: RouteType.ProxyRoute) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.details_proxy_config),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            DetailRow(label = stringResource(R.string.details_target_url), value = type.targetUrl)
            DetailRow(
                label = stringResource(R.string.details_preserve_host),
                value = if (type.preserveHostHeader) stringResource(R.string.details_yes) else stringResource(
                    R.string.details_no
                )
            )
            DetailRow(label = stringResource(R.string.details_timeout), value = "${type.timeout}ms")
        }
    }
}

@Composable
private fun BuiltInRouteConfigSection() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.details_builtin_route),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.details_builtin_route_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isSubItem: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = if (isSubItem) "  $label" else label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = if (isSubItem) FontFamily.Monospace else FontFamily.Default,
            modifier = Modifier.weight(2f)
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
}

