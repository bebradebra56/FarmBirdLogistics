package com.farmbirdfs.logjfeiowewg.presentation.screens.feeding

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.farmbirdfs.logjfeiowewg.data.local.entity.BirdGroupEntity
import com.farmbirdfs.logjfeiowewg.data.local.entity.FeedRecordEntity
import com.farmbirdfs.logjfeiowewg.data.repository.BirdGroupRepository
import com.farmbirdfs.logjfeiowewg.data.repository.FeedingRepository
import com.farmbirdfs.logjfeiowewg.navigation.Screen
import com.farmbirdfs.logjfeiowewg.presentation.components.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.Calendar

class FeedingViewModel(
    private val feedingRepo: FeedingRepository,
    private val birdGroupRepo: BirdGroupRepository
) : ViewModel() {
    val records = feedingRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val birdGroups = birdGroupRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyTotal: StateFlow<Float> = records.map { allRecords ->
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val from = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val to = cal.timeInMillis
        allRecords.filter { it.date in from until to }.sumOf { it.amount.toDouble() }.toFloat()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    fun delete(record: FeedRecordEntity) = viewModelScope.launch { feedingRepo.delete(record) }

    fun getGroupName(id: Long, groups: List<BirdGroupEntity>): String {
        return groups.find { it.id == id }?.let { "${it.birdType} (${it.count})" } ?: "Group #$id"
    }
}

@Composable
fun FeedingScreen(
    navController: NavController,
    viewModel: FeedingViewModel = koinViewModel()
) {
    val records by viewModel.records.collectAsState()
    val groups by viewModel.birdGroups.collectAsState()
    val monthlyTotal by viewModel.monthlyTotal.collectAsState()

    Scaffold(
        topBar = {
            FarmTopBar(
                title = "Feeding",
                onBack = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.FeedRecord.createRoute()) },
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
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.Grain, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        Column {
                            Text("This Month's Feed", style = MaterialTheme.typography.labelMedium)
                            Text(
                                "${String.format("%.1f", monthlyTotal)} kg",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }
                }
            }

            if (records.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.Grain,
                        title = "No feeding records",
                        subtitle = "Track what and how much you feed your birds"
                    )
                }
            } else {
                items(records, key = { it.id }) { record ->
                    FeedRecordCard(
                        record = record,
                        groupName = viewModel.getGroupName(record.birdGroupId, groups),
                        onClick = { navController.navigate(Screen.FeedRecord.createRoute(record.id)) },
                        onDelete = { viewModel.delete(record) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedRecordCard(
    record: FeedRecordEntity,
    groupName: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(onClick = onClick, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Grain, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${record.feedType} — ${record.amount}${record.unit}", style = MaterialTheme.typography.titleSmall)
                Text(groupName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(record.date.formatDate(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
