package com.farmbirdfs.logjfeiowewg.presentation.screens.loading

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.farmbirdfs.logjfeiowewg.data.local.entity.LoadingRecordEntity
import com.farmbirdfs.logjfeiowewg.data.repository.BirdGroupRepository
import com.farmbirdfs.logjfeiowewg.data.repository.ContainerRepository
import com.farmbirdfs.logjfeiowewg.data.repository.LoadingRepository
import com.farmbirdfs.logjfeiowewg.presentation.components.FarmTopBar
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

class LoadingDetailsViewModel(
    private val loadingRepo: LoadingRepository,
    private val birdGroupRepo: BirdGroupRepository,
    private val containerRepo: ContainerRepository,
    private val transportId: Long
) : ViewModel() {
    val birdGroups = birdGroupRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val containers = containerRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var selectedGroupId by mutableStateOf<Long?>(null)
    var selectedContainerId by mutableStateOf<Long?>(null)
    var count by mutableStateOf("")
    var isLoading by mutableStateOf(false)

    fun isValid() = selectedGroupId != null && selectedContainerId != null && (count.toIntOrNull() ?: 0) > 0

    fun save(onDone: () -> Unit) = viewModelScope.launch {
        isLoading = true
        val c = count.toIntOrNull() ?: 0
        val gId = selectedGroupId ?: return@launch
        val cId = selectedContainerId ?: return@launch
        loadingRepo.insert(
            LoadingRecordEntity(
                transportPlanId = transportId,
                birdGroupId = gId,
                containerId = cId,
                count = c
            )
        )
        containerRepo.addBirds(cId, c)
        isLoading = false
        onDone()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadingDetailsScreen(
    transportId: Long,
    navController: NavController,
    viewModel: LoadingDetailsViewModel = koinViewModel(parameters = { parametersOf(transportId) })
) {
    val birdGroups by viewModel.birdGroups.collectAsState()
    val containers by viewModel.containers.collectAsState()
    var groupExpanded by remember { mutableStateOf(false) }
    var containerExpanded by remember { mutableStateOf(false) }

    val selectedGroup = birdGroups.find { it.id == viewModel.selectedGroupId }
    val selectedContainer = containers.find { it.id == viewModel.selectedContainerId }

    Scaffold(
        topBar = { FarmTopBar(title = "Add Loading Record", onBack = { navController.popBackStack() }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Select Bird Group", style = MaterialTheme.typography.titleSmall)
            ExposedDropdownMenuBox(expanded = groupExpanded, onExpandedChange = { groupExpanded = it }) {
                OutlinedTextField(
                    value = selectedGroup?.let { "${it.birdType} - ${it.count} birds (Age: ${it.age})" } ?: "Select group",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(groupExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    leadingIcon = { Icon(Icons.Filled.Pets, null) }
                )
                ExposedDropdownMenu(expanded = groupExpanded, onDismissRequest = { groupExpanded = false }) {
                    birdGroups.forEach { group ->
                        DropdownMenuItem(
                            text = { Text("${group.birdType} - ${group.count} birds (Age: ${group.age})") },
                            onClick = { viewModel.selectedGroupId = group.id; groupExpanded = false }
                        )
                    }
                }
            }

            Text("Select Container", style = MaterialTheme.typography.titleSmall)
            ExposedDropdownMenuBox(expanded = containerExpanded, onExpandedChange = { containerExpanded = it }) {
                OutlinedTextField(
                    value = selectedContainer?.let { "${it.name} (${it.currentBirdCount}/${it.capacity})" } ?: "Select container",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(containerExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    leadingIcon = { Icon(Icons.Filled.Inventory2, null) }
                )
                ExposedDropdownMenu(expanded = containerExpanded, onDismissRequest = { containerExpanded = false }) {
                    containers.forEach { container ->
                        val available = container.capacity - container.currentBirdCount
                        DropdownMenuItem(
                            text = { Text("${container.name} — Available: $available") },
                            onClick = { viewModel.selectedContainerId = container.id; containerExpanded = false },
                            enabled = available > 0
                        )
                    }
                }
            }

            selectedContainer?.let { c ->
                val progress = if (c.capacity > 0) c.currentBirdCount.toFloat() / c.capacity else 0f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (progress > 0.9f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Text(
                    "${c.currentBirdCount}/${c.capacity} capacity used",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedTextField(
                value = viewModel.count,
                onValueChange = { viewModel.count = it.filter { c -> c.isDigit() } },
                label = { Text("Bird Count") },
                leadingIcon = { Icon(Icons.Filled.Numbers, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = { viewModel.save { navController.popBackStack() } },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = viewModel.isValid() && !viewModel.isLoading
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(Icons.Filled.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save Loading Record")
                }
            }
        }
    }
}
