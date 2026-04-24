package com.farmbirdfs.logjfeiowewg.presentation.screens.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.farmbirdfs.logjfeiowewg.data.local.entity.InventoryCategory
import com.farmbirdfs.logjfeiowewg.presentation.components.*
import org.koin.androidx.compose.koinViewModel

@Composable
fun SuppliesScreen(
    navController: NavController,
    viewModel: InventoryViewModel = koinViewModel()
) {
    val supplyCategories = listOf(InventoryCategory.FEED, InventoryCategory.MEDICINE, InventoryCategory.BEDDING)
    val allItems by viewModel.items.collectAsState()
    val suppliesItems = allItems.filter { it.category in supplyCategories }

    Scaffold(
        topBar = {
            FarmTopBar(title = "Supplies", onBack = { navController.popBackStack() })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.itemCategory = InventoryCategory.FEED
                viewModel.openAdd()
            }) {
                Icon(Icons.Filled.Add, null)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    supplyCategories.forEach { cat ->
                        val count = allItems.count { it.category == cat }
                        StatCard(
                            label = cat,
                            value = "$count items",
                            icon = when (cat) {
                                InventoryCategory.FEED -> Icons.Filled.Grain
                                InventoryCategory.MEDICINE -> Icons.Filled.MedicalServices
                                else -> Icons.Filled.Layers
                            },
                            modifier = Modifier.weight(1f),
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            if (suppliesItems.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.Grain,
                        title = "No supply items",
                        subtitle = "Track feed, medicine, and bedding here"
                    )
                }
            } else {
                items(suppliesItems, key = { it.id }) { item ->
                    InventoryItemCard(
                        item = item,
                        onEdit = { viewModel.openEdit(item) },
                        onDelete = { viewModel.delete(item) }
                    )
                }
            }
        }
    }

    if (viewModel.showAddDialog) {
        SuppliesAddDialogContent(viewModel = viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuppliesAddDialogContent(viewModel: InventoryViewModel) {
    val supplyCategories = listOf(InventoryCategory.FEED, InventoryCategory.MEDICINE, InventoryCategory.BEDDING)
    AlertDialog(
        onDismissRequest = { viewModel.showAddDialog = false },
        title = { Text(if (viewModel.editItem != null) "Edit Supply" else "Add Supply") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = viewModel.itemName, onValueChange = { viewModel.itemName = it }, label = { Text("Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                ExposedDropdownMenuBox(expanded = viewModel.showCatMenu, onExpandedChange = { viewModel.showCatMenu = it }) {
                    OutlinedTextField(value = viewModel.itemCategory, onValueChange = {}, label = { Text("Category") }, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(viewModel.showCatMenu) }, modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable))
                    ExposedDropdownMenu(expanded = viewModel.showCatMenu, onDismissRequest = { viewModel.showCatMenu = false }) {
                        supplyCategories.forEach { c -> DropdownMenuItem(text = { Text(c) }, onClick = { viewModel.itemCategory = c; viewModel.showCatMenu = false }) }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = viewModel.itemQuantity, onValueChange = { viewModel.itemQuantity = it.filter { c -> c.isDigit() } }, label = { Text("Qty *") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = viewModel.itemUnit, onValueChange = { viewModel.itemUnit = it }, label = { Text("Unit") }, singleLine = true, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = { TextButton(onClick = { viewModel.save() }, enabled = viewModel.itemName.isNotBlank()) { Text("Save") } },
        dismissButton = { TextButton(onClick = { viewModel.showAddDialog = false }) { Text("Cancel") } }
    )
}
