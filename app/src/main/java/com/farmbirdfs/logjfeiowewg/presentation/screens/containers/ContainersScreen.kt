package com.farmbirdfs.logjfeiowewg.presentation.screens.containers

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
import com.farmbirdfs.logjfeiowewg.data.local.entity.ContainerEntity
import com.farmbirdfs.logjfeiowewg.data.repository.ContainerRepository
import com.farmbirdfs.logjfeiowewg.navigation.Screen
import com.farmbirdfs.logjfeiowewg.presentation.components.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

class ContainersViewModel(private val repository: ContainerRepository) : ViewModel() {
    val containers = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalCount = repository.getTotalCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun delete(entity: ContainerEntity) = viewModelScope.launch { repository.delete(entity) }
}

@Composable
fun ContainersScreen(
    navController: NavController,
    viewModel: ContainersViewModel = koinViewModel()
) {
    val containers by viewModel.containers.collectAsState()
    val total by viewModel.totalCount.collectAsState()
    var deleteTarget by remember { mutableStateOf<ContainerEntity?>(null) }

    Scaffold(
        topBar = { FarmTopBar(title = "Containers") },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.AddContainer.createRoute()) },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Add Container") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "Total Containers",
                    value = "$total",
                    icon = Icons.Filled.Inventory2,
                    modifier = Modifier.weight(1f)
                )
                val totalBirds = containers.sumOf { it.currentBirdCount }
                StatCard(
                    label = "Birds Loaded",
                    value = "$totalBirds",
                    icon = Icons.Filled.Egg,
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            if (containers.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Filled.Inventory2,
                        title = "No containers",
                        subtitle = "Add containers to manage bird transport"
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(containers, key = { it.id }) { container ->
                        ContainerCard(
                            container = container,
                            onClick = { navController.navigate(Screen.AddContainer.createRoute(container.id)) },
                            onDelete = { deleteTarget = container }
                        )
                    }
                }
            }
        }
    }

    deleteTarget?.let { c ->
        DeleteDialog(
            title = "Remove Container",
            message = "Remove \"${c.name}\"?",
            onConfirm = { viewModel.delete(c); deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
private fun ContainerCard(
    container: ContainerEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val fillRatio = if (container.capacity > 0) container.currentBirdCount.toFloat() / container.capacity else 0f
    val fillColor = when {
        fillRatio > 0.9f -> MaterialTheme.colorScheme.error
        fillRatio > 0.7f -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }

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
                    Text(container.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Type: ${container.type}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Egg, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${container.currentBirdCount} / ${container.capacity}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${(fillRatio * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = fillColor
                    )
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { fillRatio.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = fillColor,
                trackColor = fillColor.copy(alpha = 0.2f)
            )
        }
    }
}
