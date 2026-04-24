package com.farmbirdfs.logjfeiowewg.presentation.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.farmbirdfs.logjfeiowewg.data.local.entity.ActivityLogEntity
import com.farmbirdfs.logjfeiowewg.data.repository.ActivityLogRepository
import com.farmbirdfs.logjfeiowewg.presentation.components.*
import kotlinx.coroutines.flow.*
import org.koin.androidx.compose.koinViewModel

class ActivityHistoryViewModel(private val repository: ActivityLogRepository) : ViewModel() {
    val logs = repository.getRecent(100)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@Composable
fun ActivityHistoryScreen(
    navController: NavController,
    viewModel: ActivityHistoryViewModel = koinViewModel()
) {
    val logs by viewModel.logs.collectAsState()

    Scaffold(
        topBar = { FarmTopBar(title = "Activity History", onBack = { navController.popBackStack() }) }
    ) { padding ->
        if (logs.isEmpty()) {
            Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    icon = Icons.Filled.History,
                    title = "No activity yet",
                    subtitle = "Your actions will be tracked here"
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val grouped = logs.groupBy { it.timestamp.formatDate() }
                grouped.forEach { (date, dayLogs) ->
                    item(key = "header_$date") {
                        SectionHeader(date)
                    }
                    items(dayLogs, key = { it.id }) { log ->
                        ActivityLogItem(log)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityLogItem(log: ActivityLogEntity) {
    val (icon, color) = when (log.category) {
        "Transport" -> Icons.Filled.LocalShipping to MaterialTheme.colorScheme.primary
        "Birds" -> Icons.Filled.Pets to MaterialTheme.colorScheme.secondary
        "Containers" -> Icons.Filled.Inventory2 to MaterialTheme.colorScheme.tertiary
        "Loading" -> Icons.Filled.MoveToInbox to MaterialTheme.colorScheme.secondary
        "Route" -> Icons.Filled.Route to MaterialTheme.colorScheme.primary
        "Conditions" -> Icons.Filled.Thermostat to MaterialTheme.colorScheme.tertiary
        "Housing" -> Icons.Filled.Home to MaterialTheme.colorScheme.primary
        "Feeding" -> Icons.Filled.Grain to MaterialTheme.colorScheme.secondary
        "Health" -> Icons.Filled.HealthAndSafety to MaterialTheme.colorScheme.error
        "Tasks" -> Icons.Filled.CheckCircle to MaterialTheme.colorScheme.secondary
        "Inventory" -> Icons.Filled.Inventory to MaterialTheme.colorScheme.tertiary
        else -> Icons.Filled.Info to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(log.action, style = MaterialTheme.typography.bodyMedium)
                if (log.details.isNotEmpty()) {
                    Text(log.details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(log.timestamp.formatDateTime(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StatusChip(text = log.category)
        }
    }
}
