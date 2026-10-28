package com.azzahid.hof.ui.components.appbars

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.azzahid.hof.R
import com.azzahid.hof.domain.state.ServerStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppBar(
    serverStatus: ServerStatus,
    serverUrl: String?,
    onToggleServer: () -> Unit,
    onShareClick: () -> Unit,
    onSendFilesClick: () -> Unit
) {
    val isBusy = serverStatus == ServerStatus.STARTING || serverStatus == ServerStatus.STOPPING
    val isRunning = serverStatus == ServerStatus.STARTED

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusIndicator(serverStatus = serverStatus, pulsing = isBusy)

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
                    if (isRunning && serverUrl != null) {
                        Text(
                            text = serverUrl.removePrefix("http://"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        actions = {
            IconButton(onClick = onSendFilesClick) {
                Icon(
                    imageVector = Icons.Outlined.Send,
                    contentDescription = stringResource(R.string.cd_send_files),
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isRunning) {
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = stringResource(R.string.cd_share_server_url),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            FilledTonalButton(
                onClick = onToggleServer,
                enabled = !isBusy,
                modifier = Modifier
                    .height(40.dp)
                    .padding(end = 8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isRunning) {
                        stringResource(R.string.server_stop)
                    } else {
                        stringResource(R.string.server_start)
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    )
}

@Composable
private fun StatusIndicator(serverStatus: ServerStatus, pulsing: Boolean) {
    val color = when (serverStatus) {
        ServerStatus.STARTED -> MaterialTheme.colorScheme.primary
        ServerStatus.ERROR -> MaterialTheme.colorScheme.error
        ServerStatus.STARTING, ServerStatus.STOPPING -> MaterialTheme.colorScheme.tertiary
        ServerStatus.STOPPED -> MaterialTheme.colorScheme.outline
    }

    val transition = rememberInfiniteTransition(label = "statusPulse")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Restart
        ),
        label = "statusPulseAlpha"
    )

    Box(
        modifier = Modifier.size(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (pulsing) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .alpha(pulseAlpha)
                    .background(color = color, shape = CircleShape)
            )
        }
        Box(
            modifier = Modifier
                .size(if (serverStatus == ServerStatus.STARTED) 12.dp else 10.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (serverStatus == ServerStatus.STARTED) {
                        Modifier.border(
                            width = 3.dp,
                            color = color.copy(alpha = 0.22f),
                            shape = CircleShape
                        )
                    } else {
                        Modifier
                    }
                )
        )
    }
}
