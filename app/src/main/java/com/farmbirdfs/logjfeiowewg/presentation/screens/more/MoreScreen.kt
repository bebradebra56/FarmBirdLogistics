package com.farmbirdfs.logjfeiowewg.presentation.screens.more

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.farmbirdfs.logjfeiowewg.navigation.Screen
import com.farmbirdfs.logjfeiowewg.presentation.components.FarmTopBar

data class MoreItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val containerColor: Color,
    val contentColor: Color
)

@Composable
fun MoreScreen(navController: NavController) {
    val context = LocalContext.current
    val items = listOf(
        MoreItem(
            "Housing",
            Icons.Filled.Home,
            Screen.Housing.route,
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32)
        ),
        MoreItem(
            "Feeding",
            Icons.Filled.Grain,
            Screen.Feeding.route,
            Color(0xFFFFF8E1),
            Color(0xFFF57F17)
        ),
        MoreItem(
            "Health",
            Icons.Filled.HealthAndSafety,
            Screen.HealthCheck.route,
            Color(0xFFFFEBEE),
            Color(0xFFC62828)
        ),
        MoreItem(
            "Inventory",
            Icons.Filled.Inventory,
            Screen.Inventory.route,
            Color(0xFFE3F2FD),
            Color(0xFF1565C0)
        ),
        MoreItem(
            "Supplies",
            Icons.Filled.Layers,
            Screen.Supplies.route,
            Color(0xFFF3E5F5),
            Color(0xFF6A1B9A)
        ),
        MoreItem(
            "Containers",
            Icons.Filled.Inventory2,
            Screen.Containers.route,
            Color(0xFFFFF3E0),
            Color(0xFFE65100)
        ),
        MoreItem(
            "Reports",
            Icons.Filled.BarChart,
            Screen.Reports.route,
            Color(0xFFE8EAF6),
            Color(0xFF283593)
        ),
        MoreItem(
            "Activity",
            Icons.Filled.History,
            Screen.ActivityHistory.route,
            Color(0xFFE0F7FA),
            Color(0xFF006064)
        ),
        MoreItem(
            "Tasks",
            Icons.Filled.CheckCircle,
            Screen.Tasks.route,
            Color(0xFFF1F8E9),
            Color(0xFF33691E)
        ),
        MoreItem(
            "Profile",
            Icons.Filled.Person,
            Screen.Profile.route,
            Color(0xFFFCE4EC),
            Color(0xFFAD1457)
        ),
        MoreItem(
            "Settings",
            Icons.Filled.Settings,
            Screen.Settings.route,
            Color(0xFFECEFF1),
            Color(0xFF37474F)
        )
    )

    Scaffold(
        topBar = { FarmTopBar(title = "More") }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                MoreGridItem(item = item, onClick = { navController.navigate(item.route) })
            }
            item {
                MoreGridItem(
                    item = MoreItem(
                        "Privacy Policy",
                        Icons.Filled.Policy,
                        Screen.Housing.route,
                        Color(0xFFE8F5E9),
                        Color(0xFF2E7D32)
                    ), onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://farmbirdlogistics.com/privacy-policy.html"))
                        context.startActivity(intent)
                    })
            }
        }
    }
}

@Composable
private fun MoreGridItem(item: MoreItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(item.containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.contentColor,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
