package com.farmbirdfs.logjfeiowewg.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.farmbirdfs.logjfeiowewg.data.preferences.UserPreferences
import com.farmbirdfs.logjfeiowewg.presentation.components.FarmTopBar
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

class ProfileViewModel(private val preferences: UserPreferences) : ViewModel() {
    val userName = preferences.userName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val farmName = preferences.farmName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    var editName by mutableStateOf("")
    var editFarm by mutableStateOf("")
    var isEditing by mutableStateOf(false)
    var isSaving by mutableStateOf(false)

    fun startEdit(name: String, farm: String) {
        editName = name; editFarm = farm; isEditing = true
    }

    fun save(onDone: () -> Unit) = viewModelScope.launch {
        isSaving = true
        preferences.setUserName(editName.trim())
        preferences.setFarmName(editFarm.trim())
        isSaving = false
        isEditing = false
        onDone()
    }
}

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val userName by viewModel.userName.collectAsState()
    val farmName by viewModel.farmName.collectAsState()

    LaunchedEffect(Unit) {
        if (!viewModel.isEditing) {
            viewModel.editName = userName
            viewModel.editFarm = farmName
        }
    }

    Scaffold(
        topBar = { FarmTopBar(title = "Profile", onBack = { navController.popBackStack() }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (userName.isNotEmpty()) userName.first().uppercaseChar().toString() else "?",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (!viewModel.isEditing) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (userName.isNotEmpty()) userName else "Your Name",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (farmName.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Agriculture, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.width(4.dp))
                            Text(farmName, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }

                Button(
                    onClick = { viewModel.startEdit(userName, farmName) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Edit, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Edit Profile")
                }
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Edit Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = viewModel.editName,
                            onValueChange = { viewModel.editName = it },
                            label = { Text("Your Name") },
                            leadingIcon = { Icon(Icons.Filled.Person, null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = viewModel.editFarm,
                            onValueChange = { viewModel.editFarm = it },
                            label = { Text("Farm Name") },
                            leadingIcon = { Icon(Icons.Filled.Agriculture, null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { viewModel.isEditing = false }, modifier = Modifier.weight(1f)) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = { viewModel.save {} },
                                modifier = Modifier.weight(1f),
                                enabled = !viewModel.isSaving
                            ) {
                                if (viewModel.isSaving) {
                                    CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                                } else {
                                    Text("Save")
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            Text("App Info", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.Start))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoRow(Icons.Filled.Apps, "App", "Farm Bird Logistics")
                    InfoRow(Icons.Filled.Info, "Version", "1.0.0")
                    InfoRow(Icons.Filled.Agriculture, "Purpose", "Poultry transport & management")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
