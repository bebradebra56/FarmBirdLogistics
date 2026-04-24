package com.farmbirdfs.logjfeiowewg.presentation.screens.feeding

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
import com.farmbirdfs.logjfeiowewg.data.local.entity.FeedRecordEntity
import com.farmbirdfs.logjfeiowewg.data.local.entity.FeedType
import com.farmbirdfs.logjfeiowewg.data.repository.BirdGroupRepository
import com.farmbirdfs.logjfeiowewg.data.repository.FeedingRepository
import com.farmbirdfs.logjfeiowewg.presentation.components.FarmTopBar
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

class AddFeedRecordViewModel(
    private val feedingRepo: FeedingRepository,
    private val birdGroupRepo: BirdGroupRepository,
    private val recordId: Long
) : ViewModel() {
    val birdGroups = birdGroupRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var selectedGroupId by mutableStateOf<Long?>(null)
    var feedType by mutableStateOf(FeedType.GRAIN)
    var amount by mutableStateOf("")
    var unit by mutableStateOf("kg")
    var notes by mutableStateOf("")
    var date by mutableStateOf(System.currentTimeMillis())
    var isLoading by mutableStateOf(false)
    var isEdit by mutableStateOf(false)
    var showFeedMenu by mutableStateOf(false)
    var showGroupMenu by mutableStateOf(false)

    init {
        if (recordId > 0) {
            isEdit = true
            viewModelScope.launch {
                feedingRepo.getById(recordId)?.let { r ->
                    selectedGroupId = r.birdGroupId
                    feedType = r.feedType
                    amount = r.amount.toString()
                    unit = r.unit
                    notes = r.notes
                    date = r.date
                }
            }
        }
    }

    fun isValid() = selectedGroupId != null && (amount.toFloatOrNull() ?: 0f) > 0f

    fun save(onDone: () -> Unit) = viewModelScope.launch {
        isLoading = true
        val entity = FeedRecordEntity(
            id = if (isEdit) recordId else 0L,
            birdGroupId = selectedGroupId ?: return@launch,
            feedType = feedType,
            amount = amount.toFloatOrNull() ?: 0f,
            unit = unit,
            notes = notes.trim(),
            date = date
        )
        if (isEdit) feedingRepo.update(entity) else feedingRepo.insert(entity)
        isLoading = false
        onDone()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFeedRecordScreen(
    recordId: Long,
    navController: NavController,
    viewModel: AddFeedRecordViewModel = koinViewModel(parameters = { parametersOf(recordId) })
) {
    val groups by viewModel.birdGroups.collectAsState()
    val selectedGroup = groups.find { it.id == viewModel.selectedGroupId }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState(initialSelectedDateMillis = viewModel.date)

    Scaffold(
        topBar = {
            FarmTopBar(
                title = if (viewModel.isEdit) "Edit Feed Record" else "Feed Record",
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

            ExposedDropdownMenuBox(expanded = viewModel.showFeedMenu, onExpandedChange = { viewModel.showFeedMenu = it }) {
                OutlinedTextField(
                    value = viewModel.feedType,
                    onValueChange = {},
                    label = { Text("Feed Type") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(viewModel.showFeedMenu) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    leadingIcon = { Icon(Icons.Filled.Grain, null) }
                )
                ExposedDropdownMenu(expanded = viewModel.showFeedMenu, onDismissRequest = { viewModel.showFeedMenu = false }) {
                    FeedType.all.forEach { t ->
                        DropdownMenuItem(text = { Text(t) }, onClick = { viewModel.feedType = t; viewModel.showFeedMenu = false })
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = viewModel.amount,
                    onValueChange = { viewModel.amount = it },
                    label = { Text("Amount *") },
                    leadingIcon = { Icon(Icons.Filled.Scale, null) },
                    modifier = Modifier.weight(2f),
                    singleLine = true
                )
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = {},
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = viewModel.unit,
                        onValueChange = {},
                        label = { Text("Unit") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                }
            }

            OutlinedTextField(
                value = viewModel.date.let {
                    java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.ENGLISH).format(java.util.Date(it))
                },
                onValueChange = {},
                label = { Text("Date") },
                readOnly = true,
                leadingIcon = { Icon(Icons.Filled.CalendarToday, null) },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Filled.EditCalendar, null) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.notes,
                onValueChange = { viewModel.notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
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
