package com.farmbirdfs.logjfeiowewg.presentation.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.farmbirdfs.logjfeiowewg.data.preferences.UserPreferences
import com.farmbirdfs.logjfeiowewg.data.repository.*
import com.farmbirdfs.logjfeiowewg.navigation.Screen
import com.farmbirdfs.logjfeiowewg.presentation.components.*
import kotlinx.coroutines.flow.*
import org.koin.androidx.compose.koinViewModel
import java.util.Calendar

class DashboardViewModel(
    private val transportRepository: TransportRepository,
    private val birdGroupRepository: BirdGroupRepository,
    private val containerRepository: ContainerRepository,
    private val taskRepository: TaskRepository,
    private val preferences: UserPreferences
) : ViewModel() {

    val activeTransportCount = transportRepository.getActiveCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalBirdGroups = birdGroupRepository.getTotalGroupCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalBirds = birdGroupRepository.getTotalBirdCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val containersUsed = containerRepository.getTotalCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingTasks = taskRepository.getPendingCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val upcomingTransports = transportRepository.getUpcoming(System.currentTimeMillis())
        .map { it.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userName = preferences.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val farmName = preferences.farmName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
}

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val activeCount by viewModel.activeTransportCount.collectAsState()
    val groupCount by viewModel.totalBirdGroups.collectAsState()
    val birdCount by viewModel.totalBirds.collectAsState()
    val containers by viewModel.containersUsed.collectAsState()
    val tasks by viewModel.pendingTasks.collectAsState()
    val upcoming by viewModel.upcomingTransports.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val farmName by viewModel.farmName.collectAsState()

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            // Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = greeting + if (userName.isNotEmpty()) ", $userName" else "!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (farmName.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Agriculture,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = farmName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Here's your farm overview for today.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        item {
            SectionHeader("Overview")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    StatCard(
                        label = "Active Transports",
                        value = "$activeCount",
                        icon = Icons.Filled.LocalShipping,
                        modifier = Modifier.width(150.dp),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                item {
                    StatCard(
                        label = "Bird Groups",
                        value = "$groupCount",
                        icon = Icons.Filled.Pets,
                        modifier = Modifier.width(150.dp),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                item {
                    StatCard(
                        label = "Total Birds",
                        value = "${birdCount ?: 0}",
                        icon = Icons.Filled.Egg,
                        modifier = Modifier.width(150.dp),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                item {
                    StatCard(
                        label = "Containers",
                        value = "$containers",
                        icon = Icons.Filled.Inventory2,
                        modifier = Modifier.width(150.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item {
                    StatCard(
                        label = "Pending Tasks",
                        value = "$tasks",
                        icon = Icons.Filled.CheckCircle,
                        modifier = Modifier.width(150.dp),
                        containerColor = if (tasks > 0)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (tasks > 0)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        item {
            SectionHeader("Quick Actions")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    label = "New Transport",
                    icon = Icons.Filled.Add,
                    onClick = { navController.navigate(Screen.CreateTransport.createRoute()) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    label = "Add Birds",
                    icon = Icons.Filled.Pets,
                    onClick = { navController.navigate(Screen.AddBirdGroup.createRoute()) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    label = "Health Check",
                    icon = Icons.Filled.HealthAndSafety,
                    onClick = { navController.navigate(Screen.HealthCheck.route) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        if (upcoming.isNotEmpty()) {
            item { SectionHeader("Upcoming Transports") }
            items(upcoming) { transport ->
                UpcomingTransportCard(
                    transport = transport,
                    onClick = { navController.navigate(Screen.TransportPlans.route) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        } else {
            item {
                SectionHeader("Upcoming Transports")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.EventBusy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "No upcoming transports scheduled",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun UpcomingTransportCard(
    transport: TransportPlanEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = statusColor(transport.status)
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Filled.LocalShipping,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transport.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${transport.origin} → ${transport.destination}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = transport.date.formatDate(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusChip(
                text = transport.status.replace("_", " "),
                containerColor = bgColor,
                contentColor = textColor
            )
        }
    }
}
