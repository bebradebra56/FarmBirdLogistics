package com.farmbirdfs.logjfeiowewg.presentation.screens.transport

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.farmbirdfs.logjfeiowewg.data.local.entity.TransportPlanEntity
import com.farmbirdfs.logjfeiowewg.data.local.entity.TransportStatus
import com.farmbirdfs.logjfeiowewg.data.repository.TransportRepository
import com.farmbirdfs.logjfeiowewg.navigation.Screen
import com.farmbirdfs.logjfeiowewg.presentation.components.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

class TransportPlansViewModel(private val repository: TransportRepository) : ViewModel() {
    val transports = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var filterStatus by mutableStateOf<String?>(null)

    val filtered: StateFlow<List<TransportPlanEntity>> = combine(transports, snapshotFlow { filterStatus }) { list, status ->
        if (status == null) list else list.filter { it.status == status }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(entity: TransportPlanEntity) = viewModelScope.launch { repository.delete(entity) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransportPlansScreen(
    navController: NavController,
    viewModel: TransportPlansViewModel = koinViewModel()
) {
    val transports by viewModel.filtered.collectAsState()
    var deleteTarget by remember { mutableStateOf<TransportPlanEntity?>(null) }

    Scaffold(
        topBar = { FarmTopBar(title = "Transport Plans") },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.CreateTransport.createRoute()) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("New Plan") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Filter chips — horizontally scrollable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = viewModel.filterStatus == null,
                    onClick = { viewModel.filterStatus = null },
                    label = { Text("All") }
                )
                listOf(
                    TransportStatus.PLANNED to "Planned",
                    TransportStatus.IN_PROGRESS to "In Progress",
                    TransportStatus.COMPLETED to "Completed",
                    TransportStatus.CANCELLED to "Cancelled"
                ).forEach { (status, label) ->
                    FilterChip(
                        selected = viewModel.filterStatus == status,
                        onClick = { viewModel.filterStatus = if (viewModel.filterStatus == status) null else status },
                        label = { Text(label) }
                    )
                }
            }

            if (transports.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Filled.LocalShipping,
                        title = "No transport plans",
                        subtitle = "Tap the button below to create your first transport plan"
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(transports, key = { it.id }) { transport ->
                        TransportPlanCard(
                            transport = transport,
                            onClick = { navController.navigate(Screen.CreateTransport.createRoute(transport.id)) },
                            onDelete = { deleteTarget = transport },
                            onLoadingClick = { navController.navigate(Screen.Loading.createRoute(transport.id)) },
                            onRouteClick = { navController.navigate(Screen.Route.createRoute(transport.id)) },
                            onConditionsClick = { navController.navigate(Screen.TransportConditions.createRoute(transport.id)) },
                            onArrivalClick = { navController.navigate(Screen.Arrival.createRoute(transport.id)) }
                        )
                    }
                }
            }
        }
    }

    deleteTarget?.let { t ->
        DeleteDialog(
            title = "Delete Transport",
            message = "Delete \"${t.name}\"? This will also remove all related loading records and route stops.",
            onConfirm = { viewModel.delete(t); deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }
}

private fun statusLabel(status: String) = when (status) {
    "PLANNED" -> "Planned"
    "IN_PROGRESS" -> "In Progress"
    "COMPLETED" -> "Completed"
    "CANCELLED" -> "Cancelled"
    else -> status
}

@Composable
private fun CardActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun TransportPlanCard(
    transport: TransportPlanEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onLoadingClick: () -> Unit,
    onRouteClick: () -> Unit,
    onConditionsClick: () -> Unit,
    onArrivalClick: () -> Unit
) {
    val (bgColor, textColor) = statusColor(transport.status)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title row with delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = transport.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.height(4.dp))
            // Status chip on its own row
            StatusChip(
                text = statusLabel(transport.status),
                containerColor = bgColor,
                contentColor = textColor
            )
            Spacer(Modifier.height(6.dp))
            // Info rows
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(2.dp))
                Text(
                    "${transport.origin} → ${transport.destination}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.CalendarToday, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(2.dp))
                Text(
                    transport.date.formatDate(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (transport.birdCount > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Egg, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(Modifier.width(2.dp))
                    Text(
                        "${transport.birdCount} birds",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CardActionButton(Icons.Filled.MoveToInbox, "Loading", onLoadingClick)
                CardActionButton(Icons.Filled.Route, "Route", onRouteClick)
                CardActionButton(Icons.Filled.Thermostat, "Conditions", onConditionsClick)
                CardActionButton(Icons.Filled.FlagCircle, "Arrival", onArrivalClick)
            }
        }
    }
}
