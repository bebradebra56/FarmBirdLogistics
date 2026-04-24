package com.farmbirdfs.logjfeiowewg.presentation.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.farmbirdfs.logjfeiowewg.data.local.entity.TaskEntity
import com.farmbirdfs.logjfeiowewg.data.repository.FeedingRepository
import com.farmbirdfs.logjfeiowewg.data.repository.TaskRepository
import com.farmbirdfs.logjfeiowewg.data.repository.TransportRepository
import com.farmbirdfs.logjfeiowewg.navigation.Screen
import com.farmbirdfs.logjfeiowewg.presentation.components.*
import kotlinx.coroutines.flow.*
import java.util.*

class CalendarViewModel(
    private val transportRepo: TransportRepository,
    private val taskRepo: TaskRepository,
    private val feedingRepo: FeedingRepository
) : ViewModel() {
    val transports = transportRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val tasks = taskRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val feedRecords = feedingRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: CalendarViewModel = org.koin.androidx.compose.koinViewModel()
) {
    val transports by viewModel.transports.collectAsState()
    val tasks by viewModel.tasks.collectAsState()

    var selectedYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }

    val transportColor = MaterialTheme.colorScheme.primary
    val taskColor = MaterialTheme.colorScheme.secondary

    val eventsByDay = remember(transports, tasks, selectedYear, selectedMonth) {
        val map = mutableMapOf<Int, MutableList<CalendarEvent>>()
        val cal = Calendar.getInstance()

        transports.forEach { t ->
            cal.timeInMillis = t.date
            if (cal.get(Calendar.YEAR) == selectedYear && cal.get(Calendar.MONTH) == selectedMonth) {
                val day = cal.get(Calendar.DAY_OF_MONTH)
                map.getOrPut(day) { mutableListOf() }.add(
                    CalendarEvent(t.name, "Transport", transportColor)
                )
            }
        }

        tasks.forEach { t ->
            cal.timeInMillis = t.date
            if (cal.get(Calendar.YEAR) == selectedYear && cal.get(Calendar.MONTH) == selectedMonth) {
                val day = cal.get(Calendar.DAY_OF_MONTH)
                map.getOrPut(day) { mutableListOf() }.add(
                    CalendarEvent(t.title, t.category, taskColor)
                )
            }
        }
        map
    }

    val selectedDayEvents: List<CalendarEvent> = selectedDay?.let { eventsByDay[it] } ?: emptyList()

    Scaffold(
        topBar = { FarmTopBar(title = "Calendar") }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
            item {
                MonthHeader(
                    year = selectedYear, month = selectedMonth,
                    onPrev = {
                        if (selectedMonth == 0) { selectedMonth = 11; selectedYear-- }
                        else selectedMonth--
                        selectedDay = null
                    },
                    onNext = {
                        if (selectedMonth == 11) { selectedMonth = 0; selectedYear++ }
                        else selectedMonth++
                        selectedDay = null
                    }
                )
                Spacer(Modifier.height(8.dp))
            }

            item {
                MonthGrid(
                    year = selectedYear,
                    month = selectedMonth,
                    eventDays = eventsByDay.keys,
                    selectedDay = selectedDay,
                    onDayClick = { selectedDay = if (selectedDay == it) null else it }
                )
                Spacer(Modifier.height(16.dp))
            }

            if (selectedDay != null) {
                item {
                    val cal = Calendar.getInstance().apply { set(selectedYear, selectedMonth, selectedDay!!) }
                    Text(
                        "Events on ${cal.timeInMillis.formatDate()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                }

                if (selectedDayEvents.isEmpty()) {
                    item {
                        Text("No events on this day", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    items(selectedDayEvents) { event ->
                        CalendarEventItem(event)
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                SectionHeader("Upcoming Tasks")
            }

            val upcomingTasks = tasks.filter { !it.isCompleted }.take(5)
            if (upcomingTasks.isEmpty()) {
                item {
                    Text("No pending tasks", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                }
            } else {
                items(upcomingTasks) { task ->
                    UpcomingTaskItem(task, onClick = { navController.navigate(Screen.Tasks.route) })
                }
            }
        }
    }
}

data class CalendarEvent(val title: String, val type: String, val color: androidx.compose.ui.graphics.Color)

@Composable
private fun MonthHeader(year: Int, month: Int, onPrev: () -> Unit, onNext: () -> Unit) {
    val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        IconButton(onClick = onPrev) { Icon(Icons.Filled.ChevronLeft, null) }
        Text("${monthNames[month]} $year", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        IconButton(onClick = onNext) { Icon(Icons.Filled.ChevronRight, null) }
    }
}

@Composable
private fun MonthGrid(year: Int, month: Int, eventDays: Set<Int>, selectedDay: Int?, onDayClick: (Int) -> Unit) {
    val cal = Calendar.getInstance().apply { set(year, month, 1) }
    val firstDow = (cal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val today = Calendar.getInstance()
    val isCurrentMonth = today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month

    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            dayNames.forEach { day ->
                Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(4.dp))

        val totalCells = firstDow + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val day = cellIndex - firstDow + 1
                    if (day < 1 || day > daysInMonth) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val isToday = isCurrentMonth && today.get(Calendar.DAY_OF_MONTH) == day
                        val isSelected = selectedDay == day
                        val hasEvent = day in eventDays
                        DayCell(day = day, isToday = isToday, isSelected = isSelected, hasEvent = hasEvent, onClick = { onDayClick(day) }, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(day: Int, isToday: Boolean, isSelected: Boolean, hasEvent: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> androidx.compose.ui.graphics.Color.Transparent
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("$day", style = MaterialTheme.typography.labelMedium, color = textColor, fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal)
        if (hasEvent) {
            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary))
        }
    }
}

@Composable
private fun CalendarEventItem(event: CalendarEvent) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), shape = RoundedCornerShape(10.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(event.color))
            Spacer(Modifier.width(10.dp))
            Column {
                Text(event.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(event.type, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun UpcomingTaskItem(task: TaskEntity, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), shape = RoundedCornerShape(10.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Filled.CheckBox, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.bodyMedium)
                Text(task.date.formatDate(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StatusChip(text = task.category)
        }
    }
}
