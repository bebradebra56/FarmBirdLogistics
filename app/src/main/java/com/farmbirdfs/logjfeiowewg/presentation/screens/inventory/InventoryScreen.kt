package com.farmbirdfs.logjfeiowewg.presentation.screens.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.farmbirdfs.logjfeiowewg.data.local.entity.InventoryCategory
import com.farmbirdfs.logjfeiowewg.data.local.entity.InventoryItemEntity
import com.farmbirdfs.logjfeiowewg.data.repository.InventoryRepository
import com.farmbirdfs.logjfeiowewg.presentation.components.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

class InventoryViewModel(private val repository: InventoryRepository) : ViewModel() {
    val items = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val lowStockItems = repository.getLowStock()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var filterCategory by mutableStateOf<String?>(null)
    val filtered: StateFlow<List<InventoryItemEntity>> = combine(items, snapshotFlow { filterCategory }) { list, cat ->
        if (cat == null) list else list.filter { it.category == cat }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var showAddDialog by mutableStateOf(false)
    var editItem by mutableStateOf<InventoryItemEntity?>(null)
    var itemName by mutableStateOf("")
    var itemCategory by mutableStateOf(InventoryCategory.EQUIPMENT)
    var itemQuantity by mutableStateOf("")
    var itemUnit by mutableStateOf("pcs")
    var itemMinStock by mutableStateOf("")
    var showCatMenu by mutableStateOf(false)

    fun openAdd() { editItem = null; itemName = ""; itemCategory = InventoryCategory.EQUIPMENT; itemQuantity = ""; itemUnit = "pcs"; itemMinStock = ""; showAddDialog = true }
    fun openEdit(item: InventoryItemEntity) {
        editItem = item; itemName = item.name; itemCategory = item.category
        itemQuantity = item.quantity.toString(); itemUnit = item.unit
        itemMinStock = if (item.minStock > 0) item.minStock.toString() else ""
        showAddDialog = true
    }

    fun save() = viewModelScope.launch {
        val entity = InventoryItemEntity(
            id = editItem?.id ?: 0L,
            name = itemName.trim(),
            category = itemCategory,
            quantity = itemQuantity.toIntOrNull() ?: 0,
            unit = itemUnit.trim(),
            minStock = itemMinStock.toIntOrNull() ?: 0
        )
        if (editItem != null) repository.update(entity) else repository.insert(entity)
        showAddDialog = false
    }

    fun delete(item: InventoryItemEntity) = viewModelScope.launch { repository.delete(item) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    navController: NavController,
    viewModel: InventoryViewModel = koinViewModel()
) {
    val items by viewModel.filtered.collectAsState()
    val lowStock by viewModel.lowStockItems.collectAsState()
    var deleteTarget by remember { mutableStateOf<InventoryItemEntity?>(null) }

    Scaffold(
        topBar = {
            FarmTopBar(
                title = "Inventory",
                onBack = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openAdd() }) {
                Icon(Icons.Filled.Add, null)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (lowStock.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${lowStock.size} item(s) low on stock",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(selected = viewModel.filterCategory == null, onClick = { viewModel.filterCategory = null }, label = { Text("All") })
                }
                items(InventoryCategory.all) { cat ->
                    FilterChip(
                        selected = viewModel.filterCategory == cat,
                        onClick = { viewModel.filterCategory = if (viewModel.filterCategory == cat) null else cat },
                        label = { Text(cat) }
                    )
                }
            }

            if (items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(icon = Icons.Filled.Inventory, title = "No inventory items", subtitle = "Track containers, feeders, medicines and more")
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items, key = { it.id }) { item ->
                        InventoryItemCard(
                            item = item,
                            onEdit = { viewModel.openEdit(item) },
                            onDelete = { deleteTarget = item }
                        )
                    }
                }
            }
        }
    }

    if (viewModel.showAddDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showAddDialog = false },
            title = { Text(if (viewModel.editItem != null) "Edit Item" else "Add Item") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = viewModel.itemName, onValueChange = { viewModel.itemName = it }, label = { Text("Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    ExposedDropdownMenuBox(expanded = viewModel.showCatMenu, onExpandedChange = { viewModel.showCatMenu = it }) {
                        OutlinedTextField(value = viewModel.itemCategory, onValueChange = {}, label = { Text("Category") }, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(viewModel.showCatMenu) }, modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable))
                        ExposedDropdownMenu(expanded = viewModel.showCatMenu, onDismissRequest = { viewModel.showCatMenu = false }) {
                            InventoryCategory.all.forEach { c -> DropdownMenuItem(text = { Text(c) }, onClick = { viewModel.itemCategory = c; viewModel.showCatMenu = false }) }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = viewModel.itemQuantity, onValueChange = { viewModel.itemQuantity = it.filter { c -> c.isDigit() } }, label = { Text("Qty *") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = viewModel.itemUnit, onValueChange = { viewModel.itemUnit = it }, label = { Text("Unit") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(value = viewModel.itemMinStock, onValueChange = { viewModel.itemMinStock = it.filter { c -> c.isDigit() } }, label = { Text("Min Stock Alert") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.save() }, enabled = viewModel.itemName.isNotBlank()) { Text("Save") } },
            dismissButton = { TextButton(onClick = { viewModel.showAddDialog = false }) { Text("Cancel") } }
        )
    }

    deleteTarget?.let { item ->
        DeleteDialog("Remove Item", "Remove \"${item.name}\"?", onConfirm = { viewModel.delete(item); deleteTarget = null }, onDismiss = { deleteTarget = null })
    }
}

@Composable
fun InventoryItemCard(item: InventoryItemEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    val isLowStock = item.minStock > 0 && item.quantity <= item.minStock
    Card(onClick = onEdit, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (isLowStock) MaterialTheme.colorScheme.errorContainer.copy(0.3f) else MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                    .background(if (isLowStock) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(if (isLowStock) Icons.Filled.Warning else Icons.Filled.Inventory, null,
                    tint = if (isLowStock) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(item.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${item.quantity} ${item.unit}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) }
        }
    }
}
