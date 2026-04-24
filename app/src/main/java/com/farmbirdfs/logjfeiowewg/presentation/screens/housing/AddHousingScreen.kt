package com.farmbirdfs.logjfeiowewg.presentation.screens.housing

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
import com.farmbirdfs.logjfeiowewg.data.local.entity.HousingEntity
import com.farmbirdfs.logjfeiowewg.data.local.entity.HousingType
import com.farmbirdfs.logjfeiowewg.data.repository.HousingRepository
import com.farmbirdfs.logjfeiowewg.presentation.components.FarmTopBar
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

class AddHousingViewModel(
    private val repository: HousingRepository,
    private val housingId: Long
) : ViewModel() {
    var name by mutableStateOf("")
    var type by mutableStateOf(HousingType.COOP)
    var capacity by mutableStateOf("")
    var currentCount by mutableStateOf("")
    var notes by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var isEdit by mutableStateOf(false)
    var showTypeMenu by mutableStateOf(false)

    init {
        if (housingId > 0) {
            isEdit = true
            viewModelScope.launch {
                repository.getById(housingId)?.let { h ->
                    name = h.name
                    type = h.type
                    capacity = h.capacity.toString()
                    currentCount = if (h.currentCount > 0) h.currentCount.toString() else ""
                    notes = h.notes
                }
            }
        }
    }

    fun isValid() = name.isNotBlank() && (capacity.toIntOrNull() ?: 0) > 0

    fun save(onDone: () -> Unit) = viewModelScope.launch {
        isLoading = true
        val entity = HousingEntity(
            id = if (isEdit) housingId else 0L,
            name = name.trim(),
            type = type,
            capacity = capacity.toIntOrNull() ?: 0,
            currentCount = currentCount.toIntOrNull() ?: 0,
            notes = notes.trim()
        )
        if (isEdit) repository.update(entity) else repository.insert(entity)
        isLoading = false
        onDone()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHousingScreen(
    housingId: Long,
    navController: NavController,
    viewModel: AddHousingViewModel = koinViewModel(parameters = { parametersOf(housingId) })
) {
    Scaffold(
        topBar = {
            FarmTopBar(
                title = if (viewModel.isEdit) "Edit Housing" else "Add Housing",
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.name,
                onValueChange = { viewModel.name = it },
                label = { Text("Housing Name *") },
                leadingIcon = { Icon(Icons.Filled.Home, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = viewModel.showTypeMenu,
                onExpandedChange = { viewModel.showTypeMenu = it }
            ) {
                OutlinedTextField(
                    value = viewModel.type,
                    onValueChange = {},
                    label = { Text("Housing Type") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(viewModel.showTypeMenu) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    leadingIcon = { Icon(Icons.Filled.Category, null) }
                )
                ExposedDropdownMenu(
                    expanded = viewModel.showTypeMenu,
                    onDismissRequest = { viewModel.showTypeMenu = false }
                ) {
                    HousingType.all.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t) },
                            onClick = { viewModel.type = t; viewModel.showTypeMenu = false }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = viewModel.capacity,
                onValueChange = { viewModel.capacity = it.filter { c -> c.isDigit() } },
                label = { Text("Capacity (birds) *") },
                leadingIcon = { Icon(Icons.Filled.Numbers, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = viewModel.currentCount,
                onValueChange = { viewModel.currentCount = it.filter { c -> c.isDigit() } },
                label = { Text("Current Occupancy") },
                leadingIcon = { Icon(Icons.Filled.Egg, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = viewModel.notes,
                onValueChange = { viewModel.notes = it },
                label = { Text("Notes") },
                leadingIcon = { Icon(Icons.Filled.Notes, null) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
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
                    Text(if (viewModel.isEdit) "Save Changes" else "Add Housing")
                }
            }
        }
    }
}
