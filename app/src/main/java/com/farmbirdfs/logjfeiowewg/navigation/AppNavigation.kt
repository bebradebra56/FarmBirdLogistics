package com.farmbirdfs.logjfeiowewg.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.farmbirdfs.logjfeiowewg.presentation.screens.arrival.ArrivalScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.birds.AddBirdGroupScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.birds.BirdGroupsScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.calendar.CalendarScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.conditions.TransportConditionsScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.containers.AddContainerScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.containers.ContainersScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.dashboard.DashboardScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.feeding.AddFeedRecordScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.feeding.FeedingScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.health.AddHealthRecordScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.health.HealthCheckScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.history.ActivityHistoryScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.housing.AddHousingScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.housing.HousingScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.inventory.InventoryScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.inventory.SuppliesScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.loading.LoadingDetailsScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.loading.LoadingScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.more.MoreScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.onboarding.OnboardingScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.profile.ProfileScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.reports.ReportsScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.route.RouteScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.settings.SettingsScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.splash.SplashScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.tasks.AddTaskScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.tasks.TasksScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.transport.CreateTransportScreen
import com.farmbirdfs.logjfeiowewg.presentation.screens.transport.TransportPlansScreen

@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }

        composable(Screen.TransportPlans.route) {
            TransportPlansScreen(navController = navController)
        }

        composable(
            route = Screen.CreateTransport.route,
            arguments = listOf(navArgument("transportId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("transportId") ?: -1L
            CreateTransportScreen(transportId = id, navController = navController)
        }

        composable(Screen.BirdGroups.route) {
            BirdGroupsScreen(navController = navController)
        }

        composable(
            route = Screen.AddBirdGroup.route,
            arguments = listOf(navArgument("groupId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("groupId") ?: -1L
            AddBirdGroupScreen(groupId = id, navController = navController)
        }

        composable(Screen.Containers.route) {
            ContainersScreen(navController = navController)
        }

        composable(
            route = Screen.AddContainer.route,
            arguments = listOf(navArgument("containerId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("containerId") ?: -1L
            AddContainerScreen(containerId = id, navController = navController)
        }

        composable(
            route = Screen.Loading.route,
            arguments = listOf(navArgument("transportId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("transportId") ?: 0L
            LoadingScreen(transportId = id, navController = navController)
        }

        composable(
            route = Screen.LoadingDetails.route,
            arguments = listOf(navArgument("transportId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("transportId") ?: 0L
            LoadingDetailsScreen(transportId = id, navController = navController)
        }

        composable(
            route = Screen.Route.route,
            arguments = listOf(navArgument("transportId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("transportId") ?: 0L
            RouteScreen(transportId = id, navController = navController)
        }

        composable(
            route = Screen.TransportConditions.route,
            arguments = listOf(navArgument("transportId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("transportId") ?: 0L
            TransportConditionsScreen(transportId = id, navController = navController)
        }

        composable(
            route = Screen.Arrival.route,
            arguments = listOf(navArgument("transportId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("transportId") ?: 0L
            ArrivalScreen(transportId = id, navController = navController)
        }

        composable(Screen.Housing.route) {
            HousingScreen(navController = navController)
        }

        composable(
            route = Screen.AddHousing.route,
            arguments = listOf(navArgument("housingId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("housingId") ?: -1L
            AddHousingScreen(housingId = id, navController = navController)
        }

        composable(Screen.Feeding.route) {
            FeedingScreen(navController = navController)
        }

        composable(
            route = Screen.FeedRecord.route,
            arguments = listOf(navArgument("recordId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("recordId") ?: -1L
            AddFeedRecordScreen(recordId = id, navController = navController)
        }

        composable(Screen.HealthCheck.route) {
            HealthCheckScreen(navController = navController)
        }

        composable(
            route = Screen.HealthRecord.route,
            arguments = listOf(navArgument("recordId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("recordId") ?: -1L
            AddHealthRecordScreen(recordId = id, navController = navController)
        }

        composable(Screen.Inventory.route) {
            InventoryScreen(navController = navController)
        }

        composable(Screen.Supplies.route) {
            SuppliesScreen(navController = navController)
        }

        composable(Screen.Calendar.route) {
            CalendarScreen(navController = navController)
        }

        composable(Screen.Tasks.route) {
            TasksScreen(navController = navController)
        }

        composable(
            route = Screen.AddTask.route,
            arguments = listOf(navArgument("taskId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("taskId") ?: -1L
            AddTaskScreen(taskId = id, navController = navController)
        }

        composable(Screen.Reports.route) {
            ReportsScreen(navController = navController)
        }

        composable(Screen.ActivityHistory.route) {
            ActivityHistoryScreen(navController = navController)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }

        composable(Screen.More.route) {
            MoreScreen(navController = navController)
        }
    }
}
