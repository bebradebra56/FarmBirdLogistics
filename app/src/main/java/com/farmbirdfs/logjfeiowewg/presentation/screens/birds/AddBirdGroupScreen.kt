package com.farmbirdfs.logjfeiowewg.presentation.screens.birds

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
import com.farmbirdfs.logjfeiowewg.data.local.entity.BirdGroupEntity
import com.farmbirdfs.logjfeiowewg.data.local.entity.BirdType
import com.farmbirdfs.logjfeiowewg.data.repository.BirdGroupRepository
import com.farmbirdfs.logjfeiowewg.presentation.components.FarmTopBar
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

class AddBirdGroupViewModel(
    private val repository: BirdGroupRepository,
    private val groupId: Long
) : ViewModel() {
    var birdType by mutableStateOf(BirdType.CHICKEN)
    var count by mutableStateOf("")
    var age by mutableStateOf("")
    var notes by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var isEdit by mutableStateOf(false)
    var showTypeMenu by mutableStateOf(false)

    init {
        if (groupId > 0) {
            isEdit = true
            viewModelScope.launch {
                repository.getById(groupId)?.let { g ->
                    birdType = g.birdType
                    count = g.count.toString()
                    age = g.age
                    notes = g.notes
                }
            }
        }
    }

    fun isValid() = count.toIntOrNull() != null && count.toInt() > 0 && age.isNotBlank()

    fun save(onDone: () -> Unit) = viewModelScope.launch {
        isLoading = true
        val entity = BirdGroupEntity(
            id = if (isEdit) groupId else 0L,
            birdType = birdType,
            count = count.toIntOrNull() ?: 0,
            age = age.trim(),
            notes = notes.trim()
        )
        if (isEdit) repository.update(entity) else repository.insert(entity)
        isLoading = false
        onDone()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBirdGroupScreen(
    groupId: Long,
    navController: NavController,
    viewModel: AddBirdGroupViewModel = koinViewModel(parameters = { parametersOf(groupId) })
) {
    Scaffold(
        topBar = {
            FarmTopBar(
                title = if (viewModel.isEdit) "Edit Bird Group" else "Add Bird Group",
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
            ExposedDropdownMenuBox(
                expanded = viewModel.showTypeMenu,
                onExpandedChange = { viewModel.showTypeMenu = it }
            ) {
                OutlinedTextField(
                    value = viewModel.birdType,
                    onValueChange = {},
                    label = { Text("Bird Type *") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(viewModel.showTypeMenu) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    leadingIcon = { Icon(Icons.Filled.Pets, null) }
                )
                ExposedDropdownMenu(
                    expanded = viewModel.showTypeMenu,
                    onDismissRequest = { viewModel.showTypeMenu = false }
                ) {
                    BirdType.all.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = { viewModel.birdType = type; viewModel.showTypeMenu = false }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = viewModel.count,
                onValueChange = { viewModel.count = it.filter { c -> c.isDigit() } },
                label = { Text("Count *") },
                leadingIcon = { Icon(Icons.Filled.Numbers, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = viewModel.age,
                onValueChange = { viewModel.age = it },
                label = { Text("Age (e.g., 4 weeks, 3 months) *") },
                leadingIcon = { Icon(Icons.Filled.ChildCare, null) },
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
                    Text(if (viewModel.isEdit) "Save Changes" else "Add Bird Group")
                }
            }
        }
    }
}
