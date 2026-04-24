package com.farmbirdfs.logjfeiowewg.presentation.screens.route

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import com.farmbirdfs.logjfeiowewg.data.local.entity.RouteStopEntity
import com.farmbirdfs.logjfeiowewg.data.local.entity.TransportPlanEntity
import com.farmbirdfs.logjfeiowewg.data.repository.RouteRepository
import com.farmbirdfs.logjfeiowewg.data.repository.TransportRepository
import com.farmbirdfs.logjfeiowewg.presentation.components.EmptyState
import com.farmbirdfs.logjfeiowewg.presentation.components.FarmTopBar
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

class RouteViewModel(
    private val routeRepo: RouteRepository,
    private val transportRepo: TransportRepository,
    private val transportId: Long
) : ViewModel() {
    val stops = routeRepo.getByTransport(transportId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    var transport by mutableStateOf<TransportPlanEntity?>(null)

    var showAddDialog by mutableStateOf(false)
    var newLocation by mutableStateOf("")
    var newTime by mutableStateOf("")
    var newNotes by mutableStateOf("")

    init {
        viewModelScope.launch { transport = transportRepo.getById(transportId) }
    }

    fun addStop() = viewModelScope.launch {
        routeRepo.insert(RouteStopEntity(
            transportPlanId = transportId,
            location = newLocation.trim(),
            stopOrder = stops.value.size + 1,
            estimatedTime = newTime.trim(),
            notes = newNotes.trim()
        ))
        newLocation = ""; newTime = ""; newNotes = ""; showAddDialog = false
    }

    fun toggleComplete(stop: RouteStopEntity) = viewModelScope.launch {
        routeRepo.setCompleted(stop.id, !stop.isCompleted)
    }

    fun delete(stop: RouteStopEntity) = viewModelScope.launch { routeRepo.delete(stop) }
}

@Composable
fun RouteScreen(
    transportId: Long,
    navController: NavController,
    viewModel: RouteViewModel = koinViewModel(parameters = { parametersOf(transportId) })
) {
    val stops by viewModel.stops.collectAsState()
    val transport = viewModel.transport

    Scaffold(
        topBar = { FarmTopBar(title = "Route", onBack = { navController.popBackStack() }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog = true }) {
                Icon(Icons.Filled.Add, null)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                transport?.let { t ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Route, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(t.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    "${t.origin} → ${t.destination}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.7f)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            if (stops.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.Route,
                        title = "No route stops",
                        subtitle = "Add stops to plan the transport route"
                    )
                }
            } else {
                itemsIndexed(stops, key = { _, s -> s.id }) { index, stop ->
                    RouteStopItem(
                        stop = stop,
                        index = index,
                        isLast = index == stops.lastIndex,
                        onToggle = { viewModel.toggleComplete(stop) },
                        onDelete = { viewModel.delete(stop) }
                    )
                }
            }
        }
    }

    if (viewModel.showAddDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showAddDialog = false },
            title = { Text("Add Route Stop") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = viewModel.newLocation,
                        onValueChange = { viewModel.newLocation = it },
                        label = { Text("Location *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = viewModel.newTime,
                        onValueChange = { viewModel.newTime = it },
                        label = { Text("Estimated Time") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = viewModel.newNotes,
                        onValueChange = { viewModel.newNotes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.addStop() },
                    enabled = viewModel.newLocation.isNotBlank()
                ) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { viewModel.showAddDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun RouteStopItem(
    stop: RouteStopEntity,
    index: Int,
    isLast: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(48.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .then(
                        if (stop.isCompleted)
                            Modifier
                        else
                            Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (stop.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (stop.isCompleted) {
                            Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                        } else {
                            Text(
                                "${index + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .padding(top = 2.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = if (!isLast) 8.dp else 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stop.location, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    if (stop.estimatedTime.isNotEmpty()) {
                        Text("ETA: ${stop.estimatedTime}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (stop.notes.isNotEmpty()) {
                        Text(stop.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Checkbox(checked = stop.isCompleted, onCheckedChange = { onToggle() })
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
