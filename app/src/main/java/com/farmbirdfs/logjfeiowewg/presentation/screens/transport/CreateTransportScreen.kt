package com.farmbirdfs.logjfeiowewg.presentation.screens.transport

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
import com.farmbirdfs.logjfeiowewg.data.local.entity.TransportPlanEntity
import com.farmbirdfs.logjfeiowewg.data.local.entity.TransportStatus
import com.farmbirdfs.logjfeiowewg.data.repository.TransportRepository
import com.farmbirdfs.logjfeiowewg.presentation.components.FarmTopBar
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.*

class CreateTransportViewModel(
    private val repository: TransportRepository,
    private val transportId: Long
) : ViewModel() {
    var name by mutableStateOf("")
    var date by mutableStateOf(System.currentTimeMillis())
    var origin by mutableStateOf("")
    var destination by mutableStateOf("")
    var birdCount by mutableStateOf("")
    var notes by mutableStateOf("")
    var status by mutableStateOf(TransportStatus.PLANNED)
    var isLoading by mutableStateOf(false)
    var isEdit by mutableStateOf(false)

    init {
        if (transportId > 0) {
            isEdit = true
            viewModelScope.launch {
                repository.getById(transportId)?.let { t ->
                    name = t.name
                    date = t.date
                    origin = t.origin
                    destination = t.destination
                    birdCount = if (t.birdCount > 0) t.birdCount.toString() else ""
                    notes = t.notes
                    status = t.status
                }
            }
        }
    }

    fun isValid() = name.isNotBlank() && origin.isNotBlank() && destination.isNotBlank()

    fun save(onDone: () -> Unit) = viewModelScope.launch {
        isLoading = true
        val entity = TransportPlanEntity(
            id = if (isEdit) transportId else 0L,
            name = name.trim(),
            date = date,
            origin = origin.trim(),
            destination = destination.trim(),
            birdCount = birdCount.toIntOrNull() ?: 0,
            notes = notes.trim(),
            status = status
        )
        if (isEdit) repository.update(entity) else repository.insert(entity)
        isLoading = false
        onDone()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTransportScreen(
    transportId: Long,
    navController: NavController,
    viewModel: CreateTransportViewModel = koinViewModel(parameters = { parametersOf(transportId) })
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            FarmTopBar(
                title = if (viewModel.isEdit) "Edit Transport" else "New Transport",
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
                label = { Text("Transport Name *") },
                leadingIcon = { Icon(Icons.Filled.LocalShipping, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = viewModel.origin,
                onValueChange = { viewModel.origin = it },
                label = { Text("Origin *") },
                leadingIcon = { Icon(Icons.Filled.MyLocation, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = viewModel.destination,
                onValueChange = { viewModel.destination = it },
                label = { Text("Destination *") },
                leadingIcon = { Icon(Icons.Filled.LocationOn, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = dateFormatter.format(Date(viewModel.date)),
                onValueChange = {},
                label = { Text("Date") },
                leadingIcon = { Icon(Icons.Filled.CalendarToday, null) },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Filled.EditCalendar, null)
                    }
                }
            )

            OutlinedTextField(
                value = viewModel.birdCount,
                onValueChange = { viewModel.birdCount = it.filter { c -> c.isDigit() } },
                label = { Text("Bird Count") },
                leadingIcon = { Icon(Icons.Filled.Egg, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (viewModel.isEdit) {
                ExposedDropdownMenuBox(
                    expanded = showStatusMenu,
                    onExpandedChange = { showStatusMenu = it }
                ) {
                    OutlinedTextField(
                        value = viewModel.status.replace("_", " "),
                        onValueChange = {},
                        label = { Text("Status") },
                        leadingIcon = { Icon(Icons.Filled.Flag, null) },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showStatusMenu) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false }
                    ) {
                        listOf(
                            TransportStatus.PLANNED,
                            TransportStatus.IN_PROGRESS,
                            TransportStatus.COMPLETED,
                            TransportStatus.CANCELLED
                        ).forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.replace("_", " ")) },
                                onClick = { viewModel.status = s; showStatusMenu = false }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = viewModel.notes,
                onValueChange = { viewModel.notes = it },
                label = { Text("Notes") },
                leadingIcon = { Icon(Icons.Filled.Notes, null) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
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
                    Text(if (viewModel.isEdit) "Save Changes" else "Create Transport")
                }
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = viewModel.date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { viewModel.date = it }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = state) }
    }
}
