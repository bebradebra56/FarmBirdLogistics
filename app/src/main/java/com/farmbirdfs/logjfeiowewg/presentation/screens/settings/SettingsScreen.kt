package com.farmbirdfs.logjfeiowewg.presentation.screens.settings

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.farmbirdfs.logjfeiowewg.data.preferences.UserPreferences
import com.farmbirdfs.logjfeiowewg.presentation.components.FarmTopBar
import com.farmbirdfs.logjfeiowewg.presentation.components.SectionHeader
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

class SettingsViewModel(private val preferences: UserPreferences) : ViewModel() {
    val useMetric = preferences.useMetric.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val notificationsEnabled = preferences.notificationsEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val tempUnit = preferences.tempUnit.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "°C")
    val weightUnit = preferences.weightUnit.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "kg")

    fun setUseMetric(value: Boolean) = viewModelScope.launch { preferences.setUseMetric(value) }
    fun setNotifications(value: Boolean) = viewModelScope.launch { preferences.setNotificationsEnabled(value) }
}

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val useMetric by viewModel.useMetric.collectAsState()
    val notifications by viewModel.notificationsEnabled.collectAsState()
    val tempUnit by viewModel.tempUnit.collectAsState()
    val weightUnit by viewModel.weightUnit.collectAsState()

    Scaffold(
        topBar = { FarmTopBar(title = "Settings", onBack = { navController.popBackStack() }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SectionHeader("Units")
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    SettingsToggleRow(
                        icon = Icons.Filled.Straighten,
                        title = "Use Metric Units",
                        subtitle = "Temperature in ${if (useMetric) "°C" else "°F"}, weight in ${if (useMetric) "kg" else "lb"}",
                        checked = useMetric,
                        onCheckedChange = { viewModel.setUseMetric(it) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsInfoRow(Icons.Filled.Thermostat, "Temperature Unit", tempUnit)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsInfoRow(Icons.Filled.Scale, "Weight Unit", weightUnit)
                }
            }

            Spacer(Modifier.height(8.dp))
            SectionHeader("Notifications")
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    SettingsToggleRow(
                        icon = Icons.Filled.Notifications,
                        title = "Enable Notifications",
                        subtitle = "Get reminders for tasks and health checks",
                        checked = notifications,
                        onCheckedChange = { viewModel.setNotifications(it) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            SectionHeader("About")
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    SettingsInfoRow(Icons.Filled.Apps, "App Name", "Farm Bird Logistics")
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsInfoRow(Icons.Filled.Info, "Version", "1.0.0")
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsInfoRow(Icons.Filled.Agriculture, "Category", "Poultry Management")
                }
            }

            Spacer(Modifier.height(8.dp))
            SectionHeader("Data")
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    SettingsInfoRow(Icons.Filled.Storage, "Storage", "Local Database (Room)")
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsInfoRow(Icons.Filled.Security, "Privacy", "All data stored on device")
                }
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsInfoRow(icon: ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
