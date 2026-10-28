package com.azzahid.hof.ui.screens.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azzahid.hof.Constants
import com.azzahid.hof.R
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.RouteType
import com.azzahid.hof.domain.state.HomeUiState
import com.azzahid.hof.domain.state.ServerStatus
import com.azzahid.hof.ui.components.RouteCard
import com.azzahid.hof.ui.components.RouteDetailsDialog
import com.azzahid.hof.ui.components.ShareDialog
import com.azzahid.hof.ui.components.appbars.HomeAppBar
import com.azzahid.hof.ui.providers.LocalViewModelFactory
import com.azzahid.hof.ui.viewmodel.HomeViewModel


@Composable
fun TabHomeScreen(
    onNavigateToRouteBuilder: (String?) -> Unit
) {
    val viewModelFactory = LocalViewModelFactory.current
    val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    var showShareDialog by remember { mutableStateOf(false) }
    var selectedRouteForDetails by remember { mutableStateOf<Route?>(null) }
    var selectedRouteForShare by remember { mutableStateOf<Route?>(null) }

    val serverUrlPairs = remember(homeUiState.networkAddresses, homeUiState.serverStatus) {
        if (homeUiState.serverStatus == ServerStatus.STARTED) homeUiState.networkAddresses else emptyList()
    }
    var selectedServerQrUrl by remember(serverUrlPairs) {
        mutableStateOf(serverUrlPairs.firstOrNull()?.first)
    }

    val routeUrlPairs = remember(
        homeUiState.networkAddresses,
        homeUiState.serverStatus,
        selectedRouteForShare?.path
    ) {
        val route = selectedRouteForShare
        if (homeUiState.serverStatus != ServerStatus.STARTED || route == null) {
            emptyList()
        } else {
            val cleanPath = if (route.path.startsWith("/")) route.path else "/${route.path}"
            homeUiState.networkAddresses.map { (url, interfaceName) ->
                "$url$cleanPath" to interfaceName
            }
        }
    }
    var selectedRouteQrUrl by remember(routeUrlPairs) {
        mutableStateOf(routeUrlPairs.firstOrNull()?.first)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var previousServerStatus by remember { mutableStateOf(homeUiState.serverStatus) }
    val serverPort = homeUiState.serverPort.toIntOrNull() ?: Constants.DEFAULT_PORT
    val startedMessage = stringResource(R.string.server_snackbar_started, serverPort)
    val errorMessage = stringResource(R.string.server_snackbar_error)

    LaunchedEffect(homeUiState.serverStatus) {
        val current = homeUiState.serverStatus
        if (current != previousServerStatus) {
            when (current) {
                ServerStatus.STARTED -> snackbarHostState.showSnackbar(startedMessage)
                ServerStatus.ERROR -> snackbarHostState.showSnackbar(errorMessage)
                else -> {}
            }
            previousServerStatus = current
        }
    }

    LaunchedEffect(Unit) {
        homeViewModel.userMessages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        homeViewModel.importFiles(uris)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            HomeAppBar(
                serverStatus = homeUiState.serverStatus,
                serverUrl = homeUiState.serverUrl,
                onToggleServer = homeViewModel::toggleServer,
                onShareClick = { showShareDialog = true },
                onSendFilesClick = { filePicker.launch(arrayOf("*/*")) }
            )

            HomeScreenContent(
                modifier = Modifier.weight(1f),
                homeUiState = homeUiState,
                onToggleRoute = homeViewModel::toggleRoute,
                onRemoveRoute = homeViewModel::removeRoute,
                onNavigateToRouteBuilder = onNavigateToRouteBuilder,
                onShowRouteDetails = { route -> selectedRouteForDetails = route },
                onShareRoute = { route -> selectedRouteForShare = route }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }

    if (showShareDialog) {
        selectedServerQrUrl?.let { url ->
            homeViewModel.generateServerQr(url)
        }

        ShareDialog(
            urlPairs = serverUrlPairs,
            selectedQrUrl = selectedServerQrUrl,
            qrBitmap = homeUiState.serverQrBitmap,
            onQrUrlSelected = { url ->
                selectedServerQrUrl = url
                homeViewModel.generateServerQr(url)
            },
            onCopyToClipboard = homeViewModel::copyToClipboard,
            onDismiss = { showShareDialog = false }
        )
    }

    selectedRouteForDetails?.let { route ->
        RouteDetailsDialog(
            route = route,
            onDismiss = { selectedRouteForDetails = null }
        )
    }

    selectedRouteForShare?.let { route ->
        selectedRouteQrUrl?.let { url ->
            homeViewModel.generateRouteQr(url)
        }

        ShareDialog(
            urlPairs = routeUrlPairs,
            selectedQrUrl = selectedRouteQrUrl,
            qrBitmap = homeUiState.routeQrBitmap,
            onQrUrlSelected = { url ->
                selectedRouteQrUrl = url
                homeViewModel.generateRouteQr(url)
            },
            onCopyToClipboard = homeViewModel::copyToClipboard,
            onDismiss = { selectedRouteForShare = null },
            route = route
        )
    }
}

@Composable
private fun HomeScreenContent(
    modifier: Modifier = Modifier,
    homeUiState: HomeUiState,
    onToggleRoute: (Route) -> Unit,
    onRemoveRoute: (String) -> Unit,
    onNavigateToRouteBuilder: (String?) -> Unit,
    onShowRouteDetails: (Route) -> Unit,
    onShareRoute: (Route) -> Unit,
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val userRoutes = remember(homeUiState.routes) {
        homeUiState.routes
            .filter { it.type !is RouteType.BuiltInRoute }
            .sortedBy { it.order }
    }
    val builtInRoutes = remember(homeUiState.routes) {
        homeUiState.routes.filter { it.type is RouteType.BuiltInRoute }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            HomeTabSelector(
                selectedIndex = selectedTabIndex,
                userCount = userRoutes.size,
                builtInCount = builtInRoutes.size,
                onSelect = { selectedTabIndex = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 4.dp, bottom = 12.dp)
            )

            when (selectedTabIndex) {
                0 -> UserRoutesList(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    routes = userRoutes,
                    onToggleRoute = onToggleRoute,
                    onRemoveRoute = onRemoveRoute,
                    onShowRouteDetails = onShowRouteDetails,
                    onShareRoute = onShareRoute,
                    onNavigateToRouteBuilder = onNavigateToRouteBuilder,
                )

                1 -> BuiltInRoutesList(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    routes = builtInRoutes,
                    onShareRoute = onShareRoute,
                    onToggleRoute = onToggleRoute,
                )
            }
        }

        if (selectedTabIndex == 0) {
            ExtendedFloatingActionButton(
                onClick = { onNavigateToRouteBuilder(null) },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.cd_add_route),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun HomeTabSelector(
    selectedIndex: Int,
    userCount: Int,
    builtInCount: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        stringResource(R.string.home_tab_user) to userCount,
        stringResource(R.string.home_tab_system) to builtInCount
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabs.forEachIndexed { index, (title, count) ->
                val selected = selectedIndex == index
                val containerColor by animateColorAsState(
                    targetValue = if (selected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                    } else {
                        Color.Transparent
                    },
                    label = "tabContainer"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    label = "tabContent"
                )

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(containerColor)
                        .selectable(
                            selected = selected,
                            role = Role.Tab,
                            onClick = { onSelect(index) }
                        )
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = contentColor,
                        maxLines = 1
                    )

                    if (count > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            }
                        ) {
                            Text(
                                text = count.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = contentColor,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun BuiltInRoutesList(
    modifier: Modifier = Modifier,
    routes: List<Route>,
    onShareRoute: (Route) -> Unit,
    onToggleRoute: (Route) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp)
    ) {
        items(
            count = routes.size,
            key = { index -> routes[index].id }
        ) { index ->
            val route = routes[index]
            RouteCard(
                route = route,
                onToggle = { onToggleRoute(route) },
                onRemove = { },
                onShare = { onShareRoute(route) },
                onDetails = { }
            )
        }
    }
}

@Composable
private fun UserRoutesList(
    modifier: Modifier = Modifier,
    routes: List<Route>,
    onRemoveRoute: (String) -> Unit,
    onShowRouteDetails: (Route) -> Unit,
    onShareRoute: (Route) -> Unit,
    onToggleRoute: (Route) -> Unit,
    onNavigateToRouteBuilder: (String?) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp)
    ) {
        if (routes.isNotEmpty()) {
            items(
                count = routes.size,
                key = { index -> routes[index].id }
            ) { index ->
                val route = routes[index]
                RouteCard(
                    route = route,
                    onToggle = { onToggleRoute(route) },
                    onRemove = { onRemoveRoute(route.id) },
                    onShare = { onShareRoute(route) },
                    onDetails = { onShowRouteDetails(route) }
                )
            }
        } else {
            item {
                EmptyRoutesCard(onNavigateToRouteBuilder = onNavigateToRouteBuilder)
            }
        }
    }
}

@Composable
private fun EmptyRoutesCard(onNavigateToRouteBuilder: (String?) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 40.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Route,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.home_empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.home_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { onNavigateToRouteBuilder(null) }) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.home_empty_button))
            }
        }
    }
}
