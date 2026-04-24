package com.farmbirdfs.logjfeiowewg.presentation.screens.tasks

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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.farmbirdfs.logjfeiowewg.data.local.entity.TaskEntity
import com.farmbirdfs.logjfeiowewg.data.repository.TaskRepository
import com.farmbirdfs.logjfeiowewg.navigation.Screen
import com.farmbirdfs.logjfeiowewg.presentation.components.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

class TasksViewModel(private val repository: TaskRepository) : ViewModel() {
    val tasks = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val pendingCount = repository.getPendingCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    var showCompleted by mutableStateOf(false)
    val filtered: StateFlow<List<TaskEntity>> = combine(tasks, snapshotFlow { showCompleted }) { list, show ->
        if (show) list else list.filter { !it.isCompleted }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleComplete(task: TaskEntity) = viewModelScope.launch {
        repository.setCompleted(task.id, !task.isCompleted)
    }

    fun delete(task: TaskEntity) = viewModelScope.launch { repository.delete(task) }
}

@Composable
fun TasksScreen(
    navController: NavController,
    viewModel: TasksViewModel = koinViewModel()
) {
    val tasks by viewModel.filtered.collectAsState()
    val pending by viewModel.pendingCount.collectAsState()
    var deleteTarget by remember { mutableStateOf<TaskEntity?>(null) }

    Scaffold(
        topBar = { FarmTopBar(title = "Tasks") },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.AddTask.createRoute()) },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Add Task") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("$pending pending tasks", style = MaterialTheme.typography.bodyMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Show done", style = MaterialTheme.typography.labelSmall)
                    Switch(checked = viewModel.showCompleted, onCheckedChange = { viewModel.showCompleted = it })
                }
            }

            if (tasks.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(icon = Icons.Filled.CheckCircle, title = "No tasks", subtitle = "Add tasks to stay on top of your farm work")
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onToggle = { viewModel.toggleComplete(task) },
                            onClick = { navController.navigate(Screen.AddTask.createRoute(task.id)) },
                            onDelete = { deleteTarget = task }
                        )
                    }
                }
            }
        }
    }

    deleteTarget?.let { t ->
        DeleteDialog("Delete Task", "Delete \"${t.title}\"?", onConfirm = { viewModel.delete(t); deleteTarget = null }, onDismiss = { deleteTarget = null })
    }
}

@Composable
private fun TaskCard(task: TaskEntity, onToggle: () -> Unit, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.isCompleted, onCheckedChange = { onToggle() })
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(text = task.category)
                    Text(task.date.formatDate(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) }
        }
    }
}

