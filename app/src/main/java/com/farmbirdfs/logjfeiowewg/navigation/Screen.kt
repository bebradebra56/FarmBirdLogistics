package com.farmbirdfs.logjfeiowewg.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object TransportPlans : Screen("transport_plans")
    object CreateTransport : Screen("create_transport/{transportId}") {
        fun createRoute(transportId: Long = -1L) = "create_transport/$transportId"
    }
    object BirdGroups : Screen("bird_groups")
    object AddBirdGroup : Screen("add_bird_group/{groupId}") {
        fun createRoute(groupId: Long = -1L) = "add_bird_group/$groupId"
    }
    object Containers : Screen("containers")
    object AddContainer : Screen("add_container/{containerId}") {
        fun createRoute(containerId: Long = -1L) = "add_container/$containerId"
    }
    object Loading : Screen("loading/{transportId}") {
        fun createRoute(transportId: Long) = "loading/$transportId"
    }
    object LoadingDetails : Screen("loading_details/{transportId}") {
        fun createRoute(transportId: Long) = "loading_details/$transportId"
    }
    object Route : Screen("route/{transportId}") {
        fun createRoute(transportId: Long) = "route/$transportId"
    }
    object TransportConditions : Screen("conditions/{transportId}") {
        fun createRoute(transportId: Long) = "conditions/$transportId"
    }
    object Arrival : Screen("arrival/{transportId}") {
        fun createRoute(transportId: Long) = "arrival/$transportId"
    }
    object Housing : Screen("housing")
    object AddHousing : Screen("add_housing/{housingId}") {
        fun createRoute(housingId: Long = -1L) = "add_housing/$housingId"
    }
    object Feeding : Screen("feeding")
    object FeedRecord : Screen("feed_record/{recordId}") {
        fun createRoute(recordId: Long = -1L) = "feed_record/$recordId"
    }
    object HealthCheck : Screen("health_check")
    object HealthRecord : Screen("health_record/{recordId}") {
        fun createRoute(recordId: Long = -1L) = "health_record/$recordId"
    }
    object Inventory : Screen("inventory")
    object Supplies : Screen("supplies")
    object Calendar : Screen("calendar")
    object Tasks : Screen("tasks")
    object AddTask : Screen("add_task/{taskId}") {
        fun createRoute(taskId: Long = -1L) = "add_task/$taskId"
    }
    object Reports : Screen("reports")
    object ActivityHistory : Screen("activity_history")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object More : Screen("more")
}

val bottomNavScreens = listOf(
    Screen.Dashboard.route,
    Screen.TransportPlans.route,
    Screen.BirdGroups.route,
    Screen.Calendar.route,
    Screen.More.route
)
