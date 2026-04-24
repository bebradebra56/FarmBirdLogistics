package com.farmbirdfs.logjfeiowewg.presentation.screens.health

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
import com.farmbirdfs.logjfeiowewg.data.local.entity.HealthCondition
import com.farmbirdfs.logjfeiowewg.data.local.entity.HealthRecordEntity
import com.farmbirdfs.logjfeiowewg.data.repository.BirdGroupRepository
import com.farmbirdfs.logjfeiowewg.data.repository.HealthRepository
import com.farmbirdfs.logjfeiowewg.presentation.components.FarmTopBar
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.*

class AddHealthRecordViewModel(
    private val healthRepo: HealthRepository,
    private val birdGroupRepo: BirdGroupRepository,
    private val recordId: Long
) : ViewModel() {
    val birdGroups = birdGroupRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var selectedGroupId by mutableStateOf<Long?>(null)
    var condition by mutableStateOf(HealthCondition.HEALTHY)
    var notes by mutableStateOf("")
    var treatment by mutableStateOf("")
    var mortality by mutableStateOf("")
    var date by mutableStateOf(System.currentTimeMillis())
    var isLoading by mutableStateOf(false)
    var isEdit by mutableStateOf(false)
    var showCondMenu by mutableStateOf(false)
    var showGroupMenu by mutableStateOf(false)

    init {
        if (recordId > 0) {
            isEdit = true
            viewModelScope.launch {
                healthRepo.getById(recordId)?.let { r ->
                    selectedGroupId = r.birdGroupId
                    condition = r.condition
                    notes = r.notes
                    treatment = r.treatment
                    mortality = if (r.mortality > 0) r.mortality.toString() else ""
                    date = r.date
                }
            }
        }
    }

    fun isValid() = selectedGroupId != null

    fun save(onDone: () -> Unit) = viewModelScope.launch {
        isLoading = true
        val entity = HealthRecordEntity(
            id = if (isEdit) recordId else 0L,
            birdGroupId = selectedGroupId ?: return@launch,
            condition = condition,
            notes = notes.trim(),
            treatment = treatment.trim(),
            mortality = mortality.toIntOrNull() ?: 0,
            date = date
        )
        if (isEdit) healthRepo.update(entity) else healthRepo.insert(entity)
        isLoading = false
        onDone()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHealthRecordScreen(
    recordId: Long,
    navController: NavController,
    viewModel: AddHealthRecordViewModel = koinViewModel(parameters = { parametersOf(recordId) })
) {
    val groups by viewModel.birdGroups.collectAsState()
    val selectedGroup = groups.find { it.id == viewModel.selectedGroupId }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState(initialSelectedDateMillis = viewModel.date)

    Scaffold(
        topBar = {
            FarmTopBar(
                title = if (viewModel.isEdit) "Edit Health Record" else "Health Record",
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
            ExposedDropdownMenuBox(expanded = viewModel.showGroupMenu, onExpandedChange = { viewModel.showGroupMenu = it }) {
                OutlinedTextField(
                    value = selectedGroup?.let { "${it.birdType} (${it.count} birds)" } ?: "Select bird group",
                    onValueChange = {},
                    label = { Text("Bird Group *") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(viewModel.showGroupMenu) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    leadingIcon = { Icon(Icons.Filled.Pets, null) }
                )
                ExposedDropdownMenu(expanded = viewModel.showGroupMenu, onDismissRequest = { viewModel.showGroupMenu = false }) {
                    groups.forEach { g ->
                        DropdownMenuItem(
                            text = { Text("${g.birdType} — ${g.count} birds") },
                            onClick = { viewModel.selectedGroupId = g.id; viewModel.showGroupMenu = false }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(expanded = viewModel.showCondMenu, onExpandedChange = { viewModel.showCondMenu = it }) {
                OutlinedTextField(
                    value = viewModel.condition,
                    onValueChange = {},
                    label = { Text("Condition") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(viewModel.showCondMenu) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    leadingIcon = { Icon(Icons.Filled.HealthAndSafety, null) }
                )
                ExposedDropdownMenu(expanded = viewModel.showCondMenu, onDismissRequest = { viewModel.showCondMenu = false }) {
                    HealthCondition.all.forEach { c ->
                        DropdownMenuItem(text = { Text(c) }, onClick = { viewModel.condition = c; viewModel.showCondMenu = false })
                    }
                }
            }

            OutlinedTextField(
                value = viewModel.mortality,
                onValueChange = { viewModel.mortality = it.filter { c -> c.isDigit() } },
                label = { Text("Mortality (if any)") },
                leadingIcon = { Icon(Icons.Filled.Warning, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = viewModel.treatment,
                onValueChange = { viewModel.treatment = it },
                label = { Text("Treatment") },
                leadingIcon = { Icon(Icons.Filled.MedicalServices, null) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.notes,
                onValueChange = { viewModel.notes = it },
                label = { Text("Notes") },
                leadingIcon = { Icon(Icons.Filled.Notes, null) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date(viewModel.date)),
                onValueChange = {},
                label = { Text("Date") },
                readOnly = true,
                leadingIcon = { Icon(Icons.Filled.CalendarToday, null) },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Filled.EditCalendar, null) }
                },
                modifier = Modifier.fillMaxWidth()
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
                    Text(if (viewModel.isEdit) "Save Changes" else "Save Record")
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { viewModel.date = it }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = dateState) }
    }
}
