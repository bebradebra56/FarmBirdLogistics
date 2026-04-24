package com.farmbirdfs.logjfeiowewg.presentation.screens.birds

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.farmbirdfs.logjfeiowewg.data.local.entity.BirdGroupEntity
import com.farmbirdfs.logjfeiowewg.data.local.entity.BirdType
import com.farmbirdfs.logjfeiowewg.data.repository.BirdGroupRepository
import com.farmbirdfs.logjfeiowewg.navigation.Screen
import com.farmbirdfs.logjfeiowewg.presentation.components.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

class BirdGroupsViewModel(private val repository: BirdGroupRepository) : ViewModel() {
    val groups = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalBirds = repository.getTotalBirdCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    var filterType by mutableStateOf<String?>(null)

    val filtered: StateFlow<List<BirdGroupEntity>> = combine(groups, snapshotFlow { filterType }) { list, type ->
        if (type == null) list else list.filter { it.birdType == type }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(entity: BirdGroupEntity) = viewModelScope.launch { repository.delete(entity) }
}

@Composable
fun BirdGroupsScreen(
    navController: NavController,
    viewModel: BirdGroupsViewModel = koinViewModel()
) {
    val groups by viewModel.filtered.collectAsState()
    val total by viewModel.totalBirds.collectAsState()
    var deleteTarget by remember { mutableStateOf<BirdGroupEntity?>(null) }

    Scaffold(
        topBar = { FarmTopBar(title = "Bird Groups") },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.AddBirdGroup.createRoute()) },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Add Group") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Filled.Pets, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(32.dp))
                    Column {
                        Text("Total Birds", style = MaterialTheme.typography.labelMedium)
                        Text("${total ?: 0}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = viewModel.filterType == null,
                        onClick = { viewModel.filterType = null },
                        label = { Text("All") }
                    )
                }
                items(BirdType.all) { type ->
                    FilterChip(
                        selected = viewModel.filterType == type,
                        onClick = { viewModel.filterType = if (viewModel.filterType == type) null else type },
                        label = { Text(type) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            if (groups.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Filled.Pets,
                        title = "No bird groups",
                        subtitle = "Add bird groups to track your flock"
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(groups, key = { it.id }) { group ->
                        BirdGroupCard(
                            group = group,
                            onClick = { navController.navigate(Screen.AddBirdGroup.createRoute(group.id)) },
                            onDelete = { deleteTarget = group }
                        )
                    }
                }
            }
        }
    }

    deleteTarget?.let { g ->
        DeleteDialog(
            title = "Remove Bird Group",
            message = "Remove ${g.count} ${g.birdType}s? This will also remove related feeding and health records.",
            onConfirm = { viewModel.delete(g); deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
private fun BirdGroupCard(
    group: BirdGroupEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val bgColor = when (group.birdType) {
        BirdType.CHICKEN -> MaterialTheme.colorScheme.primaryContainer
        BirdType.DUCK -> MaterialTheme.colorScheme.secondaryContainer
        BirdType.GOOSE -> MaterialTheme.colorScheme.tertiaryContainer
        BirdType.QUAIL -> MaterialTheme.colorScheme.surfaceVariant
        BirdType.TURKEY -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val icon = when (group.birdType) {
        BirdType.CHICKEN -> Icons.Filled.Egg
        BirdType.DUCK -> Icons.Filled.Water
        BirdType.GOOSE -> Icons.Filled.Air
        BirdType.QUAIL -> Icons.Filled.Grain
        else -> Icons.Filled.Pets
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, modifier = Modifier.size(28.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(group.birdType, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "${group.count} birds · Age: ${group.age}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (group.notes.isNotEmpty()) {
                    Text(
                        group.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
        }
    }
}
