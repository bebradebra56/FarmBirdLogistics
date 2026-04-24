package com.farmbirdfs.logjfeiowewg.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import com.farmbirdfs.logjfeiowewg.navigation.Screen

data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("Home", Screen.Dashboard.route, Icons.Filled.Home, Icons.Filled.Home),
    BottomNavItem("Transport", Screen.TransportPlans.route, Icons.Filled.LocalShipping, Icons.Filled.LocalShipping),
    BottomNavItem("Birds", Screen.BirdGroups.route, Icons.Filled.Pets, Icons.Filled.Pets),
    BottomNavItem("Calendar", Screen.Calendar.route, Icons.Filled.CalendarMonth, Icons.Filled.CalendarMonth),
    BottomNavItem("More", Screen.More.route, Icons.Filled.GridView, Icons.Filled.GridView)
)

@Composable
fun BottomNavBar(navController: NavController, currentRoute: String?) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(Screen.Dashboard.route) {
                            saveState = item.route != Screen.Dashboard.route
                            inclusive = item.route == Screen.Dashboard.route
                        }
                        launchSingleTop = true
                        restoreState = item.route != Screen.Dashboard.route
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}
