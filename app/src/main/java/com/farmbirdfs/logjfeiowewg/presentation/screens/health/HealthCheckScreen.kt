package com.farmbirdfs.logjfeiowewg.presentation.screens.health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.farmbirdfs.logjfeiowewg.data.local.entity.BirdGroupEntity
import com.farmbirdfs.logjfeiowewg.data.local.entity.HealthRecordEntity
import com.farmbirdfs.logjfeiowewg.data.repository.BirdGroupRepository
import com.farmbirdfs.logjfeiowewg.data.repository.HealthRepository
import com.farmbirdfs.logjfeiowewg.navigation.Screen
import com.farmbirdfs.logjfeiowewg.presentation.components.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

class HealthCheckViewModel(
    private val healthRepo: HealthRepository,
    private val birdGroupRepo: BirdGroupRepository
) : ViewModel() {
    val records = healthRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val groups = birdGroupRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalMortality = healthRepo.getTotalMortality()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun delete(record: HealthRecordEntity) = viewModelScope.launch { healthRepo.delete(record) }
    fun getGroupName(id: Long, groups: List<BirdGroupEntity>) =
        groups.find { it.id == id }?.let { "${it.birdType} (${it.count})" } ?: "Group #$id"
}

@Composable
fun HealthCheckScreen(
    navController: NavController,
    viewModel: HealthCheckViewModel = koinViewModel()
) {
    val records by viewModel.records.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val mortality by viewModel.totalMortality.collectAsState()

    Scaffold(
        topBar = {
            FarmTopBar(
                title = "Health Check",
                onBack = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.HealthRecord.createRoute()) },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Add Record") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = "Total Records",
                        value = "${records.size}",
                        icon = Icons.Filled.HealthAndSafety,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Total Mortality",
                        value = "${mortality ?: 0}",
                        icon = Icons.Filled.Warning,
                        modifier = Modifier.weight(1f),
                        containerColor = if ((mortality ?: 0) > 0)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if ((mortality ?: 0) > 0)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (records.isNotEmpty()) {
                val conditionCounts = records.groupBy { it.condition }.mapValues { it.value.size }
                item {
                    HealthSummaryChart(conditionCounts)
                }
            }

            if (records.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.HealthAndSafety,
                        title = "No health records",
                        subtitle = "Record health checks to monitor your flock's condition"
                    )
                }
            } else {
                item { SectionHeader("Health Records") }
                items(records, key = { it.id }) { record ->
                    HealthRecordCard(
                        record = record,
                        groupName = viewModel.getGroupName(record.birdGroupId, groups),
                        onClick = { navController.navigate(Screen.HealthRecord.createRoute(record.id)) },
                        onDelete = { viewModel.delete(record) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HealthSummaryChart(conditionCounts: Map<String, Int>) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Condition Summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            conditionCounts.forEach { (condition, count) ->
                val (bg, fg) = statusColor(condition)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(fg)
                    )
                    Text(condition, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    StatusChip(text = "$count", containerColor = bg, contentColor = fg)
                }
            }
        }
    }
}

@Composable
private fun HealthRecordCard(
    record: HealthRecordEntity,
    groupName: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val (bg, fg) = statusColor(record.condition)
    Card(onClick = onClick, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.HealthAndSafety, null, tint = fg, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(groupName, style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(text = record.condition, containerColor = bg, contentColor = fg)
                    if (record.mortality > 0) {
                        StatusChip(
                            text = "${record.mortality} dead",
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Text(record.date.formatDate(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (record.notes.isNotEmpty()) {
                    Text(record.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
