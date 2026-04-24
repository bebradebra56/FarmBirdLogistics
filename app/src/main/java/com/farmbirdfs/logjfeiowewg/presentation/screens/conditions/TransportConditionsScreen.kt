package com.farmbirdfs.logjfeiowewg.presentation.screens.conditions

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
import com.farmbirdfs.logjfeiowewg.data.local.entity.TransportConditionEntity
import com.farmbirdfs.logjfeiowewg.data.local.entity.VentilationLevel
import com.farmbirdfs.logjfeiowewg.data.repository.ConditionRepository
import com.farmbirdfs.logjfeiowewg.presentation.components.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

class TransportConditionsViewModel(
    private val repo: ConditionRepository,
    private val transportId: Long
) : ViewModel() {
    val conditions = repo.getByTransport(transportId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var showAddDialog by mutableStateOf(false)
    var temperature by mutableStateOf("20")
    var humidity by mutableStateOf("60")
    var ventilation by mutableStateOf(VentilationLevel.GOOD)
    var notes by mutableStateOf("")
    var showVentMenu by mutableStateOf(false)

    fun addCondition() = viewModelScope.launch {
        repo.insert(TransportConditionEntity(
            transportPlanId = transportId,
            temperature = temperature.toFloatOrNull() ?: 20f,
            humidity = humidity.toFloatOrNull() ?: 60f,
            ventilation = ventilation,
            notes = notes.trim()
        ))
        temperature = "20"; humidity = "60"; ventilation = VentilationLevel.GOOD; notes = ""
        showAddDialog = false
    }

    fun delete(entity: TransportConditionEntity) = viewModelScope.launch { repo.delete(entity) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransportConditionsScreen(
    transportId: Long,
    navController: NavController,
    viewModel: TransportConditionsViewModel = koinViewModel(parameters = { parametersOf(transportId) })
) {
    val conditions by viewModel.conditions.collectAsState()

    Scaffold(
        topBar = { FarmTopBar(title = "Transport Conditions", onBack = { navController.popBackStack() }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog = true }) {
                Icon(Icons.Filled.Add, null)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            conditions.firstOrNull()?.let { latest ->
                item {
                    ConditionSummaryCard(latest)
                }
            }

            if (conditions.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.Thermostat,
                        title = "No condition logs",
                        subtitle = "Log temperature, humidity, and ventilation readings"
                    )
                }
            } else {
                item { SectionHeader("Condition History") }
                items(conditions, key = { it.id }) { cond ->
                    ConditionCard(cond, onDelete = { viewModel.delete(cond) })
                }
            }
        }
    }

    if (viewModel.showAddDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showAddDialog = false },
            title = { Text("Log Conditions") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = viewModel.temperature,
                        onValueChange = { viewModel.temperature = it },
                        label = { Text("Temperature (°C)") },
                        leadingIcon = { Icon(Icons.Filled.Thermostat, null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = viewModel.humidity,
                        onValueChange = { viewModel.humidity = it },
                        label = { Text("Humidity (%)") },
                        leadingIcon = { Icon(Icons.Filled.WaterDrop, null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    ExposedDropdownMenuBox(
                        expanded = viewModel.showVentMenu,
                        onExpandedChange = { viewModel.showVentMenu = it }
                    ) {
                        OutlinedTextField(
                            value = viewModel.ventilation,
                            onValueChange = {},
                            label = { Text("Ventilation") },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(viewModel.showVentMenu) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = viewModel.showVentMenu,
                            onDismissRequest = { viewModel.showVentMenu = false }
                        ) {
                            VentilationLevel.all.forEach { v ->
                                DropdownMenuItem(
                                    text = { Text(v) },
                                    onClick = { viewModel.ventilation = v; viewModel.showVentMenu = false }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = viewModel.notes,
                        onValueChange = { viewModel.notes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.addCondition() }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { viewModel.showAddDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ConditionSummaryCard(condition: TransportConditionEntity) {
    val ventColor = when (condition.ventilation) {
        VentilationLevel.GOOD -> MaterialTheme.colorScheme.primary
        VentilationLevel.FAIR -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Latest Reading", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ConditionMetric(
                    icon = Icons.Filled.Thermostat,
                    value = "${condition.temperature}°C",
                    label = "Temperature"
                )
                ConditionMetric(
                    icon = Icons.Filled.WaterDrop,
                    value = "${condition.humidity}%",
                    label = "Humidity"
                )
                ConditionMetric(
                    icon = Icons.Filled.Air,
                    value = condition.ventilation,
                    label = "Ventilation",
                    valueColor = ventColor
                )
            }
        }
    }
}

@Composable
private fun ConditionMetric(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = valueColor, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = valueColor)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ConditionCard(cond: TransportConditionEntity, onDelete: () -> Unit) {
    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Thermostat, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${cond.temperature}°C · ${cond.humidity}% · ${cond.ventilation}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    cond.timestamp.formatDateTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (cond.notes.isNotEmpty()) {
                    Text(cond.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
