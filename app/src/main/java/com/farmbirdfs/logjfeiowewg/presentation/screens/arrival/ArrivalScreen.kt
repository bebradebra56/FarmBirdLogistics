package com.farmbirdfs.logjfeiowewg.presentation.screens.arrival

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.farmbirdfs.logjfeiowewg.data.local.entity.TransportPlanEntity
import com.farmbirdfs.logjfeiowewg.data.local.entity.TransportStatus
import com.farmbirdfs.logjfeiowewg.data.repository.TransportRepository
import com.farmbirdfs.logjfeiowewg.presentation.components.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

class ArrivalViewModel(
    private val transportRepo: TransportRepository,
    private val transportId: Long
) : ViewModel() {
    var transport by mutableStateOf<TransportPlanEntity?>(null)
    var arrivedBirdCount by mutableStateOf("")
    var notes by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var isCompleted by mutableStateOf(false)

    init {
        viewModelScope.launch {
            transport = transportRepo.getById(transportId)
            arrivedBirdCount = transport?.birdCount?.toString() ?: ""
            isCompleted = transport?.status == TransportStatus.COMPLETED
        }
    }

    fun completeArrival(onDone: () -> Unit) = viewModelScope.launch {
        isLoading = true
        transport?.let { t ->
            transportRepo.update(
                t.copy(
                    status = TransportStatus.COMPLETED,
                    birdCount = arrivedBirdCount.toIntOrNull() ?: t.birdCount
                )
            )
            isCompleted = true
        }
        isLoading = false
        onDone()
    }
}

@Composable
fun ArrivalScreen(
    transportId: Long,
    navController: NavController,
    viewModel: ArrivalViewModel = koinViewModel(parameters = { parametersOf(transportId) })
) {
    val transport = viewModel.transport

    Scaffold(
        topBar = { FarmTopBar(title = "Arrival", onBack = { navController.popBackStack() }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            transport?.let { t ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (viewModel.isCompleted)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            if (viewModel.isCompleted) Icons.Filled.CheckCircle else Icons.Filled.LocalShipping,
                            null,
                            modifier = Modifier.size(48.dp),
                            tint = if (viewModel.isCompleted)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            t.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${t.origin} → ${t.destination}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        StatusChip(
                            text = t.status.replace("_", " "),
                            containerColor = statusColor(t.status).first,
                            contentColor = statusColor(t.status).second
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = "Planned Birds",
                        value = "${t.birdCount}",
                        icon = Icons.Filled.Egg,
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    StatCard(
                        label = "Scheduled",
                        value = t.date.formatDate(),
                        icon = Icons.Filled.CalendarToday,
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!viewModel.isCompleted) {
                    HorizontalDivider()

                    Text(
                        "Record Arrival",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = viewModel.arrivedBirdCount,
                        onValueChange = { viewModel.arrivedBirdCount = it.filter { c -> c.isDigit() } },
                        label = { Text("Arrived Bird Count") },
                        leadingIcon = { Icon(Icons.Filled.Egg, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = viewModel.notes,
                        onValueChange = { viewModel.notes = it },
                        label = { Text("Arrival Notes") },
                        leadingIcon = { Icon(Icons.Filled.Notes, null) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Button(
                        onClick = { viewModel.completeArrival { navController.popBackStack() } },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = !viewModel.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Icon(Icons.Filled.FlagCircle, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Confirm Arrival")
                        }
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                            Text(
                                "Transport completed successfully!",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
