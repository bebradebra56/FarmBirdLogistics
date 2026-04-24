package com.farmbirdfs.logjfeiowewg.presentation.screens.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.farmbirdfs.logjfeiowewg.data.local.entity.HealthCondition
import com.farmbirdfs.logjfeiowewg.data.local.entity.TransportStatus
import com.farmbirdfs.logjfeiowewg.data.repository.*
import com.farmbirdfs.logjfeiowewg.presentation.components.*
import kotlinx.coroutines.flow.*
import java.util.*

class ReportsViewModel(
    private val transportRepo: TransportRepository,
    private val birdGroupRepo: BirdGroupRepository,
    private val healthRepo: HealthRepository,
    private val feedingRepo: FeedingRepository
) : ViewModel() {
    val transports = transportRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val birdGroups = birdGroupRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val healthRecords = healthRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalBirds = birdGroupRepo.getTotalBirdCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val totalMortality = healthRepo.getTotalMortality()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}

@Composable
fun ReportsScreen(
    navController: NavController,
    viewModel: ReportsViewModel = org.koin.androidx.compose.koinViewModel()
) {
    val transports by viewModel.transports.collectAsState()
    val groups by viewModel.birdGroups.collectAsState()
    val healthRecords by viewModel.healthRecords.collectAsState()
    val totalBirds by viewModel.totalBirds.collectAsState()
    val totalMortality by viewModel.totalMortality.collectAsState()

    val completed = transports.count { it.status == TransportStatus.COMPLETED }
    val planned = transports.count { it.status == TransportStatus.PLANNED }
    val inProgress = transports.count { it.status == TransportStatus.IN_PROGRESS }
    val totalTransportedBirds = transports.filter { it.status == TransportStatus.COMPLETED }.sumOf { it.birdCount }

    val mortalityRate = if ((totalBirds ?: 0) > 0)
        ((totalMortality ?: 0).toFloat() / (totalBirds ?: 1)) * 100f else 0f

    val conditionBreakdown = healthRecords.groupBy { it.condition }.mapValues { it.value.size }
    val healthyCount = conditionBreakdown[HealthCondition.HEALTHY] ?: 0
    val sickCount = conditionBreakdown.filterKeys { it != HealthCondition.HEALTHY }.values.sum()

    val transportsByMonth = transports.groupBy { t ->
        Calendar.getInstance().apply { timeInMillis = t.date }.get(Calendar.MONTH)
    }.mapValues { it.value.size }

    Scaffold(
        topBar = { FarmTopBar(title = "Reports", onBack = { navController.popBackStack() }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeader("Transport Summary")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Total", "${transports.size}", Icons.Filled.LocalShipping, modifier = Modifier.weight(1f))
                    StatCard("Completed", "$completed", Icons.Filled.CheckCircle, modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    StatCard("Active", "$inProgress", Icons.Filled.PlayArrow, modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }

            item {
                Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Birds Transported", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column {
                                Text("Total Transported", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$totalTransportedBirds", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Current Flock", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${totalBirds ?: 0}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                SectionHeader("Health Overview")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(
                        "Mortality Rate",
                        "${String.format("%.1f", mortalityRate)}%",
                        Icons.Filled.Warning,
                        modifier = Modifier.weight(1f),
                        containerColor = if (mortalityRate > 5f) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (mortalityRate > 5f) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    StatCard(
                        "Total Deaths",
                        "${totalMortality ?: 0}",
                        Icons.Filled.Cancel,
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (conditionBreakdown.isNotEmpty()) {
                item {
                    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Condition Breakdown", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            if (healthRecords.isNotEmpty()) {
                                HealthPieChart(healthyCount = healthyCount, sickCount = sickCount)
                            }
                            Spacer(Modifier.height(8.dp))
                            conditionBreakdown.entries.sortedByDescending { it.value }.forEach { (cond, count) ->
                                val (bg, fg) = statusColor(cond)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(modifier = Modifier.size(10.dp).background(fg, androidx.compose.foundation.shape.CircleShape))
                                        Text(cond, style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text("$count records", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            if (transportsByMonth.isNotEmpty()) {
                item {
                    SectionHeader("Transports by Month")
                    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            TransportBarChart(data = transportsByMonth)
                        }
                    }
                }
            }

            item {
                SectionHeader("Bird Groups")
                Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val byType = groups.groupBy { it.birdType }
                        byType.forEach { (type, groupList) ->
                            val count = groupList.sumOf { it.count }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(type, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text("$count birds (${groupList.size} groups)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            LinearProgressIndicator(
                                progress = { (count.toFloat() / (totalBirds ?: 1).coerceAtLeast(1)).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (groups.isEmpty()) {
                            Text("No bird groups recorded", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthPieChart(healthyCount: Int, sickCount: Int) {
    val total = (healthyCount + sickCount).coerceAtLeast(1)
    val healthyAngle = (healthyCount.toFloat() / total) * 360f
    val primary = MaterialTheme.colorScheme.primary
    val error = MaterialTheme.colorScheme.error
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(120.dp)) {
            drawArc(color = primary, startAngle = -90f, sweepAngle = healthyAngle, useCenter = true, size = Size(size.width, size.height))
            drawArc(color = error, startAngle = -90f + healthyAngle, sweepAngle = 360f - healthyAngle, useCenter = true, size = Size(size.width, size.height))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$healthyCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            Text("healthy", style = MaterialTheme.typography.labelSmall, color = Color.White)
        }
    }
}

@Composable
private fun TransportBarChart(data: Map<Int, Int>) {
    val monthNames = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    val maxVal = data.values.maxOrNull() ?: 1
    val primary = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier.fillMaxWidth().height(120.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        (0..11).forEach { month ->
            val value = data[month] ?: 0
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                if (value > 0) {
                    Text("$value", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height((value.toFloat() / maxVal * 80).coerceAtLeast(4f).dp)
                        .background(
                            color = if (value > 0) primary else primary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                        )
                )
                Spacer(Modifier.height(4.dp))
                Text(monthNames[month], style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
