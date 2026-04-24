package com.farmbirdfs.logjfeiowewg.presentation.screens.tasks

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
import com.farmbirdfs.logjfeiowewg.data.local.entity.TaskCategory
import com.farmbirdfs.logjfeiowewg.data.local.entity.TaskEntity
import com.farmbirdfs.logjfeiowewg.data.repository.TaskRepository
import com.farmbirdfs.logjfeiowewg.presentation.components.FarmTopBar
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.*

class AddTaskViewModel(
    private val repository: TaskRepository,
    private val taskId: Long
) : ViewModel() {
    var title by mutableStateOf("")
    var category by mutableStateOf(TaskCategory.OTHER)
    var date by mutableStateOf(System.currentTimeMillis())
    var notes by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var isEdit by mutableStateOf(false)
    var showCatMenu by mutableStateOf(false)

    init {
        if (taskId > 0) {
            isEdit = true
            viewModelScope.launch {
                repository.getById(taskId)?.let { t ->
                    title = t.title
                    category = t.category
                    date = t.date
                    notes = t.notes
                }
            }
        }
    }

    fun isValid() = title.isNotBlank()

    fun save(onDone: () -> Unit) = viewModelScope.launch {
        isLoading = true
        val entity = TaskEntity(
            id = if (isEdit) taskId else 0L,
            title = title.trim(),
            category = category,
            date = date,
            notes = notes.trim()
        )
        if (isEdit) repository.update(entity) else repository.insert(entity)
        isLoading = false
        onDone()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    taskId: Long,
    navController: NavController,
    viewModel: AddTaskViewModel = koinViewModel(parameters = { parametersOf(taskId) })
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState(initialSelectedDateMillis = viewModel.date)

    Scaffold(
        topBar = {
            FarmTopBar(
                title = if (viewModel.isEdit) "Edit Task" else "Add Task",
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
                value = viewModel.title,
                onValueChange = { viewModel.title = it },
                label = { Text("Task *") },
                leadingIcon = { Icon(Icons.Filled.CheckCircle, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenuBox(expanded = viewModel.showCatMenu, onExpandedChange = { viewModel.showCatMenu = it }) {
                OutlinedTextField(
                    value = viewModel.category,
                    onValueChange = {},
                    label = { Text("Category") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(viewModel.showCatMenu) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    leadingIcon = { Icon(Icons.Filled.Category, null) }
                )
                ExposedDropdownMenu(expanded = viewModel.showCatMenu, onDismissRequest = { viewModel.showCatMenu = false }) {
                    TaskCategory.all.forEach { c ->
                        DropdownMenuItem(text = { Text(c) }, onClick = { viewModel.category = c; viewModel.showCatMenu = false })
                    }
                }
            }

            OutlinedTextField(
                value = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date(viewModel.date)),
                onValueChange = {},
                label = { Text("Due Date") },
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
                    Text(if (viewModel.isEdit) "Save Changes" else "Add Task")
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
