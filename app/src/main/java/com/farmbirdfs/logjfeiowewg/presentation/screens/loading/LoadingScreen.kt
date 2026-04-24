package com.farmbirdfs.logjfeiowewg.presentation.screens.loading

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.farmbirdfs.logjfeiowewg.data.local.entity.ContainerEntity
import com.farmbirdfs.logjfeiowewg.data.local.entity.LoadingRecordEntity
import com.farmbirdfs.logjfeiowewg.data.local.entity.TransportPlanEntity
import com.farmbirdfs.logjfeiowewg.data.repository.ContainerRepository
import com.farmbirdfs.logjfeiowewg.data.repository.LoadingRepository
import com.farmbirdfs.logjfeiowewg.data.repository.TransportRepository
import com.farmbirdfs.logjfeiowewg.navigation.Screen
import com.farmbirdfs.logjfeiowewg.presentation.components.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

class LoadingViewModel(
    private val loadingRepo: LoadingRepository,
    private val containerRepo: ContainerRepository,
    private val transportRepo: TransportRepository,
    private val transportId: Long
) : ViewModel() {
    val records = loadingRepo.getByTransport(transportId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val containers = containerRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalLoaded = loadingRepo.getTotalLoadedForTransport(transportId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    var transport by mutableStateOf<TransportPlanEntity?>(null)

    init {
        viewModelScope.launch {
            transport = transportRepo.getById(transportId)
        }
    }

    fun deleteRecord(record: LoadingRecordEntity) = viewModelScope.launch {
        loadingRepo.delete(record)
    }
}

@Composable
fun LoadingScreen(
    transportId: Long,
    navController: NavController,
    viewModel: LoadingViewModel = koinViewModel(parameters = { parametersOf(transportId) })
) {
    val records by viewModel.records.collectAsState()
    val containers by viewModel.containers.collectAsState()
    val total by viewModel.totalLoaded.collectAsState()
    val transport = viewModel.transport

    Scaffold(
        topBar = {
            FarmTopBar(
                title = "Loading",
                onBack = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.LoadingDetails.createRoute(transportId)) },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Add Loading") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                transport?.let { t ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(t.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${t.origin} → ${t.destination}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.7f)
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column {
                                    Text("Total Loaded", style = MaterialTheme.typography.labelSmall)
                                    Text("${total ?: 0}", style = MaterialTheme.typography.headlineSmall)
                                }
                                Column {
                                    Text("Records", style = MaterialTheme.typography.labelSmall)
                                    Text("${records.size}", style = MaterialTheme.typography.headlineSmall)
                                }
                            }
                        }
                    }
                }
            }

            if (records.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.MoveToInbox,
                        title = "No loading records",
                        subtitle = "Add loading records to track birds in containers"
                    )
                }
            } else {
                item { SectionHeader("Loading Records") }
                items(records, key = { it.id }) { record ->
                    LoadingRecordCard(
                        record = record,
                        containers = containers,
                        onDelete = { viewModel.deleteRecord(record) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingRecordCard(
    record: LoadingRecordEntity,
    containers: List<ContainerEntity>,
    onDelete: () -> Unit
) {
    val container = containers.find { it.id == record.containerId }
    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Inventory2, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = container?.name ?: "Container #${record.containerId}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${record.count} birds loaded",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = record.loadedAt.formatDateTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
