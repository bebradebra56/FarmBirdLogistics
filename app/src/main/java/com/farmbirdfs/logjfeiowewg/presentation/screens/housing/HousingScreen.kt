package com.farmbirdfs.logjfeiowewg.presentation.screens.housing

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.farmbirdfs.logjfeiowewg.data.local.entity.HousingEntity
import com.farmbirdfs.logjfeiowewg.data.repository.HousingRepository
import com.farmbirdfs.logjfeiowewg.navigation.Screen
import com.farmbirdfs.logjfeiowewg.presentation.components.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

class HousingViewModel(private val repository: HousingRepository) : ViewModel() {
    val units = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(entity: HousingEntity) = viewModelScope.launch { repository.delete(entity) }
}

@Composable
fun HousingScreen(
    navController: NavController,
    viewModel: HousingViewModel = koinViewModel()
) {
    val units by viewModel.units.collectAsState()
    var deleteTarget by remember { mutableStateOf<HousingEntity?>(null) }

    Scaffold(
        topBar = {
            FarmTopBar(
                title = "Housing",
                onBack = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.AddHousing.createRoute()) },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Add Housing") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (units.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val totalCap = units.sumOf { it.capacity }
                    val totalOccupied = units.sumOf { it.currentCount }
                    StatCard(
                        label = "Total Capacity",
                        value = "$totalCap",
                        icon = Icons.Filled.Home,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Occupied",
                        value = "$totalOccupied",
                        icon = Icons.Filled.Egg,
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            if (units.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Filled.Home,
                        title = "No housing units",
                        subtitle = "Add coops, outdoor pens, or barns to manage your flock"
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(units, key = { it.id }) { unit ->
                        HousingCard(
                            unit = unit,
                            onClick = { navController.navigate(Screen.AddHousing.createRoute(unit.id)) },
                            onDelete = { deleteTarget = unit }
                        )
                    }
                }
            }
        }
    }

    deleteTarget?.let { u ->
        DeleteDialog(
            title = "Remove Housing",
            message = "Remove \"${u.name}\"?",
            onConfirm = { viewModel.delete(u); deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
private fun HousingCard(unit: HousingEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    val fillRatio = if (unit.capacity > 0) unit.currentCount.toFloat() / unit.capacity else 0f
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(unit.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Type: ${unit.type}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${unit.currentCount} / ${unit.capacity} birds", style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { fillRatio.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
            if (unit.notes.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(unit.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
