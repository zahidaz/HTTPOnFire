package com.azzahid.hof.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.azzahid.hof.R
import com.azzahid.hof.domain.model.Route

@Composable
fun ShareDialog(
    urlPairs: List<Pair<String, String>>,
    selectedQrUrl: String?,
    qrBitmap: ImageBitmap?,
    onQrUrlSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onCopyToClipboard: (String) -> Unit,
    route: Route? = null
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
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.share_title),
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

                if (urlPairs.isNotEmpty()) {
                    QrCodeCard(qrBitmap = qrBitmap)

                    Spacer(modifier = Modifier.height(16.dp))

                    NetworkAddressesCard(
                        urlPairs = urlPairs,
                        selectedQrUrl = selectedQrUrl,
                        onCopyToClipboard = onCopyToClipboard,
                        onQrUrlSelected = onQrUrlSelected
                    )
                } else {
                    ServerNotRunningCard(route = route)
                }
            }
        }
    }
}

@Composable
private fun QrCodeCard(qrBitmap: ImageBitmap?) {
    qrBitmap?.let { bitmap ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                bitmap = bitmap,
                contentDescription = stringResource(R.string.cd_qr_code),
                modifier = Modifier.size(200.dp)
            )
        }
    }
}

@Composable
private fun NetworkAddressesCard(
    urlPairs: List<Pair<String, String>>,
    selectedQrUrl: String?,
    onCopyToClipboard: (String) -> Unit,
    onQrUrlSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.share_any_of),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(urlPairs) { (url, interfaceName) ->
                    NetworkAddressItem(
                        url = url,
                        interfaceName = interfaceName,
                        isSelected = url == selectedQrUrl,
                        onCopy = { onCopyToClipboard(url) },
                        onShowQr = { onQrUrlSelected(url) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NetworkAddressItem(
    url: String,
    interfaceName: String,
    isSelected: Boolean,
    onCopy: () -> Unit,
    onShowQr: () -> Unit
) {
    val localName = stringResource(R.string.share_network_local)
    val wifiName = stringResource(R.string.share_network_wifi)
    val displayName = remember(interfaceName, url, localName, wifiName) {
        when {
            interfaceName.equals("Localhost", ignoreCase = true) -> localName
            url.contains("192.168.") || url.contains("10.0.") || url.contains("172.") -> wifiName
            else -> interfaceName.take(10)
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = url,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row {
                    IconButton(
                        onClick = onShowQr,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = stringResource(R.string.cd_show_qr_code),
                            tint = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = stringResource(R.string.cd_copy_url),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerNotRunningCard(route: Route? = null) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.share_server_not_running),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (route != null) stringResource(R.string.share_start_server_route) else stringResource(
                    R.string.share_start_server_general
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}