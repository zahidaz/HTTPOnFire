package com.azzahid.hof.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.azzahid.hof.R
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.RouteType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

@Composable
fun RouteCard(
    modifier: Modifier = Modifier,
    route: Route,
    onToggle: () -> Unit,
    onRemove: () -> Unit,
    onShare: () -> Unit = { },
    onDetails: () -> Unit = { }
) {
    val isBuiltInRoute = route.type is RouteType.BuiltInRoute
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (route.isEnabled) {
                    Modifier.clickable { onShare() }
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (route.isEnabled)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .alpha(if (route.isEnabled) 1f else 0.38f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MethodChip(method = route.method)

                    when (val type = route.type) {
                        is RouteType.ApiRoute -> {
                            if (type.statusCode != HttpStatusCode.OK.value) {
                                StatusCodeChip(statusCode = type.statusCode)
                            }
                        }

                        is RouteType.StaticFile -> {
                            TypeChip(
                                text = stringResource(R.string.route_type_files),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        is RouteType.Directory -> {
                            TypeChip(
                                text = stringResource(R.string.route_type_files),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        is RouteType.RedirectRoute -> {
                            TypeChip(
                                text = stringResource(R.string.route_type_redirect),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            if (type.statusCode != HttpStatusCode.Found.value) {
                                StatusCodeChip(statusCode = type.statusCode)
                            }
                        }

                        is RouteType.StatusRoute -> {
                            TypeChip(
                                text = stringResource(R.string.route_type_status),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        is RouteType.OpenApiRoute -> {
                            TypeChip(
                                text = stringResource(R.string.route_type_openapi),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        is RouteType.SwaggerRoute -> {
                            TypeChip(
                                text = stringResource(R.string.route_type_docs),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        is RouteType.NotificationRoute -> {
                            TypeChip(
                                text = stringResource(R.string.route_type_notification),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        is RouteType.ProxyRoute -> {
                            TypeChip(
                                text = stringResource(R.string.route_type_proxy),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Text(
                        text = route.path,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box {
                    var showMenu by remember { mutableStateOf(false) }

                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.cd_more_options),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (!isBuiltInRoute) {
                            DropdownMenuItem(
                                onClick = {
                                    onDetails()
                                    showMenu = false
                                },
                                text = {
                                    Text(
                                        text = stringResource(R.string.action_details),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            )
                        }

                        DropdownMenuItem(
                            onClick = {
                                onShare()
                                showMenu = false
                            },
                            text = {
                                Text(
                                    text = stringResource(R.string.action_share),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )

                        DropdownMenuItem(
                            onClick = {
                                onToggle()
                                showMenu = false
                            },
                            text = {
                                Text(
                                    text = if (route.isEnabled) stringResource(R.string.action_disable) else stringResource(
                                        R.string.action_enable
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (route.isEnabled)
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.primary
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (route.isEnabled) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = if (route.isEnabled)
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )

                        if (!isBuiltInRoute) {
                            DropdownMenuItem(
                                onClick = {
                                    onRemove()
                                    showMenu = false
                                },
                                text = {
                                    Text(
                                        text = stringResource(R.string.action_delete),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            if (route.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = route.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.alpha(if (route.isEnabled) 1f else 0.38f)
                )
            }
        }
    }
}

@Composable
internal fun MethodChip(method: HttpMethod) {
    val (containerColor, contentColor, borderColor) = when (method) {
        HttpMethod.Get -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            MaterialTheme.colorScheme.primary
        )

        HttpMethod.Post -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            MaterialTheme.colorScheme.secondary
        )

        HttpMethod.Put -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            MaterialTheme.colorScheme.tertiary
        )

        HttpMethod.Delete -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            MaterialTheme.colorScheme.error
        )

        HttpMethod.Patch -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.outline
        )

        else -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            MaterialTheme.colorScheme.primary
        )
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.semantics {
            contentDescription = "${method.value} HTTP method"
        }
    ) {
        Text(
            text = method.value,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun StatusCodeChip(statusCode: Int) {
    val (containerColor, contentColor) = when (statusCode) {
        in 200..299 -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        in 300..399 -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        in 400..499 -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        in 500..599 -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    AssistChip(
        onClick = { },
        label = {
            Text(
                text = statusCode.toString(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = contentColor
            )
        },
        modifier = Modifier.semantics {
            contentDescription = "HTTP status code $statusCode"
        },
        colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
            containerColor = containerColor
        )
    )
}

@Composable
private fun TypeChip(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color),
        modifier = Modifier.semantics {
            contentDescription = "$text Route type"
        }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}