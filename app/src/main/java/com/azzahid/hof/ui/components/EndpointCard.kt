package com.azzahid.hof.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.azzahid.hof.domain.model.Endpoint
import com.azzahid.hof.domain.model.EndpointType
import com.azzahid.hof.domain.model.HttpMethod
import io.ktor.http.HttpStatusCode

@Composable
fun EndpointCard(
    modifier: Modifier = Modifier,
    endpoint: Endpoint,
    onToggle: () -> Unit,
    onRemove: () -> Unit,
    onEdit: () -> Unit = { }
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (endpoint.isEnabled)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (endpoint.isEnabled) 2.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (endpoint.description.isNotBlank()) {
                    Text(
                        text = endpoint.description,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (endpoint.isEnabled)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Box {
                    var showMenu by remember { mutableStateOf(false) }

                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                onToggle()
                                showMenu = false
                            },
                            text = {
                                Text(
                                    text = if (endpoint.isEnabled) "Disable" else "Enable",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (endpoint.isEnabled)
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.primary
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (endpoint.isEnabled) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = if (endpoint.isEnabled)
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )

                        DropdownMenuItem(
                            onClick = {
                                onEdit()
                                showMenu = false
                            },
                            text = {
                                Text(
                                    text = "Edit",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )

                        DropdownMenuItem(
                            onClick = {
                                onRemove()
                                showMenu = false
                            },
                            text = {
                                Text(
                                    text = "Delete",
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

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MethodChip(method = endpoint.method)

                when (val type = endpoint.type) {
                    is EndpointType.ApiEndpoint -> {
                        if (type.statusCode != HttpStatusCode.OK.value) {
                            StatusCodeChip(statusCode = type.statusCode)
                        }
                    }

                    is EndpointType.StaticFile -> {
                        TypeChip(text = "FILE", color = MaterialTheme.colorScheme.primary)
                    }

                    is EndpointType.Directory -> {
                        TypeChip(text = "DIR", color = MaterialTheme.colorScheme.secondary)
                    }

                    is EndpointType.RedirectEndpoint -> {
                        TypeChip(text = "REDIRECT", color = MaterialTheme.colorScheme.tertiary)
                        if (type.statusCode != HttpStatusCode.Found.value) {
                            StatusCodeChip(statusCode = type.statusCode)
                        }
                    }

                    is EndpointType.ProxyEndpoint -> {
                        TypeChip(text = "PROXY", color = MaterialTheme.colorScheme.primary)
                    }
                }

                Text(
                    text = endpoint.path,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = if (endpoint.isEnabled)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MethodChip(method: HttpMethod) {
    val (containerColor, contentColor, borderColor) = when (method) {
        HttpMethod.GET -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            MaterialTheme.colorScheme.primary
        )

        HttpMethod.POST -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            MaterialTheme.colorScheme.secondary
        )

        HttpMethod.PUT -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            MaterialTheme.colorScheme.tertiary
        )

        HttpMethod.DELETE -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            MaterialTheme.colorScheme.error
        )

        HttpMethod.PATCH -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.outline
        )
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.semantics {
            contentDescription = "${method.name} HTTP method"
        }
    ) {
        Text(
            text = method.name,
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
            contentDescription = "$text endpoint type"
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